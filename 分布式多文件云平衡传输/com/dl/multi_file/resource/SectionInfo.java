package com.dl.multi_file.resource;

import java.io.Serializable;

/**
 * 文件描述类<br>
 * 1、可描述整个文件，也可描述片段信息；<br>
 * 2、可序列化和反序列化，为对象传输奠定基础；
 * @author dl
 *
 */
public class SectionInfo implements Serializable {
	private static final long serialVersionUID = 3417961581440292164L;

	private String fileName;
	private int offset;
	private int size;
	
	public SectionInfo() {
	}

	/**
	 * 将符合协议的字符串解析为SectionInfo对象
	 * @param sectionInfoStr
	 */
	public SectionInfo(String sectionInfoStr) {
		String[] info = sectionInfoStr.split(":");
		if (info.length == 3) {
			this.fileName = info[0];
			this.offset = Integer.valueOf(info[1]);
			this.size = Integer.valueOf(info[2]);
		}
	}
	
	public SectionInfo(String fileName, int offset, int size) {
		this.fileName = fileName;
		this.offset = offset;
		this.size = size;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public boolean isRight(int offset, int size) {
		return this.offset <= offset && (this.offset + this.size) >= (offset + size);
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + offset;
		result = prime * result + size;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SectionInfo other = (SectionInfo) obj;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (offset != other.offset)
			return false;
		if (size != other.size)
			return false;
		return true;
	}

	/**
	 * 此方法产生的字符串将用作资源管理中心的map中的键；<br>
	 * 通过此字符串进行资源请求；在一套资源体系中一定是唯一的；
	 */
	@Override
	public String toString() {
		if (fileName == null || fileName.equals("")) {
			return " :" + offset + ":" + size;
		}
		return fileName + ":" + offset + ":" + size;
	}
}
