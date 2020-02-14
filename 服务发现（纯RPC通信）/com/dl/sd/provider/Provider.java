package com.dl.sd.provider;

import java.util.List;

import com.dl.rpc.client.RPCClient;
import com.dl.rpc.client.RPCClientProxy;
import com.dl.rpc.server.RPCMethodFactory;
import com.dl.sd.netWork.NetNode;

/**
 * �����ṩ��<br>
 * ��ע������ע��/ע������<br>
 * 
 * @author dl
 *
 */
public class Provider {
	private RPCClient rpcClient;
	private IProviderAction action;
	
	private NetNode netNode;
	
	public Provider() {
		RPCMethodFactory.scanPackage("com.dl.sd");
		rpcClient = new RPCClient();
		RPCClientProxy clientProxy = new RPCClientProxy();
		clientProxy.setClient(rpcClient);
		action = clientProxy.jdkProxy(IProviderAction.class);
	}
	
	public void setNetNode(NetNode netNode) {
		this.netNode = netNode;
	}
	
	public void readConfig(String path) {
		rpcClient.readConfig(path);
	}
	
	/**
	 * ע�������rpc���𣬿��ܻᷢ��ʧ�ܣ�<br>
	 * ʧ�ܺ��֪��һ�㣻
	 * @param serviceList
	 * @throws Throwable 
	 */
	public void registryService(List<String> serviceList) throws Throwable {
		action.registryService(serviceList, netNode);
	}
	
	/**
	 * ע��������rpc���𣬿��ܻᷢ��ʧ�ܣ�<br>
	 * ʧ�ܺ��֪��һ�㣻
	 * @param serviceList
	 * @throws Throwable 
	 */
	public void cancellationService(List<String> serviceList) throws Throwable {
		action.cancellationService(serviceList, netNode);	
	}
	
}
