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

package nl.esciencecenter.vbrowser.vrs.presentation;

import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_CREATION_TIME;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_FILE_SIZE;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_HOSTNAME;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_ICON;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_MIMETYPE;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_MODIFICATION_TIME;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_NAME;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_PATH;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_PERMISSIONSTRING;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_RESOURCE_STATUS;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_RESOURCE_TYPE;
import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_SCHEME;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.vbrowser.vrs.VRSTypes;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Factory class for UIPresentation of VRS Nodes.
 */
public class VRSPresentation
{
    public static String defaultVFSAttributeNames[] =
    {
            ATTR_ICON,
            ATTR_RESOURCE_TYPE,
            ATTR_NAME,
            ATTR_FILE_SIZE,
            // ATTR_MODIFICATION_TIME_STRING,
            ATTR_MODIFICATION_TIME,
            ATTR_MIMETYPE,
            ATTR_PERMISSIONSTRING,
            // ATTR_ISHIDDEN,
            // ATTR_ISLINK
            // VFS.ATTR_ISFILE,
            // VFS.ATTR_ISDIR
    };

    /**
     * Default Attributes to show for VPathNode
     */
    public static String defaultVPathAttributeNames[] =
    {
            ATTR_ICON,
            ATTR_RESOURCE_TYPE,
            ATTR_NAME,
            // ATTR_LENGTH,
            ATTR_MIMETYPE
    };

    /**
     * @see getPresentationFor(String, String, String, boolean)
     */
    public static Presentation getPresentationFor(String scheme, String host, String type)
    {
        return getPresentationFor(scheme, host, type, true);
    }

    /**
     * @see getPresentationFor(String, String, String, boolean)
     */
    public static Presentation getPresentationFor(VRL vrl, String type, boolean autocreate)
    {
        return getPresentationFor(vrl.getScheme(), vrl.getHostname(), type, autocreate);
    }

    /**
     * Checks the PresentationStore if there is already Presentation information
     * stored. If no presentation can be found and autocreate==true, a default
     * Presentation object will be created.
     * 
     * @param scheme
     *            scheme of resource
     * @param host
     *            hostname of resource
     * @param type
     *            VRS type of resource
     * @param autocreate
     *            whether to initialize a default Presentation object
     * @return
     */
    public static Presentation getPresentationFor(String scheme, String host, String type, boolean autocreate)
    {
        String id = createID(scheme, host, type);
        Presentation pres = null;

        pres = Presentation.getPresentation(id, false);
        if (pres != null)
            return pres;

        if (autocreate == false)
            return null;

        pres = createDefault();

        //
        // Set defaults:
        //

        if (type.compareTo(VRSTypes.DIR_TYPE) == 0)
        {
            pres.setChildAttributeNames(new StringList(VRSPresentation.defaultVFSAttributeNames));
        }
        else
        {
            pres.setChildAttributeNames(new StringList(VRSPresentation.defaultVPathAttributeNames));
        }

        pres.setIconAttributeName(ATTR_ICON);

        Presentation.storePresentation(id, pres);

        return pres;
    }

    private static String createID(String scheme, String host, String type)
    {
        if (scheme == null)
            throw new NullPointerException("Presentation must have at least a scheme");

        if (host == null)
            host = "";

        if (type == null)
            type = "";

        String id = scheme + "-" + host + "-" + type;
        return id;
    }

    public static Presentation createDefault()
    {
        Presentation pres = new Presentation();
        initDefaults(pres);
        return pres;
    }

    public static void initDefaults(Presentation pres)
    {
        pres.setAttributePreferredWidth(ATTR_ICON, 32);
        // pres.setAttributePreferredWidth(ATTR_INDEX, 32);
        pres.setAttributePreferredWidth(ATTR_NAME, 200);
        pres.setAttributePreferredWidth(ATTR_RESOURCE_TYPE, 140);
        pres.setAttributePreferredWidth(ATTR_SCHEME, 60);
        pres.setAttributePreferredWidth(ATTR_HOSTNAME, 140);
        // pres.setAttributePreferredWidth(ATTR_PORT, 32);
        pres.setAttributePreferredWidth(ATTR_FILE_SIZE, 70);
        pres.setAttributePreferredWidth(ATTR_PATH, 200);
        pres.setAttributePreferredWidth(ATTR_RESOURCE_STATUS, 48);
        pres.setAttributePreferredWidth(ATTR_MODIFICATION_TIME, 120);
        pres.setAttributePreferredWidth(ATTR_CREATION_TIME, 120);
        // VQueues and VJobs:

        pres.setChildAttributeNames(new StringList(defaultVPathAttributeNames));
    }

}
