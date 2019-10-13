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

import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.util.List;
import java.util.Map;

/**
 * Resource Abstraction.<br>
 * A VPath typically is an URL, Virtual File, Virtual Directory or other Resource which can be
 * accessable by an URI. It has an Virtual Resource Locator which is an URI compatible locator.
 *
 * @see VRL
 * @see java.net.URI
 * @see VFSPath
 */
public interface VPath {

    /**
     * The Virtual Resource Locator (VRL) of this Path. This is an URI compatible path.
     *
     * @return
     * @see VRL
     * @see java.net.URI
     */
    VRL getVRL();

    /**
     * Return short name or logical name. Default name is the basename of the VRL.
     *
     * @return short name or logical name of this resource.
     */
    String getName();

    /**
     * @return ResourceType of this resource, for example "File" or "Dir".
     */
    String getResourceType() throws VrsException;

    /**
     * @return ResourceSystem of this VPath. This is the resource factory interface. For a VFSPath
     * this a VFileSystem.
     * @see VFileSystem
     */
    VResourceSystem getResourceSystem() throws VrsException;

    /**
     * Resolve relative string against this VPath and return normalized and absolute VRL.
     */
    VRL resolveVRL(String path) throws VrsException;

    /**
     * Resolve relative string against this VPath and return absolute and normalized VPath.
     */
    VPath resolve(String path) throws VrsException;

    /**
     * Return parent VPath of this VPath. Default implementation returns directory name. Logical
     * nodes might return alternative VPaths.
     *
     * @return Logical Parent (V)Path of this VPath.
     * @throws VrsException
     */
    VPath getParent() throws VrsException;

    /**
     * @param size - indication of the size of the Icon. Actual width and height of icon should be
     *             equal or greater then the given 'size'.
     * @return absolute or relative iconURL of icon.
     * @throws VrsException
     */
    String getIconURL(int size) throws VrsException;

    /**
     * @return mime-type of this resource if applicable. For example "text/html".
     * @throws VrsException
     */
    String getMimeType() throws VrsException;

    /**
     * @return The state of this resource if applicable.
     * @throws VrsException
     */
    String getResourceStatus() throws VrsException;

    /**
     * @return Description of all Attributes this resource has.
     * @throws VrsException
     */
    Map<String, AttributeDescription> getAttributeDescriptions(String[] attrNames) throws VrsException;

    /**
     * Return list of attributes preferably in the same order as given in the array. If attribute
     * are not supported they may be omitted in the returned list.
     *
     * @param attributeNames - names of the attributes
     * @return Attribute List.
     * @throws VrsException
     */
    List<Attribute> getAttributes(String[] attributeNames) throws VrsException;

    /**
     * @return list of attribute names this resource supports.
     * @throws VrsException
     */
    List<String> getAttributeNames() throws VrsException;

    /**
     * Flush unwritten changes, refresh cached attributes and sync with local resources.
     *
     * @return true if sync is supported and sync was successful. false if not supported or not
     * applicable.
     * @throws VrsException if sync() is supported but the sync() itself failed.
     */
    boolean sync() throws VrsException;

    // =====================
    // VComposite Interface
    // =====================

    /**
     * @return true if this resource path can have sub paths.
     * @throws VrsException
     */
    boolean isComposite() throws VrsException;

    /**
     * List of allowed child resource types. These types will be used as allowed types for Create
     * and the (Copy)Drop methods
     *
     * @throws VrsException
     */
    List<String> getChildResourceTypes() throws VrsException;

    /**
     * List unfiltered child nodes of this resource.<br>
     * Implementation note: do not sort the list but return the order as-is including hidden
     * resources.
     */
    List<? extends VPath> list() throws VrsException;

    /**
     * Create new resource with this VPath as parent. Type must be one of getChildResourceTypes().
     *
     * @param type - one of getChildResourceTypes() to be created, for example "Dir" or "File".
     * @param name - logical name.
     * @return - new created VPath.
     * @throws VrsException
     */
    VPath create(String type, String name) throws VrsException;

}
