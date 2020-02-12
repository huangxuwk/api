package com.dl.sd.registry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.dl.sd.netWork.Communication;
import com.dl.sd.netWork.NetNode;
import com.util.ThreadPoolFactory;

/**
 * 注册中心会话层<br>
 * 1、保存通信信道；<br>
 * 2、收到消息时，开启新的线程进行处理；<br>
 * 
 * @author dl
 *
 */
public class CenterConversation extends Communication {
	private IDealMessage dealMessage;
	private NetNode netNode;
	private RoundRobin roundRobin;
	
	private volatile boolean success;

	public CenterConversation(Socket socket, DataInputStream dis, DataOutputStream dos) {
		super(socket, dis, dos);
		
		String ip = socket.getInetAddress().getHostAddress();
		int port = socket.getPort();
		NetNode netNode = new  NetNode();
		netNode.setIp(ip);
		netNode.setPort(port);
		this.netNode = netNode;
	}
	
	public NetNode getNetNode() {
		return netNode;
	}
	
	public void setRoundRobin(RoundRobin roundRobin) {
		this.roundRobin = roundRobin;
	}
	
	public void setDealMessage(IDealMessage dealMessage) {
		this.dealMessage = dealMessage;
	}

	public void dealMessage(String message) {
		dealMessage.dealMessage(netNode, message);
	}

	/*
	 * 当线程多次isReadSuccess成功，就会开启多个线程
	 * 而只有一个线程可以读到消息，其余的线程会阻塞
	 * 因此需要success标志来加以判断
	 */
	public void judgeRead() throws IOException {
		// 这里可以通过标志来判断是否有任务
		if (isReadSuccess() && !success) {
			success = true;
			InnerDealMessage inner = new InnerDealMessage();
			ThreadPoolFactory.execute(new Thread(inner));
		}
	}
	
	/**
	 * 开启线程处理对端的消息
	 * @author dl
	 *
	 */
	private class InnerDealMessage implements Runnable {

		public InnerDealMessage() {
		}	
		@Override
		public void run() {
			String message;
			try {
				message = readMessage();
				dealMessage(message);
				success = false;
			} catch (IOException e) {
				roundRobin.removeConversation(CenterConversation.this);
			}
		}
	}

}
