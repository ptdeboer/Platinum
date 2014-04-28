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

package nl.esciencecenter.ptk.vbrowser.ui.browser.viewers;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.task.ITaskSource;
import nl.esciencecenter.ptk.vbrowser.ui.attribute.AttributePanel;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserTask;
import nl.esciencecenter.ptk.vbrowser.ui.browser.ProxyBrowserController;
import nl.esciencecenter.ptk.vbrowser.ui.model.UIViewModel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.EmbeddedViewer;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerPlugin;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Internal ProxyBrowser viewer. Uses ProxyNode as attribute source. 
 */
public class ProxyObjectViewer extends EmbeddedViewer implements ProxyViewer
{
    private static final long serialVersionUID = -5320614535536348580L;

    private JScrollPane scrollPane;

    private JPanel mainPanel;

    private JPanel topPanel;

    private JPanel midPanel;

    private ViewNode viewNode;

    private JLabel iconLabel;

    private AttributePanel attrPanel;

    // Browser registry :
    private BrowserInterface browser;

    private UIViewModel uiModel;

    public ProxyObjectViewer()
    {
        super();
    }

    public ProxyObjectViewer(ProxyBrowserController masterBrowser, ViewNode node)
    {
        super();
        this.browser = masterBrowser;
        this.viewNode = node;
    }

    @Override
    public String[] getMimeTypes()
    {
        return null;
    }

    public void initGui()
    {
        uiModel = UIViewModel.createIconsModel(64);

        {

            BorderLayout thisLayout = new BorderLayout();
            this.setLayout(thisLayout);
            // this.setLayout(null); // absolute layout

            {
                this.scrollPane = new JScrollPane();

                this.getContentPanel().add(scrollPane, BorderLayout.CENTER); // addToRootPane(imagePane,BorderLayout.CENTER);
                // MainPanel
                {
                    this.mainPanel = new JPanel();
                    scrollPane.setViewportView(mainPanel);
                    mainPanel.setLayout(new BorderLayout());
                    mainPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

                    {
                        this.topPanel = new JPanel();
                        mainPanel.add(topPanel, BorderLayout.NORTH);
                        topPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
                        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                        {
                            this.iconLabel = new JLabel();
                            topPanel.add(iconLabel);
                        }

                    }

                    // AttributePanel
                    {
                        attrPanel = new AttributePanel();
                        mainPanel.add(attrPanel, BorderLayout.CENTER);
                        attrPanel.setSize(400, 800);
                        attrPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
                    }
                }
            }

            this.setToolTipText(getViewerName());

        }

        this.setSize(800, 600);

    }

    @Override
    public void doInitViewer()
    {
        initGui();
    }

    @Override
    public void doStopViewer()
    {
    }

    @Override
    public void doDisposeViewer()
    {
    }

    @Override
    public String getViewerName()
    {
        // remove html color codes:
        return "ObjectViewer";
    }

    @Override
    public String getViewerClass()
    {
        return this.getClass().getCanonicalName();
    }

    @Override
    public void doStartViewer(VRL vrl,String optMethodName)
    {
        if (vrl.equals(viewNode.getVRL())==false)
        {
            this.handle("Refresh not supported (for different VRL then current ViewNode)",null);  
        }
        try
        {
            update(viewNode);
        }
        catch (Exception e)
        {
            this.notifyException("Failed to update Resource:" + viewNode, e);
        }
    }

    @Override
    public Map<String, List<String>> getMimeMenuMethods()
    {
        return null;
    }

    public void update(final ViewNode node) throws Exception
    {
        if (node == null)
        {
            return;
        }

        BrowserTask task = new BrowserTask((ITaskSource) null, "Updating:" + node)
        {

            public void doTask()
            {
                notifyBusy(true);

                try
                {
                    updateNode(getProxyNode(node.getVRL()));
                }
                catch (Throwable e)
                {
                    this.setException(e);
                    handle("Couldn't update node:" + node, e);
                }
                finally
                {
                    notifyBusy(false);
                }

            }
        };

        task.startTask();
    }

    @Override
    protected void doUpdate(VRL vrl)
    {
        try
        {
            updateNode(this.getProxyNode(new VRL(vrl)));
        }
        catch (ProxyException e)
        {
            handle("Failed to load:" + vrl, e);
        }
    }

    protected void handle(String actionText, Throwable e)
    {
        if (browser == null)
        {
            // standalone viewer (testing).
            System.err.printf("Exception when performing:%s\n", actionText);
            e.printStackTrace();
            return;
        }
        else
        {
            browser.handleException(actionText, e);
        }
    }

    protected ProxyNode getProxyNode(VRL vrl) throws ProxyException
    {
        BrowserPlatform platform = this.browser.getPlatform();
        return platform.getProxyFactoryFor(vrl).openLocation(vrl);
    }

    private void updateNode(ProxyNode proxyNode) throws ProxyException
    {
        viewNode = proxyNode.createViewItem(uiModel);
        iconLabel.setIcon(viewNode.getIcon());
        iconLabel.setText(viewNode.getName());

        List<String> attrNames = proxyNode.getAttributeNames();
        List<Attribute> attrs = proxyNode.getAttributes(attrNames);
        AttributeSet attrSet = new AttributeSet(attrs);

        this.attrPanel.setAttributes(attrSet, false);

        super.requestFramePack();

    }

}
