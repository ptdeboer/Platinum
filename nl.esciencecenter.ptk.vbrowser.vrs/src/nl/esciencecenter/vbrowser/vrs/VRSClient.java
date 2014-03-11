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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.infors.InfoRootNode;
import nl.esciencecenter.vbrowser.vrs.io.VInputStreamCreator;
import nl.esciencecenter.vbrowser.vrs.io.VOutputStreamCreator;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfo;
import nl.esciencecenter.vbrowser.vrs.task.VRSTranferManager;
import nl.esciencecenter.vbrowser.vrs.util.VRSResourceProvider;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VRSClient
{
    protected VRSContext vrsContext=null; 
    
    protected VRL currentPathVRL=null;
    
    protected VRL homeVRL=null;
    
    /** 
     * TaskManager for this client. 
     */
    protected VRSTranferManager transferManager=null;
    
    public VRSClient(VRSContext vrsContext)
    {
        this.vrsContext=vrsContext;
        this.homeVRL=vrsContext.getHomeVRL(); 
        this.currentPathVRL=vrsContext.getCurrentPathVRL(); 
        this.transferManager=new VRSTranferManager(this); 
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
     * Set current location to which relative paths and URIs are resolve to. 
     * @param vrl current workind director or URI to resolve relative paths against. 
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
    
    public VRSTranferManager getVRSTransferManager()
    {
        return transferManager; 
    }

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
     * Create stateful resourceloader using this VRSClient. 
     */
    public ResourceLoader createResourceLoader()
    {
        VRSResourceProvider prov=new VRSResourceProvider(this); 
        ResourceLoader loader = new ResourceLoader(prov,null); 
        return loader; 
    }

    
}
