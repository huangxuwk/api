package com.dl.sd.registry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.dl.sd.netWork.NetNode;
import com.util.ThreadPoolFactory;

/**
 * 服务缓存类<br>
 * 1、对注册的服务提供者和服务列表进行缓存；<br>
 * 2、将信息缓存到以服务标签为键，服务提供者列表为值的map中；<br>
 * 3、支持服务和服务提供者的增删；
 * 
 * @author dl
 *
 */
public class ServiceCache {
	private static final ConcurrentHashMap<String, List<NetNode>> netPool;
	
	static {
		netPool = new ConcurrentHashMap<>();
	}
	
	public void put(String registryTag, NetNode netNode) {
		System.out.println(netNode);
		List<NetNode> nodeList = netPool.get(registryTag);
		if (nodeList == null) {
			nodeList = new ArrayList<NetNode>();
			netPool.put(registryTag, nodeList);
		}
		nodeList.add(netNode);
	}
	
	public List<String> getAllServiceTags() {
		return	new ArrayList<>(netPool.keySet());
	}
	
	public void remove(String serviceTag, NetNode netNode) {
		ThreadPoolFactory.execute(new Thread(new InnerRemove(serviceTag, netNode)));
	}
	
	public void remove(NetNode netNode) {
		ThreadPoolFactory.execute(new Thread(new InnerRemove(null, netNode)));
	}
	
	public List<NetNode> getNodeList(String serviceTag) {
		return netPool.get(serviceTag);
	}
	
	/**
	 * 内部删除类<br>
	 * 1、删除某服务对应的某结点；<br>
	 * 2、删除所有服务中的某结点；<br>
	 * 3、由于删除的过程比较耗时，所有放在线程中删除是值得的；
	 * @author dl
	 *
	 */
	private class InnerRemove implements Runnable {
		private String serviceTag;
		private NetNode netNode;

		public InnerRemove(String serviceTag, NetNode netNode) {
			this.serviceTag = serviceTag;
			this.netNode = netNode;
		}	
		
		@Override
		public void run() {
			if (serviceTag != null) {
				List<NetNode> tagList = netPool.get(serviceTag);
				tagList.remove(netNode);
			} else {
				Set<?> set = netPool.keySet();
				Iterator<?> iterator = set.iterator();
				while (iterator.hasNext()) {
					String tag = (String) iterator.next();
					List<NetNode> nodeList = netPool.get(tag);
					if (nodeList != null) {
						nodeList.remove(netNode);
						if (nodeList.isEmpty()) {
							iterator.remove();
						}
					}
				}
			}
		}
		
	}
	
}
