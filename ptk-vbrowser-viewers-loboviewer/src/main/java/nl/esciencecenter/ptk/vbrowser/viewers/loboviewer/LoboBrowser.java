/*
 * Copyright 2006-2010 Virtual Laboratory for e-Science (www.vl-e.nl)
 * Copyright 2012-2013 Netherlands eScience Center.
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

package nl.esciencecenter.ptk.vbrowser.viewers.loboviewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import nl.esciencecenter.ptk.ssl.CertUI;
import nl.esciencecenter.ptk.ssl.CertificateStore.CaCertOptions;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.viewers.EmbeddedViewer;
import nl.esciencecenter.ptk.vbrowser.viewers.ToolPlugin;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerContext;
import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerEvent;
import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerEventType;
import nl.esciencecenter.ptk.vbrowser.viewers.vrs.ViewerResourceLoader;
import nl.esciencecenter.vbrowser.vrs.VRS;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsIOException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class LoboBrowser extends EmbeddedViewer implements ToolPlugin
{
    private static final long serialVersionUID = 3652578919344065278L;

    private static final ClassLogger logger = ClassLogger.getLogger(LoboBrowser.class);

    static String mimetypes[] = {
            "text/html"
    };

    // === //

    private LoboBrowserPanel loboBrowserPanel;

    private LoboPanelController loboController;

    public void initGUI(ViewerContext context)
    {
        loboController = new LoboPanelController(this);

        {
            setLayout(new BorderLayout());
            add(getLoboBrowserPanel(), BorderLayout.CENTER);
            if (context.getStartedAsStandalone())
            {
                this.setPreferredSize(new Dimension(800, 600));
            }
        }
    }

    private LoboBrowserPanel getLoboBrowserPanel()
    {
        if (loboBrowserPanel == null)
        {
            this.loboController = new LoboPanelController(this);
            loboBrowserPanel = new LoboBrowserPanel(loboController, super.isStartedAsStandalone());
        }

        return loboBrowserPanel;
    }

    @Override
    public String[] getMimeTypes()
    {
        return mimetypes;
    }

    @Override
    public String getViewerName()
    {
        return "LoboBrowser";
    }

    @Override
    protected void doInitViewer()
    {
        initGUI(this.getViewerContext());
    }

    @Override
    public void doStopViewer()
    {
        loboBrowserPanel.stop();
    }

    @Override
    public void doDisposeViewer()
    {
        loboBrowserPanel.dispose();
    }

    @Override
    public void doStartViewer(VRL location, String method) throws VrsException
    {
        doUpdate(location);
    }

    @Override
    protected void doUpdate(VRL location) throws VrsException
    {
        debugPrintf("Update location:%s\n", location);

        try
        {
            if (location.hasScheme(VRS.HTTPS_SCHEME))
            {
                CaCertOptions options = new CaCertOptions();
                options.storeAccepted = true;

                ViewerResourceLoader handler = this.getResourceHandler();
                CertUI.interactiveImportCertificate(handler.getCertificateStore(), location.getHostname(), location.getPort(), options);
            }
        }
        catch (Exception e1)
        {
            throw new VrsIOException("SSLException:" + e1.getMessage(), e1);
        }

        try
        {
            // Really navigate:
            loboBrowserPanel.doNavigate(location);
        }
        catch (MalformedURLException e)
        {
            throw new VRLSyntaxException(e.getMessage(), e);
        }

    }

    // Browser panel manages it's own scroll pane.
    public boolean haveOwnScrollPane()
    {
        return true;
    }

    protected void debugPrintf(String format, Object... args)
    {
        ClassLogger.getLogger(LoboBrowser.class).debugPrintf(format, args);
    }

    public boolean checkFollow(VRL vrl) throws VrsException
    {
        // check whether lobo browser should follow this url or
        // that the MasterBrowser should follow the url otherwise.

        ViewerResourceLoader handler = this.getResourceHandler();
        // todo: more efficient way:
        String mimeType = handler.getMimeTypeOf(vrl);

        boolean val = super.isMyMimeType(mimeType);

        debugPrintf("checkFollow:%s=%s\n", vrl, val);
        return val;
    }

    public String getVersion()
    {
        return "LoboBrowser plugin version 2.1";
    }

    public String getAbout()
    {
        return "<html><body><center>"
                + "<table width=400>"
                + "<tr bgcolor=#c0c0c0><td> <h3>LoboBrowser ViewerPlugin</h3></td></tr>"
                + "<tr><td></td></tr>"
                + "<tr><td> The LoboBrowser plugin uses the Lobo Toolkit <br><p> </td></tr>"
                + "<tr><td></td></tr>"
                + "<tr><td> See: <a href=\"http://www.lobobrowser.org/\">www.lobobrowser.org</a></td></tr>"
                + "</body></html>";
    }

    // ===========================
    // MimeViewer methods
    // ===========================

    @Override
    public Map<String, List<String>> getMimeMenuMethods()
    {
        return null;
    }

    // =========================
    // Tool Plugin Interface
    // =========================

    @Override
    public String getToolName()
    {
        return this.getViewerName();
    }

    @Override
    public boolean addToToolMenu()
    {
        return true;
    }

    @Override
    public String[] getToolMenuPath()
    {
        return new String[] {
                "browsers"
        };
    }

    @Override
    public String toolBarName()
    {
        return "browsers";
    }

    @Override
    public String defaultToolMethod()
    {
        return "openLocation";
    }

    @Override
    public Icon getToolIcon(int size)
    {
        return null;
    }

    // =========================
    // Hyper Link Events
    // =========================

    protected void fireHyperLinkEvent(java.net.URL url)
    {
        try
        {
            fireHyperLinkEvent(new VRL(url), false);
        }
        catch (Exception e)
        {
            handle("Invalid URL:" + url, e);
        }
    }

    public void fireHyperLinkEvent(VRL vrl, boolean openNew)
    {
        if (openNew)
        {
            fireEvent(ViewerEvent.createHyperLinkEvent(this, ViewerEventType.HYPERLINK_OPEN_NEW, vrl));
        }
        else
        {
            fireEvent(ViewerEvent.createHyperLinkEvent(this, ViewerEventType.HYPERLINK_EVENT, vrl));
        }
    }

    public void fireLinkFollowedEvent(VRL vrl)
    {
        fireEvent(ViewerEvent.createHyperLinkEvent(this, ViewerEventType.HYPERLINK_LINK_FOLLOWED, vrl));
    }

    public void fireFrameLinkFollowedEvent(VRL docVrl, VRL link)
    {
        fireEvent(ViewerEvent.createHyperLinkEvent(this, ViewerEventType.HYPERLINK_FRAMELINK_FOLLOWED, docVrl, link));
    }

    /**
     * Simple link method to negotiate link handling.
     * 
     * Returns 'true' if the link will be handled outside this plugin, returns false is the plugin should handle it
     * self. For example by calling updateLocation()
     */
    public boolean handleLink(VRL loc, boolean openNew) throws VrsException
    {
        String mimeType = this.getResourceHandler().getMimeTypeOf(loc);

        if (isMyMimeType(mimeType) == false)
        {
            if (isStartedAsStandalone() || openNew)
            {
                fireHyperLinkEvent(loc, true);
            }
            else
            {
                fireHyperLinkEvent(loc, false);
            }

            return true;
        }
        else
        {
            return false;
        }

    }

}
