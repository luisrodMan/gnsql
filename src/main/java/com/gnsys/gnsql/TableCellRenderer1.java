package com.gnsys.gnsql;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class TableCellRenderer1 extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (value!=null)
			value = value.toString().trim();
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}

}
