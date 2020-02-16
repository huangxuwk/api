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
 * RPC�ͻ���<br>
 * 1���ṩ���ӷ������ķ�����<br>
 * 2��֧�ַ�������ip��port���ã�<br>
 * 3���ṩʵ�ָ��ؾ���Ľӿڣ�
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
	 * ���ؾ���ӿ�
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
	 * �ͻ������ӷ���ˣ�<br>
	 * ���û������˸��ؾ������������ø��ؾ����㷨��ķ����㣻
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
	 * RMI�ͻ��˺��ķ���<br>
	 * 1�����ӷ���ˣ�<br>
	 * 2���õ����÷����Ĺ�ϣֵ�����ݸ�����ˣ�<br>
	 * 3������������ת��Ϊָ�����ַ������ݸ�����ˣ�<br>
	 * 4���������Է���˷�����ִ�н����<br>
	 * 5�������յĽ��ת��Ϊ���󲢷��أ�<br>
	 * 6���ر�ͨ���ŵ������һ��RMI���ӣ�
	 * @param method ���õķ���
	 * @param args ������������
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
