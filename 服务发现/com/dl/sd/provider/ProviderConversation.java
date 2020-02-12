package com.dl.sd.provider;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import com.dl.sd.netWork.Communication;
import com.dl.sd.netWork.EMessageType;
import com.dl.sd.netWork.NetMessage;
import com.util.ArgumentMaker;

/**
 * 服务提供者的会话层<br>
 * 1、与注册中心会话层通信；<br>
 * 2、提供与注册中心会话层类似的功能；<br>
 * 3、支持注册中心宕机后的操作；
 * 
 * @author dl
 *
 */
public class ProviderConversation extends Communication implements Runnable {
	private ITimingQuery timingQuery;
	private Provider provider;
	
	private volatile boolean goon;
	
	public ProviderConversation() {
		goon = true;
	}
	
	public ProviderConversation(Socket client, DataInputStream dis, DataOutputStream dos) {
		super(client, dis, dos);
		goon = true;
	}
	
	public void setProvider(Provider provider) {
		this.provider = provider;
	}
	
	public void setTimingQuery(ITimingQuery timingQuery) {
		this.timingQuery = timingQuery;
	}

	private String serviceMessage(List<String> serviceList) {
		return ArgumentMaker.gson.toJson(serviceList);
	}
	
	public boolean registryService(List<String> serviceList) {
		NetMessage netMessage = new NetMessage()
				.setType(EMessageType.REGISTRY)
				.setParamater(serviceMessage(serviceList));

		try {
			sendMessage(netMessage);
		} catch (IOException e) {
			// 通信中断
			timingQuery.dealTimingQuery(provider);
			return false;
		}
		
		return true;
	}
	
	public boolean cancellationService(List<String> serviceList) {
		NetMessage netMessage = new NetMessage()
				.setType(EMessageType.CANCELLATION)
				.setParamater(serviceMessage(serviceList));
		
		try {
			sendMessage(netMessage);
		} catch (IOException e) {
			// 通信中断
			timingQuery.dealTimingQuery(provider);
			return false;
		}
		
		return true;
	}
	
	public void shutdown() {
		goon = false;
		close();
	}
	
	// 保留方法
	public void dealMessage(String message) {
	}

	@Override
	public void run() {
		while (goon) {
			try {
				String message = readMessage();
				dealMessage(message);
			} catch (IOException e) {
				timingQuery.dealTimingQuery(provider);
			}
		}
	}

}
