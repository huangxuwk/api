package com.dl.multi_file.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.parser_reflect.util.PropertiesParser;

/**
 * 扫描文件，读写文件的基本类<br>
 * 1、给定默认根路径，也可通过properties文件配置；<br>
 * 2、根据根路径读取路径下的文件，将扫描到的资源保存在map中；<br>
 * 3、提供RandomAccessFile的随机文件读写；
 * @author dl
 *
 */
public class LocalResources {
	/**
	 * 默认根路径
	 */
	public static final String DEFAULT_PATH = "N:\\LocalResources";

	// 以文件名、起始地址与文件长字符串为键，以文件描述对象为值
	private static final Map<String, SectionInfo> systemMap = new HashMap<>();
	/**
	 * 根路径
	 */
	private static String rootPath;
	
	public LocalResources() {
		rootPath = DEFAULT_PATH;
	}
	
	public static void setRootPath(String rootPath) {
		LocalResources.rootPath = rootPath;
	}
	
	public void readConfig(String path) {
		PropertiesParser.load(path);
		
		String rootPath = PropertiesParser.findElement("rootPath");
		if (rootPath != null && !rootPath.equals("")) {
			LocalResources.rootPath = rootPath;
		}
	}
	
	/**
	 * 根据根目录读取本地资源；
	 */
	public Map<String, SectionInfo> scanLocalResource() {
		File rootFile = new File(rootPath);
		scanLocalResource(rootFile);
		
		return new HashMap<>(systemMap);
	}
	
	/**
	 * 带有递归的文件扫描；
	 * @param rootFile
	 */
	private void scanLocalResource(File rootFile) {
		File[] files = rootFile.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				String filePath = file.getAbsolutePath();
				int len = rootPath.length();
				String path = filePath.substring(len);
				int length = (int) file.length();
				SectionInfo info = new SectionInfo(path, 0, length);
				systemMap.put(info.toString(), info);
			} else {
				scanLocalResource(file);
			}
		}		
	}
	
	/**
	 * 返回map的键集，以便上层使用
	 * @return
	 */
	public List<String> resourceList() {
		return new ArrayList<>(systemMap.keySet());
	}
	
	public SectionInfo getSection(String resourceHandle) {
		return systemMap.get(resourceHandle);
	}
	
	/**
	 * 根据文件名来获取文件完整信息的字符串；
	 * @param path
	 * @return
	 */
	public String getIntactHandle(String path) {
		String[] str = path.split(":");
		Collection<String> handles = systemMap.keySet();
		for (String string : handles) {
			if (string.split(":")[0].equals(str[0])) {
				return string;
			}
		}
		return null;
	}
	
	/**
	 * 根据文件名读取指定偏移量和长度的字节数据；
	 * @param name
	 * @param offset
	 * @param size
	 */
	public byte[] readFromLocal(SectionInfo sectionInfo) {
		String path = sectionInfo.getFileName();
		int offset = sectionInfo.getOffset();
		int size = sectionInfo.getSize();
		String fileName = rootPath + "\\" + path;
		File file = new File(fileName);
		int length = (int) file.length();
		
		byte[] datas = new byte[length];
		RandomAccessFile randomAccessFile = null;
		try {
			randomAccessFile = new RandomAccessFile(file, "r");
			randomAccessFile.read(datas, offset, size);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (randomAccessFile != null) {
				try {
					randomAccessFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return datas;
	}
	
	/**
	 * 将字节数据写入文件，由读写对象、偏移地址和长度决定；
	 * @param randomAccessFile
	 * @param datas
	 * @param offset
	 * @param size
	 */
	public void writeInLocal(RandomAccessFile randomAccessFile
			, byte[] datas, int offset, int size) {
		try {
			randomAccessFile.write(datas, offset, size);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 提供带有根路径的文件读写对象；
	 * @param name
	 * @return
	 */
	public RandomAccessFile getRandomAccessFile(String path) {
		String fileName = rootPath + "\\" + path;
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(fileName, "rw");
			return randomAccessFile;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 提供带有根路径的文件只读对象；
	 * @param path
	 * @return
	 */
	public RandomAccessFile getRandomAccessFileOnlyRead(String path) {
		String fileName = rootPath + "\\" + path;
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(fileName, "r");
			return randomAccessFile;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;		
	}
	
	/**
	 * 提供增加本地资源的方法
	 * @param sectionInfo
	 */
	public void addResource(SectionInfo sectionInfo) {
		systemMap.put(sectionInfo.toString(), sectionInfo);
	}

}
