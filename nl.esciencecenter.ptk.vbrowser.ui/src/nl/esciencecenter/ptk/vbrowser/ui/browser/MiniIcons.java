package nl.esciencecenter.ptk.vbrowser.ui.browser;

import java.awt.Color;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import nl.esciencecenter.ptk.ui.icons.ImageRenderer;

public class MiniIcons
{

    public static String tabDeleteMiniColors[][]={ { ".","#000000"},{"X","#ff0000"}};

    public static String tabDeleteMiniIcon=
             "...........\n"
            +".XX.....XX.\n"
            +"..XX...XX..\n"
            +"...XX.XX...\n"
            +"....XXX....\n"
            +"...XX.XX...\n"
            +"..XX...XX..\n"
            +".XX.....XX.\n"
            +"...........\n";
            
    public static String tabAddMiniIcon=
            ".........\n"
           +"....X....\n"
           +"....X....\n"
           +"....X....\n"
           +".XXXXXXX.\n"
           +"....X.....\n"
           +"....X....\n"
           +"....X....\n"
           +".........\n";
 
    public static String tabMiniQuestionMark =
            ".........\n"
           +"...XXXX..\n"
           +"..XX..XX.\n"
           +"......XX.\n"
           +".....XX..\n"
           +"....XX...\n"
           +"....XX...\n"
           +".........\n"
           +"....XX...\n";
    
    public static Image getTabDeleteImage()
    {
        Map<String, Color> colormap=new HashMap<String, Color>(); 
        colormap.put(".",new Color(0,0,0,0)); 
        colormap.put("X",new Color(255,0,0,255)); 
        colormap.put("x",new Color(255,0,0,128)); 

        Image image = new ImageRenderer(null).createImage(tabDeleteMiniIcon,colormap,Color.BLACK,'.');
        return image;
    }

    public static Image getTabAddImage()
    {
        Map<String, Color> colormap=new HashMap<String, Color>(); 
        colormap.put(".",new Color(0,0,0,0)); 
        colormap.put("X",new Color(0,0,0,255)); 
        colormap.put("x",new Color(0,0,0,128)); 

        Image image = new ImageRenderer(null).createImage(tabAddMiniIcon,colormap,Color.BLACK,'.');
        return image;
    }

    public static Image getMiniQuestionmark()
    {
        Map<String, Color> colormap=new HashMap<String, Color>(); 
        colormap.put(".",new Color(0,0,0,0)); 
        colormap.put("X",new Color(0,0,0,255)); 
        colormap.put("x",new Color(0,0,0,128)); 

        Image image = new ImageRenderer(null).createImage(tabMiniQuestionMark,colormap,Color.BLACK,'.');
        return image;
    }


}
