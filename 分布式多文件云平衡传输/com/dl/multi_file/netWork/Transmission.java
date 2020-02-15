package com.dl.multi_file.netWork;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.dl.multi_file.resource.SectionInfo;

/**
 * 传输层<br>
 * 1、提供数据片段的接收与发送；<br>
 * 2、以分段形式发送，默认为8kb/次；<br>
 * 3、数据发送前，先发送描述对象；
 * @author dl
 *
 */
public class Transmission {
	public static final int DEFAULT_BUFFFER_LENGTH = 2 << 13;
	
	private int sendLength;
	
	public Transmission() {
		sendLength = DEFAULT_BUFFFER_LENGTH;
	}
	
	public void setSendLength(int sendLength) {
		this.sendLength = sendLength;
	}
	
	/**
	 * 接收服务器发送的字节流数据；<br>
	 * 在数据发送前会先发送描述对象，当对象为null表示发送完；
	 * @param dis
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public SectionInfo recvfrom(DataInputStream dis) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(dis);
		SectionInfo sectionInfo = (SectionInfo) ois.readObject();
		
		return sectionInfo;
	}

	/**
	 * 读取字节流，读取长度由参数决定；<br>
	 * 由于发送端发送的数据可能过长，因此采用分段读取的方式；
	 * @param dis
	 * @param size
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public byte[] recvfrom(DataInputStream dis, int size) throws IOException, ClassNotFoundException {
		byte[] data = new byte[size];
		
		int restLen = size;
		int readLen;
		int len;
		int offset = 0;
		
		while (restLen > 0) {
			len = restLen > sendLength ? sendLength : restLen;
			readLen = dis.read(data, offset, len);
			restLen -= readLen;
			offset += readLen;
		}
		
		return data;
	}
	
	/**
	 * 发送描述对象
	 * @param dos
	 * @param sectionInfo
	 * @throws IOException
	 */
	public void sendObject(DataOutputStream dos, SectionInfo sectionInfo) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(dos);
		oos.writeObject(sectionInfo);
	}
	
	/**
	 * 发送字节数据
	 * @param dos
	 * @param data
	 * @throws IOException
	 */
	public void sendData(DataOutputStream dos, byte[] datas) throws IOException {
		dos.write(datas);
	}
}
