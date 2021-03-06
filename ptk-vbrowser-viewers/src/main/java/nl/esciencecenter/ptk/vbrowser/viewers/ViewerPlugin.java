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

import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerListener;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;

/**
 * Default interface for Viewer Plugins.<br>
 * All Browser Viewer plugins implement this interface. <br>
 * Some optional interfaces may be implemented as well for example the ToolPlugin or MimeViewer
 * plugin.
 */
public interface ViewerPlugin {
    /**
     * Register Viewer Event Listener for this viewer.
     */
    void addViewerListener(ViewerListener listener);

    /**
     * Remove registered Viewer Event Listener.
     */
    void removeViewerListener(ViewerListener listener);

    /**
     * Short name to display in Browser menu. Used for "View With ->" Sub Menu.
     */
    String getViewerName();

    /**
     * Bindings to get Actual Swing Component Object associated with this ViewerPlugin. A typical
     * Viewer can return itself, for example if a Swing Container (JPanel).
     *
     * @return Actual Swing Component to be embedded in browser, or StandAlone ViewerFrame.
     */
    JComponent getViewerPanel();

    /**
     * Init viewer and create UI Components, this method will be called <strong>during</strong> the
     * Swing Event Thread. <br>
     * Do not load contents yet. After initViewer(), startViewer() will be called with the actual
     * VRL to view.
     *
     * @param viewerContext - Context, is null when viewer is not registered or no browser view context is
     *                      available.
     */
    void initViewer(ViewerContext viewerContext);

    /**
     * Start actual viewer or update with new VRL and resume the viewer.<br>
     * This method may be called multiple times to either indicate an update or an method being
     * invoked. If called after a <code>stopViewer()</code> call, this viewer is expected to resume.
     *
     * @param vrl           - the VRL to view
     * @param optMenuMethod - optional method called by user through interactive menu, for example "Edit" or
     *                      "View".
     * @throws VrsException
     */
    void startViewer(VRL vrl, String optMenuMethod) throws VrsException;

    /**
     * Stop this viewer and suspend all (background) activity.<br>
     * Suspend all background processing, but do no dispose viewer. The startViewer() method may be
     * called to start this viewer again (with an updated VRL).
     */
    void stopViewer();

    /**
     * Dispose viewer and release resources. After this method <code>startViewer</code> and
     * <code>stopViewer</code> may not be called anymore.
     */
    void disposeViewer();

    // ==================================
    // Attribute/Configuration interface.
    // ==================================

    /**
     * @return true if, the viewer will manage it's own scrolling and panning.
     */
    boolean haveOwnScrollPane();

    /**
     * @return whether this viewer always must be started in stand-alone (=not embedded) mode. true
     * means do not embed viewer.
     */
    boolean isStandaloneViewer();

}
