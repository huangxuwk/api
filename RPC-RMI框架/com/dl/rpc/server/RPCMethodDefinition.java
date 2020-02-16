package com.dl.rpc.server;

import java.lang.reflect.Method;

/**
 * ���淽����Ϣ����<br>
 * ��ɨ�赽�ķ����Ͷ�Ӧ����Ͷ��󱣴棻
 * 
 * @author dl
 *
 */
public class RPCMethodDefinition {
	private Class<?> klass;
	private Object object;
	private Method method;

	public RPCMethodDefinition() {
	}
	
	public RPCMethodDefinition(Class<?> klass, Object object, Method method) {
		this.klass = klass;
		this.object = object;
		this.method = method;
	}
	
	public Class<?> getKlass() {
		return klass;
	}

	public void setKlass(Class<?> klass) {
		this.klass = klass;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

}
