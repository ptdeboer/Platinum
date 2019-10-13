/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.api;

public interface EmulatorListener {

    /**
     * When an Xterm Graph Mode update was received
     */
    void notifyGraphMode(int type, String arg);

    /**
     * When the CharacterSet has changed
     */
    void notifyCharSet(String charSet);

    /**
     * Is send AFTER the terminal has been resized. CharPanel already has updated it size
     */
    void notifyResized(int columns, int rows);

}
