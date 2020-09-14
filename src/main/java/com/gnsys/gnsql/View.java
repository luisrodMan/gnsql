package com.gnsys.gnsql;

import javax.swing.JComponent;

public interface View {

	String getName();
	
	void restore(Bundle bundle);

	Bundle getBundle();
	
	JComponent getView();

	void close();

	void onViewActivated();
	
}
