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

package nl.esciencecenter.ptk.vbrowser.ui.browser.tabs;

import nl.esciencecenter.ptk.object.Disposable;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeContainer;
import nl.esciencecenter.ptk.vbrowser.ui.object.UIDisposable;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerPlugin;

import javax.swing.*;
import java.awt.*;

/**
 * Managed Tab Panel. This panel contains the Icon/Table/Resource panel.
 */
public class TabContentPanel extends JPanel {

    private JPanel topPanel;
    private JScrollPane scrollPane;
    private JComponent content;

    public TabContentPanel(boolean withScrollPane) {
        super();
        initGui(withScrollPane);
    }

    public static TabContentPanel createTab(JComponent comp, boolean withScrollPane) {
        TabContentPanel tabP = new TabContentPanel(withScrollPane);
        tabP.setContent(comp);
        return tabP;
    }

    protected void initGui(boolean withScrollPane) {
        {
            this.setLayout(new BorderLayout());
            {
                this.topPanel = new JPanel();
                this.add(topPanel, BorderLayout.NORTH);
            }

            if (withScrollPane) {
                this.scrollPane = new JScrollPane();
                this.add(scrollPane, BorderLayout.CENTER);
                scrollPane.getVerticalScrollBar().setUnitIncrement(48 / 2);
            }
        }
    }

    public ViewNode getViewNode() {
        if (content instanceof ViewNodeContainer) {
            return ((ViewNodeContainer) content).getViewNode();
        }

        return null;
    }

    public JComponent getContent() {
        return this.content;
    }

    public void setContent(JComponent comp) {
        if (this.content != null) {
            if (content instanceof UIDisposable) {
                ((UIDisposable) content).dispose();
            }
        }

        if (this.scrollPane != null) {
            this.scrollPane.setViewportView(comp);
        } else {
            if (content != null) {
                this.remove(content);
            }
            if (comp != null) {
                this.add(comp, BorderLayout.CENTER);
            }
        }
        this.content = comp;
    }

    public JScrollPane getScrollPane() {
        return this.scrollPane;
    }

    public void setScrollBarUnitIncrement(int size) {
        if (scrollPane != null) {
            scrollPane.getVerticalScrollBar().setUnitIncrement(size);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(size);
        }
    }

    public void dispose() {
        JComponent content = getContent();
        if (content instanceof Disposable) {
            ((Disposable) content).dispose();
        }

        if (content instanceof ViewerPlugin) {
            ((ViewerPlugin) content).disposeViewer();
        }
    }
}
