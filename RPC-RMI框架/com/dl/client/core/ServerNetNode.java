package com.dl.client.core;

/**
 * 结点信息类<br>
 * 为负载均衡做准备；
 * 
 * @author dl
 *
 */
public class ServerNetNode {
	private String ip;
	private int port;
	
	public ServerNetNode() {
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
