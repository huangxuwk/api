package com.dl.sd.provider;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;

import com.parser_reflect.util.PropertiesParser;

/**
 * 服务提供者<br>
 * 1、向注册中心注册/注销服务；<br>
 * 2、注册中心宕机，采取定时重连的方式，直到连接成功为止；<br>
 * 3、支持ip、port的配置；
 * 
 * @author dl
 *
 */
public class Provider {
	public static final String DEFAULT_IP = "127.0.0.1";
	public static final int DEFAULT_PORT = 55550;

	private Socket client;
	private int port;
	private String ip;
	
	private ITimingQuery timingQuery;
	private ProviderConversation conversation;
	
	public Provider() {
		this.ip = DEFAULT_IP;
		this.port = DEFAULT_PORT;
	}

	public int getPort() {
		return port;
	}

	public void setTimingQuery(ITimingQuery timingQuery) {
		this.timingQuery = timingQuery;
	}
	
	public void setPort(int port) {
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public void readConfig(String path) {
		PropertiesParser.load(path);
		
		String ip = PropertiesParser.findElement("ip");
		if (ip != null && !ip.equals("")) {
			this.ip = ip;
		}
		String portStr = PropertiesParser.findElement("port");
		try {
			if (portStr != null && !portStr.equals("")) {
				int port = Integer.valueOf(portStr);
				if (port > 0 && port < 65536) {
					this.port = port;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean startup() {
		return startup(false);
	}
	
	boolean startup(boolean timing) {
		try {
			client = new Socket(ip, port);
			DataInputStream dis = new DataInputStream(client.getInputStream());
			DataOutputStream dos = new DataOutputStream(client.getOutputStream());
			conversation = new ProviderConversation(client, dis, dos);
			conversation.setTimingQuery(timingQuery);
			conversation.setProvider(this);
			new Thread(conversation).start();
		} catch (Exception e) {
			if (!timing) {
				timingQuery.dealTimingQuery(this);				
			}
			return false;
		}
		return true;
	}
	
	public void shutdown() {
		conversation.shutdown();
	}
	
	/**
	 * 注册服务，由高层负责，可能会发送失败；<br>
	 * 失败后告知上一层；
	 * @param serviceList
	 * @return
	 */
	public boolean registryService(List<String> serviceList) {
		return conversation.registryService(serviceList);
	}
	
	/**
	 * 注销服务，由高层负责，可能会发送失败；<br>
	 * 失败后告知上一层；
	 * @param serviceList
	 * @return
	 */
	public boolean cancellationService(List<String> serviceList) {
		return conversation.cancellationService(serviceList);
	}
	
}
