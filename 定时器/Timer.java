package com.timer.util;

import com.util.ThreadPoolFactory;

public class Timer implements Runnable {
	// Ĭ�ϵȴ�ʱ��
	private static final int DEFAULT_DALEY_TIME = 1000;
	// ��ʱ��ʱ��
	private long delayTime;
	// ÿ���������������Ӧ����ͬ
	private Object lock;
	// ��־�������Կ����߳��Ƿ��������
	private volatile boolean goon;
	// �˽ӿڶ����û���Ҫ��ʱִ�еķ���
	private UserAction userAction;
	
	public Timer() {
		this(DEFAULT_DALEY_TIME);
	}

	public Timer(long delayTime) {
		this(delayTime, null);
		this.delayTime = delayTime;
	}

	public Timer(long delayTime, UserAction userAction) {
		lock = new Object();
		this.delayTime = delayTime;
		this.userAction = userAction;
	}

	public long getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(long delayTime) {
		this.delayTime = delayTime;
	}

	public void setUserAction(UserAction userAction) {
		this.userAction = userAction;
	}

	public void startThread() throws Exception {
		if (goon == true) {
			return;
		}
		if (userAction == null) {
			throw new Exception("�û�����δ���壡");
		}
		goon = true;
		new Thread(this, "timer").start();
	}
	
	public void stopThread() {
		if (goon == false) {
			return;
		}
		goon = false;
	}
	
	@Override
	public void run() {
		while (goon) {
			synchronized (lock) {
				try {
					// �����ȴ���wait()����������ڶ�����
					lock.wait(delayTime);
					// �ȴ���ɣ������߳�ִ������
					ThreadPoolFactory.execute(new Thread(new Action()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class Action implements Runnable {
		// ���߳̿�ʼִ���û�����
		@Override
		public void run() {
			// ���̱߳����ѣ���ִ�д˷�����ִ���꣬����������
			userAction.userAction();
		}
	}
	
}
