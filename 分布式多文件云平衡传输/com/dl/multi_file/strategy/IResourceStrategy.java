package com.dl.multi_file.strategy;

import java.util.List;

import com.dl.multi_file.resource.SectionInfo;

/**
 * 资源分配策略接口
 * @author dl
 *
 */

public interface IResourceStrategy {
	
	/**
	 * 做资源分配的方法<br>
	 * 通过给定的资源列表和发送个数来分配资源；
	 * @param sectionList 资源列表
	 * @param sendCount 发送个数
	 * @return 分配后的资源列表
	 */
	List<List<SectionInfo>> doResourceStrategy(List<SectionInfo> sectionList, int sendCount);
}
