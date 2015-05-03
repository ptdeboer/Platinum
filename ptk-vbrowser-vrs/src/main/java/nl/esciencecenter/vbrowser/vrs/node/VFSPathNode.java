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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.io.FSPath;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VFileSystem;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VRSTypes;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.io.VFSFileAttributes;
import nl.esciencecenter.vbrowser.vrs.io.VStreamWritable;

import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Default implementation for VFSPaths.
 */
public abstract class VFSPathNode extends VPathNode implements VFSPath
{
    private static final PLogger logger = PLogger.getLogger(VFSPathNode.class);

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
            ATTR_ISSYMBOLIC_LINK,
            ATTR_PERMISSIONSTRING
    // implementation specific permissions string
    };

    protected VFSPathNode(VFileSystem fileSystem, VRL vrl)
    {
        super(fileSystem, vrl);
    }

    public VFileSystem getFileSystem()
    {
        return (VFileSystem) resourceSystem;
    }

    @Override
    public VFSPath getParent() throws VrsException
    {
        return resolvePath(getDirname());
    }

    @Override
    public VFSPath resolvePath(String relativePath) throws VrsException
    {
        VRL resolvedVrl = vrl.resolvePath(relativePath);
        return getFileSystem().resolvePath(resolvedVrl);
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
            return FSPath.DIR_TYPE;
        }
        else
        {
            return FSPath.FILE_TYPE;
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

    /**
     * @return path part of VRL as string.
     */
    public String getPathAsString()
    {
        return vrl.getPath();
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

    public Attribute getImmutableAttribute(String name) throws VrsException
    {
        // extra Immutable VFS Attributes:
        if (name.compareTo(ATTR_DIRNAME) == 0)
        {
            return new Attribute(name, getVRL().getDirname());
        }
        else if (name.compareTo(ATTR_PATH) == 0)
        {
            return new Attribute(name, getVRL().getPath());
        }

        return super.getImmutableAttribute(name);

    }

    /**
     * Returns single File Resource Attribute.
     */
    public Attribute getResourceAttribute(String name) throws VrsException
    {
        if (name == null)
        {
            return null;
        }

        // Check if super class has this attribute
        Attribute supervalue = super.getResourceAttribute(name);

        if (supervalue != null)
        {
            return supervalue;
        }

        // --------
        // mutable
        // --------

        if (name.compareTo(ATTR_RESOURCE_EXISTS) == 0)
        {
            return new Attribute(name, exists());
        }
        else if (name.compareTo(ATTR_ISDIR) == 0)
        {
            return new Attribute(name, isDir());
        }
        else if (name.compareTo(ATTR_ISFILE) == 0)
        {
            return new Attribute(name, isFile());
        }
        else if (name.compareTo(ATTR_ISSYMBOLIC_LINK) == 0)
        {
            return new Attribute(name, getFileAttributes().isSymbolicLink());
        }
        else if (name.compareTo(ATTR_FILE_SIZE) == 0)
        {
            return new Attribute(name, getFileAttributes().size());
        }
        else if (name.compareTo(ATTR_ISHIDDEN) == 0)
        {
            return new Attribute(name, isHidden());
        }
        else if (name.compareTo(ATTR_MODIFICATION_TIME) == 0)
        {
        	FileTime time = getFileAttributes().lastModifiedTime();
        	
            if (time != null)
            {
                return new Attribute(name, Presentation.createDate(time));
            }
        }
        else if (name.compareTo(ATTR_LASTACCESS_TIME) == 0)
        {
            FileTime time = getFileAttributes().lastAccessTime();
            if (time != null)
            {
                return new Attribute(name, Presentation.createDate(time));
            }
        }
        else if (name.compareTo(ATTR_CREATION_TIME) == 0)
        {
            FileTime time = getFileAttributes().creationTime();
            if (time != null)
            {
                return new Attribute(name, Presentation.createDate(time));
            }
        }
        // else if (name.compareTo(ATTR_PERMISSIONSTRING) == 0)
        // {
        // return new Attribute(name,getFileAttributes().getPermissionsString());
        // }

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

    public boolean isHidden()
    {
    	return false; 
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

    @Override
    public VFSPath create(String type, String name) throws VrsException
    {
        return create(type, name, false);
    }

    public VFSPath create(String type, String name, boolean ignoreExisting) throws VrsException
    {
        if (isFile())
        {
            throw new VrsException("Files can not create child nodes");
        }

        VFSPath path = this.resolvePath(name);

        if (StringUtil.equals(type, VRSTypes.FILE_TYPE))
        {
            path.createFile(ignoreExisting);
        }
        else if (StringUtil.equals(type, VRSTypes.DIR_TYPE))
        {
            path.mkdir(ignoreExisting);
        }
        else
        {
            throw new VrsException("Type not supported:" + type);
        }

        return path;

    }

    public VFSPath renameTo(VPath other) throws VrsException
    {
        if ((other instanceof VFSPath) == false)
        {
            throw new VrsException("Can only rename VFSPaths to other VFSPaths (VPath is not a filesystem path)");
        }

        return renameTo((VFSPath) other);
    }

    @Override
    public boolean mkdirs(boolean ignoreExisting) throws VrsException
    {
        List<VFSPath> paths = new ArrayList<VFSPath>();

        // walk up tree:
        VFSPath path = this;

        while (path.isRoot() == false)
        {
            VFSPath prev = path;
            path = path.getParent();
            if (path == null)
            {
                logger.errorPrintf("FIXME: Current path is not root, but getParent() return null!\n");
                break;
            }

            if (paths.contains(path) || prev.getVRL().equals(path.getVRL()))
            {
                PLogger.getLogger(this.getClass()).errorPrintf(
                        "*** Path Cycle detected, parent path:" + path + " already in path list from:" + this);
                break;
            }

            paths.add(path);
        }

        for (int i = (paths.size() - 1); i >= 0; i--)
        {
            VFSPath dir = paths.get(i);
            if (dir.exists() == false)
            {
                dir.mkdir(false);
            }
        }

        return mkdir(ignoreExisting);
    }

    @Override
    public VFSPath renameTo(String nameOrPath) throws VrsException
    {
        // resolve name against parent directory, the names applies to this location.
        VFSPath parentDir = this.getParent();
        VFSPath newPath = parentDir.resolvePath(nameOrPath);
        return this.renameTo(newPath);
    }

    public String toString()
    {
        return "<VFSPathNode>:[vrl=" + getVRL() + "]";
    }

    public boolean delete() throws VrsException
    {
        return delete(LinkOption.NOFOLLOW_LINKS);
    }

    public boolean delete(boolean recurse) throws VrsException
    {
        return delete(recurse, LinkOption.NOFOLLOW_LINKS);
    }

    public boolean delete(boolean recurse, LinkOption... linkOptions) throws VrsException
    {
        if (recurse == true)
        {
            deleteContents(this, linkOptions);
        }

        return delete(linkOptions);
    }

    /**
     * Recursive delete contents. Does not delete this directory.
     */
    public static void deleteContents(VFSPath dirPath, LinkOption... linkOptions) throws VrsException
    {
        // add content of current to heap
        List<? extends VFSPath> nodes = dirPath.list();

        if ((nodes == null) || (nodes.size() <= 0))
        {
            return;
        }

        for (VFSPath node : nodes)
        {
            // Pre emptive interruption!
            if (Thread.currentThread().isInterrupted())
            {
                throw new VrsException("Recursive delete was Interrupted!", new InterruptedException("Interrupted!"));
            }

            // ---
            // Assertion that the delete will not go UP the directory!
            // (Yes this has happened).
            // ---
            if (node.getVRL().isParentOf(dirPath.getVRL()))
            {
                throw new VrsException("Refusing to delete parent of current path:" + node);
            }
            else if (node.getVRL().equals(dirPath.getVRL()))
            {
                throw new VrsException("Recursive delete detected. Child node euqals parent:" + node);
            }

            if (node.isDir(linkOptions))
            {
                deleteContents(node, linkOptions);
            }
            node.delete();
        }
    }

    protected void createEmptyFile(VStreamWritable streamWritable) throws VrsException
    {
        try
        {
            OutputStream outps = streamWritable.createOutputStream(false);
            byte bytes[]=new byte[0];
            outps.write(bytes);
            outps.flush();
            outps.close();
        }
        catch (IOException e) {
            throw new VrsException (e.getMessage(),e); 
        }
    }
    
    // ===================
    // Abstract Interface
    // ===================

    /**
     * @return - true if path exists.
     * @throws VrsException
     */
    @Override
    public abstract boolean exists(LinkOption... linkOptions) throws VrsException;

    /**
     * @return returns FileAttributes for this VFSPath. FileAttributes extend
     *         java.nio.file.attribute.BasicFileAttributes.
     * @see java.nio.file.attribute.BasicFileAttributes;
     */
    public abstract VFSFileAttributes getFileAttributes(LinkOption... linkOptions) throws VrsException;

    /**
     * @return whether the current path is the FileSystem root path.
     */
    @Override
    public abstract boolean isRoot() throws VrsException;

    /**
     * Lists unfiltered and complete contents of this resource. Method should not sort the contents and return the list
     * as-is.
     */
    @Override
    public abstract List<? extends VFSPath> list() throws VrsException;

    /**
     * Create this (virtual) path as an actual file on this FileSystem.
     * 
     * @return true.
     * @throws VrsException
     */
    public abstract boolean createFile(boolean ignoreExisting) throws VrsException;

    /**
     * Create this (virtual) path as an actual directory on this FileSystem.
     * 
     * @param ignoreExisting
     *            - if directory already exists, return without exception.
     * @return true.
     * @throws VrsException
     */
    public abstract boolean mkdir(boolean ignoreExisting) throws VrsException;

    /**
     * Delete this resource.
     * 
     * @return true
     */
    public abstract boolean delete(LinkOption... linkOptions) throws VrsException;

    /**
     * Path must be an same FileSystem.
     * 
     * @param newPath
     *            - target path
     * @return equivalent path but object might not be the exact same object as was passed through.
     * @throws VrsException
     */
    public abstract VFSPath renameTo(VFSPath newPath) throws VrsException;

}
