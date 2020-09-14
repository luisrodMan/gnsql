package com.gnsys.gnsql;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Bundle implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Map<String, String> map = new HashMap<>();
	
	public void put(String name, String value) {
		map.put(name, value);
	}

	public String getString(String name, String def) {
		String value = map.get(name);
		return value!=null?value:def;
	}
	
	public String getString(String name) {
		return getString(name, null);
	}

}
