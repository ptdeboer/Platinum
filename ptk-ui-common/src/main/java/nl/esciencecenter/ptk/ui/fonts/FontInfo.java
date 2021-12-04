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

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.RenderingHints.Key;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

/**
 * Simple Font Information holder class. FontInfo is used by the FontToolbar. Use createFont() to
 * instantiate a new Font object using the specified Font information.
 */
@Slf4j
public class FontInfo {

    // ========================================================================
    // Class Constants:
    // ========================================================================

    /**
     * Custom name
     */
    public static final String FONT_ALIAS = "fontAlias";

    /**
     * Most specific font type. Might be equal to "font family" or more specific.
     */
    public static final String FONT_TYPE = "fontType";

    /**
     * Less specific font type or font "family"
     */
    public static final String FONT_FAMILY = "fontFamily";

    /**
     * Italic,Bold,etc.
     */
    public static final String FONT_STYLE = "fontStyle";

    /**
     * Size in pixels
     */
    public static final String FONT_SIZE = "fontSize";

    /**
     * Java 1.6 and 1.7 Font RenderingHints
     */
    public static final String FONT_RENDERING_HINTS = "fontRenderingHints";

    public static final String[] fontPropertyNames = {FONT_ALIAS, FONT_FAMILY, FONT_STYLE,
            FONT_SIZE, FONT_FAMILY};

    // some default font types:
    public static final String FONT_ICON_LABEL = "iconlabel";

    public static final String FONT_MONO_SPACED = "monospaced";

    public static final String FONT_DIALOG = "dialog";

    public static final String FONT_TERMINAL = "terminal";

    // enable to auto-store create fonts in ~/.vletrc/fonts/<font-alias>.prop

    private static boolean autosave = false;

    /**
     * Font Style database
     */
    static Hashtable<String, FontInfo> fontStyles = null;

    // ========================================================================
    // Info
    // ========================================================================

    /**
     * Whether to store FontInfo in persistant Font DataBase
     */
    public static void setGlobalAutoSave(boolean value) {
        autosave = value;
    }

    // ========================================================================
    // Info
    // ========================================================================

    /**
     * Font Type or Font Family Name/
     */
    protected String fontFamily = "Monospaced";

    /**
     * Optional Alias for the GUI (dialog,terminal,label)
     */
    protected String fontAlias = null;

    /**
     * Font Size in pixels.
     */
    protected Integer fontSize = 13;

    /**
     * Font Style, 0=non, 0x01=bold, 0x02=italic, etc.
     *
     * @see java.awt.Font
     */
    protected Integer fontStyle = 0;

    /**
     * Optional Foreground color, can be NULL (= use default)
     */
    protected Color foreground = Color.BLACK;

    /**
     * Optional Background color, can be NULL (= use default)
     */
    protected Color background = Color.WHITE;

    /**
     * Optional Highlighted foreground color, can be NULL (= use default)
     */
    protected Color highlightedForeground = new Color(64, 64, 240);

    /**
     * For hierarchical Fonts: currently not used
     */
    protected FontInfo parent = null;

    protected Map<Key, Object> renderingHints = null;

    protected FontInfo() {
    }

    public FontInfo(Properties props) {
        this.setFontProperties(props);
        // backward compatibility: add alias name
        if (fontAlias == null)
            fontAlias = fontFamily;
    }

    public FontInfo(Font font) {
        init(font);
    }

    protected void init(Font font) {
        fontSize = font.getSize();
        fontStyle = font.getStyle();
        fontFamily = font.getFamily();
        // alias default to fontName
        fontAlias = fontFamily;
    }

    /**
     * @return Returns the fontSize.
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * @param size The fontSize to set.
     */
    public void setFontSize(int size) {
        // System.err.println("FontInfo.setFontSize="+size);
        this.fontSize = size;
    }

    /**
     * @return Returns the fontStyle.
     */
    public int getFontStyle() {
        return fontStyle;
    }

    /**
     * @param fontStyle The fontStyle to set.
     */
    public void setFontStyle(int fontStyle) {
        this.fontStyle = fontStyle;
    }

    /**
     * @return Returns the font family, for example "Monospaced" or "Arial"
     */
    public String getFontFamily() {
        return fontFamily;
    }

    /**
     * @param family Font Family name. For example "Monospaced" or "Arial".
     */
    public void setFontFamily(String family) {
        this.fontFamily = family;
    }

    /**
     * Create Font using this Font Information.
     *
     * @return
     */
    public Font createFont() {
        return new Font(fontFamily, fontStyle, fontSize);
    }

    public boolean isBold() {
        return (fontStyle & Font.BOLD) == Font.BOLD;
    }

    public boolean isItalic() {
        return (fontStyle & Font.ITALIC) == Font.ITALIC;
    }

    public void setBold(boolean val) {
        fontStyle = setFlag(fontStyle, Font.BOLD, val);
    }

    public void setItalic(boolean val) {
        fontStyle = setFlag(fontStyle, Font.ITALIC, val);
    }

    private int setFlag(int orgvalue, int flag, boolean val) {
        if (val == true) {
            orgvalue = orgvalue | flag;
        } else if ((orgvalue & flag) == flag) {
            orgvalue -= flag;
        }

        return orgvalue;
    }

    /**
     * Return font properties as Properties Set.
     *
     * @return Properties set with this font information.
     */
    public Properties getFontProperties() {
        Properties props = new Properties();

        if (fontAlias == null)
            fontAlias = fontFamily;

        props.put(FONT_ALIAS, fontAlias);
        props.put(FONT_FAMILY, fontFamily);
        props.put(FONT_SIZE, new Integer(fontSize).toString());
        props.put(FONT_STYLE, new Integer(fontStyle).toString());

        return props;
    }

    /**
     * Uses FONT properties and updates info
     */
    public void setFontProperties(Properties props) {
        String valstr = null;

        valstr = (String) props.get(FONT_ALIAS);

        if (valstr != null)
            this.fontAlias = valstr;

        // Old Type name => renamed to FAMILY
        valstr = (String) props.get(FONT_TYPE);

        if (valstr != null)
            setFontFamily(valstr);

        // new Correct 'family' i.s.o. generic 'type'
        valstr = (String) props.get(FONT_FAMILY);

        if (valstr != null)
            setFontFamily(valstr);

        valstr = (String) props.get(FONT_SIZE);

        if (valstr != null)
            setFontSize(Integer.valueOf(valstr));

        valstr = (String) props.get(FONT_STYLE);

        if (valstr != null)
            setFontStyle(Integer.valueOf(valstr));

        valstr = (String) props.get(FONT_RENDERING_HINTS);
    }

    private void store() {
        fontStyles.put(this.fontAlias, this);

        if (autosave == true) {
            // try
            // {
            // saveFontStyles();
            // }
            // catch (IOException e)
            // {
            // logger.warnPrintf("Could save default font info.\n");
            // //e.printStackTrace();
            // }
        }
    }

    /**
     * For selected text/icon label text
     */
    public Color getHighlightedForeground() {
        return this.highlightedForeground;
    }

    public Color getBackground() {
        return this.background;
    }

    public Color getForeground() {
        return this.foreground;
    }

    /**
     * Return explicit Rendering Hints for this font. These hints override the default Font
     * Rendering hints. Currently not implemented.
     */
    public Map<Key, ?> getRenderingHints() {
        return this.renderingHints;
    }

    /**
     * Explicit Set Anti Aliasing Rendering Hints to "On" or "Off". If useAA==null the settings will
     * be set to "Default".
     *
     * @param useAA - Anti Aliasing Rendering Hint: Use true to turn On, use false to run Off, null to
     *              set to "Default".
     */
    public void setAntiAliasing(Boolean useAA) {
        if (this.renderingHints == null) {
            renderingHints = new HashMap<>();
        }

        if (useAA == null) {
            renderingHints.put(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        } else if (useAA == true) {
            renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
    }

    /**
     * @return - true if AntiAliasing Rendering Hint has been set to "on".
     */
    public boolean hasAntiAliasing() {
        if (renderingHints != null) {
            if (renderingHints.get(RenderingHints.KEY_ANTIALIASING) != null) {
                return (renderingHints.get(RenderingHints.KEY_ANTIALIASING) == RenderingHints.VALUE_ANTIALIAS_ON);
            }
        }

        return true; // default
    }

    /**
     * Update font settings of specified Component with this font.
     */
    public void updateComponentFont(JComponent jcomp) {
        jcomp.setFont(createFont());
    }

    // ==============================================
    // Static FontInfo Factory
    // ==============================================

    /**
     *
     */
    public static FontInfo getFontInfo(String alias) {
        // autoinit

        if (fontStyles == null) {
            if (fontStyles == null)
                fontStyles = new Hashtable<String, FontInfo>();
        }

        FontInfo info = fontStyles.get(alias);

        if (info != null) {
            return info;
        }

        // current hardcoded ones:
        if (alias.compareToIgnoreCase(FONT_ICON_LABEL) == 0) {
            Font font = new Font("dialog", 0, 11);
            return store(font, FONT_ICON_LABEL);

        } else if (alias.compareToIgnoreCase(FONT_DIALOG) == 0) {
            Font font = new Font("dialog", 0, 11);
            return store(font, FONT_DIALOG);

        } else if (alias.compareToIgnoreCase(FONT_MONO_SPACED) == 0) {
            Font font = new Font("monospaced", 0, 12);
            return store(font, FONT_MONO_SPACED);
        } else if (alias.compareToIgnoreCase(FONT_TERMINAL) == 0) {
            Font font = new Font("monospaced", 0, 12);
            FontInfo newInfo = store(font, FONT_MONO_SPACED);
            newInfo.setAntiAliasing(true);
            return newInfo;
        }
        return null;
    }

    /**
     * Store FontInfo under (new) alias.
     *
     * @param font  java.awt.Font to store
     * @param alias reference name
     * @return
     */
    protected static FontInfo store(Font font, String alias) {
        FontInfo info = new FontInfo(font);
        info.fontAlias = alias;
        info.store();

        return info;
    }

    /**
     * Store FontInfo
     */
    public static void store(FontInfo info) {
        info.store();
    }

}
