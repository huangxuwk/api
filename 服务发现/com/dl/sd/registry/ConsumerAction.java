package com.dl.sd.registry;

import java.util.List;

import com.dl.rpc.server.ProxyAnntotation;
import com.dl.sd.consumer.IConsumerAction;
import com.dl.sd.netWork.NetNode;

@ProxyAnntotation(interfaces = { IConsumerAction.class })
public class ConsumerAction implements IConsumerAction {
	private ServiceCache serviceCache;
	
	public ConsumerAction() {
		serviceCache = new ServiceCache();
	}
	
	
	@Override
	public List<String> updataServiceTags() {
		return serviceCache.getAllServiceTags();
	}

	@Override
	public List<NetNode> updataNews(String serviceTag) {
		return serviceCache.getNodeList(serviceTag);
	}

}
