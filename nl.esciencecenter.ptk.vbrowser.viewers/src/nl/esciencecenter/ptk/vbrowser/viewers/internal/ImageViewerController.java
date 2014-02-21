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

package nl.esciencecenter.ptk.vbrowser.viewers.internal;

import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JViewport;

public class ImageViewerController implements ComponentListener
{
	// simple mouse and key navigator: 
	public class KeyMouseMapper implements KeyListener, MouseMotionListener, MouseWheelListener, MouseListener
	{
		private Point dragStart;
		private Point dragOffsetStart;

		public KeyMouseMapper(ImageViewerController imageController)
		{
		}

		public void keyPressed(KeyEvent e)
		{
		}

		public void keyReleased(KeyEvent e)
		{
		}

		public void keyTyped(KeyEvent e)
		{
		}

		public void mouseDragged(MouseEvent e)
		{
			//Debug("drag:"+e); 
			//if (e.getSource()!=imageViewer.getImagePane()) 
			//	return; 
			
			Point newP=e.getPoint();
			
			// get.getLocationOnScreen(); 
			//Debug("drag p:"+e.getPoint()); 

			if (this.dragStart==null)
				return; 

			// diff between starting point 
			int dx=newP.x-this.dragStart.x; 
			int dy=newP.y-this.dragStart.y;
			
			int offsetx=dragOffsetStart.x-dx;  
			int offsety=dragOffsetStart.y-dy;

			if (offsetx<0) 
				offsetx=0;

			if (offsety<0) 
				offsety=0; 

			imageViewer.setViewPosition(offsetx,offsety); 
		}

		public void mouseMoved(MouseEvent e)
		{

		}

		public void mouseWheelMoved(MouseWheelEvent e)
		{
			boolean ctrl= ((e.getModifiersEx() & MouseWheelEvent.CTRL_DOWN_MASK)>0); 

			int rotClicks=e.getWheelRotation();
			int nrClicks=e.getClickCount();

			// nr of clicks doesn't return direction 
			if ((rotClicks<0) && (nrClicks>0))  	
				nrClicks=-nrClicks; 

			if (ctrl)
			{
				if (nrClicks<0) 
					imageViewer.zoomIn();

				if (nrClicks>0) 
					imageViewer.zoomOut();
			}

			//Debug("Wheel clicks = "+nrClicks+(ctrl?" +CTRL":"")); 

		}

	
		public void mouseClicked(MouseEvent e)
		{
		    imageViewer.errorPrintf("MouseClicked:%s\n",e); 
		    
//			if (GuiSettings.isAltMouseButton(e))
//			{
//				imageViewer.reset();
//				return; 
//			}
			
			
			// reset or switch to custom settings 
			if (e.getClickCount()==2)
			{
				imageViewer.toggleFitToScreen(); 
			}
		}

		public void mouseEntered(MouseEvent e)
		{
		}

		public void mouseExited(MouseEvent e)
		{
		}

		public void mousePressed(MouseEvent e)
		{
			this.dragStart=e.getPoint(); 
			// keep current offset: 
			this.dragOffsetStart=imageViewer.getViewPosition(); 
			 
		} 

		public void mouseReleased(MouseEvent e)
		{
			this.dragStart=null; 
		}		
	}
	
	// ===
	//
	// ===
	
	private KeyMouseMapper keyMouseMapper;
	private ImageViewer imageViewer;

	
	public ImageViewerController(ImageViewer imageVwr)
	{
		this.imageViewer=imageVwr; 
    	this.keyMouseMapper=new KeyMouseMapper(this);
    	
    	//ImagePane pane = imageViewer.getImagePane(); 
    	JViewport viewPort = imageViewer.scrollPane.getViewport(); 
    	
    	viewPort.addKeyListener(keyMouseMapper); 
    	viewPort.addMouseMotionListener(keyMouseMapper);
    	viewPort.addMouseWheelListener(keyMouseMapper); 
    	viewPort.addMouseListener(keyMouseMapper);

    	// observer both scrollPane and ViewPort: 
    	imageViewer.scrollPane.addComponentListener(this); 
    	viewPort.addComponentListener(this);
	}

	//
	// ImagePane Component Observer 
	//

	public void componentHidden(ComponentEvent e)
	{
	}

	public void componentMoved(ComponentEvent e)
	{
	}

	public void componentResized(ComponentEvent e)
	{
		//
		// After zoom/image update, size is set 
		// Check Image size ! 
		// 
//		debug("ImagePane image size  = "+imageViewer.imagePane.getImageSize());
//		debug("ImagePane comp  size  = "+imageViewer.imagePane.getSize());
//		debug("scrollPane size       = "+imageViewer.scrollPane.getSize());
//		// part which is actually shown 
//
//		debug("ViewPort size         = "+imageViewer.scrollPane.getViewport().getSize()); 
//		debug("ViewPort position     = "+imageViewer.scrollPane.getViewport().getViewPosition());

		if (e.getSource()==imageViewer.scrollPane)
		{
			// rezoom 
			if (imageViewer.fitToScreen==true)
				imageViewer.doZoom(); 
		}
		
		//scrollPane.revalidate(); 
		
		/*if (e.getSource()==this.imagePane)
		{
			Debug("ImagePane resized:"+imagePane.getSize());
			Debug("this.getSize():"+this.getSize());
			this.revalidate(); 
			 
			Container parent=this.getParent(); 
			
			if (parent instanceof JScrollPane)
			{
				((JScrollPane) parent).revalidate(); 
			}
			
		}*/
	}
	

	public void componentShown(ComponentEvent e) 
	{
	}
	
}
