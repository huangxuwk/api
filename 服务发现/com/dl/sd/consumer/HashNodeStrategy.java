package com.dl.sd.consumer;

import java.util.List;

import com.dl.sd.netWork.NetNode;

/**
 * 通过哈希算法来实现负载均衡<br>
 * 优点：使用散列算法可以一定程度的分散客户端的访问<br>
 * 缺点：若散列算法性能较差，负载均衡的性能也会很差
 * 
 * @author dl
 *
 */
public class HashNodeStrategy implements INodeStrategy {

	@Override
	public NetNode ServerBalance(Consumer consumer, String serviceTag, List<NetNode> nodeList) {
		int tmp = consumer.hashCode() + serviceTag.hashCode();
		int hashCode = Math.abs(tmp ^ (tmp >>> 16));
		int size = nodeList.size();
	
		return nodeList.get(hashCode % (size - 1));
	}

}
