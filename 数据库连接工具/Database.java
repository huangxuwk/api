package com.database.util;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.parser_reflect.util.PropertiesParser;

public class Database {
	private static Connection connection;
	private static String driver;
	private static String url;
	private static String user;
	private static String password;
	
	static {
		driver = PropertiesParser.findElement("driver");
		url = PropertiesParser.findElement("url");
		user = PropertiesParser.findElement("user");
		password = PropertiesParser.findElement("password");
		
		getConnection();
	}
	
	public Database() {

	}
	
	public static void getConnection() {
		if (null == connection) {
			synchronized (Class.class) {
				if (null == connection) {
					try {
						Class.forName(driver);
						connection = DriverManager.getConnection(url, user, password);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> executeQuery(String className) {
		List<T> dataList = new ArrayList<>();
		ClassTable ct = getClassTable(className);
		if (ct == null) {
			return null;
		}
		
		try {
			String sql = "SELECT " + ct.getPropertyString() + " FROM " + ct.getTable();
			PreparedStatement state = connection.prepareStatement(sql);
			ResultSet rs = state.executeQuery();
			while (rs.next()) {
				Class<?> klass = Class.forName(className);
				Object object = klass.newInstance();
				
				ct.setProperty(rs, object);
				dataList.add((T) object);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return dataList;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String className, Object key) {
		ClassTable ct = getClassTable(className);
		if (ct == null) {
			return null;
		}
		try {
			String sql = "SELECT " + ct.getPropertyString() + " FROM " + ct.getTable() + " WHERE " + ct.getKey().getName() + "=?";
			PreparedStatement state = connection.prepareStatement(sql);
			state.setObject(1, key);
			ResultSet rs = state.executeQuery();
			ResultSet resultSet = rs;
			if (resultSet.next()) {
				Class<?> klass = Class.forName(className);
				Object object = klass.newInstance();
				ct.setProperty(resultSet, object);
				return (T) object;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public <T> void insert(T model) {
		String className = model.getClass().getName();
		ClassTable ct = getClassTable(className);
		if (ct == null) {
			return;
		}
		List<Field> listField = ct.getFieldList();
		
		int length = ct.getList().size();
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < length; i++) {
			str.append("?");
			if (i != length - 1) {
				str.append(", ");
			}
		}
		try {
			String sql = "INSERT INTO " + ct.getTable() +  "(" + ct.getPropertyString() + ")" + "VALUES (" + str + ")";
			PreparedStatement state = connection.prepareStatement(sql);
			for (int i = 0; i < length; i++) {
				listField.get(i).setAccessible(true);
				System.out.println(listField.get(i).get(model));
				state.setObject(i+1, listField.get(i).get(model));
			}
			state.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public <T> void update(T model) {
		String className = model.getClass().getName();
		ClassTable ct = getClassTable(className);
		if (ct == null) {
			return;
		}
		
		PropertyColumn key = ct.getKey();
		key.getField().setAccessible(true);
		List<PropertyColumn> pcList = ct.getList(); 
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < pcList.size(); i++) {
			if (!pcList.get(i).equals(key)) {
				try {
					if (pcList.get(i).getField().get(model).getClass() == String.class) {
						str.append(pcList.get(i).getName() + "='" + pcList.get(i).getField().get(model) + "'");
					} else {
						str.append(pcList.get(i).getName() + "=" + pcList.get(i).getField().get(model));
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				if (i != pcList.size() - 1) {
					str.append(", ");
				}
			}
		}
		try {
			String sql = "UPDATE " + ct.getTable() + " SET "+ str +" WHERE " + key.getName() + "=" + key.getField().get(model);
			System.out.println(sql);
			PreparedStatement state = connection.prepareStatement(sql);
			state.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public <T> void deteled(T model) {
		String className = model.getClass().getName();
		ClassTable ct = getClassTable(className);
		if (ct == null) {
			return;
		}
		PropertyColumn key = ct.getKey();
		key.getField().setAccessible(true);
		
		try {
			String sql = "DELETE FROM " + ct.getTable() + " WHERE " + key.getName() + "=" + key.getField().get(model);
			PreparedStatement state = connection.prepareStatement(sql);
			state.executeUpdate();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private ClassTable getClassTable(String className) {
		ClassTable ct = ClassTableFactory.get(className);
		if (ct == null) {
			ClassTableFactory.load(className);
			ct = ClassTableFactory.get(className);
		}
		return ct;
	}
	
}
