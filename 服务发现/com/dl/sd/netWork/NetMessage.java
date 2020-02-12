package com.dl.sd.netWork;

/**
 * 消息转换类<br>
 * 1、将消息类型与消息内容整合成字符串；<br>
 * 2、将收到的字符串转化为指定的对象；
 * 
 * @author dl
 *
 */
public class NetMessage {
	private EMessageType type;
	private String action;
	private String paramater;
	
	public NetMessage() {
	}
	
	public NetMessage(String message) {
		String mess = message.toString();
		int index = mess.indexOf(":");
		this.type = EMessageType.valueOf(mess.substring(0, index));
		mess = mess.substring(index+1);
		index = mess.indexOf(":");
		String action = mess.substring(0, index);
		this.action = action.equals("") ? null : action;
		this.paramater = mess.substring(index+1);
	}
	
	public EMessageType getType() {
		return type;
	}
	
	public NetMessage setType(EMessageType type) {
		this.type = type;
		return this;
	}
	
	public String getAction() {
		return action;
	}
	
	public NetMessage setAction(String action) {
		this.action = action;
		return this;
	}
	
	public String getParamater() {
		return paramater;
	}
	
	public NetMessage setParamater(String paramater) {
		this.paramater = paramater;
		return this;
	}
	
	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append(type + ":").append((action == null ? "" : action) + ":").append(paramater);
		
		return str.toString();
	}
	
}
