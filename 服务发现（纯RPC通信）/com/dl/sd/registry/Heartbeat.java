package com.dl.sd.registry;

import java.util.List;

import com.dl.rpc.client.RPCClient;
import com.dl.rpc.client.RPCClientProxy;
import com.dl.sd.netWork.NetNode;
import com.timer.util.Timer;
import com.timer.util.UserAction;

/**
 * �������<br>
 * 1������available()�޷����Զ��쳣���ߣ����ʹ��������⣻<br>
 * 2����ʱ������ṩ���б���һ����Ϣ���ж϶Զ��Ƿ����ߣ�<br>
 * 3��ʱ�ӿ����ã�
 * 
 * @author dl
 *
 */
public class Heartbeat {
	public static final int DEFAULTTIME = 60*60*1000;
	
	private RPCClient heartClient;
	private IHeartbeatAction action;
	
	private Timer timer;
	private int heartBeatTime;
	private ServiceCache serviceCache;
	
	public Heartbeat() {
		heartClient = new RPCClient();
		RPCClientProxy proxy = new RPCClientProxy();
		action = proxy.jdkProxy(IHeartbeatAction.class);
		
		serviceCache = new ServiceCache();
		heartBeatTime = DEFAULTTIME;
	}
	
	public void setTime(int heartBeatTime) {
		this.heartBeatTime = heartBeatTime;
	}
	
	public void startHeartBeat() {
		timer = new Timer(heartBeatTime);
		timer.setUserAction(new UserAction() {
			@Override
			public void userAction() {
				List<NetNode> nodeList = serviceCache.getNodelist();
				for (NetNode netNode : nodeList) {
					heartClient.setIp(netNode.getIp());
					heartClient.setPort(netNode.getPort());
					try {
						action.askToProvider();
					} catch (Throwable e) {
						// �Զ˵��ߣ�ɾ���ý��
						serviceCache.remove(netNode);
					}
				}
			}
		});
		try {
			timer.startThread();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		timer.stopThread();
	}
	
}
