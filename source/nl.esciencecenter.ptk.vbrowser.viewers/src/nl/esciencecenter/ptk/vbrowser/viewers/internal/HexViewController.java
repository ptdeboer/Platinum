/*
 * Copyright 2006-2010 Virtual Laboratory for e-Science (www.vl-e.nl)
 * Copyright 2012-2013 Netherlands eScience Center.
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

package nl.esciencecenter.ptk.vbrowser.viewers.internal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import nl.esciencecenter.ptk.ui.widgets.URIDropTargetLister;

public class HexViewController implements AdjustmentListener, KeyListener, ActionListener, URIDropTargetLister
{
    
	private HexViewer hexViewer;

	public HexViewController(HexViewer viewer) 
	{
		this.hexViewer=viewer; 
	}

	public void setContents(String txt)
    {
        try
        {
            hexViewer.setContents(txt.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            hexViewer.setContents(txt.getBytes()); 
        } 
    }

    public void handleDrop(URI uri)
    {
	    this.hexViewer.doUpdateURI(uri); 
    }

    public void adjustmentValueChanged(AdjustmentEvent e)
	{
		
		debug("adjustment event+"+e);
		debug(">>> new value="+e.getValue());
		int val=e.getValue(); 
		
		//val=val-(val%hexViewer.nrBytesPerLine); 
 		this.hexViewer.moveToOffset(val); 
 		hexViewer.redrawContents(); 
 		
 		if (true) 
 			return; 
 		
		
		long prev=hexViewer.getOffset();
		// round step to nrBytesPerLine 
		long diff=val-prev;
		long nrbpl=hexViewer.getNrBytesPerLine();
		
		debug("diff="+diff+", nrbpl="+nrbpl);
		// micro increments! make sure minimum is nr  bytes per line 
		
		if ((diff>-nrbpl) && (diff<nrbpl))
		{
			if (diff<0) 
				diff=-nrbpl;
			else if (diff>=0)
				diff=nrbpl;
			//else diff=0 
		}
		// minus remainder to make sure increment is whole multiplication of nrBytesPerLine
		if (diff>0) 
			diff=diff-(diff%nrbpl);
		else if (diff<0)
			diff=diff+(diff%nrbpl);
		
		debug("diff="+diff);
		
		//val=val-(val%hexViewer.nrBytesPerLine); 
 		this.hexViewer.moveToOffset(prev+diff); 
 		hexViewer.redrawContents(); 
	}

	public void keyTyped(KeyEvent e) 
	{		
	}
	
	public void keyPressed(KeyEvent e)
	{
		int kchar=e.getKeyChar(); 
		int kcode=e.getKeyCode();
		int mods=e.getModifiers(); 
		
		String kstr=KeyEvent.getKeyText(kcode);
		hexViewer.debug("kstr="+kstr);
		hexViewer.debug("kchar="+kchar);

		

		if ((mods & KeyEvent.CTRL_MASK) >0)
		{
			if (kstr.compareToIgnoreCase("B")==0)
			{
				hexViewer.setWordSize(1); 
				hexViewer.redrawContents(); 
			}
			// [T]oolbar  (CTRL-F) = find
			else if (kstr.compareToIgnoreCase("T")==0)
			{
				hexViewer.toggleFontToolBar(); 
			}
			else if (kstr.compareToIgnoreCase("B")==0)
			{
				hexViewer.setWordSize(1); 
				hexViewer.redrawContents(); 
			}
			else if (kstr.compareTo("1")>=0  && (kstr.compareTo("8")<=0)) 
			{
				hexViewer.setWordSize(new Integer(kstr)); 
				System.err.println("wordSize="+hexViewer.getWordSize());
				hexViewer.redrawContents(); 
			}
			if (kstr.compareToIgnoreCase("Right")==0)
			{
				hexViewer.setMinimumBytesPerLine(hexViewer.getMinimumBytesPerLine()
						+ hexViewer.getWordSize()); 
				
				hexViewer.redrawContents(); 
			}
			else if (kstr.compareToIgnoreCase("Left")==0)
			{
				hexViewer.setMinimumBytesPerLine(hexViewer.getMinimumBytesPerLine()
						- hexViewer.getWordSize()); 
							
				hexViewer.redrawContents(); 
			}
			
		}
		else if (kstr.compareToIgnoreCase("Page Down")==0)
		{
			hexViewer.addOffset(hexViewer.nrBytesPerView);			
			hexViewer.redrawContents(); 
		}
		else if (kstr.compareToIgnoreCase("Page Up")==0)
		{
			hexViewer.addOffset(-hexViewer.nrBytesPerView);			
			hexViewer.redrawContents(); 
		}
		else if (kstr.compareToIgnoreCase("Right")==0)
		{
			hexViewer.addOffset(1); 			
			hexViewer.redrawContents(); 
		}
		else if (kstr.compareToIgnoreCase("Left")==0)
		{
			hexViewer.addOffset(-1);			
			hexViewer.redrawContents(); 
		}
		else if (kstr.compareToIgnoreCase("Up")==0)
		{
			hexViewer.addOffset(-hexViewer.nrBytesPerLine);  			
			hexViewer.redrawContents(); 
		}
		else if (kstr.compareToIgnoreCase("Down")==0)
		{
			hexViewer.addOffset(hexViewer.nrBytesPerLine);  			
			hexViewer.redrawContents(); 
		}
	}
	
	public void keyReleased(KeyEvent e) 
	{
	}
	
	void debug(String msg) 
	{
	    hexViewer.debugPrintf("%s\n",msg); 
	}

	public void actionPerformed(ActionEvent e) 
	{
		Object source=e.getSource(); 
		
		if (source==this.hexViewer.offsetField)
		{
			String txt=this.hexViewer.offsetField.getText();
			hexViewer.moveToOffset(Long.decode(txt));
			hexViewer.redrawContents();
		}
	}

    @Override
    public void notifyUriDrop(List<URI> uriList)
    {
        if (uriList.size()>0)
        {
            hexViewer.doUpdateURI(uriList.get(0));
        }
    }
	
}
