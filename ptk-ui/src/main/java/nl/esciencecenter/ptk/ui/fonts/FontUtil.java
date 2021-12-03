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

package nl.esciencecenter.ptk.ui.fonts;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class FontUtil {
    /**
     * Create Font Info from Font, copying font attributes like style and name.
     */
    public static FontInfo createFontInfo(Font font) {
        FontInfo info = new FontInfo();
        info.init(font);
        return info;
    }

    /**
     * Get FontInfo from Font Info data base.
     */
    public static FontInfo getFontInfo(String name) {
        return FontInfo.getFontInfo(name);
    }

    /**
     * Check's FontInfo alias database, if not Font.getFont(name) is returned;
     *
     * @param name
     * @return either java's default font or font from font database.
     */
    public static Font createFont(String name) {
        FontInfo info = FontInfo.getFontInfo(name);

        if (info != null)
            return info.createFont();

        return Font.getFont(name);
    }

    // breadcrump to update renderingHints:
    public static boolean updateRenderingHints(JComponent comp, Map<?, ?> renderingHints) {
        if (renderingHints == null) {
            // clear hints ?
            return false;
        }
        Graphics graphics = comp.getGraphics();

        if (graphics instanceof Graphics2D) {
            ((Graphics2D) graphics).addRenderingHints(renderingHints);
            return true;
        }

        return false;
    }

}
