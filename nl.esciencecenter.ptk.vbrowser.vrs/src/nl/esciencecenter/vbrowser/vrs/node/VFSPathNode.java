package nl.esciencecenter.vbrowser.vrs.node;

import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.io.FSNode;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VFileSystem;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VRSTypes;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.io.VPathDeletable;
import nl.esciencecenter.vbrowser.vrs.io.VPathRenamable;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public abstract class VFSPathNode extends VPathNode implements VFSPath, VPathRenamable, VPathDeletable
{
    public static final String[] vfsAttributeNames =
    {
            ATTR_RESOURCE_TYPE,
            ATTR_NAME,
            ATTR_SCHEME,
            ATTR_HOSTNAME,
            ATTR_PORT,
            ATTR_MIMETYPE,
            // ATTR_ISREADABLE,
            // ATTR_ISWRITABLE,
            ATTR_ISHIDDEN,
            ATTR_ISFILE,
            ATTR_ISDIR,
            ATTR_FILE_SIZE,
            // minimal time wich must be supported
            ATTR_MODIFICATION_TIME,
            // ATTR_ISSYMBOLICLINK,
            ATTR_PERMISSIONSTRING // implementation specific permissions string
    };

    protected VFSPathNode(VFileSystem fileSystem, VRL vrl)
    {
        super(fileSystem, vrl);
    }

    protected VFileSystem getVFileSystem()
    {
        return (VFileSystem) resourceSystem;
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
            return new StringList(VRSTypes.FILE_TYPE, VRSTypes.DIR_TYPE);
        }
        return null;
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

    public List<AttributeDescription> getResourceAttributeDescriptions()
    {
        List<AttributeDescription> attrs = new ArrayList<AttributeDescription>();

        for (String name : vfsAttributeNames)
        {
            attrs.add(new AttributeDescription(name, AttributeType.ANY, true, "File Attribute:" + name));
        }
        return attrs;
    }

    /**
     * Returns single File Resource Attribute.
     */
    public Attribute getAttribute(String name) throws VrsException
    {
        if (name == null)
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
        // else if (name.compareTo(ATTR_ISREADABLE) == 0)
        // return new Attribute(name, getFileAttributes().isReadable());
        // else if (name.compareTo(ATTR_ISWRITABLE) == 0)
        // return new Attribute(name, getFileAttributes().isWritable());
        else if (name.compareTo(ATTR_ISHIDDEN) == 0)
            return new Attribute(name, getFileAttributes().isHidden());
        else if (name.compareTo(ATTR_MODIFICATION_TIME) == 0)
        {
            Date date = getFileAttributes().getModificationTimeDate();

            if (date != null)
            {
                return new Attribute(name, date);
            }
        }
        else if (name.compareTo(ATTR_LASTACCESS_TIME) == 0)
        {
            Date date = getFileAttributes().getLastAccessTimeDate();

            if (date != null)
            {
                return new Attribute(name, date);
            }
        }
        else if (name.compareTo(ATTR_CREATION_TIME) == 0)
        {
            Date date = getFileAttributes().getCreationTimeDate();

            if (date != null)
            {
                return new Attribute(name, getFileAttributes().getModificationTimeDate());
            }
        }
        // else if (name.compareTo(ATTR_PERMISSIONSTRING) == 0)
        // return new
        // Attribute(name,getFileAttributes().getPermissionsString());

        return null;
    }

    public long getLength() throws VrsException
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

    public boolean create() throws VrsException
    {
        if (isFile())
        {
            createFile();
            return true;
        }
        else if (isDir())
        {
            mkdir(false);
            return true;
        }
        else
        {
            throw new VrsException("Don't know how to create:" + this);
        }
    }

    public VPath renameTo(VPath other) throws VrsException
    {
        if ((other instanceof VFSPath) == false)
        {
            throw new VrsException("Can only rename VFSPaths to other VFSPaths (VPath is not a filesystem path)");
        }

        return renameTo((VFSPath) other);
    }

    // ===================
    // Abstract Interface
    // ===================

    public abstract FileAttributes getFileAttributes() throws VrsException;

    @Override
    public abstract boolean isRoot() throws VrsException;

    @Override
    public abstract List<? extends VFSPath> list() throws VrsException;

    /**
     * Create this (virtual) path as an actual file on this FileSystem.
     * 
     * @throws VrsException
     */
    public abstract void createFile() throws VrsException;

    /**
     * Create this (virtual) path as an actual directory on this FileSystem.
     * 
     * @param ignoreExisting
     *            - if directory already exists, return without exception.
     * @throws VrsException
     */
    public abstract void mkdir(boolean ignoreExisting) throws VrsException;

    public abstract void delete(boolean recurse) throws VrsException;

    public abstract VFSPath renameTo(VFSPath other) throws VrsException;

}
