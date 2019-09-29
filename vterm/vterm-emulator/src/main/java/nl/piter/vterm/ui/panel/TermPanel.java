package nl.piter.vterm.ui.panel;

import nl.piter.vterm.api.CharacterTerminal;
import nl.piter.vterm.emulator.Emulator;
import nl.piter.vterm.ui.charpane.CharPane;
import nl.piter.vterm.ui.charpane.ColorMap;
import nl.piter.vterm.ui.fonts.FontInfo;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * TermPanel which contains and manages a CharPane
 */
public class TermPanel extends JPanel implements ComponentListener {

    public final static String[] splashText = {
            "  ***   VTerm VT100+/xterm Emulator ***  ",
            " ***        (C) Piter.NL             *** ",
            "  ***     Author Piter T. de Boer   ***  ",};

    private CharPane charPane;
    private EmulatorKeyMapper keyMapper;
    private Emulator emulator;

    //private boolean mustStop=false;

    public void initGUI() {
        // JPanel 
        this.setBorder(new BevelBorder(BevelBorder.RAISED));
        this.enableEvents(AWTEvent.KEY_EVENT_MASK);
        // unset focus, must get TAB chars: 
        this.setFocusTraversalKeysEnabled(false);
        this.setLayout(new BorderLayout());

        // CharPane:
        {
            charPane = new CharPane();
            this.add(charPane, BorderLayout.CENTER);
        }

        this.setBackground(Color.BLACK);

        //Listeners: 
        {
            charPane.addComponentListener(this);
            this.addComponentListener(this);
        }

        // this.addKeyListener(this); 
    }

    // ===
    // Component Listener 
    // ===

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {

    }

    public void componentResized(ComponentEvent e) {
        // this component or parent has been resized/ 
        // the layout manager resize the charpane, so
        // now the internal text buffers have to be updated. 

        if (e.getSource().equals(charPane) == false)
            return;

        // get actual component size which is not the text buffer image size ! 
        Dimension size = charPane.getSize();

        // update internal character buffer size: 
        charPane.resizeTextBuffersToAWTSize(size);
    }

    public void componentShown(ComponentEvent e) {
    }

    // ===
    // TermPanel !
    // === 

    public int getColumnCount() {
        return charPane.getColumnCount();
    }

    public int getRowCount() {
        return charPane.getRowCount();
    }

    public FontInfo getFontInfo() {
        return charPane.getFontInfo();
    }

    public void dispose() {
        terminate();

        if (charPane != null)
            charPane.dispose();
        charPane = null;

        //this.mustStop=true;
        this.keyMapper = null;
        this.emulator = null;
    }

    public void terminate() {
        inactivate();

        if (emulator != null) {
            emulator.signalTerminate();

            // unregister keymapper:
            if (keyMapper != null) {
                this.removeKeyListener(keyMapper);
            }
        }
    }

    public void updateFontSize(Integer val, boolean resetGraphics) {
        this.charPane.setFontSize(val);
        if (resetGraphics)
            ;
        charPane.resetGraphics();
    }

    public void updateColorMap(ColorMap colorMap, boolean resetGraphics) {
        charPane.setColorMap(colorMap);
        if (resetGraphics)
            ;
        charPane.resetGraphics();
    }

    public void repaintGraphics(boolean splash) {
        charPane.resetGraphics();

        if (splash)
            this.drawSplash();
    }

    void drawSplash() {
        if (splashText != null) {
            int offx = 20;
            int offy = 8;

            for (int y = 0; y < splashText.length; y++) {
                String line = splashText[y];
                charPane.putString(line, offx, offy + y);
            }
        }
        charPane.paintTextBuffer();
        charPane.repaint();
    }

    public void updateFontType(String type, boolean resetGraphics) {
        charPane.setFontType(type);
        if (resetGraphics)
            charPane.resetGraphics();
    }

    /**
     * Returns Character Terminal
     */
    public CharacterTerminal getCharacterTerminal() {
        return this.charPane;
    }

    public void drawTestScreen() {
        charPane.drawTestScreen();
    }

    public void clearAll() {
        this.charPane.clear();
    }

    public boolean getSynchronizedScrolling() {
        String str = this.charPane.getOption(CharPane.OPTION_ALWAYS_SYNCHRONIZED_SCROLLING);

        if (str != null)
            return Boolean.parseBoolean(str);

        return false;
    }

    public void setSynchronizedScrolling(boolean val) {
        charPane.setOption(CharPane.OPTION_ALWAYS_SYNCHRONIZED_SCROLLING, "" + val);
    }

    public void reset() {
        charPane.reset();
    }

    public void activate() {
        charPane.startRenderers();
    }

    public void inactivate() {
        charPane.stopRenderers();
    }

    public void resetGraphics() {
        charPane.resetGraphics();
    }

    public Emulator getEmulator() {
        return this.emulator;
    }

    /**
     * Set Emulator but do not start anything
     */
    public void setEmulator(Emulator emulator) {
        //this.mustStop=false;
        this.emulator = emulator;
        this.keyMapper = new EmulatorKeyMapper(emulator);
        this.addKeyListener(keyMapper);
    }

}
