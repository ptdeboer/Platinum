package nl.piter.vterm.sys;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Properties;

@Slf4j
public class SysEnv {

    private static SysEnv instance = null;

    private SysEnv() {
        log.info("<INIT>");
        log.debug(" - user.name={}",getUserName());
        log.debug(" - user.home={}",getUserHome());
        log.debug(" - user.os={}",getOsName());
        this.sysFS=new SysFS();
    }

    public static SysEnv sysEnv() {
        if (instance == null) {
            instance = new SysEnv();
        }
        return instance;
    }

    // --- //
    private final SysFS sysFS;

    public boolean isLinux() {
        return true;
    }

    public boolean isWindows() {
        return false; // hah!
    }

    public String getUserName() {
        return getSysProperty("user.name");
    }

    public String getUserHome() {
        return getSysProperty("user.home");
    }

    public String getSysProperty(String name) {
        return System.getProperty(name);
    }

    public URI getUserHomeDir() throws IOException {
        return sysFS.resolveFileURI(getUserHome());
    }

    public String getOsName() {
        return getSysProperty("os.name");
    }

    public Properties loadProperties(URI propFileUri) throws IOException {
        log.error("Trying to load properties:{}", propFileUri);
        try {
            Properties properties = new Properties();
            try (InputStream inps = sysFS.newInputStream(propFileUri)) {
                properties.load(inps);
            }
            return properties;
        } finally {
        }
    }

    public void saveProperties(URI propFileUri, Properties properties, String propertiesHeader) throws IOException {
        log.debug("saveProperties():'{}' => {}",propertiesHeader,propFileUri);
        //
        try (OutputStream outps = sysFS.newOutputStream(propFileUri)) {
            properties.store(outps, propertiesHeader);
        } finally {
        }
    }

    /**
     * Resolve files/paths against user.home location.
     */
    public URI resolveUserHomePath(String subPath) throws IOException {
        String path = null;
        try {
            path = getUserHomeDir().normalize().toURL().getPath();
        } catch (MalformedURLException ex) {
            throw new IOException(ex.getMessage(), ex);
        }
        path += "/" + subPath;
        return sysFS.resolveFileURI(path);
    }

    public SysFS sysFS() {
        return sysFS;
    }

}
