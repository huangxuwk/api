package com.dl.multi_file.client;

import java.util.List;

import com.dl.multi_file.resource.SectionInfo;
import com.dl.rpc.client.RPCClient;
import com.dl.rpc.client.RPCClientProxy;
import com.dl.sd.consumer.Consumer;
import com.dl.sd.consumer.ICenterAction;
import com.dl.sd.netWork.NetNode;

/**
 * 资源请求端核心类 - 客户端
 * @author dl
 *
 */
public class RequestClient {
	private Consumer consumer;
	
	private ICenterAction centerAction;
	private IClientServerAction serverAction;
	
	private RPCClient rpcClient;
	
	public RequestClient() {
		consumer = new Consumer();
		
		rpcClient = new RPCClient();
		RPCClientProxy proxy = new RPCClientProxy();
		proxy.setClient(rpcClient);
		centerAction = proxy.jdkProxy(ICenterAction.class);
		serverAction = proxy.jdkProxy(IClientServerAction.class);
	}
	
	/**
	 * 提供下载资源管理中心全部资源句柄列表的方法
	 * @return
	 */
	public List<String> updataResourceHandles() {
		return consumer.updataServiceTags();
	}

	/**
	 * 提供根据资源句柄下载对应结点列表的方法
	 * @param resourceName 资源句柄
	 * @return
	 */
	public List<NetNode> updataNews(String resourceHandle) {
		return consumer.updataNews(resourceHandle);
	}
	
	public void setResourceServer(NetNode netNode) {
		rpcClient.setIp(netNode.getIp());
		rpcClient.setPort(netNode.getPort());
	}
	
	/**
	 * 提供通过rpc向服务器请求资源的方法
	 * @param recipient 接收服务器信息
	 * @param sectionList 资源信息列表
	 * @return
	 * @throws Throwable 
	 */
	public void requestResource(NetNode recipient, List<SectionInfo> sectionList) throws Throwable {
		serverAction.requestResource(recipient, sectionList);
	}
	
	/**
	 * 当发现服务器掉线，客户端通过rpc报告给管理中心
	 */
	public void reportDropped(NetNode netNode) {
		try {
			centerAction.reportDropped(netNode);
		} catch (Throwable e) {
			// 管理中心宕机，不做处理
		}
	}
}
