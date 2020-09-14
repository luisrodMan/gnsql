package com.gnsys.gnsql;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.gnsys.gnsql.editor.NumberingView;

public class SqlEditor extends TextEditor {

	private JSplitPane verticalSplit;
	private Connection con;
	private JScrollPane resultContainer;
	private JTable resultTable;

	private JTextArea messageArea = new JTextArea();
	private JTabbedPane bottomPane = new JTabbedPane();
	private boolean runningCommand;
	
	private String connectionName;

	public SqlEditor() {
		this(null);
	}

	public SqlEditor(String name) {
		super(name);
		messageArea.setEditable(false);
		
		messageArea.setBackground(getEditor().getBackground());
		messageArea.setForeground(Color.lightGray);

		bottomPane.addTab("Messages", messageArea);

		verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		verticalSplit.setLeftComponent(super.getView());
		verticalSplit.setRightComponent(bottomPane);
	}

	@Override
	public Bundle getBundle() {
		Bundle bundle = super.getBundle();
		if (getPath()==null) {
			bundle.put("text", getText());
			bundle.put("connectionName", connectionName);
		}
		return bundle;
	}

	@Override
	public void restore(Bundle bundle) {
		super.restore(bundle);
		if (bundle.getString("path")==null && bundle.getString("text")!=null) {
			getEditor().setText(bundle.getString("text"));
			this.connectionName = bundle.getString("connectionName");
		}
	}
	
	@Override
	public void onViewActivated() {
		if (connectionName!=null)
			App.getInstance().setConnectionSelected(connectionName);
	}

	public JComponent getView() {
		return verticalSplit;
	}

	public void executeSentenceKeyStroke() {
		if (runningCommand) {
			// show wait
			return;
		}
		
		String sql = getEditor().getSelectedText();
		if (sql==null)
			return;
		sql = sql.trim();
		while (sql.endsWith(";"))
			sql = sql.substring(0, sql.length()-1);

		if (con==null) {
			createConnection();
			try {
				con.setAutoCommit(false);
			} catch (SQLException e) {
				messageArea.setText("Error setting auto commit to false:"+e.getMessage());
				e.printStackTrace();
			}
			messageArea.setText("Connection created.");
		}
		if (con==null) {
			messageArea.setText("problem creating connection.");
			return;
		}
		runningCommand = true;

		final String sql2 = sql;
		System.out.println("sql: "+sql2);
		messageArea.setText("executing query.....");

		if (resultTable!=null && bottomPane.getTabCount()>1)
			bottomPane.removeTabAt(1);
		new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					ResultSet set = con.createStatement().executeQuery(sql2);
					ResultSetMetaData metadata = set.getMetaData();
					String[] colnames = new String[metadata.getColumnCount()];
					for (int i = 0; i<colnames.length; i++)
						colnames[i] = metadata.getColumnName(i+1);

					DefaultTableModel model = null;
					int max = App.getInstance().getRowLimit();
					int count = 0;
					List<Object[]> rows = new LinkedList<>();
					
					while (count++<max && set.next()) {
						Object[] cols = new Object[colnames.length];
						for (int i = 0; i<colnames.length; i++) {
							cols[i] = set.getObject(i+1);
							if (cols[i] instanceof java.sql.Clob) {
								java.sql.Clob clob = (Clob) cols[i];
								try (BufferedReader reader = new BufferedReader(new InputStreamReader(clob.getAsciiStream()))) {
									String l = null;
									String t = "";
									while ((l=reader.readLine())!=null)
										t += l;
									cols[i] = t;
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
						rows.add(cols);
					}
					// esto no es optimo xd usar custom model for eficency
					Object[][] datas = new Object[rows.size()][colnames.length];
					for (int i = 0; i<datas.length; i++)
						datas[i] = rows.get(i);
					model = new DefaultTableModel(datas, colnames);
					// not tested yet
					JTable table = new JTable(model);
					table.setDefaultRenderer(Object.class, new TableCellRenderer1());
					table.setBackground(Color.darkGray);
					table.setForeground(Color.gray);
					table.setSelectionBackground(Color.orange);
					TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
					int samplesPerCol = 10;
					for (int c = 0; c<colnames.length; c++) {
						Component headerComponent = renderer.getTableCellRendererComponent(table, colnames[c], false, false, 0, c);
						int pw = headerComponent.getPreferredSize().width;
						int i = 0;
						while (i<samplesPerCol && i<datas.length) {
							Object value = datas[i][c];
							pw = Math.max(pw, renderer.getTableCellRendererComponent(table, value==null?null:value.toString().trim(), false, false, 0, c).getPreferredSize().width);
							i++;
						}
						table.getColumnModel().getColumn(c).setMinWidth(pw/4);
						table.getColumnModel().getColumn(c).setPreferredWidth(pw+6);
					}
					table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					messageArea.setText("query executed.");
					JScrollPane s = new JScrollPane(table);
					s.setBackground(table.getBackground());
					s.getViewport().setBackground(Color.DARK_GRAY);
					bottomPane.addTab("Results", (resultContainer = s));
					resultTable = table;
					bottomPane.setSelectedComponent(resultContainer);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					messageArea.setText(e.getMessage());
					e.printStackTrace();
				}

				runningCommand = false;
			}
		}).run();

	}

	public void close() {
		try {
			if (con!=null)
				con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createConnection() {
		try {
			if (connectionName==null) {
				DBConnection con = App.getInstance().getSelectedConnection();
				if (con != null)
					connectionName = con.getName();
			}
			if (connectionName==null) {
				System.err.println("No hay connexiones xd");
				return;
			}
			DBConnection c = App.getInstance().getConnectionManager(connectionName);
			messageArea.setText("Creating connection....... from: " + connectionName);
			con = c.createConnection();
//			Class.forName("com.ibm.as400.access.AS400JDBCDriver");
//			Properties properties = new Properties();
//			properties.setProperty("user", "JDE");
//			properties.setProperty("password", "JDE");
//			con = DriverManager.getConnection("jdbc:as400://9.9.9.6:60000", properties);
		} catch (Exception e) {
			messageArea.setText(e.getMessage());
			e.printStackTrace();
		}
	}

//	private void createConnection() {
//		try {
//			Class.forName("oracle.jdbc.driver.OracleDriver");
//			con = DriverManager.getConnection("jdbc:oracle:thin:@9.9.10.163:1521/pdb_oretail", "rms16", "welcome1");
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}

}
