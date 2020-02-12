package com.dl.sd.consumer;

import java.util.List;

import com.dl.rpc.client.ClientProxy;
import com.dl.rpc.client.RPCClient;
import com.dl.sd.netWork.NetNode;
import com.parser_reflect.util.PropertiesParser;
import com.timer.util.Timer;
import com.timer.util.UserAction;

/**
 * ����������<br>
 * 1�����߼���ע����������ȫ�������б�<br>
 * 2����ʱ��ע�����ĸ��·����б�Ͷ�Ӧ�����ṩ���б�<br>
 * 3��ͨ��ע�����ĸ��ķ����ṩ���б���и��ؾ��⣻<br>
 * 4��ע������崻�����ȡ��ʱ�����ķ�ʽ��ֱ�����ӳɹ�Ϊֹ��
 * 
 * @author dl
 *
 */
public class Consumer {
	private RPCClient rpcClient;
	private ClientCache cache;
	
	private Timer timer;
	private int delayTime;
	
	private IConsumerAction action;
	private INodeStrategy strategy;
	
	public Consumer() {
		rpcClient = new RPCClient();
		ClientProxy clientProxy = new ClientProxy();
		clientProxy.setClient(rpcClient);
		action = clientProxy.jdkProxy(IConsumerAction.class);
		cache = new ClientCache();
	}
	
	public void setStrategy(INodeStrategy strategy) {
		this.strategy = strategy;
	}
	
	public void readConfig(String path) {
		PropertiesParser.load(path);
		
		String delayTimeStr = PropertiesParser.findElement("delayTime");
		try {
			if (delayTimeStr != null && !delayTimeStr.equals("")) {
				int delayTime = Integer.valueOf(delayTimeStr);
				if (delayTime > 0 && delayTime < 65536) {
					this.delayTime = delayTime;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void startup() {
		try {
			cache.addServiceTags(action.updataServiceTags());
			startTimeUpdata();
		} catch(Exception e) {
			// ͨ���жϣ���ʱ�������˶�ʱ���ӵĹ���
		}
	}
	
	public void shutdown() {
		if (timer != null) {
			timer.stopThread();
		}
	}
	
	/**
	 * ͨ�������ǩ�õ���Ӧ�ķ����ṩ��<br>
	 * 1���ȴӱ��ػ����в�ѯ����б�<br>
	 * 2��������ػ���Ϊ�գ�����ע���������룻<br>
	 * 3��ͨ�����Ѷ˵ĸ��ؾ�������ҵ����ʵķ����ṩ�ߣ�<br>
	 * 4��Ҫ���û���������INodeStrategy����
	 * @param serviceTag �����ǩ
	 * @return
	 */
	public NetNode getServer(String serviceTag) {
		List<NetNode> nodeList = cache.getServerList(serviceTag);
		if (nodeList == null) {
			nodeList = action.updataNews(serviceTag);
			if (nodeList == null) {
				return null;
			}
			cache.addService(serviceTag, nodeList);
		}
		return strategy.ServerBalance(this, serviceTag, nodeList);
	}
	
	/**
	 * ������ʱ���·���ͷ����ṩ���б�ķ���
	 */
	public void startTimeUpdata() {
		try {
			timer = new Timer();
			if (delayTime > 0) {
				timer.setDelayTime(delayTime);
			}
			timer.setUserAction(new UserAction() {
				@Override
				public void userAction() {
					List<String> tagList = action.updataServiceTags();
					cache.addServiceTags(tagList);
					List<NetNode> nodeList;
					for (String serviceTag : tagList) {
						nodeList = action.updataNews(serviceTag);
						cache.addService(serviceTag, nodeList);
					}
				}
			});
			timer.startThread();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
}
