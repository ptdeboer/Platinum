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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.FileSystemException;

import nl.esciencecenter.ptk.io.FSUtil;
import nl.esciencecenter.ptk.net.URIFactory;
import nl.esciencecenter.ptk.net.URIUtil;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceAccessDeniedException;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceCreationException;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceNotEmptyException;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceNotFoundException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsIOException;
import nl.esciencecenter.vbrowser.vrs.io.VStreamCreator;
import nl.esciencecenter.vbrowser.vrs.node.VFileSystemNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class LocalFileSystem extends VFileSystemNode implements VStreamCreator
{
    private FSUtil fsUtil;

    public LocalFileSystem(VRSContext context) throws VrsException
    {
        super(context, new VRL("file:/"));
        fsUtil = new FSUtil();
    }

    @Override
    protected LocalFSPathNode createVFSNode(VRL vrl) throws VrsException
    {
        return createNode(vrl.getPath());
    }

    protected LocalFSPathNode createNode(String path) throws VrsException
    {
        try
        {
            // optionall resolve tilde:
            VRL homeVLR = this.getVRSContext().getHomeVRL();
            path = URIFactory.resolveTilde(homeVLR.getPath(), path);
            return new LocalFSPathNode(this, fsUtil.newFSNode(path));
        }
        catch (IOException e)
        {
            throw convertException(null, "Failed to resolve path:" + path, e);
        }
    }

    protected FSUtil getFSUtil()
    {
        return this.fsUtil;
    }
    
    @Override
    public InputStream createInputStream(VRL vrl) throws VrsException
    {
        return createVFSNode(vrl).createInputStream();
    }

    @Override
    public OutputStream createOutputStream(VRL vrl) throws VrsException
    {
        return createVFSNode(vrl).createOutputStream(false);
    }

    public static VrsException convertException(VPath sourcePath, String actionText, Throwable ex)
    {
        // new nio.file exceptions have reason in the Exception name.
        if (ex instanceof java.nio.file.AccessDeniedException)
        {
            return new ResourceAccessDeniedException(sourcePath, actionText + "\n" + "Access Denied.\n" + ex.getMessage(), ex);
        }
        else if (ex instanceof java.nio.file.DirectoryNotEmptyException)
        {
            return new ResourceNotEmptyException(sourcePath, actionText + "\n" + "Directory not empty.\n" + ex.getMessage(), ex);
        }
        else if (ex instanceof java.nio.file.FileAlreadyExistsException)
        {
            return new ResourceCreationException(sourcePath, actionText + "\n" + "File already exists.\n" + ex.getMessage(), ex);
        }
        else if (ex instanceof java.nio.file.NoSuchFileException)
        {
            return new ResourceNotFoundException(sourcePath, actionText + "\n" + "No such file.\n" + ex.getMessage(), ex);
        }
        else if (ex instanceof java.nio.file.NotDirectoryException)
        {
            return new ResourceNotFoundException(sourcePath, actionText + "\n" + "Not a directory.\n" + ex.getMessage(), ex);
        }
        else if (ex instanceof java.nio.file.NotLinkException)
        {
            return new ResourceNotFoundException(sourcePath, actionText + "\n" + "Not a link.\n" + ex.getMessage(), ex);
        }
        else if (ex instanceof FileSystemException)
        {
            // Exception name provide reason
            String exName = ex.getClass().getName();
            return new VrsIOException(actionText + "\n" + exName + ".\n" + ex.getMessage(), (IOException) ex);
        }
        else if (ex instanceof IOException)
        {
            // Exception name provide reason
            String exName = ex.getClass().getName();
            return new VrsIOException(actionText + "\n" + exName + ".\n" + ex.getMessage(), (IOException) ex);
        }
        else
        {
            return new VrsException(actionText + "\n" + ex.getMessage(), ex);
        }

    }


}
