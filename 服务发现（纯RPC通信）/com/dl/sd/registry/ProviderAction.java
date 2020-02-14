package com.dl.sd.registry;

import java.util.List;

import com.dl.rpc.server.RPCProxyAnntotation;
import com.dl.sd.netWork.NetNode;
import com.dl.sd.provider.IProviderAction;

@RPCProxyAnntotation(interfaces = { IProviderAction.class })
public class ProviderAction implements IProviderAction {
	private ServiceCache serviceCache;
	
	public ProviderAction() {
		serviceCache = new ServiceCache();
	}

	@Override
	public void registryService(List<String> serviceList, NetNode netNode) throws Throwable {
		for (String registryTag : serviceList) {
			serviceCache.put(registryTag, netNode);
		}
	}

	@Override
	public void cancellationService(List<String> serviceList, NetNode netNode) throws Throwable {
		for (String registryTag : serviceList) {
			serviceCache.remove(registryTag, netNode);
		}
	}

}
