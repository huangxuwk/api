package com.parser_reflect.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class PackageScanner {
	
	// 抽象方法是提供给工具使用者处理文件的手段
	public abstract void dealClass(Class<?> klass);
	
	private void dealClassFile(String rootPackage, File file) {
		String fileName = file.getName();
		if (fileName.endsWith(".class")) {
			fileName = fileName.replace(".class", "");
			try {
				Class<?> klass = Class.forName(rootPackage + "." + fileName);
				// 扫描到类并得到Class，使用抽象方法交给工具使用者处理
				dealClass(klass);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	// 扫描普通包
	private void dealDirectory(String rootPackage, File curFile) {
		File[] fileList = curFile.listFiles();
		for (File file : fileList) {
			// 判断是否为目录，若是目录，则形成递归，扫描所有的目录和文件
			if (file.isDirectory()) {
				dealDirectory(rootPackage + "." + file.getName(), file);
			} else if (file.isFile()) {
				dealClassFile(rootPackage, file);
			}
		}
	}
	
	// 扫描jar包
	private void dealJarPackage(URL url) {
		try {
			JarURLConnection connection = (JarURLConnection) url.openConnection();
			JarFile jarFile = connection.getJarFile();
			Enumeration<JarEntry> jarEntries = jarFile.entries();
			// 依次扫描该包内的所有目录和文件
			while (jarEntries.hasMoreElements()) {
				JarEntry jar = jarEntries.nextElement();
				if(jar.isDirectory() || !jar.getName().endsWith(".class")) {
					continue;
				}
				String jarName = jar.getName();
				jarName = jarName.replace(".class", "");
				jarName = jarName.replace("/", ".");
				
				try {
					Class<?> klass = Class.forName(jarName);
					// 扫描到类并得到Class，使用抽象方法交给工具使用者处理
					dealClass(klass);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 使用包名进行扫描
	public void packageScanner(String packageName) {
		String rootPackage = packageName;
		packageName = packageName.replace(".", "/");
		URL url = Thread.currentThread().getContextClassLoader().getResource(packageName);
		try {
			// 判断该包为普通包还是jar包，选择不用的方法来处理
			if (url.getProtocol().equals("file")) {
				URI uri = url.toURI();
				File root = new File(uri);
				dealDirectory(rootPackage, root);
			} else {
				dealJarPackage(url);
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	// 使用类名进行扫描
	public void packageScanner(Class<?> klass) {
		packageScanner(klass.getPackage().getName());
	}
 
	public void packageScanner(Class<?>[] classes) {
		for(Class<?> klass : classes) {
			packageScanner(klass);
		}
	}
 
	public void packageScanner(String[] packages) {
		for(String packageName : packages) {
			packageScanner(packageName);
		}
	}

}
