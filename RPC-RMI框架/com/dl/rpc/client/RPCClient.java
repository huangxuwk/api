package com.dl.rpc.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;

import com.parser_reflect.util.PropertiesParser;
import com.util.ArgumentMaker;

/**
 * RPC客户端<br>
 * 1、提供连接服务器的方法；<br>
 * 2、支持服务器的ip、port配置；<br>
 * 3、提供实现负载均衡的接口；
 * 
 * @author dl
 *
 */
public class RPCClient {
	public static final String DEFAULT_IP = "127.0.0.1";
	public static final int DEFAULT_PORT = 55555;
	
	private Socket client;
	private DataInputStream dis;
	private DataOutputStream dos;
	private int port;
	private String ip;
	
	private RPCServerBalance serverBalance;
	
	public RPCClient() {
		this.ip = DEFAULT_IP;
		this.port = DEFAULT_PORT;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	/**
	 * 负载均衡接口
	 * @param serverBalance
	 */
	public void setServerBalance(RPCServerBalance serverBalance) {
		this.serverBalance = serverBalance;
	}
	
	public void readConfig(String path) {
		PropertiesParser.load(path);
		
		String ip = PropertiesParser.findElement("RPCIp");
		if (ip != null && !ip.equals("")) {
			this.ip = ip;
		}
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
	 * 客户端连接服务端；<br>
	 * 若用户设置了负载均衡器，将采用负载均衡算法后的服务结点；
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private void startup() throws UnknownHostException, IOException {
		if (serverBalance != null) {
			RPCServerNetNode node = serverBalance.getServerNode();
			this.ip = node.getIp();
			this.port = node.getPort();
		}
		client = new Socket(ip, port);
		dis = new DataInputStream(client.getInputStream());
		dos = new DataOutputStream(client.getOutputStream());
	}
	
	private void close() {
		try {
			if (!client.isClosed() && client != null) {
				client.close();		
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			client = null;
		}
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
	 * RMI客户端核心方法<br>
	 * 1、连接服务端；<br>
	 * 2、得到调用方法的哈希值并传递给服务端；<br>
	 * 3、将参数数组转化为指定的字符串传递给服务端；<br>
	 * 4、接收来自服务端方法的执行结果；<br>
	 * 5、将接收的结果转化为对象并返回；<br>
	 * 6、关闭通信信道，完成一个RMI连接；
	 * @param method 调用的方法
	 * @param args 参数对象数组
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public <T> T remoteProcedureCall(Method method, Object[] args) throws UnknownHostException, IOException {
		startup();
		int hashCode = method.toString().hashCode();
		ArgumentMaker maker = new ArgumentMaker();
		try {
			dos.writeUTF(String.valueOf(hashCode));
			if (args == null) {
				dos.writeUTF("");
			} else {
				int index = 0;
				for (Object object : args) {
					maker.addArg("arg" + index++, object);
				}
				dos.writeUTF(maker.toString());
			}
			String str = dis.readUTF();
			Type returnType = method.getGenericReturnType();
			if (returnType != void.class) {
				T result = (T) ArgumentMaker.gson.fromJson(str, returnType);			
				return result;
			}
			close();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
