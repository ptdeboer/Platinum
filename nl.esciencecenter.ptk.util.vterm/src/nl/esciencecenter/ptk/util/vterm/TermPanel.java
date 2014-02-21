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

package nl.esciencecenter.ptk.util.vterm;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import nl.esciencecenter.ptk.ui.charpane.CharPane;
import nl.esciencecenter.ptk.ui.charpane.ColorMap;
import nl.esciencecenter.ptk.ui.charpane.ICharacterTerminal;
import nl.esciencecenter.ptk.ui.fonts.FontInfo;

/** 
 * TermPanel which contains and manages a CharPane 
 */
public class TermPanel extends JPanel implements ComponentListener
{
	private static final long serialVersionUID = -6433404098046770571L;

	static String  splashText[]={
		" ***   VLTerm VT100+ Emulator     *** ",
		"***  (C) VL-e consortium/Piter.NL  ***",
		" ***  Author Piter T. de Boer     *** ",
    };

	private CharPane charPane;    
	private EmulatorKeyMapper keyMapper;
	private Emulator emulator; 
	//private boolean mustStop=false;
	
	public void initGUI()
	{
		// JPanel 
		this.setBorder(new BevelBorder(BevelBorder.RAISED)); 
		this.enableEvents(AWTEvent.KEY_EVENT_MASK);
		 // unset focus, must get TAB chars: 
        this.setFocusTraversalKeysEnabled(false);
        this.setLayout(new BorderLayout()); 
		
        // charPane: 
		{
			charPane=new CharPane(); 
			this.add(charPane,BorderLayout.CENTER); 
		}
		
		this.setBackground(Color.BLACK);
		
		//Listeners: 
		{
			charPane.addComponentListener(this);
			this.addComponentListener(this); 
		}
		
		// this.addKeyListener(this); 
	}
	
	// ===
	// Component Listener 
	// ===

	public void componentHidden(ComponentEvent e)
	{
	}

	public void componentMoved(ComponentEvent e)
	{
		   
	}

	public void componentResized(ComponentEvent e)
	{
		// this component or parent has been resized/ 
		// the layout manager resize the charpane, so
		// now the internal text buffers have to be updated. 
		
		if (e.getSource().equals(charPane)==false)
			return; 
		
		// get actual component size which is not the text buffer image size ! 
		Dimension size = charPane.getSize();
		
		// update internal character buffer size: 
		charPane.resizeTextBuffersToAWTSize(size); 
	}
	
	public void componentShown(ComponentEvent e)
	{
	}
	
	// ===
	// TermPanel !
	// === 
	
	public int getColumnCount() 
	{
		return charPane.getColumnCount(); 
	}

	public int getRowCount() 
	{
		return charPane.getRowCount();
	}

	public FontInfo getFontInfo()
	{
		return charPane.getFontInfo(); 
	}

	/**
	 *  Set Emulator but do not start anything 
	 */ 
	public void setEmulator(Emulator emulator)
	{
		//this.mustStop=false; 
		this.emulator=emulator; 
		this.keyMapper=new EmulatorKeyMapper(emulator);
		this.addKeyListener(keyMapper); 
    }

	public void dispose()
	{
		terminate(); 
		
		if (charPane!=null)
			charPane.dispose(); 
		charPane=null;
		
		//this.mustStop=true; 
        this.keyMapper=null;  
        this.emulator=null; 
	}
	

	public void terminate()
	{
		inactivate();  
		
		if (emulator!=null)
		{
			emulator.signalTerminate();
	
			// unregister keymapper: 
			if (keyMapper!=null)
			{
			    this.removeKeyListener(keyMapper);
			}
		}
	}  
		 
	public void updateFontSize(Integer val, boolean resetGraphics)
	{
		this.charPane.setFontSize(val); 
		if (resetGraphics); 
			charPane.resetGraphics(); 
	}
	
	public void updateColorMap(ColorMap colorMap, boolean resetGraphics)
	{
		charPane.setColorMap(colorMap);
		if (resetGraphics); 
			charPane.resetGraphics(); 
	}
	

	public void repaintGraphics(boolean splash) 
	{
		charPane.resetGraphics(); 
	
		if (splash)
			this.drawSplash(); 
	}
	
	void drawSplash()
    {
       if (splashText!=null)
       {
           int offx=20;
           int offy=8;
           
           for (int y=0;y<splashText.length;y++)
           {
               String line=splashText[y]; 
               charPane.putString(line,offx,offy+y);  
           }
       }
       charPane.paintTextBuffer();
       charPane.repaint(); 
   }

	public void updateFontType(String type, boolean resetGraphics) 
	{
		charPane.setFontType(type);
		if (resetGraphics)		
			charPane.resetGraphics(); 
	}

	/** Returns Character Terminal */ 
	public ICharacterTerminal getCharacterTerminal() 
	{
		return this.charPane; 
	}
	

	public void drawTestScreen() 
	{
		charPane.drawTestScreen(); 
	}

//	 /** Override Component.processKeyEvent -> use custom KeyMapper */ 
//    private void processKeyEventNot(KeyEvent e)
//    {
//      	debug("Key!:"+e);
//
//    	// handle key event ! 
//    	if (e.getSource().equals(this)==false)
//    		return; 
//  
//    	// Redirector to Emulator Key Mapper 
//    	if (keyMapper==null) 
//    	{
//    		debug("*** No KeyMapper ***");
//    		return; //no keymapper present
//    	}
//    	// System.out.println(e);
//    	int id = e.getID();
//    	if (id == KeyEvent.KEY_PRESSED)
//    	{
//    		keyMapper.keyPressed(e);
//    	}	
//    	else if (id == KeyEvent.KEY_RELEASED)
//    	{ 
//    		keyMapper.keyReleased(e); 
//    	}
//    	else if (id == KeyEvent.KEY_TYPED)
//    	{
//    		keyMapper.keyTyped(e);/* keyTyped(e); */
//    	}
//    	
//    	// Check Non Terminal Code: 
//    	{
//    		boolean ctrl=(e.getModifiersEx()&KeyEvent.CTRL_DOWN_MASK)>0; 
//    		int keycode=e.getKeyCode(); 
//    	
//    		// CTRL PAGE UP/DOWN -> Scoll CharPane
//    		if (ctrl)
//    		{
//    			if (keycode==KeyEvent.VK_PAGE_UP)
//    			{
//    				this.charPane.pageUp(); 
//    			}
//    			else if (keycode==KeyEvent.VK_PAGE_DOWN)
//    			{
//    				this.charPane.pageDown(); 
//    			}
//    		}
//    	}
//    }
    
   
	public void clearAll() 
	{
		this.charPane.clear(); 
	}

    public boolean getSynchronizedScrolling()
    {
        String str=this.charPane.getOption(CharPane.OPTION_ALWAYS_SYNCHRONIZED_SCROLLING); 
        
        if (str!=null)
            return Boolean.parseBoolean(str);
        
        return false; 
    }
    
    public void setSynchronizedScrolling(boolean val)
    {
        charPane.setOption(CharPane.OPTION_ALWAYS_SYNCHRONIZED_SCROLLING,""+val); 
    }

    public void reset()
    {
          charPane.reset(); 
    }

	public void activate() 
	{
		charPane.startRenderers(); 
	}

	public void inactivate() 
	{
		charPane.stopRenderers(); 
	}

	public void resetGraphics() 
	{
		charPane.resetGraphics();
	}

    public Emulator getEmulator()
    {
        return this.emulator; 
    }

}
