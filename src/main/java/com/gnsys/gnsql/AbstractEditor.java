package com.gnsys.gnsql;

public abstract class AbstractEditor extends AbstractView implements Editor {

	public AbstractEditor(String name) {
		super(name);
	}

	@Override
	public Bundle getBundle() {
		Bundle bundle = super.getBundle();
		bundle.put("path", getPath());
		return bundle;
	}
	
}
