package com.dl.sd.netWork;

public class NetNode implements INetNode {
	private String ip;
	private int port;
	private int sendCount;
	private int sendingCount;
	
	public NetNode() {
	}

	public NetNode(String ip, int port) {
		this.ip = ip;
		this.port = port;
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
	
	public int getSendCount() {
		return sendCount;
	}

	public void setSendCount(int sendCount) {
		this.sendCount = sendCount;
	}

	public int getSendingCount() {
		return sendingCount;
	}

	public void setSendingCount(int sendingCount) {
		this.sendingCount = sendingCount;
	}
	
	public void accomplishTask() {
		this.sendCount++;
		this.sendingCount--;
	}

	public void startNewTask() {
		this.sendingCount++;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NetNode other = (NetNode) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ip: " + ip + "port: " + port;
	}
}
