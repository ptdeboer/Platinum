/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.emulator;

import lombok.extern.slf4j.Slf4j;
import nl.piter.vterm.api.CharacterTerminal;
import nl.piter.vterm.api.EmulatorListener;
import nl.piter.vterm.emulator.Tokens.Token;
import nl.piter.vterm.ui.charpane.StyleChar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static nl.piter.vterm.emulator.VTxTokenDefs.CTRL_ESC;

/**
 * Implementation of most VT100 codes, VT102, and some xterm/xterm-256-color;
 */
@Slf4j
public class VTxXEmulator implements Emulator {

    protected InputStream errorInput;
    private final OutputStream outputStream;

    protected boolean isConnected = false;
    protected String termType;
    protected int nr_columns;
    protected int nr_rows;
    private String encoding = "UTF-8";

    protected Object haltMutex = new Object();
    protected Object terminateMutex = new Object();

    protected boolean signalHalt = false;
    protected boolean signalTerminate = false;

    private CharacterTerminal term = null;
    private VTxTokenizer tokenizer = null;
    //private final VTXTokenTableFactory tokenDefFactory;

    private final List<EmulatorListener> listeners = new ArrayList();

    byte[] single = new byte[1];

    /**
     * Inclusive region start: (y>=y1)
     */
    int region_y1 = 0; // 1;

    /**
     * EXCLUSIVE region end:(y<y2)
     */
    int region_y2 = 0; //term_height;

    private boolean hasRegion;

    int tabSize = 8;

    // --- misc ---
    private boolean applicationCursorKeys;
    private int savedCursorX;
    private int savedCursorY;
//    private VTXTokenTableFactory emulatorDefs;

    /**
     * Construct new Terminal Emulator.
     *
     * @param term
     * @param inputStream
     * @param outputStream
     */
    public VTxXEmulator(CharacterTerminal term, InputStream inputStream, OutputStream outputStream) {
        setTerm(term);
        setInputStream(inputStream);
        this.outputStream = outputStream;
        this.nr_columns = term.getColumnCount();
        this.nr_rows = term.getRowCount();
        // this.tokenDefFactory = new VTXTokenTableFactory();
    }

    void setTerm(CharacterTerminal term) {
        this.term = term;
    }

    void setInputStream(InputStream inps) {
        this.tokenizer = new VTxTokenizer(inps);
    }

    /**
     * Reset states, but do NO disconnect
     */
    public void reset() {
        nr_columns = term.getColumnCount();
        nr_rows = term.getRowCount();
    }

    public void send(byte b) throws IOException {
        single[0] = b;
        send(single);
    }

    public void send(byte[] code) throws IOException {
        if (code == null) {
            log.error("Cowardly refusing to send NULL bytes");
            return;
        }

        synchronized (this.outputStream) {
            this.outputStream.write(code);
            this.outputStream.flush();
        }
    }

    /**
     * Check whether there is text from stderr which is connected to the Terminal implementation.
     */
    protected void readErrorStream() throws IOException {
        if (errorInput == null)
            return;

        int MAX = 1024;
        byte[] buf = new byte[MAX + 1];

        if (this.errorInput.available() > 0) {
            int size = this.errorInput.available();

            if (size > MAX)
                size = 1024;

            int numread = errorInput.read(buf, 0, size);

            buf[numread] = 0;

            String str = new String(buf, 0, numread);
            System.err.println(str);
        }

    }

    public void setErrorInput(InputStream errorStream) {
        this.errorInput = errorStream;
    }

    public boolean isConnected() {
        return isConnected;
    }

    protected void setConnected(boolean val) {
        isConnected = val;
    }

    public String getTermType() {
        return termType;
    }

    /**
     * Set/send new TERM type
     */
    public void setTermType(String type) {
        this.termType = type;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    // ======================
    //
    // ======================

    @Override
    public void addListener(EmulatorListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(EmulatorListener listener) {
        this.listeners.remove(listener);
    }

    protected void fireGraphModeEvent(int type, String text) {
        for (EmulatorListener listener : listeners) {
            listener.notifyGraphMode(type, text);
        }
    }

    protected void fireResizedEvent(int columns, int rows) {
        for (EmulatorListener listener : listeners) {
            listener.notifyResized(columns, rows);
        }
    }

    public void signalHalt(boolean val) {
        this.signalHalt = val;

        synchronized (haltMutex) {
            if (val == false)
                haltMutex.notifyAll();
        }
    }

    public void step() {
        // when halted, a notify will execute one step in the terminal
        synchronized (haltMutex) {
            haltMutex.notifyAll();
        }
    }

    public void signalTerminate() {
        this.signalTerminate = true;

        synchronized (terminateMutex) {
            terminateMutex.notifyAll();
        }
    }

    public boolean sendSize(int cols, int rows) {
        this.nr_columns = cols;
        this.nr_rows = rows;
        this.region_y1 = 0;
        this.region_y2 = rows;
        log.error("sendSize(): Doesn't work");
//        sendTermSize();
        return false;
    }

    /**
     * Update terminal size and region without sending control sequences.
     *
     * @param cols
     * @param rows
     * @param y1
     * @param y2
     * @return
     */
    public boolean updateRegion(int cols, int rows, int y1, int y2) {
        this.nr_columns = cols;
        this.nr_rows = rows;
        this.region_y1 = y1;
        this.region_y2 = y2;
        return true;
    }

    public int[] getRegion() {
        return new int[]{this.nr_columns, this.nr_rows, this.region_y1, this.region_y2};
    }

    public boolean sendTermSize() {
        log.warn("sendTermSize():[{},{}]", nr_columns, nr_rows);

//        int r = nr_rows;
//        int c= nr_columns;
//
//        byte pr1 = (byte) ('0' + ((r / 10) % 10));
//        byte pr2 = (byte) ('0' + (r % 10));
//        byte pc1 = (byte) ('0' + (c / 100) % 10);
//        byte pc2 = (byte) ('0' + (c / 10) % 10);
//        byte pc3 = (byte) ('0' + (c % 10));
//
//
//        byte[] bytes = {(byte) CTRL_ESC, '[', '1','8',';', pr1, pr2, ';', pc1, pc2,pc3, 't'};
//
//
//        try {
//            this.send(bytes);
//        } catch (IOException e) {
//            checkIOException(e, true);
//        }

        return false;

    }

    public void sendTermType() {

        // *** Report from 'vttest' when using it inside xterm.
        // For some reason when connecting through ssh, this works, but not using
        // the homebrew 'ptty.lxe'  file. (is does when forking a 'bin/csh' ).
        // Report is:    <27> [ ? 1 ; 2 c  -- means VT100 with AVO (could be a VT102)
        // Legend:  AVO = Advanced Video Option

        // I am vt10x compatible:
        byte[] bytes = {CTRL_ESC, '[', '?', '1', ';', '2', 'c'};

        try {
            this.send(bytes);
        } catch (IOException e) {
            checkIOException(e, true);
        }
    }


    public void start() {
        nr_columns = term.getColumnCount();
        nr_rows = term.getRowCount();
        region_y1 = 0;
        region_y2 = nr_rows;

        log.info("<<<Session Started>>>");
        setConnected(true);
        while (signalTerminate == false) {
            synchronized (haltMutex) {
                if (this.signalHalt) {
                    try {
                        this.haltMutex.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                nextToken();
            }
            // Catch ALL and continue!!
            catch (Throwable e) {
                log.error("nextToken():Exception >>>", e);
            }
        }// while

        setConnected(false);
        log.info("<<<Session Ended>>>");
    }

    protected void nextToken() throws IOException {
        readErrorStream();

        int x = term.getCursorX();
        int y = term.getCursorY();

        Token token = tokenizer.nextToken();

        // Text representation parse bytes sequence
        byte[] bytes = tokenizer.getBytes();

        int arg1 = 0;
        int arg2 = 0;

        int numIntegers = tokenizer.args().numArgs();

        if (numIntegers > 0)
            arg1 = tokenizer.args().arg1();

        if (numIntegers > 1)
            arg2 = tokenizer.args().arg2();

        int defNum1 = 1;
        if (arg1 > 0)
            defNum1 = arg1;

        switch (token) {
            case EOF:
                log.debug("EOF: Connection Closed.");
                signalTerminate = true;
                break;
            case EOT:
                log.debug("EOT: Connection Closed.");
                signalTerminate = true;
                break;
            case NUL:
            case DEL:
                //ignore
                break;
            case SEND_TERM_ID:
                // supported ?
                log.warn("***Fixme: Request Identify not tested");
                this.sendTermType();
                break;
            case BEEP:
                term.beep();
                break;
            case HT: { // TAB
                x = ((x / tabSize + 1) * tabSize);
                if (x >= nr_columns) {
                    x = 0;
                    y += 1;
                }
                term.setCursor(x, y);
                //term.drawCursor();
                break;
            }
            case BS: { // backspace
                x -= 1;
                if (x < 0) {
                    y -= 1;
                    x = nr_columns - 1;
                }
                term.setCursor(x, y);
                break;
            }
            case LF:
            case VT:
            case FF: {
                // MIN(nr_rows,region);
                int maxy = nr_rows;
                if (region_y2 < maxy)
                    maxy = region_y2;

                // Auto LineFeed when y goes out of bounds (or region)
                if (y + 1 >= maxy) {
                    // scroll REGION
                    term.scrollRegion(this.region_y1, maxy, 1, true);
                    y = maxy - 1; // explicit keep cursor in region.
                } else {
                    y += 1;
                    term.setCursor(x, y);
                    log.debug("FF: New Cursor (x,y)=[{},{}]", x, y);
                }
                break;
            }
            case CR: { // carriage return
                x = 0;
                term.setCursor(x, y);
                break;
            }
            case UP:
                log.trace("UP:{}", token, arg1);
                term.setCursor(x, y);
                break;
            case DOWN:
                log.trace("DOWN:{}\n", token, arg1);
                y += defNum1;
                term.setCursor(x, y);
                break;
            case LEFT:
                log.trace("LEFT:{}", token, arg1);
                x -= defNum1;
                term.setCursor(x, y);
                break;
            case RIGHT:
                log.trace("RIGHT:{}", token, arg1);
                x += defNum1;
                term.setCursor(x, y);
                break;
//            case SET_CURSORX:
//                if (numIntegers > 0)
//                    x = arg1 - 1;
//                else
//                    x = 0;
//                term.setCursor(x, y);
//                break;
            case SAVE_CURSOR:
                saveCursor();
                break;
            case RESTORE_CURSOR:
                restoreCursor();
                break;
            case SET_REGION: {
                if (numIntegers == 0) {
                    //reset
                    region_y1 = 0;
                    region_y2 = nr_rows;
                    hasRegion = false;
                } else {
                    region_y1 = arg1 - 1; // inclusive ->inclusive (-1)
                    region_y2 = arg2; // inclusive -> exclusive (-1+1)
                    hasRegion = true;
                }
                break;
            }
            case SET_COLUMN: {
                term.setCursor(arg1 - 1, y);
                break;
            }
            case SET_ROW: {
                term.setCursor(x, arg1 - 1);
                break;
            }
            case DEL_CHAR: {
                // delete under cursor shift right of cursor left !
                int num = 1;
                if (numIntegers > 0)
                    num = arg1;
                // mutli delete is move chars to left
                term.move(x + num, y, nr_columns - x - num, 1, x, y);
                break;
            }
            case ERASE_CHARS: {
                int n = arg1;
                for (int i = 0; i < n; i++)
                    term.putChar(' ', x + i, y);
                term.setCursor(x, y);
                break;
            }
            case DELETE_LINES: {
                int n = arg1;
                for (int i = 0; i < n; i++)
                    for (int j = 0; j < nr_columns - 1; j++)
                        term.putChar(' ', j, y + i);
                term.setCursor(x, y);
                break;
            }
            case INDEX: { // move down
                if (y + 1 >= this.region_y2) {
                    // move down scrollRegion up:
                    term.scrollRegion(this.region_y1, this.region_y2, 1, true);
                } else {
                    y++;
                    term.setCursor(x, y);
                }
                break;
            }
            case NEXT_LINE: { // move down
                if (y + 1 >= this.region_y2) {
                    // move down scrollRegion up:
                    term.scrollRegion(this.region_y1, this.region_y2, 1, true);
                    term.setCursor(0, y);
                } else {
                    y++;
                    term.setCursor(0, y);
                }
                break;
            }
            case REVERSE_INDEX: { // move up
                if ((y - 1) < this.region_y1) {
                    // move up scrollRegion down:
                    term.scrollRegion(this.region_y1, this.region_y2, 1, false);
                } else {
                    y--;
                    term.setCursor(x, y);
                }
                break;
            }
            case INSERT_LINES: {
                //default: one
                int numlines = 1;

                if (arg1 > 0)
                    numlines = arg1 + 1;

                // insert at current position: scroll down:
                term.scrollRegion(y, this.region_y2, numlines, false);
                break;
            }
            case SET_CURSOR: {
                if (numIntegers > 0)
                    y = arg1 - 1;
                else
                    y = 0;

                if (numIntegers > 1)
                    x = arg2 - 1;
                else
                    x = 0;

                log.trace("SET_CURSOR:[{},{}]", x, y);
                term.setCursor(x, y);
                break;
            }
            case LINE_ERASE: {
                int mode = 0;
                if (numIntegers > 0)
                    mode = arg1;
                log.debug("LINE_ERASE: mode={}", mode);

                if (mode == 0) {
                    // cursor(inclusive) to end of line
                    term.clearArea(x, y, nr_columns, y + 1);
                } else if (mode == 1) {
                    // begin of line to cursor (inclusive)
                    term.clearArea(0, y, x + 1, y + 1);
                } else if (mode == 2) {
                    // complete line
                    term.clearArea(0, y, nr_columns, y + 1);
                } else {
                    log.error("LINE_ERASE: unsupported mode:{}", mode);
                }
                break;
            }
            case SCREEN_ERASE: {
                int mode = 2; // no arg = full screen ? (VI does this!)
                if (numIntegers > 0)
                    mode = arg1;

                if (mode == 0) {
                    // cursor(inclusive) to end screen
                    term.clearArea(x, y, nr_columns, y); // rest of line
                    term.clearArea(0, y + 1, nr_columns, nr_rows);
                } else if (mode == 1) {
                    // begin of screen to cursor (inclusive)
                    term.clearArea(0, 0, nr_columns, y);
                    term.clearArea(0, y, x + 1, y);
                } else if (mode == 2) {
                    // complete screen
                    term.clearArea(0, 0, nr_columns, nr_rows);
                    term.setCursor(0, 0); //reset cursor ?
                }
                break;
            }
            case SET_FONT_STYLE:
                handleSetFontStyle(term, tokenizer.args().numArgs(), tokenizer.args().ints());
                break;
            case DEC_SETMODE:
            case DEC_RESETMODE:
                boolean decValue = (token.compareTo(Token.DEC_SETMODE) == 0);
                handleDecMode(term, tokenizer.args().numArgs(), tokenizer.args().ints(), decValue);
                break;
            case SET_MODE:
            case RESET_MODE:
                boolean modeValue = (token.compareTo(Token.SET_MODE) == 0);
                handleSetResetMode(term, tokenizer.args().numArgs(), tokenizer.args().ints(), modeValue);
                break;
            case DEVICE_STATUS: {
                if (arg1 == 6) {
                    log.error("***Fixme: Request Cursor Report");
                    x = 120;
                    y = 30;

                    byte px1 = (byte) ('0' + (x / 10) % 10);
                    byte px2 = (byte) ('0' + x % 10);
                    byte py1 = (byte) ('0' + (y / 10) % 10);
                    byte py2 = (byte) ('0' + (y % 10));

                    byte[] sbytes = {(byte) CTRL_ESC, '[', py1, py2, ';', px1, px2, 'R'};

                    this.send(sbytes);
                } else {
                    log.warn("DEVICE_STATUS: Unknown device status mode:{}", arg1);
                }
                break;
            }
            case CHARSET_G0_UK:
                term.setCharSet(0, CharacterTerminal.VT_CHARSET_UK);
                break;
            case CHARSET_G1_UK:
                term.setCharSet(1, CharacterTerminal.VT_CHARSET_UK);
                break;
            case CHARSET_G0_US:
                term.setCharSet(0, CharacterTerminal.VT_CHARSET_US);
                break;
            case CHARSET_G1_US:
                term.setCharSet(1, CharacterTerminal.VT_CHARSET_US);
                break;
            case CHARSET_G0_GRAPHICS:
                term.setCharSet(0, CharacterTerminal.VT_CHARSET_GRAPHICS);
                break;
            case CHARSET_G1_GRAPHICS:
                term.setCharSet(1, CharacterTerminal.VT_CHARSET_GRAPHICS);
                break;
            case CHARSET_G0:
                term.setCharSet(0);
                break;
            case CHARSET_G1:
                term.setCharSet(1);
                break;
            case CHAR:
                // one or more characters: moves cursor !
                writeChar(bytes);
                break;
            case XGRAPHMODE: {
                // Graph mode
                // 1 short title
                // 2 long title
                int type = arg1;
                this.fireGraphModeEvent(type, tokenizer.args().strArg());
                //System.err.println("XGRAPH type="+type+"np="+token.np+","+token.nd+"-"+token.strArg);
                break;
            }
            case SEND_PRIMARY_DA:
                if (this.tokenizer.args().numArgs() > 0) {
                    FIXME("SEND_PRIMARY_DA: has argument(s):{}", arg1);
                }
                sendTermType();
                break;
            case SEND_SECONDARY_DA:
                if (this.tokenizer.args().numArgs() > 0) {
                    FIXME("SEND_SECONDARY_DA: has argument(s):{}", arg1);
                }
                sendTermType();
                break;
            case UNKNOWN:
            case ERROR:
                String seqstr = tokenizer.formattedBytesString(bytes);
                // vt100 specifies to write checkerboard char:
                // drawChar('▒');
                FIXME("Token error:{},{},sequence={}", token,
                        tokenizer.getText(encoding + ":"), seqstr);
                break;
            // rest:
            case ETX:
            case ENQ:
            case DC1:
            case DC2:
            default:
                FIXME("*** Unimplementation Token:{},args={}", token, tokenizer.getFormattedArguments());
                break;
        }// switch (token)
    }

    private void handleSetFontStyle(CharacterTerminal charTerm, int numIntegers, int[] integers) {
        int mode = 0;

        if (numIntegers == 0) {
            // empty = clear
            charTerm.setDrawStyle(0);
        } else {
            mode = integers[0];
            // Aboscure undocumented feature ?
            // 38 ; 5 ; Ps 	Set background color to Ps
            // 48 ; 5 ; Ps 	Set foreground color to Ps
            //
            if (((mode == 38) || (mode == 48)) && (numIntegers == 3)) {
                int ccode = tokenizer.args().ints(2);
                FIXME("Got multi color code:{}", ccode);

                if (mode == 38) {
                    charTerm.setDrawBackground(ccode);
                } else if (mode == 48) {
                    charTerm.setDrawForeground(ccode);
                }

            } else
                for (int i = 0; i < numIntegers; i++) {
                    mode = tokenizer.args().ints(i);

                    if (mode == 0)
                        charTerm.setDrawStyle(0); // reset
                    else if (mode == 1)
                        charTerm.addDrawStyle(StyleChar.STYLE_BOLD);
                    else if (mode == 4)
                        charTerm.addDrawStyle(StyleChar.STYLE_UNDERSCORE);
                    else if (mode == 5) {
                        // blink supported ?
                        charTerm.addDrawStyle(StyleChar.STYLE_BLINK);
                        charTerm.addDrawStyle(StyleChar.STYLE_UBERBOLD);
                    } else if (mode == 7)
                        charTerm.addDrawStyle(StyleChar.STYLE_INVERSE);
                    else if (mode == 8)
                        charTerm.addDrawStyle(StyleChar.STYLE_HIDDEN);
                    else if ((mode >= 30) && (mode <= 37))
                        charTerm.setDrawForeground(mode - 30);
                    else if ((mode >= 40) && (mode <= 47))
                        charTerm.setDrawBackground(mode - 40);
                    else if (mode == 39)
                        charTerm.setDrawBackground(-1);
                    else if (mode == 49)
                        charTerm.setDrawForeground(-1);

                }
        }
    }

    private void handleDecMode(CharacterTerminal charTerm, int numIntegers, int[] integers,
                               boolean value) {
        if (numIntegers == 0)
            return; //Reset all ?

        int mode = integers[0];

        switch (mode) {
            case 1:
                this.applicationCursorKeys = value;
                break;
            case 3: {
                if (value)
                    this.nr_columns = 132;
                else
                    this.nr_columns = 80;
                charTerm.setColumns(nr_columns);
                this.fireResizedEvent(nr_columns, nr_rows);
                break;
            }
            case 4:
                charTerm.setSlowScroll(value);
                break;
            case 7:
                log.debug("DECMODE:wraparound={}", value);
                charTerm.setWrapAround(value);
                break;
            case 12: // Start Blinking
                charTerm.setCursorOptions(value);
                break;
            case 25:
                charTerm.setEnableCursor(value);
                break;
            case 45:
                log.warn("Received unsupported DECMODE:Set Alt Screen={}", value);
                boolean result = charTerm.setAltScreenBuffer(value);
                if ((value == true) && (result == false))
                    FIXME("DECMODE: Alternative Text Buffer not supported by Character Terminal.");
                break;
            case 1034:
                // P s = 1034 → Interpret "meta" key, sets eighth bit. (enables the eightBitInput resource).
                FIXME("Metakey/8bit?{}", value);
                break;
            case 1048:
                if (value)
                    saveCursor();
                else
                    restoreCursor();
                break;
            case 1049: {
                // switch to als screen + use application cursor keys
                if (value)
                    saveCursor();
                else
                    restoreCursor();
                this.applicationCursorKeys = value;
                charTerm.setAltScreenBuffer(value);
                if (value)
                    charTerm.clearText();
                break;
            }
            default:
                FIXME("Unknown DEC mode:set({},{})", mode, value);
                break;
        }
    }

    private void handleSetResetMode(CharacterTerminal charTerm, int numIntegers, int[] integers,
                                    boolean value) {
        if (numIntegers == 0)
            return; //Reset all ?

        int mode = integers[0];

        switch (mode) {
            case 4:
                if (value == true) {
                    FIXME("INSERT (true=insert, false=replace):{}; ", value);
                }
                break;
            default:
                FIXME("Unknown SET/RESET mode:{}={}", mode, value);
                break;
        }
    }

    private void restoreCursor() {
        this.term.setCursor(savedCursorX, savedCursorY);
    }

    private void saveCursor() {
        savedCursorX = term.getCursorX();
        savedCursorY = term.getCursorY();
    }

    private void writeChar(byte[] bytes) {
        // let terminal do auto wrap around.
        term.writeChar(bytes);
    }

    public byte[] getKeyCode(String keystr) {
        byte[] bytes = KeyMappings.getKeyCode(termType, keystr.toUpperCase());
        if (bytes == null) {
            log.warn("Failed to find keycode:{}", keystr);
        }
        return bytes;
    }


    private void FIXME(String format, Object... args) {
        log.warn("***Fixme:" + format, args);
    }


    protected void checkIOException(Exception e, boolean sendException) {
        log.error("Exception", e);
        System.err.println("***Error:" + (sendException ? "SEND" : "RECEIVE") + "Exception:" + e);
        e.printStackTrace();
    }
}
