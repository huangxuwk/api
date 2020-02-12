package com.dl.sd.registry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ��ѯ
 * 1�����������ӵ�ע�����ĵķ����������б��У�<br>
 * 2����ѯ�б��е����������㣬�����ж��Ƿ�����Ϣ��<br>
 * 3��������Ϣ������һ���µ��߳�ȥ������Ϣ��<br>
 * 4����ĳ���崻�����ɾ���ý�㣻
 * 
 * @author dl
 *
 */
public class RoundRobin implements Runnable {
	// �̰߳�ȫ���б�
	private static final List<CenterConversation> coPool = new CopyOnWriteArrayList<>();
	
	private IDealMessage dealMessage;
	private ServiceCache serviceCache;
	private volatile boolean goon;
	
	public RoundRobin() {
		serviceCache = new ServiceCache();
		goon = true;
	}
	
	public void setDealMessage(IDealMessage dealMessage) {
		this.dealMessage = dealMessage;
	}
	
	public List<CenterConversation> getCoList() {
		return new ArrayList<CenterConversation>(coPool);
	}
	
	/**
	 * ͨ�����ݽ�����socket������γ�һ��CenterConversation���󲢷��������
	 * @param socket
	 */
	public void addCommunication(Socket socket) {
		try {
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			CenterConversation conversation = new CenterConversation(socket, dis, dos);
			conversation.setDealMessage(dealMessage);
			conversation.setRoundRobin(this);
			coPool.add(conversation);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void removeConversation(CenterConversation conversation) {
		coPool.remove(conversation);
		serviceCache.remove(conversation.getNetNode());
	}
	
	public void stopRound() {
		goon = false;
	}

	@Override
	public void run() {
		while (goon) {
			Iterator<CenterConversation> iterator = coPool.iterator();
			while (iterator.hasNext()) {
				CenterConversation co = iterator.next();
				try {
					co.judgeRead();
				} catch (IOException e) {
					// ͨ�Ŷ��ѣ�ɾ���÷�����
					removeConversation(co);
				}
			}
		}
	}
}
