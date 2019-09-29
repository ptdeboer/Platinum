package nl.piter.vterm.ui.fonts;

public class FontConst {

    /**
     * Logical name, used a key in font database.
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
     * Italic,Bold,Underlined, etc.
     */
    public static final String FONT_STYLE = "fontStyle";

    /**
     * Size in screen pixels.
     */
    public static final String FONT_SIZE = "fontSize";

    /**
     * Java 1.6 and 1.7 Font RenderingHints
     */
    public static final String FONT_RENDERING_HINTS = "fontRenderingHints";

    public static final String[] fontPropertyNames = {FONT_ALIAS, FONT_FAMILY, FONT_STYLE,
            FONT_SIZE, FONT_FAMILY};

    // ---
    // Some default font types:
    // ---

    public static final String FONT_ICON_LABEL = "iconlabel";

    public static final String FONT_MONO_SPACED = "monospaced";

    public static final String FONT_DIALOG = "dialog";

    public static final String FONT_TERMINAL = "terminal";

}
