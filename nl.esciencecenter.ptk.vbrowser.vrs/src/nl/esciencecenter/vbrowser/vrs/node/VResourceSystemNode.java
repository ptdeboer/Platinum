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

import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public abstract class VResourceSystemNode extends VPathNode implements VResourceSystem
{
    protected VRSContext vrsContext=null;
    
    protected VResourceSystemNode(VRSContext context,VRL serverVrl)
    {
        super(null,serverVrl);  
        this.resourceSystem=this; 
        this.vrsContext=context; 
    }

    @Override
    public VRL getServerVRL()
    {
        return this.getVRL(); 
    }
    
    @Override
    public VRL resolveVRL(String path) throws VrsException
    {
        return this.getServerVRL().resolvePath(path); 
    }
    
    protected VRSContext getVRSContext()
    {
        return vrsContext; 
    }
    
    protected ResourceSystemInfo getResourceSystemInfo() throws VrsException
    {
        return vrsContext.getResourceSystemInfoFor(getVRL(), true);
    }

    @Override
    public int hashCode()
    {
        return this.getServerVRL().hashCode(); 
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (getClass() != other.getClass())
            return false;

        if ((other instanceof VResourceSystem)==false)
            return false; 
        
        VResourceSystem otherRS=(VResourceSystem)other;
        // equal Server VRL means equal.
        return this.getServerVRL().equals(otherRS.getServerVRL());
    }
 
    public String toString()
    {
        return "<VResourceSystem>[serverVrl="+getServerVRL()+"]"; 
    }
    

}
