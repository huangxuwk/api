package com.dl.sd.consumer;
import com.dl.sd.netWork.NetNode;

public interface ICenterAction {
	 void reportDropped(NetNode netNode) throws Throwable;
}
