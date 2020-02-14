package com.dl.sd.registry;

import com.dl.rpc.server.RPCProxyAnntotation;
import com.dl.sd.consumer.ICenterAction;
import com.dl.sd.netWork.NetNode;

/**
 * ��Դ����˺���Դ��������RPCͨ�ŵĽӿ�ʵ����
 * @author dl
 *
 */
@RPCProxyAnntotation(interfaces = { ICenterAction.class })
public class CenterAction implements ICenterAction {
	private ServiceCache ServiceCache;
	
	public CenterAction() {
		ServiceCache = new ServiceCache();	
	}
	
	@Override
	public void reportDropped(NetNode netNode) throws Throwable {
		ServiceCache.remove(netNode);
	}


}
