package com.dl.sd.registry;

import com.dl.sd.netWork.NetNode;

/**
 * 处理消息的接口
 * @author dl
 *
 */
public interface IDealMessage {
	void dealMessage(NetNode netNode, String message);
}
