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

package nl.esciencecenter.ptk.ui.image;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.io.IOException;

import javax.swing.JComponent;

import nl.esciencecenter.ptk.util.logging.PLogger;


/** 
 * ImagePane which handles asynchronous imageUpdates. 
 * After an image is loaded the component size is updated to
 * match the new size. 
 * This because when updating an image from a remote resource, 
 * the size might not be known yet as long as the AWT toolkit is 
 * 'decoding;  the image bytes.
 *  
 */ 
public class ImagePane extends JComponent
{
    private static PLogger logger; 
    
    static
    {
        logger=PLogger.getLogger(ImagePane.class); 
    }
    
	// wait for an image to be updated 
	public static class ImageWaiter implements ImageObserver
	{
		Image image=null; 

		int newWidth=0; 
		int newHeight=0;

		boolean allBits=false; 
		boolean error=false; 
		
		public ImageWaiter(Image _image)
		{
			image=_image;
		}

		public void setImage(Image _image)
		{
			this.image=_image; 
			allBits=false; 
		}

		public synchronized boolean imageUpdate(Image img, int infoFlags, int x, int y,
				int width, int height)
		{
			logger.debugPrintf(">>> ImageWaiter: flags=%s\n",infoFlags); 

			
			if ((infoFlags & ImageObserver.ERROR)>0) 
			{
				this.error=true; 
				
				synchronized(this)
				{
					// wakeup ! 
					this.notifyAll();
				}

				return false; 
			}
			
			boolean done=false; 

			if (width>0) 
				newWidth=width; 

			if (height>0) 
				newHeight=height; 

			logger.debugPrintf(">>> ImageWaiter: update %d,%d => %d,%d \n",width,height,newWidth,newHeight);

			if (allBits==false)
				this.allBits=((infoFlags & ImageObserver.ALLBITS)>0); 

			if (allBits) 
			{
				if ((newWidth>0) && (newHeight>0)) 
				{
					synchronized(this)
					{
						// wakeup ! 
						this.notifyAll();
					}

					done=true; 
				}
			}

			// new update needed ? 
			return (done==false); 
		}

		/** waits for size to be known 
		 * @throws IOException */
		public void waitForCompletion(boolean waitForAllBits) throws IOException
		{
			//debug(">>> ImageWaiter: waitForCompletion()");

			boolean wait=true; 

			while(wait==true)
			{
				// get size and trigger imageUpdate ! 

				this.newHeight=image.getHeight(this); 
				this.newWidth=image.getWidth(this);
			
				if (error==true)
				{
					throw new IOException("Image Loading failed"); 
				}
				
				if ((newHeight<=0) || (newWidth<=0))
				{
					wait=true; 
				}
				else 
				{
					// size is known now, wait for all bits ? 
					if (waitForAllBits==false)
					{
						// don't wait 
						wait=false;
					}
					else
					{
						if (allBits==true)
							wait=false;
						else
							wait=true; 
					}
				}


				// image not done yet:
				if (wait)
				{
					try
					{
						synchronized(this)
						{
							// wait 1s. check again:
							this.wait(1000);
						}
					}
					catch (InterruptedException e)
					{
						logger.logException(PLogger.WARN,e,"waitForCompletion():Interrupted\n"); 
					}
				}

			}

			logger.debugPrintf("<<< ImageWaiter: waitForCompletion():DONE!\n");

		}
		
		
	} // imageWaiters 


	
	// needed by swing 
	private static final long serialVersionUID = 4745630397251453021L;

	// ===============================================================
	// Instance :
	// ================================================================
	
	/** The Image */ 
	private Image image=null;
	
	/** Default Background Color */ 
	Color bgcolor=Color.GRAY;
	
	/** Width of Image, might not be same as this component's width */ 
	private int imageWidth=-1;
	
	/** Height of Image, might not be same as this component's height*/ 
	private int imageHeight=-1;


	public ImagePane(Image source) throws IOException
	{
		init(); 
		this.setImage(source,false); 
	}

	public ImagePane()
	{
		init(); 
	}
	

	private void init()
	{
		
	}


	/**
	 * Set new image, if waitForCompletion==true, this method
	 * will only return when the whole image loaded and ready. 
	 * 
	 * @param bytes
	 * @param waitForCompletion Set to true to block and wait for the image 
	 *                          to be complete.
	 * @throws IOException 
	 */
	public void setImage(byte bytes[],boolean waitForCompletion) throws IOException
	{
		Toolkit tk = Toolkit.getDefaultToolkit();
		setImage(tk.createImage(bytes),waitForCompletion);
	}

	/**
	 * Set new image, if waitForCompletion==true, this method
	 * will only return when the whole image is ready. 
	 * 
	 * @param image Image to be drawn. 
	 * @param waitForCompletion Set to true to block and wait for the image 
	 *                          to be complete.
	 * @throws IOException 
	 */
	public void setImage(Image newImage, boolean waitForCompletion) throws IOException
	{
		if ((newImage!=null) && (waitForCompletion)) 
		{
				// Since createImage is asynchronous, the image size
				// might not be know. Wait until updateImage provides
				// the right image size. 
				// Specify waitForallBits==true if the complete image needs to be available. 
				// Set to false if only the correct size is needed. 
				
				logger.debugPrintf("Waiting for image completion...\n");
				
				ImageWaiter waiter=new ImageWaiter(newImage); 

				waiter.waitForCompletion(false); 
				
				logger.debugPrintf("Done: Waiting for image completion"); 
		}

		// image complete: swap: 
		// swap 
		// swap 
		
		if (this.image!=null)
		{
			image.flush(); 
			image=null; 
		}
		
		this.image=newImage; 
		
		if (this.image==null) 
			return; 
		
		int tmpHeight=image.getHeight(this); // might return -1!
		int tmpWidth=image.getWidth(this);  // might return -1!

		// be carefull due to ascynchronous nature of updateImage
		// height events COULD happen during this code:

		logger.debugPrintf("this height=%d\n",imageHeight); 
		logger.debugPrintf("this width=%d\n",imageWidth);
		logger.debugPrintf("new height=%d\n",tmpHeight); 
		logger.debugPrintf("new width=%d\n",tmpWidth);

		//
		// if size is not know yet, imageUpdate will do the trick 
		// 
		if ((tmpHeight>0) && (tmpWidth>0))
		{
			this.imageHeight=tmpHeight; 
			this.imageWidth=tmpWidth; 
			updateSize(imageWidth,imageHeight);
		}
	}

	@Override
	public void setSize(int w, int h)
	{
		// setSize is callend by parent container to set 
		// actual size of component. This is NOT the image size !  
		// do optional checking here: 
		logger.debugPrintf("setSize:%d,%d\n",w,h);
		// call component setSize
		// AutoResize => Update Image Size ! 
		super.setSize(w, h);
	}
	
	// return actual size of image 
	@Override
	public Dimension getPreferredSize()
	{
		if ((this.imageHeight<=0) || (this.imageWidth<=0))
			return super.getSize(); 
		
		return new Dimension(this.imageWidth,this.imageHeight);
	}
	
	/**
	 * Return size of actual image. This can differ from 
	 * current components size.
	 */
	public Dimension getImageSize()
	{
		return new Dimension(this.imageWidth,this.imageHeight); 
	}
	
	// returns Component size:  
	public Dimension getSize()
	{
	    return super.getSize(); 
	}
	public Dimension getMaximumSize()
	{
		if ((this.imageHeight<=0) || (this.imageWidth<=0))
			return super.getSize(); 

		return getPreferredSize(); 
	}
	
	/**
	 * Since this is a lightweight component we should handle 
	 * ImageObserver events ourselfs. 
	 * After an (background) update: perform the update during swing event thread
	 */ 
	protected void updateSize(final int newWidth,final int newHeight)
	{
		this.imageWidth=newWidth; 
		this.imageHeight=newHeight; 

		logger.debugPrintf("async update size="+newWidth+","+newHeight);
		// setSize(this.width,this.height);
		
		// methods go background already if not event thread !  
		revalidate(); 
		repaint(); // request repaint! 

		/* Not Needed for revalidate&repaint
		Runnable runT=new Runnable()
		{
			public void run()
			{
				//setSize(newWidth,newHeight);
				revalidate(); 
				repaint(); // request repaint! 
			}
		};

		SwingUtilities.invokeLater(runT);
		*/
	}


	// Interface: ImageObserver
	// 
	// Must handle imageUpdate events as image is 'processed'
	// in another thread. ! 

	public boolean imageUpdate(Image img,
			int infoFlags,
			int x,
			int y,
			int newWidth,
			int newHeight)
	{
		//Let super method to the work (usually a (re)paint):
		boolean val=super.imageUpdate(img,infoFlags,x,y,newWidth,newHeight); 
		// check 
		boolean updateSize=false;

		if ((infoFlags&ImageObserver.HEIGHT)>0)
		{
			if (newWidth>0)
			{
				this.imageWidth=newWidth; 
				logger.debugPrintf("ImagePane:NEW height=%d\n",imageHeight);
				updateSize=true;
			}
		}

		if ((infoFlags&ImageObserver.WIDTH)>0)
		{
			if (newHeight>0)
			{
				this.imageHeight=newHeight; 

				logger.debugPrintf("ImagePane:NEW width=%d\n",imageWidth);
				updateSize=true;
			}
		}

		//
		// don't check for ALL_BITS: drawing will be done in background
		// 
		if ((updateSize) && (this.imageWidth>0) && (this.imageHeight>0))
		{
			updateSize(this.imageWidth,this.imageHeight); 
		}

		return val;
	}

	/* Override update for speed ! 
    public void update(Graphics g)
    {
        paint(g); 
    }*/

	public void paint(Graphics g)
	{
		Image targetImage=this.image;

		// viewed imaged not ready ? 
		//if ( (targetImage==null) || (targetImage.getHeight(null)<=0) || (targetImage.getWidth(null)<=0))
		//	targetImage=this.orgImage;

		//if (targetImage==null)
		//	targetImage=this.orgImage;
		
		// Do NOT add X,Y Offset, parent container does this already: 
		{
		  int x=this.getLocation().x;
		  int y=this.getLocation().y;
		  logger.debugPrintf("paint(): offset x,y=%d,%d\n",x,y); 
		}
		
		g.drawImage(targetImage,0,0,bgcolor,this);
		//Dimension dim = this.getPreferredSize(); 

		//g.drawImage(image,x,y,dim.width,dim.height,bgcolor,this);
	}


	public void dispose()
	{
		if (this.image!=null)
		{
			this.image.flush(); 
			this.image=null;
		} 
	}

//	public void loadImage(VRL location, boolean wait) throws VlException
//	{
//	 	Image img = UIGlobal.getResourceLoader().getImage(location);
//		
//		if (img==null)
//			throw new IOException("Image loader returned NULL for:"+location); 
//		
//		setImage(img,wait);
//	}


	public Image getImage()
	{
		return this.image; 
	}
	
}
