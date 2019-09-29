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

package nl.esciencecenter.vbrowser.vrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.ptk.data.ListHolder;
import nl.esciencecenter.ptk.data.VARListHolder;
import nl.esciencecenter.ptk.io.RandomReadable;
import nl.esciencecenter.ptk.io.RandomWritable;
import nl.esciencecenter.ptk.io.ResourceProvider;
import nl.esciencecenter.ptk.object.Disposable;
import nl.esciencecenter.ptk.util.ContentReader;
import nl.esciencecenter.ptk.util.ContentWriter;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceCreationException;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceTypeMismatchException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.infors.InfoRootNode;
import nl.esciencecenter.vbrowser.vrs.io.VInputStreamCreator;
import nl.esciencecenter.vbrowser.vrs.io.VOutputStreamCreator;
import nl.esciencecenter.vbrowser.vrs.io.VRandomReadable;
import nl.esciencecenter.vbrowser.vrs.io.VRandomWritable;
import nl.esciencecenter.vbrowser.vrs.io.VStreamReadable;
import nl.esciencecenter.vbrowser.vrs.io.VStreamWritable;
import nl.esciencecenter.vbrowser.vrs.io.copy.VRSCopyManager;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Client to Virtual Resource System.
 */
public class VRSClient implements Disposable, ResourceProvider {

    protected VRSContext vrsContext = null;

    protected VRL currentPathVRL = null;

    protected VRL homeVRL = null;

    /**
     * Copy/Move TaskManager for this client. Typically one VRSClient is linked with one
     * transferManager.
     */
    protected VRSCopyManager transferManager = null;

    public VRSClient(VRSContext vrsContext) {
        this.vrsContext = vrsContext;
        this.homeVRL = vrsContext.getHomeVRL();
        this.currentPathVRL = vrsContext.getCurrentPathVRL();
        this.transferManager = new VRSCopyManager(this);
    }

    public VRSContext getVRSContext() {
        return this.vrsContext;
    }

    public VPath openPath(VRL vrl) throws VrsException {
        VResourceSystem resourceSystem = getVResourceSystemFor(vrl);
        return resourceSystem.resolve(vrl);
    }

    public VFSPath openVFSPath(VRL vrl) throws VrsException {
        VPath path = this.openPath(vrl);
        if (path instanceof VFSPath) {
            return (VFSPath) path;
        } else {
            throw new VrsException("Location is not a filesystem path:(type=" + path.getResourceType() + "):" + vrl);
        }
    }

    public VResourceSystem getVResourceSystemFor(VRL vrl) throws VrsException {
        VResourceSystem resourceSystem = vrsContext.getRegistry().getVResourceSystemFor(vrsContext, vrl);
        if (resourceSystem == null) {
            throw new VrsException("Scheme not implemented, couldn't get ResourceSystem for:" + resourceSystem);
        }
        return resourceSystem;
    }

    public VResourceSystemFactory getVRSFactoryForScheme(String scheme) {
        return vrsContext.getRegistry().getVResourceSystemFactoryFor(vrsContext, scheme);
    }

    /**
     * Resolve relative path against current working path and return VRL.
     * 
     * @param path
     *            relative path
     * @return resolved absolute VRL
     * @throws VRLSyntaxException
     *             if path string contains invalid characters
     */
    public VRL resolvePath(String path) throws VRLSyntaxException {
        return currentPathVRL.resolvePath(path);
    }

    /**
     * Set current location to which relative paths and URIs are resolved to.
     * 
     * @param vrl
     *            current "working directory" or URI to resolve relative paths against.
     */
    public void setCurrentPath(VRL vrl) {
        if (vrl == null) {
            throw new NullPointerException("Current path can not be NULL!");
        }

        this.currentPathVRL = vrl;
    }

    public OutputStream createOutputStream(VRL vrl) throws VrsException {
        VResourceSystem vrs = getVResourceSystemFor(vrl);
        if ((vrs instanceof VOutputStreamCreator) == false) {
            throw new VrsException("createOutputStream() not support for scheme:" + vrl);
        }
        return ((VOutputStreamCreator) vrs).createOutputStream(vrl);
    }

    public InputStream createInputStream(VRL vrl) throws VrsException {
        VResourceSystem vrs = getVResourceSystemFor(vrl);
        if ((vrs instanceof VInputStreamCreator) == false) {
            throw new VrsException("createInputStream() not support for scheme:" + vrl);
        }
        return ((VInputStreamCreator) vrs).createInputStream(vrl);
    }

    /**
     * VRS copy and move manager.
     * 
     * @return VRSCopyManager
     */
    public VRSCopyManager getVRSTransferManager() {
        return transferManager;
    }

    /**
     * Return the Root Node of the Info Resource System. Virtual location to start browsing.
     * 
     * @return InfoRootNode which is the logical root of the Virtual Resource System.
     * @throws VrsException
     */
    public InfoRootNode getInfoRootNode() throws VrsException {
        return (InfoRootNode) openPath(vrsContext.getInfoRootNodeVRL());
    }

    public List<VPath> openPaths(List<VRL> vrls) throws VrsException {
        ArrayList<VPath> paths = new ArrayList<VPath>();
        for (VRL vrl : vrls) {
            paths.add(openPath(vrl));
        }
        return paths;
    }

    public ResourceConfigInfo getResourceSystemInfoFor(VRL vrl, boolean autoCreate) throws VrsException {
        return this.vrsContext.getResourceSystemInfoFor(vrl, autoCreate);
    }

    public VPath copyFileToDir(VRL sourceFile, VRL destDirectory) throws VrsException {
        VARListHolder<VPath> resultPathsH = new ListHolder<VPath>();
        boolean result = transferManager.doCopyMove(new ExtendedList<VRL>(sourceFile), destDirectory, false, null,
                resultPathsH, null, null);
        if ((result == false) || (resultPathsH.isEmpty())) {
            throw new ResourceCreationException("No results for CopyMove action:" + sourceFile + "to:" + destDirectory,
                    null);
        }
        return resultPathsH.get().get(0);
    }

    public VPath copyDirToDir(VRL sourceDir, VRL destParentDirectory) throws VrsException {
        VARListHolder<VPath> resultPathsH = new ListHolder<VPath>();
        boolean result = transferManager.doCopyMove(new ExtendedList<VRL>(sourceDir), destParentDirectory, false, null,
                resultPathsH, null, null);
        if ((result == false) || (resultPathsH.isEmpty())) {
            throw new ResourceCreationException("No results for CopyMove action:" + sourceDir + "to:"
                    + destParentDirectory, null);
        }
        return resultPathsH.get().get(0);
    }

    public boolean existsDir(VRL dirVrl) throws VrsException {
        VPath path = this.openPath(dirVrl);
        if ((path instanceof VFSPath) == false) {
            return false;
        }
        VFSPath vfsPath = (VFSPath) path;
        return (vfsPath.exists() && vfsPath.isDir());
    }

    public boolean existsFile(VRL dirVrl) throws VrsException {
        VPath path = this.openPath(dirVrl);
        if ((path instanceof VFSPath) == false) {
            return false;
        }
        VFSPath vfsPath = (VFSPath) path;
        return (vfsPath.exists() && vfsPath.isFile());
    }

    public VFSPath mkdirs(VRL dirVrl) throws VrsException {
        VFSPath path = openVFSPath(dirVrl);
        path.mkdirs(true);
        return path;
    }

    public OutputStream createOutputStream(VPath file, boolean append) throws VrsException {
        if (file instanceof VStreamWritable) {
            return ((VStreamWritable) file).createOutputStream(append);
        } else {
            throw new ResourceTypeMismatchException("Cannot create InputStream from:" + file, null);
        }
    }

    public InputStream createInputStream(VPath file) throws VrsException {
        if (file instanceof VStreamReadable) {
            return ((VStreamReadable) file).createInputStream();
        } else {
            throw new ResourceTypeMismatchException("Cannot create InputStream from:" + file, null);
        }
    }

    public RandomReadable createRandomReader(VPath file) throws VrsException {
        if (file instanceof VRandomReadable) {
            return ((VRandomReadable) file).createRandomReadable();
        } else {
            throw new ResourceTypeMismatchException("Cannot create RandomReadable from:" + file, null);
        }
    }

    public RandomWritable createRandomWriter(VPath file) throws VrsException {
        if (file instanceof VRandomWritable) {
            return ((VRandomWritable) file).createRandomWritable();
        } else {
            throw new ResourceTypeMismatchException("Cannot create RandomWriter from:" + file, null);
        }
    }

    public VFSPath moveFileToDir(VFSPath file, VFSPath destinationDir) throws VrsException {
        VARListHolder<VFSPath> resultPathsH = new ListHolder<VFSPath>();
        VARListHolder<VPath> deletedNodesH = new ListHolder<VPath>();
        this.transferManager.doCopyMove(new ExtendedList<VFSPath>(file), destinationDir, true, resultPathsH,
                deletedNodesH, null);
        return resultPathsH.get(0);
    }

    public VFSPath copyFileToDir(VFSPath file, VFSPath destinationDir) throws VrsException {
        VARListHolder<VFSPath> resultPathsH = new ListHolder<VFSPath>();
        VARListHolder<VPath> deletedNodesH = new ListHolder<VPath>();
        this.transferManager.doCopyMove(new ExtendedList<VFSPath>(file), destinationDir, false, resultPathsH,
                deletedNodesH, null);
        return resultPathsH.get(0);
    }

    public VFSPath moveFileToFile(VFSPath sourceFile, VFSPath targetFile) throws VrsException {
        transferManager.copyMoveToFile(sourceFile, targetFile, true);
        return targetFile;
    }

    public VFSPath copyFileToFile(VFSPath sourceFile, VFSPath targetFile) throws VrsException {
        transferManager.copyMoveToFile(sourceFile, targetFile, false);
        return targetFile;
    }

    public VFSPath copyDirToDir(VFSPath sourceDir, VFSPath destinationPARENTDir, String optSubdirectoryName)
            throws VrsException {
        return copyMoveDirToDir(sourceDir, destinationPARENTDir, optSubdirectoryName, false);
    }

    public VFSPath moveDirToDir(VFSPath sourceDir, VFSPath destinationPARENTDir, String optSubdirectoryName)
            throws VrsException {
        return copyMoveDirToDir(sourceDir, destinationPARENTDir, optSubdirectoryName, true);
    }

    public VFSPath copyMoveDirToDir(VFSPath sourceDir, VFSPath destinationPARENTDir, String optSubdirectoryName,
            boolean isMove) throws VrsException {
        // resolve optional new SubDirectory name
        if (optSubdirectoryName == null) {
            optSubdirectoryName = sourceDir.getVRL().getBasename();
        }

        VFSPath targetDir = destinationPARENTDir.resolve(optSubdirectoryName);
        targetDir.mkdir(false);
        this.transferManager.copyMoveDirContents(sourceDir, targetDir, true, null);
        return targetDir;
    }

    public void dispose() {
        this.currentPathVRL = null;
        this.homeVRL = null;
        if (this.transferManager!=null) {
            this.transferManager.dispose();
            this.transferManager = null;
        }
        if (this.vrsContext!=null) {
            this.vrsContext.dispose();
            this.vrsContext = null;
        }
    }

    // ====================================
    // Read/Writer helpers
    // ====================================

    public void writeContents(VPath file, String xml) throws VrsException {
        try (OutputStream outps = createOutputStream(file, false)) {
            new ContentWriter(outps, false).write(xml);
        } catch (IOException e) {
            throw new VrsException("Failed to write String contents to:" + file, e);
        }
    }

    public void writeContents(VPath file, byte[] bytes) throws VrsException {
        try (OutputStream outps = createOutputStream(file, false)) {
            new ContentWriter(outps, false).write(bytes);
        } catch (IOException e) {
            throw new VrsException("Failed to write Byte contents to:" + file, e);
        }
    }

    public String readContentsAsString(VPath file) throws VrsException {
        try (InputStream inps = this.createInputStream(file)) {
            return new ContentReader(inps, this.vrsContext.getCharEncoding(), false).readString();
        } catch (IOException e) {
            throw new VrsException("Failed to read String contents from:" + file);
        }
    }

    public byte[] readContents(VPath file) throws VrsException {
        try (InputStream inps = this.createInputStream(file)) {
            return new ContentReader(inps, this.vrsContext.getCharEncoding(), false).readBytes();
        } catch (IOException e) {
            throw new VrsException("Failed to read Byte contents from:" + file);
        }
    }
    
    /** 
     * Create URI/URL base Resource loader using this VRSClient 
     */ 
    public ResourceLoader createResourceLoader() {
        return new ResourceLoader(this, null);
    }

    // ====================================
    // URI Based ResourceProvider interface 
    // ====================================

    @Override
    public URI resolvePathURI(String relpath) throws Exception {
        return this.vrsContext.getCurrentPathVRL().resolvePath(relpath).toURI();
    }

    @Override
    public OutputStream createOutputStream(URI uri) throws Exception {
        return this.createOutputStream(new VRL(uri));
    }

    @Override
    public InputStream createInputStream(URI uri) throws Exception {
        return this.createInputStream(new VRL(uri));
    }

    @Override
    public RandomReadable createRandomReader(URI uri) throws Exception {
        return this.createRandomReader(openPath(new VRL(uri)));
    }

    @Override
    public RandomWritable createRandomWriter(URI uri) throws Exception {
        return this.createRandomWriter(openPath(new VRL(uri)));
    }

}
