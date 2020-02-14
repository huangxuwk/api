package com.dl.sd.registry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.dl.sd.netWork.INetNode;
import com.dl.sd.netWork.NetNode;
import com.util.ThreadPoolFactory;

/**
 * 服务缓存类<br>
 * 1、对注册的服务器和服务列表进行缓存；<br>
 * 2、将信息缓存到以服务号为键，服务器列表为值的map中；<br>
 * 3、支持服务和服务器的增删；
 * 
 * @author dl
 *
 */
public class ServiceCache {
	private static final ConcurrentHashMap<String, List<NetNode>> netPool;
	private static final CopyOnWriteArrayList<NetNode> nodeList;
	
	static {
		netPool = new ConcurrentHashMap<>();
		nodeList = new CopyOnWriteArrayList<>();
	}
	
	public ServiceCache() {
	}
	
	public CopyOnWriteArrayList<NetNode> getNodelist() {
		return nodeList;
	}
	
	public void put(String registryTag, NetNode netNode) {
		List<NetNode> nodeList = netPool.get(registryTag);
		if (nodeList == null) {
			nodeList = new ArrayList<NetNode>();
			netPool.put(registryTag, nodeList);
		}
		if (!nodeList.contains(netNode)) {
			nodeList.add(netNode);	
		}
		if (!nodeList.contains(netNode)) {
			nodeList.add(netNode);
		}
	}
	
	public void remove(String serviceTag, INetNode netNode) {
		new ThreadPoolFactory().execute(new Thread(new InnerRemove(serviceTag, netNode)));
	}
	
	public void remove(INetNode netNode) {
		new ThreadPoolFactory().execute(new Thread(new InnerRemove(null, netNode)));
	}
	
	public List<NetNode> getNodeList(String serviceTag) {
		return netPool.get(serviceTag);
	}
	
	public List<String> getAllServiceTags() {
		return	new ArrayList<>(netPool.keySet());
	}
	
	private class InnerRemove implements Runnable {
		private String serviceTag;
		private INetNode netNode;

		public InnerRemove(String serviceTag, INetNode netNode) {
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
