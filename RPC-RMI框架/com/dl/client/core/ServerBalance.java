package com.dl.client.core;

/**
 * 提供服务器负载均衡的接口
 * 
 * @author dl
 *
 */
public interface ServerBalance {
	ServerNetNode getServerNode();
}
