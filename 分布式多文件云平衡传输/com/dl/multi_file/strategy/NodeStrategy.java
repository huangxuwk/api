package com.dl.multi_file.strategy;

import java.util.ArrayList;
import java.util.List;

import com.dl.sd.netWork.NetNode;

/**
 * 结点选择策略实现类<br>
 * 1、通过结点的发送次数和发送状态来判断是否要发送；<br>
 * 2、通过桶排序来选取发送次数较少和同时发送个数小于阈值的结点；<br>
 * 3、发送结点的个数和结点同时最大发送个数是可设置的；
 * @author dl
 *
 */
public class NodeStrategy implements INodeStrategy {
	/**
	 * 默认发送的结点个数
	 */
	public static final int DEFAULT_SEND_COUNT = 5;
	/**
	 * 结点的默认最大同时发送个数
	 */
	public static final int DEFAULT_MAX_SENDING_COUNT = 3;
	
	private int sendCount;
	private int maxSendingCount;
	
	public NodeStrategy() {
	}
	
	public NodeStrategy(int sendCount, int maxSendingCount) {
		this.sendCount = sendCount;
		this.maxSendingCount = maxSendingCount;
	}
	
	public int getSendCount() {
		return sendCount;
	}

	public void setSendCount(int sendCount) {
		this.sendCount = sendCount;
	}

	public int getMaxSendingCount() {
		return maxSendingCount;
	}

	public void setMaxSendingCount(int maxSendingCount) {
		this.maxSendingCount = maxSendingCount;
	}

	@Override
	public List<NetNode> doNodeStrategy(List<NetNode> nodeList) {
		if (nodeList == null) {
			return null;
		}
		int size = nodeList.size();
		if (sendCount <= 0 ) {
			sendCount = DEFAULT_SEND_COUNT;
		}
		if (sendCount > size) {
			sendCount = size;
		}
		if (maxSendingCount <= 0) {
			maxSendingCount = DEFAULT_MAX_SENDING_COUNT;
		}
		/*
		 * 利用桶排序来找到适合的结点
		 */
		int maxNum = nodeList.get(0).getSendCount();
		for (int i = 1; i < size; i++) {
			int tempCount = nodeList.get(i).getSendCount();
			if (maxNum < tempCount) {
				maxNum = tempCount;
			}
		}
		int[] nodeCount = new int[++maxNum];
		for (NetNode node : nodeList) {
			nodeCount[node.getSendCount()]++;
		}
		List<NetNode> resultList = new ArrayList<>();
		for (int i = 0; i < maxNum; i++) {
			if (nodeCount[i] > 0) {
				for (NetNode netNode : nodeList) {
					if (netNode.getSendCount() == i
							&& netNode.getSendingCount() <= maxSendingCount) {
						resultList.add(netNode);
						if (resultList.size() == sendCount) {
							return resultList;
						}
					}
				}
				nodeCount[i] = 0;
			}
		}
			
		return null;
	}

}
