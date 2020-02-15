package com.dl.multi_file.client;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 端口号池<br>
 * 1、实现端口的可分配，避免多资源并发接收时的端口冲突；<br>
 * 2、资源接收者可以申请一个端口号，使用完后归还；<br>
 * 3、在接收者中设置定时器保证归还端口，避免端口号丢失；<br>
 * 4、端口号池的容量和起始数是可以通过文件配置的；
 * @author dl
 *
 */
public class PortPool {
	public static final int DEFAULT_CAPACITY = 10;
	
	private static final Queue<Integer> portPool;
	
	static {
		portPool = new ArrayBlockingQueue<>(DEFAULT_CAPACITY);
		for (int i = 0; i < DEFAULT_CAPACITY; i++) {
			portPool.offer(59000 + i);
		}
	}
	
	public static boolean hasNext() {
		return !portPool.isEmpty();
	}
	
	public static int borrow() {
		Integer port = portPool.poll();
		if (port != null) {
			return port;
		}
		return -1;
	}
	
	public static void giveBack(int port) {
		portPool.offer(port);
	}
}
