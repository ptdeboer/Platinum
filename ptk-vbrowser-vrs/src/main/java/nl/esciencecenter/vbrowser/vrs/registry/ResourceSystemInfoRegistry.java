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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceSystemInfoRegistry {

    private final static Logger logger = LoggerFactory.getLogger(ResourceSystemInfoRegistry.class);

    public static final String SYSREGINFO_FILE = "sysreginfo.vrsx";

    public static final String SystemInfoRegistryGroupName = "ResourceSystemInfo";

    public static final String SystemInfoRegistryVersion = "0.1";

    public static String getVersionInfo() {
        return String.format("%s:%s", SystemInfoRegistryGroupName, SystemInfoRegistryVersion);
    }

    /**
     * Owner of this registry.
     */
    private VRSContext vrsContext;

    private Map<String, ResourceConfigInfo> resourceInfos = new Hashtable<String, ResourceConfigInfo>();

    public ResourceSystemInfoRegistry(VRSContext vrsContext) {
        this.vrsContext = vrsContext;
    }

    public ResourceConfigInfo putInfo(ResourceConfigInfo info) {
        synchronized (resourceInfos) {
            // always update ID. 
            resourceInfos.put(info.getID(), info);
        }
        save();
        return info;
    }

    public ResourceConfigInfo getInfo(String id) {
        synchronized (resourceInfos) {
            return resourceInfos.get(id);
        }
    }

    /**
     * Returns a copy of the ResourceSystemInfos as a list.
     */
    public List<ResourceConfigInfo> list() {
        return new ExtendedList<ResourceConfigInfo>(getResourceInfos().values());
    }

    /**
     * Save configurations into the persistant store.
     * 
     * @return true if saved into the persistant store, false is persistant store is not configured.
     */
    protected boolean save() {

        logger.info("Saving ResourceSystemInfoRegistry for VRSContext:{}", vrsContext);

        if (vrsContext.hasPersistantConfig() == false) {
            logger.info("No persistant configuration for:" + vrsContext);
            return false;
        }

        VRL cfgDir = vrsContext.getPersistantConfigLocation();
        if (cfgDir == null) {
            logger.warn("Persistant configuration enabled but no configuration location for:" + vrsContext);
        }

        try {
            new InfoRegistrySaver(this, cfgDir, SYSREGINFO_FILE).save();
        } catch (VrsException e) {
            logger.error("Failed to save persistant system info registry to {}/{}", cfgDir, SYSREGINFO_FILE);
        }

        return true;
    }

    protected Map<String, ResourceConfigInfo> getResourceInfos() {
        return this.resourceInfos;
    }

    protected VRSContext getVRSContext() {
        return this.vrsContext;
    }

    protected ResourceConfigInfo createFrom(AttributeSet attrSet, String infoId) {
        ResourceConfigInfo info = new ResourceConfigInfo(this, attrSet, infoId);
        return info;
    }

    public boolean reload() {

        if (vrsContext.hasPersistantConfig() == false) {
            logger.info("No persistant configuration for:" + vrsContext);
            return false;
        }

        VRL cfgDir = vrsContext.getPersistantConfigLocation();
        if (cfgDir == null) {
            logger.warn("Persistant configuration enabled but no configuration location for:" + vrsContext);
        }

        try {
            return loadFrom(cfgDir, SYSREGINFO_FILE);
        } catch (VrsException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected boolean loadFrom(VRL configDir, String infoFileName) throws MalformedURLException, VrsException,
            IOException {

        List<ResourceConfigInfo> infos = new InfoRegistrySaver(this, configDir, infoFileName).load();

        synchronized (this.resourceInfos) {
            this.resourceInfos.clear();
            if (infos == null) {
                return false;
            }

            for (ResourceConfigInfo info : infos) {
                if (vrsContext.suppertsResourceSystemFor(info.getServerVRL())) {
                    String id = this.vrsContext.createResourceSystemInfoIDFor(info.getServerVRL());
                    info.updateId(id);
                    this.resourceInfos.put(id, info);
                } else {
                    logger.error("Configuration not supported for resource:" + info.getServerVRL());
                }

            }
        }

        return true;
    }

    public void dispose () 
    {
        this.resourceInfos.clear(); 
        this.resourceInfos=null;
        this.vrsContext=null;
    }
}
