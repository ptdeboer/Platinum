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

package nl.esciencecenter.ptk.util.vterm;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.exec.ShellChannel;
import nl.esciencecenter.ptk.io.FSUtil;
import nl.esciencecenter.ptk.ui.charpane.ColorMap;
import nl.esciencecenter.ptk.ui.dialogs.ExceptionDialog;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.ptk.util.vterm.VTermChannelProvider.TermChannelOptions;

public class VTerm extends JFrame implements Runnable {
    // ========================================================================

    // ========================================================================

    private static PLogger logger;

    static {
        logger = PLogger.getLogger(VTerm.class);
        //logger.setLevelToDebug(); 
    }

    private static final long serialVersionUID = -3209091621707504735L;

    public static int verbose = 0; // 0=silent, 1 = error and fixmes,2=debug,3 very debug  

    private static final String SESSION_SSH = "SSH";

    private static final String SESSION_BASH = "BASH";

    private static final String SESSION_TELNET = "TELNET";

    private static final String SESSION_SHELLCHANNEL = "SHELLCHANNEL";

    private static final String aboutText = "<html><center>VTerm VT100+ Emulator<br>"
            + "(beta version)<br>" + "Author Piter T. de Boer<br>"
            + "(C) Piter.NL</center></html>";

    
    // =======================================================================
    //
    // =======================================================================

    // options/config: 
    protected boolean _saveConfigEnabled = true;

    protected String termType = VT10xEmulatorDefs.TERM_XTERM;

    // =======================
    // Session fields
    // =======================

    protected java.net.URI startURI = null;

    protected String sessionType = SESSION_SSH;

    protected TermPanel terminalPanel;

    private Thread thread = null;

    private ShellChannel shellChannel;

    // 
    private boolean sessionAlive;

    // Menu 
    private boolean isResizable = true;

    // ==============
    // GUI 
    // ==============

    private JMenuBar menu;
    private JCheckBoxMenuItem sshXForwardingCB;
    private JCheckBoxMenuItem channelCompressionCB;
    private JCheckBoxMenuItem fontAAcheckBox;
    private JMenuItem startBashMenuItem;
    private JMenuItem closeSessionMenuItem;
    private JMenuItem startSSHMenuItem;
    private JCheckBoxMenuItem menuTypeVt100CB;
    private JCheckBoxMenuItem menuTypeXtermCB;
    private VTermController termController;
    private JCheckBoxMenuItem optionsSyncScrolling;

    /** Current view properties */
    private Properties sessionProperties = new Properties();

    VTermChannelProvider termProvider;

    public VTerm(VTermChannelProvider termProvider) {
        this.termProvider = termProvider;

        loadConfigSettings();

        // gui uses loaded settings !
        initGui();

        try {
            startURI = new URI("ssh", GlobalProperties.getGlobalUserName(), "localhost", 22, null,
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    void loadConfigSettings() {
        this.persistantProperties = loadProperties();
    }

    public void initGui() {
        this.setTitle("");

        this.setLayout(new BorderLayout());

        this.termController = new VTermController(this);

        {
            terminalPanel = new TermPanel();
            terminalPanel.initGUI();
            this.add(terminalPanel, BorderLayout.CENTER);
        }

        {
            menu = this.createMenuBar();
            this.setJMenuBar(menu);
        }

        // Apply loaded config settings to CharPane !
        this.applyGraphicConfigSettings();

        //setResizable(this.isResizable);

        // Listeners:
        {
            this.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    exit(0);
                }
            });

            this.terminalPanel.addComponentListener(termController);
            this.addComponentListener(termController);
            this.addWindowListener(termController);
            //this.vlTerm.addTermListener(termPanelListener); 
        }

        this.pack();
    }

    /**
     * Apply current configured properties to CharPane. Does reset graphics !
     */
    void applyGraphicConfigSettings() {
        // update configuration: 
        String propStr = (String) persistantProperties.get("vlterm.syncScrolling");
        if (propStr != null) {
            boolean val = Boolean.parseBoolean(propStr);
            this.optionsSyncScrolling.setSelected(val);
            this.terminalPanel.setSynchronizedScrolling(val);
        }

        // directly set charPane options: do not reset graphics each time !
        propStr = (String) persistantProperties.get("vlterm.color.scheme");
        if (propStr != null)
            this.terminalPanel.updateColorMap(ColorMap.getColorMap(propStr), false);

        propStr = (String) persistantProperties.get("vlterm.font.type");
        if (propStr != null)
            this.terminalPanel.updateFontType(propStr, false);

        propStr = (String) persistantProperties.get("vlterm.font.size");
        if (propStr != null)
            this.terminalPanel.updateFontSize(Integer.parseInt(propStr), false);

        propStr = (String) persistantProperties.get("vlterm.font.antiAliasing");

        if (propStr != null) {
            boolean val = Boolean.parseBoolean(propStr);
            this.terminalPanel.getFontInfo().setAntiAliasing(val);
            this.fontAAcheckBox.setSelected(val);
        }

        // reset graphics=initialize graphics
        this.terminalPanel.resetGraphics();
        // this.updateFrameSize(); 
    }

    public TermChannelOptions getChannelOptions(String type, boolean autoCreate) {
        TermChannelOptions options = termProvider.getChannelOptions(type);
        if (options != null) {
            return options;
        }

        return new TermChannelOptions();
    }

    public void setChannelOptions(String type, TermChannelOptions options) {
        termProvider.setChannelOptions(type, options);
    }

    public JMenuBar createMenuBar() {
        //ChannelOptions options = getChannelOptions();

        JMenuBar menubar = new JMenuBar();
        JMenu menu;
        JMenuItem mitem;

        menu = new JMenu("Session");
        menubar.add(menu);

        startSSHMenuItem = new JMenuItem("Start SSH Session...");
        menu.add(startSSHMenuItem);
        startSSHMenuItem.addActionListener(termController);
        startSSHMenuItem.setActionCommand(SESSION_SSH);

        /*
         * mitem=new JMenuItem("Start Telnet Session..."); menu.add(mitem);
         * mitem.addActionListener(this);
         * mitem.setActionCommand(SESSION_TELNET);
         * 
         * 
         */
        startBashMenuItem = new JMenuItem("Start BASH Session...");
        menu.add(startBashMenuItem);
        startBashMenuItem.addActionListener(termController);
        startBashMenuItem.setActionCommand(SESSION_BASH);
        menu.add(startBashMenuItem);

        {
            mitem = new JMenuItem("Repaint");
            menu.add(mitem);
            mitem.addActionListener(termController);
            mitem.setActionCommand("Repaint");
        }

        {
            mitem = new JMenuItem("Clear Screen");
            menu.add(mitem);
            mitem.addActionListener(termController);
            mitem.setActionCommand("ClearScreen");
        }

        {
            closeSessionMenuItem = new JMenuItem("Terminate Session");
            menu.add(closeSessionMenuItem);
            closeSessionMenuItem.addActionListener(termController);
            closeSessionMenuItem.setActionCommand("Close");
            closeSessionMenuItem.setEnabled(false);
        }

        mitem = new JMenuItem("Quit VLTerm");
        menu.add(mitem);
        mitem.addActionListener(termController);
        mitem.setActionCommand("Quit");

        // SETTINGS MENU
        {
            menu = new JMenu("Settings");
            menubar.add(menu);

            /*
             * mitem=new JMenuItem("HTTP...");
             * 
             * mitem.addActionListener(this); mitem.setActionCommand("HTTP");
             * menu.add(mitem); mitem=new JMenuItem("SOCKS5...");
             * mitem.addActionListener(this); mitem.setActionCommand("SOCKS5");
             * menu.add(mitem); menubar.add(menu);
             */
            {
                TermChannelOptions sshOptions = getChannelOptions(SESSION_SSH, true);

                JMenu sshmenu = new JMenu("SSH");
                menu.add(sshmenu);
                mitem = new JMenuItem("Local Port Forwarding...");
                mitem.addActionListener(termController);
                mitem.setActionCommand("LocalPort");
                sshmenu.add(mitem);
                mitem = new JMenuItem("Remote Port Forwarding...");
                mitem.addActionListener(termController);
                mitem.setActionCommand("RemotePort");
                sshmenu.add(mitem);

                channelCompressionCB = new JCheckBoxMenuItem("Compression...");
                channelCompressionCB.addActionListener(termController);
                channelCompressionCB.setActionCommand("Compression");
                channelCompressionCB.setState(sshOptions.useChannelCompression);

                sshmenu.add(channelCompressionCB);

                {
                    JMenu xForwardingMenu = new JMenu("X Forwarding");
                    sshmenu.add(xForwardingMenu);

                    sshXForwardingCB = new JCheckBoxMenuItem("enable X Forwarding");
                    sshXForwardingCB.addActionListener(termController);
                    sshXForwardingCB.setActionCommand("XForwarding");
                    sshXForwardingCB.setState(sshOptions.useChannelXForwarding);
                    sshXForwardingCB.setEnabled(true);
                    xForwardingMenu.add(sshXForwardingCB);

                    mitem = new JMenuItem("X Forwarding Settings");
                    mitem.addActionListener(termController);
                    mitem.setActionCommand("XForwardingSettings");
                    mitem.setEnabled(false);

                    xForwardingMenu.add(mitem);
                }
            }

            {
                JMenu emulatorMenu = new JMenu("Emulator");
                menu.add(emulatorMenu);

                menuTypeVt100CB = new JCheckBoxMenuItem(VT10xEmulatorDefs.TERM_VT100);
                menuTypeVt100CB.addActionListener(termController);
                menuTypeVt100CB.setActionCommand(VT10xEmulatorDefs.TERM_VT100);
                emulatorMenu.add(menuTypeVt100CB);
                menuTypeVt100CB.setEnabled(false);

                menuTypeXtermCB = new JCheckBoxMenuItem(VT10xEmulatorDefs.TERM_XTERM);
                menuTypeXtermCB.addActionListener(termController);
                menuTypeXtermCB.setActionCommand(VT10xEmulatorDefs.TERM_XTERM);
                emulatorMenu.add(menuTypeXtermCB);
            }

            JSeparator sep = new JSeparator();
            menu.add(sep);

            // Font-> 
            {
                JMenu fontmenu = new JMenu("Font");
                menu.add(fontmenu);
                {
                    // Font->size
                    JMenu sizemenu = new JMenu("size");
                    fontmenu.add(sizemenu);

                    String sizes[] = { "7", "8", "9", "10", "11", "12", "13", "14", "16", "18",
                            "20", "24" };

                    for (String s : sizes) {
                        mitem = new JMenuItem(s);
                        mitem.addActionListener(termController);
                        mitem.setActionCommand("fontsize-" + s);
                        sizemenu.add(mitem);
                    }
                }
                // Font->type
                {
                    JMenu typemenu = new JMenu("type");
                    fontmenu.add(typemenu);

                    // selection of MONO spaced fonts; 
                    String types[] = { "Monospaced", "Courier", "Courier New", "Courier 10 Pitch",
                            "Luxi Mono", "Liberation Mono", "DejaVu Sans Mono",
                            "Lucida Sans Typewriter", "Andale Mono", "Impact" };

                    for (String s : types) {
                        mitem = new JMenuItem(s);
                        mitem.addActionListener(termController);
                        mitem.setActionCommand("fonttype-" + s);
                        typemenu.add(mitem);
                    }
                }
                // Font-Anti aliasing
                {
                    fontAAcheckBox = new JCheckBoxMenuItem("Anti aliasing");
                    fontAAcheckBox.addActionListener(termController);
                    fontAAcheckBox.setActionCommand("font-aa");
                    fontAAcheckBox.setState(terminalPanel.getFontInfo().hasAntiAliasing());
                    fontmenu.add(fontAAcheckBox);
                }

            }
            // Color ->
            {
                JMenu colormenu = new JMenu("Colors");
                menu.add(colormenu);
                {
                    JMenu schememenu = new JMenu("Scheme");
                    colormenu.add(schememenu);

                    String names[] = ColorMap.getColorMapNames();

                    for (int i = 0; i < names.length; i++) {
                        mitem = new JMenuItem(names[i]);
                        mitem.addActionListener(termController);
                        mitem.setActionCommand("colorscheme-" + names[i]);
                        schememenu.add(mitem);
                    }
                }
            }
            // Font-Anti aliasing
            {
                optionsSyncScrolling = new JCheckBoxMenuItem("Synchronize Scrolling");
                optionsSyncScrolling.addActionListener(termController);
                optionsSyncScrolling.setActionCommand("syncScrolling");
                optionsSyncScrolling.setState(terminalPanel.getSynchronizedScrolling());
                menu.add(optionsSyncScrolling);
            }

            sep = new JSeparator();
            menu.add(sep);
            {
                mitem = new JMenuItem("Save");
                mitem.addActionListener(termController);
                mitem.setActionCommand("SaveSettings");
                mitem.setVisible(_saveConfigEnabled);
                menu.add(mitem);
            }
        }

        menu = new JMenu("Help");
        menubar.add(menu);
        mitem = new JMenuItem("About...");
        mitem.addActionListener(termController);
        mitem.setActionCommand("About");
        menu.add(mitem);
        mitem = new JMenuItem("Debug ON");
        mitem.addActionListener(termController);
        mitem.setActionCommand("DebugON");
        menu.add(mitem);
        mitem = new JMenuItem("Debug OFF");
        mitem.addActionListener(termController);
        mitem.setActionCommand("DebugOFF");
        menu.add(mitem);
        mitem = new JMenuItem("Test Screen");
        mitem.addActionListener(termController);
        mitem.setActionCommand("testscreen");
        menu.add(mitem);
        //updateMenuSettings();
        return menubar;
    }

    //    public void updateMenuSettings()
    //    {
    //        getChannelOptions().useChannelCompression = compressionCheckBoxMenuItem.getState();
    //        getChannelOptions().userChannelXForwarding  = x11CheckBoxMenuItem.getState();
    //    }

    public void setTitle(String str) {
        super.setTitle("VLTerm:" + str);
    }

    private void exit(int i) {
        dispose();
    }

    @Override
    public void dispose() {
        super.dispose();

        terminateSession();
        if (terminalPanel != null)
            this.terminalPanel.dispose();

        this.terminalPanel = null;
    }

    // after change of graphics: repack main Frame.

    void updateFrameSize() {
        Runnable update = new Runnable() {
            public void run() {
                terminalPanel.setSize(terminalPanel.getPreferredSize());
                setSize(getPreferredSize());
                pack();
            }
        };

        SwingUtilities.invokeLater(update);
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        Component source = (Component) e.getSource();

        //ChannelOptions options = this.getChannelOptions(); 

        if (action.compareTo(SESSION_BASH) == 0) {
            this.sessionType = SESSION_BASH;
            startSession();
        } else if (action.compareTo(SESSION_TELNET) == 0) {
            if (thread == null) {
                this.sessionType = SESSION_TELNET;

                InputDialog dialog = new InputDialog("Enter username@hostname", "", false);

                String str = dialog.getText();

                parseLocation("telnet", str);

                startSession();
            }
        } else if (action.compareTo(SESSION_SSH) == 0) {
            if (thread == null) {
                this.sessionType = SESSION_SSH;

                String sessionstr = this.getConfigProperty("vterm.last.session.ssh");

                if (sessionstr == null) {
                    sessionstr = startURI.toString();
                }

                String locstr = JOptionPane.showInputDialog(this, "Enter username@hostname",
                        sessionstr);

                if (locstr == null)
                    return;

                this.savePersistantProperty("vterm.last.session.ssh", locstr);
                parseLocation("ssh", locstr);
                startSession();

                return;
            }
        } else if (action.equals("XForwardingSettings")) {
            TermChannelOptions sshOptions = this.getChannelOptions(SESSION_SSH, true);

            String display = JOptionPane
                    .showInputDialog(
                            this,
                            "XDisplay name (hostname:0)",
                            (sshOptions.channelXForwardingHost == null) ? ""
                                    : (sshOptions.channelXForwardingHost + ":" + sshOptions.channelXForwardingPort));

            if (display == null) {
                sshOptions.useChannelXForwarding = false;
            }

            sshOptions.channelXForwardingHost = display.substring(0, display.indexOf(':'));
            sshOptions.channelXForwardingPort = Integer.parseInt(display.substring(display
                    .indexOf(':') + 1));

            // if port= ":0" - ":99" use:6000-6099 else use given port (>=1024)

            if (sshOptions.channelXForwardingPort < 1024) {
                sshOptions.channelXForwardingPort += 6000;
            }

            sshOptions.useChannelXForwarding = true;
            setChannelOptions(SESSION_SSH, sshOptions);
        }

        else if (action.startsWith("fontsize")) {
            String strs[] = action.split("-");

            if (strs.length > 1 && strs[1] != null) {
                Integer val = Integer.valueOf(strs[1]);
                this.terminalPanel.updateFontSize(val, true);

                this.sessionProperties.setProperty("vlterm.font.size", "" + val);
            }

            terminalPanel.repaintGraphics(false);
            updateFrameSize();
        } else if (action.startsWith("fonttype")) {
            String strs[] = action.split("-");

            if (strs.length > 1 && strs[1] != null) {
                this.terminalPanel.updateFontType(strs[1], true);
                this.sessionProperties.setProperty("vlterm.font.type", "" + strs[1]);
            }

            terminalPanel.repaintGraphics(false);
            updateFrameSize();
        } else if (action.startsWith("colorscheme")) {
            String strs[] = action.split("-");

            if (strs.length > 1 && strs[1] != null) {
                String name = strs[1];
                terminalPanel.updateColorMap(ColorMap.getColorMap(name), true);
                this.sessionProperties.setProperty("vlterm.color.scheme", name);
            }

            updateFrameSize();
        } else if (action.startsWith("syncScrolling")) {
            boolean state = this.optionsSyncScrolling.getState();
            terminalPanel.setSynchronizedScrolling(state);
            this.sessionProperties.setProperty("vlterm.syncScrolling", "" + state);
        } else if (action.compareTo("font-aa") == 0) {
            boolean val = fontAAcheckBox.getState();
            terminalPanel.getFontInfo().setAntiAliasing(val);
            this.sessionProperties.setProperty("vlterm.font.antiAliasing", "" + val);

            terminalPanel.repaintGraphics(false);
            updateFrameSize();
        }

        else if (action.equals("Close")) {
            terminateSession();
        } else if (action.equals("Repaint")) {
            terminalPanel.repaintGraphics(false);
        } else if (action.equals("ClearScreen")) {
            terminalPanel.clearAll();
        } else if (action.equals("Quit")) {
            exit(0);
        } else if (action.equals("DebugON")) {
            PLogger.getRootLogger().setLevelToDebug();
        } else if (action.equals("DebugOFF")) {
            PLogger.getRootLogger().setLevelToError();
        } else if (action.equals("About")) {
            this.showMessage(aboutText);
        } else if (action.equals("testscreen")) {
            terminalPanel.drawTestScreen();
        } else if (source == this.menuTypeVt100CB) {
            this.menuTypeVt100CB.setSelected(true);
            this.menuTypeXtermCB.setSelected(false);
            this.termType = VT10xEmulatorDefs.TERM_VT100;
            this.setTermType(this.termType);
        } else if (source == this.menuTypeXtermCB) {
            this.menuTypeVt100CB.setSelected(false);
            this.menuTypeXtermCB.setSelected(true);
            this.termType = VT10xEmulatorDefs.TERM_XTERM;
            this.setTermType(this.termType);
        } else if (action.equals("SaveSettings")) {
            saveSettings();
        }
    }

    protected void openLocation(URI loc) throws IOException {
        // parse URI: 

        String scheme = loc.getScheme();
        String host = loc.getHost();

        if (StringUtil.isEmpty(host)) {
            host = "localhost";
        }

        String user = loc.getUserInfo();

        if (StringUtil.isEmpty(user)) {
            user = GlobalProperties.getGlobalUserName();
        }

        int port = loc.getPort();
        if (port <= 0)
            port = 22;

        String path = loc.getPath();

        try {
            startURI = new URI(scheme, user, host, port, path, null, null);
        } catch (URISyntaxException e) {
            throw new IOException(e.getMessage(), e);
        }

        this.startSession();
    }

    private void parseLocation(String scheme, String locstr) {
        try {

            String user;
            int port = 0;
            String host;

            if (locstr == null)
                return;

            // user ...@
            int s = locstr.indexOf('@');

            if (s < 0) {
                user = "";
            } else {
                user = locstr.substring(0, s);
                locstr = locstr.substring(s + 1);
            }

            // host ... :
            s = locstr.indexOf(':');
            if (s < 0) {
                host = locstr;
            } else {
                host = locstr.substring(0, s);
                locstr = locstr.substring(s + 1);
                port = Integer.valueOf(locstr);
            }

            this.startURI = new URI(scheme, user, host, port, null, null, null);
        } catch (Exception e) {
            this.showMessage("Invalid Location. Must be user@host[:port]. Value=" + locstr);
        }
    }

    /**
     * Creates new Thread which invokes run() method.
     */
    public void startSession() {
        this.thread = new Thread(this);
        this.thread.start();

        this.menuUpdateSessionAlive(true);
    }

    private void menuUpdateSessionAlive(boolean val) {
        sessionAlive = val;

        if (val == true) {
            startSSHMenuItem.setEnabled(false);
            startBashMenuItem.setEnabled(false);
            closeSessionMenuItem.setEnabled(true);
        } else {
            startSSHMenuItem.setEnabled(true);
            startBashMenuItem.setEnabled(true);
            closeSessionMenuItem.setEnabled(false);
        }
    }

    /**
     * Start session, will only return when session has ended
     */
    public void run() {
        executeSession();
    }

    protected void executeSession() {
        // ================
        // PRE SESSION !!!
        // ================
        if (startURI == null) {
            throw new NullPointerException("Start URI is NULL!");
        }

        InputStream inps = null;
        OutputStream outps = null;
        InputStream errs = null;

        // Complete Reset ! 
        terminalPanel.reset();

        logger.infoPrintf(">>> Starting Type=%s\n", sessionType);
        this.sessionAlive = true;

        if (this.sessionType.compareTo(SESSION_TELNET) == 0) {
            try {
                Socket sock = new Socket(startURI.getHost(), startURI.getPort());
                inps = sock.getInputStream();
                outps = sock.getOutputStream();

                // telnet vt100 identification
                // DOES NOT WORK !
                byte IAC = (byte) 255;
                byte WILL = (byte) 251;
                byte SB = (byte) 250;
                byte SE = (byte) 240;

                //
                // DOES NOT WORK:
                //

                byte bytes[] = {
                        // terminal type
                        IAC, WILL, (byte) 24, IAC, SB, (byte) 24, (byte) 0, 'V', 'T', '1', '0',
                        '0', IAC, SE,
                        // terminal speed
                        IAC, WILL, (byte) 32, IAC, SB, (byte) 32, (byte) 0, '9', '6', '0', '0',
                        ',', '9', '6', '0', '0', IAC, SE };

                outps.write(bytes);

                Emulator emulator = new VT10xEmulator(this.terminalPanel.getCharacterTerminal(),
                        inps, outps);
                // emulator.setErrorInput(errs);
                emulator.addListener(this.termController);

                terminalPanel.setEmulator(emulator);
                terminalPanel.requestFocus();
                // start input/ouput loop (method will not return)
                emulator.start();
                // exit

                // done:
                sock.close();
            } catch (Exception e) {
                logger.logException(PLogger.ERROR, e, "*** Exception:%s\n", e);
            } catch (Error e) {
                logger.logException(PLogger.ERROR, e, "*** Exception:%s\n", e);
            }

        } else if (this.sessionType.compareTo(SESSION_BASH) == 0) {

            try {
                this.shellChannel = this.termProvider.createChannel(SESSION_BASH, null, null, null,
                        null);
                shellChannel.connect();

                Emulator emulator = new VT10xEmulator(this.terminalPanel.getCharacterTerminal(),
                        shellChannel.getStdout(), shellChannel.getStdin());

                emulator.setErrorInput(errs);
                emulator.addListener(this.termController);
                // emulator.setErrorInput(errs);

                this.terminalPanel.setEmulator(emulator);
                terminalPanel.requestFocus();

                startShellProcessWatcher(shellChannel, emulator);

                // start input/output loop (method will not return)
                emulator.start();
            } catch (Exception ex) {
                logger.logException(PLogger.ERROR, ex, "Could start bash: %s\n", ex);
                showError(ex);
            }

            // if (bashChannel != null)
            //     bashChannel.disconnect(); 

        } else if (this.sessionType.compareTo(SESSION_SSH) == 0) {
            TermChannelOptions options = this.getChannelOptions(SESSION_SSH, true);

            try {
                // ================================
                // Only here is SSHChannel visible! 
                // ================================

                this.shellChannel = termProvider.createChannel(SESSION_SSH, startURI,
                        startURI.getUserInfo(), null, options);
                shellChannel.connect();

                Emulator emulator = new VT10xEmulator(this.terminalPanel.getCharacterTerminal(),
                        shellChannel.getStdout(), shellChannel.getStdin());

                this.terminalPanel.setEmulator(emulator);
                emulator.addListener(this.termController);

                // set focus to terminal panel:
                this.terminalPanel.requestFocus();

                emulator.reset();
                // start input/output loop (method will not return)
                emulator.start();

            } catch (Exception e) {
                logger.logException(PLogger.ERROR, e, "VLTerm Exception!\n");
                showError(e);
            } catch (Error e) {
                logger.logException(PLogger.ERROR, e, "Internal Error:%s\n", e);
                showError(e);
            }
        } else if (this.sessionType.compareTo(SESSION_SHELLCHANNEL) == 0) {
            try {
                // ================================
                // Use external shell channel 
                // ================================
                if (this.shellChannel == null)
                    throw new IOException("No Shell Channel specified!");
                // shellChannel.connect();

                Emulator emulator = new VT10xEmulator(this.terminalPanel.getCharacterTerminal(),
                        shellChannel.getStdout(), shellChannel.getStdin());

                this.terminalPanel.setEmulator(emulator);
                emulator.addListener(this.termController);

                // set focus to terminal panel:
                this.terminalPanel.requestFocus();

                emulator.reset();
                // start input/ouput loop (method will not return)
                emulator.start();

            } catch (Exception e) {
                logger.logException(PLogger.ERROR, e, "VLTerm Exception!\n");
                showError(e);
            } catch (Error e) {
                logger.logException(PLogger.ERROR, e, "Internal Error:%s\n", e);
                showError(e);
            }
        }
        // ================
        // POST SESSION !!!
        // ================

        logger.infoPrintf("*** Session Ended: emulator stopped.  ***\n");

        terminateSession(); // terminate if still running 
        menuUpdateSessionAlive(false);
        repaint();
        showMessage("Session Ended");
    }

    /** Watches a shell process and signals emulator when shell process died. */
    private void startShellProcessWatcher(final ShellChannel shellProc, final Emulator shellEmu) {
        Runnable run = new Runnable() {
            public void run() {
                int val = 0;

                try {
                    shellProc.waitFor();
                    val = shellProc.exitValue();
                } catch (InterruptedException e) {
                    logger.logException(PLogger.ERROR, e, "*** Interupted *** \n");
                }

                if (val == 0)
                    logger.infoPrintf("BASH Stopped normally. Exit value is 0.\n");
                else
                    logger.errorPrintf("*** Bash died abnormally. Exit value=%d\n", val);

                shellEmu.signalTerminate();
            }
        };

        Thread procWatcher = new Thread(run);
        procWatcher.start();
    }

    private void showError(Throwable e) {
        ExceptionDialog.show(this, e);
    }

    class InputDialog implements ActionListener {
        String result = null;

        Dialog dialog = null;

        TextField textf = null;

        InputDialog(String title, String text, boolean passwd) {
            super();
            dialog = new Dialog(new Frame(), title, true);
            Button ok = new Button("OK");
            Button cancel = new Button("CANCEL");
            textf = new TextField(20);
            textf.setText(text);
            if (passwd) {
                textf.setEchoCharacter('*');
            }
            dialog.setLayout(new FlowLayout());
            dialog.setLocation(100, 50);
            ok.addActionListener(this);
            cancel.addActionListener(this);
            dialog.add(textf);
            dialog.add(ok);
            dialog.add(cancel);
            dialog.pack();
            dialog.setVisible(true);
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
                if (!dialog.isVisible()) {
                    break;
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            String action = e.getActionCommand();
            if (action.equals("OK")) {
                result = textf.getText();
            } else if (action.equals("CANCEL")) {
            }
            dialog.setVisible(false);
            return;
        }

        String getText() {
            return result;
        }
    }

    protected void setShellChannel(ShellChannel chan) {
        this.sessionType = SESSION_SHELLCHANNEL;
        this.shellChannel = chan;
    }

    void showSplash() {
        this.terminalPanel.drawSplash();
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    protected Emulator getEmulator() {
        if (terminalPanel==null)
            return null; //terminal already disposed.
        return terminalPanel.getEmulator();
    }

    public void terminateSession() {
        this.menuUpdateSessionAlive(false);

        if (getEmulator() != null) {
            this.getEmulator().signalTerminate();
        }

        // Terminal; 
        if (this.terminalPanel != null) {
            this.terminalPanel.terminate();
        }

        // Shell Channel: 
        try {
            if (this.shellChannel != null) {
                this.shellChannel.disconnect(false);
            }
        } catch (IOException e) {
            logger.logException(PLogger.WARN, e, "Excpetion during disconnect:%s\n", this);
        }

        this.shellChannel = null;
        thread = null;
    }

    /**
     * Sends emulator the new preferred size. Depends on implementation whether this will be
     * respected
     * 
     * @param nr_columns
     * @param nr_rows
     */
    public void sendTermSize(int nr_columns, int nr_rows) {
        try {
            if (shellChannel != null)
                this.shellChannel.setTermSize(nr_columns, nr_rows, nr_columns, nr_rows);
        } catch (IOException e) {
            logger.logException(PLogger.ERROR, e, "IOException: Couldn't send terminal size\n");
        }

        if (this.getEmulator() != null)
            this.getEmulator().sendSize(nr_columns, nr_rows);
    }

    /**
     * Sends getEmulator() the new preferred size. Depends on implemenation whether this will be
     * respected
     * 
     * @param nr_columns
     * @param nr_rows
     */
    public void setTermType(String type) {
        try {
            if (shellChannel != null)
                this.shellChannel.setTermType(type);

        } catch (IOException e) {
            logger.logException(PLogger.ERROR, e, "IOException: Couldn't send terminal type:%s\n",
                    type);
        }

        // update getEmulator() 
        if (this.getEmulator() != null)
            this.getEmulator().setTermType(type);
    }

    Properties persistantProperties = null;

    String getConfigProperty(String name) {
        if (persistantProperties == null) {
            persistantProperties = loadProperties();

            if (persistantProperties == null)
                return null;
        }

        Object val = persistantProperties.getProperty(name);
        if (val != null)
            return val.toString();

        return null;
    }

    protected Properties loadProperties() {
        try {
            java.net.URL propFileUri = getPropertiesFileURL();
            return new ResourceLoader().loadProperties(propFileUri);
        } catch (Exception e) {
            logger.logException(PLogger.INFO, e, "VTerm:Couldn't load default properties: vterm.prop\n");
            return new Properties();
        }
    }

    /**
     * save persistant property
     */
    protected void savePersistantProperty(String name, String value) {
        if (persistantProperties == null) {
            persistantProperties = loadProperties();
        }

        persistantProperties.put(name, value);

        saveConfig();
    }

    public java.net.URL getPropertiesFileURL() throws IOException {
        FSUtil fsUtils = FSUtil.getDefault();
        return fsUtils.getUserHomeDir().resolveURI(".vterm/vterm.prop").toURL();
    }

    protected void saveConfig() {
        java.net.URL propFileUri = null;

        try {
            propFileUri = getPropertiesFileURL();
            new ResourceLoader().saveProperties(propFileUri, persistantProperties,
                    "VTerm Properties");
        } catch (Exception e) {
            logger.logException(Level.WARNING, e, "Couldn't write config to:%s\n", propFileUri);
        }

        return;
    }

    /**
     * Save current (view) settings
     */
    void saveSettings() {
        if (persistantProperties == null)
            persistantProperties = loadProperties();

        // copy session properties into persistant propereties
        persistantProperties.putAll(this.sessionProperties);
        saveConfig();
    }

}
