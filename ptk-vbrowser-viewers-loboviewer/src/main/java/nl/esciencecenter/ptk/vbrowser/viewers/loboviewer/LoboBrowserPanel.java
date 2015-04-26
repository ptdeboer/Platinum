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

import java.net.MalformedURLException;

import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import org.lobobrowser.clientlet.ClientletResponse;
import org.lobobrowser.clientlet.ComponentContent;
import org.lobobrowser.gui.BrowserPanel;
import org.lobobrowser.ua.NavigatorFrame;
import org.lobobrowser.ua.NavigatorProgressEvent;

/**
 * This browser contain a tool bar. It manages an history and can be consider as a full web browser.
 */
public class LoboBrowserPanel extends BrowserPanel
{
    private static final long serialVersionUID = 8270762419862152305L;

    // private VRSClient vrs=VFSClient.getDefault();

    private LoboPanelController controller;

    public LoboBrowserPanel(LoboPanelController controller, boolean topbar)
    {
        super(null, topbar, topbar, true);
        this.controller = controller;
        this.addNavigationListener(controller);
    }

    @Override
    public void handleDocumentRendering(NavigatorFrame frame, ClientletResponse response, ComponentContent content)
    {
        controller.debugPrintf("handleDocumentRendering():frame=%s\n", frame.getTopFrame().getProgressEvent().getUrl());
        controller.debugPrintf("handleDocumentRendering():%s\n", content.getMimeType());

        try
        {
            if (response != null)
            {
                int repCode = response.getResponseCode();
                String repStr = response.getResponseMessage();
                java.net.URL url = response.getResponseURL();
                controller.debugPrintf("LoboBrowserPanel: responsecode=%d, response str=%s,url=%s\n", repCode, repStr, url);
            }
        }
        catch (Exception e) // it is a directory or https or http
        {
            controller.handle("handleDocumentRendering()", e);
        }

        super.handleDocumentRendering(frame, response, content);
    }

    public void handleDocumentAccess(NavigatorFrame frame, ClientletResponse response)
    {
        java.net.URL frameUrl = frame.getTopFrame().getProgressEvent().getUrl();
        String mimeType = response.getMimeType();
        java.net.URL url = response.getResponseURL();

        controller.debugPrintf("handleDocumentAccess():frame=%s\n", frameUrl);
        controller.debugPrintf("handleDocumentAccess():url=%s\n", url);
        controller.debugPrintf("handleDocumentAccess():mimetype=%s\n", mimeType);

        try
        {
            boolean handled = controller.handleLink(new VRL(url), controller.isStandalone());
            controller.debugPrintf(">>> handleDocumentAccess('%s'):handled=%s\n", url, handled);

            if (handled == false)
            {
                super.handleDocumentAccess(frame, response);
                return;
            }
            else
            {
                // before Navigate isn't called always when handling a link.
                // By throwing an Error here, Lobo can be stopped !
                // Must block LOBO to continue if VBrowser has handled the link !
                throw new LoboBlockedError();
            }

        }
        catch (VrsException e)
        {
            controller.handle("Couldn't handle url:" + url, e);
        }
    }

    @Override
    public void handleError(NavigatorFrame frame, ClientletResponse response, Throwable e)
    {
        NavigatorProgressEvent event = null;
        event = frame.getProgressEvent();

        if (event != null)
            controller.debugPrintf("Caught blockerror:%s\n", event.getUrl());
        else
            controller.debugPrintf("Caught blockerror: No Event!\n");
        // Hack:
        // LoboBlockedError is used to stop Lobo from processing links!
        if (e instanceof LoboBlockedError)
        {
            return;
        }

        String str = "";

        if (event != null)
            str = "Error while handling url:" + event.getUrl() + "\n";
        else
            str = "Couldn't handle request\n";
        // redirect to viewer:
        controller.handle(str, e);
    }

    /** Really peform navigation */
    protected void doNavigate(String location) throws MalformedURLException
    {
        controller.debugPrintf("doNavigate(String):%s\n", location);
        super.navigate(location);
    }

    /**
     * Really perfrom navigation
     * 
     * @throws VRLSyntaxException
     */
    protected void doNavigate(VRL loc) throws MalformedURLException, VRLSyntaxException
    {
        controller.debugPrintf("doNavigate(VRL):%s\n", loc);
        navigate(loc.toString());
    }

    @Override
    public void navigate(String urlstr) throws MalformedURLException
    {
        controller.debugPrintf("navigate(String):%s\n", urlstr);
        super.navigate(urlstr);
    }

    @Override
    public void navigate(java.net.URL url) throws MalformedURLException
    {
        controller.debugPrintf("navigate():%s\n", url);
        // super.navigate(url);
        super.navigate(url);
    }

}
