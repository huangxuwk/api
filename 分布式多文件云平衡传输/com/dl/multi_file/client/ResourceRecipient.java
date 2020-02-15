package com.dl.multi_file.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.dl.multi_file.netWork.Transmission;
import com.dl.multi_file.progress_bar.IProgressManager;
import com.dl.multi_file.resource.LeakageManager;
import com.dl.multi_file.resource.LocalResources;
import com.dl.multi_file.resource.SectionInfo;
import com.dl.multi_file.server.ProvideServer;
import com.dl.sd.netWork.NetNode;
import com.util.ThreadPoolFactory;

/**
 * 资源接收者<br>
 * 1、开启短暂的服务器进行不同结点资源的接收；<br>
 * 2、若在传输中断开连接需要进行管理中心的rpc报备和断点续传；<br>
 * 3、一个客户端最多同时开启一个资源接收者；
 * @author dl
 *
 */
public class ResourceRecipient implements Runnable {
	public static final long DEFAULT_MAX_RESPONSE_TIME = 50;
	
	private static Transmission transmission = new Transmission();
	
	private ServerSocket recipient;
	private NetNode recipientNode;

	private LocalResources localResources;
	private LeakageManager leakageManager;
	private volatile IProgressManager progressManager;
	
	private volatile int sendCount;
	private volatile int successCount;
	private Timer timer;
	/**
	 * 开启资源接收服务器后的最长响应时间；到达后关闭接收；
	 */
	private long maxResponseTime;
	private volatile boolean goon;
	private Map<String, RandomAccessFile> randomMap;
	
	private List<InnerReceiver> innerReceiverList;
	
	public ResourceRecipient() {
		randomMap = new HashMap<>();
		innerReceiverList = new ArrayList<>();
		localResources = new LocalResources();
		maxResponseTime = DEFAULT_MAX_RESPONSE_TIME;
	}
	
	public void setLeakageManager(LeakageManager leakageManager) {
		this.leakageManager = leakageManager;
	}
	
	public void setSendCount(int sendCount) {
		this.sendCount = sendCount;
	}
	
	public void setMaxResponseTime(long maxResponseTime) {
		this.maxResponseTime = maxResponseTime;
	}
	
	public void setProgressManager(IProgressManager progressManager) {
		this.progressManager = progressManager;
	}
	
	/**
	 * 得到本接受者的结点信息<br>
	 * 1、ip、port组成的recipientNode对象；<br>
	 * 2、若为空，会先创建recipientNode对象；
	 * @return
	 */
	public NetNode getRecipientNode() {
		if (this.recipientNode == null) {
			String ip = ProvideServer.getLocalIp();
			if (!PortPool.hasNext()) {
				return null;
			}
			int port = PortPool.borrow();
			NetNode node = new NetNode(ip, port);
			this.recipientNode = node;
		}
		return recipientNode;
	}
	
	/**
	 * 1、开启侦听发送者连接线程;<br>
	 * 2、开启定时器，超过阈值时间将停止接收资源和进行断点续传；<br>
	 * 3、归还端口号；
	 */
	public void startupReceive() {
		try {
			recipient = new ServerSocket(recipientNode.getPort());
			new ThreadPoolFactory().execute(this);
			timer = new Timer();
			timer.schedule(new TimerTask() {
				int timeCount = 0;
				@Override
				public void run() {
					if ((++timeCount) >= maxResponseTime) {
						shutdown();
						timer.cancel();
					}
				}
			}, 0, 1000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭接收服务器；关闭线程组线程；关闭写文件对象；断点续传；
	 */
	private void shutdown() {
		goon = true;
		try {
			if (recipient != null && recipient.isClosed()) {
				recipient.close();				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			recipient = null;
		}
		
		// 打断资源接收
		for (InnerReceiver innerReceiver : innerReceiverList) {
			innerReceiver.closeDis();
		}
		
		Collection<RandomAccessFile> randomList = randomMap.values();
		for (RandomAccessFile randomAccessFile : randomList) {
			try {
				if (randomAccessFile != null) {
					randomAccessFile.close();					
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				randomAccessFile = null;
			}
		}
		if (progressManager != null) {
			progressManager.closeProgressBar();			
		}
		leakageManager.checkReceiveCompleted();
		PortPool.giveBack(recipientNode.getPort());
	}
	
	/**
	 * 将写进文件的RandomAccessFile放在容器中；
	 * 防止多次打开和关闭，等接收完毕后统一关闭；
	 * @param path
	 * @return
	 */
	private RandomAccessFile getRandomAccessFile(SectionInfo section) {
		String path = section.getFileName();
		RandomAccessFile random = randomMap.get(path);
		if (random == null) {
			random = localResources.getRandomAccessFile(path);
			randomMap.put(path, random);
		}
		return random;
	}
	
	@Override
	public void run() {
		for (int i = 0; i < sendCount; i++) {
			try {
				Socket receiver = recipient.accept();
				InnerReceiver inReceiver = new InnerReceiver(receiver); 
				innerReceiverList.add(inReceiver);
				new ThreadPoolFactory().execute(new Thread(inReceiver));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 内部接收者类<br>
	 * 1、根据给定的接收者得到通信信道；<br>
	 * 2、先读取SectionInfo对象，根据是否为null来判断是否读取完成；<br>
	 * 3、无论如何要在线程结束时将successCount标记加一；
	 * @author dl
	 *
	 */
	class InnerReceiver implements Runnable {
		private Socket receiver;
		private DataInputStream dis;
		
		public InnerReceiver(Socket receiver) {
			this.receiver = receiver;
		}
		
		@Override
		public void run() {
			try {
				dis = new DataInputStream(receiver.getInputStream());
				
				SectionInfo section;
				byte[] datas;
				// 判断接收的secion对象是否为null，若是，则接收完毕
				while ((section = transmission.recvfrom(dis)) != null) {
					datas = transmission.recvfrom(dis, section.getSize());
					RandomAccessFile random = getRandomAccessFile(section);
					random.seek(section.getOffset());
					// 写入文件
					localResources.writeInLocal(random, datas, 0, section.getSize());
					// 更新断点续传
					leakageManager.receiverNews(section);
					// 更新进度条信息
					if (progressManager != null) {
						progressManager.receiveNewSection(section);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (++successCount == sendCount && goon != true) {
					timer.cancel();
					shutdown();
				}
			}
		}
		
		public void closeDis() {
			try {
				if (dis != null) {
					dis.close();	
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				dis = null;
			}
		}
	}
	
}
