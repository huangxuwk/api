package com.dl.sd.provider;

import java.util.List;

import com.dl.sd.netWork.NetNode;

public interface IProviderAction {
	void registryService(List<String> serviceList, NetNode netNode) throws Throwable;
	void cancellationService(List<String> serviceList, NetNode netNode)  throws Throwable;
}
