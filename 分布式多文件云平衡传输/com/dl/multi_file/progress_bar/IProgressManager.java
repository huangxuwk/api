package com.dl.multi_file.progress_bar;

import java.util.List;

import com.dl.multi_file.resource.SectionInfo;

/**
 * 进度条接口
 * 1、用户可以通过请求资源的信息初始化进度条；
 * 2、资源接收者收到资源后可定向改变对于进度条进度；
 * 3、关闭本次资源接收的所有进度条；
 * @author dl
 *
 */
public interface IProgressManager {
	/**
	 * 根据申请的资源信息列表初始化进度条
	 * @param sectionList
	 */
	void initProgressBar(List<SectionInfo> sectionList);
	/**
	 * 当接收到新的资源片段后，将同步更新进度条
	 * @param section
	 */
	void receiveNewSection(SectionInfo section);
	/**
	 * 正常情况下，进度条到100%会自动关闭，但若出现漏传的现象，就需要手动关闭；
	 */
	void closeProgressBar();
}
