/*
 * Copyright 2006-2010 Virtual Laboratory for e-Science (www.vl-e.nl)
 * Copyright 2012-2013 Netherlands eScience Center.
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

package nl.esciencecenter.ptk.vbrowser.viewers.loboviewer.resfs;

import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Resource file system for "res:" resources. 
 */
public class ResFS implements VResourceSystemFactory
{

    private String[] schemes =
    {
            "res"
    };

    @Override
    public String[] getSchemes()
    {
        return schemes;
    }

    @Override
    public String createResourceSystemId(VRL vrl)
    {
        return "resourcesystem-res";
    }

    @Override
    public ResourceSystemInfo updateResourceInfo(VRSContext context, ResourceSystemInfo resourceSystemInfo, VRL vrl)
    {
        return resourceSystemInfo;
    }

    @Override
    public VResourceSystem createResourceSystemFor(VRSContext context, ResourceSystemInfo info, VRL vrl) throws VrsException
    {
        return new ResResourceSystem(context, vrl);
    }

}
