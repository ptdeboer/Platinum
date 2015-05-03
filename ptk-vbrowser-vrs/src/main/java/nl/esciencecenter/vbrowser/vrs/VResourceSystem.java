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

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/** 
 * Factory interface for VPaths. 
 */
public interface VResourceSystem
{
    public VRSContext getVRSContext();
    
    /**
     *  Server location also counts as unique identigyin ID. 
     *  Equivalent ResourceSystems should have similar Server VRL().  
     * @return server VRL. 
     */
    public VRL getServerVRL();
    
    /**
     * Resolve relative path against this ResourceSystem. 
     * @param path relative path 
     * @return absolute VRL 
     * @throws VrsException if path contains invalid characters. 
     */
    public VRL resolveVRL(String path) throws VrsException; 
    
    /** 
     * Resolve relative path and return VPath. 
     * @param path relative path 
     * @return resolve VPath 
     * @throws VrsException if path contains invalid characters. 
     */
    public VPath resolvePath(String path) throws VrsException; 
    
    /** 
     * Resolve relative or absolute VRL to VPath. 
     * @param vrl relative or absolute VRL 
     * @return resolve VPath 
     * @throws VrsException if VRL contains an invalid path. 
     */
    public VPath resolvePath(VRL vrl) throws VrsException;
    
}
