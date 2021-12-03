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

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.mimetypes.MimeTypes;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.util.*;

import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.*;

@Slf4j
public class VPathNode implements VPath {

    static protected String[] vpathImmutableAttributeNames = {ATTR_LOCATION, //
            ATTR_SCHEME,//
            ATTR_RESOURCE_TYPE,//
            ATTR_NAME,//
            ATTR_HOSTNAME,//
            ATTR_PORT,//
            ATTR_ICONURL,//
            ATTR_PATH,//
            ATTR_MIMETYPE //
    };

    protected VRL vrl;

    protected VResourceSystem resourceSystem;

    protected VPathNode(VResourceSystem resourceSystem, VRL vrl) {
        this.vrl = vrl;
        this.resourceSystem = resourceSystem;
    }

    public VRL getVRL() {
        return vrl;
    }

    public java.net.URI getURI() {
        return vrl.toURINoException();
    }

    public VResourceSystem getResourceSystem() {
        return this.resourceSystem;
    }

    public VRL resolveVRL(String relativeUri) throws VRLSyntaxException {
        return vrl.resolvePath(relativeUri);
    }

    public String getName() {
        return getVRL().getBasename();
    }

    public boolean isComposite() throws VrsException {
        return false;
    }

    public String getIconURL(int size) throws VrsException {
        return null;
    }

    public String getMimeType() throws VrsException {
        return MimeTypes.getDefault().getMimeType(getVRL().getPath());
    }

    public String getResourceStatus() {
        return null;
    }


    public Map<String, AttributeDescription> getAttributeDescriptions(String[] names) throws VrsException {
        Map<String, AttributeDescription> all = this.getAttributeDescriptions();

        Map<String, AttributeDescription> filtered = new LinkedHashMap<>();
        for (String name : names) {
            filtered.put(name, all.get(name));
        }
        return filtered;
    }

    public Map<String, AttributeDescription> getAttributeDescriptions() throws VrsException {
        Map<String, AttributeDescription> set1 = getImmutableAttributeDescriptions();
        List<AttributeDescription> set2 = getResourceAttributeDescriptions();
        if (set2 != null) {
            for (AttributeDescription attr : set2) {
                set1.put(attr.getName(), attr);
            }
        }
        return set1;
    }

    /**
     * Default Immutable Attributes. Typically these are location derived attributes, like
     * scheme,port,hostname,etc. ResourceType is also immutable, but mime-type isn't as the content
     * of a file may change.
     *
     * @return List containing the Immutable Attributes.
     */
    public Map<String, AttributeDescription> getImmutableAttributeDescriptions() {
        LinkedHashMap<String, AttributeDescription> list = new LinkedHashMap<String, AttributeDescription>();
        for (String name : vpathImmutableAttributeNames) {
            list.put(name, new AttributeDescription(name, AttributeType.STRING, false, null));
        }
        return list;
    }

    public List<AttributeDescription> getResourceAttributeDescriptions() throws VrsException {
        return null;
    }

    /**
     * Final getAttributeNames, override getAttributeDiscriptions for actual Attribute Definitions.
     *
     * @throws VrsException
     */
    final public List<String> getAttributeNames() throws VrsException {
        Map<String, AttributeDescription> list = getAttributeDescriptions();
        if (list == null) {
            return null;
        }
        Iterator<String> iterator = list.keySet().iterator();
        StringList names = new StringList();
        while (iterator.hasNext()) {
            names.add(iterator.next());
        }
        return names;
    }

    @Override
    public List<Attribute> getAttributes(String[] names) throws VrsException {
        ArrayList<Attribute> list = new ArrayList<Attribute>();
        for (String name : names) {
            Attribute attr = getAttribute(name);
            if (attr != null) {
                list.add(attr);
            } else {
                log.warn("Attribute not defined:{}", name);
            }
        }
        return list;
    }

    final public Attribute getAttribute(String name) throws VrsException {
        Attribute attr = getImmutableAttribute(name);

        if (attr != null) {
            return attr;
        } else {
            return getResourceAttribute(name);
        }
    }

    /**
     * Implement this for Resource Specific Attributes.
     */
    public Attribute getResourceAttribute(String name) throws VrsException {
        return null;
    }

    /**
     * Get Immutable attribute. Typically these are location derived attributes like
     * Scheme,Host,Port,etc. and do not change during the lifetime of this object.
     *
     * @param name - immutable attribute name
     * @return Attribute or null if attribute isn't defined.
     */
    public Attribute getImmutableAttribute(String name) throws VrsException {
        // by prefix values with "", a NULL value will be convert to "NULL".
        if (name.compareTo(ATTR_RESOURCE_TYPE) == 0)
            return new Attribute(name, getResourceType());
        else if (name.compareTo(ATTR_LOCATION) == 0)
            return new Attribute(name, getVRL());
        else if (name.compareTo(ATTR_NAME) == 0)
            return new Attribute(name, getName());
        else if (name.compareTo(ATTR_SCHEME) == 0)
            return new Attribute(name, vrl.getScheme());
        else if (name.compareTo(ATTR_HOSTNAME) == 0)
            return new Attribute(name, vrl.getHostname());
        else if ((name.compareTo(ATTR_PORT) == 0))
            return new Attribute(name, vrl.getPort());
        else if (name.compareTo(ATTR_ICONURL) == 0)
            return new Attribute(name, getIconURL(16));
        else if (name.compareTo(ATTR_PATH) == 0)
            return new Attribute(name, vrl.getPath());
        else if ((name.compareTo(ATTR_URI_QUERY) == 0) && getVRL().hasQuery())
            return new Attribute(name, vrl.getQuery());
        else if ((name.compareTo(ATTR_URI_FRAGMENT) == 0) && getVRL().hasFragment())
            return new Attribute(name, getVRL().getFragment());
        else if (name.compareTo(ATTR_NAME) == 0)
            return new Attribute(name, getName());
        else if (name.compareTo(ATTR_LOCATION) == 0)
            return new Attribute(name, getVRL());
        else if (name.compareTo(ATTR_MIMETYPE) == 0)
            return new Attribute(name, getMimeType());

        return null;
    }

    @Override
    public String getResourceType() throws VrsException {
        return "<VPath:?>";
    }

    @Override
    public VPath resolve(String path) throws VrsException {
        // Since a filesystem extends PathNode itself, check for cycle.
        if (this.resourceSystem == this) {
            throw new Error(
                    "Internal Error: resolvePath(): Cannot delegate resolvePath to resourceSystem as I *am* the ResourceSystem:"
                            + this);
        }

        return this.resourceSystem.resolve(path);
    }

    @Override
    public VPath getParent() throws VrsException {
        if (vrl.isRootPath()) {
            // break infinite parent of parent loop.  
            return null;
        }

        String parentPath = this.vrl.getDirname();
        return resourceSystem.resolve(parentPath);
    }

    @Override
    public List<? extends VPath> list() throws VrsException {
        return null;
    }

    @Override
    public List<String> getChildResourceTypes() throws VrsException {
        return null;
    }

    @Override
    public VPath create(String type, String name) throws VrsException {
        throw new VrsException("Can not create new '" + type + "' node named:" + name);
    }

    @Override
    public boolean sync() throws VrsException {
        return false;
    }

}
