/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.ptk.vbrowser.viewers.internal;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.data.HashMapList;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.exec.LocalExec;
import nl.esciencecenter.ptk.vbrowser.viewers.MimeViewer;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerJPanel;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerPlugin;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

/**
 * Start MediaPlayer application.
 */
public class ExtMediaViewer extends ViewerJPanel implements ActionListener, ViewerPlugin,
        MimeViewer {
    private JTextPane mainTP;

    public ExtMediaViewer() {
    }

    // do not embed the viewer inside the VBrowser.
    @Override
    public boolean isStandaloneViewer() {
        return true;
    }

    @Override
    public String[] getMimeTypes() {
        return new String[]{"video/mp4"};
    }

    @Override
    public String getViewerName() {
        return "MediaPlayer";
    }

    public void initGUI() {
        FormLayout thisLayout = new FormLayout("5dlu, max(p;145dlu):grow, 5dlu",
                "max(p;5dlu), 24dlu, max(p;5dlu), 6dlu, max(p;15dlu), max(p;5dlu)");
        this.setLayout(thisLayout);
        this.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
        {
            mainTP = new JTextPane();
            this.add(mainTP, new CellConstraints("2, 2, 1, 2, default, default"));
            mainTP.setText("Starting MediaPlayer...");
            mainTP.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
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

    public void startURI(VRL loc) {
        try {
            debug("starting:" + loc);

            // Execute Plain VRL:
            executeCmd(loc);
            closeViewer();
        } catch (Throwable e) {
            notifyException("Failed to start mediaplayer for:" + loc, e);
        }
    }

    private void executeCmd(VRL loc) {
        try {
            String binHome = "";
            String cmdPath = binHome + "smplayer";

            if (GlobalProperties.isWindows())
                cmdPath += ".exe";

            String[] cmds = new String[2];
            cmds[0] = cmdPath;
            cmds[1] = loc.toString();
            String[] result = LocalExec.execute(cmds);

        } catch (Throwable e) {
            notifyException("Failed to start WebStarter for:" + loc, e);
        }
    }

    @Override
    public void doDisposeViewer() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        closeViewer();
    }

    @Override
    public Map<String, List<String>> getMimeMenuMethods() {
        String[] mimeTypes = getMimeTypes();

        // Use HashMapList to keep order of menu entries: first is default(!)

        Map<String, List<String>> mappings = new HashMapList<String, List<String>>();

        for (int i = 0; i < mimeTypes.length; i++) {
            List<String> list = new StringList("play:MediaPlayer");
            mappings.put(mimeTypes[i], list);
        }

        return mappings;
    }


    @Override
    public ViewerJPanel getViewerPanel() {
        return this;
    }
}
