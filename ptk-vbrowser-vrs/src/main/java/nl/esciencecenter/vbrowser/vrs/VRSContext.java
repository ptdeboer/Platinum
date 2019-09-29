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

import java.util.Properties;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.crypt.Secret;
import nl.esciencecenter.ptk.io.FSUtil;
import nl.esciencecenter.ptk.ssl.CertificateStore;
import nl.esciencecenter.ptk.ssl.CertificateStoreException;
import nl.esciencecenter.ptk.ui.SimpelUI;
import nl.esciencecenter.ptk.ui.UI;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.vbrowser.vrs.credentials.Credential;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.Registry;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfoRegistry;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Main Context of the Virtual Resource System. Hold Registry, ResourceSystemInfoRegistry and
 * instantiated ResourceSystems.
 */
public class VRSContext {

    private static final PLogger logger = PLogger.getLogger(VRSContext.class);

    private static long instanceCounter = 0;

    // ---
    // Instance 
    // --- 
    private long id = instanceCounter++;

    protected Registry registry;

    protected VRSProperties vrsProperties;

    protected ResourceSystemInfoRegistry resourceInfoRegistry;

    protected UI ui;

    private VRL persistantConfigLocation = null;

    private boolean hasPersistantConfig = false;

    public VRSContext() {
        init(new VRSProperties("VRSContext"));
    }

    public long getID() {
        return id;
    }

    public VRSContext(Properties props) {
        init(new VRSProperties("VRSContext", props, true));
    }

    public VRSContext(VRSProperties props) {
        init(props.duplicate(false));
    }

    private void init(VRSProperties privateProperties) {
        logger.debugPrintf("***New VRSContext(), id=" + id + "***");
        // default Static Registry ! 
        this.registry = Registry.getInstance();
        this.vrsProperties = privateProperties;
        resourceInfoRegistry = new ResourceSystemInfoRegistry(this);
        ui = new SimpelUI();
    }

    public Registry getRegistry() {
        return registry;
    }

    /**
     * @return Configure properties for this VRSContext.
     */
    public VRSProperties getProperties() {
        return this.vrsProperties;
    }

    public CertificateStore getCertificateStore() throws VrsException {
        try {
            VRL caCertsLoc = null;
            VRL loc = this.getPersistantConfigLocation();
            if (loc!=null) {
                caCertsLoc = loc.appendPath("cacerts");

                CertificateStore certificateStore = CertificateStore.loadCertificateStore(caCertsLoc.getPath(),
                        new Secret(CertificateStore.DEFAULT_PASSPHRASE.toCharArray()), true, true);
                return certificateStore;
            } else {
                return CertificateStore.getDefault(true);
            }
        } catch (CertificateStoreException e) {
            throw new VrsException(e.getMessage(), e);
        }
    }

    public ResourceSystemInfoRegistry getResourceSystemInfoRegistry() {
        return this.resourceInfoRegistry;
    }

    public String createResourceSystemInfoIDFor(VRL vrl) throws VrsException {
        VResourceSystemFactory fac = registry.getVResourceSystemFactoryFor(this, vrl.getScheme());
        if (fac == null) {
            throw new VrsException("Scheme not supported. No ResourceSystemFactory for:" + vrl);
        }
        String id = fac.createResourceSystemId(vrl);
        return id;
    }

    public boolean suppertsResourceSystemFor(VRL vrl) {
        VResourceSystemFactory fac = registry.getVResourceSystemFactoryFor(this, vrl.getScheme());
        return (fac != null);
    }

    public ResourceConfigInfo getResourceSystemInfoFor(VRL vrl, boolean autoCreate) throws VrsException {
        //
        VResourceSystemFactory fac = registry.getVResourceSystemFactoryFor(this, vrl.getScheme());
        if (fac == null) {
            throw new VrsException("Scheme not supported. No ResourceSystemFactory for:" + vrl);
        }

        String id = fac.createResourceSystemId(vrl);
        ResourceConfigInfo info = resourceInfoRegistry.getInfo(id);

        if ((info == null) && (autoCreate == true)) {
            info = new ResourceConfigInfo(resourceInfoRegistry, vrl, id);
        }
        if (info != null) {
            info = fac.updateResourceInfo(this, info, vrl);
        }
        return info;
    }

    /**
     * Return actual ResourceSystemInfo used for the specified ResourceSystem.
     */
    public ResourceConfigInfo getResourceSystemInfoFor(VResourceSystem vrs) throws VrsException {
        // todo: query actual instance instead of registry.
        return getResourceSystemInfoFor(vrs.getServerVRL(), false);
    }

    public void putResourceConfigInfo(ResourceConfigInfo info) {
        resourceInfoRegistry.putInfo(info);
    }

    /**
     * For head-less environments, getUI() will return a dummy Object. For non graphical
     * environments this method will return a dummy ui. When registered in the VBrowser this method
     * will return an interactive callback interface to the VBrowser.
     * 
     * @return register UI or dummy UI for non-interactive environments.
     */
    public UI getUI() {
        return ui;
    }

    /**
     * Create absolute and normalized User Home VRL.
     */
    public VRL getHomeVRL() {
        return new VRL(FSUtil.getDefault().getUserHomeURI());
    }

    /**
     * Create absolute and normalized cwd of application Home VRL.
     */
    public VRL getCurrentPathVRL() {
        return new VRL(FSUtil.getDefault().getWorkingDirURI());
    }

    public String getUserName() {
        return GlobalProperties.getGlobalUserName();
    }

    public void setPersistantConfigLocation(VRL configHome, boolean enabled) {
        this.persistantConfigLocation = configHome;
        this.hasPersistantConfig = enabled;
        vrsProperties.set(VRSContextProperties.VRS_PERSISTANT_CONFIG_LOCATION_PROP, configHome);
        vrsProperties.set(VRSContextProperties.VRS_PERSISTANT_CONFIG_ENABLED_PROP, enabled);

        if (enabled) {
            this.resourceInfoRegistry.reload();
        }
    }

    public VRL getPersistantConfigLocation() {

        if (this.persistantConfigLocation != null) {
            return persistantConfigLocation;
        }

        String propVal = vrsProperties.getStringProperty(VRSContextProperties.VRS_PERSISTANT_CONFIG_LOCATION_PROP);

        if (propVal != null) {
            try {
                // return 
                return new VRL(propVal);
            } catch (Exception e) {
                logger.errorPrintf("Could parse property %s:'%s'\n",
                        VRSContextProperties.VRS_PERSISTANT_CONFIG_LOCATION_PROP, propVal);
            }
        }

        logger.debugPrintf("Property not defined or wrong value:%s\n",
                VRSContextProperties.VRS_PERSISTANT_CONFIG_LOCATION_PROP);
        return null;
    }

    public boolean hasPersistantConfig() {
        return (hasPersistantConfig) && (getPersistantConfigLocation() != null);
    }

    public String getVO() {
        return null;
    }

    /**
     * Return default credential for the specified type.
     */
    public Credential getDefaultCredential(String credentialType) {
        return null;
    }

    public VRL getInfoRootNodeVRL() throws VRLSyntaxException {
        return new VRL("info:/");
    }

    public String toString() {
        return "VRSContext:[id='" + this.id + "',persistantConfigLocation='" + persistantConfigLocation + "']";
    }

    public void dispose() {
        this.registry.cleanupFor(this);
        this.registry=null;
        this.resourceInfoRegistry.dispose();
        this.resourceInfoRegistry=null;
    }

    public String getCharEncoding() {
        return ResourceLoader.CHARSET_UTF8;
    }
}
