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

package nl.esciencecenter.ptk.util.vterm;

import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.CTRL_BEL;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.CTRL_BS;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.CTRL_CAN;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.CTRL_CR;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.CTRL_ENQ;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.CTRL_EOT;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.CTRL_ESC;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.CTRL_ETX;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.CTRL_FF;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.CTRL_HT;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.CTRL_LF;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.CTRL_SI;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.CTRL_SO;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.CTRL_SUB;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.CTRL_VT;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token.BEEP;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token.BS;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token.CAN;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token.CHARSET_G0;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token.CHARSET_G1;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token.CR;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token.ENQ;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token.EOT;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token.ESC;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token.ETX;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token.FF;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token.HT;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token.LF;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token.SUB;
import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token.VT;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token;
import nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.TokenOption;

/**
 * Simple tokenizer class. issue nextToken() to parse inputStream. getBytes(); returns parse byte
 * sequence.
 * 
 * @author Piter T. de Boer.
 */

public class VT10xTokenizer {
    static PLogger logger;

    static {
        logger = PLogger.getLogger(VT10xTokenizer.class);
        //logger.setLevel(Level.FINEST); 
    }

    // token bytes buffer: 
    private static final int MAX_BYTES = 256;
    // typically unput buffer should not be bigger then 1. 
    private static int MAX_BUF = 1024;

    /** Mini Byte buffer can used as byte stack. */
    public static class MiniBuffer {
        // Use direct memory buffers?: ByteBuffer bytes = ByteBuffer.allocateDirect(200);

        byte bytes[] = null;
        int index = 0;

        public MiniBuffer(int size) {
            bytes = new byte[size];
        }

        public void put(byte b) {
            bytes[index] = b;
            index++;
        }

        public byte pop() throws IOException {
            if (index <= 0)
                throw new IOException("Byte Byffer is empty: can not pop");

            index--;
            return bytes[index];
        }

        /** eat current byte, if buffer is empty. Do nothing */
        public void eat() {
            if (index <= 0)
                return;

            index--;
        }

        public String toString(String encoding) throws UnsupportedEncodingException {
            return new String(bytes, 0, index, encoding);
        }

        // set index to 0; 
        public void reset() {
            index = 0;
        }

        /** Returns duplicate of byte buffer */
        public byte[] getBytes() {
            byte b2[] = new byte[index];

            System.arraycopy(bytes, 0, b2, 0, index);

            return b2;
        }

        public int size() {
            return index;
        }

        public int capacity() {
            return bytes.length;
        }

        public int freeSpace() {
            return bytes.length - index;
        }

        /** Auto casts integer to byte value. Uses lower 0x00ff value */
        public void put(int c) {
            put((byte) (c & 0x00ff));
        }

        public boolean isPrefixFor(String sequence) {
            // check: currrent size is to long to be a prefix 
            if (index > sequence.length())
                return false;

            for (int i = 0; i < index; i++)
                if (sequence.charAt(i) == bytes[i])
                    continue;
                else
                    return false;

            return true;
        }

        public int current() throws IOException {
            if (index <= 0)
                throw new IOException("Byte Byffer is empty: no current in buffer");
            return bytes[index - 1];
        }

    }

    // =======================================================================
    //
    // ======================================================================

    private InputStream inputStream = null;

    private int verbose = 1;

    boolean ansi_mode = false;

    private Emulator emulator;

    // ===
    // State of Tokenizer 
    // ===
    private Token currentToken;

    /** Buffer which hold current parsed byte sequence */
    MiniBuffer byteBuffer = new MiniBuffer(MAX_BYTES); // sequence parsed:  
    MiniBuffer putBuffer = new MiniBuffer(MAX_BYTES);
    private MiniBuffer patternBuffer = new MiniBuffer(MAX_BUF);

    private boolean stateKeepPatternBuffer = false;
    private boolean stateScanningTokenEscapePrefix = false;

    /** Token Arguments */
    private int integerList[] = new int[16];
    private int tokenNumIntegers;
    private int tokenDummyND;
    private String tokenStringArgument;
    private int tokenDummyNP;

    boolean formatDebug = false;

    VT10xTokenizer(Emulator emulator, InputStream inps) {
        // Debug optimimalization: do not create Debug String messages 
        // if they won't be printed anyway ! 
        formatDebug = (VTerm.verbose >= 2);
        this.emulator = emulator;
        this.inputStream = inps;
    }

    /**
     * Returns parsed bytes in duplicate byte array. hold bytes which are parsed after nextToken()
     * was called. A new nextToken() call will clear this sequence.
     */
    public byte[] getBytes() {
        return byteBuffer.getBytes();
    }

    public int readChar() throws IOException {
        int c = 0;

        if (byteBuffer.freeSpace() <= 0)
            throw new IOException("Token Buffer Overflow");

        if (putBuffer.size() > 0) {
            c = putBuffer.pop();
        } else
            c = inputStream.read();

        byteBuffer.put(c);

        return c;
    }

    public void ungetChar(int c) throws IOException {
        if (putBuffer.freeSpace() <= 0)
            throw new IOException("Token PutBuffer (unget) Overflow");
        putBuffer.put((byte) c);
        byteBuffer.pop();
    }

    /** Ad Hoc tokenizer, need to use proper scanner */

    public Token nextToken() throws IOException {
        VT10xTokenizer tokenizer = this;
        Object[][] _tokens = emulator.getTokenTable();

        // Clear State(s): 
        currentToken = null;
        boolean noNextChar = false;

        // ==============================================
        // C0 handling: has previous pattern in buffer ? 
        // ==============================================

        if (stateKeepPatternBuffer == false) {
            byteBuffer.reset();
            patternBuffer.reset();
            this.tokenNumIntegers = 0;
            this.tokenStringArgument = null;
            stateScanningTokenEscapePrefix = false; //whether current sequence is an esc prefix
        } else {
            //reset next time unless stated otherwise: 
            stateKeepPatternBuffer = false;

            // cotinue where you were 
        }

        // Central State Machine Parser 
        do {
            int c = tokenizer.readChar();

            // special case: 0 character -> ignore! 
            if (c == 0x00) {

                logger.warnPrintf("Warning:: character 0 at #%d", byteBuffer.size());
                //eat: 
                byteBuffer.eat();
                continue;
            }

            //===============================================================
            // From the Web: 
            //> Hmmm... I didn't realize LF was ignored in a CSI.  I had assumed
            //> that *all* characters (except flow control) were expected *literally*
            //> in these sequences!
            //
            //No, it's not at all ignored, nor is it treated as a literal.  If you
            //send, for example, the sequence "ESC [ LF C", then the terminal will
            //move the cursor down one line (scrolling if necessary) and then to the
            //right one position (stopping at the right margin if necessary). 

            // Put current in pattern buffer for matching 
            patternBuffer.put(c);

            // log character bugger only at finest logging level !
            if (logger.isLoggable(Level.FINEST))
                logger.debugPrintf(" + appending char:'%c'[0x%s]. Pattern=%s\n", (char) c,
                        byte2hexstr(c), this.formattedBytesString(patternBuffer.getBytes()));

            Token c0Token = getC0Token(c);
            if (c0Token != null) {
                currentToken = c0Token;

                // Optimilization check ESC directly: do not scan token buffer
                if (currentToken == Token.ESC) {
                    // Double ESC => Cancel Or Error!
                    if (this.stateScanningTokenEscapePrefix) {
                        match(Token.ERROR);
                    } else {
                        stateScanningTokenEscapePrefix = true;
                        continue;
                    }
                }

                // escape mode ? => match C0 Token
                if (stateScanningTokenEscapePrefix == false) {
                    return match(currentToken);
                }

                // C0 char while in Escape Sequence Mode !
                // keep current pattern, handle C0 first:
                logger.warnPrintf("*** Warning: Received C0 during ESCAPE Sequence:%s\n", c0Token);
                //currentToken=Token.ERROR; 

                if (logger.isLoggable(PLogger.DEBUG)) {
                    logger.debugPrintf("> Pattern=%s\n",
                            this.formattedBytesString(patternBuffer.getBytes()));
                    logger.debugPrintf("> Buffer=%s\n",
                            this.formattedBytesString(byteBuffer.getBytes()));
                }

                byteBuffer.eat(); // eat C0 

                // Just unget whole buffer and reset pattern !  
                {
                    while (byteBuffer.size() > 0)
                        ungetChar(byteBuffer.current());
                    patternBuffer.reset();
                }

                // or: return C0 token and keep pattern: in buffer
                // stateKeepPatternBuffer=true;// do not keep state: start over
                return match(currentToken);
            }

            // ================
            // check Char
            // ================

            if ((stateScanningTokenEscapePrefix == false) && isChar(c)) {
                currentToken = parseChar(c);
                if (currentToken != null) {
                    return match(currentToken);
                }
            }

            // =================
            // scan token table:
            // =================

            boolean hasMatch = false;

            // Scan tokens by linear matching: 
            for (int i = 0; i < _tokens.length; i++) {
                Object tokenDef[] = _tokens[i];

                String sequence = tokenDef[0].toString();
                Object tokObj = tokenDef[tokenDef.length - 1];
                TokenOption tokenOption = null;

                if (tokenDef[1] instanceof TokenOption)
                    tokenOption = (TokenOption) tokenDef[1];

                // simple linear checking: todo: pattern compiling and matching
                if (patternBuffer.isPrefixFor(sequence)) {
                    // PREFIX MATCH:
                    hasMatch = true;

                    if (patternBuffer.size() != sequence.getBytes().length) {
                        // No exact MATCH: continue parsing. 
                        continue;
                    }

                    logger.debugPrintf("Pre: Matching:%s\n", tokObj);

                    // MATCHED Sequence! 
                    if (tokObj instanceof Token) {
                        currentToken = (Token) tokObj;
                    } else // if (tokObj instanceof String)
                    {
                        // Unimplemented token: Check String definition. 
                        fixmePrintf("Tokenizer: Unknown or umimplemented token object:%d", tokObj);
                        if (tokenizer.tokenStringArgument == null)
                            tokenizer.tokenStringArgument = "";
                        tokenizer.tokenStringArgument = ">>> Token Object='" + tokObj + "'"
                                + tokenStringArgument;
                        return match(Token.UNKNOWN);
                    }

                    // ***
                    // Dirty tokenizer, scan integer list first then continue
                    // to find complete token matching against patternBuffer !
                    // ***
                    if (tokenOption == TokenOption.OPTION_PARSE_INTEGERS) {
                        // update integer list: might be empty ! 
                        tokenNumIntegers = parseIntegerList(integerList);
                    } else if ((tokenOption == TokenOption.OPTION_PARSE_GRAPHMODE)) {
                        parseGraphModeArguments();
                    }

                    // check terminating Token: ESC and _PREFIX Tokens are Non Terminating 
                    if ((currentToken != null) && (currentToken.isTerminator())) {
                        return match(currentToken);
                    }

                }
            }// for

            // update current state: 
            stateScanningTokenEscapePrefix = hasMatch;

            // Not a Token Nor A Char: ERROR 
            if (stateScanningTokenEscapePrefix == false) {
                // not a prefix, but have already parsed some bytes.
                logger.errorPrintf("Unexpected char at #%d:0x%s='%c'\n", byteBuffer.size(),
                        byte2hexstr(c), (char) c);

                logger.errorPrintf("Pattern Sequence=%s\n",
                        formattedBytesString(patternBuffer.getBytes()));
                logger.errorPrintf("Complete Sequence=%s\n",
                        formattedBytesString(byteBuffer.getBytes()));

                return match(Token.ERROR);
            }

        } while (true);

    }

    private Token getC0Token(int c) {
        if (c <= -1)
            return Token.EOF;

        switch (c) {
            case 0x00:
                return Token.NUL;
            case CTRL_ETX:
                return ETX;
            case CTRL_EOT:
                return EOT;
            case CTRL_ENQ:
                return ENQ;
            case CTRL_BS:
                return BS;
            case CTRL_HT:
                return HT;
            case CTRL_CR:
                return CR;
            case CTRL_LF:
                return LF;
            case CTRL_VT:
                return VT;
            case CTRL_FF:
                return FF;
            case CTRL_CAN:
                return CAN;
            case CTRL_SUB:
                return SUB;
            case CTRL_ESC:
                return ESC;
            case CTRL_BEL:
                return BEEP;
            case CTRL_SI:
                return CHARSET_G0;
            case CTRL_SO:
                return CHARSET_G1;
            default:
                if (c < 0x1f) {
                    logger.errorPrintf("Unknown C0 Character:#%d\n", c);
                    return Token.ERROR;
                }
                return null;
        }
    }

    private Token match(Token token) {
        if (logger.isLoggable(PLogger.DEBUG))
            ;
        logger.debugPrintf("MATCHED:%s,args=%s\n", currentToken, getFormattedArguments());

        this.currentToken = token;
        return token;
    }

    private Token parseChar(int c) throws IOException {
        Token token;

        if (isChar(c)) {
            // check utf-8 
            if (((c & 0x80) > 0) && (ansi_mode == false)) {
                // posible utf-8 sequence. Check utf-8 prefixes:

                int num = 1; //already have first byte 

                // utf-8 can exist of 6 bytes length (32bits encoded) 
                // binary prefix are:
                //  110xxxxx (c0) for 2 bytes
                //  1110xxxx (e0) for 3 bytes 
                //  11110xxx (fo) for 4 bytes 
                //  111110xx (f8) for 5 bytes 
                //  1111110x (fc) for 6 bytes 

                if ((c & 0xe0) == 0xc0) {
                    num = 2;
                } else if ((c & 0xf0) == 0xe0) {
                    num = 3;
                } else if ((c & 0xf8) == 0xf0) {
                    num = 4;
                } else if ((c & 0xfc) == 0xf8) {
                    num = 5;
                } else if ((c & 0xfd) == 0xfc) {
                    num = 6;
                }

                // read bytes as-is: 
                for (int i = 1; i < num; i++) {
                    byte b = readUByte(); // put into bute buffer

                    /*// escaped utf-8 char MUST have hight bit set. 
                    if ((buffer[index]&0x80)==0)
                    {
                       Error("UTF-8 Decoding error");
                     token=Token.ERROR; 
                     token.setText(text);
                     return token; 
                    }*/
                }
            }

            // Char sequence of one 
            token = Token.CHAR;
            //Debug(2,"CHARS='"+getText("UTF-8")+"'");
            return token;
        }

        return null;
    }

    /**
     * Read Unsigned Byte value: 0<= value <=255. This method does NOT return values < 0 ! If this
     * is the case an IOEception is thrown. This contrary to getChar(), which may return -1 in the
     * case of an EOF.
     * 
     * @return
     * @throws IOException
     */

    private byte readUByte() throws IOException {
        int c = readChar();

        if (c < 0)
            throw new IOException("EOF: End of stream");
        // cast to unsigned byte value: 
        return (byte) (c & 0x00ff);
    }

    private boolean isChar(int c) {
        if ((c >= 0x20) && (c < 0x7f)) {
            return true;
        } else if ((c >= 0x80) && (ansi_mode == false)) {
            return true;
        }

        return false;
    }

    String parseInt() throws IOException {
        String str = "";
        boolean cont = true;

        while (cont) {
            int digit = this.readChar();
            if (isDigit(digit)) {
                str += (char) digit;
                cont = true;
            } else {
                this.ungetChar(digit);
                cont = false;
            }
        }

        if (str.compareTo("") == 0)
            return null;

        return str;
    }

    String parseString() throws IOException {
        String str = "";
        boolean cont = true;

        while (cont) {
            int c = this.readChar();
            if (isPrintable(c)) {
                str += (char) c;
                cont = true;
            } else {
                this.ungetChar(c);
                cont = false;
            }
        }

        if (str.compareTo("") == 0)
            return null;

        return str;
    }

    /**
     * parse (optional) arguments: [ <INT> ] [ ; <INT> ]*
     */

    int parseIntegerList(int[] array) throws IOException {
        int numInts = 0;

        //  
        while (true) {
            String intstr = parseInt();

            if (intstr != null) {
                array[numInts++] = Integer.valueOf(intstr);
            }

            int digit = readChar();
            // System.out.print("#"+new
            // Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");

            if (digit == ';') {

                if (intstr == null) {
                    //allowed: 
                    //Debug("Empty integer value in Escape Sequence");
                    array[numInts++] = 0; // add null ! 
                } else {
                    // already added to array
                }
                continue; // parse next integer
            } else {
                // unkown char: put back and return list 
                ungetChar(digit);
                break; // end of argument list
            }
        }

        return numInts;
    }

    /** parse graph mode: <Int> <ND> <String> <NP> */
    void parseGraphModeArguments() throws IOException {
        String intstr = parseInt();

        integerList[0] = Integer.valueOf(intstr);
        tokenNumIntegers = 1;

        // ND: any non-digit char: 
        tokenDummyND = readChar();

        String strstr = parseString();
        tokenStringArgument = strstr;

        // NP: any non-printable char: 
        tokenDummyNP = readChar();
    }

    public int getNumIntegers() {
        return tokenNumIntegers;
    }

    public int getInteger(int index) {
        return integerList[index];
    }

    public String getStringArgument() {
        return this.tokenStringArgument;
    }

    private boolean isDigit(int digit) {
        return (('0' <= digit) && (digit <= '9'));
    }

    // allowed char set ? 
    private boolean isPrintable(int c) {
        return isChar(c);
    }

    /** return byte buffer as text using specified encoding */
    public String getText(String encoding) {
        try {
            return byteBuffer.toString(encoding);
        } catch (UnsupportedEncodingException e) {

            //Error("Exception:"+e); 
            //e.printStackTrace();
            return new String(byteBuffer.getBytes()); // defualt !
        }
    }

    public String getFormattedArguments() {
        String str = "";

        if (tokenNumIntegers > 0) {
            str = str + "(";

            for (int i = 0; i < this.tokenNumIntegers; i++) {
                str += this.integerList[i];
                if (i < tokenNumIntegers - 1)
                    str += ";";
            }
            // for graph mode 
            if (this.tokenStringArgument != null)
                str += ",'" + tokenStringArgument + "'";

            str = str + ")";
        }
        return str;
    }

    // === 
    // Misc
    // ===

    public String formattedBytesString(byte[] bytes) {
        return formattedBytesString(bytes, bytes.length);
    }

    // 
    public static String byte2hexstr(int val) {
        if (val < 0)
            return "<EOF>"; // -1= EOF; 

        String str = Integer.toHexString(val);
        if (str.length() < 2)
            return "0" + str; // pad with 0; 
        else
            return str;
    }

    public String formattedBytesString(byte[] bytes, int nrb) {
        String str = "{";

        for (int i = 0; i < nrb; i++) {
            char c = (char) bytes[i];

            str += byte2hexstr(c);
            if (i + 1 < nrb)
                str += ",";
        }
        str += "}=>{";
        for (int i = 0; i < nrb; i++) {
            char c = (char) bytes[i];
            str += getSymbolicCharString(c);
            if (i + 1 < nrb)
                str += ",";
        }

        str += "}";

        return str;
    }

    public static String getSymbolicCharString(char c) {
        if (c == 0)
            return VT10xEmulatorDefs.Token.NUL.toString();

        Object tokenDefs[][] = VT10xEmulatorDefs.getTokenTable();

        for (int i = 0; i < tokenDefs.length; i++) {
            Object tokdef[] = tokenDefs[i];
            Object token = tokdef[tokdef.length - 1];

            if (tokdef[0].toString().length() == 1) {
                if (tokdef[0].toString().charAt(0) == c) {
                    return "<" + token + ">";
                }
            }
        }

        if ((c >= ' ') && (c <= 'z'))
            return "'" + c + "'";

        return byte2hexstr(c);
    }

    public static void main(String args[]) {
        for (int i = 0; i < 255; i++) {
            System.out.println(" - " + i + "='" + getSymbolicCharString((char) i) + "'\n");
        }
    }

    /**
     * Returns storing nteger arguments array. Check numIntegers how many have been actually parsed
     * !
     */
    public int[] getIntegers() {
        return this.integerList;
    }

    private void fixmePrintf(String format, Object... args) {
        VT10xEmulator.logger.warnPrintf("FIXME:" + format, args);
    }

}