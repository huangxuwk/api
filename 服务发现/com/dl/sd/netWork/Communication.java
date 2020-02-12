package com.dl.sd.netWork;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * ͨ�Ų�<br>
 * 1���ṩ�������շ���Ϣ���ֶΣ�<br>
 * 2����������Ϣ���շ����������κ��߼��Ĵ���
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
	 * �����ϵ���Ϣ���͸��Զˣ�<br>
	 * ���ͨ�Ŷ��ѣ���ӻ������Ƴ���socket�����쳣��;
	 * @param message
	 * @throws IOException
	 */
	public void sendMessage(NetMessage message) throws IOException {
		dos.writeUTF(message.toString());
	}
	
	/**
	 * �ж��Ƿ��пɶ���Ϣ���������Ϣ������Ϣ���͸��߳�ִ�У�<br>
	 * ���ͨ�Ŷ��ѣ���ӻ������Ƴ���socket�����쳣��;
	 * @return
	 * @throws IOException
	 */

	public String readMessage() throws IOException {
		return dis.readUTF();
	}
	
	/**
	 * ��⻺�����Ƿ��пɶ�����Ϣ��������һ�����Ƶ��ֽڳ���
	 * @return
	 * @throws IOException
	 */
	public boolean isReadSuccess() throws IOException {
		return dis.available() > 0;
	}
	
}
