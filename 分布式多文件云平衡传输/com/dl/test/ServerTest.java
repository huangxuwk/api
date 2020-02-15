package com.dl.test;

import java.util.List;

import com.dl.multi_file.resource.LocalResources;
import com.dl.multi_file.server.ProvideServer;
import com.dl.rpc.server.RPCMethodFactory;

public class ServerTest {

	public static void main(String[] args) {
		RPCMethodFactory.scanPackage("com.dl");
		
		LocalResources local = new LocalResources();
		local.scanLocalResource();
		List<String> list = local.resourceList();
		for (String string : list) {
			System.out.println(string);
		}
		ProvideServer server = new ProvideServer();
		try {
			server.registryResource(list);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
