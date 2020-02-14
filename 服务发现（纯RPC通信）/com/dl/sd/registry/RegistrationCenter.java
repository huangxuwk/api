package com.dl.sd.registry;

import com.dl.rpc.server.RPCMethodFactory;
import com.dl.rpc.server.RPCServer;

/**
 * 注册中心服务器<br>
 * 1、开启注册中心服务器和RPC服务器；<br>
 * 2、开启心跳检测，开启线程池；<br>
 * 3、两个服务器的ip、port都可配置，心跳时延也可配置；<br>
 * 4、开启侦听连接线程，开启轮询线程；<br>
 * 5、支持对所有的开启项进行关闭；
 * 
 * @author dl
 *
 */
public class RegistrationCenter {
	private RPCServer rpcServer;
	
	private Heartbeat heartbeat;
	
	public RegistrationCenter() {
		RPCMethodFactory.scanPackage("com.dl.sd");
		rpcServer = new RPCServer();
		heartbeat = new Heartbeat();
	}
	
	public void setHeartbeatTime(int heartbeatTime) {
		heartbeat.setTime(heartbeatTime);
	}
	
	public void readConfig(String path) {
		rpcServer.readConfig(path);
	}
	
	public void startup() {
		rpcServer.startup();
		heartbeat.startHeartBeat();
	}
	
	public void shutdown() {
		rpcServer.shutdown();
		heartbeat.shutdown();
	}

}
