package com.dl.sd.consumer;

import java.util.List;

import com.dl.sd.netWork.NetNode;

/**
 * 提供RPC远程方法调用的接口
 * 
 * @author dl
 *
 */
public interface IConsumerAction {
	List<String> updataServiceTags();
	List<NetNode> updataNews(String serviceTag);
}
