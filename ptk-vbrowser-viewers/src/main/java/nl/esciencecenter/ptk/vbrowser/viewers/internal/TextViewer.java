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

package nl.esciencecenter.ptk.vbrowser.viewers.internal;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.data.HashMapList;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.task.ActionTask;
import nl.esciencecenter.ptk.ui.fonts.FontInfo;
import nl.esciencecenter.ptk.ui.fonts.FontToolBar;
import nl.esciencecenter.ptk.ui.fonts.FontToolbarListener;
import nl.esciencecenter.ptk.ui.fonts.FontUtil;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerJPanel;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.mimetypes.MimeTypes;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

/**
 * Embedded textviewer for the VBrowser.
 */
@Slf4j
public class TextViewer extends ViewerJPanel implements ActionListener, FontToolbarListener {
    // --
    private static final String viewerSettingsFile = "textviewer.props";

    private static final String CONFIG_LINE_WRAP = "textviewer.linewrap";

    private static final String[] configPropertyNames = {CONFIG_LINE_WRAP};

    public static final String ACTION_VIEW = "View";

    public static final String ACTION_EDIT = "Edit";

    /**
     * The mimetypes i can view
     */
    private static final String[] mimeTypes = {MimeTypes.MIME_TEXT_PLAIN, MimeTypes.MIME_TEXT_HTML, "text/x-c",
            "text/x-cpp", "text/x-java", "application/x-sh", "application/x-csh", "application/x-shellscript",
            // MimeTypes.MIME_BINARY, -> Now handled by MimeType mapping!
            "application/vlet-type-definition",
            // nfo files: uses CP437 Encoding (US Extended ASCII)!
            "text/x-nfo"};

    // ===
    // Instance
    // ===

    private boolean loadError = false;

    private JTextArea textArea = null;

    private boolean muststop = false;

    private final Properties configProperties = new Properties();

    // =======================================================================
    // =======================================================================
    boolean editable = false;
    protected String textEncoding = "UTF-8";

    //
    // GUI Components
    // ---

    private JToolBar toolbar;

    private JButton refreshButton;

    private JScrollPane textScrollPane;

    private FontToolBar fontToolbar;

    private JPanel toolPanel;

    private JToolBar optionsToolbar;

    private JButton saveConfigButton;

    private JToggleButton enableEditButton;

    private JButton saveButton;

    private JMenuBar menuBar;

    private JMenuItem refreshMenuItem;

    private JMenuItem saveMenuItem;

    private JPanel topPanel;

    private JCheckBoxMenuItem editMenuItem;

    private JMenuItem saveConfigMenuItem;

    private JCheckBoxMenuItem wrapMenuItem;

    private JMenuItem loadConfigMenuItem;

    private Vector<JRadioButton> encodingButtons;

    private JMenuItem enableEncodingMenuitem;

    private boolean _showWarningEncoding = true;

    // tasks

    private ActionTask loadTask;

    public TextViewer() {
        // initialization is done in initViewer() !
    }


    public String getTextEncoding() {
        return this.textEncoding;
    }

    public void setTextEncoding(String charSet) {
        this.textEncoding = charSet;
    }


    public void initGui() {
        // TextViewer is a JPanel:
        {
            this.setLayout(new BorderLayout());
            this.setPreferredSize(new Dimension(800, 600));
        }

        {
            topPanel = new JPanel();
            this.add(topPanel, BorderLayout.NORTH);
            topPanel.setLayout(new BorderLayout());
            topPanel.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));

            // Menu
            {
                menuBar = new JMenuBar();

                topPanel.add(menuBar, BorderLayout.NORTH);

                {
                    JMenu menu;
                    JMenuItem dumm;
                    menuBar.add(menu = new JMenu("Resource"));
                    {
                        dumm = new JMenuItem("Resource");
                        menu.add(dumm);
                        dumm.setEnabled(false);
                    }
                    menu.add(new JSeparator());
                    {
                        editMenuItem = new javax.swing.JCheckBoxMenuItem("Edit");
                        menu.add(editMenuItem);
                        editMenuItem.addActionListener(this);
                        editMenuItem.setSelected(false);
                    }
                    {
                        refreshMenuItem = new JMenuItem("Reload");
                        menu.add(refreshMenuItem);
                        refreshMenuItem.addActionListener(this);
                    }

                    {
                        saveMenuItem = new JMenuItem("Save");
                        menu.add(saveMenuItem);
                        saveMenuItem.addActionListener(this);
                        saveMenuItem.setEnabled(false);
                    }
                }
                {
                    // [Settings] menu
                    JMenu menu;
                    // JMenuItem dumm;
                    menuBar.add(menu = new JMenu("Settings"));
                    {
                        wrapMenuItem = new JCheckBoxMenuItem("Wrap text");
                        menu.add(wrapMenuItem);
                        wrapMenuItem.addActionListener(this);
                        wrapMenuItem.setSelected(false);
                    }

                    // [Settings]=>[Encoding]
                    {
                        JMenu encodingMenu = new JMenu("Encoding");
                        menu.add(encodingMenu);

                        {
                            enableEncodingMenuitem = new JMenuItem("Enable");
                            encodingMenu.add(enableEncodingMenuitem);
                            enableEncodingMenuitem.addActionListener(this);
                        }

                        encodingMenu.add(new JSeparator());

                        // [Settings]=>[Encoding]=>{Encodings}

                        String[] encs = ResourceLoader.getDefaultCharEncodings();
                        encodingButtons = new Vector<JRadioButton>();
                        ButtonGroup bGroup = new ButtonGroup();
                        for (String encoding : encs) {
                            JRadioButton item = new JRadioButton(encoding);
                            encodingMenu.add(item);
                            item.setActionCommand("encoding:" + encoding);
                            item.addActionListener(this);
                            item.setEnabled(false);
                            encodingButtons.add(item);
                            bGroup.add(item);
                        }
                    }

                    menu.add(new JSeparator());

                    {
                        saveConfigMenuItem = new JMenuItem("Save Settings");
                        menu.add(saveConfigMenuItem);
                        saveConfigMenuItem.addActionListener(this);
                        // saveConfigMenuItem.setSelected(false);
                    }
                    {
                        loadConfigMenuItem = new JMenuItem("(Re)load Settings");
                        menu.add(loadConfigMenuItem);
                        loadConfigMenuItem.addActionListener(this);
                        // loadConfigMenuItem.setSelected(false);
                    }
                }
            }
            // Toolbars
            {
                toolPanel = new JPanel();
                topPanel.add(toolPanel, BorderLayout.CENTER);
                toolPanel.setLayout(new FlowLayout());
                // // Font Toolbar
                {
                    this.fontToolbar = new FontToolBar(this, 16, 24);
                    toolPanel.add(fontToolbar);
                }
                {
                    toolbar = new JToolBar();
                    toolPanel.add(toolbar);
                    {
                        refreshButton = new JButton();
                        toolbar.add(refreshButton);
                        refreshButton.setIcon(getIconOrBroken("texteditor/refresh.gif"));

                        refreshButton.setActionCommand("refresh");
                        refreshButton.addActionListener(this);
                        refreshButton.setToolTipText("Reload text");
                    }

                }
                {
                    optionsToolbar = new JToolBar();
                    toolPanel.add(optionsToolbar);
                    {
                        saveConfigButton = new JButton();
                        optionsToolbar.add(saveConfigButton);
                        saveConfigButton.setIcon(getIconOrBroken("texteditor/saveconfig.png"));
                        saveConfigButton.setActionCommand("saveConfig");
                        saveConfigButton.addActionListener(this);
                        saveConfigButton.setToolTipText("Save setings");
                    }
                    {
                        enableEditButton = new JToggleButton();
                        optionsToolbar.add(enableEditButton);
                        enableEditButton.setIcon(getIconOrBroken("texteditor/enableedit.png"));

                        enableEditButton.setActionCommand("enableEdit");
                        enableEditButton.addActionListener(this);
                        enableEditButton.setToolTipText("Edit this text");
                        enableEditButton.setEnabled(true);
                    }
                    {
                        saveButton = new JButton();
                        optionsToolbar.add(saveButton);
                        saveButton.setIcon(getIconOrBroken("texteditor/save.png"));

                        saveButton.setActionCommand("save");
                        saveButton.addActionListener(this);
                        saveButton.setToolTipText("Save text");
                        saveButton.setEnabled(false);
                    }
                }
            }
        }

        {
            textScrollPane = new JScrollPane();
            add(textScrollPane, BorderLayout.CENTER);

            {
                textArea = new JTextArea();
                // java 1.5 anti aliasing:

                /*
                 * textArea.putClientProperty( com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,
                 * this.fontToolbar.getAntiAliasing());
                 */

                textArea.setText("");
                textArea.setSize(800, 600);
                textArea.setEditable(this.editable);
                textScrollPane.setViewportView(textArea);
            }
        }

        // update with stored settings:
        {
            loadSettings();
            FontInfo info = getFontInfo();
            info.updateComponentFont(this.textArea);
        }
    }

    protected FontInfo getFontInfo() {
        return fontToolbar.getFontInfo();
    }

    @Override
    public boolean haveOwnScrollPane() {
        return true;
    }

    /**
     * @param txt
     */
    public void setText(String txt) {
        textArea.setText(txt);
        // needed when updating outside Event thread:
        textArea.revalidate();
    }

    protected void updateFont(FontInfo info) {
        this.fontToolbar.setFontInfo(info);
        this.updateFont(info.createFont(), info.getRenderingHints());
    }

    public void updateFont(Font font, Map<?, ?> renderingHints) {
        textArea.setFont(font);
        FontUtil.updateRenderingHints(textArea, renderingHints);// doesn't work.
        textArea.repaint();
    }

    /**
     * @param location
     * @throws VrsException
     */
    public void doStartViewer(VRL location, String optionalMethod) {
        doUpdate(location);
    }

    protected void doUpdate(final VRL location) {

        try {

            if (isSaving()) {
                throw new IOException("Still waiting for previous save to finished!");
            }

            if (location == null)
                return; // clear ?

            loadTask = new ActionTask(null, "Load Contents of:" + this) {
                public void doTask() {
                    _load(location);
                }

                @Override
                public void stopTask() {

                }

            };

            loadTask.startTask();
        } catch (Exception e) {
            notifyException("Failed to (re)load URI:" + getVRL(), e);
        }

    }

    protected void _load(VRL uri) {
        log.debug("TextViewer Loading:{}", uri);

        // reset stop flag:
        this.muststop = false;

        this.setText(""); // clear previous ..

        if (uri == null) {
            setText("<<<NULL URI>>>");
            return;
        }

        setBusyLoadSave(true);

        try {
            setViewerTitle("Loading :" + getVRL());

            String txt = "";

            String mimeType = getResourceHandler().getMimeType(uri.getPath());

            if (this.muststop == true) {
                setBusyLoadSave(false);
                return; // receive stop signal BEFORE getContents...
            }

            //            // Special Replica handling:
            //            if (this.getResourceHandler().hasReplicas(getVRL())) {
            //                VRL vrls[] = getResourceHandler().getReplicas(getVRL());
            //
            //                if ((vrls == null) || (vrls.length <= 0)) {
            //                    // Use Exception dialog as Warning Dialog
            //                    throw new IOException(
            //                            "Warning:File doesn't have any replicas.\n"
            //                                    + "You can start editing this file and when saving this file a new replica will be created.");
            //
            //                }
            //            }
            //

            txt = getResourceHandler().readText(uri, textEncoding);
            // Override
            if (StringUtil.equals(mimeType, "text/x-nfo")) {
                textEncoding = ResourceLoader.CHARSET_CP437;
                setFont("Monospaced");
            } else {
                // Keep current
                // textEncoding=ResourceLoader.CHARSET_UTF8;
            }
            if (this.muststop == true) {
                setBusyLoadSave(false);
                return; // receive stop signal AFTER getContents...
            }

            setText(txt);
            loadError = false;
            requestFocusOnText();
            updateTitle();

        } catch (Exception e) {
            loadError = true;
            setViewerTitle("Error Loading :" + getVRL());
            //
            setText("");
            notifyException("Failed to load text:" + getVRL(), e);
        } finally {
            setBusyLoadSave(false);
        }
    }

    public void setFont(String name) {
        this.fontToolbar.selectFont(name);
    }

    protected void updateTitle() {
        if (this.editable == false) {
            setViewerTitle("Viewing:" + getVRL().getBasename());
        } else {
            setViewerTitle("Editing:" + getVRL().getBasename());
        }
    }

    protected void requestFocusOnText() {
        this.textArea.requestFocusInWindow();
    }

    @Override
    public String[] getMimeTypes() {
        return mimeTypes;
    }

    @Override
    public void doStopViewer() {
        this.muststop = true;
    }

    @Override
    public void doDisposeViewer() {
        this.textArea = null;
    }

    @Override
    public void doInitViewer() {
        initGui();
    }

    @Override
    public String getViewerName() {
        return "TextEditor";
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        String actionCmd = e.getActionCommand();
        // FontToolbar events are handled by that component:
        if ((source == this.refreshButton) || (source == this.refreshMenuItem)) {
            try {
                if (isSaving()) {
                    notifyException("Still Saving!", new Exception("Still waiting for previous save to finished!"));
                    return;
                }
                doUpdate(getVRL());
            } catch (Exception ex) {
                notifyException("Failed to (re)load:" + getVRL(), ex);
            }
        } else if ((source == this.saveConfigButton) || (source == this.saveConfigMenuItem)) {
            saveSettings();
        } else if (source == this.loadConfigMenuItem) {
            loadSettings();
        } else if (source == this.enableEditButton) {
            enableEdit(this.editable == false); // toggle
            this.requestFocusOnText();
        } else if (source == this.editMenuItem) {
            enableEdit(editMenuItem.isSelected());
        } else if (source == this.wrapMenuItem) {
            setLineWrap(this.wrapMenuItem.getState());

        } else if ((source == this.saveButton) || (source == this.saveMenuItem)) {
            try {
                save();
            } catch (Exception ex) {
                this.notifyException("Failed to save:" + getVRL(), ex);
            }
        } else if (source == enableEncodingMenuitem) {
            boolean val = (enableEncodingMenuitem.getText().compareToIgnoreCase("Enable") == 0);

            if (val)
                enableEncodingMenuitem.setText("Disable");
            else
                enableEncodingMenuitem.setText("Enable");

            for (JRadioButton rbut : this.encodingButtons)
                rbut.setEnabled(val);
        } else if (actionCmd.startsWith("encoding:")) {
            String[] strs = actionCmd.split(":");
            setEncoding(strs[1]);
            // only show warning during GUI actions
            showWarnEncoding();
        }
    }

    protected void setEncoding(String val) {
        this.textEncoding = val;
    }

    protected void showWarnEncoding() {
        if (this._showWarningEncoding == false)
            return;

        super.showMessage("Encoding warning", "Warning: You are changing the encoding of this text.\n"
                + "Either:\n" + "(1) Reload this file to read the original text using the new encoding, or\n"
                + "(2) Save this resource to encode the current text using the new encoding.");

        // I will zay thiz zonly wanz:
        this._showWarningEncoding = false;
    }

    protected void setLineWrap(boolean state) {
        this.textArea.setLineWrap(state);
        this.configProperties.setProperty(CONFIG_LINE_WRAP, "" + state);
    }

    ActionTask saveTask = null;

    protected boolean isSaving() {
        if (saveTask == null)
            return false;

        if (saveTask.isAlive() == false) {
            saveTask = null;
            return false;
        } else {
            return true;
        }
    }

    protected void save() throws IOException {
        final String textToSave = this.textArea.getText();

        if (isSaving()) {
            throw new IOException("Still waiting for previous save to finished!");
        }

        saveTask = new ActionTask(null, "Save Contents of:" + this) {
            public void doTask() {
                _save(textToSave, getTextEncoding());
            }

            @Override
            public void stopTask() {
            }
        };

        saveTask.startTask();
    }

    protected void setBusyLoadSave(final boolean val) {
        if (this.editable)
            saveButton.setEnabled((val == false));
        else
            saveButton.setEnabled(editable);

        refreshButton.setEnabled((val == false));
        notifyBusy(val);

        if (val)
            this.textArea.setCursor(this.getBusyCursor());
        else
            this.textArea.setCursor(this.getDefaultCursor());
    }

    /**
     * Write String as new contents
     *
     * @param encoding
     */
    protected void _save(final String txt, String encoding) {
        try {
            setBusyLoadSave(true);
            setViewerTitle("Saving text:" + getURIBasename());
            updateTitle();

            this.getResourceHandler().writeText(getVRL(), txt, encoding);
        } catch (Exception e) {
            notifyException("Failed to write text to:" + getVRL(), e);
        } finally {
            setBusyLoadSave(false);
        }
    }

    /**
     * Enable/Disable edit
     */
    protected void enableEdit(boolean val) {
        if (val == true) {

            this.editable = true;
            this.textArea.setEditable(true);
            this.saveButton.setEnabled(true);
            this.enableEditButton.setSelected(true);
            this.setViewerTitle("Editing:" + getURIBasename());
            this.editMenuItem.setSelected(true);
            this.saveMenuItem.setEnabled(true);
        } else {

            this.editable = false;
            this.textArea.setEditable(false);
            this.saveButton.setEnabled(false);
            this.enableEditButton.setSelected(false);
            this.setViewerTitle("Viewing:" + getURIBasename());
            this.editMenuItem.setSelected(false);
            this.saveMenuItem.setEnabled(false);
        }
        updateTitle();
        // this.enableEditButton.setSelected(true);
    }

    protected void saveSettings() {
        Properties allProps = new Properties();

        Properties props = this.fontToolbar.getFontInfo().getFontProperties();
        allProps.putAll(props);
        allProps.putAll(this.configProperties);

        try {
            saveConfigProperties(allProps, viewerSettingsFile);
        } catch (Exception e) {
            notifyException("Failed to save configuration properties", e);
        }
    }

    protected void loadSettings() {
        try {
            Properties props = loadConfigProperties(viewerSettingsFile);
            // System.err.println("load settings");
            FontInfo info = new FontInfo(props);

            this.updateFont(info);

            // update/copy settings
            for (String name : configPropertyNames) {
                Object value = props.getProperty(name);
                if (value != null)
                    this.configProperties.setProperty(name, value.toString());
            }
            updateSettings();
        } catch (Exception e) {
            log.warn("Exception when loading settings: {}", e);
        }
    }

    protected void updateSettings() {

        try {
            String value = this.configProperties.getProperty(CONFIG_LINE_WRAP);
            if (value != null)
                this.wrapMenuItem.setState(Boolean.parseBoolean(value));
        } catch (Exception e) {
            log.error("Property Exception '{}':{}", CONFIG_LINE_WRAP, e);
        }
    }

    @Override
    public Map<String, List<String>> getMimeMenuMethods() {
        // Use HashMapList to keep order of menu entries: first is default(!)
        Map<String, List<String>> mappings = new HashMapList<String, List<String>>();

        for (int i = 0; i < mimeTypes.length; i++) {
            List<String> list = new StringList(ACTION_VIEW + ":View Text", ACTION_EDIT + ":Edit Text");
            mappings.put(mimeTypes[i], list);
        }

        return mappings;
    }

}
