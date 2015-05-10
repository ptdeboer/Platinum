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

package nl.esciencecenter.vbrowser.vrs.infors;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.vbrowser.vrs.VRSTypes;

public class InfoRSConstants {

    public static final String INFO_SCHEME = "info";

    public static final String LOCALSYSTEM = "LocalSystem";

    public static final String SYSTEMINFOS_NODE = "ResourceSystemInfosNode";

    public static final String INFOSYSTEMROOTNODE = "InfoSystemRoot";

    public static final String INFOCONFIGNODE = "InfoConfig";

    public static final String RESOURCELINK = "ResourceLink";

    public static final String RESOURCEFOLDER = "ResourceFolder";

    public static final String RESOURCEINFO_CONFIG = "ResourceInfoConfig";

    public static final String RESOURCE_ICONURL = "resourceIconURL";

    public static final String RESOURCE_SHOWLINKICON = "resourceShowLinkIcon";

    public static final String RESOURCE_MIMETYPE = "resourceMimeType";

    public static final String RESOURCE_NAME = "resourceName";

    public static final String RESOURCE_TARGETVRL = "resourceTargetVRL";

    public static final String RESOURCE_TARGET_ISCOMPOSITE = "resourceTargateIsComposite";

    public static final String LOCALSYSTEM_OSNAME = "system." + GlobalProperties.PROP_JAVA_OS_NAME;

    public static final String LOCALSYSTEM_OSVERSION = "system." + GlobalProperties.PROP_JAVA_OS_VERSION;

    public static final String LOCALSYSTEM_OSARCH = "system." + GlobalProperties.PROP_JAVA_OS_ARCH;

    public static final String LOCALSYSTEM_HOMEDIR = "system.user.home";

    public static final String LOCALSYSTEM_JAVAHOME = "system.java.home";

    public static final String LOCALSYSTEM_JAVAVERSION = "system.java.version";

    public static final String RESOURCELINK_MIMETYPE = VRSTypes.VBROWSER_VRS_MIMETYPE_PREFIX + "-" + RESOURCELINK;

    public static final String RESOURCEFOLDER_MIMETYPE = VRSTypes.VBROWSER_VRS_MIMETYPE_PREFIX + "-" + RESOURCEFOLDER;

}
