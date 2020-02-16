package com.dl.rpc.client;

/**
 * 提供服务器负载均衡的接口
 * 
 * @author dl
 *
 */
public interface RPCServerBalance {
	RPCServerNetNode getServerNode();
}
