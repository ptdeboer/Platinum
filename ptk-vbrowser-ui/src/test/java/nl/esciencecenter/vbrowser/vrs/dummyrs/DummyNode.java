package nl.esciencecenter.vbrowser.vrs.dummyrs;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.util.List;
import java.util.Map;

public class DummyNode implements VPath {
    protected DummyRS dummyRS;
    protected VRL vrl;
    protected boolean isComposite = false;

    public DummyNode(DummyRS _dummyRS, VRL _vrl, boolean composite) {
        dummyRS = _dummyRS;
        _vrl = vrl;
        isComposite = composite;
    }

    @Override
    public VRL getVRL() {
        return vrl;
    }

    @Override
    public String getName() {
        return vrl.getBasename();
    }

    @Override
    public String getResourceType()  {
        return "DUMMY";
    }

    @Override
    public VResourceSystem getResourceSystem()  {
        return this.dummyRS;
    }

    @Override
    public VRL resolveVRL(String path) throws VRLSyntaxException {
        return vrl.resolvePath(path);
    }

    @Override
    public VPath resolve(String path) throws VRLSyntaxException {
        return this.dummyRS.createNode(resolveVRL(path));
    }

    @Override
    public VPath getParent()  {
        return dummyRS.createNode(vrl.getParent());
    }

    @Override
    public String getIconURL(int size)  {
        return "dummy.png";
    }

    @Override
    public String getMimeType() {
        return "dummy";
    }

    @Override
    public String getResourceStatus() {
        return "dummy";
    }

    @Override
    public Map<String, AttributeDescription> getAttributeDescriptions(String[] names) {
        return null;
    }

    @Override
    public List<Attribute> getAttributes(String[] names)  {
        return null;
    }

    @Override
    public List<String> getAttributeNames()  {
        return null;
    }

    @Override
    public boolean sync()  {
        return true;
    }

    @Override
    public boolean isComposite()  {
        return isComposite;
    }

    @Override
    public List<String> getChildResourceTypes()  {
        return new StringList("dummy");
    }

    @Override
    public List<? extends VPath> list()  {
        return null;
    }

    @Override
    public VPath create(String type, String name) throws VRLSyntaxException {
        return dummyRS.createNode(resolveVRL(name));
    }

}
