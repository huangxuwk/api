package com.dl.sd.registry;

import com.dl.rpc.server.RPCProxyAnntotation;
import com.dl.sd.consumer.ICenterAction;
import com.dl.sd.netWork.NetNode;

/**
 * 资源请求端和资源管理中心RPC通信的接口实现类
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
