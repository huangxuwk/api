package com.dl.test;

import com.dl.multi_file.center.ManagementCenter;
import com.dl.rpc.server.RPCMethodFactory;

public class CenterTest {

	public static void main(String[] args) {
		RPCMethodFactory.scanPackage("com.dl");
		
		ManagementCenter center = new ManagementCenter();
		center.startup();
		
	}
}
