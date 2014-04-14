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
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.ptk.data.ListHolder;
import nl.esciencecenter.ptk.data.VARListHolder;
import nl.esciencecenter.ptk.io.IOUtil;
import nl.esciencecenter.ptk.io.RandomReadable;
import nl.esciencecenter.ptk.io.RandomWritable;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceCreationException;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceTypeMismatchException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsIOException;
import nl.esciencecenter.vbrowser.vrs.infors.InfoRootNode;
import nl.esciencecenter.vbrowser.vrs.io.VInputStreamCreator;
import nl.esciencecenter.vbrowser.vrs.io.VOutputStreamCreator;
import nl.esciencecenter.vbrowser.vrs.io.VRandomReadable;
import nl.esciencecenter.vbrowser.vrs.io.VRandomWritable;
import nl.esciencecenter.vbrowser.vrs.io.VStreamReadable;
import nl.esciencecenter.vbrowser.vrs.io.VStreamWritable;
import nl.esciencecenter.vbrowser.vrs.io.copy.VRSCopyManager;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfo;
import nl.esciencecenter.vbrowser.vrs.util.VRSResourceProvider;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VRSClient
{
    protected VRSContext vrsContext=null; 
    
    protected VRL currentPathVRL=null;
    
    protected VRL homeVRL=null;
    
    /** 
     * Copy/Move TaskManager for this client. 
     * Typically one VRSClient is linked with one transferManager. 
     */
    protected VRSCopyManager transferManager=null;
    
    public VRSClient(VRSContext vrsContext)
    {
        this.vrsContext=vrsContext;
        this.homeVRL=vrsContext.getHomeVRL(); 
        this.currentPathVRL=vrsContext.getCurrentPathVRL(); 
        this.transferManager=new VRSCopyManager(this); 
    }
    
    public VRSContext getVRSContext()
    {
        return this.vrsContext; 
    }
    
    public VPath openPath(VRL vrl) throws VrsException
    {
        VResourceSystem resourceSystem = getVResourceSystemFor(vrl); 
        return resourceSystem.resolvePath(vrl);  
    }

    public VFSPath openVFSPath(VRL vrl) throws VrsException
    {
        VPath path=this.openPath(vrl); 
        if (path instanceof VFSPath)
        {
            return (VFSPath)path;
        }
        else
        {
            throw new VrsException("Location is not a filesystem path:(type="+path.getResourceType()+"):"+vrl); 
        }
    }
    
    public VResourceSystem getVResourceSystemFor(VRL vrl) throws VrsException
    {
        VResourceSystem resourceSystem = vrsContext.getRegistry().getVResourceSystemFor(vrsContext,vrl); 
        if (resourceSystem==null)
        {
            throw new VrsException("Scheme not implemented, couldn't get ResourceSystem for:"+resourceSystem);
        }
        return resourceSystem;  
    }

    public VResourceSystemFactory getVRSFactoryForScheme(String scheme)
    {
        return vrsContext.getRegistry().getVResourceSystemFactoryFor(vrsContext,scheme); 
    }

    /** 
     * Resolve relative path against current working path and return VRL. 
     * @param path relative path 
     * @return resolved absolute VRL 
     * @throws VRLSyntaxException if path string contains invalid characters 
     */
    public VRL resolvePath(String path) throws VRLSyntaxException
    {
        return currentPathVRL.resolvePath(path);
    }
    
    /** 
     * Set current location to which relative paths and URIs are resolved to. 
     * @param vrl current "working directory" or URI to resolve relative paths against. 
     */
    public void setCurrentPath(VRL vrl)
    {
        if (vrl==null)
        {
            throw new NullPointerException("Current path can not be NULL!"); 
        }
        
        this.currentPathVRL=vrl; 
    }

    public OutputStream createOutputStream(VRL vrl) throws VrsException
    {
        VResourceSystem vrs = getVResourceSystemFor(vrl);
        
        if ((vrs instanceof VOutputStreamCreator)==false)
        {
            throw new VrsException("createOutputStream() not support for scheme:"+vrl); 
        }
        
        return ((VOutputStreamCreator)vrs).createOutputStream(vrl);
    }

    public InputStream createInputStream(VRL vrl) throws VrsException
    {
        VResourceSystem vrs = getVResourceSystemFor(vrl);
        
        if ((vrs instanceof VInputStreamCreator)==false)
        {
            throw new VrsException("createInputStream() not support for scheme:"+vrl); 
        }
        
        return ((VInputStreamCreator)vrs).createInputStream(vrl);
    }
    
    /** 
     * VRS copy and move manager. 
     * @return VRSCopyManager
     */
    public VRSCopyManager getVRSTransferManager()
    {
        return transferManager; 
    }

    /** 
     * Return the Root Node of the Info Resource System. Virtual location to start browsing. 
     * @return InfoRootNode which is the logical root of the Virtual Resource System. 
     * @throws VrsException
     */
    public InfoRootNode getInfoRootNode() throws VrsException
    {
        return (InfoRootNode)openPath(new VRL("info:/")); 
    }

    public List<VPath> openPaths(List<VRL> vrls) throws VrsException
    {
        ArrayList<VPath> paths=new ArrayList<VPath>();
        
        for (VRL vrl:vrls)
        {
            paths.add(openPath(vrl)); 
        }
        
        return paths; 
    }

    public ResourceSystemInfo getResourceSystemInfoFor(VRL vrl, boolean autoCreate) throws VrsException
    {
        return this.vrsContext.getResourceSystemInfoFor(vrl, autoCreate);
    }

    /** 
     * Create statefull resourceloader using this VRSClient. 
     */
    public ResourceLoader createResourceLoader()
    {
        VRSResourceProvider prov=new VRSResourceProvider(this); 
        ResourceLoader loader = new ResourceLoader(prov,null); 
        return loader; 
    }

    public VPath copyFileToDir(VRL sourceFile,VRL destDirectory) throws VrsException
    {
       VARListHolder<VPath> resultPathsH=new ListHolder<VPath>(); 
       boolean result=transferManager.doCopyMove(new ExtendedList<VRL>(sourceFile), destDirectory,false, null,resultPathsH,null,null); 
       if ( (result==false) || (resultPathsH.isEmpty()) )
       {
           throw new ResourceCreationException(null,"No results for CopyMove action:"+sourceFile+"to:"+destDirectory,null);  
       }
       return resultPathsH.get().get(0); 
    }
    
    public VPath copyDirToDir(VRL sourceDir,VRL destParentDirectory) throws VrsException
    {
       VARListHolder<VPath> resultPathsH=new ListHolder<VPath>(); 
       boolean result=transferManager.doCopyMove(new ExtendedList<VRL>(sourceDir), destParentDirectory,false, null,resultPathsH,null,null); 
       if ( (result==false) || (resultPathsH.isEmpty()) )
       {
           throw new ResourceCreationException(null,"No results for CopyMove action:"+sourceDir+"to:"+destParentDirectory,null);  
       }
       return resultPathsH.get().get(0); 
    }

    public boolean existsDir(VRL dirVrl) throws VrsException
    {
        VPath path=this.openPath(dirVrl);  
        if ((path instanceof VFSPath)==false)
        {
            return false;
        }
        
        VFSPath vfsPath=(VFSPath)path; 
        return (vfsPath.exists() && vfsPath.isDir());
    }
    
    public boolean existsFile(VRL dirVrl) throws VrsException
    {
        VPath path=this.openPath(dirVrl);  
        if ((path instanceof VFSPath)==false)
        {
            return false;
        }
        
        VFSPath vfsPath=(VFSPath)path; 
        return (vfsPath.exists() && vfsPath.isFile());
    }
    
    public VFSPath mkdirs(VRL dirVrl) throws VrsException
    {
        VFSPath path=openVFSPath(dirVrl);
        path.mkdirs(true); 
        return path; 
    }

    public OutputStream createOutputStream(VPath file, boolean append)  throws VrsException
    {
        if (file instanceof VStreamWritable)
        {
            return ((VStreamWritable)file).createOutputStream(append); 
        }
        else
        {
            throw new ResourceTypeMismatchException(file,"Cannot create InputStream from:"+file,null); 
        }
    }
 
    public InputStream createInputStream(VPath file)  throws VrsException
    {
        if (file instanceof VStreamReadable)
        {
            return ((VStreamReadable)file).createInputStream();
        }
        else
        {
            throw new ResourceTypeMismatchException(file,"Cannot create InputStream from:"+file,null); 
        }
    }

    public RandomReadable createRandomReader(VPath file) throws VrsException
    {
        if (file instanceof VRandomReadable)
        {
            return ((VRandomReadable)file).createRandomReadable();
        }
        else
        {
            throw new ResourceTypeMismatchException(file,"Cannot create RandomReadable from:"+file,null); 
        }
    }

    public RandomWritable createRandomWriter(VPath file) throws VrsException
    {
        if (file instanceof VRandomWritable)
        {
            return ((VRandomWritable)file).createRandomWritable();
        }
        else
        {
            throw new ResourceTypeMismatchException(file,"Cannot create RandomWriter from:"+file,null); 
        }
    }
    
    public byte[] readContents(VPath file) throws VrsException
    {
        InputStream inps=this.createInputStream(file);
        byte[] bytes;
        try
        {
            bytes = new ResourceLoader().readBytes(inps);
            return bytes; 
        }
        catch (IOException e)
        {
            throw new VrsIOException(e.getMessage(),e); 
        } 
        finally
        {
            IOUtil.autoClose(inps); 
        }
    }

    public void writeContents(VPath file, byte bytes[]) throws VrsException
    {
        OutputStream outps=this.createOutputStream(file,false);
        try
        {
            new ResourceLoader().writeBytes(outps,bytes);
        }
        catch (IOException e)
        {
            throw new VrsIOException(e.getMessage(),e); 
        } 
        finally
        {
            IOUtil.autoClose(outps); 
        }
    }

    public VFSPath moveFileToDir(VFSPath file, VFSPath destinationDir) throws VrsException
    {
        VARListHolder<VFSPath> resultPathsH=new ListHolder<VFSPath>(); 
        VARListHolder<VPath> deletedNodesH=new ListHolder<VPath>(); 
        this.transferManager.doCopyMove(new ExtendedList<VFSPath>(file), destinationDir, true, resultPathsH, deletedNodesH, null); 
        return resultPathsH.get(0); 
    }
   

    public VFSPath copyFileToDir(VFSPath file, VFSPath destinationDir) throws VrsException
    {
        VARListHolder<VFSPath> resultPathsH=new ListHolder<VFSPath>(); 
        VARListHolder<VPath> deletedNodesH=new ListHolder<VPath>(); 
        this.transferManager.doCopyMove(new ExtendedList<VFSPath>(file), destinationDir, false, resultPathsH, deletedNodesH, null); 
        return resultPathsH.get(0); 
    }

    public VFSPath moveFileToFile(VFSPath sourceFile, VFSPath targetFile) throws VrsException
    {
        transferManager.copyMoveToFile(sourceFile, targetFile, true); 
        return targetFile; 
    }

    public VFSPath copyFileToFile(VFSPath sourceFile, VFSPath targetFile) throws VrsException
    {
        transferManager.copyMoveToFile(sourceFile, targetFile, false); 
        return targetFile; 
    }
    
    public VFSPath copyDirToDir(VFSPath sourceDir, VFSPath destinationPARENTDir, String optSubdirectoryName) throws VrsException
    {
        return copyMoveDirToDir(sourceDir,destinationPARENTDir,optSubdirectoryName,false); 
    }
    
    public VFSPath moveDirToDir(VFSPath sourceDir, VFSPath destinationPARENTDir, String optSubdirectoryName) throws VrsException
    {
        return copyMoveDirToDir(sourceDir,destinationPARENTDir,optSubdirectoryName,true); 
    }
    
    public VFSPath copyMoveDirToDir(VFSPath sourceDir, VFSPath destinationPARENTDir, String optSubdirectoryName, boolean isMove) throws VrsException
    {
        // resolve optional new SubDirectory name
        if (optSubdirectoryName==null)
        {   
            optSubdirectoryName=sourceDir.getVRL().getBasename(); 
        }
        
        VFSPath targetDir=destinationPARENTDir.resolvePath(optSubdirectoryName); 
        targetDir.mkdir(false);
        this.transferManager.copyMoveDirContents(sourceDir,targetDir, true, null); 
        return targetDir; 
    }
     
}
