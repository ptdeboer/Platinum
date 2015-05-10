package nl.esciencecenter.ptk.vbrowser.uitest.resourcetable;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTable;

import nl.esciencecenter.ptk.data.LongHolder;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.vbrowser.ui.browser.MiniIcons;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyDataSource;
import nl.esciencecenter.ptk.vbrowser.ui.model.UIViewModel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.event.VRSEventListener;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class AltDummyDataSource implements ProxyDataSource {

    protected Presentation dummyPresentation;

    StringList rowAttrs = new StringList("dummy1", "dummy2", "dummy3", "dummy4");
    StringList prefRowAttrs = new StringList("dummy1", "dummy2");

    public AltDummyDataSource() {
        dummyPresentation = new Presentation(); // VRSPresentation.createDefault();
        dummyPresentation.setPreferredContentAttributeNames(prefRowAttrs);
        dummyPresentation.setColumnsAutoResizeMode(JTable.AUTO_RESIZE_OFF); // .AUTO_RESIZE_OFF;

        dummyPresentation.setAttributePreferredWidths("dummy1", 8, 60, 999);
        dummyPresentation.setAttributePreferredWidths("dummy2", 32, 100, 999);
        dummyPresentation.setAttributePreferredWidths("dummy3", 64, 140, 999);
        dummyPresentation.setAttributePreferredWidths("dummy4", 128, 180, 999);

    }

    @Override
    public void addDataSourceEventListener(VRSEventListener listener) {
    }

    @Override
    public void removeDataSourceEventListener(VRSEventListener listener) {
    }

    @Override
    public ViewNode getRoot(UIViewModel uiModel) throws ProxyException {
        return createViewNode("root");
    }

    public ViewNode createViewNode(String name) throws ProxyException {
        try {
            return ViewNode.create(new VRL("dummy:/" + name), name,
                    new ImageIcon(MiniIcons.getTabAddImage()), true, "DummyType");
        } catch (VRLSyntaxException e) {
            throw new ProxyException(e.getMessage(), e);
        }

    }

    @Override
    public ViewNode[] getChilds(UIViewModel uiModel, VRL locator, int offset, int range,
            LongHolder numChildsLeft) throws ProxyException {
        ViewNode nodes[] = new ViewNode[2];
        nodes[0] = createViewNode("dummy1");
        nodes[1] = createViewNode("dummy2");

        return nodes;
    }

    @Override
    public ViewNode[] createViewNodes(UIViewModel uiModel, VRL[] locations) throws ProxyException {
        ViewNode nodes[] = new ViewNode[locations.length];
        for (int i = 0; i < locations.length; i++) {
            nodes[i] = createViewNode(locations[i].getBasename());
        }
        return nodes;
    }

    @Override
    public List<String> getAttributeNames(VRL locator) throws ProxyException {
        return rowAttrs;
    }

    @Override
    public List<Attribute> getAttributes(VRL locator, String[] attrNames) throws ProxyException {
        List<Attribute> attrs = new ArrayList<Attribute>();
        for (String name : attrNames) {
            attrs.add(new Attribute(name, "ValueFor:" + name));
        }

        return attrs;
    }

    @Override
    public ProxyNode getRootNode() {
        return null;
    }

    @Override
    public Presentation getPresentation() throws ProxyException {
        return dummyPresentation;
    }

}
