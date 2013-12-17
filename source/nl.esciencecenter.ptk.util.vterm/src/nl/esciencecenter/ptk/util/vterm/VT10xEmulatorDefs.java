/*
 * Copyright 2006-2010 Virtual Laboratory for e-Science (www.vl-e.nl)
 * Copyright 2012-2013 Netherlands eScience Center.
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

import static nl.esciencecenter.ptk.util.vterm.VT10xEmulatorDefs.Token.*;
 
public class VT10xEmulatorDefs
{
	/* VT102 Control characters (octal)
    000 = Null (fill character)
    003 = ETX (Can be selected half-duplex turnaround char)
    004 = EOT (Can be turnaround or disconnect char, if turn, then DLE-EOT=disc.)
    005 = ENQ (Transmits answerback message)
    007 = BEL (Generates bell tone)
    010 = BS  (Moves cursor left)
    011 = HT  (Moves cursor to next tab)
    012 = LF  (Linefeed or New line operation)
    013 = VT  (Processed as LF)
    014 = FF  (Processed as LF, can be selected turnaround char)
    015 = CR  (Moves cursor to left margin, can be turnaround char)
    016 = SO  (Selects G1 charset)
    017 = SI  (Selects G0 charset)
    021 = DC1 (XON, causes terminal to continue transmit)
    023 = DC3 (XOFF, causes terminal to stop transmitting)
    030 = CAN (Cancels escape sequence)
    032 = SUB (Processed as CAN)
    033 = ESC (Processed as sequence indicator)
    */ 

	final public static char CTRL_NUL=0x00; // Ignored on input; not stored in buffer
	final public static char CTRL_ETX=0x03; // CTRL-C: 
	final public static char CTRL_EOT=0x04; // CTRL-D: End Of Transission ? 
	//
	final public static char CTRL_ENQ=0x05; // CTRL_E Transmit ANSWERBACK message
	final public static char CTRL_BEL=0x07; // CTRL-G: BEEEEEEEEEEEEEEEEP
	final public static char CTRL_BS =0x08; // CTRL-H: Backspace
	final public static char CTRL_HT =0x09; // CTRL-I: next stabstop
	final public static char CTRL_LF =0x0a; // CTRL-J: line feed/new line (depends on line feed mode)
	final public static char CTRL_VT =0x0b; // => LF : line feed/new line
	final public static char CTRL_FF =0x0c; // => LF : line feed/new line
	final public static char CTRL_CR =0x0d; // CTRL-M: Cariage Return
	//
	final public static char CTRL_SO =0x0e; // CTRL-N: G1 character set  
	final public static char CTRL_SI =0x0f; // CTRL-O: G0 character set 
	// 
	final public static char CTRL_XON =0x11; // CTRL-S: XON (only XON/XOFF are allowed) 
	final public static char CTRL_XOFF =0x13;// CTRL-Q: XOFF (turn off XON/XOFF mode)
	//
	final public static char CTRL_CAN =0x18; // Abort CTRL sequence, output ERROR Char  
	final public static char CTRL_SUB =0x1a; // Same as CAN  
	final public static char CTRL_ESC =0x1b; // New Escape Sequence  (aborts previous)
	final public static char CTRL_DEL =0x7f; // Ignored  

	// 8 bit characters:                     // <name> (7 bit equivalent-> doesn't match)   
	final public static char IND = 0x84;   // Index (ESC D)
	final public static char NEL = 0x85;   // Next Line  = ESC E
	final public static char HTS = 0x88;   // Tab Set ESC H
	final public static char RI  = 0x8d;   // 
	final public static char SS2 = 0x8e;   // 
	final public static char SS3 = 0x8f;   //
	final public static char DCS = 0x90;   //
	final public static char SPA = 0x96;   // 
	final public static char EPA = 0x97;   // 
	final public static char SOS = 0x98;   // 
	final public static char DECID = 0x9a; //
	final public static char CSI = 0x9b;   //
	final public static char ST = 0x9c;    // 
	final public static char OSC = 0x9d;   // 
	final public static char PM = 0x9e;    //
	final public static char APC = 0x9f;   // 
	
	final public static String CTRL_CSI_PREFIX=CTRL_ESC+"[";

	final public static String TERM_VT100="vt100"; 

	final public static String TERM_VT52="vt52";

	final public static String TERM_XTERM="xterm"; 
	

	public static enum Token implements IToken
	{
		// === 
		// Single CTRL to tokens 
		// ===
		
		EOF,    // -1 
		NUL,ETX,EOT,EOD,ENQ,BEEP,BS,HT,
		LF,VT,FF,CR,
		DC1,DC2,CAN,SUB,ESC(false),DEL,CHARSET_G0,CHARSET_G1,

		//=== 
		//Double Char Escape: ESC+CHAR:
		//===
		SAVE_CURSOR,RESTORE_CURSOR, APPLICATION_KEYPAD,
		NUMERIC_KEYPAD,INDEX,NEXT_LINE,TAB_SET,REVERSE_INDEX,

		// ===
		// PREFIX // start of Esc+"[" sequence
		// === 
		CSI_SEQ_PREFIX(false),
		PRIVDEC_SEQ_PREFIX(false), // DEC private \E[? escape ?
		REQ_SECONDARY_DA_PREFIX(false), // alternate Device Request ! 
		// Xterm graph mode
		XGRAPHMODE,
		
		// === ptdeboer
		// Escape Sequences \E[ & \E[?
		// === 
		// screen manupilation:
		SET_REGION,SET_CURSOR,SET_CURSORX,SET_FONT_STYLE,SET_MODE,RESET_MODE,
		LINE_ERASE,SCREEN_ERASE,
		SET_COLUMN,DEL_CHAR,ERASE_CHARS,SET_ROW,
		INSERT_BLANK_CHARS, PRECEDING_LINE, FORWARD_TABS, INSERT_LINES, 
		SCROLL_UP, BACKWARD_TABS, SCROLL_DOWN_OR_MOUSETRACK, DELETE_LINES,

		// Character sequence
		CHAR, // sequence of one or more UTF-8 characters !
		// TermGlobal.error 

		// Key tokens (send only): 
		F1,F2,F3,F4,F5,F6,F7,F8,F9,F10, 
		// movement tokens (Send only) (more are added as plain text, not as tokens)
		UP,DOWN,LEFT,RIGHT,//ENTER,TAB,BACKSPACE,
		// Misc/VT52 tokens (not complete)
		REQ_IDENTIFY,
		REQ_IDENTIFY2,
		VT52_ANSI_MODE,
		IDENTIFY_AS_VT102,
		DEVICE_STATUS,

		// ====================
		// Dec Privates ?
		// ======================
		DEC_SETMODE,DEC_RESETMODE, 
		CHARSET_GO_UK, CHARSET_GO_US,CHARSET_GO_GRAPHICS, CHARSET_GO_ALT_ROM_NORMAL, CHARSET_GO_ALT_ROM_SPECIAL,
		CHARSET_G1_UK, CHARSET_G1_US,CHARSET_G1_GRAPHICS, CHARSET_G1_ALT_ROM_NORMAL, CHARSET_G1_ALT_ROM_SPECIAL,
		DEC_SCREEN_ALIGNMENT,
		//
		EXIT_VT52_MODE,
		REQ_UNKNOWN, // Request for device -> not implemented or unknown. 
		UNKNOWN, // really unknown. 
		ERROR // ERROR sequence, tokenizer holds raw character sequence (in utf-8)
	      ;
		
		private boolean isTerminator=true; 
		
		private Token(boolean terminator)
		{
			isTerminator=terminator; // is false  for prefix sequences 
		}
		
		private Token()
		{
			isTerminator=true; 
		}
		
		public boolean isTerminator()
		{
			return isTerminator; 
		}
		
	 }

	public static enum TokenOption
	{
		OPTION_PARSE_INTEGERS,
		OPTION_PARSE_GRAPHMODE; 
	}
	/**
	 * Simple token table. 
	 * Store TOKEN as string together whith char sequence (as string).
	 * This list is searched linear so that the first match is used. 
	 */
	private static Object tokenDefs[][] = 
	{
		// <PREFIX>,<POSTFIX>,<TOKENSTTRING> 

		// ==================================================
		// Single Char Tokens
		// ==================================================
		// cannot put nul char into table: { CTRL_NUL,null,NUL },   // Warning: 0x00 => Empty String
		{ CTRL_ETX,null,ETX },   
		{ CTRL_EOT,null,EOT }, 
		{ CTRL_ENQ,null,ENQ }, 
		{ CTRL_BS, null,BS },
		{ CTRL_HT, null,HT },
		{ CTRL_CR, null,CR },
		{ CTRL_LF, null,LF },
		{ CTRL_VT, null,VT },
		{ CTRL_FF, null,FF },
		{ CTRL_CAN,null,CAN },
		{ CTRL_SUB,null,SUB },
		{ CTRL_ESC,null,ESC },	// Carefull ESC is NOT Terminating TOKEN ! 

		{ CTRL_BEL+"",null,BEEP },
	
		// charsets:
		{ CTRL_SI+"", null,CHARSET_G0 },
		{ CTRL_SO+"", null,CHARSET_G1 },

		//=================================================
		// Double Char Escape codes
		//=================================================
		{ CTRL_ESC + "7", null,SAVE_CURSOR },
		{ CTRL_ESC + "8", null,RESTORE_CURSOR },
		{ CTRL_ESC + "=", null,APPLICATION_KEYPAD },
		{ CTRL_ESC + ">", null,NUMERIC_KEYPAD },
		{ CTRL_ESC + "<", null,EXIT_VT52_MODE },

		{ CTRL_ESC + "D", null,INDEX },
		{ CTRL_ESC + "E", null,NEXT_LINE },

		{ CTRL_ESC + "H", null,TAB_SET },
		{ CTRL_ESC + "M", null,REVERSE_INDEX }, // DELETE_LINE
		{ CTRL_ESC + "Z", null,REQ_IDENTIFY },

		// select G0 character set:
		{ CTRL_ESC + "(A", null,CHARSET_GO_UK },
		{ CTRL_ESC + "(B", null,CHARSET_GO_US },
		{ CTRL_ESC + "(0", null,CHARSET_GO_GRAPHICS },
		{ CTRL_ESC + "(1", null,CHARSET_GO_ALT_ROM_NORMAL },
		{ CTRL_ESC + "(2", null,CHARSET_GO_ALT_ROM_SPECIAL },
		// select G1 character set:
		{ CTRL_ESC + ")A", null,CHARSET_G1_UK },
		{ CTRL_ESC + ")B", null,CHARSET_G1_US },
		{ CTRL_ESC + ")0", null,CHARSET_G1_GRAPHICS },
		{ CTRL_ESC + ")1", null,CHARSET_G1_ALT_ROM_NORMAL },
		{ CTRL_ESC + ")2", null,CHARSET_G1_ALT_ROM_SPECIAL },
		
		// Not implemented but add filter and detect them anyway 
		{ CTRL_ESC+ " F", null,"7 Bits Controls" }, 
		{ CTRL_ESC+ " G", null,"8 Bits Controls" }, 
		{ CTRL_ESC+ " L", null,"Set ANSI conformance level 1 - vt100"},
		{ CTRL_ESC+ " M", null,"Set ANSI conformance level 2 - vt200"},
		{ CTRL_ESC+ " N", null,"Set ANSI conformance level 3 - vt300"},
		
		{ CTRL_ESC+ "#3", null,"DEC Double Heigh, top half"},
		{ CTRL_ESC+ "#4", null,"DEC Double Heigh, bottom half"},
		{ CTRL_ESC+ "#5", null,"DEC single width line"},
		{ CTRL_ESC+ "#6", null,"DEC double width line"},
		{ CTRL_ESC+ "#8", null,DEC_SCREEN_ALIGNMENT}, // "DEC Screen aligment Test"},
		
		//=================================================
		// CSI Escape Sequences "^[[" Or: <ESC> '['
		//=================================================
		
		// important: Prefix must be first Escape+[ token in token list !
		// so that the prefix is matched first in nextToken() ! 
		
		// XTERM: <ESC<  ']' Ps ND <String> NP          
		{ CTRL_ESC + "]", TokenOption.OPTION_PARSE_GRAPHMODE, XGRAPHMODE },

		
		{ CTRL_CSI_PREFIX,        TokenOption.OPTION_PARSE_INTEGERS, CSI_SEQ_PREFIX },
		{ CTRL_CSI_PREFIX + ">",  TokenOption.OPTION_PARSE_INTEGERS, REQ_SECONDARY_DA_PREFIX },
	
		// \[? after \[ !
		//{ CTRL_CSI_PREFIX + ">c",null,REQ_UNKNOWN },	?

		{ CTRL_CSI_PREFIX + "?",  TokenOption.OPTION_PARSE_INTEGERS,PRIVDEC_SEQ_PREFIX },
		{ CTRL_CSI_PREFIX + "?h", null,DEC_SETMODE },
		{ CTRL_CSI_PREFIX + "?l", null,DEC_RESETMODE },
		

		// ESCAPE SEQUENCES, all
		// \E[ excape prefix, integer list is optional (is parsed inside
		// dirty Tokinezer after "\E["

		// extra XTERM codes: 
		{ CTRL_CSI_PREFIX + "@", null,INSERT_BLANK_CHARS },   // \E[K
		// Cursors: 
		{ CTRL_CSI_PREFIX + "A", null, UP }, // vt100/xterm cursor control
		{ CTRL_CSI_PREFIX + "B", null, DOWN }, //vt100/xterm cursor control 
		{ CTRL_CSI_PREFIX + "C", null, RIGHT }, //vt100/xterm cursor control
		{ CTRL_CSI_PREFIX + "D", null, LEFT }, //vt100/xterm cursor control
 
		{ CTRL_CSI_PREFIX + "E", null,NEXT_LINE },
		{ CTRL_CSI_PREFIX + "F", null,PRECEDING_LINE },

		{ CTRL_CSI_PREFIX + "G", null,SET_COLUMN },
		{ CTRL_CSI_PREFIX + "H", null,SET_CURSOR },   // \E[<y>;<x>H
		{ CTRL_CSI_PREFIX + "I", null,FORWARD_TABS },   // \E[<y>;<x>H
		{ CTRL_CSI_PREFIX + "J", null,SCREEN_ERASE }, // \E[K
		{ CTRL_CSI_PREFIX + "K", null,LINE_ERASE },   // \E[K
		{ CTRL_CSI_PREFIX + "L", null,INSERT_LINES },   // \E[K
		{ CTRL_CSI_PREFIX + "M", null,DELETE_LINES },   // \E[K
		{ CTRL_CSI_PREFIX + "P", null,DEL_CHAR },

		{ CTRL_CSI_PREFIX + "S", null,SCROLL_UP },
		// one integer=scroll down, 5 integers=mouse track
		{ CTRL_CSI_PREFIX + "T", null,SCROLL_DOWN_OR_MOUSETRACK }, 
		{ CTRL_CSI_PREFIX + "X", null,ERASE_CHARS },
		{ CTRL_CSI_PREFIX + "Z", null,BACKWARD_TABS},
		
		{ CTRL_CSI_PREFIX + "c", null,REQ_IDENTIFY },
		{ CTRL_CSI_PREFIX + "d", null,SET_ROW },
		{ CTRL_CSI_PREFIX + "f", null,SET_CURSOR }, // double -> see \E[H

		{ CTRL_CSI_PREFIX + "h", null,SET_MODE }, // \E[<c>;..;<c>h
		{ CTRL_CSI_PREFIX + "l", null,RESET_MODE }, // \E[<c>;..;<c>l
		{ CTRL_CSI_PREFIX + "m", null,SET_FONT_STYLE }, // \E[<c>;..;<c>m
		{ CTRL_CSI_PREFIX + "n", null,DEVICE_STATUS },
		{ CTRL_CSI_PREFIX + "r", null,SET_REGION }, // \E[<y1>;<y2>r
		{ CTRL_CSI_PREFIX + "t", null,"Window-Manipulation" }, // \E[<y1>;<y2>r
		// Led Control: has integer beteen '[' and 'q'. 
		{ CTRL_CSI_PREFIX + "q", null,"DEC_LED" }, // [0q,[1q,[2q,[3q,[4q
		// DECTEST: has integer between '['  and 'y'
		{ CTRL_CSI_PREFIX + "y", null,"DEC_TEST" }, // [0q,[1q,[2q,[3q,[4q
		
		// secondary request: can have parameters:  '>' <Ps> 'c'. 
		{ CTRL_CSI_PREFIX + ">c", null,REQ_IDENTIFY2 }, 
		// {"ö" , "GREMLIN"},

		// RESPONSE CODES: 
		// VT102 specific 
		{ CTRL_ESC + "[?6c", null,IDENTIFY_AS_VT102 },
	};	
	
	/**
	 * Match symbolic (AWT) Key Code String to actual Control Sequence to send.
	 * Optional prefixed with TERM Type: 
	 */ 
	public static String keyMappings[][]=
		{
			//Default key mapping. Applies for most VT100/XTerms 
		
			{ CTRL_ESC + "OP", "F1" },
			{ CTRL_ESC + "OQ", "F2" },
			{ CTRL_ESC + "OR", "F3" },
			{ CTRL_ESC + "OS", "F4" },
			{ CTRL_ESC + "Ot", "F5" },
			{ CTRL_ESC + "Ou", "F6" },
			{ CTRL_ESC + "Ov", "F7" },
			{ CTRL_ESC + "OI", "F8" },
			{ CTRL_ESC + "Ow", "F9" },
			{ CTRL_ESC + "Ox", "F10" },

			{ CTRL_ESC + "[5~","PAGE_UP" },
			{ CTRL_ESC + "[6~","PAGE_DOWN" },

			{ CTRL_ESC + "[2~","INSERT" },
			{ CTRL_ESC + "[3~","DELETE" },
			
			{ CTRL_ESC + "[5~","PAGE_UP" },
			{ CTRL_ESC + "[6~","PAGE_DOWN" },

			{ CTRL_ESC + "[2~","INSERT" },
			{ CTRL_ESC + "[3~","DELETE" },
	
			{ CTRL_CR+"",  "ENTER" },
			{ CTRL_BS+"",  "BACKSPACE" },
			{ CTRL_HT+"",  "TAB" },
			
			{ CTRL_ESC + "OA",""+UP },
			{ CTRL_ESC + "OB",""+DOWN },
			{ CTRL_ESC + "OC",""+RIGHT },
			{ CTRL_ESC + "OD",""+LEFT },
			
			// Terminal specifics. of other then XTERM/VT100 default: specify here: 
			
			{ CTRL_ESC + "[A","XTERM_"+UP },
			{ CTRL_ESC + "[B","XTERM_"+DOWN },
			{ CTRL_ESC + "[C","XTERM_"+RIGHT },
			{ CTRL_ESC + "[D","XTERM_"+LEFT },

			{ CTRL_ESC + "OA","VT100_"+UP },
			{ CTRL_ESC + "OB","VT100_"+DOWN },
			{ CTRL_ESC + "OC","VT100_"+RIGHT },
			{ CTRL_ESC + "OD","VT100_"+LEFT },

			{ CTRL_ESC + "OA", "APP_" + UP },
			{ CTRL_ESC + "OB", "APP_" + DOWN },
			{ CTRL_ESC + "OC", "APP_" + RIGHT },
			{ CTRL_ESC + "OD", "APP_" + LEFT },
		
			{ CTRL_ESC + "A", "VT52_" + UP },
			{ CTRL_ESC + "B", "VT52_" + DOWN },
			{ CTRL_ESC + "C", "VT52_" + RIGHT },
			{ CTRL_ESC + "D", "VT52_" + LEFT },
			
			{ CTRL_ESC + "P", "VT52_F1" },
			{ CTRL_ESC + "Q", "VT52_F2" },
			{ CTRL_ESC + "R", "VT52_F3" },
			{ CTRL_ESC + "S", "VT52_F4" },

		};
	
	/**
     * Look in token table to match key/token string 
     * and return escape sequence 
     * 
     * @param keystr
	 * @param keystr2 
     * @return
     */
    public static byte[] getKeyCode(String termType, String keystr) 
    {
    	if (termType!=null)
    	{
    		String termKeystr=(termType+"_"+keystr);
    	
    		for (int i=0;i<keyMappings.length;i++)
    			if (keyMappings[i][1].compareToIgnoreCase(termKeystr)==0)
    				return keyMappings[i][0].getBytes();
    	}
    	
    	// default: 
    	for (int i=0;i<keyMappings.length;i++)
    		if (keyMappings[i][1].compareToIgnoreCase(keystr)==0)
    			return keyMappings[i][0].getBytes();

    	return null; 
    }
    
  
    
    public static Object[][] getTokenTable()
    {
        return tokenDefs; 
    }
}
