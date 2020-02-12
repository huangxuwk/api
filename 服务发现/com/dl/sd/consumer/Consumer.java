package com.dl.sd.consumer;

import java.util.List;

import com.dl.rpc.client.ClientProxy;
import com.dl.rpc.client.RPCClient;
import com.dl.sd.netWork.NetNode;
import com.parser_reflect.util.PropertiesParser;
import com.timer.util.Timer;
import com.timer.util.UserAction;

/**
 * 服务消费者<br>
 * 1、上线即向注册中心申请全部服务列表；<br>
 * 2、定时从注册中心更新服务列表和对应服务提供者列表；<br>
 * 3、通过注册中心给的服务提供者列表进行负载均衡；<br>
 * 4、注册中心宕机，采取定时重连的方式，直到连接成功为止；
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
			// 通信中断，定时更新起到了定时连接的功能
		}
	}
	
	public void shutdown() {
		if (timer != null) {
			timer.stopThread();
		}
	}
	
	/**
	 * 通过服务标签得到对应的服务提供者<br>
	 * 1、先从本地缓存中查询结点列表；<br>
	 * 2、如果本地缓存为空，则向注册中心申请；<br>
	 * 3、通过消费端的负载均衡策略找到合适的服务提供者；<br>
	 * 4、要求用户必须设置INodeStrategy对象；
	 * @param serviceTag 服务标签
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
	 * 开启定时更新服务和服务提供者列表的方法
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
