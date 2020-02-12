package com.dl.sd.registry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.dl.sd.netWork.NetNode;
import com.util.ThreadPoolFactory;

/**
 * ���񻺴���<br>
 * 1����ע��ķ����ṩ�ߺͷ����б���л��棻<br>
 * 2������Ϣ���浽�Է����ǩΪ���������ṩ���б�Ϊֵ��map�У�<br>
 * 3��֧�ַ���ͷ����ṩ�ߵ���ɾ��
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
	 * �ڲ�ɾ����<br>
	 * 1��ɾ��ĳ�����Ӧ��ĳ��㣻<br>
	 * 2��ɾ�����з����е�ĳ��㣻<br>
	 * 3������ɾ���Ĺ��̱ȽϺ�ʱ�����з����߳���ɾ����ֵ�õģ�
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
