package com.dl.sd.provider;

import com.dl.rpc.server.RPCProxyAnntotation;
import com.dl.sd.registry.IHeartbeatAction;

@RPCProxyAnntotation(interfaces = { IHeartbeatAction.class })
public class HeartbeatAction implements IHeartbeatAction {

	@Override
	public void askToProvider() throws Throwable {
	}

}
