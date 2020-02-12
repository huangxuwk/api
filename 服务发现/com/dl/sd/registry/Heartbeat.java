package com.dl.sd.registry;

import java.io.IOException;
import java.util.List;

import com.dl.sd.netWork.EMessageType;
import com.dl.sd.netWork.NetMessage;
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
	
	private Timer timer;
	private int heartBeatTime;
	private RoundRobin roundRobin;
	
	public Heartbeat() {
		roundRobin = new RoundRobin();
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
				List<CenterConversation> conList = roundRobin.getCoList();
				NetMessage netMessage = new NetMessage();
				netMessage.setType(EMessageType.HEARTBEAT);
				for (CenterConversation centerConversation : conList) {
					try {
						centerConversation.sendMessage(netMessage);
					} catch (IOException e) {
						// 对端掉线
						roundRobin.removeConversation(centerConversation);
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
