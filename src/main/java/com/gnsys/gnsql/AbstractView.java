package com.gnsys.gnsql;

public abstract class AbstractView implements View {

	private String name;
	
	public AbstractView(String name) {
		this.name = name;
	}

	@Override
	public void restore(Bundle bundle) {
		name = bundle.getString("name");
	}

	@Override
	public Bundle getBundle() {
		Bundle map = new Bundle();
		map.put("name", getName());
		return map;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void onViewActivated() {
		// TODO Auto-generated method stub
		
	}

}
