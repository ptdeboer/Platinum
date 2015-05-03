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

package nl.esciencecenter.vbrowser.vrs.registry;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.vbrowser.vrs.VRSContext;

public class ResourceSystemInfoRegistry
{
    /** 
     * Owner Object of this registry.  
     */ 
    @SuppressWarnings("unused")
    private VRSContext vrsContext; 
    
    private Map<String,ResourceConfigInfo> resourceInfos=new Hashtable<String,ResourceConfigInfo>(); 
    
    public ResourceSystemInfoRegistry(VRSContext vrsContext)
    {
        this.vrsContext=vrsContext;
    }
    
    public ResourceConfigInfo putInfo(ResourceConfigInfo info)
    {
        synchronized(resourceInfos)
        {
            // always update ID. 
            resourceInfos.put(info.getID(),info);
            return info;
        }
    }
    
    public ResourceConfigInfo getInfo(String id)
    {
        synchronized(resourceInfos)
        {
            return resourceInfos.get(id);
        }
    }

    /** 
     * Returns a copy of the ResourceSystemInfos as list. 
     */
    public List<ResourceConfigInfo> list()
    {
        return new ExtendedList<ResourceConfigInfo>(resourceInfos.values());
    }
    
}
