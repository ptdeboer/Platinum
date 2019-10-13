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

import java.awt.*;
import java.awt.image.BufferedImage;

//import com.sun.media.jai.codec.ByteArraySeekableStream;
//import com.sun.media.jai.codec.ImageDecodeParam;
//import com.sun.media.jai.codec.SeekableStream;
//import com.sun.media.jai.codecimpl.GIFImageDecoder;

public class ImageUtil {

    public static class ImageError extends Error {
        public ImageError(String message) {
            super(message);
        }

        private static final long serialVersionUID = -554788643749026422L;
    }

    //    /**
    //     * Loads gif icon and decodes it using Jai classed.
    //     * Decodes the GIF image sequence but doesn't retain frame wait times! 
    //     * <p>  
    //     * This method does NOT do any caching.  
    //     */  
    //    public static ImageSequence jaiLoadAnimatedGif(URL url) throws Exception
    //    {
    //        // Use Rasters. 
    //        // 
    //        
    //        byte bytes[]=ResourceLoader.fsutil().getBytes(url.toString());
    //        debugPrintf("Read #%d bytes\n",bytes.length);
    //        
    //        SeekableStream sStream=new ByteArraySeekableStream(bytes,0,bytes.length); 
    //        
    //        //GifImageDecoder gifDec=null; 
    //        
    //        GIFImageDecoder gifDec = new GIFImageDecoder(sStream, null);
    //        
    //        // Get First:
    //        //Raster raster = gifDec.decodeAsRaster();
    //        // is null 
    //        //ImageDecodeParam param = gifDec.getParam();
    //        //debugPrintf("param=%s\n",param);
    //        
    //        // ===============================
    //        // Image 0: Decode first image: Must Exist!
    //        // ===============================
    //        int numPages=0; // gifDec.getNumPages();
    //        List<BufferedImage> images=new ArrayList<BufferedImage>(); 
    //        RenderedImage renImg = gifDec.decodeAsRenderedImage();
    //        ImageDecodeParam param = gifDec.getParam(); 
    //        
    //        debugPrintf(" - param= %s\n",param);
    //        
    //        // Can't determine nr of images, so just try to decode as much as possible 
    //        try
    //        {
    //            while(true)
    //            {
    //                // ================
    //                // Store Picture   
    //                // ================
    //                Raster data=renImg.getData(); 
    //                Rectangle b = data.getBounds();
    //                debugPrintf("--- raster #%d ---\n",numPages);
    //                debugPrintf("raster bounds    = %d,%d,%d,%d \n",b.x,b.y,b.width,b.height);
    //                
    //                String[] names = renImg.getPropertyNames();
    //                
    //                if ((names==null) || (names.length<=0)) 
    //                    debugPrintf(" - NULL properties \n");
    //                else
    //                    for (String name:names)
    //                    {
    //                        debugPrintf(" prop: '%10s'=%s\n",name,"");
    //                    }
    //                debugPrintf(" - renImg = %s\n",data);
    //                debugPrintf(" - data   = %s\n",data);
    //                 debugPrintf(" - parent = %s\n",data.getParent());
    //                
    //                ColorModel cm = renImg.getColorModel(); 
    //               
    //                BufferedImage bufImg=new BufferedImage(cm,(WritableRaster)data,false,null);
    //                images.add(bufImg);
    //                // ==============
    //                // Next -> throws exception is no more images 
    //                // ==============
    //                numPages++;
    //                renImg=gifDec.decodeAsRenderedImage(numPages);
    //                
    //                // ??? Where is the header information ? 
    //            }
    //        }
    //        catch (Exception e)
    //        {
    //            ;// exception -> no more left 
    //        }
    //        
    //        return new ImageSequence(images); 
    //    }

    //    /** 
    //     * Loads legacy Netscape 2.0 animated Gificon and decodes it which seems to be 
    //     * the animated gif standard nowdays! 
    //     * This method does NOT do any caching.  
    //     */  
    //    public static ImageSequence loadAnimatedGif(URL url) throws Exception
    //    {
    //        NS2GifDecoder gifDec = new NS2GifDecoder(); 
    //        
    //        gifDec.read(url);
    //        Dimension size = gifDec.getFrameSize(); 
    //        int num=gifDec.getFrameCount(); 
    //        
    //        int loopc=gifDec.getLoopCount(); 
    //        
    //        //debugPrintf(" --- header ---\n");
    //        //debugPrintf("  - num frames = %d\n",num);
    //        //debugPrintf("  - loop count = %dn",loopc);
    //        //debugPrintf("  - frame size = %dx%d\n",size.width,size.height);
    //    
    //        List<BufferedImage> images=new ArrayList<BufferedImage>(); 
    //        List<ImageSequence.FrameInfo> infos=new ArrayList<ImageSequence.FrameInfo>();
    //        // get frames; 
    //        for (int i=0;i<num;i++)
    //        {
    //            //debugPrintf(" --- frame #%d ---\n",i);
    //            
    //            BufferedImage frame = gifDec.getFrame(i);
    //            int delay=gifDec.getDelay(i); 
    //            //debugPrintf("  - delay = %d\n",delay);
    //            images.add(frame);
    //            // 1st = 1st image,etc. 
    //            //inf.imageNr=i;
    //            //inf.waitTimeMs=delay; 
    //    
    //            ImageSequence.FrameInfo inf=new ImageSequence.FrameInfo(i,delay);
    //            infos.add(inf); 
    //        }   
    //        
    //        ImageSequence anim=new ImageSequence(images,infos,loopc);
    //        return anim; 
    //    }

    public static BufferedImage convertToBufferedImage(Image image) {
        if (image instanceof BufferedImage)
            return (BufferedImage) image;

        int width = image.getWidth(null);
        int height = image.getHeight(null);

        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageGraphics = newImage.createGraphics();

        imageGraphics.drawImage(image, 0, 0, null);

        return newImage;
    }

}
