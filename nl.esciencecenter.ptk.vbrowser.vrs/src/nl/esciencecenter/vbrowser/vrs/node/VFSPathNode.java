/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.vbrowser.vrs.node;

import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.*;

import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.io.FSNode;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
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

    public VFileSystem getVFileSystem()
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
    public List<String> getChildResourceTypes() throws VrsException
    {
        if (isDir())
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
    public boolean isDir(LinkOption... linkOptions) throws VrsException
    {
        return getFileAttributes(linkOptions).isDirectory();
    }

    @Override
    public boolean isFile(LinkOption... linkOptions) throws VrsException
    {
        return getFileAttributes(linkOptions).isRegularFile();
    }

    public boolean create() throws VrsException
    {
        if (isFile())
        {
            createFile(false);
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

    public VFSPath create(String type,String name,boolean ignoreExisting) throws VrsException
    {
        if (isFile()) 
        {
            throw new VrsException("Files can not create child nodes"); 
        }
        
        VFSPath path=this.resolvePath(name); 
        
        if (StringUtil.equals(type,VRSTypes.FILE_TYPE)) 
        {
            path.createFile(ignoreExisting); 
        }
        else if (StringUtil.equals(type,VRSTypes.DIR_TYPE))
        {
            path.mkdir(ignoreExisting); 
        }
        else
        {
            throw new VrsException("Type not supported:"+type); 
        }
        
        return path;
        
    }
    public VPath renameTo(VPath other) throws VrsException
    {
        if ((other instanceof VFSPath) == false)
        {
            throw new VrsException("Can only rename VFSPaths to other VFSPaths (VPath is not a filesystem path)");
        }

        return renameTo((VFSPath) other);
    }


    @Override
    public void mkdirs(boolean ignoreExisting) throws VrsException
    {
        List<VFSPath> paths=new ArrayList<VFSPath>(); 

        // walk up tree: 
        VFSPath path=this; 
        while (path.isRoot()==false)
        {
            path=path.getParent();
            if (paths.contains(path))
            {
                ClassLogger.getLogger(this.getClass()).errorPrintf("*** Path Cycle detected, parent path:"+path+" already in path list from:"+this); 
                break; 
            }
            
            paths.add(path);  
        }
        
        for (int i=(paths.size()-1);i>=0;i--)
        {
            VFSPath dir = paths.get(i);
            if (dir.exists()==false)
            {
                dir.mkdir(false);
            }
        }
        
        mkdir(ignoreExisting); 
    }
    
    @Override
    public void delete(boolean recurse) throws VrsException
    {
        // todo make option: default link behaviour 
        delete(recurse,LinkOption.NOFOLLOW_LINKS); 
    }

    @Override
    public VFSPath renameTo(String nameOrPath) throws VrsException
    {
        VFSPath newPath=this.resolvePath(nameOrPath); 
        return this.renameTo(newPath); 
    }
    
    // ===================
    // Abstract Interface
    // ===================

    /**
     * @return - true if path exists. 
     * @throws VrsException
     */
    public abstract boolean exists(LinkOption... linkOptions) throws VrsException; 
    
    public abstract FileAttributes getFileAttributes(LinkOption... linkOptions) throws VrsException;

    @Override
    public abstract boolean isRoot() throws VrsException;

    @Override
    public abstract List<? extends VFSPath> list() throws VrsException;

    /**
     * Create this (virtual) path as an actual file on this FileSystem.
     * 
     * @throws VrsException
     */
    public abstract void createFile(boolean ignoreExisting) throws VrsException;

    /**
     * Create this (virtual) path as an actual directory on this FileSystem.
     * 
     * @param ignoreExisting
     *            - if directory already exists, return without exception.
     * @throws VrsException
     */
    public abstract void mkdir(boolean ignoreExisting) throws VrsException;
    
    public abstract void delete(boolean recurse, LinkOption... linkOptions) throws VrsException;

    /** 
     * Path must be an same FileSystem. 
     * @param newPath - target path 
     * @return similar path 
     * @throws VrsException
     */
    public abstract VFSPath renameTo(VFSPath newPath) throws VrsException;

}
