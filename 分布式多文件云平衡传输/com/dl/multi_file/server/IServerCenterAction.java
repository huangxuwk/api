package com.dl.multi_file.server;

import java.util.List;

import com.dl.sd.netWork.NetNode;

/**
 * 资源提供端和资源管理中心的RPC通信接口<br>
 * @author dl
 *
 */
public interface IServerCenterAction {
	/**
	 * 资源发送前，向管理中心报告增加正在发送资源个数；
	 * @param resourceHandles
	 * @param netNode
	 * @throws Throwable
	 */
	void startNewTask(List<String> resourceHandles, NetNode netNode) throws Throwable;
	
	/**
	 * 资源发送完成后，向管理中心报告增加发送完成次数和减少正在发送个数；
	 * @param resourceHandles
	 * @param netNode
	 * @throws Throwable
	 */
	void accomplishTask(List<String> resourceHandles, NetNode netNode)  throws Throwable;
}
