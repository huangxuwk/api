package com.dl.rpc.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.parser_reflect.util.PackageScanner;

/**
 * RPC����ɨ����<br>
 * 1��������RPC�ĺ����࣬��ָ�������е�ָ���ӿڽ���ɨ�裻<br>
 * 2����ɨ�赽�ķ������浽methodPool�У�<br>
 * 3��map�Խӿڷ����Ĺ�ϣֵΪ������MethodDefinitionΪֵ��<br>
 * 4���ṩ�ֶ�ע��ķ�����֧��ͨ����ϣֵ��ȡDefinition���ֶΣ�
 * 
 * @author dl
 *
 */
public class RPCMethodFactory {
	private static final Map<Integer, RPCMethodDefinition> methodPool;
	
	static {
		methodPool = new HashMap<>();
	}

	public RPCMethodFactory() {
	}
	
	/**
	 * ��ɨ�裬�ҵ�ָ��ע����ࣻ
	 * 
	 * @param path
	 */
	public static void scanPackage(String path) {
		new PackageScanner() {
			
			@Override
			public void dealClass(Class<?> klass) {
				if (klass.getAnnotation(RPCProxyAnntotation.class) == null) {
					return;
				}
				priRegistry(klass);
			}
		}.packageScanner(path);
	}
	
	/**
	 * ��������ָ���ӿڵķ������γ�map��
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
		
		RPCProxyAnntotation proxy = klass.getAnnotation(RPCProxyAnntotation.class);
		Class<?>[] interfaces = proxy.interfaces();
		// ����ע���еĽӿ�����
		for (Class<?> clazz : interfaces) {
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				try {
					// method.getParameterTypes() ���Եõ����Ͳ�������
					Method meth = klass.getDeclaredMethod(method.getName(), method.getParameterTypes());
					if (meth != null) {
						RPCMethodDefinition definition = new RPCMethodDefinition(klass, object, meth);
						// �γ���hashCodeΪ��, MethodDefinitionΪֵ��key-value
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
	 * �ṩ��ʾ���ֶ�ע���ֶΣ�
	 * 
	 * @param klass
	 */
	public void registry(Class<?> klass) {
		if (klass.getAnnotation(RPCProxyAnntotation.class) == null) {
			return;
		}
		priRegistry(klass);
	}
	
	/**
	 * ͨ���ӿڷ�����hashCode����ȡ��Ӧ��MethodDefinition��
	 * 
	 * @param rpcMethodId
	 * @return
	 */
	public static RPCMethodDefinition getMethodDefinition(int rpcMethodId) {
		return methodPool.get(rpcMethodId);
	}
}
