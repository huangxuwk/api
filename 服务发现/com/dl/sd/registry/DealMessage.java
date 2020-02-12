package com.dl.sd.registry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import com.dl.sd.netWork.NetMessage;
import com.dl.sd.netWork.NetNode;
import com.google.gson.reflect.TypeToken;
import com.util.ArgumentMaker;

/**
 * 接口实现类<br>
 * 1、处理服务器发送的消息；<br>
 * 2、通过反射机制来避免多个switch-case；<br>
 * 3、使用反射机制的前提是要使用特定规则的方法名和传递相同的参数；
 * 
 * @author dl
 *
 */
public class DealMessage implements IDealMessage {
	private ServiceCache serviceCache;
	private Type type = new TypeToken<List<String>>() {}.getType();
	
	public DealMessage() {
		serviceCache = new ServiceCache();
	}
		
	@Override
	public void dealMessage(NetNode netNode, String message) {
		NetMessage netMessage = new NetMessage(message);
		
		Class<?> klass = DealMessage.class;
		String type = netMessage.getType().toString();
		String methodName = "deal" + type.substring(0, 1) + type.substring(1).toLowerCase();
		
		try {
			Method method = 
					klass.getDeclaredMethod(methodName, new Class<?>[]{NetNode.class, NetMessage.class});
			
			method.invoke(this, netNode, netMessage);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private void dealRegistry(NetNode netNode, NetMessage netMessage) {
		String para = netMessage.getParamater();
		List<String> tagList = ArgumentMaker.gson.fromJson(para, type);
		for (String registryTag : tagList) {
			serviceCache.put(registryTag, netNode);
		}
	}
	
	@SuppressWarnings("unused")
	private void dealCancellation(NetNode netNode, NetMessage netMessage) {
		String para = netMessage.getParamater();
		List<String> tagList = ArgumentMaker.gson.fromJson(para, type);
		for (String registryTag : tagList) {
			serviceCache.remove(registryTag, netNode);
		}
	}
	
}
