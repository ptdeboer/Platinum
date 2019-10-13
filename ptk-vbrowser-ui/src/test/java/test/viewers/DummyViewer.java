package test.viewers;

import nl.esciencecenter.ptk.vbrowser.viewers.MimeViewer;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerContext;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerPlugin;
import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerListener;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class DummyViewer implements ViewerPlugin, MimeViewer {

    JPanel panel;

    JLabel label;

    @Override
    public String[] getMimeTypes() {
        return null;
    }

    @Override
    public Map<String, List<String>> getMimeMenuMethods() {
        return null;
    }

    @Override
    public void addViewerListener(ViewerListener listener) {
    }

    @Override
    public void removeViewerListener(ViewerListener listener) {
    }

    @Override
    public String getViewerName() {
        return "DummyViewer";
    }

    @Override
    public JComponent getViewerPanel() {
        return panel;
    }

    @Override
    public void initViewer(ViewerContext viewerContext) {
        {
            panel = new JPanel();
            panel.setPreferredSize(new Dimension(400, 400));
            panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
            panel.setLayout(new BorderLayout());
            {
                label = new JLabel();
                panel.add(label, BorderLayout.CENTER);
            }
        }
    }

    @Override
    public void startViewer(VRL vrl, String optMenuMethod) {
        label.setText("Started for:" + vrl);
        panel.revalidate();
    }

    @Override
    public void stopViewer() {
        label.setText("Stopped!");
    }

    @Override
    public void disposeViewer() {
    }

    @Override
    public boolean haveOwnScrollPane() {
        return false;
    }

    @Override
    public boolean isStandaloneViewer() {
        return false;
    }

}
