package tests.integration.vfs;

import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.VRSContext;

/**
 * Super class for the VFS Test Cases.
 */
public abstract class VTestCase
{
    public static final int VERBOSE_NONE = 10;

    public static final int VERBOSE_ERROR = 8;

    public static final int VERBOSE_WARN = 6;

    public static final int VERBOSE_INFO = 4;

    public static final int VERBOSE_DEBUG = 2;

    int verboseLevel = VERBOSE_INFO;

    public void setVerbose(int level)
    {
        verboseLevel = level;
    }

    public void verbose(int verbose, String msg)
    {
        if (verbose >= verboseLevel)
            System.out.println("testVFS:" + msg);
    }

    public void errorPrintf(String format, Object... args)
    {
        if (VERBOSE_INFO >= verboseLevel)
            System.out.printf(format, args);
    }

    public void warnPrintf(String format, Object... args)
    {
        if (VERBOSE_WARN >= verboseLevel)
            System.out.printf(format, args);
    }

    public void message(String msg)
    {
        verbose(VERBOSE_INFO, msg);
    }

    public void messagePrintf(String format, Object... args)
    {
        if (VERBOSE_INFO >= verboseLevel)
            System.out.printf(format, args);
    }

    public void debug(String msg)
    {
        verbose(VERBOSE_DEBUG, msg);
    }

    public void debugPrintf(String format, Object... args)
    {
        if (VERBOSE_DEBUG >= verboseLevel)
            System.out.printf(format, args);
    }

    // =========
    // Instance
    // =========

    protected VRSClient vrs = null;

    protected ResourceLoader resourceLoader = null;

    protected VRSClient getVFS()
    {
        if (vrs == null)
        {
            vrs = new VRSClient(Settings.getStaticTestContext());
        }
        return vrs;
    }

    protected VRSContext getVRSContext()
    {
        return getVFS().getVRSContext();
    }

    protected ResourceLoader getVRSResourceLoader()
    {
        if (this.resourceLoader == null)
        {
            this.resourceLoader = getVFS().createResourceLoader();
        }

        return this.resourceLoader;
    }
    
    protected void deleteLater(VFSPath path) {
        
    }

}
