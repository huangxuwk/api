package com.dl.multi_file.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dl.multi_file.client.ResourceRequester;

/**
 * 断点续传管理类<br>
 * 1、请求的资源可能为多个文件，因此需要给每个文件对应一个NoReceiveSection对象；<br>
 * 2、当资源接收任务完成后，会检查所有的NoReceiveSection对象中的List是否为空；<br>
 * 3、根据步骤2中检查的情况，再去决定要不要申请资源；
 * @author dl
 *
 */
public class LeakageManager {
	private Map<String, NoReceiveSection> noSectionMap;

	private ResourceRequester resourceRequester;
	
	public LeakageManager(ResourceRequester resourceRequester) {
		noSectionMap = new HashMap<>();
		this.resourceRequester =resourceRequester;
	}
	
	/**
	 * 初始化所有资源对应的断点续传对象
	 * @param sectionList
	 */
	public void init(List<SectionInfo> sectionList) {
		for (SectionInfo sectionInfo : sectionList) {
			String[] str = sectionInfo.toString().split(":");
			noSectionMap.put(str[0], new NoReceiveSection(sectionInfo));
		}
	}
	
	/**
	 * 接收到新的资源片段
	 * @param sectionInfo
	 */
	public void receiverNews(SectionInfo sectionInfo) {
		String[] str = sectionInfo.toString().split(":");
		NoReceiveSection noReceiveSection = noSectionMap.get(str[0]);
		if (noReceiveSection != null) {
			noReceiveSection.receiveNewSection(sectionInfo);
		}
	}
	
	/**
	 * 当接收者接收完成后，要检查是否存在没有接收到的片段，若有，则重新申请；
	 */
	public void checkReceiveCompleted() {
		List<SectionInfo> sectionList = new ArrayList<SectionInfo>();
		
		Collection<NoReceiveSection> collection = noSectionMap.values();
		for (NoReceiveSection noReceiveSection : collection) {
			if (!noReceiveSection.isCompleted()) {
				List<SectionInfo> sList = noReceiveSection.getSectionList();
				sectionList.addAll(sList);
			}
		}
		// 这里也可以由上层决定，抛给应用层去完成，用弹窗的形式
		if (!sectionList.isEmpty()) {
			List<String> resourceHandles = new ArrayList<>();
			for (SectionInfo section : sectionList) {
				resourceHandles.add(section.toString());
			}
			resourceRequester.requestResource(resourceHandles);
		}
	}
	
}
