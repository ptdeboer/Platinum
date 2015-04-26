/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.ptk.ui.charpane;

import java.awt.Color;

/**
 * Interface to a Character Terminal. 
 * 
 * @author P.T. de Boer
 */
public interface ICharacterTerminal
{
	// VT100 CharSets: 
	public final static String VT_CHARSET_US = "CHARSET_US"; 
	
	public final static String VT_CHARSET_UK = "CHARSET_UK";
	
	public final static String VT_CHARSET_GRAPHICS = "CHARSET_GRAPHICS"; 

	int getRowCount();
  
	int getColumnCount();

	void setCursor(int x, int y);

	int getCursorY();
  
	int getCursorX();
 
	/** Move cursor with specified offset. Wraparound or scroll might occur*/ 
	void moveCursor(int deltax, int deltay); 
	
	///** 
	// * Print text at current position, might wrap around if getWrapAround()==true. 
	// * Auto scrolls if getAutoScroll()==true
	// * @param text
	// */
	//void writeString(String text); 

	///** Print string at specfied position. Text might wraparound or autoscroll */ 
	//void putString(String text,int x, int y); 

	/** 
	 * Write char at current position, might wrap around if getWrapAround()==true. 
	 * Auto scrolls if getAutoScroll()==true
	 */
	void writeChar(byte bytes[]); 
	
	/** Put utf-8 character sequence, redrawing might occur later */
	void putChar(byte[] bytes, int x, int y);

	/** 
	 * Write char at current position and move cursor to the right. 
	 * Might wrap around if getWrapAround()==true. 
	 * Auto scrolls if getAutoScroll()==true
	 */
	void writeChar(int charVal); 

	/** Put char at specified position. Doesn't do autoscroll or wraparound */ 
	void putChar(int charVal,int x,int y); 

	void move(int startX, int startY, int width, int height, int toX, int toY);

	/** Clear text buffer(s), does not reset graphics */ 
	void clearText();

	void clearArea(int x1, int y1, int x2, int y2);

	/** Reset graphics, internal state and clear text buffers */ 
	void reset();
	    
	void beep();

	/** Default foreground color */
	void setForeground(Color color);
	
	/** Default background color */ 
	void setBackground(Color color); 
  
	Color getForeground();
  
	Color getBackground();
  
	// Cursor Font Colors&Style. See Font 
	void setDrawStyle(int style);

	void addDrawStyle(int style); // logical OR of current draw style and argument
  
	/** Current used draw style */ 
	int getDrawStyle();
  
	/**
	 * Set styled color number from color map. If style==0 then no color 
	 * from the styled colormap is used */ 
	void setDrawBackground(int nr);
  
	/**
	 * Set styled color number from color map. If style==0 then no color 
	 * from the styled colormap is used */ 
	void setDrawForeground(int nr);
  
	/**
	 * Color map for indexed color codes.
	 * If draw style==0 then default background/foreground will be used. */ 
	void setColorMap(ColorMap colorMap); 
	
	/** Scroll lines from startline to (exclusive) endline */ 
	void scrollRegion(int starline,int endline,int numlines, boolean scrollUp);

	/** Set line wrap around. */ 
	void setWrapAround(boolean value);
  
	/** Returns whether line wrap around is enabled */ 
	boolean getWrapAround(); 
	
	/** Whether autoscroll is enabled */ 
	boolean getAutoScroll(); 

	/** Enable auto scroll when writing beyond terminal last line */  
	void setAutoScroll(boolean autoScroll); 

	/** Switch to charset */  
	void setCharSet(int nr); 
  
	/** Set charset */ 
	void setCharSet(int i, String str);

	void setEnableCursor(boolean value);

	/** Set the nr of columns -> initiates a resize ! */
	void setColumns(int i);

	/** Swith to alternate text buffer. Returns false if not supported */
	boolean setAltScreenBuffer(boolean value);

	/** Synchronized scrolling */ 
	void setSlowScroll(boolean value);
  
}
