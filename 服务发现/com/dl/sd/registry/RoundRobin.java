package com.dl.sd.registry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 轮询
 * 1、将所有连接到注册中心的服务器放在列表中；<br>
 * 2、轮询列表中的所有网络结点，依次判断是否有消息；<br>
 * 3、若有消息，则开启一个新的线程去处理消息；<br>
 * 4、若某结点宕机，则删除该结点；
 * 
 * @author dl
 *
 */
public class RoundRobin implements Runnable {
	// 线程安全的列表
	private static final List<CenterConversation> coPool = new CopyOnWriteArrayList<>();
	
	private IDealMessage dealMessage;
	private ServiceCache serviceCache;
	private volatile boolean goon;
	
	public RoundRobin() {
		serviceCache = new ServiceCache();
		goon = true;
	}
	
	public void setDealMessage(IDealMessage dealMessage) {
		this.dealMessage = dealMessage;
	}
	
	public List<CenterConversation> getCoList() {
		return new ArrayList<CenterConversation>(coPool);
	}
	
	/**
	 * 通过传递进来的socket来打包形成一个CenterConversation对象并放入池子中
	 * @param socket
	 */
	public void addCommunication(Socket socket) {
		try {
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			CenterConversation conversation = new CenterConversation(socket, dis, dos);
			conversation.setDealMessage(dealMessage);
			conversation.setRoundRobin(this);
			coPool.add(conversation);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void removeConversation(CenterConversation conversation) {
		coPool.remove(conversation);
		serviceCache.remove(conversation.getNetNode());
	}
	
	public void stopRound() {
		goon = false;
	}

	@Override
	public void run() {
		while (goon) {
			Iterator<CenterConversation> iterator = coPool.iterator();
			while (iterator.hasNext()) {
				CenterConversation co = iterator.next();
				try {
					co.judgeRead();
				} catch (IOException e) {
					// 通信断裂，删除该服务器
					removeConversation(co);
				}
			}
		}
	}
}
