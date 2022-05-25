package nl.esciencecenter.ptk.vbrowser.ui.dummy;

import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.BrowserInterfaceAdaptor;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactory;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNodeDataSourceProvider;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTable;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTableModel;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import java.awt.*;

public class DummyProxyResourceTable {

    public static void main(String[] args) {
        try {
            showTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showTable() throws VRLSyntaxException, ProxyException {
        BrowserPlatform platform = StartDummyBrowser.getDummyPlatform();

        JFrame frame = new JFrame();
        frame.setSize(800, 600);

        frame.setLayout(new BorderLayout());

        JScrollPane pane = new JScrollPane();
        frame.add(pane, BorderLayout.CENTER);

        ResourceTable table = new ResourceTable(new BrowserInterfaceAdaptor(platform),
                new ResourceTableModel(false));
        pane.setViewportView(table);

        VRL vrl = new VRL("dummy:///");
        ProxyFactory dummyFac = platform.getProxyFactoryFor(vrl);
        ProxyNode root = dummyFac.openLocation("dummy:///");

        ProxyNodeDataSourceProvider dataSource = new ProxyNodeDataSourceProvider(root);
        table.setDataSource(dataSource, true);

        frame.setVisible(true);

    }
}
