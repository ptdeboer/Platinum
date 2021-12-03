package nl.esciencecenter.ptk.vbrowser.ui.browser.viewers;

import nl.esciencecenter.ptk.task.ITaskSource;
import nl.esciencecenter.ptk.vbrowser.ui.attribute.AttributePanel;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserTask;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.infors.InfoRSConstants;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

public class ProxyPropertiesEditorController implements ActionListener {

    private final static Logger logger = LoggerFactory.getLogger(ProxyPropertiesEditorController.class);

    private final ProxyPropertiesEditor editorPanel;

    private ResourceConfigInfo configInfo;

    public ProxyPropertiesEditorController(ProxyPropertiesEditor editorPanel) {
        this.editorPanel = editorPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String cmd = e.getActionCommand();

        if (cmd.equals(ProxyPropertiesEditor.APPLY_ACTION)) {
            doApply();
        } else if (cmd.equals(ProxyPropertiesEditor.OK_ACTION)) {
            doOK();
        } else if (cmd.equals(ProxyPropertiesEditor.RESET_ACTION)) {
            doReset();
        } else if (cmd.equals(ProxyPropertiesEditor.CANCEL_ACTION)) {
            doCancel();
        }
    }

    private void doCancel() {
        logger.debug("Closing");
        this.editorPanel.closeViewer();
    }

    private void doReset() {
        update(this.editorPanel.getViewNode());
    }

    private void doOK() {
        this.editorPanel.closeViewer();
    }

    private void doApply() {

        if (this.editorPanel.getConfigAttributePanel().hasChangedAttributes()) {
            logger.debug("Update configuration!");
            AttributePanel panel = this.editorPanel.getConfigAttributePanel();
            Attribute[] attrs = panel.getChangedAttributes();

            for (Attribute attr : attrs) {
                logger.debug("Changed attr:{}", attr);
                this.configInfo.setAttribute(attr);
            }
            this.configInfo.store();
        } else {
            logger.debug("No changed configuration properties");
        }

        if (this.editorPanel.getAttributePanel().hasChangedAttributes()) {
            logger.debug("Update properties!");
            AttributePanel panel = this.editorPanel.getAttributePanel();
            Attribute[] attrs = panel.getChangedAttributes();

            for (Attribute attr : attrs) {
                logger.debug("Changed property attr:{}", attr);
            }
            doUpdateAttributes(attrs);
        } else {
            logger.debug("No changed properties");
        }

    }

    private void doUpdateAttributes(Attribute[] attrs) {
        try {
            this.getProxyNode(editorPanel.getViewNode().getVRL()).updateAttributes(attrs);
        } catch (ProxyException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void update(final ViewNode node) {

        if (node == null) {
            return;
        }

        BrowserTask task = new BrowserTask((ITaskSource) null, "Updating:" + node) {

            public void doTask() {
                editorPanel.notifyBusy(true);
                try {
                    updateNode(getProxyNode(node.getVRL()));
                } catch (Throwable e) {
                    this.setException(e);
                    handle("Couldn't update node:" + node, e);
                } finally {
                    editorPanel.notifyBusy(false);
                }

            }
        };

        task.startTask();
    }

    protected ProxyNode getProxyNode(VRL vrl) throws ProxyException {
        BrowserPlatform platform = editorPanel.getBrowserPlatform();
        return platform.getProxyFactoryFor(vrl).openLocation(vrl);
    }

    private void updateNode(ProxyNode proxyNode) throws ProxyException {
        logger.debug("updateNode():{}", proxyNode);

        ViewNode viewNode = proxyNode.createViewItem(this.editorPanel.getUIModel());
        editorPanel.getIconLabel().setIcon(viewNode.getIcon());
        editorPanel.getIconLabel().setText(viewNode.getName());

        updateAttrs(proxyNode);
        updateConfigAttrs(proxyNode);

        // callback:
        editorPanel.requestFramePack();
    }

    private void updateAttrs(ProxyNode proxyNode) throws ProxyException {
        // node attributes
        List<String> attrNames = proxyNode.getAttributeNames();
        List<Attribute> attrs = proxyNode.getAttributes(attrNames);
        AttributeSet attrSet = new AttributeSet(attrs);
        Map<String, AttributeDescription> descriptions = proxyNode.getAttributeDescriptions(attrNames.toArray(new String[0]));

        for (String key : descriptions.keySet()) {
            AttributeDescription desc = descriptions.get(key);
            if (desc != null) {
                Attribute attr = attrSet.get(key);
                if (attr != null) {
                    attr.setEditable(desc.isEditable());
                }
            }
        }
        //
        editorPanel.getAttributePanel().setEditable(proxyNode.isEditable());
        editorPanel.getAttributePanel().asyncSetAttributes(attrSet);
    }


    private void updateConfigAttrs(ProxyNode proxyNode) throws ProxyException {

        String resourceType = proxyNode.getResourceType();

        boolean editConfig = InfoRSConstants.RESOURCEINFO_CONFIG.equals(resourceType);

        this.configInfo = proxyNode.getResourceConfigInfo();

        if (configInfo == null) {
            this.editorPanel.setEditable(false);
            logger.debug("No Configuration Attributes for:{}", proxyNode);
            editorPanel.getConfigAttributePanel().setAttributes(new AttributeSet());
            return;
        }

        // edit all configs for now:
        editConfig = (configInfo != null);

        this.editorPanel.setEditable(editConfig);
        AttributeSet configAttrs = configInfo.getConfigAttributeSet();
        for (Attribute attr : configAttrs.toArray()) {
            logger.debug("Got config attr:{}", attr);
        }

        editorPanel.getConfigAttributePanel().setAttributes(configAttrs);
    }

    public void doUpdate(VRL vrl) {

        try {
            updateNode(getProxyNode(new VRL(vrl)));
        } catch (ProxyException e) {
            handle("Failed to load:" + vrl, e);
        }

    }

    private void handle(String message, Throwable e) {
        logger.error("Exception:{}", e);
        this.editorPanel.handle(message, e);
    }

}
