package com.parser_reflect.util;


import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class XmlParser {
	private static DocumentBuilder db;
	
	static {
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}		
	}
	
	public XmlParser() {
	}
	
	public Document parserPath(String path) throws SAXException, IOException {
		InputStream is = Class.class.getResourceAsStream(path);
		Document document = db.parse(is);
		
		return document;
	}
	
	public abstract void parseElement(Element element);
	
	public void parseTag(String path, String tagname) {
		try {
			NodeList nodeList = parserPath(path).getElementsByTagName(tagname);
			for (int index = 0; index < nodeList.getLength(); index++) {
				Element element = (Element) nodeList.item(index);
				parseElement(element);
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void parseTag(Element element, String tagname) {
		NodeList nodeList = element.getElementsByTagName(tagname);
		for (int index = 0; index < nodeList.getLength(); index++) {
			Element ele = (Element) nodeList.item(index);
			parseElement(ele);
		}
	}
}
