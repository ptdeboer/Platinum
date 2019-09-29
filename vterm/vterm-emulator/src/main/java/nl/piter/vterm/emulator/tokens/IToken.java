package nl.piter.vterm.emulator.tokens;

import nl.piter.vterm.emulator.Tokens;

public interface IToken {

    /**
     * Single char, char sequence from Terminator or Prefix Sequence
     */
    char[] chars();

    String str();

    char[] prefix();

    char[] full();

    Tokens.Token token();

    Tokens.TokenOption option();

    Character terminator();

    String description();

}
