package com.dl.sd.consumer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.dl.sd.netWork.NetNode;

/**
 * 服务消费者缓存器<br>
 * 1、可对申请到的服务和服务提供者列表进行缓存；<br>
 * 2、与注册中心的缓存器功能一致；<br>
 * 3、服务消费者缓存器的存在避免了注册中心宕机后无法使用服务的问题；
 * 
 * @author dl
 *
 */
public class ClientCache {
	private static final Map<String, List<NetNode>> netPool = new ConcurrentHashMap<>();
	
	public ClientCache() {
	}
	
	public void addServiceTags(List<String> serviceTags) {
		netPool.clear();
		for (String string : serviceTags) {
			netPool.put(string, null);
		}
	}
	
	public void addService(String serviceTag, List<NetNode> nodeList) {
		netPool.put(serviceTag, nodeList);
	}
	
	public List<NetNode> getServerList(String serviceTag) {
		return netPool.get(serviceTag);
	}
	
	public Set<String> keySet() {
		return netPool.keySet();
	}
}
