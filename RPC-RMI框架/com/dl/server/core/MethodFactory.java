package com.dl.server.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.parser_reflect.util.PackageScanner;

/**
 * RPC方法扫描类<br>
 * 1、本类是RPC的核心类，对指定的类中的指定接口进行扫描；<br>
 * 2、将扫描到的方法缓存到methodPool中；<br>
 * 3、map以接口方法的哈希值为键，以MethodDefinition为值；<br>
 * 4、提供手动注册的方法，支持通过哈希值获取Definition的手段；
 * 
 * @author dl
 *
 */
public class MethodFactory {
	private static final Map<Integer, MethodDefinition> methodPool;
	
	static {
		methodPool = new HashMap<>();
	}

	public MethodFactory() {
	}
	
	/**
	 * 包扫描，找到指定注解的类；
	 * 
	 * @param path
	 */
	public static void scanPackage(String path) {
		new PackageScanner() {
			
			@Override
			public void dealClass(Class<?> klass) {
				if (klass.getAnnotation(ProxyAnntotation.class) == null) {
					return;
				}
				priRegistry(klass);
			}
		}.packageScanner(path);
	}
	
	/**
	 * 遍历类中指定接口的方法，形成map。
	 * 
	 * @param klass
	 */
	private static void priRegistry(Class<?> klass) {
		Object object = null;
		try {
			object = klass.newInstance();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}
		
		ProxyAnntotation proxy = klass.getAnnotation(ProxyAnntotation.class);
		Class<?>[] interfaces = proxy.interfaces();
		// 遍历注解中的接口数组
		for (Class<?> clazz : interfaces) {
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				try {
					// method.getParameterTypes() 可以得到泛型参数类型
					Method meth = klass.getDeclaredMethod(method.getName(), method.getParameterTypes());
					if (meth != null) {
						MethodDefinition definition = new MethodDefinition(klass, object, meth);
						// 形成以hashCode为键, MethodDefinition为值的key-value
						methodPool.put(method.toString().hashCode(), definition);
					}
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 提供显示的手动注册手段；
	 * 
	 * @param klass
	 */
	public void registry(Class<?> klass) {
		if (klass.getAnnotation(ProxyAnntotation.class) == null) {
			return;
		}
		priRegistry(klass);
	}
	
	/**
	 * 通过接口方法的hashCode来获取相应的MethodDefinition；
	 * 
	 * @param rpcMethodId
	 * @return
	 */
	public static MethodDefinition getMethodDefinition(int rpcMethodId) {
		return methodPool.get(rpcMethodId);
	}
}
