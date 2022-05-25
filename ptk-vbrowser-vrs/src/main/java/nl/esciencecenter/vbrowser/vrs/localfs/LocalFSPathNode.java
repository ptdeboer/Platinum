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

package nl.esciencecenter.vbrowser.vrs.localfs;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.io.FSPath;
import nl.esciencecenter.ptk.io.RandomReadable;
import nl.esciencecenter.ptk.io.RandomWritable;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.exceptions.*;
import nl.esciencecenter.vbrowser.vrs.io.VFSFileAttributes;
import nl.esciencecenter.vbrowser.vrs.io.VRandomAccessable;
import nl.esciencecenter.vbrowser.vrs.io.VStreamAccessable;
import nl.esciencecenter.vbrowser.vrs.node.VFSPathNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.List;

import static nl.esciencecenter.ptk.io.FSUtil.fsutil;

@Slf4j
public class LocalFSPathNode extends VFSPathNode implements VStreamAccessable, VRandomAccessable {

    protected LocalFileSystem localfs;

    protected FSPath fsNode;

    protected LocalFSPathNode(LocalFileSystem fileSystem, FSPath node) {
        super(fileSystem, new VRL(node.toURI()));
        this.localfs = fileSystem;
        this.fsNode = node;
    }

    @Override
    public boolean isRoot() {
        return fsNode.isRoot();
    }

    @Override
    public boolean isDir(LinkOption... linkOptions) {
        return fsNode.isDirectory(linkOptions);
    }

    @Override
    public boolean isFile(LinkOption... linkOptions) {
        return fsNode.isFile(linkOptions);
    }

    @Override
    public boolean exists(LinkOption... linkOptions) {
        return fsNode.exists(linkOptions);
    }

    @Override
    public List<VFSPath> list() throws VrsException {
        log.debug("list():{}", this);

        try {
            FSPath[] nodes;
            nodes = fsNode.listNodes();
            ArrayList<VFSPath> pathNodes = new ArrayList<VFSPath>();
            for (FSPath node : nodes) {
                log.debug(" - adding:{}", node);
                pathNodes.add(new LocalFSPathNode(localfs, node));
            }
            return pathNodes;

        } catch (java.nio.file.AccessDeniedException e) {
            throw new ResourceAccessDeniedException(e.getMessage(), e);
        } catch (IOException e) {
            throw LocalFileSystem.convertException(this, "Failed to list path:" + getVRL(), e);
        }
    }

    @Override
    public VFSFileAttributes getFileAttributes(LinkOption... linkOptions) throws VrsException {
        try {
            return new LocalFileAttributes(fsNode.getFSInterface().getBasicAttributes(fsNode, linkOptions));
        } catch (IOException e) {
            throw LocalFileSystem.convertException(this, "Failed to get FileAttributes from:" + getVRL(), e);
        }
    }

    public OutputStream createOutputStream(boolean append) throws VrsException {
        try {
            return fsutil().createOutputStream(fsNode, append);
        } catch (IOException e) {
            throw LocalFileSystem.convertException(this, "Failed to create OutputStream from:" + getVRL(), e);
        }
    }


    public InputStream createInputStream() throws VrsException {
        try {
            return fsutil().createInputStream(fsNode);
        } catch (IOException e) {
            throw LocalFileSystem.convertException(this, "Failed to create InputStream from:" + getVRL(), e);
        }
    }

    @Override
    public boolean createFile(boolean ignoreExisting) throws VrsException {
        try {
            if ((ignoreExisting == false) && exists()) {
                throw new ResourceCreationException("File already exists:" + getVRL(), null);
            }

            fsNode = fsNode.create();
        } catch (IOException e) {
            throw LocalFileSystem.convertException(this, "Failed to create file:" + getVRL(), e);
        }

        return true;
    }

    @Override
    public boolean mkdir(boolean ignoreExisting) throws VrsException {
        try {
            if (fsNode.exists()) {
                if (ignoreExisting) {
                    return true;
                } else {
                    throw new ResourceAlreadyExistsException("mkdir(): directory already exists:"
                            + this.fsNode.getPath(), null);
                }

            }
            fsNode.getFSInterface().mkdir(fsNode);
        } catch (IOException e) {
            throw LocalFileSystem.convertException(this, "Failed to create directory:" + getVRL(), e);
        }

        return true;
    }

    public boolean delete() throws VrsException {
        return delete(LinkOption.NOFOLLOW_LINKS);
    }

    @Override
    public boolean delete(LinkOption... options) throws VrsException {
        try {
            if (fsNode.isDirectory(options)) {
                List<FSPath> nodes = fsNode.list();
                if ((nodes != null) && (nodes.size() > 0)) {
                    throw new ResourceNotEmptyException("Directory is not empty:" + fsNode, null);
                }
            }

            fsNode.delete(options);
        } catch (IOException e) {
            throw LocalFileSystem.convertException(this, "Couldn't delete:" + getVRL(), e);
        }
        return true;
    }

    @Override
    public VFSPath renameTo(VFSPath other) throws VrsException {
        try {
            String newPath = fsNode.renameTo(other.getVRL().getPath()).getPathname();
            return this.resolve(newPath);
        } catch (IOException e) {
            throw LocalFileSystem.convertException(this, "Couldn't rename:" + getVRL(), e);
        }
    }

    @Override
    public boolean sync() {
        return fsNode.sync();
    }

    @Override
    public long fileLength(LinkOption... linkOptions) throws VrsException {
        try {
            return fsNode.getFileSize();
        } catch (IOException e) {
            throw LocalFileSystem.convertException(this, "Couldn't get file size of:" + getVRL(), e);
        }
    }

    @Override
    public RandomReadable createRandomReadable() throws VrsException {
        try {
            return fsutil().createRandomReader(fsNode);
        } catch (IOException e) {
            throw LocalFileSystem.convertException(this, "Couldn't create RandomReadable from:" + getVRL(), e);
        }

    }

    @Override
    public RandomWritable createRandomWritable() throws VrsException {
        try {
            return fsutil().createRandomWriter(fsNode);
        } catch (IOException e) {
            throw LocalFileSystem.convertException(this, "Couldn't create RandomWriter from:" + getVRL(), e);
        }
    }

    public String createPermissionsString() {
        boolean d = this.isDir(LinkOption.NOFOLLOW_LINKS);
        boolean r = this.fsNode.isReadable();
        boolean w = this.fsNode.isWritable();
        boolean x = this.fsNode.isExecutable();
        boolean l = fsNode.isSymbolicLink();

        char[] drwx = new char[4];

        drwx[0] = d ? 'd' : '-';
        drwx[0] = l ? 'l' : drwx[0];
        drwx[1] = r ? 'r' : '-';
        drwx[2] = w ? 'w' : '-';
        drwx[3] = x ? 'x' : '-';
        return new String(drwx);
    }

}
