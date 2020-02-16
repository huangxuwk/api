package com.dl.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.swing.JFrame;

/**
 * ������<br>
 * 1��ΪRPC������׼�����ͻ���ֻ�ñ���ӿڣ�<br>
 * 2��ͨ���ӿڵõ�������������÷�����<br>
 * 3���ڷ����н������أ�����ʱ���ӷ���������Զ�̷������ã�<br>
 * 4��֧��ģ̬��Ŀ�����
 * 
 * @author dl
 *
 */
public class RPCClientProxy {
	private RPCClient client;
	
	public RPCClientProxy() {
	}
	
	public void setClient(RPCClient client) {
		this.client = client;
	}
	
	/**
	 * ͨ��jdk��ʽ�����ݽ����Ľӿڣ���invoke()�н���Զ�̲�����
	 * @param interfaces �������Ŀ��ӿ�
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T jdkProxy(Class<?> interfaces) {
		ClassLoader classLoader = interfaces.getClassLoader();
		return (T) Proxy.newProxyInstance(classLoader, new Class<?>[]{interfaces}, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				return client.remoteProcedureCall(method, args);
			}
		});
	}
	
	/**
	 * ģ̬��ʽRMI���ӣ���ֹ�û���ε����<br>
	 * 1��ͨ��jdk��ʽ�����ݽ����Ľӿڣ���invoke()�п���ģ̬��<br>
	 * 2��ģ̬��Ŀ��������������̣߳����ģ̬��Ĺر���Ҫ������һ���߳��н��У�<br>
	 * 3��ֻ�е�����˷��ؽ������ܹر�ģ̬�����ֻ�ܽ�Զ�̲�����������һ���߳��У�<br>
	 * 4����ʹ��Callable�ӿ��������̵߳ķ���ֵ��������˵ķ���ֵ����
	 * @param interfaces �������Ŀ��ӿ�
	 * @param parent ������
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T jdkProxy(Class<?> interfaces, JFrame parent) {
		ClassLoader classLoader = interfaces.getClassLoader();
		return (T) Proxy.newProxyInstance(classLoader, new Class<?>[]{interfaces}, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				RPCClientDialog dialog = new RPCClientDialog();
				DialogManager manager = new DialogManager(dialog, method, args);
				FutureTask<Object> future = new FutureTask<>(manager);
				new Thread(future).start();
				dialog.showMessageDialog(parent, "��ʾ", "���ڴ������Ժ�");
				
				return future.get();
			}
		});
	}
	
	/**
	 * ģ̬��Ĺ�����
	 * 1��ʵ����Callable�ӿڣ��ɽ����̷߳����ķ���ֵ��<br>
	 * 2��ģ̬�������������ǰ�̣߳����ģ̬��Ŀ�����ر���Ҫ�������߳��н��У�<br>
	 * 3����Զ�̷����ĵ��÷��ڴ��߳��У����ս����ر�ģ̬��
	 * 
	 * @author dl
	 *
	 */
	private class DialogManager implements Callable<Object> {
		private RPCClientDialog dialog;
		private Method method;
		private Object[] args;
		
		public DialogManager(RPCClientDialog dialog, Method method, Object[] args) {
			this.dialog = dialog;
			this.method = method;
			this.args = args;
		}
		
		@Override
		public Object call() throws Exception {
			Object result = null;
			try {
				result = client.remoteProcedureCall(method, args);
			} finally {
				// ���۷���ʲô������ر�ģ̬��
				dialog.close();
			}
			return result;
		}
		
	}
}
