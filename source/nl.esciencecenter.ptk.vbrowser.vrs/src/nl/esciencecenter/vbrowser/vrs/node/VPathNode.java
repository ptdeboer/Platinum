package nl.esciencecenter.vbrowser.vrs.node;

import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.*;

import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.mimetypes.MimeTypes;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VPathNode implements VPath
{
    private final static ClassLogger logger=ClassLogger.getLogger(VPathNode.class); 
    
    static protected String[] vpathImmutableAttributeNames =
        {
            ATTR_LOCATION,
            ATTR_RESOURCE_TYPE, 
            ATTR_NAME, 
            ATTR_SCHEME, 
            ATTR_HOSTNAME, 
            ATTR_PORT, 
            ATTR_ICONURL, 
            ATTR_PATH, 
            ATTR_MIMETYPE,

        };
    
    protected VRL vrl;
    
    protected VResourceSystem resourceSystem; 
    
    protected VPathNode(VResourceSystem resourceSystem,VRL vrl)
    {
        this.vrl=vrl;
        this.resourceSystem=resourceSystem; 
    }
    
    public VRL getVRL()
    {
        return vrl; 
    }
    
    public java.net.URI getURI()
    {
        return vrl.toURINoException(); 
    }
    
    public VResourceSystem VResourceSystem()
    {
        return this.resourceSystem; 
    }
    
    public VRL resolvePathVRL(String relativeUri) throws VRLSyntaxException
    {
        return vrl.resolvePath(relativeUri); 
    }
    
    public String getName()
    {
        return getVRL().getBasename(); 
    }

    public boolean isComposite() throws VrsException
    {
        return false;
    }

    public String getIconURL(int size) throws VrsException 
    {
        return null;
    }

    public String getMimeType() throws VrsException 
    {
        return MimeTypes.getDefault().getMimeType(getVRL().getPath()); 
    }

    public String getResourceStatus() throws VrsException 
    {
        return null;
    }

    public List<AttributeDescription> getAttributeDescriptions() 
    {
        List<AttributeDescription> list = getImmutableAttributeDescriptions(); 
        List<AttributeDescription> list2 = getResourceAttributeDescriptions(); 
        if (list2!=null)
        {
            for (AttributeDescription attr:list2)
            {
                list.add(attr); 
            }
        }
        return list;
        
    }
    public List<AttributeDescription> getImmutableAttributeDescriptions() 
    {
        ArrayList<AttributeDescription> list=new ArrayList<AttributeDescription>(); 
        
        for (String name:vpathImmutableAttributeNames)
        {
            list.add(new AttributeDescription(name,AttributeType.STRING,false));
        }
        return list; 
    }
    
    public List<AttributeDescription> getResourceAttributeDescriptions() 
    {
        return null; 
    }
    
    public List<String> getAttributeNames()
    {
        List<AttributeDescription> list = getAttributeDescriptions();
        if (list==null)
        {
            return null;
        }
        
        StringList names=new StringList();
        for (AttributeDescription descr:list)
        {
            names.add(descr.getName()); 
        }
        
        return names;
    }

    @Override
    public List<Attribute> getAttributes(List<String> names) throws VrsException
    {
        ArrayList<Attribute> list=new ArrayList<Attribute>(); 
        for (String name:names)
        {
            Attribute attr=getAttribute(name); 
            if (attr!=null)
            {
                list.add(attr);
            }
            else
            {
                logger.warnPrintf("Attribute not defined:%s\n",name); 
            }
        }
        return list; 
    }
    
    public Attribute getAttribute(String name) throws VrsException
    {
        return getImmutableAttribute(name); 
    }
    
    public Attribute getResourceAttribute(String name) throws VrsException
    {
        return null;  
    }
    
    public Attribute getImmutableAttribute(String name) throws VrsException
    {
        // by prefix values with "", a NULL value will be convert to "NULL".
        if (name.compareTo(ATTR_RESOURCE_TYPE) == 0)
            return new Attribute(name, getResourceType());
        else if (name.compareTo(ATTR_LOCATION) == 0)
            return new Attribute(name, getVRL());
        else if (name.compareTo(ATTR_NAME) == 0)
            return new Attribute(name, getName());
        else if (name.compareTo(ATTR_SCHEME) == 0)
            return new Attribute(name, vrl.getScheme()); 
        else if (name.compareTo(ATTR_HOSTNAME) == 0)
            return new Attribute(name, vrl.getHostname());
        // only return port attribute if it has a meaningful value
        else if (name.compareTo(ATTR_PORT) >= 0)
            return new Attribute(name, vrl.getPort());
        else if (name.compareTo(ATTR_ICONURL) == 0)
            return new Attribute(name, getIconURL(16));
        else if (name.compareTo(ATTR_PATH) == 0)
            return new Attribute(name, vrl.getPath());
        else if ((name.compareTo(ATTR_URI_QUERY) == 0) && getVRL().hasQuery())
            return new Attribute(name, vrl.getQuery());
        else if ((name.compareTo(ATTR_URI_FRAGMENT) == 0) && getVRL().hasFragment())
            return new Attribute(name, getVRL().getFragment());
        else if (name.compareTo(ATTR_NAME) == 0)
            return new Attribute(name, getName());
        else if (name.compareTo(ATTR_LOCATION) == 0)
            return new Attribute(name, getVRL());
        else if (name.compareTo(ATTR_MIMETYPE) == 0)
            return new Attribute(name, getMimeType());

        return null;
    }
    
    @Override
    public String getResourceType() throws VrsException
    {
        return "<?>";
    }

    @Override
    public VPath resolvePath(String path) throws VrsException
    {
        return this.resourceSystem.resolvePath(path); 
    }

    @Override
    public VPath getParent() throws VrsException
    {   
        String parentPath=this.vrl.getDirname();
        return resourceSystem.resolvePath(parentPath); 
    }

    @Override
    public List<? extends VPath> list() throws VrsException
    {
        return null;
    }

    @Override
    public List<String> getChildNodeResourceTypes() throws VrsException
    {
        return null;
    }

}
