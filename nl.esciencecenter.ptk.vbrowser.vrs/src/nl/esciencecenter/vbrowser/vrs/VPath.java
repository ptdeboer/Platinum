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

import java.util.List;

import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public interface VPath
{
    public VRL getVRL();

    /** 
     * Return short name or logical name. 
     * Default is basename of VRL. 
     * @return short name or logical name of this resource. 
     */
    public String getName(); 
        
    public String getResourceType() throws VrsException; 

    public VResourceSystem getResourceSystem() throws VrsException; 

    public VRL resolvePathVRL(String path) throws VrsException; 

    public VPath resolvePath(String path) throws VrsException; 
 
    /**
     * Return parent VPath of this VPath. 
     * Default implementation returns directory name.  
     * @return Logical Parent (V)Path of this VPath. 
     * @throws VrsException
     */
    public VPath getParent() throws VrsException;
    
    public String getIconURL(int size) throws VrsException; 

    public String getMimeType() throws VrsException;
    
    public String getResourceStatus() throws VrsException;

    public List<AttributeDescription> getAttributeDescriptions(); 

    public List<Attribute> getAttributes(List<String> names) throws VrsException;

    public List<String> getAttributeNames() throws VrsException;

    /** 
     * Refresh cached attributes and sync with local resources. 
     * @return true if sync is supported and sync was successful. false if not supported or not applicable.  
     * @throws VrsException 
     */
    public boolean sync() throws VrsException;  
    
    // =====================
    // Delete/Rename 
    // =====================
    
    public void delete(boolean recurse) throws VrsException; 
    
    public VPath renameTo(String nameOrPath) throws VrsException; 

    // =====================
    // VComposite Interface
    // =====================
    
    public boolean isComposite() throws VrsException;
    
    /** 
     * List of allow child resource types. These types will be use as allowed types for Create and the (Copy)Drop methods 
     * @throws VrsException
     */
    public List<String> getChildResourceTypes() throws VrsException; 

    public List<? extends VPath> list() throws VrsException;

    /** 
     * Create new resource with this path as parent. Type must be one of getChildResourceTypes(). 
     * @param type
     * @param name
     * @return
     * @throws VrsException
     */
    public VPath create(String type, String name) throws VrsException; 
    
    // public Map<String,List<Attribute>> getChildAttributes(List<String> childNames, List<String> attrNames) throws VrsException;

}
