package nl.esciencecenter.ptk.vbrowser.ui.browser.menu;

import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionCmd;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionCmdType;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;
import nl.esciencecenter.ptk.vbrowser.ui.browser.laf.LookAndFeelType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

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
                openMenuItem.setText("Open Location");
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
                        preferencesMenu.add(new JSeparator());
                    }
                    {
                        JMenu lafMenu = createLafMenu(actionListener);
                        lafMenu.setText("Look and Feel");
                        preferencesMenu.add(lafMenu);
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

    public JMenu createLafMenu(ActionListener actionListener) {
        JMenu lafMenu = new JMenu();
        // Windows LAF doesn't work on Linux :-D
        {
            JMenuItem lafMI = new JMenuItem();
            lafMenu.add(lafMI);
            lafMI.setText("Metal (Default)");
            lafMI.setActionCommand(ActionCmd.create(ActionCmdType.LOOKANDFEEL, LookAndFeelType.METAL).toString());
            lafMI.addActionListener(actionListener);
        }
        {
            JMenuItem lafMI = new JMenuItem();
            lafMenu.add(lafMI);
            lafMI.setText("Plastic 3D");
            lafMI.setActionCommand(ActionCmd.create(ActionCmdType.LOOKANDFEEL, LookAndFeelType.PLASTIC_3D).toString());
            lafMI.addActionListener(actionListener);
        }
        {
            JMenuItem lafMI = new JMenuItem();
            lafMenu.add(lafMI);
            lafMI.setText("Plastic XP");
            lafMI.setActionCommand(ActionCmd.create(ActionCmdType.LOOKANDFEEL, LookAndFeelType.PLASTIC_XP).toString());
            lafMI.addActionListener(actionListener);
        }
        {
            JMenuItem lafMI = new JMenuItem();
            lafMenu.add(lafMI);
            lafMI.setText("Quaqua (leopard)");
            lafMI.setActionCommand(ActionCmd.create(ActionCmdType.LOOKANDFEEL, LookAndFeelType.QUAQUA).toString());
            lafMI.addActionListener(actionListener);
        }
        {
            JMenuItem lafMI = new JMenuItem();
            lafMenu.add(lafMI);
            lafMI.setText("Nimbus");
            lafMI.setActionCommand(ActionCmd.create(ActionCmdType.LOOKANDFEEL, LookAndFeelType.NIMBUS.toString()).toString());
            lafMI.addActionListener(actionListener);
        }
        {
            JMenuItem lafMI = new JMenuItem();
            lafMenu.add(lafMI);
            lafMI.setText("Seaglass");
            lafMI.setActionCommand(ActionCmd.create(ActionCmdType.LOOKANDFEEL, LookAndFeelType.SEAGLASS.toString()).toString());
            lafMI.addActionListener(actionListener);
        }
        lafMenu.add(new JSeparator());
        {
            JMenuItem lafMI = new JMenuItem();
            lafMenu.add(lafMI);
            lafMI.setText("Save Look and Feel");
            lafMI.setActionCommand(ActionCmdType.SAVE_LOOKANDFEEL.toString());
            lafMI.addActionListener(actionListener);
        }
        return lafMenu;
    }

    protected JMenu populateToolsMenu(BrowserInterface browserController, JMenu toolsMenu, ActionListener actionListener) {
        ToolMenuCreator menuCreator = new ToolMenuCreator(browserController.getPlatform().getViewerRegistry(), actionListener);
        return menuCreator.create(toolsMenu);
    }

}
