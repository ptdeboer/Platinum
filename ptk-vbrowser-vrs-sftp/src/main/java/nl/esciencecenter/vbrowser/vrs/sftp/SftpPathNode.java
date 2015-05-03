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

package nl.esciencecenter.vbrowser.vrs.sftp;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.LinkOption;
import java.util.List;

import nl.esciencecenter.ptk.io.RandomReadable;
import nl.esciencecenter.ptk.io.RandomWritable;
import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.io.VRandomAccessable;
import nl.esciencecenter.vbrowser.vrs.io.VStreamAccessable;
import nl.esciencecenter.vbrowser.vrs.node.VFSPathNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import com.jcraft.jsch.SftpATTRS;

public class SftpPathNode extends VFSPathNode implements VStreamAccessable, VRandomAccessable
{
    private static final PLogger logger = PLogger.getLogger(SftpPathNode.class);

    private SftpFileSystem sftpfs;

    /** Absolute path on remote filesystem */
    private String path;

    private SftpATTRS attrs;

    protected SftpPathNode(SftpFileSystem sftpfs, VRL vrl)
    {
        super(sftpfs, vrl);
        this.sftpfs = sftpfs;
        init(vrl.getPath());
    }

    protected void init(String path)
    {
        this.path = path;
    }

    @Override
    public boolean isRoot()
    {
        if ("/".compareTo(path) == 0)
        {
            return true;
        }
        return false;
    }

    public SftpATTRS getSftpAttrs() throws VrsException
    {
        if (this.attrs == null)
        {
            // boolean resolveLink=(linkOptions!=null) && (linkOptions.length>0)
            // && (linkOptions[0]==LinkOption.NOFOLLOW_LINKS);
            boolean resolveLink = false;
            this.attrs = this.sftpfs.fetchSftpAttrs(path, resolveLink);
        }
        return attrs;
    }

    @Override
    public boolean isDir(LinkOption... linkOptions) throws VrsException
    {
        return (getSftpAttrs().isDir());
    }

    @Override
    public boolean isFile(LinkOption... linkOptions) throws VrsException
    {
        return (getSftpAttrs().isDir() == false);
    }

    @Override
    public boolean exists(LinkOption... linkOptions) throws VrsException
    {
        return sftpfs.exists(path);
    }

    @Override
    public List<? extends VFSPath> list() throws VrsException
    {
        logger.debugPrintf("list():%s\n", this);
        return sftpfs.listNodes(path);
    }

    @Override
    public SftpFileAttributes getFileAttributes(LinkOption... linkOptions) throws VrsException
    {
        return new SftpFileAttributes(getSftpAttrs());
    }

    public OutputStream createOutputStream(boolean append) throws VrsException
    {
        return this.sftpfs.createOutputStream(path, append);
    }

    public InputStream createInputStream() throws VrsException
    {
        return this.sftpfs.createInputStream(path);
    }

    @Override
    public boolean mkdir(boolean ignoreExisting) throws VrsException
    {
        return this.sftpfs.mkdir(this.getPathAsString(), ignoreExisting);
    }

    public boolean delete() throws VrsException
    {
        return delete(LinkOption.NOFOLLOW_LINKS);
    }

    @Override
    public boolean delete(LinkOption... linkOptions) throws VrsException
    {
        return this.sftpfs.delete(this.getPathAsString(), this.isDir(linkOptions),linkOptions);
    }

    @Override
    public VFSPath renameTo(VFSPath other) throws VrsException
    {
        return sftpfs.renameTo(this,other);
    }

    @Override
    public boolean sync()
    {
        this.attrs = null;
        return true;
    }

    @Override
    public long fileLength(LinkOption... linkOptions) throws VrsException
    {
        return this.getSftpAttrs().getSize();
    }

    @Override
    public RandomReadable createRandomReadable() throws VrsException
    {
        throw new VrsException("Not Implemented: createRandomReadable()");
    }

    @Override
    public RandomWritable createRandomWritable() throws VrsException
    {
        throw new VrsException("Not Implemented: createRandomWritable()");
    }

    @Override
    public boolean createFile(boolean ignoreExisting) throws VrsException
    {
        this.createEmptyFile(this);
        return true;
    }
}
