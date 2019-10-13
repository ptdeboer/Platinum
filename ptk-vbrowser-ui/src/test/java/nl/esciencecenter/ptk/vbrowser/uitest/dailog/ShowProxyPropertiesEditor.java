package nl.esciencecenter.ptk.vbrowser.uitest.dailog;

import nl.esciencecenter.ptk.ui.icons.IconProvider;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.browser.ProxyBrowserController;
import nl.esciencecenter.ptk.vbrowser.ui.browser.viewers.ProxyPropertiesEditor;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.vrs.VRSProxyFactory;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerContext;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import java.awt.*;

public class ShowProxyPropertiesEditor {

    public static void main(String[] args) {
        try {
            show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void show() throws VrsException {
        //platform
        BrowserPlatform platform = BrowserPlatform.getInstance("vbrowser");
        VRSProxyFactory fac = VRSProxyFactory.createFor(platform);
        platform.registerProxyFactory(fac);
        ProxyBrowserController browser = (ProxyBrowserController) platform.createBrowser(false);

        //
        ViewNode viewNode = createViewNode();

        ResourceConfigInfo info = fac.getVRSContext().getResourceSystemInfoFor(viewNode.getVRL(),
                true);

        Attribute attr = new Attribute("testProp", "testValue", true);
        info.setAttribute(attr);

        attr = new Attribute("testPropInt", 1);
        attr.setEditable(true);
        info.setAttribute(attr);

        info.store();

        ProxyPropertiesEditor proxyPropViewer = new ProxyPropertiesEditor(browser, viewNode);
        ViewerContext ctx = new ViewerContext(null);
        proxyPropViewer.initViewer(ctx);
        proxyPropViewer.setVisible(true);

        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.add(proxyPropViewer, BorderLayout.CENTER);
        frame.pack();
        frame.setSize(400, 600);
        frame.setVisible(true);

        proxyPropViewer.startViewer(viewNode.getVRL(), "view");
    }

    private static ViewNode createViewNode() throws VRLSyntaxException {

        Icon defaultIcon = IconProvider.getDefault().getFileIcon(64);

        ViewNode node = ViewNode.create(new VRL("file:///home/"), "Node", defaultIcon, false,
                "File");

        return node;
    }

}
