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

package nl.esciencecenter.ptk.vbrowser.viewers;

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import java.awt.*;

/**
 * Viewer Frame for stand alone Viewer Panels.
 */
public class ViewerFrame extends JFrame {

    // ===

    protected ViewerPlugin viewerPlugin;

    public ViewerFrame(ViewerPlugin viewer) {
        this.viewerPlugin = viewer;
        initGui();
    }

    protected void initGui() {
        Component viewerComponent = viewerPlugin.getViewerPanel();
        if (viewerComponent != null) {
            this.add(viewerComponent);
        }
    }

    public ViewerPlugin getViewer() {
        return viewerPlugin;
    }

    public void initViewer(ViewerContext context) {
        this.viewerPlugin.initViewer(context);
    }

    public void startViewer(VRL vrl, String optMenuMethod) throws VrsException {
        this.viewerPlugin.startViewer(vrl, optMenuMethod);
    }

    public void stopViewer() {
        this.viewerPlugin.stopViewer();
    }

    public void disposeViewer() {
        this.viewerPlugin.disposeViewer();
    }

}
