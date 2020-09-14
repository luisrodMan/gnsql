package com.gnsys.gnsql.editor;

import java.awt.Graphics;
import java.awt.Panel;
import java.awt.Rectangle;

public class NumberingView extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	private int linesCount;
	
	public void setLineNumberCount(int count) {
		this.linesCount = count;
		repaint();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		int fontHeight = g.getFontMetrics().getHeight();
		Rectangle bounds = g.getClipBounds();
		int lines = bounds.height/fontHeight;
		if (linesCount < lines)
			lines = linesCount;
		lines = linesCount;
		int fline = bounds.y / fontHeight;
		int xx=linesCount;
		int xxx = 1;
		while (xx>10) {
			xxx++;
			xx /= 10;
		}
		String format = "%"+xxx+"d";
		
		for (int i = 0; i < lines; i++)
			g.drawString(String.format(format, fline+i), 0, fline+i*fontHeight);
		
//		System.out.println("clip: "+g.getClip());
//		System.out.println("clip2: "+g.getClipBounds());
	}

	public int getLineCount() {
		return linesCount;
	}
	
}
