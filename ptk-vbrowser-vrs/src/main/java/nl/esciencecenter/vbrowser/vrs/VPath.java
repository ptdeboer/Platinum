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
import java.util.Map;

import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Resource Abstraction.<br>
 * A VPath points to a URI, File of other resource which can be access by an URI.
 * 
 * @see VRL
 * @see java.net.URI
 * @see VFSPath
 */
public interface VPath {

    /**
     * The Virtual Resource Locator of this Path. Is an URI Compatible path.
     * 
     * @see VRL
     * @see java.net.URI
     * @return
     */
    public VRL getVRL();

    /**
     * Return short name or logical name. Default is basename of VRL.
     * 
     * @return short name or logical name of this resource.
     */
    public String getName();

    /**
     * @return ResourceType of this resource, for example "File" or "Dir".
     */
    public String getResourceType() throws VrsException;

    /**
     * @return ResourceSystem of this VPath. This is the resource factory interface. For a File this
     *         a VFileSystem.
     * @see VFileSystem
     */
    public VResourceSystem getResourceSystem() throws VrsException;

    /**
     * Resolve relative string against this VPath and return normalized and absolute VRL.
     */
    public VRL resolvePathVRL(String path) throws VrsException;

    /**
     * Resolve relative string against this VPath and return absolute and normalized VPath.
     */
    public VPath resolvePath(String path) throws VrsException;

    /**
     * Return parent VPath of this VPath. Default implementation returns directory name. Logical
     * nodes might return alternative VPaths.
     * 
     * @return Logical Parent (V)Path of this VPath.
     * @throws VrsException
     */
    public VPath getParent() throws VrsException;

    /**
     * @param size
     *            - indication of the size of the Icon. Actual width and height of icon should be
     *            equal or greater then the given 'size'.
     * @return actual iconURL of icon to show.
     * @throws VrsException
     */
    public String getIconURL(int size) throws VrsException;

    public String getMimeType() throws VrsException;

    public String getResourceStatus() throws VrsException;

    public Map<String, AttributeDescription> getAttributeDescriptions() throws VrsException;

    public List<Attribute> getAttributes(String names[]) throws VrsException;

    public List<String> getAttributeNames() throws VrsException;

    /**
     * Flush unwritten changes, refresh cached attributes and sync with local resources.
     * 
     * @return true if sync is supported and sync was successful. false if not supported or not
     *         applicable.
     * @throws VrsException
     *             if sync() is supported but the sync() itself failed.
     */
    public boolean sync() throws VrsException;

    // =====================
    // VComposite Interface
    // =====================

    public boolean isComposite() throws VrsException;

    /**
     * List of allow child resource types. These types will be use as allowed types for Create and
     * the (Copy)Drop methods
     * 
     * @throws VrsException
     */
    public List<String> getChildResourceTypes() throws VrsException;

    /**
     * List unfiltered child nodes of this resource. Preferably do not sort the list but return the
     * order as-is.
     */
    public List<? extends VPath> list() throws VrsException;

    /**
     * Create new resource with this VPath as parent. Type must be one of getChildResourceTypes().
     * 
     * @param type
     *            - one of getChildResourceTypes() to be created.
     * @param name
     *            - logical name.
     * @return - new created VPath.
     * @throws VrsException
     */
    public VPath create(String type, String name) throws VrsException;

    // public Map<String,List<Attribute>> getChildAttributes(List<String> childNames, List<String> attrNames) throws
    // VrsException;

}
