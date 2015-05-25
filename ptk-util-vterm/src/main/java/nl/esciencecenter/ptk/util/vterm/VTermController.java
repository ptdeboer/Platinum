package nl.esciencecenter.ptk.util.vterm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class VTermController implements WindowListener, ComponentListener, EmulatorListener,
        ActionListener {
    /**
     * 
     */
    private final VTerm vTerm;

    private String shortTitle;

    private String longTitle;

    public VTermController(VTerm vTerm) {
        this.vTerm = vTerm;
    }

    public void componentHidden(ComponentEvent e) {

    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        if (e.getSource() == this.vTerm.terminalPanel) {
            this.vTerm.sendTermSize(this.vTerm.terminalPanel.getColumnCount(), this.vTerm.terminalPanel.getRowCount());
        }
    }

    public void componentShown(ComponentEvent e) {
        //vlTerm.charPane.startRenderers(); 
        //vlTerm.charPane.repaint(); 
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

        // System.err.println(">>> NR="+type+" => "+token.strArg);  
        this.vTerm.setTitle("[" + shortTitle + "] " + longTitle);
    }

    public void notifyCharSet(String charSet) {
    }

    public void actionPerformed(ActionEvent e) {
        this.vTerm.actionPerformed(e);
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
        //vlTerm.charPane.stopRenderers(); 
    }

    public void windowDeiconified(WindowEvent e) {
        this.vTerm.terminalPanel.activate(); // charPane.startRenderers(); 
    }

    public void windowIconified(WindowEvent e) {
        this.vTerm.terminalPanel.inactivate(); // charPane.stopRenderers(); 
    }

    public void windowOpened(WindowEvent e) {
        this.vTerm.terminalPanel.activate(); // charPane.startRenderers(); 
    }

    public void notifyResized(int columns, int rows) {
        // charPane has already been resized: Update frame!
        this.vTerm.updateFrameSize();
    }

}