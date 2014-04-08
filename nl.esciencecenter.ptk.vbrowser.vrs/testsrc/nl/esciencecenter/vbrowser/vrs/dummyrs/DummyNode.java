package nl.esciencecenter.vbrowser.vrs.dummyrs;

import java.util.List;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class DummyNode implements VPath
{
    protected DummyRS dummyRS; 
    protected VRL vrl; 
    protected boolean isComposite=false; 
    
    public DummyNode(DummyRS _dummyRS, VRL _vrl,boolean composite)
    {
        dummyRS=_dummyRS; 
        _vrl=vrl; 
        isComposite=composite; 
    }

    @Override
    public VRL getVRL()
    {
        return vrl; 
    }

    @Override
    public String getName()
    {
        return vrl.getBasename();
    }

    @Override
    public String getResourceType() throws VrsException
    {
        return "DUMMY";
    }

    @Override
    public VResourceSystem getResourceSystem() throws VrsException
    {
        return this.dummyRS; 
    }

    @Override
    public VRL resolvePathVRL(String path) throws VrsException
    {
        return vrl.resolvePath(path); 
    }

    @Override
    public VPath resolvePath(String path) throws VrsException
    {
        return this.dummyRS.createNode(resolvePathVRL(path)); 
    }

    @Override
    public VPath getParent() throws VrsException
    {
        return dummyRS.createNode(vrl.getParent()); 
    }

    @Override
    public String getIconURL(int size) throws VrsException
    {
        return "dummy.png";
    }

    @Override
    public String getMimeType() throws VrsException
    {
        return "dummy";
    }

    @Override
    public String getResourceStatus() throws VrsException
    {
        return "dummy";
    }

    @Override
    public List<AttributeDescription> getAttributeDescriptions() throws VrsException
    {
        return null;
    }

    @Override
    public List<Attribute> getAttributes(String names[]) throws VrsException
    {
        return null;
    }

    @Override
    public List<String> getAttributeNames() throws VrsException
    {
        return null;
    }

    @Override
    public boolean sync() throws VrsException
    {
        return true;
    }

    @Override
    public boolean isComposite() throws VrsException
    {
        return isComposite;
    }

    @Override
    public List<String> getChildResourceTypes() throws VrsException
    {
        return new StringList("dummy");
    }

    @Override
    public List<? extends VPath> list() throws VrsException
    {
        return null;
    }

    @Override
    public VPath create(String type, String name) throws VrsException
    {
        return dummyRS.createNode(resolvePathVRL(name)); 
    }

}
