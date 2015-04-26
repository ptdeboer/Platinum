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

package nl.esciencecenter.ptk.vbrowser.ui.browser;

import java.awt.Color;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.ptk.ui.icons.ImageRenderer;

/** 
 * XPM like bitmaps for icons. 
 */
public class MiniIcons
{

    public static String tabDeleteMiniColors[][] =
        {
            { ".", "#000000" },
            { "X", "#ff0000" }
        };
    

    public static String tabDeleteMiniIcon =
              "...........\n"
            + ".XX.....XX.\n"
            + "..XX...XX..\n"
            + "...XX.XX...\n"
            + "....XXX....\n"
            + "...XX.XX...\n"
            + "..XX...XX..\n"
            + ".XX.....XX.\n"
            + "...........\n";
            
    public static String tabAddMiniIcon=
            ".........\n"
           +"....X....\n"
           +"....X....\n"
           +"....X....\n"
           +".XXXXXXX.\n"
           +"....X....\n"
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
        Map<String, Color> colormap = new HashMap<String, Color>();
        colormap.put(".", new Color(0, 0, 0, 0));
        colormap.put("X", new Color(255, 0, 0, 255));
        colormap.put("x", new Color(255, 0, 0, 128));

        Image image = new ImageRenderer(null).createImage(tabDeleteMiniIcon, colormap, Color.BLACK, '.');
        return image;
    }

    public static Image getTabAddImage()
    {
        Map<String, Color> colormap = new HashMap<String, Color>();
        colormap.put(".", new Color(0, 0, 0, 0));
        colormap.put("X", new Color(0, 0, 0, 255));
        colormap.put("x", new Color(0, 0, 0, 128));

        Image image = new ImageRenderer(null).createImage(tabAddMiniIcon, colormap, Color.BLACK, '.');
        return image;
    }

    public static Image getMiniQuestionmark()
    {
        Map<String, Color> colormap = new HashMap<String, Color>();
        colormap.put(".", new Color(0, 0, 0, 0));
        colormap.put("X", new Color(0, 0, 0, 255));
        colormap.put("x", new Color(0, 0, 0, 128));

        Image image = new ImageRenderer(null).createImage(tabMiniQuestionMark, colormap, Color.BLACK, '.');
        return image;
    }

}
