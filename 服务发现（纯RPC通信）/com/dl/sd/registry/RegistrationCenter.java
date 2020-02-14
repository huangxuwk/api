package com.dl.sd.registry;

import com.dl.rpc.server.RPCMethodFactory;
import com.dl.rpc.server.RPCServer;

/**
 * ע�����ķ�����<br>
 * 1������ע�����ķ�������RPC��������<br>
 * 2������������⣬�����̳߳أ�<br>
 * 3��������������ip��port�������ã�����ʱ��Ҳ�����ã�<br>
 * 4���������������̣߳�������ѯ�̣߳�<br>
 * 5��֧�ֶ����еĿ�������йرգ�
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
