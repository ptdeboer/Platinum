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

import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionCmdType;
import nl.esciencecenter.ptk.vbrowser.ui.browser.MiniIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Custom tab header: Title + mini action icons.
 */
public class TabTopLabelPanel extends JPanel {

    public enum TabButtonType {
        Delete, Add
    }

    public class TabButton extends JButton {
        TabButtonType type;

        public TabButton(TabButtonType buttonType) {
            int size = 17;
            this.type = buttonType;

            setPreferredSize(new Dimension(size, size));
            switch (type) {
                case Delete:
                    setToolTipText("Close this tab");
                    this.setIcon(new ImageIcon(MiniIcons.getTabDeleteImage()));
                    break;
                case Add:
                    setToolTipText("Copy tab");
                    this.setIcon(new ImageIcon(MiniIcons.getTabAddImage()));
                    break;
                default:
                    setToolTipText("?");
                    this.setIcon(new ImageIcon(MiniIcons.getMiniQuestionmark()));
                    break;
            }

            // Make it transparent
            setContentAreaFilled(false);
            setFocusable(true);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            // Making nice rollover effect
            // we use the same listener for all buttons
            TabButtonListener buttonMouseListener = new TabButtonListener();
            addMouseListener(buttonMouseListener);
            addFocusListener(buttonMouseListener);
            setRolloverEnabled(false); // do it myself for consistency between LaFs
            // Close the proper tab by clicking the button
            // addActionListener(this);
        }

        public TabContentPanel getTabPanel() {
            return TabTopLabelPanel.this.getTabPanel();
        }
    }

    public static class TabButtonListener extends MouseAdapter implements FocusListener {

        public void mouseEntered(MouseEvent e) {
            setBorder(e.getComponent(), true);
        }

        public void mouseExited(MouseEvent e) {
            setBorder(e.getComponent(), false);
        }

        @Override
        public void focusGained(FocusEvent e) {
            setBorder(e.getComponent(), true);
        }

        @Override
        public void focusLost(FocusEvent e) {
            setBorder(e.getComponent(), false);
        }

        public void setBorder(Component component, boolean value) {
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(value);
            }
        }

    }

    private final TabButton addButton;
    private final TabButton delButton;
    private final TabContentPanel tabPane;
    private final JLabel tabLabel;

    public TabTopLabelPanel(String name,
                            final TabContentPanel pane,
                            final BrowserJTabbedPaneController.TabButtonHandler buttonHandler) {

        // Unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 1, 0));

        if (pane == null) {
            throw new NullPointerException("Tab pane or Parent TabbedPane is null.");
        }

        tabPane = pane;
        setOpaque(false);

        {
            // === Layout === //
            {
                // === Tab JLabel === //
                this.tabLabel = new JLabel(name) {
                    // block mouse events to allow default tab switching
                    public boolean contains(int x, int y) {
                        return false;
                    }
                };

                add(tabLabel);
                setLabelText(name);
            }
            // add more space between the label and the button
            tabLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            {
                // === Delete Button === //
                delButton = new TabButton(TabButtonType.Delete);
                add(delButton);
                delButton.setActionCommand("" + ActionCmdType.CLOSE_TAB);
                delButton.addActionListener(buttonHandler);
            }
            {
                // === Add Button === //
                addButton = new TabButton(TabButtonType.Add);
                add(addButton);
                addButton.setActionCommand("" + ActionCmdType.NEW_TAB);
                addButton.addActionListener(buttonHandler);
            }

            // Add more space to the top of the component
            setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
            this.setFocusable(true);
        }
    }

    public void setLabelText(String name) {
        this.tabLabel.setText(name);
        tabLabel.setToolTipText(name);
    }

    protected TabContentPanel getTabPanel() {
        return this.tabPane;
    }

    public void setEnableAddButton(boolean value) {
        addButton.setVisible(value);
    }


}
