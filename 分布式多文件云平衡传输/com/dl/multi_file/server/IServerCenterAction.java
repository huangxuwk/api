package com.dl.multi_file.server;

import java.util.List;

import com.dl.sd.netWork.NetNode;

/**
 * ��Դ�ṩ�˺���Դ�������ĵ�RPCͨ�Žӿ�<br>
 * @author dl
 *
 */
public interface IServerCenterAction {
	/**
	 * ��Դ����ǰ����������ı����������ڷ�����Դ������
	 * @param resourceHandles
	 * @param netNode
	 * @throws Throwable
	 */
	void startNewTask(List<String> resourceHandles, NetNode netNode) throws Throwable;
	
	/**
	 * ��Դ������ɺ���������ı������ӷ�����ɴ����ͼ������ڷ��͸�����
	 * @param resourceHandles
	 * @param netNode
	 * @throws Throwable
	 */
	void accomplishTask(List<String> resourceHandles, NetNode netNode)  throws Throwable;
}
