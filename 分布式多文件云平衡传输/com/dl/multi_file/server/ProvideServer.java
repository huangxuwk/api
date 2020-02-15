package com.dl.multi_file.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.dl.multi_file.resource.LocalResources;
import com.dl.rpc.server.RPCServer;
import com.dl.sd.netWork.NetNode;
import com.dl.sd.provider.Provider;
import com.util.ThreadPoolFactory;

/**
 * 资源服务器（资源提供者）<br>
 * 1、通过服务发现的提供者对象来注册注销资源；<br>
 * 2、作为rpc服务器来接收客户端的请求；<br>
 * 3、本服务器提供了将本地资源全部注册到管理中心的方法；
 * @author dl
 *
 */
public class ProvideServer {
	private Provider provider;
	private RPCServer rpcServer;
	
	private LocalResources localResources;
	private static NetNode netNode;
	
	public ProvideServer() {
		localResources = new LocalResources();
		provider = new Provider();
		rpcServer = new RPCServer();
		rpcServer.setPort(60000);
		rpcServer.startup();
	}
	
	public static NetNode getNetNode() {
		return netNode;
	}
	
	
	/**
	 * 读取指定路径下的rpc配置文件
	 * @param path
	 */
	public void readRPCConfig(String path) {
		rpcServer.readConfig(path);
	}
	
	public void shutdown() {
		rpcServer.shutdown();
		new ThreadPoolFactory().close(false);
	}
	
	/**
	 * 服务器上线先注册本地资源<br>
	 * 1、网络结点上线服务器的前提是客户端扫描到本地有资源或申请了某些资源；<br>
	 * 2、服务器要做到一上线就将本地资源全部注册到管理中心；
	 * @throws Throwable 注册失败，rpc连接失败
	 */
	public void registryLocalResource() throws Throwable {
		List<String> resourceList = localResources.resourceList();
		registryResource(resourceList);
	}
	
	private void getLocalNet() {
		String ip = getLocalIp();
		int port = rpcServer.getPort();
		NetNode netNode = new NetNode(ip, port);
		ProvideServer.netNode = netNode;
		provider.setNetNode(netNode);
	}
	
	public void registryResource(List<String> resourceList) throws Throwable {
		if (netNode == null) {
			getLocalNet();
		}
		provider.registryService(resourceList);		
	}
	
	public void registryResource(String resource) throws Throwable {
		List<String> resourceList = new ArrayList<>();
		resourceList.add(resource);
		registryResource(resourceList);
	}
	
	/**
	 * 注销资源，由rpc负责，可能会发送失败；<br>
	 * 失败后告知上一层；
	 * @param resourceList 注销资源列表
	 * @throws Throwable 注销失败，rpc连接失败
	 */
	public void cancellationService(List<String> resourceList) throws Throwable {
		if (netNode == null) {
			getLocalNet();
		}
		provider.cancellationService(resourceList);
	}
	
	public void cancellationService(String resource) throws Throwable {
		List<String> resourceList = new ArrayList<>();
		resourceList.add(resource);
		cancellationService(resourceList );
	}

	/**
	 * 提供获取本地ip地址的方法
	 * @return
	 */
	public static String getLocalIp() {
		try {
			InetAddress address = InetAddress.getLocalHost();
			String ip = address.getHostAddress();
			return ip;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
}
