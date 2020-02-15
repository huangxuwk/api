package com.dl.test;

import java.util.List;

import com.dl.multi_file.client.RequestClient;
import com.dl.multi_file.client.ResourceRequester;
import com.dl.multi_file.resource.LocalResources;

public class ClientTest {
	
	public static void main(String[] args) {
		LocalResources local = new LocalResources();
		local.scanLocalResource();
		List<String> list = local.resourceList();
		for (String string : list) {
			System.out.println(string);
		}
		RequestClient client = new RequestClient();
		ResourceRequester requester = new ResourceRequester(client);
		requester.openProgressBar(true);
		for (String string : list) {
			if (string.equals("\\1.mp4:0:317768250")) {
				requester.requestResource(string);				
			}
		}
	}
}
