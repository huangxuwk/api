package com.dl.multi_file.client;

import java.util.List;

import com.dl.multi_file.resource.SectionInfo;
import com.dl.sd.netWork.NetNode;

/**
 * ��Դ����˺���Դ�ṩ�˵�RPCͨ�Žӿ�<br>
 * @author dl
 *
 */
public interface IClientServerAction {
	/**
	 * ��Դ���������Դ�ṩ�˷�������
	 * @param recipient
	 * @param sectionList
	 * @throws Throwable
	 */
	void requestResource(NetNode recipient, List<SectionInfo> sectionList) throws Throwable;
}
