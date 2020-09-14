package com.gnsys.gnsql;

import java.awt.event.ActionListener;

public class KeyMap {
	
	private String name;
	private String shortcut;
	private ActionListener listener;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getShortcut() {
		return shortcut;
	}
	public void setShortcut(String shortcut) {
		this.shortcut = shortcut;
	}
	public ActionListener getListener() {
		return listener;
	}
	public void setListener(ActionListener listener) {
		this.listener = listener;
	}

}
