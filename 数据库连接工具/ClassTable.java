package com.database.util;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClassTable {
	Class<?> klass;
	String table;
	List<PropertyColumn> list;
	PropertyColumn key;
	
	public ClassTable() {
		list = new ArrayList<>();
	}

	public Class<?> getKlass() {
		return klass;
	}

	public void setKlass(Class<?> klass) {
		this.klass = klass;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public List<PropertyColumn> getList() {
		return list;
	}

	public void add(PropertyColumn pc) {
		list.add(pc);
	}

	public PropertyColumn getKey() {
		return key;
	}

	public void setKey(PropertyColumn key) {
		this.key = key;
	}
	
	public String getPropertyString() {
		StringBuffer str = new StringBuffer("");
		for  (int i = 0; i < list.size(); i++) {
			str.append(list.get(i).getName());
			if (i != list.size() - 1) {
				str.append(",");
			}
		}
		return str.toString();
	}
	
	public List<Field> getFieldList() {
		List<Field> propertyList = new ArrayList<>();
		
		for (PropertyColumn pc : list) {
			propertyList.add(pc.getField());
		}
		
		return propertyList;
	}
	
	public void setProperty(ResultSet resultSet, Object object) {
		try {
			for (PropertyColumn pc : list) {
				pc.set(object, resultSet.getObject(pc.getName()));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return klass.getName() + "-"+ table + "-" + list + "-" + key;
	}
	
}
