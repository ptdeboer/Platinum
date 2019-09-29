package nl.esciencecenter.ptk.vbrowser.ui;

import nl.esciencecenter.ptk.vbrowser.ui.tool.vtermstarter.VTermStarter;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import java.awt.*;

public class VTermStarterDev {

    public static void main(String args[]) {
        JFrame frame = new JFrame();
        VTermStarter vtermStart = new VTermStarter();
        frame.setLayout(new BorderLayout());
        frame.add(vtermStart, BorderLayout.CENTER);
        vtermStart.doInitViewer();

        frame.pack();
        frame.setVisible(true);

        try {
            vtermStart.startViewer(new VRL("file",null, "/tmp"),null);
        } catch (VrsException e) {
            e.printStackTrace();
        }

    }

}
