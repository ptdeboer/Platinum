/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.api;

import nl.piter.vterm.ui.charpane.ColorMap;

import java.awt.*;

/**
 * Interface to a Character Terminal.
 * <p>
 * OutputStream
 */
public interface CharacterTerminal {

    // VT100 CharSets:
    String VT_CHARSET_US = "CHARSET_US";

    String VT_CHARSET_UK = "CHARSET_UK";

    String VT_CHARSET_GRAPHICS = "CHARSET_GRAPHICS";

    int getRowCount();

    int getColumnCount();

    void setCursor(int x, int y);

    int getCursorY();

    int getCursorX();

    /**
     * Move cursor with specified offset. Wraparound or scroll might occur
     */
    void moveCursor(int deltax, int deltay);


    /**
     * Write char at current position, might wrap around if getWrapAround()==true. Auto scrolls if
     * getAutoScroll()==true
     */
    void writeChar(byte[] bytes);

    /**
     * Put utf-8 character sequence, redrawing might occur later
     */
    void putChar(byte[] bytes, int x, int y);

    /**
     * Write char at current position and move cursor to the right. Might wrap around if
     * getWrapAround()==true. Auto scrolls if getAutoScroll()==true
     */
    void writeChar(int charVal);

    /**
     * Put char at specified position. Doesn't do autoscroll or wraparound
     */
    void putChar(int charVal, int x, int y);

    void move(int startX, int startY, int width, int height, int toX, int toY);

    /**
     * Clear text buffer(s), does not reset graphics
     */
    void clearText();

    void clearArea(int x1, int y1, int x2, int y2);

    /**
     * Reset graphics, internal state and clear text buffers
     */
    void reset();

    void beep();

    Color getForeground();

    /**
     * Default foreground color
     */
    void setForeground(Color color);

    Color getBackground();

    /**
     * Default background color
     */
    void setBackground(Color color);

    void addDrawStyle(int style); // logical OR of current draw style and argument

    /**
     * Current used draw style
     */
    int getDrawStyle();

    // Cursor Font Colors&Style. See Font
    void setDrawStyle(int style);

    /**
     * Set styled color number from color map. If style==0 then no color from the styled colormap is
     * used
     */
    void setDrawBackground(int nr);

    /**
     * Set styled color number from color map. If style==0 then no color from the styled colormap is
     * used
     */
    void setDrawForeground(int nr);

    /**
     * Color map for indexed color codes. If draw style==0 then default background/foreground will
     * be used.
     */
    void setColorMap(ColorMap colorMap);

    /**
     * Scroll lines from startline(inclusive) to endline (exclusive)
     */
    void scrollRegion(int starline, int endline, int numlines, boolean scrollUp);

    /**
     * @returns whether line wrap around is enabled.
     */
    boolean getWrapAround();

    /**
     * Set line wrap around.
     */
    void setWrapAround(boolean value);

    /**
     * @return whether autoscroll is enabled
     */
    boolean getAutoScroll();

    /**
     * Enable auto scroll when writing beyond terminal last line
     */
    void setAutoScroll(boolean autoScroll);

    /**
     * Switch to charset
     */
    void setCharSet(int nr);

    /**
     * Set charset
     */
    void setCharSet(int i, String str);

    void setEnableCursor(boolean value);

    /**
     * Set the nr of columns -> initiates a resize !
     */
    void setColumns(int i);

    /**
     * Swith to alternate text buffer. Returns false if not supported
     */
    boolean setAltScreenBuffer(boolean value);

    /**
     * Synchronized scrolling
     */
    void setSlowScroll(boolean value);

    void setCursorOptions(boolean blink);

}
