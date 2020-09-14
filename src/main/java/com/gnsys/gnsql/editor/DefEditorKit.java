package com.gnsys.gnsql.editor;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TabSet;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class DefEditorKit extends StyledEditorKit {

	private static final long serialVersionUID = 1L;
	
	public int tabSize = 4;
	
	public void setTabSize(int size) {
		this.tabSize = size;
	}

	public ViewFactory getViewFactory() {
		return new MyViewFactory();
	}

	class MyViewFactory implements ViewFactory {

		public View create(Element elem) {
			String kind = elem.getName();
			if (kind!=null) {
				if (kind.equals(AbstractDocument.ContentElementName)) {
					return new LabelView(elem);
				} else if (kind.equals(AbstractDocument.ParagraphElementName)) {
					return new CustomTabParagraphView(elem);
				} else if (kind.equals(AbstractDocument.SectionElementName)) {
					return new BoxView(elem, View.Y_AXIS);
				} else if (kind.equals(StyleConstants.ComponentElementName)) {
					return new ComponentView(elem);
				} else if (kind.equals(StyleConstants.IconElementName)) {
					return new IconView(elem);
				}
			}

			return new LabelView(elem);
		}
	}

	class CustomTabParagraphView extends ParagraphView {

		public CustomTabParagraphView(Element elem) {
			super(elem);
		}

		public float nextTabStop(float x, int tabOffset) {
//			TabSet tabs = getTabSet();
//			if (tabs==null) {
				// a tab every 72 pixels.
				return (float) x+tabSize;
//			}

//			return super.nextTabStop(x, tabOffset);
		}

	}
}