/*
 * (C) Piter.nl
 */
// source:

package nl.esciencecenter.ptk.vbrowser.viewers.internal;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.data.HashMapList;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.io.IOUtil;
import nl.esciencecenter.ptk.vbrowser.viewers.MimeViewer;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerJPanel;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerPlugin;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

/**
 * Start WebResource application.
 */
public class WebResourceViewer extends ViewerJPanel implements ActionListener, ViewerPlugin,
        MimeViewer {

    private JTextPane mainTP;
    private JTextArea infoTA;
    private JTextField urlTF;
    private JPanel contenPnl;

    public WebResourceViewer() {
    }

    // do not embed the viewer inside the VBrowser.
    @Override
    public boolean isStandaloneViewer() {
        return false;
    }

    public String[] getMimeTypes() {
        return new String[]{"text/html"};
    }

    @Override
    public String getViewerName() {
        return "WebResourceViewer";
    }

    public void initGUI() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
        {
            mainTP = new JTextPane();
            this.add(mainTP, BorderLayout.CENTER);
            mainTP.setLayout(new BorderLayout());
            mainTP.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
            {
                contenPnl = new JPanel();
                contenPnl.setLayout(new BorderLayout());
                mainTP.add(contenPnl, BorderLayout.CENTER);
                {
                    {
                        urlTF = new JTextField();
                        contenPnl.add(urlTF, BorderLayout.NORTH);
                    }
                    {
                        infoTA = new JTextArea();
                        contenPnl.add(infoTA, BorderLayout.CENTER);
                    }
                }
            }
        }
    }

    @Override
    public void doInitViewer() {
        initGUI();
    }

    @Override
    public void doStopViewer() {
    }

    @Override
    public void doUpdate(VRL loc) {
        startURI(loc);
    }

    @Override
    public void doStartViewer(VRL vrl, String optionalMethod) {
        startURI(vrl);
    }

    public void startURI(VRL vrl) {
        try {
            debug("starting:" + vrl);
            this.urlTF.setText("url:"+vrl.toString());
            String text=getResourceHandler().readText(vrl,"UTF8");
            this.infoTA.setText(text);
        } catch (Throwable e) {
            notifyException("Exception:"+e,e);
        }
    }

    @Override
    public void doDisposeViewer() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // closeViewer();
    }

    @Override
    public Map<String, List<String>> getMimeMenuMethods() {
        String[] mimeTypes = getMimeTypes();

        // Use HashMapList to keep order of menu entries: first is default(!)

        Map<String, List<String>> mappings = new HashMapList<String, List<String>>();

        for (int i = 0; i < mimeTypes.length; i++) {
            List<String> list = new StringList("view:WebResourceViewer");
            mappings.put(mimeTypes[i], list);
        }

        return mappings;
    }


    @Override
    public ViewerJPanel getViewerPanel() {
        return this;
    }
}
