package com.dl.sd.consumer;

import java.util.List;

import com.dl.sd.netWork.INetNode;
import com.dl.sd.netWork.NetNode;

/**
 * ���ѡ����Խӿ�<br>
 * 1������ע�����Ļ�ȡ�������б����������Ҫ����ѡ���㣻<br>
 * 2��Ϊ���ⵥ������������ѹ����������⣬��Ҫ���и��ؾ��⣻<br>
 * 3�����ؾ���ʵ�ֵ��ַ��϶࣬��˸��û����˽ӿڣ�
 * 
 * @author dl
 *
 */
public interface INodeStrategy {
	INetNode ServerBalance(Consumer consumer, String serviceTag, List<NetNode> nodeList);
}
