///*
// * Copyrighted 2012-2013 Netherlands eScience Center.
// *
// * Licensed under the Apache License, Version 2.0 (the "License").  
// * You may not use this file except in compliance with the License. 
// * For details, see the LICENCE.txt file location in the root directory of this 
// * distribution or obtain the Apache License at the following location: 
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software 
// * distributed under the License is distributed on an "AS IS" BASIS, 
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// * See the License for the specific language governing permissions and 
// * limitations under the License.
// * 
// * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
// * ---
// */
//// source: 
//
//package nl.esciencecenter.ptk.ui.util;
//
//import java.awt.Color;
//import java.awt.Image;
//import java.awt.image.BufferedImage;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.MalformedURLException;
//import java.net.URI;
//import java.net.URL;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Vector;
//
//import javax.imageio.ImageIO;
//import javax.imageio.stream.ImageInputStream;
//import javax.swing.Icon;
//import javax.swing.ImageIcon;
//
//import nl.esciencecenter.ptk.ui.icons.ImageRenderer;
//import nl.esciencecenter.ptk.util.ResourceLoader;
//import nl.esciencecenter.ptk.util.logging.ClassLogger;
//
//
///**
// * ResourceLoader for UI specific resources. 
// */
//public class UIResourceLoader extends ResourceLoader
//{
//    private static UIResourceLoader instance;
//
//    private static ClassLogger logger;
//    
//    // =================================================================
//    // Static methods 
//    // ================================================================= 
//    
//    static
//    {
//        logger = ClassLogger.getLogger(UIResourceLoader.class);
//        //logger.setLevelToDebug();
//    }
//	
//	public static UIResourceLoader getDefault()
//	{
//		if (instance==null)
//			instance=new UIResourceLoader(null);
//		
//		return instance; 
//	}
//
//
//    // =================================================================
//    // Object methods 
//    // =================================================================
//
//    public UIResourceLoader()
//    {
//    }
//    
//    public UIResourceLoader(URL urls[]) 
//    {
//        super(urls); 
//    }
//
//    /**
//     * Returns image as icon. Does not cache result. 
//     * Use IconProvider to create icons.  
//     * @throws IOException 
//     * @throws MalformedURLException 
//     */
//    public ImageIcon getIcon(URL url) throws MalformedURLException, IOException
//    {
//        return new ImageIcon(getImage(url)); 
//    }
//    
//    /**
//     * Returns image as icon. Does not cache result. 
//     * Use IconProvider to create icons.  
//     * @throws IOException 
//     * @throws MalformedURLException 
//     */
//    public ImageIcon getIcon(String iconUrl) throws IOException
//    {
//        return new ImageIcon(getImage(iconUrl));  
//    }
//    
//    /** 
//     * Load (a)synchronously an image specified by URI.
//     *  
// 	 * @throws IOException 
//     */
//    public Image getImage(URI location) throws IOException
//    {
//        return getImage(location.toURL()); 
//    }
//
//    /**
//     * Find image and return it. 
//     * Resolves URL string to absolute URL.
//     * @throws IOException if url is invalid 
//     */
//    public Image getImage(String url) throws IOException
//    {
//        URL resolvedURL=this.resolveUrl(null,url);
//        
//        if (resolvedURL==null)
//            throw new IOException("Couldn't resolve url:"+url); 
//        
//        return this.getImage(resolvedURL);  
//    }
//
//    /**
//     * Find image and return it. 
//     * @throws IOException if url is invalid 
//     */
//    public Image getImage(URL url) throws IOException
//    {
//        if (url==null)
//            return null; 
//        
//        // .ico support ! 
//      if (isIco(url.toString())) 
//      {
//          logger.debugPrintf("getImage(): loading .ico image:%s\n",url);
//          return getIcoImage(url); 
//      }
//      
//      try
//      {
//            Image image;
//            image = ImageIO.read(url);
//            return image;
//      }
//      catch (IOException e)
//      {
//            throw new IOException("Failed to read image:"+url,e); 
//      } 
//    } 
//    
//    // ========================================================================
//    // Image/Icon methods
//    // ========================================================================
//   
//	/** Read PROPRIATIARY: .ico file and return Icon Image */ 
//    public BufferedImage getIcoImage(URL iconurl) throws IOException
//    {
//        try
//        {
//            InputStream inps=createInputStream(iconurl);  
//            ImageInputStream in = ImageIO.createImageInputStream(inps);
//
//            nl.ikarus.nxt.priv.imageio.icoreader.obj.ICOFile f;
//            f = new  nl.ikarus.nxt.priv.imageio.icoreader.obj.ICOFile(in);
//
//            // iterate over bitmaps: 
//            Iterator<?> it = f.getEntryIterator();
//
//            Vector<BufferedImage> bitmaps=new Vector<BufferedImage>();
//            
//            BufferedImage biggestImage=null; 
//            int biggestSize=0; 
//            
//            while(it.hasNext()) 
//            {
//                nl.ikarus.nxt.priv.imageio.icoreader.obj.IconEntry ie = (nl.ikarus.nxt.priv.imageio.icoreader.obj.IconEntry) it.next();
//
//                try
//                {
//                    BufferedImage img = ie.getBitmap().getImage();
//                    bitmaps.add(img); 
//                    int size= img.getWidth()*img.getHeight(); 
//                    if (size>biggestSize)
//                    {
//                        biggestSize=size; 
//                        biggestImage=img; 
//                    }
//                            //System.err.println(" - width="+img.getWidth());  
//                    //System.err.println(" - height="+img.getHeight());  
//
//                }
//                catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
//            }
//
//            return  biggestImage;  
//            
//        }
//        catch (IOException e)
//        {
//            throw new IOException("Read error:"+iconurl,e); 
//        }
//    }
//
//    public Icon getMiniBrokenImageIcon()
//    {
//        // create image on the fly:
//        String imageStr=
//                 ".x............x.\n"
//                +"xRx..........xRx\n"
//                +"xRRx........xRRx\n"
//                +".xRRx......xRRx.\n"
//                +"..xRRx....xRRx..\n"
//                +"...xRRx..xRRx...\n"
//                +"....xRRxxRRx....\n"
//                +".....xRRRRx.....\n"
//                +".....xRRRRx.....\n"
//                +"....xRRxxRRx....\n"
//                +"...xRRx..xRRx...\n"
//                +"..xRRx....xRRx..\n" 
//                +".xRRx......xRRx.\n" 
//                +"xRRx........xRRx\n" 
//                +"xRx..........xRx\n" 
//                +".x............x.\n"; 
//        
//        Map<String,java.awt.Color> colorMap=new HashMap<String,java.awt.Color>();
//        colorMap.put(".", Color.WHITE);
//        colorMap.put("R",Color.RED); 
//        colorMap.put("x",Color.BLACK); 
//        Image image=new ImageRenderer(null).createImage(imageStr,colorMap,Color.WHITE,' '); 
//        return new ImageIcon(image);  
//    }
//   
//
//}
