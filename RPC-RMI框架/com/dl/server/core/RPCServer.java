package com.dl.server.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.parser_reflect.util.PropertiesParser;
import com.util.ArgumentMaker;

/**
 * RPC服务器<br>
 * 1、支持RPC服务器的开启与关闭；<br>
 * 2、使用线程池来处理RPC客户端的方法调用；<br>
 * 3、可配置服务器的port；
 * 
 * @author dl
 *
 */
public class RPCServer implements Runnable {
	public static final int DEFAULT_PORT = 55555;
	private static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(10, 100, 
			5000, TimeUnit.MILLISECONDS, 
			new LinkedBlockingDeque<>());
	
	private ServerSocket server;
	private int port;
	
	private volatile boolean goon;
	
	/**
	 * 读取默认路径下的配置文件
	 */
	public RPCServer() {
		this.port = DEFAULT_PORT;
		readConfig("/RPCConfig.properties");
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * 读取指定路径下的配置文件
	 * @param path
	 */
	public void readConfig(String path) {
		PropertiesParser.load(path);
		
		String portStr = PropertiesParser.findElement("RPCPort");
		try {
			if (portStr != null && !portStr.equals("")) {
				int port = Integer.valueOf(portStr);
				if (port > 0 && port < 65536) {
					this.port = port;	
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 开启服务器，并将客户端侦听线程加入线程池。
	 */
	public void startup() {
		try {
			server = new ServerSocket(port);
			goon = true;
			threadPool.execute(new Thread(this));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭服务器，并关闭线程池。
	 */
	public void shutdown() {
		goon = false;
		try {
			if (!server.isClosed() && server != null) {
				server.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			server = null;
		}
		threadPool.shutdown();
	}

	/**
	 * 侦听线程，若接收到新的客户端连接，将通过线程池来处理。
	 */
	@Override
	public void run() {
		while (goon) {
			try {
				Socket socket = server.accept();
				ServerExecutor executer = new ServerExecutor();
				executer.setClient(socket);
				threadPool.execute(executer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 通信内部类<br>
	 * 1、保存通信信道，接收和发送消息；<br>
	 * 2、解析客户端的消息，并找到相应的方法执行后发送；
	 * 
	 * @author dl
	 *
	 */
	private class ServerExecutor implements Runnable {
		private DataInputStream dis;
		private DataOutputStream dos;
		
		public ServerExecutor() {
		}
		
		public void setClient(Socket client) {
			try {
				dis = new DataInputStream(client.getInputStream());
				dos = new DataOutputStream(client.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * 关闭通信信道
		 */
		private void close() {
			try {
				if (dis != null) {
					dis.close();		
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				dis = null;
			}
			try {
				if (dos != null) {
					dos.close();	
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				dos = null;
			}
		}
		
		/**
		 * 1、客户端发送两次，服务端接收两次；<br>
		 * 2、通过接收到的rpcMethodId找到对应的方法并执行；<br>
		 * 3、将方法执行的结果发送给客户端；
		 */
		@Override
		public void run() {
			try {
				String method = dis.readUTF();
				int rpcMethodId = Integer.valueOf(method);
				String argsStr = dis.readUTF();
				ArgumentMaker maker = new ArgumentMaker(argsStr);
				MethodDefinition methodDefinition = MethodFactory.getMethodDefinition(rpcMethodId);
				if (methodDefinition == null) {
					dos.writeUTF("null");
				} else {
					Object result = invokeMethod(methodDefinition, maker);
					String resultStr = ArgumentMaker.gson.toJson(result);
					dos.writeUTF(resultStr);
				}
				close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * 1、通过methodDefinition得到对应的方法和对象；<br>
		 * 2、通过gson将传送过来的字符串转换为指定的对象；
		 * 
		 * @param methodDefinition
		 * @param maker
		 * @return
		 */
		private Object invokeMethod(MethodDefinition methodDefinition, ArgumentMaker maker) {
			Object object = methodDefinition.getObject();
			Method method = methodDefinition.getMethod();
			
			Parameter[] parameters = method.getParameters();
			int size = parameters.length;
			Object[] args = new Object[size];
			for (int index = 0; index < size; index++) {
				args[index] = maker.getValue(
							"arg" + index, 
							parameters[index].getParameterizedType());
			}
			
			try {
				return method.invoke(object, args);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}		
	}
}
