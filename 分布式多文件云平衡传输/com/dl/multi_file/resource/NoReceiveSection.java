package com.dl.multi_file.resource;

import java.util.LinkedList;
import java.util.List;

/**
 * δ����Ƭ�μ�¼��<br>
 * 1����ͨ�������Ƭ����Ϣ��ʼ���������<br>
 * 2��ÿ��������Դ��Ӧһ����¼�����<br>
 * 3������Դ������Ϻ��ͨ�������������������Դ�Ƿ������ϣ�
 * 
 * @author dl
 *
 */
public class NoReceiveSection {
	private List<SectionInfo> sectionList;
	
	/**
	 * ��ʼ��
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
	 * ͨ�����յ���Ƭ�θ����б��е���Դ��Ϣ
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
	 * ͨ����������ԴƬ���ҵ��б��ж�Ӧ�ĺ���Ƭ��
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
