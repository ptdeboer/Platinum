package nl.esciencecenter.ptk.ui.fonts;

import java.awt.*;

public class FontItem {

    private String fontName;
    private final Font customFont;

    public FontItem(String fontName) {
        this.fontName=fontName;
        this.customFont =  new Font(fontName, 0,16);
    }

    public String getFontName() {
        return this.fontName;
    }

    public Font getCustomFont() {
        return this.customFont;
    }
}
