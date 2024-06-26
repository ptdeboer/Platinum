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

package nl.esciencecenter.ptk.vbrowser.ui.proxy;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyNodeDnDHandler;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.vbrowser.vrs.event.VRSEventNotifier;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.util.Hashtable;
import java.util.Map;

/**
 * ProxyNodeFactory
 */
@Slf4j
public abstract class ProxyFactory {

    /**
     * Proxy Cache element stores actual ProxyNode and cache satus. The element is also used a mutex
     * when opening a location.
     */
    protected class ProxyCacheElement {
        /**
         * Atomic locator !
         */
        protected final VRL locator;

        private ProxyNode node;

        private long time;

        protected ProxyCacheElement(VRL locator) {
            this.locator = locator;
        }

        public ProxyCacheElement(VRL vrl, ProxyNode newNode) {
            this.locator = vrl;
            this.node = newNode;
            mark();
        }

        public synchronized ProxyNode getNode() {
            return node;
        }

        protected synchronized void setNode(ProxyNode node) {
            this.node = node;
            mark();
        }

        public synchronized boolean hasNode() {
            return (this.node != null);
        }

        protected void mark() {
            time = System.currentTimeMillis();
        }

        public long getTime() {
            return time;
        }

        public void dispose() {
            if (node != null) {
                // clear field, but keep VRL for reference!
                node.dispose();
                node = null;
            }
        }
    }

    protected class ProxyCache {
        protected Map<VRL, ProxyCacheElement> _nodes = new Hashtable<VRL, ProxyCacheElement>();

        protected ProxyCacheElement get(VRL locator) {
            return _nodes.get(locator);
        }

        protected ProxyCacheElement put(VRL locator, ProxyCacheElement proxyCacheElement) {
            return _nodes.put(locator, proxyCacheElement);
        }

        protected void clear() {
            _nodes.clear();
        }

        protected boolean exists(VRL locator) {
            return (_nodes.get(locator) != null);
        }

        protected ProxyCacheElement createEntry(VRL locator) {
            ProxyCacheElement cacheEl = new ProxyCacheElement(locator);
            proxyCache.put(locator, cacheEl);
            return cacheEl;
        }

        protected synchronized ProxyCacheElement put(ProxyNode node) {
            VRL vrl = node.getVRL();
            ProxyCacheElement cacheEl = new ProxyCacheElement(vrl, node);
            proxyCache.put(vrl, cacheEl);
            return cacheEl;
        }

        protected ProxyCacheElement remove(ProxyNode node) {
            return remove(node.getVRL());
        }

        protected ProxyCacheElement remove(VRL locator) {
            synchronized (_nodes) {
                ProxyCacheElement el = _nodes.remove(locator);
                if (el != null) {
                    el.dispose();
                    return el;
                }
            }
            return null;
        }

    }

    // ========================================================================
    //
    // ========================================================================

    protected boolean enableCache = true;

    protected ProxyCache proxyCache = new ProxyCache();

    protected ProxyCache cacheRemoved = new ProxyCache();

    protected BrowserPlatform platform;

    protected ProxyFactory(BrowserPlatform browserPlatform) {
        this.platform = browserPlatform;
        initProxyEventCacheUpdater();
    }

    public BrowserPlatform getPlatform() {
        return platform;
    }

    protected void initProxyEventCacheUpdater() {
        platform.getVRSEventNotifier().addListener(new ProxyNodeCacheUpdater(this), null);
    }

    // ========================================================================
    //
    // ========================================================================

    final public ProxyNode openLocation(String locationString) throws ProxyException {
        try {
            return openLocation(new VRL(locationString));
        } catch (VRLSyntaxException e) {
            throw new ProxyException("VRLSyntaxException", e);
        }
    }

    final public ProxyNode openLocation(VRL locator) throws ProxyException {
        if (locator == null) {
            throw new ProxyException("NULL Locator!");
        }

        if (enableCache == false) {
            return doOpenLocation(locator);
        } else {
            ProxyCacheElement cacheEl;

            synchronized (this.proxyCache) {
                cacheEl = this.proxyCache.get(locator);
                // create new element
                if (cacheEl == null) {
                    cacheEl = this.proxyCache.createEntry(locator);
                    log.debug("+++ Cache: new element for:{}", locator);
                }

                log.debug("--- Cache: cached element for:{}", locator);
            }

            ProxyNode node;

            // Now synchronized around cache element !
            synchronized (cacheEl) {
                if (cacheEl.hasNode() == true) {
                    // ====================
                    // Cache hit
                    // ====================

                    node = cacheEl.getNode();
                    log.debug("<<< Cache: Cache Hit: cached proxy node for:{}", locator);
                    return node;
                } else {
                    // ====================
                    // Actual openLocation
                    // ====================
                    log.debug(">>> Cache: START OpenLocation for:{}", locator);

                    {
                        node = doOpenLocation(locator);
                        cacheEl.setNode(node);
                    }

                    log.debug(">>> Cache: FINISHED OpenLocation for:{}", locator);
                }
            }

            // New Node: Perform prefetch here, but outside mutex erea.
            node.doPrefetchAttributes();

            return node;
        }
    }

    public void cacheClear() {
        synchronized (proxyCache) {
            this.proxyCache.clear();
        }
    }

    public void refreshChilds(VRL parentVrl) {
        ProxyCacheElement cacheEl = this.proxyCache.get(parentVrl);
        if (cacheEl != null) {
            cacheEl.getNode().clearCache();
        }
    }

    public void clearCache(VRL vrl) {
        ProxyCacheElement cacheEl = this.proxyCache.get(vrl);
        if (cacheEl != null) {
            cacheEl.getNode().clearCache();
        }
    }

    protected void handleException(String message, Exception e) {
        log.error(message, e);
    }

    protected ProxyNode cacheFetch(VRL locator) {
        synchronized (proxyCache) {
            ProxyCacheElement el = proxyCache.get(locator);
            if (el == null) {
                return null;
            }
            return el.node;
        }
    }

    protected void cacheUpdate(ProxyNode node) {
        synchronized (proxyCache) {
            proxyCache.put(node);
        }
    }

    /**
     * Removing a ProxyNode does not invalidate it. Instances might be kept.
     */
    protected void cacheRemove(ProxyNode proxyNode) {
        log.debug("--- Cache: cacheRemove: @{}", proxyNode.id);

        ProxyNode cacheNode = cacheFetch(proxyNode.getVRL());
        this.proxyCache.remove(proxyNode);

        if (cacheNode.equals(proxyNode)) {
            log.error("cacheRemove(): Warning: given ProxyNode does not match cached node: {} != {}",
                    proxyNode, cacheNode);
            this.proxyCache.remove(cacheNode);
        }

        if (cacheNode != null) {
            ProxyNode parent = cacheNode.cache.parent;
            if (parent != null) {
                // clear parent; 
                parent.cache.childNodes = null;
            }
        }
    }


    // ========================================================================
    // Proxy Node Events
    // ========================================================================

    public VRSEventNotifier getProxyNodeEventNotifier() {
        // Cross paltform event notifier.
        return platform.getVRSEventNotifier();
    }

    // ========================================================================
    // Abstract interface
    // ========================================================================

    abstract public ProxyNode doOpenLocation(VRL locator) throws ProxyException;

    abstract public boolean canOpen(VRL locator, StringHolder reason);

    // DND Handler
    abstract public ProxyNodeDnDHandler getProxyDnDHandler(ViewNode viewNode);

}
