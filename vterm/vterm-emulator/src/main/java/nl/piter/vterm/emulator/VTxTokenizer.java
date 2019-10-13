/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.emulator;

import lombok.extern.slf4j.Slf4j;
import nl.piter.vterm.emulator.Tokens.Token;
import nl.piter.vterm.emulator.Tokens.TokenOption;
import nl.piter.vterm.emulator.Util.MiniBuffer;
import nl.piter.vterm.emulator.tokens.IToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Simple tokenizer class. issue nextToken() to parse inputStream. getBytes(); returns parse byte
 * sequence.
 */
@Slf4j
public class VTxTokenizer {

    // token bytes buffer:
    private static final int MAX_BYTES = 256;

    // typically unput buffer should not be bigger then 1.
    private static int MAX_BUF = 1024;

    // =======================================================================
    //
    // ======================================================================

    private final VTxTokenDefs tokenDefs;

    boolean ansi_mode = false;

    /**
     * Buffer which hold current parsed byte sequence
     */
    MiniBuffer byteBuffer = new MiniBuffer(MAX_BYTES); // sequence parsed:
    MiniBuffer putBuffer = new MiniBuffer(MAX_BYTES);
    private InputStream inputStream;

    // ===
    // State of Tokenizer
    // ===

    private MiniBuffer patternBuffer = new MiniBuffer(MAX_BUF);

    /**
     * Token Arguments
     */
    public class Arguments {
        private int[] integerArgs = new int[16];
        private int numIntegerArgs;
        private int dummyND;
        private String stringArg;
        private int dummyNP;

        public int numArgs() {
            return numIntegerArgs;
        }

        public int arg1() {
            return integerArgs[0];
        }

        public int arg2() {
            return integerArgs[1];
        }

        public String strArg() {
            return stringArg;
        }

        public void reset() {
            this.numIntegerArgs = 0;
            for (int i = 0; i < integerArgs.length; i++) {
                this.integerArgs[i] = 0;
            }
            this.stringArg = null;
            this.dummyND = -1;
            this.dummyNP = -1;
        }

        public int[] ints() {
            int[] ints = new int[this.numIntegerArgs];
            System.arraycopy(this.integerArgs, 0, ints, 0, numIntegerArgs);
            return ints;
        }

        public int ints(int i) {
            if (i < this.numIntegerArgs) {
                return ints()[i];
            }
            return -1;
        }
    }

    public class State {
        protected boolean keepPatternBuffer;
        protected boolean scanningSequence;
        protected boolean optIntegersParsed;
        protected boolean optGraphModeParsed;
        protected int errorChar;
        protected Token matchedToken;

        public void reset() {
            this.keepPatternBuffer = false;
            this.scanningSequence = false;
            this.optIntegersParsed = false;
            this.optGraphModeParsed = false;
            this.errorChar = 0;
            this.matchedToken = null;
        }
    }

    private State state = new State();
    private Arguments arguments = new Arguments();

    public VTxTokenizer(InputStream inps) {
        this.inputStream = inps;
        this.tokenDefs = new VTxTokenDefs();
    }

    public Arguments args() {
        return this.arguments;
    }

    public String getSymbolicCharString(char c) {
        if (c == 0)
            return Tokens.Token.NUL.toString();

        Object[] tokenDef = this.tokenDefs.findCharToken(c);
        if (tokenDef != null) {
            Object token = tokenDef[tokenDef.length - 1];
            return "<" + token + ">";
        }

        if ((c >= ' ') && (c <= 'z')) {
            return "'" + c + "'";
        }

        return Util.byte2hexstr(c);
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
        if (putBuffer.freeSpace() <= 0) {
            throw new IOException("Token PutBuffer (unget) overflow: putBuffer is full.");
        }
        putBuffer.put((byte) c);
        byteBuffer.pop();
    }

    /**
     * Ad Hoc tokenizer, need to use proper scanner
     */

    public Token nextToken() throws IOException {

        VTxTokenizer tokenizer = this;

        // ==============================================
        // C0 handling: has previous pattern in buffer ?
        // ==============================================
        if (state.keepPatternBuffer == false) {
            byteBuffer.reset();
            patternBuffer.reset();
            this.arguments.numIntegerArgs = 0;
            this.arguments.stringArg = null;
            this.state.reset();
        } else {
            // Reset next time unless (re)stated otherwise:
            state.keepPatternBuffer = false;
        }

        // Central State Machine Parser
        do {
            int c = tokenizer.readChar();

            // special case: 0 character -> ignore!
            if (c == 0x00) {
                log.warn("Warning:: character 0 at #{}", byteBuffer.size());
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
            if (log.isTraceEnabled()) {
                log.info("+ appending [{}]'{}': buffer='{}'", String.format("%02x", c), (char) c, formattedBytesString(patternBuffer.getBytes()));
            }

            Token c0Token = getC0Token(c);
            if (c0Token != null) {

                // Optimilization check ESC directly: do not scan token buffer
                if (c0Token == Token.ESC) {
                    // Double ESC => Cancel Or Error!
                    if (this.state.scanningSequence) {
                        return match(Token.ERROR);
                    } else {
                        state.scanningSequence = true;
                        continue;
                    }
                }

                // escape mode ? => match C0 Token
                if (state.scanningSequence == false) {
                    return match(c0Token);
                }

                // C0 char while in Escape Sequence Mode !
                // keep current pattern, handle C0 first:
                log.warn("*** Received C0 during ESCAPE Sequence:'{}'", c0Token);
                //currentToken=Token.ERROR;

                if (log.isDebugEnabled()) {
                    log.debug("> Pattern={}", this.formattedBytesString(patternBuffer.getBytes()));
                    log.debug("> Buffer ={}", this.formattedBytesString(byteBuffer.getBytes()));
                }

                byteBuffer.eat(); // eat C0

                // Just unget whole buffer and reset pattern !
                {
                    while (byteBuffer.size() > 0)
                        ungetChar(byteBuffer.current());
                    patternBuffer.reset();
                }

                // or: return C0 token and keep pattern: in buffer
                // state.keepPatternBuffer=true;// do not keep state: start over
                return match(c0Token);
            }

            // ================
            // check Char
            // ================

            if ((state.scanningSequence == false) && isChar(c)) {
                Token charToken = parseChar(c);
                if (charToken != null) {
                    return match(charToken);
                }
            }

            // =================
            // sequences
            // =================
            boolean fullMatch = false;
            boolean prefixMatch = false;
            boolean partialPrefixMatch = false;

            // (I)
            IToken tokenDef = this.tokenDefs.findMatch(patternBuffer.bytes, patternBuffer.index);
            fullMatch = (tokenDef != null);

            // (II)
            if (!fullMatch) {
                tokenDef = this.tokenDefs.findPrefix(patternBuffer.bytes, patternBuffer.index);
                prefixMatch = (tokenDef != null);
            }

            Token sequenceToken = null;

            // (III)
            if (tokenDef == null) {
                // nill
                List<IToken> result = this.tokenDefs.findPartialPrefix(patternBuffer.bytes, patternBuffer.index);
                if (result.size() > 0) {
                    this.state.scanningSequence = true;
                    partialPrefixMatch = true;
                    tokenDef = result.get(0);
                }
            }

            // TODO token with description text i.s.o actual token
            if (!(tokenDef.token() instanceof Token)) {
                log.warn("*** Skipping unknown token object:'{}'", tokenDef);
                if (tokenizer.args().stringArg == null) {
                    tokenizer.args().stringArg = "";
                }
                tokenizer.args().stringArg = ">>> Token Object='" + tokenDef + "'"
                        + args().stringArg;
            } else {
                // supported token:
                sequenceToken = tokenDef.token();
            }

            log.trace(">>> fullmatch     :'{}'", fullMatch);
            log.trace(">>> prefixMatch   :'{}'", prefixMatch);
            log.trace(">>> partialPrefix :'{}'", partialPrefixMatch);

            if (sequenceToken != null) {
                log.trace(">>> sequenceToken :'{}':'{}'", sequenceToken, formattedBytesString(new String(tokenDef.chars()).getBytes()));
            } else {
                log.trace(">>> sequenceToken :NULL");
            }

            // Need better state matcher!!!
            if (partialPrefixMatch) {
                // Still in 'prefix' mode. No exact prefix nor full match.
                state.scanningSequence = partialPrefixMatch;
                continue;
            }

            if (fullMatch || prefixMatch) {

                // **
                // Dirty tokenizer, scan integer list first then continue
                // to find complete token matching against patternBuffer !
                // ***

                if (prefixMatch && (tokenDef.option() == TokenOption.OPTION_INTEGERS)) {
                    // Careful: LOOKAHEAD
                    if (isDigit(lookahead())) {
                        arguments.numIntegerArgs = parseIntegerList(arguments.integerArgs);
                        this.state.optIntegersParsed = (arguments.numIntegerArgs > 0);
                    }
                    log.trace(">>> state.optIntegersParsed:'{}'", state.optIntegersParsed);
                }

                if ((tokenDef.option() == TokenOption.OPTION_GRAPHMODE)) {
                    if (isDigit(lookahead())) {
                        this.state.optGraphModeParsed = parseGraphModeArguments();
                    }
                    log.trace(">>> state.optGraphModeParsed:'{}'", state.optGraphModeParsed);
                }
                // else more options parsing ?

                if (sequenceToken != null) {
                    // Prefix sequence match: stop
                    if (sequenceToken.isTerminator() == false) {
                        // Old version, but keep for detection.
                        log.warn("***FIXME Detected prefix token indicator:{}" + sequenceToken);

                        fullMatch = false;
                        prefixMatch = true;
                    }

                    if (fullMatch) {
                        log.debug("- pattern sequence={}", formattedBytesString(patternBuffer.getBytes()));
                        log.debug("FULL CHARS_TOKEN MATCH:{}", sequenceToken);
                        return match(sequenceToken);
                    }
                }
            }

            // Fall trough;
            this.state.errorChar = c;
        } while (state.scanningSequence);

        // === ERROR FALL THROUGH ===
        // Not a Token Nor A Char: ERROR
        log.error("- error pattern Sequence={}", formattedBytesString(patternBuffer.getBytes()));
        log.error("- error complete Sequence={}", formattedBytesString(byteBuffer.getBytes()));
        // not a prefix, but have already parsed some bytes.
        log.error("***Unexpected char at #{}:0x{}='{}'\n", byteBuffer.size(), Util.byte2hexstr(state.errorChar), (char) state.errorChar);
        return match(Token.ERROR);

    }

    private Token getC0Token(int c) {
        if (c <= -1)
            return Token.EOF;

        switch (c) {
            case 0x00:
                return Token.NUL;
            case VTxTokenDefs.CTRL_ETX:
                return Token.ETX;
            case VTxTokenDefs.CTRL_EOT:
                return Token.EOT;
            case VTxTokenDefs.CTRL_ENQ:
                return Token.ENQ;
            case VTxTokenDefs.CTRL_BS:
                return Token.BS;
            case VTxTokenDefs.CTRL_HT:
                return Token.HT;
            case VTxTokenDefs.CTRL_CR:
                return Token.CR;
            case VTxTokenDefs.CTRL_LF:
                return Token.LF;
            case VTxTokenDefs.CTRL_VT:
                return Token.VT;
            case VTxTokenDefs.CTRL_FF:
                return Token.FF;
            case VTxTokenDefs.CTRL_CAN:
                return Token.CAN;
            case VTxTokenDefs.CTRL_SUB:
                return Token.SUB;
            case VTxTokenDefs.CTRL_ESC:
                return Token.ESC;
            case VTxTokenDefs.CTRL_BEL:
                return Token.BEEP;
            case VTxTokenDefs.CTRL_SI:
                return Token.CHARSET_G0;
            case VTxTokenDefs.CTRL_SO:
                return Token.CHARSET_G1;
            default:
                if (c < 0x1f) {
                    log.error("Unknown C0 Character:#{}\n", c);
                    return Token.ERROR;
                }
                return null;
        }
    }

    private final Logger vtermTokenRecorder= LoggerFactory.getLogger("VTERM-RECORDER");

    private Token match(Token token) {

        vtermTokenRecorder.debug("{x},{},[{},'{}'])",
                patternBuffer.getBytes(),
                token, args().ints(),
                (args().stringArg != null ? args().stringArg : ""));

        this.state.matchedToken = token;
        if (log.isTraceEnabled()) {
            log.trace("MATCHED:{},args={}", this.state.matchedToken, getFormattedArguments());
        }
        return token;
    }

    private Token parseChar(int c) throws IOException {
        Token token;

        if (!isChar(c)) {
            return null;
        }

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

            byte[] utfBytes = new byte[num];
            utfBytes[0] = (byte) c;

            // read bytes as-is:
            for (int i = 1; i < num; i++) {
                utfBytes[i] = readUByte(); // put into byte buffer

                    /*// escaped utf-8 char MUST have hight bit set.
                    if ((buffer[index]&0x80)==0)
                    {
                       Error("UTF-8 Decoding error");
                     token=Token.ERROR;
                     token.setText(text);
                     return token;
                    }*/
            }

            String utf8 = new String(utfBytes, StandardCharsets.UTF_8);
            log.info("Is this UTf8? :'{}'", utf8);

        }

        // Char sequence of one
        token = Token.CHAR;
        //Debug(2,"CHARS='"+getText("UTF-8")+"'");
        return token;
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
        // cast unsigned byte value:
        return (byte) (c & 0x00ff);
    }

    private boolean isChar(int c) {
        if ((c >= 0x20) && (c < 0x7f)) {
            return true;
        } else return (c >= 0x80) && (ansi_mode == false);

    }

    private String parseInt() throws IOException {
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

    private String parseString() throws IOException {
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
     * Read one char put it into the read ahead buffer
     * and return it.
     */
    private int lookahead() throws IOException {
        int c = this.readChar();
        this.ungetChar(c);
        return c;
    }

    /**
     * parse (optional) arguments: [ <INT> ] [ ; <INT> ]*
     */

    private int parseIntegerList(int[] array) throws IOException {
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
        log.debug("- parseIntegerList():nr of int:{}", numInts);

        return numInts;
    }

    /**
     * parse graph mode: <Int> <ND> <String> <NP>
     */
    private boolean parseGraphModeArguments() throws IOException {
        String intstr = parseInt();
        if (intstr == null) {
            return false;
        }
        arguments.integerArgs[0] = Integer.valueOf(intstr);
        arguments.numIntegerArgs = 1;

        // ND: any non-digit char, typically ';'.
        arguments.dummyND = readChar();

        String argStr = parseString();
        arguments.stringArg = argStr;

        // NP: any non-printable char: typically BEEP (\007)
        arguments.dummyNP = readChar();
        return true;
    }

    private boolean isDigit(int digit) {
        return (('0' <= digit) && (digit <= '9'));
    }

    // allowed char set ?
    private boolean isPrintable(int c) {
        return isChar(c);
    }

    // ===
    // Misc
    // ===

    /**
     * return byte buffer as text using specified encoding
     */
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
        String str = "[";

        if (args().numIntegerArgs > 0) {
            for (int i = 0; i < args().numIntegerArgs; i++) {
                str += args().integerArgs[i];
                if (i < args().numIntegerArgs - 1)
                    str += ";";
            }
            // for graph mode
            if (args().stringArg != null)
                str += ",'" + args().stringArg + "'";

        }
        str = str + "]";
        return str;
    }

    public String formattedBytesString(byte[] bytes) {
        return formattedBytesString(bytes, bytes.length);
    }

    public String formattedBytesString(byte[] bytes, int nrb) {
        String str = "{";

        for (int i = 0; i < nrb; i++) {
            char c = (char) bytes[i];

            str += Util.byte2hexstr(c);
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


}