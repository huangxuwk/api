package com.parser_reflect.util;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PropertiesParser {
	private final static  Map<String, String> map = new HashMap<>();
	private static Properties parse;
	
	static {
		parse = new Properties();
	}
	
	public PropertiesParser() {
	}
	
	public static void load(String path) {
		InputStream is = Class.class.getResourceAsStream(path);
		parseTag(is);
	}
	
	public static void parseTag(InputStream is) {
		try {
			parse.load(is);
			Set<Object> keys = parse.keySet();
			Iterator<Object> it = keys.iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				String value = parse.getProperty(key);
				map.put(key, value);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String findElement(String key) {
			return map.get(key);
	}
}
