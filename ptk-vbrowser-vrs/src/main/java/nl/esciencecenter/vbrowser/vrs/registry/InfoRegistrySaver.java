package nl.esciencecenter.vbrowser.vrs.registry;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.data.xml.XMLData;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.exceptions.XMLDataException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Save/Load ResourceSystemInfo using XMLAttributeSets.
 */
public class InfoRegistrySaver {

    private final static Logger logger = LoggerFactory.getLogger(ResourceSystemInfoRegistry.class);

    private final static String PERSISTANT_CONFIG_ID = "persistant-config-id";

    private ResourceSystemInfoRegistry infoRegistry;

    private VRL configDir;

    private String fileName;

    public InfoRegistrySaver(ResourceSystemInfoRegistry registry, VRL cfgDir, String fileName) {
        this.infoRegistry = registry;
        this.configDir = cfgDir;
        this.fileName = fileName;
    }

    public void save() throws VrsException {

        logger.info("Saving ResourceSystemInfoRegistry to:{}/{}", configDir, fileName);

        List<AttributeSet> infoSetList = new ArrayList<AttributeSet>();
        Map<String, ResourceConfigInfo> infos = infoRegistry.getResourceInfos();
        Set<String> keys = infos.keySet();

        for (String key : keys) {
            ResourceConfigInfo info = infos.get(key);
            AttributeSet attrSet = info.getConfigAttributeSet();
            attrSet.put(PERSISTANT_CONFIG_ID, info.getID());
            attrSet.setName("ResourceConfigInfo:" + info.getID());
            infoSetList.add(attrSet);
        }

        try {
            XMLData xmlData = new XMLData(infoRegistry.getVRSContext());
            String infoXML;

            infoXML = xmlData.toXML(ResourceSystemInfoRegistry.getVersionInfo(), infoSetList);
            saveXML(infoXML, configDir, fileName);
        } catch (VrsException | IOException e) {

        }
    }

    protected void saveXML(String xml, VRL configDirVrl, String fileName) throws VrsException, MalformedURLException,
            IOException {
        //
        logger.info("Saving ResourceSystemInfoRegistry to:{}/{}", configDirVrl, fileName);
        VRSClient vrsClient = new VRSClient(infoRegistry.getVRSContext());
        //
        xml = XMLData.prettyFormat(xml, 3);
        VFSPath path = vrsClient.openVFSPath(configDirVrl);
        VFSPath dir = path;
        VFSPath file = path.resolvePath(fileName);
        if (dir.exists() == false) {
            logger.info("Creating new config dir:{}", dir);
            dir.mkdirs(true);
        }
        vrsClient.createResourceLoader().writeTextTo(file.getVRL().toURL(), xml, ResourceLoader.CHARSET_UTF8);

    }

    protected List<ResourceConfigInfo> load() throws VrsException, MalformedURLException, IOException {
        VRSClient vrsClient = new VRSClient(infoRegistry.getVRSContext());
        VFSPath path = vrsClient.openVFSPath(configDir);
        VFSPath file = path.resolvePath(fileName);

        if (file.exists() == false) {
            logger.info("Persistant system info registry not found:{}", file);
            return null;
        }

        String xml = vrsClient.createResourceLoader().readText(file.getVRL().toURL(), ResourceLoader.CHARSET_UTF8);

        return parseXML(xml);
    }

    protected List<ResourceConfigInfo> parseXML(String xml) throws XMLDataException {

        XMLData xmlData = new XMLData(infoRegistry.getVRSContext());
        StringHolder groupNameH = new StringHolder();
        List<AttributeSet> list = xmlData.parseAttributeSetList(xml, groupNameH);
        List<ResourceConfigInfo> infos = new ArrayList<ResourceConfigInfo>();

        for (AttributeSet set : list) {
            String id = set.getStringValue(PERSISTANT_CONFIG_ID);
            set.remove(PERSISTANT_CONFIG_ID);
            ResourceConfigInfo info = infoRegistry.createFrom(set, id);
            logger.info("new ResourceConfigInfo:{}", info);
            ;
            infos.add(info);
        }

        return infos;

    }

}
