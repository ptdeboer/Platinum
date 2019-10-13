/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.ui.panel;

import nl.piter.vterm.api.EmulatorListener;

import java.awt.event.*;

public class VTermJFrameController implements WindowListener, ComponentListener, EmulatorListener,
        ActionListener {

    private VTermJFrame vTermJFrame;
    private String shortTitle;
    private String longTitle;

    public VTermJFrameController(VTermJFrame vTermJFrame) {
        this.vTermJFrame = vTermJFrame;
    }

    public void componentHidden(ComponentEvent e) {

    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        if (e.getSource() == vTermJFrame.getTerminalPanel()) {
            vTermJFrame.sendTermSize(vTermJFrame.getTerminalPanel().getColumnCount(), vTermJFrame.getTerminalPanel().getRowCount());
        }
    }

    public void componentShown(ComponentEvent e) {
        //vterm.charPane.startRenderers();
        //vterm.charPane.repaint();
    }

    public void notifyGraphMode(int type, String arg) {
        if (type == 1)
            this.shortTitle = arg;
        else
            this.longTitle = arg;

        if (shortTitle == null)
            shortTitle = "";

        if (longTitle == null)
            longTitle = "";

        vTermJFrame.setTitle("[" + shortTitle + "] " + longTitle);
    }

    public void notifyCharSet(String charSet) {
    }

    public void actionPerformed(ActionEvent e) {
        vTermJFrame.actionPerformed(e);
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
        //vterm.charPane.stopRenderers();
    }

    public void windowDeiconified(WindowEvent e) {
        vTermJFrame.getTerminalPanel().activate(); // charPane.startRenderers();
    }

    public void windowIconified(WindowEvent e) {
        vTermJFrame.getTerminalPanel().inactivate(); // charPane.stopRenderers();
    }

    public void windowOpened(WindowEvent e) {
        vTermJFrame.getTerminalPanel().activate(); // charPane.startRenderers();
    }

    public void notifyResized(int columns, int rows) {
        // charPane has already been resized: Update frame!
        vTermJFrame.updateFrameSize();
    }

}
