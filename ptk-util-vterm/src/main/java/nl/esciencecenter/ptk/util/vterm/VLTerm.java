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
///*
// * Copyright 2006-2010 Virtual Laboratory for e-Science (www.vl-e.nl)
// * Copyright 2012-2013 Netherlands eScience Center.
// *
// * Licensed under the Apache License, Version 2.0 (the "License").
// * You may not use this file except in compliance with the License. 
// * You may obtain a copy of the License at the following location:
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * 
// * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
// * ---
// */
//// source:
//
//package nl.esciencecenter.vlet.util.vlterm;
//
//import java.awt.BorderLayout;
//import java.awt.Button;
//import java.awt.Component;
//import java.awt.Dialog;
//import java.awt.FlowLayout;
//import java.awt.Frame;
//import java.awt.TextField;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.ComponentEvent;
//import java.awt.event.ComponentListener;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.awt.event.WindowListener;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.Socket;
//import java.util.Properties;
//import java.util.logging.Level;
//
//import javax.swing.JCheckBoxMenuItem;
//import javax.swing.JFrame;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
//import javax.swing.JOptionPane;
//import javax.swing.JSeparator;
//import javax.swing.SwingUtilities;
//
//import nl.esciencecenter.ptk.util.logging.ClassLogger;
//import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
//import nl.esciencecenter.vlet.VletConfig;
//import nl.esciencecenter.vlet.gui.UIGlobal;
//import nl.esciencecenter.vlet.gui.UILogger;
//import nl.esciencecenter.vlet.gui.dialog.ExceptionForm;
//import nl.esciencecenter.vlet.gui.util.charpane.ColorMap;
//import nl.esciencecenter.vlet.vfs.ssh.jcraft.SSHChannel;
//import nl.esciencecenter.vlet.vfs.ssh.jcraft.SSHChannel.SSHChannelOptions;
//import nl.esciencecenter.vlet.vrs.VRSContext;
//import nl.esciencecenter.vlet.vrs.io.VShellChannel;
//import nl.esciencecenter.vlet.vrs.vdriver.localfs.BASHChannel;
//
//public class VLTerm extends JFrame implements  Runnable
//{
//    // ========================================================================
//    
//    // ========================================================================
//    
//    private static ClassLogger logger;
//    
//    static
//    {
//        logger=ClassLogger.getLogger(VLTerm.class);
//        //logger.setLevelToDebug(); 
//    }
//    
//	private static final long serialVersionUID = -3209091621707504735L;
//
//	public static int verbose=0; // 0=silent, 1 = error and fixmes,2=debug,3 very debug  
//
//    private static final String SESSION_SSH = "SSH";
//
//    private static final String SESSION_BASH = "BASH";
//
//    private static final String SESSION_TELNET = "TELNET";
//    
//    private static final String SESSION_SHELLCHANNEL = "SHELLCHANNEL";
//
//    private static final String aboutText = 
//          "<html><center>VLTerm VT100+ Emulator<br>"
//        + "(beta version)<br>"
//        + "(C) VL-e consortium<br>"
//        + "Author Piter T. de Boer<br>"
//        + "Render Engine (C) Piter.NL</center></html>";
//    
//    // ========================================================================
//    
//    // ========================================================================
//    
//    public class TermController implements WindowListener,ComponentListener, EmulatorListener, ActionListener
//    {
//		private String shortTitle;
//	
//		private String longTitle;
//
//		public TermController()
//		{
//			
//		}
//		
//		public void componentHidden(ComponentEvent e)
//		{
//			
//		}
//
//		public void componentMoved(ComponentEvent e)
//		{
//		}
//
//		public void componentResized(ComponentEvent e)
//		{
//			if (e.getSource()==vlTerm)
//			{
//				sendTermSize(vlTerm.getColumnCount(),vlTerm.getRowCount());				 
//			}
//		}
//
//		public void componentShown(ComponentEvent e)
//		{
//			//vlTerm.charPane.startRenderers(); 
//			//vlTerm.charPane.repaint(); 
//		}
//
//		public void notifyGraphMode(int type, String arg) 
//		{
//			if (type==1)
//				this.shortTitle=arg;
//			else
//				this.longTitle=arg;
//        
//			if (shortTitle==null)
//				shortTitle="";
//			
//			if (longTitle==null)
//				longTitle="";
//        
//          // System.err.println(">>> NR="+type+" => "+token.strArg);  
//          setTitle("["+shortTitle+"] "+longTitle);
//		}
//
//		public void notifyCharSet(String charSet)
//		{
//			// charPane handle charset events
//		}
//
//		public void actionPerformed(ActionEvent e) 
//		{
//			// forward event
//			VLTerm.this.actionPerformed(e); 
//		}
//
//		public void windowActivated(WindowEvent e)
//		{
//			//vlTerm.charPane.startRenderers(); 
//		}
//
//		public void windowClosed(WindowEvent e)
//		{
//			
//		}
//
//		public void windowClosing(WindowEvent e)
//		{
//			
//		}
//
//		public void windowDeactivated(WindowEvent e)
//		{
//			//vlTerm.charPane.stopRenderers(); 
//		}
//
//		public void windowDeiconified(WindowEvent e)
//		{
//			vlTerm.activate(); // charPane.startRenderers(); 
//		}
//
//		public void windowIconified(WindowEvent e)
//		{
//			vlTerm.inactivate(); // charPane.stopRenderers(); 
//		}
//
//		public void windowOpened(WindowEvent e)
//		{
//			vlTerm.activate(); // charPane.startRenderers(); 
//		}
//
//		public void notifyResized(int columns,int rows) 
//		{
//			// charPane has already been resized: Update frame!
//			VLTerm.this.updateFrameSize(); 
//		}
//
//    }
//    // =======================================================================
//    //
//    // =======================================================================
//    //
//    // options/config: 
//    boolean _saveConfigEnabled=true; 
//
//    String termType=VT10xEmulatorDefs.TERM_XTERM; 
//    
//    String host = "localhost";
//
//    String user = "user";
//
//    int port = 22;
//
//    SSHChannelOptions sshOptions=new SSHChannelOptions(); 
//
//    // =======================
//    // Session fields
//    // =======================
//
//    String sessionType = SESSION_SSH;
//
//    TermPanel vlTerm;
//    
//    Thread thread = null;
//    
//    Emulator emulator = null;
//
//    private VShellChannel shellChannel;
//    
//    // 
//    private boolean sessionAlive;
//    
//    // Menu 
//    private boolean isResizable = true;
//
//    // ==============
//    // GUI 
//    // ==============
//    
//    private JMenuBar menu;
//    private JCheckBoxMenuItem x11CheckBoxMenuItem;
//    private JCheckBoxMenuItem compressionCheckBoxMenuItem;
//    private JCheckBoxMenuItem fontAAcheckBox;
//    private JMenuItem startBashMenuItem;
//    private JMenuItem closeSessionMenuItem;
//    private JMenuItem startSSHMenuItem;
//	private JCheckBoxMenuItem menuTypeVt100CB;
//	private JCheckBoxMenuItem menuTypeXtermCB;
//	private TermController termController;
//    private JCheckBoxMenuItem optionsSyncScrolling;
//    
//    /** Current view properties */ 
//    private Properties sessionProperties=new Properties();
//    
//    public VLTerm()
//    {
//    	// defaults: 
//        this.user = VletConfig.getUserName();
//
//        loadConfigSettings(); 
//        
//        // gui uses loaded settings !
//        initGui();
//    }
//    
//    void loadConfigSettings()
//    {
//    	this.persistantProperties=loadProperties();
//    }
//    
//    public void initGui()
//    {
//        this.setTitle("");
//
//        this.setLayout(new BorderLayout());
//
//    	this.termController=new TermController(); 
//
//        {
//            vlTerm = new TermPanel();
//            vlTerm.initGUI(); 
//            this.add(vlTerm, BorderLayout.CENTER);
//        }
//
//        {
//            menu = this.createMenuBar();
//            this.setJMenuBar(menu);
//        }
//
//        // Apply loaded config settings to CharPane !
//        this.applyGraphicConfigSettings(); 
//        
//        //setResizable(this.isResizable);
//
//        // Listeners:
//        {
//        	this.addWindowListener(new WindowAdapter()
//        	{
//        		public void windowClosing(WindowEvent e)
//        		{
//        			exit(0); 
//        		}
//        	});
//        	
//           	this.vlTerm.addComponentListener(termController);
//           	this.addComponentListener(termController);
//           	this.addWindowListener(termController);
//        	//this.vlTerm.addTermListener(termPanelListener); 
//        }
//        
//        this.pack(); 
//    }
//
//    
//    /** Apply current configured properties to CharPane. Does reset graphics ! */ 
//    void applyGraphicConfigSettings()
//    {
//    	// update configuration: 
//    	String propStr = (String) persistantProperties.get("vlterm.syncScrolling");
//    	if (propStr!=null)
//    	{
//    		boolean val=Boolean.parseBoolean(propStr);
//    	 	this.optionsSyncScrolling.setSelected(val); 
//    	 	this.vlTerm.setSynchronizedScrolling(val); 
//    	}
//    	
//    	// directly set charPane options: do not reset graphics each time !
//    	propStr = (String) persistantProperties.get("vlterm.color.scheme");
//    	if (propStr!=null)
//    	 	this.vlTerm.updateColorMap(ColorMap.getColorMap(propStr),false);
//
//    	propStr = (String) persistantProperties.get("vlterm.font.type");
//    	if (propStr!=null)
//    		this.vlTerm.updateFontType(propStr,false); 
//    	
//    	propStr = (String) persistantProperties.get("vlterm.font.size");
//    	if (propStr!=null)
//    		this.vlTerm.updateFontSize(Integer.parseInt(propStr),false); 
//    
//    	propStr = (String) persistantProperties.get("vlterm.font.antiAliasing");
//    	
//    	if (propStr!=null)
//    	{
//    		boolean val=Boolean.parseBoolean(propStr);
//    		this.vlTerm.getFontInfo().setAntiAliasing(val); 
//    		this.fontAAcheckBox.setSelected(val);  
//    	}
//    	
//    	// reset graphics=initialize graphics
//    	this.vlTerm.resetGraphics(); 
//    	// this.updateFrameSize(); 
//    }
//    
//    
//    public JMenuBar createMenuBar()
//    {
//        JMenuBar menubar = new JMenuBar();
//        JMenu menu;
//        JMenuItem mitem;
//
//        menu = new JMenu("Session");
//        menubar.add(menu);
//
//        startSSHMenuItem = new JMenuItem("Start SSH Session...");
//        menu.add(startSSHMenuItem);
//        startSSHMenuItem.addActionListener(termController);
//        startSSHMenuItem.setActionCommand(SESSION_SSH);
//
//        /*
//         * mitem=new JMenuItem("Start Telnet Session..."); menu.add(mitem);
//         * mitem.addActionListener(this);
//         * mitem.setActionCommand(SESSION_TELNET);
//         * 
//         * 
//         */
//        startBashMenuItem=new JMenuItem("Start BASH Session...");
//        menu.add(startBashMenuItem);
//        startBashMenuItem.addActionListener(termController);
//        startBashMenuItem.setActionCommand(SESSION_BASH);
//        menu.add(startBashMenuItem);
//
//        {
//        	mitem = new JMenuItem("Repaint");
//        	menu.add(mitem);
//        	mitem.addActionListener(termController);
//        	mitem.setActionCommand("Repaint");
//        }
//        
//        {
//        	mitem = new JMenuItem("Clear Screen");
//        	menu.add(mitem);
//        	mitem.addActionListener(termController);
//        	mitem.setActionCommand("ClearScreen");
//        }
//        
//        {
//        	closeSessionMenuItem = new JMenuItem("Terminate Session");
//        	menu.add(closeSessionMenuItem);
//        	closeSessionMenuItem.addActionListener(termController);
//        	closeSessionMenuItem.setActionCommand("Close");
//        	closeSessionMenuItem.setEnabled(false); 
//        }
//        
//        mitem = new JMenuItem("Quit VLTerm");
//        menu.add(mitem);
//        mitem.addActionListener(termController);
//        mitem.setActionCommand("Quit");
//        
//
//        // SETTINGS MENU
//        {
//            menu = new JMenu("Settings");
//            menubar.add(menu);
//
//            /*
//             * mitem=new JMenuItem("HTTP...");
//             * 
//             * mitem.addActionListener(this); mitem.setActionCommand("HTTP");
//             * menu.add(mitem); mitem=new JMenuItem("SOCKS5...");
//             * mitem.addActionListener(this); mitem.setActionCommand("SOCKS5");
//             * menu.add(mitem); menubar.add(menu);
//             */
//            {
//                JMenu sshmenu = new JMenu("SSH");
//                menu.add(sshmenu);
//                mitem = new JMenuItem("Local Port Forwarding...");
//                mitem.addActionListener(termController);
//                mitem.setActionCommand("LocalPort");
//                sshmenu.add(mitem);
//                mitem = new JMenuItem("Remote Port Forwarding...");
//                mitem.addActionListener(termController);
//                mitem.setActionCommand("RemotePort");
//                sshmenu.add(mitem);
//
//                compressionCheckBoxMenuItem = new JCheckBoxMenuItem(
//                "Compression...");
//                compressionCheckBoxMenuItem.addActionListener(termController);
//                compressionCheckBoxMenuItem.setActionCommand("Compression");
//                compressionCheckBoxMenuItem.setState(this.sshOptions.compression);
//                sshmenu.add(compressionCheckBoxMenuItem);
//
//                {
//                    JMenu x11menu = new JMenu("X11 Forwarding");
//                    sshmenu.add(x11menu);
//    
//                    x11CheckBoxMenuItem = new JCheckBoxMenuItem(
//                    "enable X11 Forwarding");
//                    x11CheckBoxMenuItem.addActionListener(termController);
//                    x11CheckBoxMenuItem.setActionCommand("X11Forwarding");
//                    x11CheckBoxMenuItem.setState(this.sshOptions.xforwarding);
//                    x11menu.add(x11CheckBoxMenuItem);
//    
//                    mitem = new JMenuItem("X11 Forwarding Settings");
//                    mitem.addActionListener(termController);
//                    mitem.setActionCommand("X11ForwardingSettings");
//                    x11menu.add(mitem);
//                }
//            }
//            {
//                JMenu emulatorMenu = new JMenu("Emulator");
//                menu.add(emulatorMenu);
//
//                menuTypeVt100CB = new JCheckBoxMenuItem(VT10xEmulatorDefs.TERM_VT100);
//                menuTypeVt100CB.addActionListener(termController);
//                menuTypeVt100CB.setActionCommand(VT10xEmulatorDefs.TERM_VT100);
//                emulatorMenu.add(menuTypeVt100CB);
//                
//                menuTypeXtermCB = new JCheckBoxMenuItem(VT10xEmulatorDefs.TERM_XTERM);
//                menuTypeXtermCB.addActionListener(termController);
//                menuTypeXtermCB.setActionCommand(VT10xEmulatorDefs.TERM_XTERM);
//                emulatorMenu.add(menuTypeXtermCB);
//            }
//
//            JSeparator sep = new JSeparator();
//            menu.add(sep);
//
//            // Font-> 
//            {
//                JMenu fontmenu = new JMenu("Font");
//                menu.add(fontmenu);
//                {
//                    // Font->size
//                    JMenu sizemenu = new JMenu("size");
//                    fontmenu.add(sizemenu);
//
//                    String sizes[] =
//                    { "7","8", "9", "10", "11", "12","13", "14", "16", "18", "20", "24" };
//
//                    for (String s : sizes)
//                    {
//                        mitem = new JMenuItem(s);
//                        mitem.addActionListener(termController);
//                        mitem.setActionCommand("fontsize-" + s);
//                        sizemenu.add(mitem);
//                    }
//                }
//                // Font->type
//                {
//                    JMenu typemenu = new JMenu("type");
//                    fontmenu.add(typemenu);
//
//                    // selection of MONO spaced fonts; 
//                    String types[] =  { 
//                    		"Monospaced", 
//                    		"Courier",
//                    		"Courier New",
//                    		"Courier 10 Pitch",
//                    		"Luxi Mono",
//                    		"Liberation Mono",
//                    		"DejaVu Sans Mono",
//                    		"Lucida Sans Typewriter",
//                    		"Andale Mono",
//                    		"Impact"};
//
//                    for (String s : types)
//                    {
//                        mitem = new JMenuItem(s);
//                        mitem.addActionListener(termController);
//                        mitem.setActionCommand("fonttype-" + s);
//                        typemenu.add(mitem);
//                    }
//                }
//                // Font-Anti aliasing
//                {
//                    fontAAcheckBox = new JCheckBoxMenuItem("Anti aliasing");
//                    fontAAcheckBox.addActionListener(termController);
//                    fontAAcheckBox.setActionCommand("font-aa");
//                    fontAAcheckBox.setState(vlTerm.getFontInfo().hasAntiAliasing());
//                    fontmenu.add(fontAAcheckBox);
//                }
//
//            }
//            // Color ->
//            {
//                    JMenu colormenu = new JMenu("Colors");
//                    menu.add(colormenu);
//                    {
//                        JMenu schememenu = new JMenu("Scheme");
//                        colormenu.add(schememenu);
//                        
//                        String names[] = ColorMap.getColorMapNames();
//                     
//                        for (int i=0;i<names.length;i++)
//                        {
//                            mitem = new JMenuItem(names[i]);
//                            mitem.addActionListener(termController);
//                            mitem.setActionCommand("colorscheme-" + names[i]);
//                            schememenu.add(mitem);
//                        }
//                    }
//            }
//            // Font-Anti aliasing
//            {
//                    optionsSyncScrolling = new JCheckBoxMenuItem("Synchronize Scrolling");
//                    optionsSyncScrolling.addActionListener(termController);
//                    optionsSyncScrolling.setActionCommand("syncScrolling");
//                    optionsSyncScrolling.setState(vlTerm.getSynchronizedScrolling());
//                    menu.add(optionsSyncScrolling);
//            }
//            
//            sep = new JSeparator();
//            menu.add(sep);
//            {
//            	mitem = new JMenuItem("Save");
//            	mitem.addActionListener(termController);
//            	mitem.setActionCommand("SaveSettings");
//            	mitem.setVisible(_saveConfigEnabled); 
//            	menu.add(mitem);
//            }
//        }
//
//        menu = new JMenu("Help");
//        menubar.add(menu);
//        mitem = new JMenuItem("About...");
//        mitem.addActionListener(termController);
//        mitem.setActionCommand("About");
//        menu.add(mitem);
//        mitem = new JMenuItem("Debug ON");
//        mitem.addActionListener(termController);
//        mitem.setActionCommand("DebugON");
//        menu.add(mitem);
//        mitem = new JMenuItem("Debug OFF");
//        mitem.addActionListener(termController);
//        mitem.setActionCommand("DebugOFF");
//        menu.add(mitem);
//        mitem = new JMenuItem("Test Screen");
//        mitem.addActionListener(termController);
//        mitem.setActionCommand("testscreen");
//        menu.add(mitem);
//        updateMenuSettings();
//        return menubar;
//    }
//
//    public void updateMenuSettings()
//    {
//        this.sshOptions.compression = compressionCheckBoxMenuItem.getState();
//        this.sshOptions.xforwarding = x11CheckBoxMenuItem.getState();
//    }
//    
//    public void setTitle(String str)
//    {
//        super.setTitle("VLTerm:"+str);
//    }
//    
//  
//    private void exit(int i)
//    {
//    	dispose(); 
//    }
//    
//    
//    @Override
//    public void dispose()
//    {
//    	super.dispose(); 
//    	
//        terminateSession();
//        if (vlTerm!=null)
//        	this.vlTerm.dispose();
//        
//        this.vlTerm=null; 
//    }
//
//    // after change of graphics: repack main Frame.
//
//    void updateFrameSize()
//    {
//    	Runnable update=new Runnable()
//    	{
//    		public void run()
//    		{
//    	    	vlTerm.setSize(vlTerm.getPreferredSize()); 
//    	    	setSize(getPreferredSize());
//    	        pack();
//    		}
//    	};
//    	
//    	SwingUtilities.invokeLater(update); 
//    }
//
//    public void actionPerformed(ActionEvent e)
//    {
//        String action = e.getActionCommand();
//        Component source=(Component)e.getSource(); 
//        
//        if (action.compareTo(SESSION_BASH) == 0)
//        {
//            this.sessionType = SESSION_BASH;
//            startSession();
//        }
//        else if (action.compareTo(SESSION_TELNET) == 0)
//        {
//            if (thread == null)
//            {
//                this.sessionType = SESSION_TELNET;
//
//                InputDialog dialog = new InputDialog("Enter username@hostname",
//                        "", false);
//
//                String str = dialog.getText();
//
//                parseLocation(str);
//
//                startSession();
//            }
//        }
//        else if (action.compareTo(SESSION_SSH) == 0)
//        {
//            if (thread == null)
//            {
//                this.sessionType = SESSION_SSH;
//                
//                String sessionstr=this.getConfigProperty("vlterm.last.session.ssh"); 
//                if (sessionstr==null)
//            	{
//                	sessionstr=user+"@"+host; 
//            	}
//                
//                String locstr = JOptionPane.showInputDialog(this,
//                        "Enter username@hostname",sessionstr);
//                
//                if (locstr == null)
//                    return;
//                
//                this.savePersistantProperty("vlterm.last.session.ssh",locstr); 
//                
//                parseLocation(locstr);
//
//                startSession();
//
//                return;
//
//            }
//        }
//        else if (action.equals("X11ForwardingSettings"))
//        {
//            String display = JOptionPane.showInputDialog(this,
//                    "XDisplay name (hostname:0)", (sshOptions.xhost == null) ? "" : (sshOptions.xhost
//                            + ":" + sshOptions.xport));
//
//            if (display == null)
//                sshOptions.xforwarding = false;
//
//            sshOptions.xhost = display.substring(0, display.indexOf(':'));
//            sshOptions.xport = Integer.parseInt(display
//                    .substring(display.indexOf(':') + 1));
//
//            // if port= ":0" - ":99" use:6000-6099 else use given port (>=1024)
//
//            if (sshOptions.xport < 1024)
//                sshOptions.xport += 6000;
//
//            sshOptions.xforwarding = true;
//        }
//
//        else if (action.startsWith("fontsize"))
//        {
//            String strs[] = action.split("-");
//
//            if (strs.length > 1 && strs[1] != null)
//            {
//                Integer val = Integer.valueOf(strs[1]);
//                this.vlTerm.updateFontSize(val,true);
//                
//                this.sessionProperties.setProperty("vlterm.font.size",""+val); 
//            }
//
//            vlTerm.repaintGraphics(false);
//            
//            updateFrameSize();
//
//        }
//        else if (action.startsWith("fonttype"))
//        {
//            String strs[] = action.split("-");
//
//            if (strs.length > 1 && strs[1] != null)
//            {
//                this.vlTerm.updateFontType(strs[1],true);
//                this.sessionProperties.setProperty("vlterm.font.type",""+strs[1]); 
//            }
//            
//            vlTerm.repaintGraphics(false); 
//
//            updateFrameSize();
//
//        }
//        else if (action.startsWith("colorscheme"))
//        {
//            String strs[] = action.split("-");
//
//            if (strs.length > 1 && strs[1] != null)
//            {
//                String name=strs[1]; 
//                vlTerm.updateColorMap(ColorMap.getColorMap(name),true);  
//                this.sessionProperties.setProperty("vlterm.color.scheme",name); 
//            }
//                       
//            updateFrameSize();
//        }
//        else if (action.startsWith("syncScrolling"))
//        {
//            boolean state= this.optionsSyncScrolling.getState();
//            vlTerm.setSynchronizedScrolling(state); 
//            this.sessionProperties.setProperty("vlterm.syncScrolling",""+state); 
//        }
//        else if (action.compareTo("font-aa")==0)
//        {
//           // System.err.println("font-aa");
//            boolean val=fontAAcheckBox.getState();
//            
//          //  System.err.println("val:"+val);
//
//            vlTerm.getFontInfo().setAntiAliasing(val);
//            this.sessionProperties.setProperty("vlterm.font.antiAliasing",""+val); 
//
//            vlTerm.repaintGraphics(false); 
//            updateFrameSize();
//        }
//        
//        else if (action.equals("Close"))
//        {
//            terminateSession();
//        }
//        else if (action.equals("Repaint"))
//        {
//        	vlTerm.repaintGraphics(false); 
//        }
//        else if (action.equals("ClearScreen"))
//        {
//        	vlTerm.clearAll(); 
//        }
//        else if (action.equals("Quit"))
//        {
//            exit(0); 
//        }
//        else if (action.equals("DebugON"))
//        {
//        	ClassLogger.getRootLogger().setLevelToDebug();  
//        }
//        else if (action.equals("DebugOFF"))
//        {
//            ClassLogger.getRootLogger().setLevelToError();  
//        }
//        else if (action.equals("About"))
//        {
//            this.showMessage(aboutText);
//        }
//        else if (action.equals("testscreen"))
//        {
//            vlTerm.drawTestScreen(); 
//        }
//        else if (source==this.menuTypeVt100CB)
//        {
//        	this.menuTypeVt100CB.setSelected(true);
//        	this.menuTypeXtermCB.setSelected(false); 
//        	this.termType=VT10xEmulatorDefs.TERM_VT100; 
//        	this.setTermType(this.termType); 
//        }
//        else if (source==this.menuTypeXtermCB)
//        {
//        	this.menuTypeVt100CB.setSelected(false);
//        	this.menuTypeXtermCB.setSelected(true); 
//        	this.termType=VT10xEmulatorDefs.TERM_XTERM;
//        	this.setTermType(this.termType); 
//        }
//        else if(action.equals("SaveSettings"))
//        {
//        	saveSettings();
//        }	
//    }
//
//    protected void openLocation(VRL loc)
//    {
//    	this.user=loc.getUserinfo(); 
//    	this.host=loc.getHostname(); 
//    	this.port=22;
//    	if (loc.getPort()>0)
//    		this.port=loc.getPort(); 
//    	
//    	
//    	this.startSession(); 
//	}
//    
//	private void parseLocation(String str)
//    {
//
//        if (str == null)
//            return;
//
//        // user ...@
//        int s = str.indexOf('@');
//
//        if (s < 0)
//        {
//            user = "";
//        }
//        else
//        {
//            user = str.substring(0, s);
//            str = str.substring(s + 1);
//        }
//
//        // host ... :
//        s = str.indexOf(':');
//        if (s < 0)
//        {
//            host = str;
//        }
//        else
//        {
//            host = str.substring(0, s);
//            str = str.substring(s + 1);
//            port = Integer.valueOf(str);
//        }
//    }
//
//    /**
//     * Creates new Thread which invokes run() method.
//     */
//    public void startSession()
//    {
//        this.thread = new Thread(this);
//        this.thread.start();
//        
//        this.menuUpdateSessionAlive(true); 
//    }
//
//    private void menuUpdateSessionAlive(boolean val)
//    {
//        sessionAlive=val; 
//        
//        if (val==true)
//        {
//            startSSHMenuItem.setEnabled(false);
//            startBashMenuItem.setEnabled(false); 
//            closeSessionMenuItem.setEnabled(true);
//        }
//        else
//        {
//            startSSHMenuItem.setEnabled(true);
//            startBashMenuItem.setEnabled(true); 
//            closeSessionMenuItem.setEnabled(false);
//        }
//    }
//
//    /**
//     * Start session, will only return when session has ended
//     */
//    public void run()
//    {
//    	executeSession();
//    }
//    
//    private void executeSession()
//    {
//        // ================
//        // PRE SESSION !!!
//        // ================
//
//        InputStream inps = null;
//        OutputStream outps = null;
//        InputStream errs = null;
//        
//        // Complete Reset ! 
//        vlTerm.reset(); 
//
//        logger.infoPrintf(">>> Starting Type=%s\n",sessionType);
//        this.sessionAlive = true;
//        
//        if (this.sessionType.compareTo(SESSION_TELNET) == 0)
//        {
//            try
//            {
//                Socket sock = new Socket(host, port);
//                inps = sock.getInputStream();
//                outps = sock.getOutputStream();
//
//                // telnet vt100 identification
//                // DOES NOT WORK !
//                byte IAC = (byte) 255;
//                byte WILL = (byte) 251;
//                byte SB = (byte) 250;
//                byte SE = (byte) 240;
//
//                //
//                // DOES NOT WORK:
//                //
//
//                byte bytes[] =
//                {
//                        // terminal type
//                        IAC, WILL, (byte) 24, IAC, SB, (byte) 24, (byte) 0,
//                        'V', 'T', '1', '0', '0', IAC, SE,
//                        // terminal speed
//                        IAC, WILL, (byte) 32, IAC, SB, (byte) 32, (byte) 0,
//                        '9', '6', '0', '0', ',', '9', '6', '0', '0', IAC, SE };
//
//                outps.write(bytes);
//
//                emulator = new VT10xEmulator(this.vlTerm.getCharacterTerminal(), inps, outps);
//                // emulator.setErrorInput(errs);
//                emulator.addListener(this.termController);
//                
//                vlTerm.setEmulator(emulator);
//                vlTerm.requestFocus();
//                // start input/ouput loop (method will not return)
//                emulator.start();
//                // exit
//
//                // done:
//                sock.close();
//            }
//            catch (Exception e)
//            {
//                logger.logException(ClassLogger.ERROR,e,"*** Exception:%s\n",e);
//            }
//            catch (Error e)
//            {
//                logger.logException(ClassLogger.ERROR,e,"*** Exception:%s\n",e);
//            }
//
//        }
//        else if (this.sessionType.compareTo(SESSION_BASH) == 0)
//        {
//            
//            try
//            {
//                // ================================
//                // Only here is BASHChannel visible! 
//                // ================================
//                
//                BASHChannel bashChannel = BASHChannel.create(); 
//                bashChannel.connect(); 
//                
//                emulator = new VT10xEmulator(this.vlTerm.getCharacterTerminal(),bashChannel.getStdout(),bashChannel.getStdin()); 
//                
//                emulator.setErrorInput(errs);
//                emulator.addListener(this.termController);
//                // emulator.setErrorInput(errs);
//
//                this.vlTerm.setEmulator(emulator);
//                vlTerm.requestFocus();
//                
//                this.shellChannel=bashChannel; 
//                startShellProcessWatcher(shellChannel,emulator); 
//                
//                // start input/output loop (method will not return)
//                emulator.start();
//            }
//            catch (Exception ex)
//            {
//                logger.logException(ClassLogger.ERROR,ex,"Could start bash: %s\n",ex);
//                showError(ex); 
//            }
//
//           // if (bashChannel != null)
//           //     bashChannel.disconnect(); 
//
//        }
//        else if (this.sessionType.compareTo(SESSION_SSH) == 0)
//        {
//            try
//            {
//                // ================================
//                // Only here is SSHChannel visible! 
//                // ================================
//                
//                SSHChannel sshChannel = SSHChannel.createSSHChannel(UIGlobal.getVRSContext(),user, host, port, sshOptions); 
//                
//                sshChannel.connect();
//                
//                emulator = new VT10xEmulator(this.vlTerm.getCharacterTerminal(),sshChannel.getStdout(), sshChannel.getStdin()); 
//                shellChannel=sshChannel;
//                
//                this.vlTerm.setEmulator(emulator);
//                emulator.addListener(this.termController);
//                    
//                // set focus to terminal panel:
//                this.vlTerm.requestFocus();  
//                    
//                emulator.reset();
//                // start input/output loop (method will not return)
//                emulator.start();
//
//            }
//            catch (Exception e)
//            {
//                logger.logException(ClassLogger.ERROR,e,"VLTerm Exception!\n"); 
//                showError(e); 
//            }
//            catch (Error e)
//            {
//                logger.logException(ClassLogger.ERROR,e,"Internal Error:%s\n",e);  
//                showError(e); 
//            }
//        }
//        else if (this.sessionType.compareTo(SESSION_SHELLCHANNEL) == 0)
//        {
//            try
//            {
//                // ================================
//                // Use external shell channel 
//                // ================================
//                if (this.shellChannel==null)
//                    throw new IOException("No Shell Channel specified!"); 
//                // shellChannel.connect();
//                
//                emulator = new VT10xEmulator(this.vlTerm.getCharacterTerminal(),shellChannel.getStdout(), shellChannel.getStdin()); 
//                
//                this.vlTerm.setEmulator(emulator);
//                emulator.addListener(this.termController);
//                    
//                // set focus to terminal panel:
//                this.vlTerm.requestFocus();  
//                    
//                emulator.reset();
//                // start input/ouput loop (method will not return)
//                emulator.start();
//
//            }
//            catch (Exception e)
//            {
//                logger.logException(ClassLogger.ERROR,e,"VLTerm Exception!\n"); 
//                showError(e); 
//            }
//            catch (Error e)
//            {
//                logger.logException(ClassLogger.ERROR,e,"Internal Error:%s\n",e);  
//                showError(e); 
//            }
//        }
//        // ================
//        // POST SESSION !!!
//        // ================
//
//        logger.infoPrintf("*** Session Ended: emulator stopped.  ***\n");
//
//        terminateSession(); // terminate if still running 
//        menuUpdateSessionAlive(false); 
//        repaint();
//        showMessage("Session Ended");
//    }
//
//    /** Watches a shell process and signals emulator when shell process died. */ 
//    private void startShellProcessWatcher(final VShellChannel shellProc, final Emulator shellEmu) 
//    {
//         Runnable run = new Runnable()
//         {
//             public void run()
//             {
//                 int val = 0;
//
//                 try
//                 {
//                     shellProc.waitFor();
//                     val = shellProc.exitValue();
//                 }
//                 catch (InterruptedException e)
//                 {
//                     logger.logException(ClassLogger.ERROR,e,"*** Interupted *** \n");
//                 }
//                 
//                 if (val==0)
//                     logger.infoPrintf("BASH Stopped normally. Exit value is 0.\n"); 
//                 else
//                     logger.errorPrintf("*** Bash died abnormally. Exit value=%d\n",val); 
//                 
//                 shellEmu.signalTerminate(); 
//             }
//         };
//
//         Thread procWatcher = new Thread(run);
//         procWatcher.start();
//	}
//
//
//	private void showError(Throwable e)
//    {
//    	ExceptionForm.show(e); 
// 	}
//
//	class InputDialog implements ActionListener
//    {
//        String result = null;
//
//        Dialog dialog = null;
//
//        TextField textf = null;
//
//        InputDialog(String title, String text, boolean passwd)
//        {
//            super();
//            dialog = new Dialog(new Frame(), title, true);
//            Button ok = new Button("OK");
//            Button cancel = new Button("CANCEL");
//            textf = new TextField(20);
//            textf.setText(text);
//            if (passwd)
//            {
//                textf.setEchoCharacter('*');
//            }
//            dialog.setLayout(new FlowLayout());
//            dialog.setLocation(100, 50);
//            ok.addActionListener(this);
//            cancel.addActionListener(this);
//            dialog.add(textf);
//            dialog.add(ok);
//            dialog.add(cancel);
//            dialog.pack();
//            dialog.setVisible(true);
//            while (true)
//            {
//                try
//                {
//                    Thread.sleep(1000);
//                }
//                catch (Exception ee)
//                {
//                }
//                if (!dialog.isVisible())
//                {
//                    break;
//                }
//            }
//        }
//
//        public void actionPerformed(ActionEvent e)
//        {
//            String action = e.getActionCommand();
//            if (action.equals("OK"))
//            {
//                result = textf.getText();
//            }
//            else if (action.equals("CANCEL"))
//            {
//            }
//            dialog.setVisible(false);
//            return;
//        }
//
//        String getText()
//        {
//            return result;
//        }
//    }
//
// 
//    public static void main(String[] arg)
//    {
//    	newVLTerm(); 
//    }
//    
//    public static void newVLTerm()
//    {
//    	newVLTerm(null,null); 
//    }
//    
//    public static VLTerm newVLTerm(VShellChannel shellChan)
//    {
//        return newVLTerm(null,shellChan); 
//    }
//    
//    public static VLTerm newVLTerm(VRL loc)
//    {
//        return newVLTerm(loc,null);
//    }
//
//    
//    public static VLTerm newVLTerm(final VRL optionalLocation,final VShellChannel shellChan)
//    {
//        final VLTerm term = new VLTerm();
//
//    	// always create windows during Swing Event thread 
//    	Runnable creator=new Runnable()
//    	{
//    		public void run()
//    		{
//    			
//    			// center on screen
//    			term.setLocationRelativeTo(null);
//    			term.setVisible(true);
//    			term.showSplash();
//    			term.requestFocus();
//        
//    			term.updateFrameSize();
//    			if (shellChan!=null)
//    			{
//    			    term.setShellChannel(shellChan);
//    			    term.startSession(); 
//    			}
//    			if (optionalLocation!=null)
//    				term.openLocation(optionalLocation); 
//    			
//    		}
//    	};
//    	
//    	SwingUtilities.invokeLater(creator); 
//
//        return term; 
//
//        /*
//         * { Insets insets = frame.getInsets(); int width =
//         * awtTerm.getTermWidth(); int height = awtTerm.getTermHeight(); width +=
//         * (insets.left + insets.right); height += (insets.top + insets.bottom);
//         * frame.setSize(width, height); }
//         */
//    }
//
//
//	protected void setShellChannel(VShellChannel chan)
//    {
//	    this.sessionType=SESSION_SHELLCHANNEL; 
//	    this.shellChannel=chan; 
//    }
//
//    private void showSplash()
//    {
//       this.vlTerm.drawSplash();       
//    }
//
//    protected VRSContext getVRSContext()
//    {
//	   return UIGlobal.getVRSContext(); 
//    }
//   
//    public void showMessage(String message)
//    {
//        JOptionPane.showMessageDialog(this, message);
//    }
//
//    public void terminateSession()
//    {
//        this.menuUpdateSessionAlive(false);
//        
//        // Terminal; 
//        if (this.vlTerm!=null)
//            this.vlTerm.terminate(); 
//        try
//        {
//            if (this.shellChannel!=null)
//                this.shellChannel.disconnect();
//        }
//        catch (IOException e)
//        {
//            logger.logException(ClassLogger.WARN,e,"Excpetion during disconnect:%s\n",this); 
//        }
//        
//        this.shellChannel=null;
//        thread = null;
//        
//    }
//  
//    /**
//     * Sends emulator the new preferred size. 
//     * Depends on implementation whether this will be respected 
//     * 
//     * @param nr_columns
//     * @param nr_rows
//     */
//	public void sendTermSize(int nr_columns, int nr_rows)
//	{
//	    try
//	    {
//	        if (shellChannel!=null)
//	            this.shellChannel.setTermSize(nr_columns,nr_rows,nr_columns,nr_rows);
//	    }
//	    catch (IOException e)
//        {
//	        logger.logException(ClassLogger.ERROR,e,"IOException: Couldn't send terminal size\n"); 
//        }
//
//		if (this.emulator!=null)
//			this.emulator.sendSize(nr_columns,nr_rows);
//	}
//	 /**
//     * Sends emulator the new preferred size. 
//     * Depends on implemenation whether this will be respected 
//     * 
//     * @param nr_columns
//     * @param nr_rows
//     */
//	public void setTermType(String type)
//	{
//	    try
//	    {
//    		if (shellChannel!=null)
//                this.shellChannel.setTermType(type);
//		
//	    }
//	    catch (IOException e)
//	    {
//	        logger.logException(ClassLogger.ERROR,e,"IOException: Couldn't send terminal type:%s\n",type); 
//	    }
//	    
//		// update emulator 
//		if (this.emulator!=null)
//			this.emulator.setTermType(type); 
//	}
//	
//	Properties persistantProperties=null; 
//	
//	String getConfigProperty(String name)
//	{
//		if (persistantProperties==null)
//		{
//			persistantProperties=loadProperties();
//		
//			if (persistantProperties==null)
//				return null; 
//		}
//		
//		Object val=persistantProperties.getProperty(name);
//		if (val!=null)
//			return val.toString();
//		
//		return null; 
//	}
//	
//	private Properties loadProperties()
//	{
//		try 
//		{
//		    VRL loc=VletConfig.getUserConfigDir().appendPath("/vlterm.prop"); 
//			return VletConfig.loadPropertiesFromURL(loc.toURL());
//		}
//		catch (Exception e) 
//		{
//		    UILogger.logException(this,ClassLogger.WARN,e,"VLTerm:Couldn't load config vlterm.prop\n"); 
//		    return new Properties();
//		}
//		
//	}
//	
//	/** save persistant property */ 
//	void savePersistantProperty(String name,String value)
//	{
//		if (persistantProperties==null)
//		{
//			persistantProperties=loadProperties();
//		}
//		
//		persistantProperties.put(name,value); 
//		
//		saveConfig();
//	}
//	
//	void saveConfig()
//	{	
//		
//		try 
//		{
//		    VRL loc=VletConfig.getUserConfigDir().appendPath("/vlterm.prop"); 
//			UIGlobal.getResourceLoader().saveProperties(loc.toURI(),persistantProperties,"VLTerm Configuration"); 
//		}
//		catch (Exception e) 
//		{
//		    UILogger.logException(this,Level.WARNING,e,"Couldn't write config\n"); 
//		}
//		
//		return; 
//	}
//	
//	/** Save current (view) settings */ 
//	void saveSettings()
//	{
//		if (persistantProperties==null)
//			persistantProperties=loadProperties();
//		
//		// copy session properties into persistant propereties
//		persistantProperties.putAll(this.sessionProperties);
//		saveConfig(); 
//	}
//  
//}
