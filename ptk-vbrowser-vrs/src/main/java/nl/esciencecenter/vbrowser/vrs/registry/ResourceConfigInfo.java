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

import nl.esciencecenter.ptk.crypt.Secret;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.object.Duplicatable;
import nl.esciencecenter.vbrowser.vrs.VRS;
import nl.esciencecenter.vbrowser.vrs.VRSProperties;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;
import nl.esciencecenter.vbrowser.vrs.data.AttributeUtil;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Resource Configuration Information for VResourceSystems or other configurable resources.
 */
public class ResourceConfigInfo implements Duplicatable<ResourceConfigInfo> {

    // ---
    // Flags/Switchess
    // ---
    public static final String NEED_USERINFO = "needUserinfo";

    public static final String NEED_SERVERPATH = "needServerPath";

    // -----------------
    // Server attributes
    // -----------------

    public static final String RESOURCE_SCHEME = "scheme";

    public static final String RESOURCE_USERINFO = "userinfo";

    public static final String RESOURCE_HOSTNAME = "hostname";

    public static final String RESOURCE_PORT = "port";

    public static final String RESOURCE_PATH = "path";

    public static final String ATTR_USER_KEY_FILES = "userKeyFiles";

    public static final String ATTR_USER_KEY_STORES = "userKeyStores";

    public static final String ATTR_AUTH_SCHEME = "authScheme";

    public static final String[] defaultConfigAttributes = {RESOURCE_SCHEME, RESOURCE_USERINFO, RESOURCE_HOSTNAME,
            RESOURCE_PORT, RESOURCE_PATH};

    public enum AuthScheme {
        NONE, PASSWORD, USER_CERTFIICATE
    }

    /**
     * Meta attribute. Contains configuration attributes.
     */
    private static final String CONFIG_ATTRIBUTENAMES = "_configAttributesNames";

    // ==================
    // Instance
    // ==================

    protected AttributeSet attributes;

    private Secret passwd = null;

    private ResourceSystemInfoRegistry infoRegistry = null;

    private String _id;

    public ResourceConfigInfo(ResourceSystemInfoRegistry registry, VRL serverVRL, String infoId) {
        this.infoRegistry = registry;
        attributes = new AttributeSet();
        setServerVRL(serverVRL);
        this._id = infoId;
    }

    protected ResourceConfigInfo(ResourceSystemInfoRegistry registry, AttributeSet attributes, String infoId) {
        this.infoRegistry = registry;
        this.attributes = attributes;
        this._id = infoId;
    }

    protected void updateId(String newId) {
        this._id = newId;
    }

    protected void setServerVRL(VRL vrl) {
        attributes.set(RESOURCE_SCHEME, vrl.getScheme());
        attributes.set(RESOURCE_HOSTNAME, vrl.getHostname());
        // must use default port;

        int port = vrl.getPort();
        if (port <= 0) {
            port = VRS.getDefaultPort(vrl.getScheme());
        }
        attributes.set(RESOURCE_PORT, port);
        attributes.set(RESOURCE_PATH, vrl.getPath());
        attributes.set(RESOURCE_USERINFO, vrl.getUserinfo());
    }

    public void store() {
        if (this.infoRegistry == null) {
            throw new NullPointerException("No Registry. Can not store ResourceSystemInfo!");
        }
        this.infoRegistry.putInfo(this);
    }

    public VRL getServerVRL() {
        String path = "/";

        if (getNeedServerPath()) {
            path = this.getServerPath();
        }

        if (getNeedUserInfo()) {
            return new VRL(getServerScheme(), getUserInfo(), getServerHostname(), getServerPort(), path);
        } else {
            return new VRL(getServerScheme(), getServerHostname(), getServerPort(), path);
        }
    }

    /**
     * Returns duplicate of properties. Changing this properties won't efffect this
     * ResourceSystemInfo Use setProperties() to update the Resource Properties.
     *
     * @return duplicate of Resource Properties.
     */
    public VRSProperties getProperties() {
        return attributes.toVRSProperties();
    }

    public void setProperty(String name, String value) {
        attributes.set(name, value);
    }

    public Attribute getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(Attribute attr) {
        attributes.put(attr);
    }

    public String getProperty(String name) {
        return attributes.getStringValue(name);
    }

    public String getServerScheme() {
        return attributes.getStringValue(RESOURCE_SCHEME);
    }

    public String getUserInfo() {
        return attributes.getStringValue(RESOURCE_USERINFO);
    }

    public void setUserInfo(String userInfo) {
        attributes.set(RESOURCE_USERINFO, userInfo);
    }

    public int getServerPort() {
        return attributes.getIntValue(RESOURCE_PORT, -1);
    }

    public String getServerHostname() {
        return attributes.getStringValue(RESOURCE_HOSTNAME);
    }

    public String getServerPath() {
        return attributes.getStringValue(RESOURCE_PATH);
    }

    public boolean getNeedUserInfo() {
        return attributes.getBooleanValue(NEED_USERINFO, false);
    }

    public boolean getNeedServerPath() {
        return attributes.getBooleanValue(NEED_SERVERPATH, false);
    }

    public void setNeedUserInfo(boolean value) {
        attributes.set(NEED_USERINFO, value);
    }

    public void setServerPath(String serverPath, boolean isMandatory) {
        attributes.set(RESOURCE_PATH, serverPath);
        attributes.set(NEED_SERVERPATH, isMandatory);
    }

    /**
     * @return actual username without optional group and/or VO information.
     */
    public String getUsername() {
        String userInfo = this.getUserInfo();
        if (userInfo == null) {
            return null;
        }
        // split "<username>[:<VO>]" parts:
        String[] parts = userInfo.split(":");
        if (parts == null)
            return null;
        return parts[0];
    }

    /**
     * Return user logical group or VO.
     *
     * @return User group, VO or null if not defined.
     */
    public String getUserVO() {
        String userInfo = this.getUserInfo();
        if (userInfo == null) {
            return null;
        }
        // split "<username>[:<VO>]" parts:
        String[] parts = userInfo.split(":");
        if (parts.length < 2) {
            return null;
        }
        return parts[1];
    }

    public Secret getPassword() {
        return passwd;
    }

    public void setPassword(Secret secret) {
        passwd = secret;
    }

    public String getID() {
        return this._id;
    }

    @Override
    public boolean shallowSupported() {
        return false;
    }

    @Override
    public ResourceConfigInfo duplicate() {
        ResourceConfigInfo info = new ResourceConfigInfo(infoRegistry, attributes.duplicate(false), _id);
        info.passwd = getPassword(); // copy ?
        return info;
    }

    @Override
    public ResourceConfigInfo duplicate(boolean shallow) {
        return duplicate();
    }

    /**
     * @return Returns backing actual AttributeSet with configuration.
     */
    protected AttributeSet attributes() {
        return this.attributes;
    }

    public AttributeSet getConfigAttributeSet() {
        StringList names = new StringList(defaultConfigAttributes);
        StringList attrs = this.getConfigAttributeNames();
        names.add(attrs, true);
        return attributes.subSet(names);
    }

    public boolean setDefaultAttribute(String name, String value, boolean editable) {
        Attribute attr = attributes.get(name);
        this.addConfigAttributeName(name);

        if (attr == null) {
            attributes.put(new Attribute(name, value, editable));
            return true;
        }

        if (editable != attr.isEditable()) {
            attr.setEditable(editable);
            attributes.put(attr);
            return true;
        }

        return false;
    }

    /**
     * Set default attribute and update editable flag. If attribute is already set, the stored value
     * will be kept as-is.
     *
     * @return true if new attribute has been set or flags has been changed, false if previous value
     * has been kept.
     */
    public boolean setDefaultAttribute(Attribute attr, boolean editable) {
        Attribute prevAttr = attributes.get(attr.getName());
        this.addConfigAttributeName(attr.getName());

        if (prevAttr == null) {
            attributes.put(attr);
            return true;
        }

        if (editable != prevAttr.isEditable()) {
            prevAttr.setEditable(editable);
            attributes.put(prevAttr);
            return true;
        }

        return false;
    }

    public StringList getConfigAttributeNames() {
        Attribute attr = this.attributes.get(CONFIG_ATTRIBUTENAMES);
        if (attr == null)
            return null;
        return attr.getStringListValue();
    }

    protected void addConfigAttributeName(String name) {
        // Auto update and populate optional meta- config attribute names property
        Attribute attr = this.attributes.get(CONFIG_ATTRIBUTENAMES);
        StringList list = null;

        if (attr == null) {
            attr = new Attribute(AttributeType.STRING, CONFIG_ATTRIBUTENAMES, null);
        } else {
            list = attr.getStringListValue();
        }
        if (list == null) {
            list = new StringList();
        }
        if (list.contains(name) == false) {
            list.add(name);
            attr.setStringListValue(list);
            this.attributes.put(attr);
        }
    }

    public void setAuthScheme(AuthScheme scheme, boolean editable) {
        attributes.set(AttributeUtil.createStringAttribute(ATTR_AUTH_SCHEME, "" + scheme, editable));
    }

    public void setAuthSchemeToNone() {
        attributes.set(ATTR_AUTH_SCHEME, "" + AuthScheme.NONE);
    }

    public void setAuthSchemeToPassword() {
        attributes.set(ATTR_AUTH_SCHEME, "" + AuthScheme.PASSWORD);
    }

    @Override
    public String toString() {
        return "ResourceSystemInfo:[id=" + _id + ",serverVrl=+" + getServerVRL() + ",password="
                + ((passwd != null) ? "<PASSWORD>" : "<No Password>") + ",properties=" + attributes + "]";
    }

}
