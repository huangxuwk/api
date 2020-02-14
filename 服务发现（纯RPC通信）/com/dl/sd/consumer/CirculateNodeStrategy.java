package com.dl.sd.consumer;

import java.util.List;

import com.dl.sd.netWork.INetNode;
import com.dl.sd.netWork.NetNode;

/**
 * 相同服务循环使用服务器<br>
 * 优点：结点选择较为简单<br>
 * 缺点：当大量的客户端结点选择同步时，依然会造成服务器的过载
 * 
 * @author dl
 *
 */
public class CirculateNodeStrategy implements INodeStrategy {
	private static int SerialNumber;
	
	public CirculateNodeStrategy() {
		SerialNumber = 0;
	}
	
	@Override
	public INetNode ServerBalance(Consumer consumer, String serviceTag, List<NetNode> nodeList) {
		if (SerialNumber >= nodeList.size()) {
			SerialNumber = 0;
		}
		
		return nodeList.get(SerialNumber++);
	}
	
}
