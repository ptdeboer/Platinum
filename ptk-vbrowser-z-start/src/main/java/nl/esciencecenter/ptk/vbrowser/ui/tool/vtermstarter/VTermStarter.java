package nl.esciencecenter.ptk.vbrowser.ui.tool.vtermstarter;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.data.Pair;
import nl.esciencecenter.ptk.vbrowser.viewers.ToolPlugin;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerJPanel;
import nl.esciencecenter.ptk.vbrowser.viewers.menu.MenuMapping;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.sftp.SSHShellChannelFactory;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import nl.piter.vterm.api.ShellChannel;
import nl.piter.vterm.emulator.VTermChannelProvider;
import nl.piter.vterm.ui.VTerm;
import nl.piter.vterm.ui.VTermSessionManager;


import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * VTerm factory implemented as EmbeddedViewer.
 */
@Slf4j
public class VTermStarter extends ViewerJPanel implements ActionListener, ToolPlugin {

    private JPanel panel;
    private JButton okButton;
    private JTextField textF;
    private JPanel butPanel;

    @Override
    public String getViewerName() {
        return "VTerm";
    }

    @Override
    public String[] getMimeTypes() {
        return new String[]{"application/ssh-location"};
    }

    @Override
    public void doInitViewer() {
        initGUI();
    }

    @Override
    public boolean isStandaloneViewer() {
        return true;
    }

    private void initGUI() {

        {
            panel = new JPanel();
//            panel.setPreferredSize(new Dimension(250, 60));

            this.add(panel);

            panel.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
            panel.setLayout(new BorderLayout());
            {
                textF = new JTextField();
                textF.setText("\nA VTerm will be started.\n");
                panel.add(textF, BorderLayout.CENTER);
            }
            {
                butPanel = new JPanel();
                panel.add(butPanel, BorderLayout.SOUTH);
                butPanel.setLayout(new FlowLayout());
                // butPanel.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));

                {
                    okButton = new JButton();
                    okButton.setText("OK");
                    butPanel.add(okButton);
                    okButton.addActionListener(this);
                }
            }
        }

    }

    @Override
    public void doUpdate(VRL vrl) throws VrsException {
        startVTerm(vrl, null);
    }

    @Override
    public void doStopViewer() {
    }

    @Override
    public void doDisposeViewer() {
    }

    @Override
    protected void doStartViewer(VRL vrl, String optionalMethod) throws VrsException {
        startVTerm(getVRL(), optionalMethod);
    }

    public void startVTerm(VRL vrl, String optMethod)
            throws VrsException {
        log.debug("method = '{}' => startVTerm for:'{}'", optMethod, vrl);

        try {
            java.net.URI uri = null;
            if (vrl != null) {
                uri = vrl.toURI();
            }
            // Share Context !
            VRSContext context = this.getResourceHandler().getVRSClient().getVRSContext();
            VTermChannelProvider provider = new VTermChannelProvider();
            provider.registerChannelFactory("SSH", new SSHShellChannelFactory(context));
            // Could already create authenticated shell channel here (Optional!)

            startVTerm(provider, VTermSessionManager.SESSION_SSH, null, uri);
        } catch (URISyntaxException e) {
            throw new VrsException("URI Syntax exception:" + vrl, e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.closeViewer();
    }

    @Override
    public Map<String, List<String>> getMimeMenuMethods() {
        return null;
    }

    @Override
    public ViewerJPanel getViewerPanel() {
        return this;
    }

    @Override
    public String getToolName() {
        return "VTerm";
    }

    @Override
    public boolean addToToolMenu() {
        return true;
    }

    @Override
    public String[] getToolMenuPath() {
        return new String[]{"terminal"};
    }

    @Override
    public String toolBarName() {
        return "VTerm";
    }

    @Override
    public String defaultToolMethod() {
        return null;
    }

    @Override
    public Icon getToolIcon(int size) {
        return null;
    }

    @Override
    public List<Pair<MenuMapping, List<String>>> getMenuMappings() {
        ArrayList<String> methods = new ArrayList<String>();
        methods.add("open:Open VTerm");

        MenuMapping sftpMenu = new MenuMapping(null, "sftp", null, null);
        MenuMapping shellMenu  = new MenuMapping(null, "file", null, null);
        List<Pair<MenuMapping, List<String>>> mappings = new ArrayList<>();
        mappings.add(new Pair<>(sftpMenu, methods));
        mappings.add(new Pair<>(shellMenu, methods));

        return mappings;
    }

    public static VTerm startVTerm(final VTermChannelProvider provider, String sessionType, final ShellChannel shellChan, final URI loc) {
        return new nl.piter.vterm.VTermStarter().withChannelProvider(provider).start(sessionType,shellChan, loc);
    }

}
