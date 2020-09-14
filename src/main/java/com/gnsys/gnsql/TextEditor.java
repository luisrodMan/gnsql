package com.gnsys.gnsql;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import com.gnsys.gnsql.editor.DefEditorKit;
import com.gnsys.gnsql.editor.NumberingView;

public class TextEditor extends AbstractEditor {

	private String path;
	private JTextPane editor;
	private NumberingView nums;
	private JScrollPane container;
	
	public TextEditor() {
		this(null);
	}
	
	public TextEditor(String name) {
		super(name);
		
		editor = new JTextPane();
		editor.setFont(new Font("Courier New Regular", Font.PLAIN, 15));
		DefEditorKit ekit = new DefEditorKit();
		ekit.setTabSize((int)editor.getFontMetrics(editor.getFont()).getStringBounds("    ", editor.getGraphics()).getWidth());
		editor.setEditorKit(ekit);
		editor.setStyledDocument(new XD(editor));
		getEditor().setForeground(Color.lightGray);
		getEditor().setBackground(Color.DARK_GRAY);
		getEditor().setSelectionColor(Color.orange);
		getEditor().setCaretColor(Color.white);
		
		container = new JScrollPane(editor);
//		nums = new NumberingView();
//		nums.setPreferredSize(new Dimension(30, 30));
//		nums.setFont(editor.getFont());
//		nums.setBackground(editor.getBackground());
//		nums.setForeground(editor.getForeground());
//		container.setRowHeaderView(nums);
		
//		editor.getDocument().addDocumentListener(new DocumentListener() {
//			@Override
//			public void removeUpdate(DocumentEvent arg0) {
//				int rowCount = editor.getDocument().getDefaultRootElement().getElementCount();
//				if (nums.getLineCount()!=rowCount) {
//					nums.setLineNumberCount(rowCount);
//				}
//			}
//			@Override
//			public void insertUpdate(DocumentEvent arg0) {
//				int rowCount = editor.getDocument().getDefaultRootElement().getElementCount();
//				if (nums.getLineCount()!=rowCount) {
//					nums.setLineNumberCount(rowCount);
//				}
//			}
//			@Override
//			public void changedUpdate(DocumentEvent arg0) {
//				
//			}
//		});
		
		final UndoManager undo = new UndoManager();
		   Document doc = editor.getDocument();
		    
		   // Listen for undo and redo events
		   doc.addUndoableEditListener(new UndoableEditListener() {
		       public void undoableEditHappened(UndoableEditEvent evt) {
		           undo.addEdit(evt.getEdit());
		       }
		   });
		    
		   // Create an undo action and add it to the text component
		   editor.getActionMap().put("Undo",
		       new AbstractAction("Undo") {
		           public void actionPerformed(ActionEvent evt) {
		               try {
		                   if (undo.canUndo()) {
		                       undo.undo();
		                   }
		               } catch (CannotUndoException e) {
		               }
		           }
		      });
		    
		   // Bind the undo action to ctl-Z
		   editor.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
		    
		   // Create a redo action and add it to the text component
		   editor.getActionMap().put("Redo",
		       new AbstractAction("Redo") {
		           public void actionPerformed(ActionEvent evt) {
		               try {
		                   if (undo.canRedo()) {
		                       undo.redo();
		                   }
		               } catch (CannotRedoException e) {
		               }
		           }
		       });
		    
	   // Bind the redo action to ctl-Y
	   editor.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
		
		// load keymaps
		for (KeyMap keymap : App.getInstance().getKeymaps().get("editor")) {
			if (keymap.getListener()!=null) {
				editor.getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke(keymap.getShortcut()), new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						keymap.getListener().actionPerformed(null);
					}
				});
			}
		}
		
		editor.getInputMap().put(KeyStroke.getKeyStroke("ctrl D"), new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int caret = editor.getCaretPosition();
				Document doc = editor.getDocument();
				int line = doc.getDefaultRootElement().getElementIndex(caret);
				System.out.println("line: " + line);
				int start = doc.getDefaultRootElement().getElement(line).getStartOffset();
				int end = doc.getDefaultRootElement().getElement(line).getEndOffset();
				try {
					String text = doc.getText(start, end-start);
					System.out.println("text["+text+"]");
					if (line==doc.getDefaultRootElement().getElementCount()-1) {
						if (line>0)
							start -= System.lineSeparator().length();
						end -= System.lineSeparator().length();
					}
					if (start!=end)
						doc.remove(start, end-start);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		editor.getInputMap().put(KeyStroke.getKeyStroke("ctrl alt DOWN"), new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("down xd");
				int caret = editor.getCaretPosition();
				Document doc = editor.getDocument();
				int line = doc.getDefaultRootElement().getElementIndex(caret);
				System.out.println("line: " + line);
				int start = doc.getDefaultRootElement().getElement(line).getStartOffset();
				int end = doc.getDefaultRootElement().getElement(line).getEndOffset();
				System.out.println("start: " + start + " " + end);
				try {
					String text = doc.getText(start, end-start);
					doc.insertString(end, text, null);
					editor.setSelectionStart(end);
					editor.setSelectionEnd(end+(end-start)-System.lineSeparator().length());
					System.out.println("text["+text+"]");
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	public JTextPane getEditor() {
		return editor;
	}
	
	public String getText() {
		String t = editor.getText();
		return t==null?"":t;
	}
	
	public void save() {
		
	}
	
	public void saveAs() {
		
	}
	
	public boolean isDirty() {
		return true;
	}

	public String getPath() {
		return path;
	}

	@Override
	public JComponent getView() {
		return container;
	}

	@Override
	public void close() {
		
	}
	
	private class XD extends DefaultStyledDocument {
		
		private String[] keywords = {"a","abort","abs","absolute","access","action","ada","add","admin","after","aggregate","alias","all","allocate","also","alter","always","analyse","analyze","and","any","are","array","as","asc","asensitive","assertion","assignment","asymmetric","at","atomic","attribute","attributes","audit","authorization","auto_increment","avg","avg_row_length","backup","backward","before","begin","bernoulli","between","bigint","binary","bit","bit_length","bitvar","blob","bool","boolean","both","breadth","break","browse","bulk","by","c","cache","call","called","cardinality","cascade","cascaded","case","cast","catalog","catalog_name","ceil","ceiling","chain","change","char","char_length","character","character_length","character_set_catalog","character_set_name","character_set_schema","characteristics","characters","check","checked","checkpoint","checksum","class","class_origin","clob","close","cluster","clustered","coalesce","cobol","collate","collation","collation_catalog","collation_name","collation_schema","collect","column","column_name","columns","command_function","command_function_code","comment","commit","committed","completion","compress","compute","condition","condition_number","connect","connection","connection_name","constraint","constraint_catalog","constraint_name","constraint_schema","constraints","constructor","contains","containstable","continue","conversion","convert","copy","corr","corresponding","count","covar_pop","covar_samp","create","createdb","createrole","createuser","cross","csv","cube","cume_dist","current","current_date","current_default_transform_group","current_path","current_role","current_time","current_timestamp","current_transform_group_for_type","current_user","cursor","cursor_name","cycle","data","database","databases","date","datetime","datetime_interval_code","datetime_interval_precision","day","day_hour","day_microsecond","day_minute","day_second","dayofmonth","dayofweek","dayofyear","dbcc","deallocate","dec","decimal","declare","default","defaults","deferrable","deferred","defined","definer","degree","delay_key_write","delayed","delete","delimiter","delimiters","dense_rank","deny","depth","deref","derived","desc","describe","descriptor","destroy","destructor","deterministic","diagnostics","dictionary","disable","disconnect","disk","dispatch","distinct","distinctrow","distributed","div","do","domain","double","drop","dual","dummy","dump","dynamic","dynamic_function","dynamic_function_code","each","element","else","elseif","enable","enclosed","encoding","encrypted","end","end-exec","enum","equals","errlvl","escape","escaped","every","except","exception","exclude","excluding","exclusive","exec","execute","existing","exists","exit","exp","explain","external","extract","false","fetch","fields","file","fillfactor","filter","final","first","float","float4","float8","floor","flush","following","for","force","foreign","fortran","forward","found","free","freetext","freetexttable","freeze","from","full","fulltext","function","fusion","g","general","generated","get","global","go","goto","grant","granted","grants","greatest","group","grouping","handler","having","header","heap","hierarchy","high_priority","hold","holdlock","host","hosts","hour","hour_microsecond","hour_minute","hour_second","identified","identity","identity_insert","identitycol","if","ignore","ilike","immediate","immutable","implementation","implicit","in","include","including","increment","index","indicator","infile","infix","inherit","inherits","initial","initialize","initially","inner","inout","input","insensitive","insert","insert_id","instance","instantiable","instead","int","int1","int2","int3","int4","int8","integer","intersect","intersection","interval","into","invoker","is","isam","isnull","isolation","iterate","join","k","key","key_member","key_type","keys","kill","lancompiler","language","large","last","last_insert_id","lateral","leading","least","leave","left","length","less","level","like","limit","lineno","lines","listen","ln","load","local","localtime","localtimestamp","location","locator","lock","login","logs","long","longblob","longtext","loop","low_priority","lower","m","map","match","matched","max","max_rows","maxextents","maxvalue","mediumblob","mediumint","mediumtext","member","merge","message_length","message_octet_length","message_text","method","middleint","min","min_rows","minus","minute","minute_microsecond","minute_second","minvalue","mlslabel","mod","mode","modifies","modify","module","month","monthname","more","move","multiset","mumps","myisam","name","names","national","natural","nchar","nclob","nesting","new","next","no","no_write_to_binlog","noaudit","nocheck","nocompress","nocreatedb","nocreaterole","nocreateuser","noinherit","nologin","nonclustered","none","normalize","normalized","nosuperuser","not","nothing","notify","notnull","nowait","null","nullable","nullif","nulls","number","numeric","object","octet_length","octets","of","off","offline","offset","offsets","oids","old","on","online","only","open","opendatasource","openquery","openrowset","openxml","operation","operator","optimize","option","optionally","options","or","order","ordering","ordinality","others","out","outer","outfile","output","over","overlaps","overlay","overriding","owner","pack_keys","pad","parameter","parameter_mode","parameter_name","parameter_ordinal_position","parameter_specific_catalog","parameter_specific_name","parameter_specific_schema","parameters","partial","partition","pascal","password","path","pctfree","percent","percent_rank","percentile_cont","percentile_disc","placing","plan","pli","position","postfix","power","preceding","precision","prefix","preorder","prepare","prepared","preserve","primary","print","prior","privileges","proc","procedural","procedure","process","processlist","public","purge","quote","raid0","raiserror","range","rank","raw","read","reads","readtext","real","recheck","reconfigure","recursive","ref","references","referencing","regexp","regr_avgx","regr_avgy","regr_count","regr_intercept","regr_r2","regr_slope","regr_sxx","regr_sxy","regr_syy","reindex","relative","release","reload","rename","repeat","repeatable","replace","replication","require","reset","resignal","resource","restart","restore","restrict","result","return","returned_cardinality","returned_length","returned_octet_length","returned_sqlstate","returns","revoke","right","rlike","role","rollback","rollup","routine","routine_catalog","routine_name","routine_schema","row","row_count","row_number","rowcount","rowguidcol","rowid","rownum","rows","rule","save","savepoint","scale","schema","schema_name","schemas","scope","scope_catalog","scope_name","scope_schema","scroll","search","second","second_microsecond","section","security","select","self","sensitive","separator","sequence","serializable","server_name","session","session_user","set","setof","sets","setuser","share","show","shutdown","signal","similar","simple","size","smallint","some","soname","source","space","spatial","specific","specific_name","specifictype","sql","sql_big_result","sql_big_selects","sql_big_tables","sql_calc_found_rows","sql_log_off","sql_log_update","sql_low_priority_updates","sql_select_limit","sql_small_result","sql_warnings","sqlca","sqlcode","sqlerror","sqlexception","sqlstate","sqlwarning","sqrt","ssl","stable","start","starting","state","statement","static","statistics","status","stddev_pop","stddev_samp","stdin","stdout","storage","straight_join","strict","string","structure","style","subclass_origin","sublist","submultiset","substring","successful","sum","superuser","symmetric","synonym","sysdate","sysid","system","system_user","table","table_name","tables","tablesample","tablespace","temp","template","temporary","terminate","terminated","text","textsize","than","then","ties","time","timestamp","timezone_hour","timezone_minute","tinyblob","tinyint","tinytext","to","toast","top","top_level_count","trailing","tran","transaction","transaction_active","transactions_committed","transactions_rolled_back","transform","transforms","translate","translation","treat","trigger","trigger_catalog","trigger_name","trigger_schema","trim","true","truncate","trusted","tsequal","type","uescape","uid","unbounded","uncommitted","under","undo","unencrypted","union","unique","unknown","unlisten","unlock","unnamed","unnest","unsigned","until","update","updatetext","upper","usage","use","user","user_defined_type_catalog","user_defined_type_code","user_defined_type_name","user_defined_type_schema","using","utc_date","utc_time","utc_timestamp","vacuum","valid","validate","validator","value","values","var_pop","var_samp","varbinary","varchar","varchar2","varcharacter","variable","variables","varying","verbose","view","volatile","waitfor","when","whenever","where","while","width_bucket","window","with","within","without","work","write","writetext","x509","xor","year","year_month","zerofill","zone"};
		Map<String, Integer> keywordsMap = new HashMap<>();
		Pattern p = Pattern.compile("([a-zA-Z_]+[0-9]*|[0-9]+|'[^']*'?|\"[^\"]*\"?|--.*?\n)");
		
		SimpleAttributeSet defaultStyle = new SimpleAttributeSet();
		SimpleAttributeSet stringStyle = new SimpleAttributeSet();
		SimpleAttributeSet keywordStyle = new SimpleAttributeSet();
		SimpleAttributeSet numberStyle = new SimpleAttributeSet();
		SimpleAttributeSet commentStyle = new SimpleAttributeSet();

		public XD(JTextPane editor) {
			StyleConstants.setForeground(stringStyle, Color.green);
			StyleConstants.setForeground(keywordStyle, Color.orange);
			StyleConstants.setForeground(numberStyle, Color.WHITE);
			StyleConstants.setForeground(commentStyle, Color.GRAY);
			if (keywordsMap.isEmpty()) {
				for (String keyword: keywords)
					keywordsMap.put(keyword.toLowerCase(), 1);
			}
		}
		
		@Override
		public void insertString(int arg0, String arg1, AttributeSet arg2) throws BadLocationException {
			super.insertString(arg0, arg1, arg2);
			updateStyles();
		}
		boolean f = false;
		private void updateStyles() {
			
			// ok los attributos se recorren solo asi que agregar attibutos nuevos solo para los fragmentos
//			agregados o modificados xd
//			if (f)
//				return;
//			f = true;
			try {
				StyleConstants.setForeground(defaultStyle, editor.getForeground());
				Matcher m = p.matcher(editor.getDocument().getText(0, editor.getDocument().getLength()));
				javax.swing.SwingUtilities.invokeLater(()-> {
					int l = 0;
					try {
						while (m.find()) {
							if (m.start()>l)
								editor.getStyledDocument().setCharacterAttributes(l, m.start()-l, defaultStyle, true);
							char c = editor.getStyledDocument().getText(m.start(), 1).charAt(0);
							if (c=='\''||c=='"') 
								editor.getStyledDocument().setCharacterAttributes(m.start(), m.end()-m.start(), stringStyle, true);
							else if (keywordsMap.get(m.group().toLowerCase())!=null) 
								editor.getStyledDocument().setCharacterAttributes(m.start(), m.end()-m.start(), keywordStyle, true);
							else if (Character.isDigit(c))
								editor.getStyledDocument().setCharacterAttributes(m.start(), m.end()-m.start(), numberStyle, true);
							else if (c=='-' && (m.end()-m.start())>1)
								editor.getStyledDocument().setCharacterAttributes(m.start(), m.end()-m.start(), commentStyle, true);
							else
								editor.getStyledDocument().setCharacterAttributes(m.start(), m.end()-m.start(), defaultStyle, true);
							l = m.end();
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
					if (l < editor.getDocument().getLength()) {
						editor.getStyledDocument().setCharacterAttributes(l, editor.getDocument().getLength()-l, defaultStyle, true);
					}
//					System.out.println("fin");
				});
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		
	}

}
