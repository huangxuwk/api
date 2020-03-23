package com.database.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import com.parser_reflect.util.XmlParser;

public class ClassTableFactory {
	private static final Map<String, ClassTable> classTableMap = new HashMap<>();
	
	public ClassTableFactory() {
	}

	public static void load(String path, String tagOne, String tagTwo) {
		new XmlParser() {
			ClassTable ct = new ClassTable();
			@Override
			public void parseElement(Element element) {
				try {
					ct.setKlass(Class.forName(element.getAttribute("className")));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				ct.setTable(element.getAttribute("table"));
				
				try {
					new XmlParser() {
						Class<?> klass = ct.getKlass();
						@Override
						public void parseElement(Element element) {
							PropertyColumn pc = new PropertyColumn();
							
							String key = element.getAttribute("key");
							pc.setName(ct.getTable() + "." + element.getAttribute("property"));
							try {
								pc.setField(klass.getDeclaredField(element.getAttribute("name")));
							} catch (Exception e) {
								e.printStackTrace();
							}
							if (key.length() > 0) {
								ct.setKey(pc);
							}
							ct.add(pc);
						}
					}.parseTag(element, tagTwo);
				} catch (Exception e) {
					e.printStackTrace();
				}
				classTableMap.put(ct.getKlass().getName(), ct);
			}
		}.parseTag(path, tagOne);
	}	
	
	public static void load(String className) {
		try {
			Class<?> klass = Class.forName(className);
			if (!klass.isAnnotationPresent(Annotation.class)) {
				return;
			}
			
			ClassTable ct = new ClassTable();
			Annotation an = klass.getAnnotation(Annotation.class);
			
			ct.setTable(an.value());
			Field[] fields = klass.getDeclaredFields();
			for (Field f : fields) {
				PropertyColumn pc = new PropertyColumn();
				Annotation af = f.getAnnotation(Annotation.class);
				pc.setName(ct.getTable() + "." + af.value());
				pc.setField(f);
				ct.add(pc);
				
				if (!af.key().isEmpty()) {
					ct.setKey(pc);
				}
			}
			classTableMap.put(className, ct);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	public static ClassTable get(String className) {
		return classTableMap.get(className);
	}
	
	public static ClassTable get(Class<?> klass) {
		return classTableMap.get(klass.getName());
	}
	 
	@Override
	public String toString() {
		return classTableMap.toString();
	}
}
