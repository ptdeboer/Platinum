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

import nl.esciencecenter.ptk.net.URIFactory;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.node.VResourceSystemNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Info Resource System. Browse logical resources and (remote) URLs.
 */
public class InfoRS extends VResourceSystemNode // implements VStreamCreator
{

    public static VRL createPathVRL(String path) {
        path = URIFactory.uripath("/" + path);
        return new VRL("info", null, 0, path);
    }

    private InfoRootNode rootNode = null;

    private VRSClient vrsClient;

    public InfoRS(VRSContext context) throws VrsException {
        super(context, new VRL("info:/"));
        vrsClient = new VRSClient(context);
    }

    protected InfoRootNode getRootNode() throws VrsException {
        if (rootNode == null) {
            initRootNode();
        }

        return rootNode;
    }

    protected void initRootNode() throws VrsException {
        rootNode = new InfoRootNode(this);
    }

    @Override
    public InfoRSPathNode resolvePath(VRL vrl) throws VrsException {
        if (!vrl.getScheme().equals("info")) {
            throw new VrsException("Can only handle 'info:' nodes:" + vrl);
        }

        String paths[] = vrl.getPathElements();

        int n = 0;
        if (paths != null)
            n = paths.length;

        InfoRootNode root = getRootNode();

        if (n == 0 || StringUtil.equals(vrl.getPath(), null, "", "/")) {
            return root;
        }

        return root.findNode(vrl);
    }

    protected VRSClient getVRSClient() {
        return vrsClient;
    }

    public VRSContext getVRSContext() {
        return vrsClient.getVRSContext();
    }

}
