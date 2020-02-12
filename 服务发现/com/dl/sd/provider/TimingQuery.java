package com.dl.sd.provider;

/**
 * 接口实现类<br>
 * 当注册中心宕机后，将使用本类去不断连接注册中心；
 * @author dl
 *
 */
public class TimingQuery implements ITimingQuery {

	@Override
	public void dealTimingQuery(Provider provider) {
		provider.startup();
	}
	
}
