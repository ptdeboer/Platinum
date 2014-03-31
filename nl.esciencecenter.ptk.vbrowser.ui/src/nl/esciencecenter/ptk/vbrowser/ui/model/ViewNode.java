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

package nl.esciencecenter.ptk.vbrowser.ui.model;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * An ViewNode holds the UI state of Viewed resource, like icons and presentation attributes.<br>
 * This is the UI component which is actually 'viewed'. Multiple ViewNodes can be "viewing" a single resource
 * (ProxyNode). See ProxyNode for resource attributes.
 */
public class ViewNode // candidate: implements Serializable
{
    public static final String DEFAULT_ICON = "defaultIcon";

    public static final String FOCUS_ICON = "focusIcon";

    public static final String SELECTED_ICON = "selectedIcon";

    public static final String SELECTED_FOCUS_ICON = "selectedFocusIcon";

    public static VRL[] toVRLs(ViewNode[] selections)
    {
        VRL vrls[] = new VRL[selections.length];
        for (int i = 0; i < selections.length; i++)
        {
            vrls[i] = selections[i].getVRL();
        }
        return vrls;
    }

    // ==========
    // Instance
    // ==========

    /**
     * Atomic Locator which may never change during the lifetime of this object.
     */
    protected final VRL locator;

    protected final boolean isComposite;

    protected String name;

    protected Map<String, Icon> iconMapping = new Hashtable<String, Icon>();

    protected String resourceType;

    protected String resourceStatus;

    protected String mimeType;

    protected StringList allowedChildTypes;

    public ViewNode(VRL locator, Icon icon, String name, boolean isComposite)
    {
        this.locator = locator;
        initIcons(icon);
        this.name = name;
        this.isComposite = isComposite;
    }

    private void initIcons(Icon defaultIcon)
    {
        this.iconMapping.clear();
        this.iconMapping.put(DEFAULT_ICON, defaultIcon);
    }

    public VRL getVRL()
    {
        return locator;
    }

    public boolean matches(VRL vrl)
    {
        return locator.equals(vrl);
    }

    public boolean isComposite()
    {
        return isComposite;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String newName)
    {
        name = newName;
    }

    public void setResourceType(String resourceType)
    {
        this.resourceType = resourceType;
    }

    public Icon getIcon()
    {
        return getIcon(DEFAULT_ICON);
    }

    /**
     * Pre-rendered selected icon
     */
    public Icon getSelectedIcon()
    {
        return getIcon(SELECTED_ICON);
    }

    /**
     * Returns status icon if specified
     */
    public Icon getIcon(String name)
    {
        Icon icon = iconMapping.get(name);
        if (icon != null)
        {
            return icon;
        }

        icon = iconMapping.get(DEFAULT_ICON);
        return icon;
    }

    public void setIcon(String name, Icon icon)
    {
        iconMapping.put(name, icon);
    }

    public boolean isBusy()
    {
        return false; // is Busy should be updated using events
    }

    public String getResourceType()
    {
        return this.resourceType;
    }

    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public void setResourceStatus(String newStatus)
    {
        this.resourceStatus = newStatus;
    }

    public String getResourceStatus()
    {
        return resourceStatus;
    }

    public void setChildTypes(List<String> childTypes)
    {
        this.allowedChildTypes = new StringList(childTypes);
    }

    public List<String> getAllowedChildTypes()
    {
        return this.allowedChildTypes;
    }

    // ===
    // Generated Methods
    // ===

    @Override
    public String toString()
    {
        return "ViewNode [locator=" + locator + ", isComposite=" + isComposite
                + ", name=" + name + ", resourceType=" + resourceType
                + ", resourceStatus=" + resourceStatus + ", mimeType=" + mimeType + ", allowedChildTypes="
                + allowedChildTypes + "]";
    }

}
