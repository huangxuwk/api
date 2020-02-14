package com.dl.sd.registry;

import java.util.List;

import com.dl.rpc.client.RPCClient;
import com.dl.rpc.client.RPCClientProxy;
import com.dl.sd.netWork.NetNode;
import com.timer.util.Timer;
import com.timer.util.UserAction;

/**
 * 心跳检测<br>
 * 1、由于available()无法检测对端异常掉线，因此使用心跳检测；<br>
 * 2、定时向服务提供者列表发送一条消息，判断对端是否在线；<br>
 * 3、时延可设置；
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
						// 对端掉线，删除该结点
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
