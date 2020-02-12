package com.dl.sd.netWork;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 通信层<br>
 * 1、提供基本的收发信息的手段；<br>
 * 2、仅进行消息的收发，不进行任何逻辑的处理；
 * 
 * @author dl
 *
 */
public class Communication {
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
		
	public Communication() {
	}
	
	public Communication(Socket socket, DataInputStream dis, DataOutputStream dos) {
		this.socket = socket;
		this.dis = dis;
		this.dos = dos;
	}

	public DataInputStream getDis() {
		return dis;
	}

	public void setDis(DataInputStream dis) {
		this.dis = dis;
	}

	public DataOutputStream getDos() {
		return dos;
	}

	public void setDos(DataOutputStream dos) {
		this.dos = dos;
	}
		
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public void close() {
		try {
			if (!socket.isClosed() && socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			socket = null;
		}
		try {
			if (dis != null) {
				dis.close();		
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			dis = null;
		}
		try {
			if (dos != null) {
				dos.close();	
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			dos = null;
		}
	}
	
	/**
	 * 将整合的消息发送给对端；<br>
	 * 如果通信断裂，则从缓存中移除该socket（抛异常）;
	 * @param message
	 * @throws IOException
	 */
	public void sendMessage(NetMessage message) throws IOException {
		dos.writeUTF(message.toString());
	}
	
	/**
	 * 判断是否有可读消息；如果有消息，将消息发送给线程执行；<br>
	 * 如果通信断裂，则从缓存中移除该socket（抛异常）;
	 * @return
	 * @throws IOException
	 */

	public String readMessage() throws IOException {
		return dis.readUTF();
	}
	
	/**
	 * 检测缓存中是否有可读的信息，并返回一个估计的字节长度
	 * @return
	 * @throws IOException
	 */
	public boolean isReadSuccess() throws IOException {
		return dis.available() > 0;
	}
	
}
