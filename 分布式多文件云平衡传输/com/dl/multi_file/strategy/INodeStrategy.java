package com.dl.multi_file.strategy;

import java.util.List;

import com.dl.sd.netWork.NetNode;

/**
 * 结点选择策略接口
 * @author dl
 *
 */
public interface INodeStrategy {
	/**
	 * 做结点选择的方法<br>
	 * 通过给定的结点列表来选出合适的结点；
	 * @param nodeList 结点列表
	 * @return 选择后的结点列表
	 */
	List<NetNode> doNodeStrategy(List<NetNode> nodeList);
}
