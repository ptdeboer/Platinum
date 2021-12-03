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

import nl.esciencecenter.ptk.data.HashMapList;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.vbrowser.ui.attribute.AttributePanel;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.browser.ProxyBrowserController;
import nl.esciencecenter.ptk.vbrowser.ui.model.UIViewModel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerJPanel;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Properties Viewer/Editor. Uses ProxyNode as attribute source.
 */
public class ProxyPropertiesEditor extends ViewerJPanel implements ProxyViewer {

    public static final String APPLY_ACTION = "Apply";
    public static final String RESET_ACTION = "Reset";
    public static final String OK_ACTION = "OK";
    public static final String CANCEL_ACTION = "Cancel";

    private static final String[] mimeTypes = {
            "ResourceInfoConfig",//
            "application/vbrowser-vrs-infors-ResourceInfoConfig",//
            "application/vbrowser-vrs-ResourceLink"//
    };

    // === instance === //

    private ViewNode viewNode;
    // ui
    private JScrollPane resourceAttrSP;
    private JScrollPane configAttrSP;
    private JScrollPane linkConfigPanelSP;
    private JPanel mainPanel;
    private JPanel topPanel;
    private JLabel iconLabel;
    private JTabbedPane tabPanel;
    private JPanel configAttrButtonPnl;
    // controller
    private ProxyPropertiesEditorController controller;
    // panels
    private AttributePanel resourceAttrPanel;
    private AttributePanel resourceConfigPanel;
    private AttributePanel linkConfigPanel;

    // Browser registry :
    private BrowserInterface browser;
    private UIViewModel uiModel;

    public ProxyPropertiesEditor() {
        super();
    }

    public ProxyPropertiesEditor(ProxyBrowserController masterBrowser, ViewNode node) {
        super();
        this.browser = masterBrowser;
        this.viewNode = node;
    }

    @Override
    public String[] getMimeTypes() {
        return mimeTypes;
    }

    public void initGui() {

        this.controller = new ProxyPropertiesEditorController(this);
        uiModel = UIViewModel.createIconsModel(64);

        {
            // === Layout === //
            BorderLayout thisLayout = new BorderLayout();
            this.setLayout(thisLayout);
            {
                this.mainPanel = new JPanel();
                this.getContentPanel().add(mainPanel, BorderLayout.CENTER);
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
                // TAB
                {
                    this.tabPanel = new JTabbedPane();
                    mainPanel.add(tabPanel, BorderLayout.CENTER);
                    {
                        this.resourceAttrSP = new JScrollPane();
                        tabPanel.addTab("properties", resourceAttrSP);
                        // AttributePanel
                        {
                            resourceAttrPanel = new AttributePanel();
                            resourceAttrSP.setViewportView(resourceAttrPanel);
                            resourceAttrPanel.setSize(400, 800);
                            resourceAttrPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

                        }
                    }
                    mainPanel.add(tabPanel, BorderLayout.CENTER);
                    {
                        this.configAttrSP = new JScrollPane();
                        tabPanel.addTab("config", configAttrSP);
                        // Config AttributePanel
                        {
                            resourceConfigPanel = new AttributePanel();
                            configAttrSP.setViewportView(resourceConfigPanel);
                            resourceConfigPanel.setSize(400, 800);
                            resourceConfigPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
                        }
                    }

                    {
                        this.linkConfigPanelSP = new JScrollPane();
                        tabPanel.addTab("link", linkConfigPanel);
                        // Config AttributePanel
                        {
                            linkConfigPanel = new AttributePanel();
                            linkConfigPanelSP.setViewportView(linkConfigPanel);
                            linkConfigPanel.setSize(400, 800);
                            linkConfigPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
                        }
                    }
                }
                // Button panel.
                {
                    this.configAttrButtonPnl = new JPanel();
                    this.mainPanel.add(this.configAttrButtonPnl, BorderLayout.SOUTH);
                    this.configAttrButtonPnl.setLayout(new FlowLayout(FlowLayout.CENTER));

                    {
                        JButton but;
                        this.configAttrButtonPnl.add(but = new JButton("Apply"));
                        but.addActionListener(controller);
                        but.setActionCommand(APPLY_ACTION);
                    }
                    {
                        JButton but;
                        this.configAttrButtonPnl.add(but = new JButton("Reset"));
                        but.addActionListener(controller);
                        but.setActionCommand(RESET_ACTION);

                    }
                    {
                        JButton but;
                        this.configAttrButtonPnl.add(but = new JButton("Cancel"));
                        but.addActionListener(controller);
                        but.setActionCommand(CANCEL_ACTION);

                    }
                    {
                        JButton but;
                        this.configAttrButtonPnl.add(but = new JButton("OK"));
                        but.addActionListener(controller);
                        but.setActionCommand(OK_ACTION);
                    }
                }
            }
        }

        this.setToolTipText(getViewerName());
        this.setSize(800, 600);
    }

    @Override
    public void doInitViewer() {
        initGui();
    }

    @Override
    public void doStopViewer() {
    }

    @Override
    public void doDisposeViewer() {
    }

    @Override
    public String getViewerName() {
        return "PropertiesViewers";
    }

    @Override
    public String getViewerClass() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public void doStartViewer(VRL vrl, String optMethodName) {
        if (vrl.equals(viewNode.getVRL()) == false) {
            this.handle("Refresh not supported (for different VRL then current ViewNode)", null);
        }
        try {
            this.controller.update(viewNode);
        } catch (Exception e) {
            this.notifyException("Failed to update Resource:" + viewNode, e);
        }
    }

    public Map<String, List<String>> getMimeMenuMethods() {
        String[] mimeTypes = getMimeTypes();
        // Use HashMapList to keep order of menu entries: first is default(!)
        Map<String, List<String>> mappings = new HashMapList<String, List<String>>();
        for (int i = 0; i < mimeTypes.length; i++) {
            List<String> list = new StringList("config:Edit Configuration");
            mappings.put(mimeTypes[i], list);
        }
        return mappings;
    }

    @Override
    protected void doUpdate(VRL vrl) {
        controller.doUpdate(vrl);
    }

    protected void handle(String actionText, Throwable e) {
        if (browser == null) {
            return;
        } else {
            browser.handleException(actionText, e);
        }
    }

    protected BrowserPlatform getBrowserPlatform() {
        return this.browser.getPlatform();
    }

    protected JLabel getIconLabel() {
        return this.iconLabel;
    }

    protected ViewNode getViewNode() {
        return this.viewNode;
    }

    protected UIViewModel getUIModel() {
        return this.uiModel;
    }

    public AttributePanel getAttributePanel() {
        return this.resourceAttrPanel;
    }

    public AttributePanel getConfigAttributePanel() {
        return this.resourceConfigPanel;
    }

    public void setEditable(boolean isEditable) {
        this.resourceConfigPanel.setEditable(isEditable);
    }
}
