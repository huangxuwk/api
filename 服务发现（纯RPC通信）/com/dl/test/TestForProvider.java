package com.dl.test;

import java.util.ArrayList;

import com.dl.sd.provider.Provider;

public class TestForProvider {

	public static void main(String[] args) {
			Provider provider = new Provider();
			ArrayList<String> services = new ArrayList<>();
			try {
				provider.registryService(services);		
				provider.cancellationService(services);
			} catch (Throwable e) {
				e.printStackTrace();
			}
//		provider.shutdown();
		
	}

}
