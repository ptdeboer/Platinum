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

/**
 * Known VRS and VFSTypes.
 */
public class VRSTypes {

    public final static String FILE_TYPE = "File";

    public final static String DIR_TYPE = "Dir";

    public final static String VLINK_TYPE = "VLink";

    public static final String FILESYSTEM_TYPE = "FileSystem";;

    public static final String RESOURCESYSTEM_TYPE = "ResourceSystem";;

    /**
     * Default MimeType prefix for all VRS Resources, if no content type specified.
     */
    public static final String VBROWSER_VRS_MIMETYPE_PREFIX = "application/vbrowser-vrs";

    /**
     * Default MimeType prefix for all VFS Resources, if no content type specified.
     */
    public static final String VBROWSER_VFS_MIMETYPE_PREFIX = "application/vbrowser-vfs";
}
