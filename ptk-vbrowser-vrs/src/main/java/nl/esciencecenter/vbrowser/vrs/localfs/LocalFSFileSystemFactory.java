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

import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class LocalFSFileSystemFactory implements VResourceSystemFactory
{
    public LocalFSFileSystemFactory() throws VrsException
    {
    }
    
    @Override
    
    public String[] getSchemes()
    {
        return new String[]{"file"}; 
    }

    @Override
    public String createResourceSystemId(VRL vrl)
    {
        // only one local fs. 
        return "localfs:0";  
    }

    @Override
    public VResourceSystem createResourceSystemFor(VRSContext context,ResourceConfigInfo info,VRL vrl) throws VrsException
    {
        if ("file".equals(vrl.getScheme())==false)
        {
            throw new VrsException("Only support local file system URI:"+vrl);
        }
        
        return new LocalFileSystem(context);
    }

    @Override
    public ResourceConfigInfo updateResourceInfo(VRSContext context,ResourceConfigInfo resourceSystemInfo, VRL vrl)
    {
        // Nothing to be updated. 
        return resourceSystemInfo; 
    }

}
