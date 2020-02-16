package com.dl.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.swing.JFrame;

/**
 * 代理类<br>
 * 1、为RPC调用做准备，客户端只用保存接口；<br>
 * 2、通过接口得到代理对象来调用方法；<br>
 * 3、在方法中进行拦截，拦截时连接服务器进行远程方法调用；<br>
 * 4、支持模态框的开启；
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
	 * 通过jdk方式代理传递进来的接口，在invoke()中进行远程操作；
	 * @param interfaces 被代理的目标接口
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
	 * 模态框式RMI连接（防止用户多次点击）<br>
	 * 1、通过jdk方式代理传递进来的接口，在invoke()中开启模态框；<br>
	 * 2、模态框的开启将阻塞开启线程，因此模态框的关闭需要在另外一个线程中进行；<br>
	 * 3、只有当服务端返回结果后才能关闭模态框，因此只能将远程操作放在另外一个线程中；<br>
	 * 4、可使用Callable接口来接收线程的返回值（即服务端的返回值）；
	 * @param interfaces 被代理的目标接口
	 * @param parent 父窗口
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
				dialog.showMessageDialog(parent, "提示", "正在处理，请稍后！");
				
				return future.get();
			}
		});
	}
	
	/**
	 * 模态框的管理类
	 * 1、实现了Callable接口，可接受线程方法的返回值；<br>
	 * 2、模态框开启后会阻塞当前线程，因此模态框的开启与关闭需要在两个线程中进行；<br>
	 * 3、将远程方法的调用放在此线程中，接收结果后关闭模态框；
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
				// 无论发生什么，必须关闭模态框
				dialog.close();
			}
			return result;
		}
		
	}
}
