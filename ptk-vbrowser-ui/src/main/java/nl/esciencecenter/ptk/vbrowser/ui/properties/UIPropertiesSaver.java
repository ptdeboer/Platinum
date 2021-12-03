package nl.esciencecenter.ptk.vbrowser.ui.properties;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class UIPropertiesSaver {

    private final VRSClient vrsClient;

    public UIPropertiesSaver(VRSContext vrsContext) {
        vrsClient = new VRSClient(vrsContext);
    }

    public void saveProperties(UIProperties uiProps, VRL configVrl) throws VrsException {
        log.debug("Saving UIProperties to:{}", configVrl);

        try {
            Map<String, Object> values = uiProps.getProperties();
            Properties props = new Properties();
            props.putAll(values);

            VFSPath path = vrsClient.openVFSPath(configVrl);
            VFSPath dir = path.getParent();

            if (dir.exists() == false) {
                log.debug("Creating new config dir:{}", dir);
                dir.mkdirs(true);
            }
            try (OutputStream outps = vrsClient.createOutputStream(path, false)) {
                props.store(outps, "VBrowser UIProperties");
            }

        } catch (Exception e) {
            throw new VrsException(e.getMessage(), e);
        }
    }


    public UIProperties loadFrom(VRL confLoc) throws VrsException {
        log.debug("Loading UIProperties to:{}", confLoc);

        try {
            VFSPath path = vrsClient.openVFSPath(confLoc);
            if (path.exists() == false) {
                log.debug("UIProperties not found: {}", confLoc);
            }
            Properties props = new Properties();
            try (InputStream inps = vrsClient.createInputStream(path)) {
                props.load(inps);
            }
            UIProperties uiProps = new UIProperties(props);
            return uiProps;

        } catch (Exception e) {
            throw new VrsException("Failed to load config from:" + confLoc + ".\n" + e.getMessage(), e);
        }
    }

}
