package nl.esciencecenter.vbrowser.vrs.node;

import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_DIRNAME;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_FILE_SIZE;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_HOSTNAME;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_ISDIR;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_ISFILE;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_ISHIDDEN;
//import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_ISREADABLE;
//import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_ISWRITABLE;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_MIMETYPE;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_MODIFICATION_TIME;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_NAME;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_PATH;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_PERMISSIONSTRING;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_PORT;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_RESOURCE_TYPE;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_SCHEME;

import java.util.List;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.io.FSNode;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VFileSystem;
import nl.esciencecenter.vbrowser.vrs.VRSTypes;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public abstract class VFSPathNode extends VPathNode implements VFSPath 
{
    public static final String[] attributeNames =
    { 
        ATTR_RESOURCE_TYPE,
        ATTR_NAME,
        ATTR_SCHEME,
        ATTR_HOSTNAME,
        ATTR_PORT,
        ATTR_MIMETYPE, 
//        ATTR_ISREADABLE, 
//        ATTR_ISWRITABLE, 
        ATTR_ISHIDDEN,
        ATTR_ISFILE, 
        ATTR_ISDIR, 
        ATTR_FILE_SIZE,
        // minimal time wich must be supported
        ATTR_MODIFICATION_TIME,
        // ATTR_ISSYMBOLICLINK,
        ATTR_PERMISSIONSTRING // implementation specific permissions string
    };
    
    protected VFSPathNode(VRL vrl, VFileSystem fileSystem)
    {
        super(vrl, fileSystem);
    }
    
    protected VFileSystem getVFileSystem()
    {
        return (VFileSystem)resourceSystem; 
    }
    
    @Override
    public VFSPath getParent() throws VrsException
    {   
        return resolvePath(getDirname()); 
    }
    
    @Override
    public VFSPath resolvePath(String path) throws VrsException
    {
        return getVFileSystem().resolvePath(path); 
    }
    
    @Override
    public boolean isComposite() throws VrsException 
    {
        return isDir(); 
    }
    
    @Override
    public List<String> getChildNodeResourceTypes() throws VrsException
    {
        if (isComposite())
        {
            return new StringList(VRSTypes.FILE_TYPE,VRSTypes.DIR_TYPE); 
        }
        return null ;
    }
    
    @Override
    public String getResourceType() throws VrsException
    {
        if (isDir())
        {
            return FSNode.DIR_TYPE;
        }
        else
        {
            return FSNode.FILE_TYPE;
        }
    }
    
    public String getName()
    {
        return getBasename(); 
    }
    
    public String getBasename()
    {
        return this.vrl.getBasename(); 
    }
    
    public String getDirname()
    {
        return vrl.getDirname();
    }
    
    public FileAttributes getFileAttributes()
    {
        return new FileAttributes(); 
    }
    
    /**
     * Returns single File Resource Attribute. 
     * For optimized fetching of Attributes use getAttributes(String names[]) 
     */ 
    public Attribute getAttribute(String name) throws VrsException
    {
        if (name==null) 
            return null; 

        // Check if super class has this attribute
        Attribute supervalue = super.getAttribute(name);
        
        if (supervalue != null)
        {
            return supervalue;
        }
        
        if (name.compareTo(ATTR_DIRNAME) == 0)
            return new Attribute(name, getVRL().getDirname());
        else if (name.compareTo(ATTR_PATH) == 0)
            return new Attribute(name, getVRL().getPath());
        else if (name.compareTo(ATTR_ISDIR) == 0)
            return new Attribute(name, isDir());
        else if (name.compareTo(ATTR_ISFILE) == 0)
            return new Attribute(name, isFile());
        else if (name.compareTo(ATTR_FILE_SIZE) == 0)
            return new Attribute(name, getFileAttributes().size());
//        else if (name.compareTo(ATTR_ISREADABLE) == 0)
//            return new Attribute(name, getFileAttributes().isReadable());
//        else if (name.compareTo(ATTR_ISWRITABLE) == 0)
//            return new Attribute(name, getFileAttributes().isWritable());
        else if (name.compareTo(ATTR_ISHIDDEN) == 0)
            return new Attribute(name, getFileAttributes().isHidden());
        else if (name.compareTo(ATTR_MODIFICATION_TIME) == 0)
        {
            if (getFileAttributes().getModificationTime()>=0)
            {
                return new Attribute(name,Presentation.createDate(getFileAttributes().getModificationTime()));
            }
        }
//        else if (name.compareTo(ATTR_PERMISSIONSTRING) == 0)
//            return new Attribute(name,getFileAttributes().getPermissionsString()); 
        
        return null;  
    }
    
    public long getLength()
    {
        return getFileAttributes().size(); 
    }
    
    @Override
    public boolean isDir() throws VrsException
    {
        return getFileAttributes().isDirectory(); 
    }

    @Override
    public boolean isFile() throws VrsException
    {
        return getFileAttributes().isRegularFile();  
    }
    
    @Override
    public abstract boolean isRoot() throws VrsException; 
    
    @Override
    public abstract List<VFSPath> list() throws VrsException; 
    
}
