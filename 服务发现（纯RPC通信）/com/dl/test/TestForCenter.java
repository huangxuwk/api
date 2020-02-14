package com.dl.test;

import com.dl.rpc.server.RPCMethodFactory;
import com.dl.sd.registry.RegistrationCenter;

public class TestForCenter {

	public static void main(String[] args) {
		RPCMethodFactory.scanPackage("com.dl");
		
		RegistrationCenter center = new RegistrationCenter();
		center.startup();
	}

}
