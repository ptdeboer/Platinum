package nl.esciencecenter.ptk.ui.fonts;

import java.awt.*;

public class FontItem {

    private final String fontName;
    private final Font customFont;

    public FontItem(String fontName, int size) {
        this.fontName = fontName;
        this.customFont = new Font(fontName, 0, size);
    }

    public String getFontName() {
        return this.fontName;
    }

    public Font getCustomFont() {
        return this.customFont;
    }
}
