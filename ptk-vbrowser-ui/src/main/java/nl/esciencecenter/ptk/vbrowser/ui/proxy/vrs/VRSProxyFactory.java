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

package nl.esciencecenter.ptk.vbrowser.ui.proxy.vrs;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactory;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.io.copy.VRSCopyManager;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.util.ArrayList;
import java.util.List;

/**
 * VRS Proxy Factory for VRSProxyNodes. <br>
 * Bindings between ProxyNode and the VRS.
 */
@Slf4j
public class VRSProxyFactory extends ProxyFactory {


    public static VRSProxyFactory createFor(BrowserPlatform platform) {
        return new VRSProxyFactory(platform);
    }

    // ========================================================================
    // 
    // ========================================================================

    private final VRSClient vrsClient;

    private final VRSCopyManager transferManager;

    protected VRSProxyNodeDnDHandler proxyDnDHandler = null;

    protected VRSProxyFactory(BrowserPlatform platform) {
        super(platform);

        VRSContext vrsContext = platform.getVRSContext();
        this.vrsClient = new VRSClient(vrsContext);
        this.transferManager = vrsClient.getVRSTransferManager();
        this.proxyDnDHandler = new VRSProxyNodeDnDHandler(this, transferManager);
    }

    public VRSContext getVRSContext() {
        return vrsClient.getVRSContext();
    }

    public VRSClient getVRSClient() {
        return this.vrsClient;
    }

    public VRSCopyManager getTransferManager() {
        return transferManager;
    }

    public VRSProxyNode _openLocation(VRL vrl) throws ProxyException {
        try {
            return (VRSProxyNode) openLocation(vrl);
        } catch (Exception e) {
            throw new ProxyException("Failed to open location:" + vrl + "\n" + e.getMessage(), e);
        }
    }

    // actual open location: 
    public VRSProxyNode doOpenLocation(VRL locator) throws ProxyException {
        log.debug(">>> doOpenLocation():{} <<<", locator);

        try {
            VPath vnode = vrsClient.openPath(createVRL(locator));
            return createVRSProxyNode(vnode, locator);
        } catch (Exception e) {
            throw new ProxyException("Failed to open location:" + locator + "\n" + e.getMessage(),
                    e);
        }
    }

    private VRSProxyNode createVRSProxyNode(VPath vnode, VRL locator) throws ProxyException {
        return new VRSProxyNode(this, vnode, locator);
    }

    private VRL createVRL(VRL locator) {
        return new nl.esciencecenter.vbrowser.vrs.vrl.VRL(locator.getScheme(),
                locator.getUserinfo(), locator.getHostname(), locator.getPort(), locator.getPath(),
                locator.getQuery(), locator.getFragment());
    }

    @Override
    public boolean canOpen(VRL locator, StringHolder reason) {
        // internal scheme!
        if (StringUtil.equals("myvle", locator.getScheme())) {
            reason.value = "Internal 'MyVle' object";
            return true;
        }

        VResourceSystemFactory vrs = vrsClient.getVRSFactoryForScheme(locator.getScheme());

        if (vrs != null)
            return true;

        reason.value = "Unknown scheme:" + locator.getScheme();
        return false;
    }

    public VRSProxyNodeDnDHandler getProxyDnDHandler(ViewNode viewNode) {
        return proxyDnDHandler;
    }

    public VRSProxyNode registerVRSProxyNode(VPath vpath) throws ProxyException {

        ProxyNode prevNode = super.cacheFetch(vpath.getVRL());

        if ((prevNode != null) && (prevNode instanceof VRSProxyNode)) {
            return (VRSProxyNode) prevNode;
        }

        VRSProxyNode vrsNode = createVRSProxyNode(vpath, vpath.getVRL());
        super.cacheUpdate(vrsNode);
        return vrsNode;
    }

    public List<ProxyNode> registerVRSProxyNodes(List<VPath> vpaths)
            throws ProxyException {
        ArrayList<ProxyNode> proxyNodes = new ArrayList<ProxyNode>();
        for (int i = 0; i < vpaths.size(); i++) {
            proxyNodes.add(registerVRSProxyNode(vpaths.get(i)));
        }
        return proxyNodes;
    }

}
