package com.dl.multi_file.resource;

import java.util.LinkedList;
import java.util.List;

/**
 * 未接收片段记录类<br>
 * 1、可通过申请的片段信息初始化本类对象；<br>
 * 2、每个申请资源对应一个记录类对象；<br>
 * 3、当资源接收完毕后可通过本类来检测该申请的资源是否接收完毕；
 * 
 * @author dl
 *
 */
public class NoReceiveSection {
	private List<SectionInfo> sectionList;
	
	/**
	 * 初始化
	 * @param sectionInfo
	 */
	public NoReceiveSection(SectionInfo sectionInfo) {
		sectionList = new LinkedList<>();
		sectionList.add(sectionInfo);
	}
	
	public List<SectionInfo> getSectionList() {
		return sectionList;
	}
	
	/**
	 * 通过接收到新片段更新列表中的资源信息
	 * @param sectionInfo
	 */
	public void receiveNewSection(SectionInfo sectionInfo) {
		SectionInfo section = getSection(sectionInfo);
		if (section == null) {
			return;
		}
		int offset = section.getOffset();
		int size = section.getSize();
		int newOffset = sectionInfo.getOffset();
		int newSize = sectionInfo.getSize();
		
		int leftOffset = offset;
		int leftSize = newOffset - offset;
		
		int rightOffset = newOffset + newSize;
		int rightSize = offset + size - rightOffset;
		
		sectionList.remove(section);
		String fileName = section.getFileName();
		if (leftSize > 0) {
			sectionList.add(new SectionInfo(fileName, leftOffset, leftSize));
		}
		if (rightSize > 0) {
			sectionList.add(new SectionInfo(fileName, rightOffset, rightSize));
		}
	}
	
	/**
	 * 通过给定的资源片段找到列表中对应的合适片段
	 * @param sectionInfo
	 * @return
	 */
	private SectionInfo getSection(SectionInfo sectionInfo) {
		int offset = sectionInfo.getOffset();
		int size = sectionInfo.getSize();
		for (SectionInfo section : sectionList) {
			if (section.isRight(offset, size)) {
				return section;
			}			
		}
		return null;
	}
	
	public boolean isCompleted() {
		return sectionList.isEmpty();
	}
	
}
