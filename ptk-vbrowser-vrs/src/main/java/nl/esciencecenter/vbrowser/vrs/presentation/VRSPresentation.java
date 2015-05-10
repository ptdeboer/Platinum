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
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.vbrowser.vrs.VRSTypes;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Factory class for (VRS) Presentation of VRS Nodes.
 */
public class VRSPresentation extends Presentation {

    public static String defaultVFSAttributeNames[] = { ATTR_ICON,//
            ATTR_RESOURCE_TYPE,//
            ATTR_NAME,//
            ATTR_FILE_SIZE,//
            // ATTR_MODIFICATION_TIME_STRING,
            ATTR_MODIFICATION_TIME,//
            ATTR_MIMETYPE,//
            ATTR_PERMISSIONSTRING,//
    // ATTR_ISHIDDEN,
    // ATTR_ISLINK
    // VFS.ATTR_ISFILE,
    // VFS.ATTR_ISDIR
    };

    /**
     * Default Attributes to show for VPathNode
     */
    public static String defaultVPathAttributeNames[] = { ATTR_ICON, ATTR_RESOURCE_TYPE, ATTR_NAME,
            // ATTR_LENGTH,
            ATTR_MIMETYPE };

    public static Presentation createDefault() {
        Presentation pres = new Presentation();
        initDefaults(pres);
        return pres;
    }

    public static void initDefaults(Presentation pres) {
        pres.setAttributePreferredWidths(ATTR_ICON, 32, 32, 999);
        // pres.setAttributePreferredWidth(ATTR_INDEX, 32);
        pres.setAttributePreferredWidths(ATTR_NAME, 48, 200, 999);
        pres.setAttributePreferredWidths(ATTR_RESOURCE_TYPE, 64, 140, 999);
        pres.setAttributePreferredWidths(ATTR_MIMETYPE, 100, 160, 999);
        pres.setAttributePreferredWidths(ATTR_SCHEME, 64, 100, 999);
        pres.setAttributePreferredWidths(ATTR_HOSTNAME, 64, 140, 999);
        // pres.setAttributePreferredWidth(ATTR_PORT, 32);
        pres.setAttributePreferredWidths(ATTR_FILE_SIZE, 60, 70, 999);
        pres.setAttributePreferredWidths(ATTR_PATH, 100, 200, 999);
        pres.setAttributePreferredWidths(ATTR_RESOURCE_STATUS, 48, 64, 999);
        pres.setAttributePreferredWidths(ATTR_MODIFICATION_TIME, 100, 120, 999);
        pres.setAttributePreferredWidths(ATTR_CREATION_TIME, 100, 120, 999);
        // VQueues and VJobs:

        pres.setPreferredContentAttributeNames(new StringList(defaultVPathAttributeNames));
    }

    /**
     * Master presention for scheme and type only, for example "file" scheme and "Dir" type.
     * 
     * @param scheme
     *            - scheme name for example "file" or "http"
     * @param resourceType
     *            -
     * @param autocreate
     *            - whether to autocreate the Presentation Object.
     * @return Master or root Presentation
     */
    public static Presentation getMasterPresentationFor(String scheme, String resourceType, boolean autocreate) {
        return getPresentationFor(resourceType + ":" + scheme, resourceType, autocreate);
    }

    /**
     * Master presention for scheme and type only, for example "file" scheme and "Dir" type.
     * 
     * @param scheme
     *            - scheme name for example "file" or "http"
     * @param resourceType
     *            - VRS ResourceType for example "File" or "Dir".
     * @param presentation
     *            - actual Presentation.
     */
    public static void storeMasterPresentation(String scheme, String resourceType, Presentation presentation) {
        Presentation.storePresentation(resourceType + ":" + scheme, presentation);
    }

    /**
     * Checks the PresentationStore if there is already Presentation information stored. If no
     * presentation can be found and autocreate==true, a default Presentation object will be
     * created.
     * 
     * @param VRL
     *            - vrl
     * 
     * @param autocreate
     *            - whether to initialize a default Presentation object
     * @return
     */
    public static Presentation getPresentationFor(VRL vrl, String resourceType, boolean autoCreate) {
        // Check VRL bound presentation: 
        Presentation pres = getPresentationFor(createID(vrl, resourceType), resourceType, false);

        if (pres != null) {
            return pres;
        }

        if (autoCreate == false) {
            return null;
        }

        // Copy from master presentation.  
        return getMasterPresentationFor(vrl.getScheme(), resourceType, true);
    }

    protected static Presentation getPresentationFor(String key, String resourceType, boolean autoCreate) {
        Presentation pres = Presentation.getPresentation(key, false);

        if (pres != null) {
            return pres;
        }

        if (autoCreate == false) {
            return null;
        }

        pres = createDefault();

        //
        // Set defaults:
        //

        if (resourceType.compareTo(VRSTypes.DIR_TYPE) == 0) {
            pres.setPreferredContentAttributeNames(new StringList(VRSPresentation.defaultVFSAttributeNames));
        } else if (resourceType.compareTo(VRSTypes.FILE_TYPE) == 0) {
            pres.setPreferredContentAttributeNames(new StringList(VRSPresentation.defaultVFSAttributeNames));
        } else {
            pres.setPreferredContentAttributeNames(new StringList(VRSPresentation.defaultVPathAttributeNames));
        }

        pres.setIconAttributeName(ATTR_ICON);

        return pres;
    }

    private static String createID(VRL vrl, String resourceType) {
        String scheme = vrl.getScheme();
        String hostname = vrl.getHostname();
        String path = vrl.getPath();
        int port = vrl.getPort();
        String portStr = "" + port;

        if (StringUtil.isEmpty(scheme)) {
            throw new NullPointerException("Presentation canot have an empty scheme!");
        }

        if (port <= 0) {
            portStr = "";
        }

        if (StringUtil.isEmpty(hostname)) {
            hostname = "";
        }

        if (resourceType == null) {
            resourceType = "";
        } else {
            resourceType = "(" + resourceType + ")";
        }

        String id = scheme + ":" + resourceType + "@" + hostname + ":" + portStr + path;

        return id;
    }

    public static void storePresentation(VRL vrl, String resourceType, Presentation presentation) {
        Presentation.storePresentation(createID(vrl, resourceType), presentation);
    }

    // static class 

    private VRSPresentation() {
    }

}
