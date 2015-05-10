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
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public interface VResourceSystemFactory {

    /**
     * @return supported schemes.
     */
    public String[] getSchemes();

    /**
     * Create unique resource system ID. <br>
     * Equivalent ResourceSystems, for example FileSystems on the same server, should have an equal
     * resource system ID. A file system might return a different ID per mounted file system or
     * local drive.
     * 
     * @param vrl
     *            - VRL to deduce resource system from.
     * @return - unique resource system ID for this ID.
     */
    public String createResourceSystemId(VRL vrl);

    public ResourceConfigInfo updateResourceInfo(VRSContext context, ResourceConfigInfo resourceSystemInfo, VRL vrl);

    /**
     * Create new resource System. This system will be cached and re-used by the VRS if the
     * ResourceSystemID matches.
     */
    public VResourceSystem createResourceSystemFor(VRSContext context, ResourceConfigInfo info, VRL vrl)
            throws VrsException;

}
