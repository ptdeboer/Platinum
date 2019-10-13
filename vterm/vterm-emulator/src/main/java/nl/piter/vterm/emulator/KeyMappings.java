/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.emulator;

import lombok.extern.slf4j.Slf4j;

import static nl.piter.vterm.emulator.Tokens.Token.*;

/**
 * Match symbolic (AWT) Key Code String to actual Control Sequence to send.<br>
 * Optional prefixed with TERM Type for Terminal specific control sequences.
 */
@Slf4j
public class KeyMappings {

    public static String[][] keyMappings = {
            //Default key mapping. Applies for most VT100/XTerms
            {"F1", VTxTokenDefs.CTRL_ESC + "OP"}, //
            {"F2", VTxTokenDefs.CTRL_ESC + "OQ"}, //
            {"F3", VTxTokenDefs.CTRL_ESC + "OR"}, //
            {"F4", VTxTokenDefs.CTRL_ESC + "OS"}, //
            {"F5", VTxTokenDefs.CTRL_ESC + "Ot"}, //
            {"F6", VTxTokenDefs.CTRL_ESC + "Ou"}, //
            {"F7", VTxTokenDefs.CTRL_ESC + "Ov"}, //
            {"F8", VTxTokenDefs.CTRL_ESC + "OI"}, //
            {"F9", VTxTokenDefs.CTRL_ESC + "Ow"}, //
            {"F10", VTxTokenDefs.CTRL_ESC + "Ox"},//
            {"PAGE_UP", VTxTokenDefs.CTRL_ESC + "[5~"},  //
            {"PAGE_DOWN", VTxTokenDefs.CTRL_ESC + "[6~"},//
            {"INSERT", VTxTokenDefs.CTRL_ESC + "[2~"},   //
            {"DELETE", VTxTokenDefs.CTRL_ESC + "[3~"},   //
            {"PAGE_UP", VTxTokenDefs.CTRL_ESC + "[5~"},  //
            {"PAGE_DOWN", VTxTokenDefs.CTRL_ESC + "[6~"},//
            {"ENTER", VTxTokenDefs.CTRL_CR + ""},        //
            {"BACKSPACE", VTxTokenDefs.CTRL_BS + "",},  //
            {"TAB", VTxTokenDefs.CTRL_HT + ""},
            {"" + UP, VTxTokenDefs.CTRL_ESC + "OA"},
            {"" + DOWN, VTxTokenDefs.CTRL_ESC + "OB"},
            {"" + RIGHT, VTxTokenDefs.CTRL_ESC + "OC"},
            {"" + LEFT, VTxTokenDefs.CTRL_ESC + "OD"},
            // Terminal specifics. of other then TERM_XTERM/VT100 default: specify here:
            {"XTERM_" + UP, VTxTokenDefs.CTRL_ESC + "[A"},   //
            {"XTERM_" + DOWN, VTxTokenDefs.CTRL_ESC + "[B"}, //
            {"XTERM_" + RIGHT, VTxTokenDefs.CTRL_ESC + "[C"},//
            {"XTERM_" + LEFT, VTxTokenDefs.CTRL_ESC + "[D"}, //
            {"XTERM_F1", VTxTokenDefs.CTRL_ESC + "OP"}, //
            {"XTERM_F2", VTxTokenDefs.CTRL_ESC + "OQ"}, //
            {"XTERM_F3", VTxTokenDefs.CTRL_ESC + "OR"}, //
            {"XTERM_F4", VTxTokenDefs.CTRL_ESC + "OS"}, //
            {"XTERM_F5", VTxTokenDefs.CTRL_ESC + "Ot"}, //
            {"XTERM_F6", VTxTokenDefs.CTRL_ESC + "Ou"}, //
            {"XTERM_F7", VTxTokenDefs.CTRL_ESC + "Ov"}, //
            {"XTERM_F8", VTxTokenDefs.CTRL_ESC + "OI"}, //
            {"XTERM_F9", VTxTokenDefs.CTRL_ESC + "Ow"}, //
            {"XTMER_F10", VTxTokenDefs.CTRL_ESC + "Ox"},//
            {"VT100_" + UP, VTxTokenDefs.CTRL_ESC + "OA"},   //
            {"VT100_" + DOWN, VTxTokenDefs.CTRL_ESC + "OB"}, //
            {"VT100_" + RIGHT, VTxTokenDefs.CTRL_ESC + "OC"},//
            {"VT100_" + LEFT, VTxTokenDefs.CTRL_ESC + "OD"}, //
            {"APP_" + UP, VTxTokenDefs.CTRL_ESC + "OA"},     //
            {"APP_" + DOWN, VTxTokenDefs.CTRL_ESC + "OB"},   //
            {"APP_" + RIGHT, VTxTokenDefs.CTRL_ESC + "OC"},  //
            {"APP_" + LEFT, VTxTokenDefs.CTRL_ESC + "OD"},   //
            {"VT52_" + UP, VTxTokenDefs.CTRL_ESC + "A"},     //
            {"VT52_" + DOWN, VTxTokenDefs.CTRL_ESC + "B"},   //
            {"VT52_" + RIGHT, VTxTokenDefs.CTRL_ESC + "C"},  //
            {"VT52_" + LEFT, VTxTokenDefs.CTRL_ESC + "D"},   //
            {"VT52_F1", VTxTokenDefs.CTRL_ESC + "P"},        //
            {"VT52_F2", VTxTokenDefs.CTRL_ESC + "Q"},        //
            {"VT52_F3", VTxTokenDefs.CTRL_ESC + "R"},        //
            {"VT52_F4", VTxTokenDefs.CTRL_ESC + "S"},        //
            {"HOME", VTxTokenDefs.CTRL_ESC + "[H"},
            {"END", VTxTokenDefs.CTRL_ESC + "[F"}
            //
    };

    /**
     * Look in token table to match key/token string and return escape sequence
     *
     * @param termType
     * @param keystr
     * @return
     */
    public static byte[] getKeyCode(String termType, String keystr) {
        if (termType != null) {
            String termKeystr = (termType + "_" + keystr);

            for (int i = 0; i < keyMappings.length; i++)
                if (keyMappings[i][0].compareToIgnoreCase(termKeystr) == 0) {
                    log.error("Mapping keystr '{}' -> '{}'", keystr, keyMappings[i][1]);
                    return keyMappings[i][1].getBytes();
                }

        }

        // default:
        for (int i = 0; i < keyMappings.length; i++)
            if (keyMappings[i][0].compareToIgnoreCase(keystr) == 0) {
                log.error("Mapping keystr '{}' -> '{}'", keystr, keyMappings[i][1]);
                return keyMappings[i][1].getBytes();
            }

        return null;
    }
}
