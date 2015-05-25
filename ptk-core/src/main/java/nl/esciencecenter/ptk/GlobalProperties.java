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

package nl.esciencecenter.ptk;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import nl.esciencecenter.ptk.util.StringUtil;

/**
 * Global properties and other runtime configurations.
 */
public class GlobalProperties {

    /** Java property name for architecture: For example: "i386" or "i586" */
    public static final String PROP_JAVA_OS_ARCH = "os.arch";

    /** Java property name for Operation System name "Linux" or "Windows" */
    public static final String PROP_JAVA_OS_NAME = "os.name";

    /** Java property name for OS Version" */
    public static final String PROP_JAVA_OS_VERSION = "os.version";

    /** Java temporary directory */
    public static final String PROP_JAVA_TMPDIR = "java.io.tmpdir";

    /** Java property which specifies the user home or $HOME. */
    public static final String PROP_JAVA_USER_HOME = "user.home";

    /** Java property which specifies the user name or $USER. */
    public static final String PROP_JAVA_USER_NAME = "user.name";

    /** Java property which specifies the 'user directory' or current working directory. */
    public static final String PROP_JAVA_USER_DIR = "user.dir";

    /** Java os.name value for Linux */
    public static final String LINUX = "Linux";

    /** Java os.name value for Windows */
    public static final String WINDOWS = "Windows";

    /** Java os.name value for Windows */
    public static final String WINDOWS7 = "Windows 7";

    /** Java os.name value for Mac OS */
    public static final String MAC_OS = "Mac OS";

    /** Java os.name value for Mac OS X */
    public static final String MAC_OSX = "Mac OS X";

    // ========
    // Privates
    // ======== 
    /** Private copy for non system properties */
    private static Properties globalProperties = new Properties();

    /**
     * Dummy method. Call this method to initialize class loading. See static{...}
     */
    public static void init() {
        ;//
    }

    public static Object getProperty(String name) {
        Object prop = globalProperties.getProperty(name);
        if (prop != null)
            return prop;

        prop = System.getProperty(name);
        return prop;
    }

    /** Auto cast Property to String value */
    public static String getStringProperty(String name) {
        Object prop = getProperty(name);
        if (prop == null)
            return null;
        return prop.toString();
    }

    public static Object getEnv(String name) {
        Object envval = System.getenv(name);
        return envval;
    }

    public static String getGlobalUserName() {
        return getStringProperty(PROP_JAVA_USER_NAME);
    }

    public static String getGlobalUserHome() {
        return getStringProperty(PROP_JAVA_USER_HOME);
    }

    /**
     * The "User Directory" is also known as "current working directory", except this is the path at
     * startup and can't be manipulated. Note: If an application might want to change the CWD, this
     * has to be done by the application itself.
     * 
     * @return
     */
    public static String getGlobalUserDir() {
        return getStringProperty(PROP_JAVA_USER_DIR);
    }

    public static String getGlobalTempDir() {
        return getStringProperty(PROP_JAVA_TMPDIR);
    }

    /**
     * Returns fully qualified hostname or 'localhost' if hostname can't be determined.
     * 
     * @throws UnknownHostException
     */
    public static String getHostname() {

        try {
            String hostname = InetAddress.getLocalHost().getCanonicalHostName();
            return hostname;
        } catch (UnknownHostException e) {
            return "localhost";
        }

    }

    public static String getOsArch() {
        return getStringProperty(PROP_JAVA_OS_ARCH);
    }

    public static String getOsName() {
        return getStringProperty(PROP_JAVA_OS_NAME);
    }

    public static String getOsVersion() {
        return getStringProperty(PROP_JAVA_OS_VERSION);
    }

    /**
     * Returns true for all OS Name values that start with "Windows". Current known values are (some
     * unconfirmed): <lu> <li>
     * Windows Me <li>Windows 2000 <li>Windows 95 <li>Windows 98 <li>Windows NT <li>Windows Vista
     * <li>Windows XP <li>Windows 7 <li>
     * Windows 8 </ul>
     * 
     * @return - true if OS Name value starts with "Windows".
     */
    public static boolean isWindows() {
        // compare in all lower case 
        return GlobalProperties.getOsName().toLowerCase().startsWith(WINDOWS.toLowerCase());
    }

    /**
     * Returns true if OSName exactly matches "Windows 7". Use isWindows() for all (modern) windows
     * versions.
     * 
     * @return - true if operating system is Windows 7 and only Windows 7.
     */
    public static boolean isWindows7() {
        return StringUtil.equalsIgnoreCase(GlobalProperties.getOsName(), WINDOWS7);
    }

    public static boolean isLinux() {
        return StringUtil.equalsIgnoreCase(GlobalProperties.getOsName(), LINUX);
    }

    /**
     * Explicit Mac Os<strong>X</strong>.
     */
    public static boolean isMacOSX() {
        return StringUtil.equalsIgnoreCase(GlobalProperties.getOsName(), MAC_OSX);
    }

    /**
     * Explicit Mac Os, but <strong>not</strong> 'Os X'.
     */
    public static boolean isMacOS() {
        return StringUtil.equalsIgnoreCase(GlobalProperties.getOsName(), MAC_OS);
    }

    /**
     * Returns true for all OS Name values that start with "Mac".
     */
    public static boolean isMac() {
        return GlobalProperties.getOsName().toLowerCase().startsWith("mac");
    }

    public static String getJavaVersion() {
        return getStringProperty("java.version");
    }

    public static String getJavaHome() {
        return getStringProperty("java.home");
    }

}
