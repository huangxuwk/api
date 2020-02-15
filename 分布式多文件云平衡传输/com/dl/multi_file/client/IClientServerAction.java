package com.dl.multi_file.client;

import java.util.List;

import com.dl.multi_file.resource.SectionInfo;
import com.dl.sd.netWork.NetNode;

/**
 * 资源请求端和资源提供端的RPC通信接口<br>
 * @author dl
 *
 */
public interface IClientServerAction {
	/**
	 * 资源请求端向资源提供端发布任务
	 * @param recipient
	 * @param sectionList
	 * @throws Throwable
	 */
	void requestResource(NetNode recipient, List<SectionInfo> sectionList) throws Throwable;
}
