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

package nl.esciencecenter.ptk.vbrowser.ui.browser;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicButtonUI;

import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionMethod;

public class TabTopLabelPanel extends JPanel {

    private static final long serialVersionUID = 5566174001482465094L;

    public static enum TabButtonType {
        Delete, Add
    };

    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };

    protected class TabButton extends JButton // implements ActionListener
    {
        private static final long serialVersionUID = -3932012699584733182L;

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

            // Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            // Make it transparent
            setContentAreaFilled(false);
            // No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            // Making nice rollover effect
            // we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            // Close the proper tab by clicking the button
            // addActionListener(this);
        }

        public void updateUI() {
        }

        public TabContentPanel getTabPanel() {
            return TabTopLabelPanel.this.getTabPanel();
        }
    }

    private TabButton addButton;

    private TabButton delButton;

    private TabContentPanel tabPane;

    private JLabel tabLabel;

    public TabTopLabelPanel(final TabContentPanel pane,
            final BrowserFrame.TabButtonHandler buttonHandler) {

        // unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));

        if (pane == null) {
            throw new NullPointerException("Tab pane or Parent TabbedPane is null");
        }

        tabPane = pane;

        setOpaque(false);

        // tab lable; 
        {
            this.tabLabel = new JLabel(pane.getName());
            add(tabLabel);
        }

        // add more space between the label and the button
        tabLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        // tab button

        {
            delButton = new TabButton(TabButtonType.Delete);
            add(delButton);
            delButton.setActionCommand("" + ActionMethod.CLOSE_TAB);
            delButton.addActionListener(buttonHandler);
        }
        {
            addButton = new TabButton(TabButtonType.Add);
            add(addButton);
            addButton.setActionCommand("" + ActionMethod.NEW_TAB);
            addButton.addActionListener(buttonHandler);
        }

        // add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }

    TabContentPanel getTabPanel() {
        return this.tabPane;
    }

    public void setEnableAddButton(boolean value) {
        addButton.setVisible(value);
    }

    public void setTabLabelText(String text) {
        tabLabel.setText(text);
    }

}
