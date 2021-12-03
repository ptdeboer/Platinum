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

package nl.esciencecenter.vbrowser.vrs.registry;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.vbrowser.vrs.VCloseable;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.infors.InfoRSFactory;
import nl.esciencecenter.vbrowser.vrs.localfs.LocalFSFileSystemFactory;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import nl.esciencecenter.vbrowser.vrs.webrs.WebRSFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Static Registry for VResourceSystemFactories. <br>
 * Currently there is only a singleton Registry. ResourceSystems are linked to a VRSContext.
 */
@Slf4j
public class Registry {

    private static Registry instance;

    public static Registry getInstance() {
        synchronized (Registry.class) {
            if (instance == null) {
                instance = new Registry();
            }
        }
        return instance;
    }

    protected static class SchemeInfo {

        protected String scheme;

        protected VResourceSystemFactory vrsFactory;

        SchemeInfo(String scheme, VResourceSystemFactory factory) {
            this.scheme = scheme;
            this.vrsFactory = factory;
        }
    }

    // ========================================================================
    //
    // ========================================================================

    private final Map<String, ArrayList<SchemeInfo>> registeredSchemes = new LinkedHashMap<String, ArrayList<SchemeInfo>>();

    /**
     * List of services. VRSFactories are registered using their class names as key.
     */
    private final Map<String, VResourceSystemFactory> registeredServices = new HashMap<String, VResourceSystemFactory>();

    private final ResourceSystemInstances instances = new ResourceSystemInstances();

    private Registry() {
        init();
    }

    private void init() {
        initFactories();
    }

    private void initFactories() {
        this.registryFactoryNoException(LocalFSFileSystemFactory.class);
        this.registryFactoryNoException(WebRSFactory.class);
        this.registryFactoryNoException(InfoRSFactory.class);
    }

    public VResourceSystemFactory getVResourceSystemFactoryFor(VRSContext vrsContext, String scheme) {
        ArrayList<SchemeInfo> list = registeredSchemes.get(scheme);
        if ((list == null) || (list.size() <= 0)) {
            return null;
        }

        return list.get(0).vrsFactory;
    }

    public VResourceSystem getVResourceSystemFor(VRSContext vrsContext, VRL vrl) throws VrsException {
        if (vrl == null) {
            throw new NullPointerException("VRL is NULL");
        }

        VResourceSystemFactory factory = getVResourceSystemFactoryFor(vrsContext, vrl.getScheme());

        if (factory == null) {
            throw new VrsException("No VResourceSystem registered for:" + vrl);
        }

        synchronized (instances) {
            String id = factory.createResourceSystemId(vrl);

            VResourceSystem resourceSystem = instances.getResourceSystem(vrsContext, id);

            if (resourceSystem == null) {
                ResourceConfigInfo info = getResourceSystemInfo(vrsContext, vrl, true);
                resourceSystem = factory.createResourceSystemFor(vrsContext, info, vrl);
                instances.putResourceSystem("" + vrsContext.getID(), id, resourceSystem);
            }
            return resourceSystem;
        }
    }

    protected ResourceConfigInfo getResourceSystemInfo(VRSContext vrsContext, VRL vrl, boolean autoCreate)
            throws VrsException {
        return vrsContext.getResourceSystemInfoFor(vrl, true);
    }

    public void registryFactoryNoException(Class<? extends VResourceSystemFactory> vrsClass) {
        try {
            registerFactory(vrsClass);
        } catch (Throwable t) {
            log.error("Exception when registering VRS Factory Class:{}", vrsClass);
            log.error(t.getMessage(), t);
        }
    }

    /**
     * Register the ResourceSystemFactory in the singleton Registry.<br>
     */
    public void registerFactory(Class<? extends VResourceSystemFactory> vrsClass) throws InstantiationException,
            IllegalAccessException {
        VResourceSystemFactory vrsInstance;

        synchronized (registeredServices) {
            // ===
            // Protected VRSFactory instance is created here !
            // ===
            vrsInstance = vrsClass.newInstance();
            registeredServices.put(vrsClass.getCanonicalName(), vrsInstance);
        }

        synchronized (registeredSchemes) {
            for (String scheme : vrsInstance.getSchemes()) {
                ArrayList<SchemeInfo> list = registeredSchemes.get(scheme);
                if (list == null) {
                    log.debug("Registering scheme:{} =>{}", scheme, vrsClass.getCanonicalName());
                    list = new ArrayList<SchemeInfo>();
                    registeredSchemes.put(scheme, list);
                }
                list.add(new SchemeInfo(scheme, vrsInstance));
            }
        }
    }

    public void unregisterFactory(Class<? extends VResourceSystemFactory> vrsClass) {
        VResourceSystemFactory vrsInstance;

        synchronized (registeredServices) {
            vrsInstance = registeredServices.remove(vrsClass.getCanonicalName());
        }

        if (vrsInstance == null)
            return;

        synchronized (registeredSchemes) {
            for (String scheme : vrsInstance.getSchemes()) {
                ArrayList<SchemeInfo> list = registeredSchemes.get(scheme);
                if (list == null) {
                    return;
                }

                // reverse list and remove entries.
                int listSize = list.size();

                for (int i = listSize - 1; i >= 0; i--) {
                    SchemeInfo entry = list.get(i);

                    if (entry.vrsFactory.equals(vrsInstance)) {
                        list.remove(i);
                        listSize = list.size();
                    }
                }
            }
        }
    }

    public void cleanupFor(VRSContext vrsContext) {
        //
        String ctxId = "" + vrsContext.getID();
        log.debug("cleanupFor:{}", ctxId);
        Map<String, VResourceSystem> list = instances.getResourceSystemsFor(vrsContext);
        //
        if ((list != null) && (!list.isEmpty())) {
            for (String key : list.keySet().toArray(new String[0])) {
                VResourceSystem resourceSys = list.get(key);
                log.debug("cleanupFor:{}: - VResourceSystem instance:{}:{}", ctxId, resourceSys.getServerVRL(),
                        resourceSys);
                if (resourceSys instanceof VCloseable) {
                    try {
                        ((VCloseable) resourceSys).close();
                    } catch (IOException e) {
                        log.error("IOException when closing:{}:{}", resourceSys, e);
                    }
                }
            }
        }
        //
        instances.unregisterResourceSystemsFor(vrsContext);
    }

}
