package com.dl.rpc.client;

/**
 * 结点信息类<br>
 * 为负载均衡做准备；
 * 
 * @author dl
 *
 */
public class RPCServerNetNode {
	private String ip;
	private int port;
	
	public RPCServerNetNode() {
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
