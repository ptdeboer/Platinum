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

import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.infors.InfoRootNode;
import nl.esciencecenter.vbrowser.vrs.io.VInputStreamCreator;
import nl.esciencecenter.vbrowser.vrs.io.VOutputStreamCreator;
import nl.esciencecenter.vbrowser.vrs.task.VRSTranferManager;
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

    public VPath openLocation(VRL vrl) throws VrsException
    {
        VResourceSystem resourceSystem = getVResourceSystemFor(vrl); 
        return resourceSystem.resolvePath(vrl);  
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

    public VRL resolvePath(String path) throws VRLSyntaxException
    {
        return currentPathVRL.resolvePath(path);
    }
    
    public void setCurrentPath(VRL vrl) throws VRLSyntaxException
    {
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
        return (InfoRootNode)openLocation(new VRL("info:/")); 
    }

    public List<VPath> openLocations(List<VRL> vrls) throws VrsException
    {
        ArrayList<VPath> paths=new ArrayList<VPath>();
        
        for (VRL vrl:vrls)
        {
            paths.add(openLocation(vrl)); 
        }
        
        return paths; 
        
    }

}
