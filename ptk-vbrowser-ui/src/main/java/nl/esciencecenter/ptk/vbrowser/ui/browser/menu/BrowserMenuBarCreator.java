package nl.esciencecenter.ptk.vbrowser.ui.browser.menu;

import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionCmd;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionCmdType;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;
import nl.esciencecenter.ptk.vbrowser.ui.browser.laf.LookAndFeelType;
import nl.esciencecenter.ptk.vbrowser.ui.properties.UIProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import static nl.esciencecenter.ptk.vbrowser.ui.browser.laf.LookAndFeelType.*;

public class BrowserMenuBarCreator {

    private final BrowserInterface browserController;
    private final ActionListener actionListener;

    public BrowserMenuBarCreator(BrowserInterface browserController, ActionListener actionListener) {
        this.browserController = browserController;
        this.actionListener = actionListener;
    }

    public JMenuBar create() {

        JMenuBar menuBar = new JMenuBar();

        // ========
        // Location
        // ========
        {
            JMenu mainMenu = new JMenu();
            menuBar.add(mainMenu);
            mainMenu.setText("Location");
            mainMenu.setMnemonic(KeyEvent.VK_L);
            {
                JMenuItem viewNewWindowMenuItem = new JMenuItem();
                mainMenu.add(viewNewWindowMenuItem);
                viewNewWindowMenuItem.setText("New Window");
                viewNewWindowMenuItem.setMnemonic(KeyEvent.VK_W);
                viewNewWindowMenuItem.addActionListener(actionListener);
                viewNewWindowMenuItem.setActionCommand(ActionCmdType.CREATE_NEW_WINDOW.toString());
            }
            {
                JMenuItem openMenuItem = new JMenuItem();
                mainMenu.add(openMenuItem);
                openMenuItem.setText("Open");
                openMenuItem.setMnemonic(KeyEvent.VK_O);
                openMenuItem.addActionListener(actionListener);
                openMenuItem.setActionCommand(ActionCmdType.OPEN_LOCATION.toString());
            }
            {
                JMenuItem openInWinMenuItem = new JMenuItem();
                mainMenu.add(openInWinMenuItem);
                openInWinMenuItem.setText("Open in new Window");
                openInWinMenuItem.setMnemonic(KeyEvent.VK_N);
                openInWinMenuItem.addActionListener(actionListener);
                openInWinMenuItem.setActionCommand(ActionCmdType.OPEN_IN_NEW_WINDOW.toString());
            }
            {
                JMenuItem openInWinMenuItem = new JMenuItem();
                mainMenu.add(openInWinMenuItem);
                openInWinMenuItem.setText("Open in new Tab");
                openInWinMenuItem.setMnemonic(KeyEvent.VK_T);
                openInWinMenuItem.addActionListener(actionListener);
                openInWinMenuItem.setActionCommand(ActionCmdType.NEW_TAB.toString());
            }
            JSeparator jSeparator = new JSeparator();
            mainMenu.add(jSeparator);
        }

        // ============
        // "Tools" Menu
        // ============
        {
            JMenu toolsMenu = new JMenu();
            menuBar.add(toolsMenu);
            toolsMenu.setText("Tools");
            toolsMenu.setMnemonic(KeyEvent.VK_T);
            populateToolsMenu(browserController, toolsMenu, actionListener);
        }

        // ============
        // "View" Menu
        // ============
        {
            JMenu viewMenu = new JMenu();
            menuBar.add(viewMenu);
            viewMenu.setText("View");
            viewMenu.setMnemonic(KeyEvent.VK_V);
            {
                //
                {
                    JMenu preferencesMenu = new JMenu();
                    viewMenu.add(preferencesMenu);
                    preferencesMenu.setText("Preferences");
                    {
                        JLabel globalPreferencesMI = new JLabel();
                        preferencesMenu.add(globalPreferencesMI);
                        globalPreferencesMI.setText("- Global Preferences -");
                        globalPreferencesMI.setForeground(Color.GRAY);
                    }
                    {
                        JCheckBoxMenuItem singleClickActionMenuItem = new JCheckBoxMenuItem();
                        preferencesMenu.add(singleClickActionMenuItem);
                        singleClickActionMenuItem.setText("Single Click Action");
                        singleClickActionMenuItem.setActionCommand(ActionCmdType.GLOBAL_SET_SINGLE_ACTION_CLICK.toString());
                        singleClickActionMenuItem.setState(getUIProperties().getSingleClickAction());
                        singleClickActionMenuItem.addActionListener(actionListener);
                    }
                    {
                        preferencesMenu.add(new JSeparator());
                    }
                    {
                        JMenu lafMenu = createLafMenu(actionListener);
                        lafMenu.setText("Look and Feel");
                        preferencesMenu.add(lafMenu);
                    }
                    {
                        preferencesMenu.add(new JSeparator());
                    }
                    {
                        JMenuItem singleClickActionMenuItem = new JMenuItem();
                        preferencesMenu.add(singleClickActionMenuItem);
                        singleClickActionMenuItem.setText("Save settings");
                        singleClickActionMenuItem.addActionListener(actionListener);
                        singleClickActionMenuItem.setActionCommand(ActionCmdType.SAVE_SETTINGS.toString());
                    }
                }
                // viewNewWindowMenuItem.setActionCommand(ActionCmdType.CREATE_NEW_WINDOW.toString());
            }
        }

        // ============
        // "Help" Menu
        // ============
        {
            JMenu viewMenu = new JMenu();
            menuBar.add(viewMenu);
            viewMenu.setText("Help");
            viewMenu.setMnemonic(KeyEvent.VK_H);
            {
                JMenuItem viewMI = new JMenuItem();
                viewMenu.add(viewMI);
                viewMI.setText("Help");
                // viewMI.setMnemonic(KeyEvent.VK_W);
                viewMI.addActionListener(actionListener);
                viewMI.setActionCommand(ActionCmdType.GLOBAL_HELP.toString());
            }
            {
                JMenuItem viewMI = new JMenuItem();
                viewMenu.add(viewMI);
                viewMI.setText("About");
                // viewMI.setMnemonic(KeyEvent.VK_W);
                viewMI.addActionListener(actionListener);
                viewMI.setActionCommand(ActionCmdType.GLOBAL_ABOUT.toString());
            }
        }

        return menuBar;
    }

    private UIProperties getUIProperties() {
        return this.browserController.getPlatform().getGuiSettings();
    }

    public JMenu createLafMenu(ActionListener actionListener) {

        LookAndFeelType lafType = getUIProperties().getLAFType();
        boolean lafEnabled = getUIProperties().getLaFEnabled();

        JMenu lafMenu = new JMenu();
        // Windows LaF doesn't work on Linux :-D
        {
            JCheckBoxMenuItem lafEnabledCB = new JCheckBoxMenuItem();
            lafMenu.add(lafEnabledCB);
            lafEnabledCB.setText("Enable");
            lafEnabledCB.setActionCommand(ActionCmdType.LOOKANDFEEL_ENABLED.toString());
            lafEnabledCB.setState(getUIProperties().getLaFEnabled());
            lafEnabledCB.addActionListener(actionListener);
        }
        lafMenu.add(new JSeparator());
        {
            // LookAndFeelType[] supported = new LookAndFeelType[]{NATIVE, METAL, PLASTIC_3D, PLASTIC_XP, NIMBUS, SEAGLASS};
            // JDK11 supported and tested: others are buggy or fugly.
            LookAndFeelType[] supported = new LookAndFeelType[]{NATIVE, METAL, NIMBUS};

            for (LookAndFeelType type : supported) {
                JMenuItem lafMI = new JMenuItem();
                lafMenu.add(lafMI);
                String name=type.getName();
//                if (lafEnabled && type==lafType) {
//                    name="<html><b>"+type.getName()+"</b></html>";
//                }
                lafMI.setText(name);
                lafMI.setActionCommand(ActionCmd.create(ActionCmdType.LOOKANDFEEL, type).toString());
                lafMI.addActionListener(actionListener);
                //lafMI.setEnabled(lafEnabled);
            }
        }
        lafMenu.add(new JSeparator());
        {
            JMenuItem lafMI = new JMenuItem();
            lafMenu.add(lafMI);
            lafMI.setText("Save Look and Feel");
            lafMI.setActionCommand(ActionCmdType.SAVE_SETTINGS.toString());
            lafMI.addActionListener(actionListener);
        }
        return lafMenu;
    }

    protected JMenu populateToolsMenu(BrowserInterface browserController, JMenu toolsMenu, ActionListener actionListener) {
        ToolMenuCreator menuCreator = new ToolMenuCreator(browserController.getPlatform().getViewerRegistry(), actionListener);
        return menuCreator.create(toolsMenu);
    }

}
