package com.dl.sd.consumer;

import java.util.List;

import com.dl.sd.netWork.INetNode;
import com.dl.sd.netWork.NetNode;

/**
 * 结点选择策略接口<br>
 * 1、当从注册中心获取服务器列表后，消费者需要自行选择结点；<br>
 * 2、为避免单个服务器负载压力过大的问题，需要进行负载均衡；<br>
 * 3、负载均衡实现的手法较多，因此给用户留了接口；
 * 
 * @author dl
 *
 */
public interface INodeStrategy {
	INetNode ServerBalance(Consumer consumer, String serviceTag, List<NetNode> nodeList);
}
