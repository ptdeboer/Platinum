package nl.esciencecenter.ptk.vbrowser.ui.tool.vtermstarter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import nl.esciencecenter.ptk.data.Pair;
import nl.esciencecenter.ptk.exec.ShellChannel;
import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.ptk.util.vterm.StartVTerm;
import nl.esciencecenter.ptk.util.vterm.VTerm;
import nl.esciencecenter.ptk.util.vterm.VTermChannelProvider;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerJPanel;
import nl.esciencecenter.ptk.vbrowser.viewers.ToolPlugin;
import nl.esciencecenter.ptk.vbrowser.viewers.menu.MenuMapping;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.sftp.SSHShellChannelFactory;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * VTerm factory implemented as EmbeddedViewer.
 */
public class VTermStarter extends ViewerJPanel implements ActionListener, ToolPlugin {
    private static final PLogger logger = PLogger.getLogger(VTermStarter.class);

    private static final long serialVersionUID = 6104695556400295643L;

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
        return new String[] { "application/ssh-location" };
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
            panel.setPreferredSize(new Dimension(250, 60));

            this.add(panel);
            panel.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
            panel.setLayout(new BorderLayout());
            {
                textF = new JTextField();
                textF.setText("\nA VLTerm will be started.\n ");

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
        startVTerm(vrl, null, null);
    }

    @Override
    public void doStopViewer() {
    }

    @Override
    public void doDisposeViewer() {
    }

    @Override
    protected void doStartViewer(VRL vrl, String optionalMethod) throws VrsException {
        startVTerm(getVRL(), optionalMethod, null);
    }

    public void startVTerm(VRL vrl, String optMethod, final ShellChannel shellChan)
            throws VrsException {
        logger.info("startVTerm for:{}", vrl);

        try {
            java.net.URI uri=null;
            if (vrl!=null) {
                uri=vrl.toURI();
            }
            // Share Context !
            VRSContext context = this.getResourceHandler().getVRSClient().getVRSContext();
            VTermChannelProvider provider = new VTermChannelProvider();
            provider.registerChannelFactory("SSH", new SSHShellChannelFactory(context));
            VTerm term = StartVTerm.startVTerm(provider, uri, shellChan);
            register(term);
        } catch (URISyntaxException e) {
            throw new VrsException("URI Syntax exeption:" + vrl, e);
        }
    }

    private void register(VTerm term) {

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
        return new String[] { "terminal" };
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
        MenuMapping menuMap = new MenuMapping(null, "sftp", null, null);
        List<Pair<MenuMapping, List<String>>> mappings = new ArrayList<Pair<MenuMapping, List<String>>>();
        mappings.add(new Pair<MenuMapping, List<String>>(menuMap, methods));
        return mappings;
    }

}
