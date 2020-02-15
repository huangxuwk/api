package com.dl.multi_file.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.dl.multi_file.progress_bar.IProgressManager;
import com.dl.multi_file.progress_bar.ProgressManager;
import com.dl.multi_file.resource.LeakageManager;
import com.dl.multi_file.resource.LocalResources;
import com.dl.multi_file.resource.SectionInfo;
import com.dl.multi_file.strategy.INodeStrategy;
import com.dl.multi_file.strategy.IResourceStrategy;
import com.dl.multi_file.strategy.NodeStrategy;
import com.dl.multi_file.strategy.ResourceStrategy;
import com.dl.sd.netWork.NetNode;
import com.util.ThreadPoolFactory;

/**
 * 资源请求者<br>
 * 1、此类对需要申请的资源进行结点选择策略及资源分配策略；<br>
 * 2、结点选择策略和资源分配策略是可设置的，且系统带有默认的；<br>
 * 3、策略完成后进行服务器的rpc请求及资源的接收；<br>
 * 4、策略的实现和资源的接收需要放到单独的线程中完成；
 * @author dl
 *
 */
public class ResourceRequester {
	private volatile INodeStrategy nodeStrategy;
	private volatile IResourceStrategy resourceStrategy;
	
	private RequestClient client;
	private LocalResources localResources;
	
	/**
	 * 提供进度条的接口，由用户设置
	 */
	private volatile IProgressManager progressManager;
	/**
	 * 控制进度条的开关，用户可以不设置进度条，而使用系统默认的；
	 */
	private volatile boolean openProgressBar;
	
	public ResourceRequester() {
		localResources = new LocalResources();
	}
	
	public ResourceRequester(RequestClient client) {
		this();
		this.client = client;
	}

	public void setClient(RequestClient client) {
		this.client = client;
	}
	
	public void setNodeStrategy(INodeStrategy nodeStrategy) {
		this.nodeStrategy = nodeStrategy;
	}

	public void setResourceStrategy(IResourceStrategy resourceStrategy) {
		this.resourceStrategy = resourceStrategy;
	}
	
	/**
	 * 提供给用户设置进度条接口的方法；
	 * @param progressManager 接口实现类对象
	 */
	public void setProgressManager(IProgressManager progressManager) {
		this.progressManager = progressManager;
	}
	
	/**
	 * 提供给用户开启进度条的方法，默认不开启；
	 * @param open
	 */
	public void openProgressBar(boolean open) {
		this.openProgressBar = open;
	}

	/**
	 * 提供申请某资源的方法<br>
	 * 1、客户端可能多次请求资源，因此开启线程处理；<br>
	 * 2、用线程池处理资源请求线程；
	 * @param nodeList
	 * @param resourceHandle
	 */
	public void requestResource(String resourceHandle) {
		List<String> resourceHandles = new ArrayList<>();
		resourceHandles.add(resourceHandle);
		requestResource(resourceHandles);
	}
	
	public void requestResource(List<String> resourceHandles) {
		new ThreadPoolFactory().execute(new InnerRequester(resourceHandles));
	}
	
	/**
	 * 资源申请内部执行类<br>
	 * 1、将用户申请的资源句柄列表变为对应的资源信息列表；<br>
	 * 2、实现结点选择策略和资源分配策略；<br>
	 * 3、依次给选择的结点分配资源发送任务；<br>
	 * 4、开启资源接收线程；
	 * @author dl
	 *
	 */
	class InnerRequester implements Runnable {
		private ResourceRecipient resourceRecipient;
		private List<NetNode> nodeList;
		private List<String> resourceHandles;

		public InnerRequester(List<String> resourceHandles) {
			this.resourceHandles = resourceHandles;
			resourceRecipient = new ResourceRecipient();
		}
		
		@Override
		public void run() {
			doDefaultStrategy();
			findCommonItem(resourceHandles);
			List<SectionInfo> sectionList = new ArrayList<>();
			// 获取请求的文件片段列表
			for (String handle : resourceHandles) {
				sectionList.add(new SectionInfo(handle));
			}
			// 进行结点选择策略和资源分配策略
			List<NetNode> targetList = nodeStrategy.doNodeStrategy(nodeList);
			int sendCount = targetList.size();
			List<List<SectionInfo>> sectionLList = resourceStrategy.doResourceStrategy(sectionList, sendCount);
			
			// 取得临时建立接收服务器的ip和port
			NetNode recipientNode = resourceRecipient.getRecipientNode();
			for (int i = 0; i < sendCount; i++) {
				NetNode server = targetList.get(i);
				client.setResourceServer(server);
				try {
					client.requestResource(recipientNode, sectionLList.get(i));
				} catch (Throwable e) {
					// 服务器宕机，将掉线信息报告
					client.reportDropped(server);
					return;
				}			
			}
			// 初始化断点续传对象
			LeakageManager leakageManager = new LeakageManager(ResourceRequester.this);
			leakageManager.init(sectionList);
			resourceRecipient.setLeakageManager(leakageManager);
			// 若用户要求开启进度条
			if (openProgressBar) {
				openProgressManager(sectionList);
				resourceRecipient.setProgressManager(progressManager);
			}
			resourceRecipient.setSendCount(sendCount);
			// 任务安排完成后，开启接收服务器
			resourceRecipient.startupReceive();
		}
		
		/**
		 * 1、若用户未设置接口实现类，则使用系统默认的进度条工具；<br>
		 * 2、根据申请的资源片段信息初始化进度条；
		 * @param sectionList
		 */
		private void openProgressManager(List<SectionInfo> sectionList) {
			if (progressManager == null) {
				synchronized (ResourceRequester.class) {
					if (progressManager == null) {
						progressManager = new ProgressManager();
					}
				}
			}
			progressManager.initProgressBar(sectionList);
		}

		private void doDefaultStrategy() {
			if (nodeStrategy == null) {
				synchronized (ResourceRequester.class) {
					if (nodeStrategy == null) {
						nodeStrategy = new NodeStrategy();
					}
				}
			}
			if (resourceStrategy == null) {
				synchronized (ResourceRequester.class) {
					if (resourceStrategy == null) {
						resourceStrategy = new ResourceStrategy();
					}
				}
			}
		}
		
		/**
		 * 不同的网络结点拥有的资源不一样，因此资源管理中心不同资源句柄对应的结点列表也不一样；
		 * 需要找到给定拥有全部请求资源的结点；
		 * @param resourceHandles 请求资源列表
		 * @return
		 */
		public List<NetNode> findCommonItem(List<String> resourceHandles) {
			int length = resourceHandles.size();
			List<List<NetNode>> nodeLList = new ArrayList<>();
			for (int i = 0; i < length; i++) {
				String handle = localResources.getIntactHandle(resourceHandles.get(i));
				nodeList = client.updataNews(handle);
				nodeLList.add(nodeList);
			}
			List<NetNode> nodeList = nodeLList.get(0);
			Iterator<NetNode> iterator = null;
			NetNode netNode = null;
			for (int i = 1; i < length; i++) {
				List<NetNode> tmpList = nodeLList.get(i);
				iterator = nodeList.iterator();
				while (iterator.hasNext()) {
					netNode = iterator.next();
					if (!tmpList.contains(netNode)) {
						iterator.remove();
					}
				}
			}
			this.nodeList = nodeList;
			return nodeList;
		}
		
	}
	
}
