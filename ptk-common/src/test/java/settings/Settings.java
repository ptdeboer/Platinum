package settings;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.io.FSPath;
import nl.esciencecenter.ptk.io.FSUtil;
import org.junit.Assert;

import java.io.IOException;
import java.util.Properties;

public class Settings {

    private static Settings instance = null;

    public final String TEST_AES256BITS_ENCRYPTION_PROP = "platinum.test.encryption.AES256bits";

    public final String TEST_LOCALTESTDIR_PROP = "platinum.test.localTestDir";

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }


    // ===
    // Instance 
    // === 

    protected Properties properties = new Properties();

    public boolean testAES256Encryption() {
        return getBoolValue(TEST_AES256BITS_ENCRYPTION_PROP, true);
    }

    public boolean getBoolValue(String name, boolean defaultValue) {
        Object value = properties.get(name);

        if (value == null) {
            return defaultValue;
        } else if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            return Boolean.parseBoolean(value.toString());
        }
    }

    public String getValue(String name, String defaultValue) {
        Object value = properties.get(name);

        if (value != null) {
            if (value instanceof String) {
                return (String) value;
            } else {
                return value.toString();
            }
        }

        return defaultValue;
    }

    public String getLocalTestDir() {
        return getValue(TEST_LOCALTESTDIR_PROP, GlobalProperties.getGlobalTempDir());
    }

    public String getTestSubdir(String subPath) {
        String testDir = getLocalTestDir();
        // 
        return testDir + "/" + subPath;
    }

    public boolean isWindows() {
        return GlobalProperties.isWindows();
    }

    public FSPath getFSUtil_testDir(boolean autoCreate) throws IOException {
        String testDirstr = getLocalTestDir() + "/testDirPTKUtil";
        FSPath testDir = getFSUtil().mkdirs(getFSUtil().newFSPath(testDirstr));

        if (testDir.exists() == false) {
            if (autoCreate == false) {
                throw new IOException("Test directory doesn't exists and I'm not allowed to create it:" + testDirstr);
            }

            getFSUtil().mkdir(testDir);
            Assert.assertTrue("Test dir must exist:" + testDir, testDir.exists());
        }
        return testDir;
    }

    public FSUtil getFSUtil() {
        return FSUtil.fsutil();

    }

}
