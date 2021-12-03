package nl.esciencecenter.ptk.vbrowser.ui.browser.tabs;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Slf4j
public class BrowserJTabbedPaneController {

    private final JTabbedPane tabPane;
    private final ActionListener delegatedListener;

    public BrowserJTabbedPaneController(JTabbedPane tabPane, ActionListener delegatedListener) {
        this.tabPane = tabPane;
        this.delegatedListener = delegatedListener;
    }

    public TabContentPanel addTab(String name, JComponent comp, boolean setFocus, boolean withScrollPane) {
        // I) New ContentPanel
        TabContentPanel tabPanel = TabContentPanel.createTab(comp, withScrollPane);
        int newIndex = tabPane.getTabCount();
        tabPane.add(tabPanel, newIndex);
        // use size from ui model:
        tabPanel.setScrollBarUnitIncrement(48 / 2);

        TabButtonHandler handler = new TabButtonHandler(tabPanel);
        // II) Tab Header
        TabTopLabelPanel topTapPnl = new TabTopLabelPanel(name, tabPanel, handler);
        tabPane.setTabComponentAt(newIndex, topTapPnl);

        if (newIndex > 0) {
            Component tabComp = tabPane.getTabComponentAt(newIndex - 1);
            if (tabComp instanceof TabTopLabelPanel) {
                // disable add button if not lasts.
                ((TabTopLabelPanel) tabComp).setEnableAddButton(false);
            }
        }
        if (setFocus) {
            tabPane.setSelectedIndex(newIndex);
        }
        return tabPanel;
    }

    public void setTabTitle(TabContentPanel tab, String name) {
        int index = this.tabPane.indexOfComponent(tab);
        if (index < 0)
            return;

        Component tabComp = tabPane.getTabComponentAt(index);

        if (tabComp instanceof TabTopLabelPanel) {
            ((TabTopLabelPanel) tabComp).setLabelText(name);
        } else {
            log.error("FIXME:Component #{} is not a TabTopLabelPanel:{}", index, tabComp);
            this.tabPane.setTitleAt(index, name);
        }

    }

    public TabContentPanel getCurrentTab() {
        Component tab = this.tabPane.getSelectedComponent();
        if (tab instanceof TabContentPanel) {
            return ((TabContentPanel) tab);
        }
        return null;
    }

    public boolean closeTab(TabContentPanel tab, boolean disposeContent) {
        int tabIndex = tabPane.indexOfComponent(tab);

        if (tabIndex < 0) {
            return false;
        }

        this.tabPane.removeTabAt(tabIndex);
        int index = this.tabPane.getTabCount();

        if (index > 0) {
            Component comp = this.tabPane.getTabComponentAt(index - 1);
            if (comp instanceof TabTopLabelPanel) {
                ((TabTopLabelPanel) comp).setEnableAddButton(true); // always enable last + button.
            }
        }

        if (disposeContent) {
            tab.dispose();
        }

        return true;
    }

    public TabContentPanel getTab(int index) {
        Component tab = this.tabPane.getComponent(index);
        if (tab instanceof TabContentPanel) {
            return ((TabContentPanel) tab);
        }
        return null;
    }

    public class TabButtonHandler implements ActionListener {

        protected TabContentPanel tabPane;

        public TabButtonHandler(TabContentPanel pane) {
            tabPane = pane;
        }

        public void actionPerformed(ActionEvent e) {
            // redirect to ProxyBrowser controller:
            // actionListener.actionPerformed(new ActionEvent(tabPane,e.getID(),e.getActionCommand()));
            delegatedListener.actionPerformed(e);
        }
    }

}
