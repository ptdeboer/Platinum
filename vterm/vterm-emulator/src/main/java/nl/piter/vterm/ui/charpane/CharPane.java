/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.ui.charpane;

import lombok.extern.slf4j.Slf4j;
import nl.piter.vterm.api.CharacterTerminal;
import nl.piter.vterm.ui.fonts.FontConst;
import nl.piter.vterm.ui.fonts.FontInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Character Terminal Render Engine. Developed by Piter.NL !
 */
@Slf4j
public class CharPane extends JComponent implements CharacterTerminal, ActionListener {

    public static final String OPTION_ALWAYS_SYNCHRONIZED_SCROLLING = "optionAlwaysSynchronizedScrolling";
    public static final int MAX_CHARSETS = 16;

    // Graphics Set:Reverse engineered from xterm codes:
    // echo <ESC>")0"<CTRL-N>"abcdefghijklmnopqrstuvwxyz"<CTRL-O>
    // Note, java uses 16-bit chars, following strings are utf-8 (!)

    public final static String graphOrg = "abcdefghijklmnopqrstuvwxyz";
    public final static String graphSet1 = "▒␉␌␍␊°±␤␋┘┐┌└┼⎺⎻─⎼⎽├┤┴┬│≤≥";

    // ========================================================================
    // Classes
    // ========================================================================
    // Color colorMap[]=colorMapWhite;

    private ColorMap colorMap = ColorMap.COLOR_MAP_GREEN_ON_BLACK;
    private FontInfo fontInfo = null;

    // ========================================================================
    // Instance
    // ========================================================================
    int nr_columns = 80;

    // ColorMap colorMap=ColorMap.COLOR_MAP_WHITE;
    int nr_rows = 24;
    int line_space = 0; // pixels
    String characterEncoding = "UTF-8";
    String[] charSets = new String[MAX_CHARSETS];
    int charSet = 0;
    boolean showCursor = true;
    int animationCounter = 0;

    private boolean wraparound = true;
    private boolean autoscroll = true;
    private boolean mustStop = false;

    // ====
    // Image, Terminals variables
    // ===

    private int cursorX = 0;
    private int cursorY = 0;

    // === Image Character Buffer ==== //
    private Image background; // background image
    private Image currentImage;
    private TextBuffer currentBuffer = null;
    private TextBuffer altTextBuffer;
    private TextBuffer fullBuffer;
    // === Font Metrics ===
    private java.awt.Font fontPlain;
    private int fontDescent;
    private int fontCharWidth;
    private int fontCharHeight;
    private Font fontBold;
    private Font fontItalic;
    private Font fontItalicBold;
    // === Curent Draw Style ===
    private int drawStyle = 0;
    private int drawForegroundIndex = -1; // no index -> use default !
    private int drawBackgroundIndex = -1;
    private Timer refreshTimer;
    private Runnable renderTask;
    private Thread renderThread;

    /**
     * Paint mutex: can also be use to wait for a repaint()
     */
    private final Object paintImageMutex = new Object();

    // ===============================
    // === MUTEX and Paint Control ===
    // ===============================
    /**
     * Render mutex do not resize or swap text buffers between rendering attempts
     */
    private final Object textBufferMutex = new Object();
    /**
     * Whether whole text buffer should be painted or only the characters which have changed
     */
    private boolean paintCompleteTextBuffer = false;
    /**
     * Whether after each scroll the scrolling should wait until the image is displayed
     */
    private boolean optionAlwaysSynchronizedScrolling = false;

    // ===============================
    // Text Rendering options
    // ===============================
    /**
     * Whether textBuffer Renderer should wait for the Swing paint thread
     */
    private final boolean optionRendererWaitForPaint = true;
    /**
     * Whether to use own graphics charset renderer instead of the default fonts graphics
     */
    private final boolean optionUseOwnGraphicsCharsetRenderer = true;
    /**
     * If not null always use this font for graphic charsets
     */
    private final String optionFixedGraphicsCharset = "Monospaced";
    /**
     * VI demands this
     */
    private final boolean optionSupportAltScreenBuffer = true;
    /**
     * set to false: Delta character renderer seems to work now
     */
    private final boolean optionAlwaysPaintCompleteTextBuffer = false;
    private Map<Key, ?> renderingHints;
    private boolean slowScroll;

    // ========================================================================
    // Object
    // ========================================================================

    public CharPane() {
        init();
    }

    public CharPane(int columns, int rows) {
        this.nr_columns = columns;
        this.nr_rows = rows;
        init();
    }

    private void init() {
        this.setLayout(null);
        this.setFocusable(true);
        // unset focuskeys, must get TAB chars:
        this.setFocusTraversalKeysEnabled(false);

        // startRefresher();
        charSets[0] = CharSets.CHARSET_US;
        charSets[1] = CharSets.CHARSET_GRAPHICS;

        // Move to parent container ! : enableEvents(AWTEvent.KEY_EVENT_MASK);
        this.setBackground(Color.BLACK);
        this.setForeground(Color.GREEN);

        // initialize font metrics:
        fontInfo = FontInfo.getFontInfo(FontConst.FONT_TERMINAL);
        initFont(fontInfo);

        this._resizeTextBuffers(this.nr_columns, this.nr_rows);
    }

    /**
     * Start timers and renderer thread is not running
     */
    public void startRenderers() {
        log.debug(">>> STARTED <<<");

        this.mustStop = false;

        if (renderTask == null) {
            // background render thread: must not use Swing Event Thread !
            renderTask = new Runnable() {
                public void run() {
                    doRender();
                }
            };
        }

        if ((renderThread == null) || (renderThread.isAlive() == false)) {
            renderThread = new Thread(renderTask);
            renderThread.start();
        }

        if ((refreshTimer == null) || (refreshTimer.isRunning() == false)) {
            // timer
            refreshTimer = new Timer(10, this);
            refreshTimer.setInitialDelay(100); // animate 10 per second (blink)
            refreshTimer.start();
        }
    }

    private void doRender() {
        while (this.mustStop == false) {
            synchronized (this.textBufferMutex) {
                if (this.currentBuffer != null) {
                    // check whether complete buffer need to be render or a part:
                    if (this.paintCompleteTextBuffer || this.currentBuffer.hasChanged()) {
                        // change *before* repaint
                        // to detect new changes while painting
                        currentBuffer.setChanged(false);

                        boolean succesfull = false;
                        try {
                            // at startup image might not be displayed yet
                            succesfull = paintTextBuffer();
                        } catch (Throwable e) {
                            // log.
                            succesfull = false;
                        }

                        // schedule repaint:
                        if (succesfull == false)
                            this.currentBuffer.setChanged(true);

                        // call repaint anyway
                        repaint();
                    }
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                log.debug("***Interrupted***", e);
            }

        }
        log.debug(">>> EXIT DO RENDER <<<");
    }

    /**
     * Initialize Backing Image. Since the parent is used to as image source, it must be diplayable,
     * so this method fails when the parent isn't visible. !
     *
     * @return
     */
    private boolean initTextBufferImage() {
        // block paint: will be back for breakfast:

        synchronized (paintImageMutex) {
            // image = new BufferedImage(getTermWidth(), getTermHeight(),BufferedImage.TYPE_INT_ARGB);
            Container parent = this.getParent();

            if ((parent == null) || (parent.isDisplayable() == false)) {
                log.warn("Parent NULL or Image source not displayable (yet).");
                return false;
            }

            if (parent.isVisible() == false) {
                log.warn("Image source not Visible (yet)!");
                return false;
            }

            // use VLTerm Frame as image source !
            // by mapping image textures into the parent frame the image
            // native resolution is used which spead of drawing!
            currentImage = parent.createImage(getImageWidth(), getImageHeight());
            // use buffered image
            // Graphics graphics = image.getGraphics();
        }

        return true;
    }

    public void drawTestScreen() {
        this.setDrawStyle(0);

        for (int i = 0; i < 2; i++)
            for (int y = 0; y < 4; y++)
                for (int x = 0; x < 8; x++) {
                    this.setDrawForeground(x);
                    this.setDrawBackground(y + i * 4);
                    putString("VTx", 16 + (x + i * 8) * 3, 1 + y);
                }

        int n = 64; //
        int m = 13;
        for (int c = 0; c < n; c++) {
            this.setDrawStyle(c);
            for (int i = 0; i < m; i++) {
                int index = c * m + i;
                int x = 1 + index % 78; // 6x13=78
                int y = 9 + index / 78;
                putChar('A' + index % 26, x, y);
            }
        }

        this.setDrawStyle(0);

        for (int y = 0; y < 4; y++)
            for (int x = 0; x < 64; x++)
                putChar(x + y * 64, 8 + x, 20 + y);

        // graphics
        this.setCharSet(1, VT_CHARSET_GRAPHICS);
        this.setCharSet(1);
        for (int y = 0; y < 2; y++) {
            if (y % 2 == 1)
                this.addDrawStyle(StyleChar.STYLE_BOLD);

            for (int x = 0; x < 16; x++) {
                int c = 'j' + x;
                if (x == 15)
                    c = 'a';// checkered
                putChar(c, 8 + x, 20 + y * 2);
            }
        }

        this.setCursor(0, 0);
        this.setDrawStyle(0);
        this.setCharSet(0);

        //
        this.paintTextBuffer();
        repaint();
    }

    /**
     * Initialize font&font metrics
     */
    private void initFont(FontInfo finfo) {
        String fontType = finfo.getFontFamily();
        int fontStyle = finfo.getFontStyle();
        int fontSize = finfo.getFontSize();

        // dummy image for font metrics:
        Image dummyImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

        fontPlain = finfo.createFont();
        fontBold = new Font(fontType, Font.BOLD, fontSize);
        fontItalic = new Font(fontType, Font.ITALIC, fontSize);
        fontItalicBold = new Font(fontType, Font.BOLD | Font.ITALIC, fontSize);

        Graphics graphics = dummyImage.getGraphics();
        graphics.setFont(fontPlain);
        graphics.dispose();
        FontMetrics metrics = graphics.getFontMetrics();
        this.fontDescent = metrics.getDescent();
        this.fontCharHeight = metrics.getHeight();
        // this.char_height=metrics.getAscent();
        this.fontCharWidth = metrics.charWidth('W');

        renderingHints = finfo.getRenderingHints();

        // DISPOSE:
        dummyImage.flush();
    }

    public void setFont(FontInfo info) {
        this.initFont(info);
        resetGraphics();
    }

    /**
     * Clear text buffer, doesn't do repaint
     */
    public void clearText() {
        synchronized (textBufferMutex) {
            this.currentBuffer.clearAll();
        }
    }

    /**
     * Clear text, state and Reset Graphics
     */
    public void reset() {
        this.stopRenderers();
        setDrawStyle(0);
        setCharSet(0);
        setCharSet(0, CharSets.CHARSET_US);
        setCharSet(1, CharSets.CHARSET_GRAPHICS);
        this.setCursor(0, 0);
        this.showCursor = true;
        this.clearText();
        resetGraphics();
        this.startRenderers();
    }

    /**
     * Clear text + reset graphics
     */
    public void clear() {
        // clear text + reset graphics
        this.clearText();
        this.resetGraphics();
        this.setCursor(0, 0);
    }

    /**
     * Reset Graphics Recalculate font metrics, image size and repaint complete text buffer. Keeps
     * text in text buff Muste be invoked when Font or Character attributes are changed
     */
    public void resetGraphics() {
        initFont(this.fontInfo);
        // setBackground(fontInfo.getBackground());
        // setForeground(fontInfo.getForeground());

        // PASS(paintImageMutex)
        // DANGEROUS: check double mutex locking !
        synchronized (paintImageMutex) {
        }

        synchronized (textBufferMutex) {
            // resize does refresh contents and initializes text image!
            this.resizeTextBuffers(this.nr_columns, this.nr_rows);
        }
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        // Claim Paint mutex to block other threads if applicable...
        synchronized (paintImageMutex) {
            // paint text image:
            Rectangle clip = g.getClipBounds();

            // debugPrintf(" clip=%d,%d+%d+%d\n",clip.x,clip.y,clip.width,clip.height);

            // paintTextBuffer();// redraw characters (if needed)
            long time = System.currentTimeMillis();

            // debugPrintf(" paint time= %d:%d.%d\n", (time/(60*1000))%60,(time/1000)%60,(time/10)%100);

            int w = currentImage.getWidth(null);
            int h = currentImage.getHeight(null);
            int imageOffsetx = 0;
            int imageOffsety = 0;
            // Note: Component offset has already been added into graphics context by Parent!
            g.drawImage(currentImage, 0, 0, w, h, imageOffsetx, imageOffsety, w, h, this);
        }

        // Post painting: Notify waiting threads:

        synchronized (paintImageMutex) {
            paintImageMutex.notifyAll();
        }
    }

    /**
     * Paint textBuffer in offscreen image buffer
     */
    public boolean paintTextBuffer() {
        return paintTextBuffer(0, 0, this.nr_columns, this.nr_rows);
    }

    /**
     * Paints region [x1,y1] to [x2,y2] - includes x2 if x1==x1 - includes y2 if y1==y2
     */
    protected boolean paintTextBuffer(int x1, int y1, int x2, int y2) {
        if (currentImage == null) {
            // at startup the window might not be visible yet.
            if (initTextBufferImage() == false)
                return false;
        }

        int charwidth = this.getCharWidth();
        int charheight = this.getLineHeight();

        // ===========
        // Option: rendererWaitForPaint
        // Wait for the current Swing Thread to pain the image.
        // This speed up the Swing painting thread but slows down the rendering.
        // ===========

        if (this.optionRendererWaitForPaint) {
            synchronized (paintImageMutex) {
                //
            }
        }

        synchronized (textBufferMutex) {
            // Concurrency!
            // keep current: reset global
            boolean paintAll = (this.paintCompleteTextBuffer || this.optionAlwaysPaintCompleteTextBuffer);

            this.paintCompleteTextBuffer = false;

            // single column mode:
            if (x2 == x1)
                x2++;

            // single line mode:
            if (y2 == y1)
                y2++;

            // don't draw past buffer
            if (x2 > nr_columns)
                x2 = nr_columns;

            if (y2 > nr_rows)
                y2 = nr_rows;

            if (x1 < 0)
                x1 = 0;

            if (y1 < 0)
                y1 = 0;

            // Debug("panel size="+this.getSize());

            // print status line
            // this.drawString("("+cursorX+"."+cursorY+")",0,this.nr_rows);

            Graphics graphics = currentImage.getGraphics();
            // do not clear background:
            // imageGraphics.setColor(this.background_color);
            // imageGraphics.fillRect(0,0,getTermWidth(),getTermHeight());

            if (graphics instanceof Graphics2D) {
                updateRenderingHints((Graphics2D) graphics);
            }
            graphics.setFont(fontPlain);
            graphics.setColor(this.colorMap.getForeground());

            for (int y = y1; y < y2; y++) {
                // whether next character already has been cleared:
                boolean paintBackgroundAheadDone = false;

                for (int x = x1; x < x2; x++) {
                    int xpos = x * charwidth;
                    int ypos = y * charheight;

                    StyleChar sChar = currentBuffer.get(x, y);

                    if (sChar == null) {
                        log.error("NULL char at:{},{}", x, y);
                        continue;
                        // return false;
                    }

                    // no redraw needed
                    if (paintAll == false && sChar.hasChanged == false)
                        continue;

                    // ====
                    // Italics Clear Ahead mode:
                    // ===
                    // Clear next char before drawing current
                    // This because an italics character can 'lean' into the next character.
                    // So clear next character first, draw current and when rendering neighbour
                    // character, do not clear background, but just draw the character.
                    // ====

                    boolean paintBackground = true;
                    boolean paintBackgroundAhead = sChar.isItalic() || sChar.isUberBold();

                    // clear next char

                    // previous char was italic: current background has aleady been draw: don't clear current;
                    if (paintBackgroundAheadDone)
                        paintBackground = false;

                    // ====

                    // first clear next:
                    if (paintBackgroundAhead == true)
                        ;
                    {
                        StyleChar nextChar = currentBuffer.get(x + 1, y);

                        if (nextChar != null) {
                            // clear neighbour background:
                            renderChar(graphics, nextChar, xpos + charwidth, ypos, true, false);
                            // form next drawing that field already has been cleared.
                            this.currentBuffer.needsRepaint(x + 1, y, true); // update draw field !
                            paintBackgroundAheadDone = true;
                        }
                    }

                    // draw current:
                    renderChar(graphics, sChar, xpos, ypos, paintBackground, true);
                    this.currentBuffer.needsRepaint(x, y, false); // has been drawn

                    // check/update cursor:
                    if ((showCursor) && (x == cursorX) && (y == cursorY)) {
                        // System.err.println("paintCursor="+x+","+y);
                        // draw cursor:
                        graphics.setXORMode(this.colorMap.getForeground());
                        graphics.setColor(this.colorMap.getBackground());

                        graphics.fillRect(xpos, ypos, charwidth - 1, charheight - 1);
                        graphics.setPaintMode();
                    }
                } // for y
            } // for x
        }

        // notify waiting threads:
        synchronized (textBufferMutex) {
            textBufferMutex.notifyAll();
        }

        return true;
    }

    private void updateRenderingHints(Graphics2D graphics) {
        if (this.renderingHints == null)
            return;

        Key[] keys = renderingHints.keySet().toArray(new Key[0]);
        for (Key key : keys) {
            Object value = renderingHints.get(key);
            graphics.setRenderingHint(key, value);
        }
    }

    public Dimension getPreferredSize() {
        // make sure layout manager respects current image size.
        return getImageSize();
    }

    public Dimension getMaximumSize() {
        // make sure layout manager respects current image size.
        return getImageSize();
    }

    /**
     * Render single character in imageGraphics
     */
    private void renderChar(Graphics imageGraphics, StyleChar sChar, int xpos, int ypos,
                            boolean paintBackground, boolean paintForeground) {
        int style = sChar.style;

        // check indexed colors!
        Color fg = resolveColor(sChar.foregroundColor);
        Color bg = resolveColor(sChar.backgroundColor);

        if (sChar.customForeground != null) {
            fg = sChar.customForeground;
        }

        // Component defaults:
        if (fg == null)
            fg = this.colorMap.getForeground();

        if (bg == null)
            bg = this.colorMap.getBackground();

        if ((style & StyleChar.STYLE_INVERSE) > 0) {
            // swap bg/fg
            Color c = fg;
            fg = bg;
            bg = c;
        }

        // optional alpha level:
        int alpha = sChar.alpha;

        // Hidden => fixed alpha of 25%;
        if ((style & StyleChar.STYLE_HIDDEN) > 0)
            alpha = 64;

        // alpha=-1 don't care/inherit from foreground
        // alpha=255 is opaque which means also inherit from foreground !
        // alpha blended color!
        if ((alpha >= 0) && (alpha < 255))
            fg = new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), alpha);

        int charHeight = getLineHeight();
        int charWidth = getCharWidth();

        // Paint background
        if (paintBackground) {
            imageGraphics.setColor(bg);
            imageGraphics.fillRect(xpos, ypos, charWidth, charHeight);
        }

        if (paintForeground == false)
            return;

        if (sChar.isChar(' ')) {
            // background image ?
            // ; space already drawn.
        } else {
            boolean isGraphicsChar = isGraphicsCharSet(sChar.charSet);
            if (isGraphicsChar && this.optionUseOwnGraphicsCharsetRenderer)
                renderGraphicsChar(imageGraphics, fg, bg, xpos, ypos, charWidth, charHeight, sChar);
            else
                renderPlainChar(imageGraphics, fg, bg, xpos, ypos, charWidth, charHeight, sChar);
        }

    }

    private void renderGraphicsChar(Graphics imageGraphics, Color fg, Color bg, int xpos, int ypos,
                                    int charWidth, int charHeight, StyleChar schar) {

        char c = (char) schar.charBytes[0];

        boolean leftLine = false;
        boolean rightLine = false;
        boolean upperLine = false;
        boolean lowerLine = false;

        // middle pixels or pixel near middle in the case of an even char height/width
        // the real middle if between two pixels.
        int midlx = xpos + (charWidth / 2); // middle pixel or the pixel to the left of the middle.
        int midrx = xpos + ((charWidth + 1) / 2); // if charWidth is even right middle pixel != left middel pixel
        int midhy = ypos + (charHeight / 2);
        int midly = ypos + ((charHeight + 1) / 2);
        // use one center point left and above logical middle (if even size!).
        midrx = midlx;
        midly = midhy;
        int endx = xpos + charWidth;
        int endy = ypos + charHeight;

        double[][] alphaBlends = {{}, {1.0}, {1.0, 0.5}, {1.0, 0.75, 0.5},
                {1.0, 0.9, 0.5, 0.25},};
        // size and attributes
        int size = 1;
        // Fat Pipes instead of thin lines;
        boolean optionFatGraphicPipes = true;

        if (optionFatGraphicPipes)
            if (schar.isBold())
                size = 4;
            else
                size = 2;
        else if (schar.isBold())
            size = 2;
        else
            size = 1;

        boolean notSupported = false;

        switch (c) {
            case 'u': // ┤
                lowerLine = true;
            case 'j': // ┘
                leftLine = true;
                upperLine = true;
                break;
            case 'k': // ┐
                lowerLine = true;
                leftLine = true;
                break;
            case 'w': // ┬
                leftLine = true;
            case 'l': // ┌
                lowerLine = true;
                rightLine = true;
                break;
            case 'v':
                leftLine = true;
            case 'm': // └
                upperLine = true;
                rightLine = true;
                break;
            case 'n':
                upperLine = true;
                rightLine = true;
                lowerLine = true;
                leftLine = true;
                break;
            case 'x':
                upperLine = true;
                lowerLine = true;
                break;
            case 'q': // ─
                leftLine = true;
                rightLine = true;
                break;
            case 't': // ├
                upperLine = true;
                lowerLine = true;
                rightLine = true;
                break;
            // "opqrs" => "⎺⎻─⎼⎽"
            case 'o':
                leftLine = true;
                rightLine = true;
                midhy = ypos + 1 + size;
                midly = ypos + 1 + size;
                break;
            case 'p':
                leftLine = true;
                rightLine = true;
                midhy -= charHeight / 4;
                midly -= charHeight / 4;
                break;
            // case q already done
            case 'r':
                leftLine = true;
                rightLine = true;
                midhy += charHeight / 4;
                midly += charHeight / 4;
                break;
            case 's':
                leftLine = true;
                rightLine = true;
                midhy = endy - 1 - size;
                midly = endy - 1 - size;
                break;
            default:
                notSupported = true;
                break;
        }

        if (notSupported)
            renderPlainChar(imageGraphics, fg, bg, xpos, ypos, charWidth, charHeight, schar);

        for (int i = 0; i < size; i++) {
            Color drawFG = fg;

            if (optionFatGraphicPipes) {
                drawFG = ColorMap.blendColor(bg, fg, alphaBlends[size][i], true);
            }

            imageGraphics.setColor(drawFG);

            // todo: beter logic
            if (leftLine) {
                if (upperLine)
                    imageGraphics.drawLine(xpos, midhy - i, midlx - i, midhy - i);
                else
                    imageGraphics.drawLine(xpos, midhy - i, midlx, midhy - i);

                if (lowerLine)
                    imageGraphics.drawLine(xpos, midly + i, midlx - i, midly + i);
                else
                    imageGraphics.drawLine(xpos, midly + i, midlx, midly + i);
            }

            if (rightLine) {
                if (upperLine)
                    imageGraphics.drawLine(midrx + i, midhy - i, endx, midhy - i);
                else
                    imageGraphics.drawLine(midrx, midhy - i, endx, midhy - i);

                if (lowerLine)
                    imageGraphics.drawLine(midrx + i, midly + i, endx, midly + i);
                else
                    imageGraphics.drawLine(midrx, midly + i, endx, midly + i);
            }

            if (upperLine) {
                if (leftLine)
                    imageGraphics.drawLine(midlx - i, ypos, midlx - i, midhy - i);
                else
                    imageGraphics.drawLine(midlx - i, ypos, midlx - i, midhy);

                if (rightLine)
                    imageGraphics.drawLine(midrx + i, ypos, midrx + i, midhy - i);
                else
                    imageGraphics.drawLine(midrx + i, ypos, midrx + i, midhy);

            }
            if (lowerLine) {
                if (leftLine)
                    imageGraphics.drawLine(midlx - i, midly + i, midlx - i, endy);
                else
                    imageGraphics.drawLine(midlx - i, midly, midlx - i, endy);

                if (rightLine)
                    imageGraphics.drawLine(midrx + i, midly + i, midrx + i, endy);
                else
                    imageGraphics.drawLine(midrx + i, midly, midrx + i, endy);
            }
        }

    }

    private void renderPlainChar(Graphics imageGraphics, Color fg, Color bg, int xpos, int ypos,
                                 int charWidth, int charHeight, StyleChar schar) {
        int style = schar.style;
        byte[] bytes = schar.charBytes;
        int numBytes = schar.numBytes;

        // lower left corner to start drawing (above descent):
        int imgx = xpos;
        int imgy = ypos + charHeight - line_space - fontDescent;// text center start above lower border

        String encoded;
        encoded = new String(bytes, 0, numBytes, StandardCharsets.UTF_8);

        encoded = checkCharSet(schar.charSet, encoded);

        // Render Character;

        // Blink ? => currently done in animation thread
        boolean blink = ((style & StyleChar.STYLE_BLINK) > 0);
        boolean bold = ((style & StyleChar.STYLE_BOLD) > 0);
        boolean italic = ((style & StyleChar.STYLE_ITALIC) > 0);
        boolean uberbold = ((style & StyleChar.STYLE_UBERBOLD) > 0);

        imageGraphics.setColor(fg);

        if (bold && !italic)
            imageGraphics.setFont(fontBold);
        else if (!bold && italic)
            imageGraphics.setFont(fontItalic);
        else if (bold && italic)
            imageGraphics.setFont(fontItalicBold);
        else
            imageGraphics.setFont(fontPlain);

        Graphics2D g2d = (Graphics2D) imageGraphics;

        if (this.fontInfo.getRenderingHints() != null) {
            g2d.setRenderingHints(fontInfo.getRenderingHints());
        }

        // =========================
        // Actual Rendering
        // =========================

        // slow :
        if (uberbold) {
            Color shadedFG = ColorMap.blendColor(bg, fg, 0.5, true);

            imageGraphics.setColor(shadedFG);
            imageGraphics.drawString(encoded, imgx - 1, imgy);
            imageGraphics.drawString(encoded, imgx + 1, imgy);
            imageGraphics.drawString(encoded, imgx, imgy - 1);
            imageGraphics.drawString(encoded, imgx, imgy + 1);
            imageGraphics.setColor(fg);
            imageGraphics.drawString(encoded, imgx, imgy);

        } else {
            imageGraphics.drawString(encoded, imgx, imgy);
        }

        // add line:
        if ((style & StyleChar.STYLE_UNDERSCORE) > 0) {
            imageGraphics.drawLine(imgx, imgy, imgx + this.fontCharWidth, imgy);
        }
    }

    public Color resolveColor(int index) {
        if ((index < 0) || (index >= colorMap.size()))
            return null;

        return this.colorMap.get(index);
    }

    private String checkCharSet(String charset, String str) {
        if (isGraphicsCharSet(charset)) {
            String graph = "";
            for (int i = 0; i < str.length(); i++) {
                graph += this.CharSet1(str.charAt(i));
            }
            return graph;
        }

        return str;
    }

    private boolean isGraphicsCharSet(String charset) {
        if (charset == null)
            return false;

        return charset.compareTo(CharSets.CHARSET_GRAPHICS) == 0;

    }

    public Dimension getImageSize() {
        return new Dimension(getImageWidth(), getImageHeight());
    }

    public int getImageWidth() {
        return fontCharWidth * nr_columns;
    }

    public int getImageHeight() {
        return getLineHeight() * nr_rows;
    }

    public int getCharWidth() {
        return fontCharWidth;
    }

    public int getCharHeight() {
        return fontCharHeight;
    }

    /**
     * Return nr of columns
     */
    public int getColumnCount() {
        return nr_columns;
    }

    /**
     * Returns nr of rows
     */
    public int getRowCount() {
        return nr_rows;
    }

    public void setCursor(int x, int y) {
        if (x < 0) {
            log.error("new cursor X<0:{}", x);
            x = 0;
        }

        if (y < 0) {
            log.error("new cursor Y<0:{}", y);
            y = 0;
        }

        // vttest! : allow to set cursor 1 position next to screen !
        // put cursor near screen:write char will autowrap!
        if (x > nr_columns)
            x = nr_columns;

        if (y > nr_rows)
            y = nr_rows;

        int prefx = cursorX;
        int prefy = cursorY;

        this.cursorX = x;
        this.cursorY = y;

        // Request repaints: !
        characterChanged(cursorX, cursorY);
        characterChanged(prefx, prefy);
    }

    /**
     * Request and schedule repaint. Actual repaint might occur later. Multiple repaint requests are
     * gathered and the hasChanged field is set to true to merge/combine repaints.
     *
     * @param x position of char which needs to be repainted
     * @param y position of char which needs to be repainted
     */
    private void characterChanged(int x, int y) {
        this.currentBuffer.needsRepaint(x, y, true);
    }

    /**
     * Clear area from [x1,y1] (inclusive) to [x2,y2] (exclusive)
     */
    public void clearArea(int x1, int y1, int x2, int y2) {
        // Debug("Clear:"+"x1="+x1+","+"y1="+y1+","+"x2="+x2+","+"y2="+y2);
        // avoid empty lines:
        if (y2 == y1)
            y2++;

        // avoid empty lines:
        if (x2 == x1)
            x2++;

        if (x2 > this.nr_columns) {
            log.debug("***Overflow: x2 > nr_columns:{}>{}\n", x2, nr_columns);
            x2 = nr_columns;
        }

        if (y2 > this.nr_rows) {
            log.debug("***Overflow: y2 > nr_rows:{}>{}\n", y2, nr_rows);
            y2 = nr_rows;
        }

        for (int y = y1; y < y2; y++) {
            for (int x = x1; x < x2; x++) {
                // clear means: put space char using current draw style:
                this.putChar(' ', x, y);
                // characterChanged(x,y);
            }
        }

        // this.charsChanged=true;
    }

    public void move(int startX, int startY, int width, int height, int toX, int toY) {
        move(startX, startY, width, height, toX, toY, null);
    }

    // not fully tested yet. If filler==null => clear values
    public void move(int startX, int startY, int width, int height, int toX, int toY,
                     StyleChar filler) {
        int beginx = 0;
        int stepx = 1;
        int endx = width;

        int beginy = 0;
        int stepy = 1;
        int endy = height;

        // reverse horizontal move
        if (toX > startX) {
            beginx = width - 1;
            stepx = -1;
            endx = -1;
        }

        // reverse vertical move
        if (toY > startY) {
            beginy = height - 1;
            stepy = -1;
            endy = -1;
        }

        synchronized (textBufferMutex) {
            for (int j = beginy; j != endy; j += stepy)
                // rows
                for (int i = beginx; i != endx; i += stepx) // columns
                {
                    currentBuffer.copy(toX + i, toY + j, startX + i, startY + j);
                    if (filler == null)
                        currentBuffer.clear(startX + i, startY + j);// clear source
                    else
                        currentBuffer.set(startX + i, startY + j, filler);// clear source

                }
        }
    }

    public void scrollRegion(int startline, int endline, int lines, boolean up) {
        if (up)
            move(0, startline + lines, nr_columns, endline - startline - lines, 0, startline);
        else
            move(0, startline, nr_columns, endline - startline - lines, 0, startline + lines);
    }

    /**
     * Method blocks until current text image is painted by Swing
     */
    public void syncPainted() {
        // wait for current textbuffer to be painted !
        // use paintMutex
        synchronized (paintImageMutex) {
            try {
                paintImageMutex.wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * String Encoding
     */
    public String getEncoding() {
        return this.characterEncoding;
    }

    /**
     * String Encoding: Does not reset graphics
     */
    public void setEncoding(String encoding) {
        this.characterEncoding = encoding;
    }

    public void writeChar(int c) {
        String str = "" + (char) c;
        this.putChar(str.getBytes(), getCursorX(), getCursorY());
        moveCursor(1, 0);
    }

    public void putChar(int c, int x, int y) {
        String str = "" + (char) c;
        this.putChar(getBytes(str), x, y);
    }

    public byte[] getBytes(String str) {
        try {
            return str.getBytes(getEncoding());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str.getBytes();
    }

    public void writeString(String str) {
        for (int i = 0; i < str.length(); i++)
            writeChar(str.charAt(i));
    }

    public void putString(String str, int x, int y) {
        setCursor(x, y);
        for (int i = 0; i < str.length(); i++)
            writeChar(str.charAt(i));
    }

    public void writeChar(byte[] bytes) {
        checkAutoWrap();

        putChar(bytes, getCursorX(), getCursorY(), true);
    }

    public void putChar(byte[] bytes, int x, int y) {
        putChar(bytes, x, y, false);
    }

    public StyleChar getStyleChar(int x, int y) {
        return this.currentBuffer.get(x, y);
    }

    // master method. Write single UTF encoded char at position x,y
    public void putChar(byte[] bytes, int x, int y, boolean autoIncrement) {

        if (currentBuffer.checkBounds(x, y) == false) {
            log.warn("checkBounds(): out of bounds (x,y)={},{}\n", x, y);
            return;
        }

        int xpos = x;
        int ypos = y;

        // synchronize per put to allow paint event to come between puts.
        synchronized (textBufferMutex) {
            // moveCursor(0,0); // does boundaray checking !

            StyleChar sChar;
            if ((sChar = this.currentBuffer.get(xpos, ypos)) == null) {
                // happens during asynchronize resize events!
                // Swing resize the current text buffer but the emulator still appends chars.
                log.warn("No character at position: {},{}\n", xpos, ypos);
            } else {
                sChar.setBytes(bytes);
                sChar.charSet = getCharSet();

                sChar.setDrawStyle(getDrawStyle());
                sChar.foregroundColor = this.drawForegroundIndex;
                sChar.backgroundColor = this.drawBackgroundIndex;
                sChar.alpha = -1; // reset;
                characterChanged(xpos, ypos);
            }
            // keep cursor position even if it is 'offscreen'
            // this.cursorX=xpos+1;
            // this.cursorY=ypos;

            // this.setCursor(cursorX,cursorY);
        }

        if (autoIncrement)
            moveCursor(1, 0);

    }

    public void moveCursor(int dx, int dy) {
        int xpos = getCursorX();
        int ypos = getCursorY();

        xpos += dx;
        ypos += dy;

        setCursor(xpos, ypos);

        // checkAutoWrap();
    }

    private void checkAutoWrap() {
        int xpos = getCursorX();
        int ypos = getCursorY();

        // PRE check:
        if ((xpos >= this.nr_columns) && (wraparound == true)) {
            xpos = 0;
            ypos++;
        }

        if (ypos >= this.nr_rows) {
            ypos = nr_rows - 1;
            if (autoscroll == true) {
                log.debug(">>> autoscroll I <<<\n");
                scrollRegion(0, this.nr_rows, 1, true); // scroll whole screen up!
            }
        }

        setCursor(xpos, ypos);
    }

    public String getCharSet() {
        return charSets[charSet]; // may be null;
    }

    public void setCharSet(int nr) {
        log.debug("setCharSet:#{}", nr);
        this.charSet = nr;
    }

    private char CharSet1(char c) {
        // linear lookup, could use mapping tables:
        for (int i = 0; i < graphOrg.length(); i++) {
            if (graphOrg.charAt(i) == c)
                return graphSet1.charAt(i);
        }
        return c;
    }

    // /** Ignores key released events. */
    // public void keyReleased(KeyEvent event)
    // {
    // }

    public void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Returns Character Height+ line_spacing + descent
     */
    public int getLineHeight() {
        return this.fontCharHeight + line_space + fontDescent;
    }

    public int getDrawStyle() {
        return drawStyle;
    }

    public void setDrawStyle(int style) {
        this.drawStyle = style;

        // style=0 is reset colors as well.
        if (style == 0) {
            this.drawBackgroundIndex = -1;
            this.drawForegroundIndex = -1;
        }
    }

    public int getCursorY() {
        return this.cursorY;
    }

    public int getCursorX() {
        return this.cursorX;
    }

    public void addDrawStyle(int style) {
        setDrawStyle(getDrawStyle() | style);
    }

    public void setDrawBackground(int nr) {
        this.drawBackgroundIndex = nr;
    }

    //
    // Moved to parent container. Parent component handles key event. CharPane is only used for displaying characters
    // public void setKeyMapper(IKeyListener mapper)
    // {
    // this.keyMapper=mapper;
    // }
    //
    // /** Override Component.processKeyEvent -> use custom KeyMapper */
    // public void processKeyEvent(KeyEvent e)
    // {
    // debug("Key!:"+e);
    //
    // if (keyMapper==null)
    // {
    // debug("*** No KeyMapper ***");
    // return; //no keymapper present
    // }
    // // System.out.println(e);
    // int id = e.getID();
    // if (id == KeyEvent.KEY_PRESSED)
    // {
    // keyMapper.keyPressed(e);
    // }
    // else if (id == KeyEvent.KEY_RELEASED)
    // {
    // keyMapper.keyReleased(e);
    // }
    // else if (id == KeyEvent.KEY_TYPED)
    // {
    // keyMapper.keyTyped(e);/* keyTyped(e); */
    // }
    //
    // // leave for other listeners ?
    // e.consume();
    // }

    public void setDrawForeground(int nr) {
        this.drawForegroundIndex = nr;
    }

    public boolean getWrapAround() {
        return wraparound;
    }

    /**
     * Set Auto wrap when character are drawn beyoned the line lenght
     */
    public void setWrapAround(boolean value) {

        this.wraparound = value;
    }

    public boolean getAutoScroll() {
        return autoscroll;
    }

    public void setAutoScroll(boolean auto) {
        this.autoscroll = auto;
    }

    /**
     * Set Font size: Does not reset graphics
     */
    public void setFontSize(int i) {
        this.fontInfo.setFontSize(i);
        // updateFont();
    }

    /**
     * Set Font type: Does not reset graphics
     */
    public void setFontType(String type) {
        this.fontInfo.setFontFamily(type);
        // updateFont();
    }

    /**
     * Set Character Set: Does not reset graphics
     */
    public void setCharSet(int i, String str) {
        log.debug("setCharSet: #{}={}", i, str);
        charSets[i] = str;
    }

    /**
     * Set Colormap: Does not reset graphics
     */
    public void setColorMap(ColorMap colorMap) {
        this.colorMap = colorMap;
    }

    public FontInfo getFontInfo() {
        return this.fontInfo;
    }

    public void dispose() {
        stopRenderers();
        if (this.currentImage != null) {
            this.currentImage.flush();
            this.currentImage = null;
        }
        this.renderTask = null;
        this.renderThread = null;

        if (this.fullBuffer != null) {
            this.fullBuffer.dispose();
            this.fullBuffer = null;
        }
        if (this.altTextBuffer != null) {
            this.altTextBuffer.dispose();
            this.altTextBuffer = null;
        }

        this.currentBuffer = null;
    }

    public void stopRenderers() {
        log.debug(">>> STOPPED <<<");

        this.mustStop = true;
        if (refreshTimer != null)
            this.refreshTimer.stop();
        if (renderThread != null)
            this.renderThread.interrupt();
        renderThread = null;
        refreshTimer = null;
        renderTask = null;
    }

    /**
     * After a resize by the parent container, this method can be called to update the internal size
     * to match the actual component size. If not the charPane will keep its current size and the
     * contents will be clipped.
     */
    public void resizeTextBuffersToAWTSize(Dimension size) {
        int cols = size.width / this.getCharWidth();
        int rows = size.height / this.getLineHeight();

        // center character image: Ugly
        // imageOffsetx=(cols*getCharWidth()-size.width)/2;
        // imageOffsety=(rows*getLineHeight()-size.height)/2;

        resizeTextBuffers(cols, rows);
    }

    /**
     * Update character pane size given the new column and row count.
     */
    public void resizeTextBuffers(int cols, int rows) {
        this.nr_rows = rows;
        this.nr_columns = cols;

        this._resizeTextBuffers(nr_columns, nr_rows);

        if (this.cursorY >= nr_rows)
            cursorY = nr_rows - 1;

        this.paintCompleteTextBuffer = true;
        this.paintTextBuffer();
        this.revalidate();
        this.repaint();
    }

    public void setColumns(int columns) {
        resizeTextBuffers(columns, this.nr_rows);
    }

    // actual resize:
    private void _resizeTextBuffers(int numCs, int numRs) {
        log.debug("resizeTextBuffer:{},{}", numCs, numRs);

        synchronized (this.textBufferMutex) {
            if (fullBuffer == null)
                this.fullBuffer = new TextBuffer(numCs, numRs);// add virtual buffer
            else
                this.fullBuffer.resize(numCs, numRs, true);

            if (altTextBuffer == null)
                this.altTextBuffer = new TextBuffer(numCs, numRs, numRs); // no virtual buffer
            else
                this.altTextBuffer.resize(numCs, numRs, true);

            if (this.currentBuffer == null)
                this.currentBuffer = fullBuffer;
        }

        // block paints:
        synchronized (paintImageMutex) {
            this.initTextBufferImage();
        }
    }

    public void pageUp() {
        // virtual page up!
    }

    public void pageDown() {

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == refreshTimer) {
            doAnimation();
        }
    }

    public void doAnimation() {
        Dimension size = this.currentBuffer.getSize();

        int div = 8; // slowdown
        int numSteps = 32;
        // smooth cosinus

        for (int x = 0; x < size.width; x++)
            for (int y = 0; y < size.height; y++) {
                StyleChar c = currentBuffer.get(x, y);

                double phase0 = (((animationCounter + x * 16) / 7) % numSteps);
                double phase1 = (((animationCounter + x * 16) / 11) % numSteps);
                double phase2 = (((animationCounter + x * 16) / 17) % numSteps);

                double cos0 = Math.cos((phase0 / numSteps) * Math.PI * 2);
                double cos1 = Math.cos((phase1 / numSteps) * Math.PI * 2);
                double cos2 = Math.cos((phase2 / numSteps) * Math.PI * 2);

                int alpha = (int) (128 + 127 * cos0);
                int val0 = (int) (128 + 127 * cos0);
                int val1 = (int) (128 + 127 * cos1);
                int val2 = (int) (128 + 127 * cos2);


                if ((c != null) && (c.hasStyle(StyleChar.STYLE_BLINK))) {
                    if (c.alpha != alpha) {
//                        c.setAlpha(alpha); // updates changes
                        currentBuffer.needsRepaint(x, y, true);// update
                    }

                    c.customForeground = new Color(val0, val1, val2);
                }
            }

        this.animationCounter++;
    }

    /**
     * Options
     */
    public String getOption(String optStr) {
        if (optStr == null)
            return null;

        if (optStr.compareTo(CharPane.OPTION_ALWAYS_SYNCHRONIZED_SCROLLING) == 0)
            return "" + this.optionAlwaysSynchronizedScrolling;

        return null;
    }

    public void setOption(String name, String value) {
        if ((name == null) || (value == null))
            return;

        if (name.compareTo(CharPane.OPTION_ALWAYS_SYNCHRONIZED_SCROLLING) == 0) {
            this.optionAlwaysSynchronizedScrolling = new Boolean(value);
        }

    }

    public void setEnableCursor(boolean value) {
        this.showCursor = value;
    }

    public boolean setAltScreenBuffer(boolean useAlt) {
        // extra buffer can mess up painting thread -> check mutex handling.
        if (this.optionSupportAltScreenBuffer == false)
            return false;

        // only swap between render events !
        synchronized (this.textBufferMutex) {
            if (useAlt)
                this.currentBuffer = this.altTextBuffer;
            else
                this.currentBuffer = this.fullBuffer;

            this.paintCompleteTextBuffer = true;
        }

        return true;
    }

    public void setSlowScroll(boolean value) {
        this.slowScroll = value;
    }

    public void setCursorOptions(boolean blink) {
        //this.cursorBlink = value; // default is off (no blinkink)
        if (blink == true) {
            CharPane.log.warn("*** Fixme: cursor blink:{}", blink);
        }
    }

    public static class CharSets {
        public final static String CHARSET_US = VT_CHARSET_US;

        public final static String CHARSET_UK = VT_CHARSET_UK;

        public final static String CHARSET_GRAPHICS = VT_CHARSET_GRAPHICS;
    }

    /**
     * UNsynchronized text buffer contains Matrix of StyleChars
     */
    public class TextBuffer {
        private StyleChar[][] textBuffer;

        // real size including current rows;
        private int virtualSize = 100;

        // offset: start of current top screen must be less then (virtualSize-rows)
        private final int virtualOffset = 0;

        private boolean bufferChanged;

        private int nrColumns;

        private int nrRows;

        public TextBuffer(int cols, int rows) {
            init(cols, rows, 100);
        }

        public TextBuffer(int numCs, int numRs, int numVRs) {
            init(numCs, numRs, numVRs);
        }

        private void init(int cols, int rows, int virtualSize) {
            // all defaults:

            this.virtualSize = virtualSize;
            this.nrColumns = cols;
            this.nrRows = rows;

            if (virtualSize < rows)
                ;
            virtualSize = rows;

            this.textBuffer = new StyleChar[nr_rows][nr_columns];

            for (int y = 0; y < rows; y++)
                for (int x = 0; x < cols; x++)
                    put(x, y, new StyleChar());

            this.bufferChanged = true;
        }

        public void clearAll() {
            for (int y = 0; y < nrRows; y++)
                for (int x = 0; x < nr_columns; x++)
                    textBuffer[y][x].clear();

            this.bufferChanged = true;
        }

        /**
         * Puts actual object into text array. Doesn't copy object !
         */
        protected void put(int x, int y, StyleChar newChar) {
            // if (checkBounds(x,y)==false)
            // return;

            textBuffer[y][x] = newChar;
            newChar.hasChanged = true;
            this.bufferChanged = true;
        }

        public boolean checkBounds(int x, int y) {
            boolean val = true;

            if (textBuffer == null)
                val = false;
            else if ((y < 0) || (x < 0))
                val = false;
            else if (y >= textBuffer.length)
                val = false;
            else if (textBuffer[y] == null)
                val = false;
            else if (x >= textBuffer[y].length)
                val = false;
            else if (textBuffer[y][x] == null) {
                // Error("NULL StyleChar at:"+x+","+y);
                val = false;
            }

            // if (val==false)
            // Error("Index out of bounds !:"+x+","+y);

            return val;
        }

        /**
         * Returns actual object in text array
         */
        public StyleChar get(int x, int y) {
            if (checkBounds(x, y) == false)
                return null;

            return textBuffer[y][x];
        }

        /**
         * Copies values from StyleChar. Does not store object
         */
        public void set(int x, int y, StyleChar schar) {
            if (checkBounds(x, y) == false)
                return;

            textBuffer[y][x].copyFrom(schar);
        }

        public void resize(int newCs, int newRs, boolean keepContents) {
            StyleChar[][] oldbuffer = this.textBuffer;

            int offset = 0;
            // move current contents up:
            if (newRs < this.nrRows)
                offset = nrRows - newRs;

            init(newCs, newRs, virtualSize);
            // int offy=textBuffer.length-numRs;
            if (keepContents)
                for (int y = 0; y < newCs; y++)
                    for (int x = 0; x < newCs; x++) {
                        // copy values from old buffer:
                        if (oldbuffer != null) {
                            int sourcey = y + offset;
                            if ((sourcey < oldbuffer.length) && (x < oldbuffer[sourcey].length))
                                textBuffer[y][x].copyFrom(oldbuffer[sourcey][x]);
                        }

                    }
            this.bufferChanged = true;
        }

        public void needsRepaint(int x, int y, boolean val) {
            if (checkBounds(x, y) == false)
                return;

            textBuffer[y][x].hasChanged = val;

            if (val == true)
                this.bufferChanged = true;
        }

        public void clear(int x, int y) {
            if (checkBounds(x, y) == false)
                return;

            textBuffer[y][x].clear();
            this.bufferChanged = true;
        }

        /**
         * Copy [dest]=[source]
         */
        public void copy(int destx, int desty, int sourcex, int sourcey) {
            if (checkBounds(sourcex, sourcey) == false)
                return;
            if (checkBounds(destx, desty) == false)
                return;
            // copy values !
            // do not copy object reference.
            textBuffer[desty][destx].copyFrom(textBuffer[sourcey][sourcex]);
            this.bufferChanged = true;
        }

        public void setChanged(boolean val) {
            this.bufferChanged = val;
        }

        public boolean hasChanged() {
            return bufferChanged;
        }

        public Dimension getSize() {
            return new Dimension(nrColumns, nrRows);
        }

        public void dispose() {
            this.textBuffer = null; // nullify object references.
        }
    }

}
