/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.api;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public interface TerminalKeyListener extends KeyListener {

    void keyPressed(KeyEvent e);

    void keyReleased(KeyEvent e);

    void keyTyped(KeyEvent e);

}
