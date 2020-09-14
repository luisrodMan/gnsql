package com.gnsys.gnsql;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Hello world!
 *
 */
public class App {
	
	private static App instance;
	
	public static App getInstance() {
		return instance;
	}

	private JFrame frame;
	private JToolBar toolbar = new JToolBar();
	private JButton newEditorBtn;
	
	private JSplitPane horizontalSplit;

	private JTabbedPane viewsPane = new JTabbedPane();
	
	private Map<JComponent, View> views = new HashMap<>();
	private Map<String, List<KeyMap>> keymaps = new HashMap<>();
	
	private JButton closeTabBtn;
	private final String AUTO_SAVED_PATH = "datas/auto";
	private JTextField rowLimitTxt = new JTextField("1000");
	
	private JComboBox<DBConnection> dbconnections = new JComboBox<>();
	
	public App() {
		instance = this;
		
		loadKeyMaps();
		loadConnectionFile();
		
		closeTabBtn = new JButton("x");
		closeTabBtn.setBorder(BorderFactory.createEmptyBorder(0,2,0,2));
		closeTabBtn.addActionListener(e -> requestCloseTabView(getActiveView()));
		
		newEditorBtn = new JButton("New Editor");
		newEditorBtn.addActionListener((e)-> newEditor());
		rowLimitTxt.setMaximumSize(new Dimension(100,25));
		rowLimitTxt.setMinimumSize(rowLimitTxt.getMaximumSize());
		rowLimitTxt.setPreferredSize(rowLimitTxt.getMinimumSize());
//		toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
		toolbar.add(newEditorBtn);
		toolbar.add(Box.createHorizontalGlue());
		toolbar.add(new JLabel("Row limit "));
		toolbar.add(rowLimitTxt);
		toolbar.add(new JLabel(" Connection "));
		dbconnections.setSize(dbconnections.getMinimumSize());
		toolbar.add(dbconnections);
		
		horizontalSplit = new JSplitPane();
		horizontalSplit.setRightComponent(viewsPane);
		
		viewsPane.addChangeListener(e -> onViewChange());
		
		restoreWorkspace();

		frame = new JFrame("EaSyQL");
		frame.add(toolbar, BorderLayout.NORTH);
		frame.add(horizontalSplit);

		frame.setLocationRelativeTo(null);
		frame.setSize(600, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.addWindowListener(new WindowAdapter () {
			public void windowClosing(WindowEvent e) {
				onClosedWindow();
			};
		});
		frame.setVisible(true);
	}
	
//	public DBConnection getSelectedConnectionManager() {
//		return (DBConnection) dbconnections.getSelectedItem();
//	}
	
	public int getRowLimit() {
		String text = rowLimitTxt.getText();
		try {
			return Integer.parseInt(text);
		} catch (Exception e) {
			return 1000;
		}
	}
	
	private void restoreWorkspace() {
		File file = new File("workspace");
		if (file.exists())
		try (java.io.ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file))) {
			Map<String, Bundle> bundles = (Map<String, Bundle>) stream.readObject();
			for (String key : bundles.keySet()) {
				View view = null;
				Bundle bundle = bundles.get(key);
				for (Constructor<?> con : Class.forName(bundle.getString("view-class")).getConstructors()) {
					if (con.getParameterCount()==0) {
						view = (View) con.newInstance();
						break;
					}
				}
				if (view != null) {
					view.restore(bundles.get(key));
					addView(view);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Map<String, List<KeyMap>> getKeymaps() {
		return keymaps;
	}
	
	public DBConnection getSelectedConnection() {
		return (DBConnection) dbconnections.getSelectedItem();
	}
	
	public void setConnectionSelected(String name) {
		int i = getConnectionIdx(name);
		if (i > -1)
			dbconnections.setSelectedIndex(i);
	}
	
	public int getConnectionIdx(String name) {
		for (int i = 0; i < dbconnections.getItemCount(); i++) {
			DBConnection c = dbconnections.getItemAt(i);
			if (c.getName().toLowerCase().equals(name.toLowerCase()))
				return i;
		}
		return -1;
	}
	
	public DBConnection getConnectionManager(String connectionName) {
		int i = getConnectionIdx(connectionName);
		return i==-1? null : dbconnections.getItemAt(i);
	}
	
	private void loadConnectionFile() {
		File path = new File("connections");
		if (!path.exists())
			return;
		Gson gson = new Gson();
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(path))) {
			JsonObject datas = gson.fromJson(reader, JsonObject.class);
			for (String key : datas.keySet()) {
				JsonObject cdata = datas.getAsJsonObject(key);
				Map<String, Object> d = new HashMap<>();
				for (String pro : cdata.keySet()) {
					JsonElement e = cdata.get(pro);
					if (pro.equalsIgnoreCase("properties") && e.isJsonObject()) {
						JsonObject props = e.getAsJsonObject();
						Map<String, String> dd = new HashMap<>();
						for (String kk : props.keySet()) {
							dd.put(kk, props.get(kk).getAsString());
						}
						d.put(pro, dd);
					} else {
						d.put(pro, e.getAsString());
					}
				}
				String connectionType = (String) d.get("connectionType");
				DBConnection con = null;
				if (connectionType!=null) {
					if (connectionType.equalsIgnoreCase("properties")) {
						con = new DBConnectionFromProperties(key, d);
					} else
						System.err.println("Invalid connection type");
				} else
					System.err.println("Invalid connection type null");
				if (con!=null)
					this.dbconnections.addItem(con);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void onClosedWindow() {
		// ask for save
		for (View c : views.values()) {
			if (c instanceof Editor) {
				Editor e = (Editor) c;
				if (e.isDirty()) {
					if (e.getPath()!=null) {
						// ask for save
						//e.save();
					}
				}
			}
		}
		// save
		Map<String, Bundle> bundles = new TreeMap<>();
		for (View c : views.values()) {
			Bundle bundle = c.getBundle();
			bundle.put("view-class", c.getClass().getName());
			bundles.put(c.getName(), bundle);
			c.close();
		}
		File file = new File("workspace");
		try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file))) {
			stream.writeObject(bundles);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("saving: " + views.size());
	}
	
	private void requestCloseTabView(View view) {
		if (view==null)
			throw new RuntimeException("tab null xd");
		if (view==getActiveView()) {
			if (closeTabBtn.getParent()!=null)
				closeTabBtn.getParent().remove(closeTabBtn);
			lastActiveTab = null;
		}
		viewsPane.removeTabAt(getViewIndex(view));
		views.remove(view.getView());
		view.close();
	}
	
	private SqlEditor lastActiveTab;
	
	private int getViewIndex(View con) {
		for (int i = 0; i < viewsPane.getTabCount(); i++)
			if (viewsPane.getComponentAt(i)==con.getView())
				return i;
		return -1;
	}
	
	private void onViewChange() {
		if (lastActiveTab!=null) {
			int i = getViewIndex(lastActiveTab);
			viewsPane.setTabComponentAt(i, null);
			viewsPane.setTitleAt(i, lastActiveTab.getName());
		}
		if (viewsPane.getSelectedIndex()!=-1) {
			JPanel tabWithCloseBtn = new JPanel();
			tabWithCloseBtn.setBorder(BorderFactory.createEmptyBorder());
			tabWithCloseBtn.setFocusable(false);
			tabWithCloseBtn.setOpaque(false);
			tabWithCloseBtn.add(new JLabel(getActiveView().getName()));
			tabWithCloseBtn.add(closeTabBtn);
			viewsPane.setTabComponentAt(viewsPane.getSelectedIndex(), tabWithCloseBtn);
			views.get(viewsPane.getSelectedComponent()).onViewActivated();
		}
	}
	
	private void newEditor() {
		String name = "Untitled";
		Map<String, Integer> names = new HashMap<>();
		for (JComponent com : views.keySet()) {
			names.put(views.get(com).getName(), 1);
		}
		int i = 1;
		while (names.containsKey(name)) {
			name = "Untitled " + i++;
		}
		SqlEditor con = new SqlEditor(name);
		addView(con);
	}
	
	private void addView(View view) {
		views.put(view.getView(), view);
		viewsPane.addTab(view.getName(), view.getView());
		viewsPane.setSelectedComponent(view.getView());
	}
	
	private void loadKeyMaps() {
		Gson gson = new Gson();
		try (InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/keymap"))) {
			final JsonObject maps = gson.fromJson(reader, JsonObject.class);
			maps.keySet().forEach(name -> {
				List<KeyMap> keys = new LinkedList<>();
				for (JsonElement kdat : maps.getAsJsonArray(name)) {
					JsonArray kdata = kdat.getAsJsonArray();
					KeyMap keymap = new KeyMap();
					keymap.setName(kdata.get(0).getAsString());
					keymap.setShortcut(kdata.get(1).getAsString());
					keys.add(keymap);
				}
				keymaps.put(name, keys);
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// register defaultActions
		for (KeyMap keymap : keymaps.get("editor")) {
			if (keymap.getName().equals("Execute")) {
				keymap.setListener((e) -> {
					SqlEditor con = (SqlEditor) getActiveView();
					con.executeSentenceKeyStroke();
				});
			}
		}
	}
	
	public View getActiveView() {
		return viewsPane.getSelectedIndex()==-1?null: views.get(viewsPane.getSelectedComponent());
	}

	
	public static void main(String[] args) {
		// esto de swing fix the error on parse text and call multiple setCharacterAttributes
		javax.swing.SwingUtilities.invokeLater(()-> {
			new App();
		});
	}

}
