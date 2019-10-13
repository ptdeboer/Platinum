/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.emulator.tokens;

import lombok.ToString;
import nl.piter.vterm.emulator.Tokens;

/**
 * For single Char and CharSequences (String)
 * UTF8 transcoding: the actual encoding, UT8 or not, is preserved in the char array.
 */
@ToString
public class CharToken implements IToken {

    /**
     * Factory method.
     */
    public static IToken createFrom(char[] chars,
                                    Tokens.TokenOption option,
                                    Character terminatorCharm,
                                    Tokens.Token token,
                                    String tokenDescription) {
        // factory
        return new CharToken(chars, option, terminatorCharm, token, tokenDescription);
    }

    // --- //

    protected final char[] chars;
    protected final Tokens.Token token;
    protected final Tokens.TokenOption option;
    protected final String tokenDescription;
    protected final Character terminatorChar;
    // cached:
    private final char[] _fullSequence;
    protected String _str;

    public CharToken(char[] chars, Tokens.TokenOption tokenOption, Character terminatorChar, Tokens.Token token, String tokenDescription) {
        this.chars = chars;
        this.token = token;
        this.option = tokenOption;
        this.tokenDescription = tokenDescription;
        this.terminatorChar = terminatorChar;
        //
        if (terminatorChar == null) {
            this._fullSequence = chars;
        } else {
            this._fullSequence = (new String(chars) + terminatorChar).toCharArray();
        }
        this._str = new String(chars());
    }

    public char[] chars() {
        return chars;
    }

    public char[] prefix() {
        return this.chars;
    }

    public String str() {
        return _str;
    }

    public char[] full() {
        return this._fullSequence;
    }

    public Tokens.Token token() {
        return this.token;
    }

    public String description() {
        return this.tokenDescription;
    }

    @Override
    public Character terminator() {
        return terminatorChar;
    }

    @Override
    public Tokens.TokenOption option() {
        return option;
    }

}