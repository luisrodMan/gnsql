package com.gnsys.gnsql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DBConnectionFromProperties implements DBConnection {
	
	private Map<String, Object> values;
	private String name;
	
	public DBConnectionFromProperties(String name, Map<String, Object> values) {
		this.name = name;
		this.values = values;
	}

	public String getName() {
		return name;
	}
	
	@SuppressWarnings("unchecked")
	private String getValue(String name) {
		Object value = values.get(name);
		if (value!=null)
			return value.toString();
		else {
			Object o = values.get("properties");
			if (o!=null && o instanceof HashMap<?, ?>)
				return ((HashMap<String, String>)o).get(name);
		}
		return null;
	}
	
	public Connection createConnection() throws ClassNotFoundException, SQLException {
		Class.forName(getValue("driver"));
		Properties properties = new Properties();
		properties.setProperty("user", getValue("user"));
		properties.setProperty("password", getValue("password"));
		return DriverManager.getConnection(getValue("url"), properties);
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
