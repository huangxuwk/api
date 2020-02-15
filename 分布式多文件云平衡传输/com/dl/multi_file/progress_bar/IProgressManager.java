package com.dl.multi_file.progress_bar;

import java.util.List;

import com.dl.multi_file.resource.SectionInfo;

/**
 * �������ӿ�
 * 1���û�����ͨ��������Դ����Ϣ��ʼ����������
 * 2����Դ�������յ���Դ��ɶ���ı���ڽ��������ȣ�
 * 3���رձ�����Դ���յ����н�������
 * @author dl
 *
 */
public interface IProgressManager {
	/**
	 * �����������Դ��Ϣ�б��ʼ��������
	 * @param sectionList
	 */
	void initProgressBar(List<SectionInfo> sectionList);
	/**
	 * �����յ��µ���ԴƬ�κ󣬽�ͬ�����½�����
	 * @param section
	 */
	void receiveNewSection(SectionInfo section);
	/**
	 * ��������£���������100%���Զ��رգ���������©�������󣬾���Ҫ�ֶ��رգ�
	 */
	void closeProgressBar();
}
