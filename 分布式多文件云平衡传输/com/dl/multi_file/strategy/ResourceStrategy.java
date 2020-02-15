package com.dl.multi_file.strategy;

import java.util.ArrayList;
import java.util.List;

import com.dl.multi_file.resource.SectionInfo;

/**
 * 资源分配策略实现类<br>
 * 1、实现了按文件个数均分的分配策略；<br>
 * 2、实现了按文件大小均分的分配策略；<br>
 * 3、当资源列表个数大于发送个数时采用策略1；<br>
 * 4、当资源列表个数小于发送个数时采用策略2；
 * @author dl
 *
 */
public class ResourceStrategy implements IResourceStrategy {
	
	@Override
	public List<List<SectionInfo>> doResourceStrategy(List<SectionInfo> sectionList, int sendCount) {
		List<List<SectionInfo>> sectionLList = new ArrayList<>();
		for (int i = 0; i < sendCount; i++) {
			sectionLList.add(i, new ArrayList<>());
		}
		return sectionList.size() >= sendCount 
					? resourceCountStrategy(sectionList, sectionLList)
					: resourceSizeStrategy(sectionList, sectionLList);														
	}
	
	/*
	 * 策略1：根据资源个数来平均分配；
	 */
	private List<List<SectionInfo>> resourceCountStrategy(List<SectionInfo> sectionList, List<List<SectionInfo>> sectionLList) {
		int sendCount = sectionLList.size();
		int index = 0;
		for (SectionInfo section : sectionList) {
			List<SectionInfo> sList = sectionLList.get(index);
			sList.add(section);
			index = (index + 1) % sendCount;
		}
		return sectionLList;
	}
	
	/*
	 * 策略2：根据资源大小来平均分配；
	 */
	private List<List<SectionInfo>> resourceSizeStrategy(List<SectionInfo> sectionList, List<List<SectionInfo>> sectionLList) {
		long resourceSize = 0;
		for (SectionInfo section : sectionList) {
			resourceSize += section.getSize();
		}
		int sendCount = sectionLList.size();
		int sectionCount = sectionList.size();
		// 平均资源大小
		int deSectionSize = (int) (resourceSize / sendCount);
		// 最大资源大小
		int maxSectionSize = (int) (deSectionSize + resourceSize % sendCount);
		int index = 0;
		// 每个结点发送的累计资源大小
		int CumulativeSize = 0;
		// 该结点分配的剩余大小
		int SurplusSize = 0;
		// 将最大的资源片段给第一个结点
		List<SectionInfo> firstList = sectionLList.get(0);
		while (CumulativeSize < maxSectionSize && index < sectionCount) {
			SectionInfo section = sectionList.get(index);
			int size = section.getSize();
			SurplusSize = maxSectionSize - CumulativeSize;
			// 如果片段大小小于剩余大小，直接添加，再判断下一片段
			if (size <= SurplusSize) {
				firstList.add(section);
				CumulativeSize += size;
				index++;
			} else {
				// 如果片段大小大于剩余大小，先分割，将该片段分割成两段；
				// 第一段是上一结点剩余大小的片段；
				// 第二段是该片段剩余的片段；
				String fileName = section.getFileName();
				int offset = section.getOffset();
				firstList.add(new SectionInfo(fileName, offset, SurplusSize));
				sectionList.set(index, new SectionInfo(fileName, offset + SurplusSize, size - SurplusSize));
				CumulativeSize += SurplusSize;
			}
		}
		
		for (int i = 1; i < sendCount && index < sectionCount; i++) {
			CumulativeSize = 0;
			SurplusSize = 0;
			List<SectionInfo> list = sectionLList.get(i);
			while (CumulativeSize < deSectionSize && index < sectionCount) {
				SectionInfo section = sectionList.get(index);
				int size = section.getSize();
				SurplusSize = maxSectionSize - CumulativeSize;
				if (size <= SurplusSize) {
					list.add(section);
					CumulativeSize += size;
					index++;
				} else {
					String fileName = section.getFileName();
					int offset = section.getOffset();
					list.add(new SectionInfo(fileName, offset, SurplusSize));
					sectionList.set(index, new SectionInfo(fileName, offset + SurplusSize, size - SurplusSize));
					CumulativeSize += SurplusSize;
				}
			}
		}
		
		return sectionLList;
	}
	
}
