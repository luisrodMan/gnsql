package com.gnsys.gnsql;

public interface Editor extends View {
	
	boolean isDirty();
	
	String getPath();
	
	void save();
	
	void saveAs();
	
}
