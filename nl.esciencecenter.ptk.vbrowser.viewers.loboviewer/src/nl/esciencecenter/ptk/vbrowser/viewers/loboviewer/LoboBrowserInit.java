package nl.esciencecenter.ptk.vbrowser.viewers.loboviewer;

import javax.net.ssl.SSLContext;

import org.lobobrowser.main.PlatformInit;

import nl.esciencecenter.ptk.ssl.CertificateStore;
import nl.esciencecenter.ptk.ssl.CertificateStoreException;
import nl.esciencecenter.ptk.ssl.SslConst;
import nl.esciencecenter.ptk.ssl.SslUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.vrs.vrlstreamhandler.VRLStreamHandlerFactory;
import nl.esciencecenter.vbrowser.vrs.VRSContext;

public class LoboBrowserInit
{
    private static final ClassLogger logger = ClassLogger.getLogger(LoboBrowserInit.class);

    static
    {
        // initPlatform();
    }

    private static boolean globalURLStreamFactoryInitialized = false;

    public static void initPlatform(BrowserPlatform platform)
    {
        VRSContext context = platform.getVRSContext();

        try
        {
            context.getRegistry().registerFactory(nl.esciencecenter.ptk.vbrowser.viewers.loboviewer.resfs.ResFS.class);
        }
        catch (Exception e)
        {
            logger.logException(ClassLogger.ERROR, e, "Static Init Exception:%s\n", e);
        }

        try
        {
            PlatformInit instance = PlatformInit.getInstance();
            // disable logging BEFORE initializing Lobo
            instance.initLogging(false);
            instance.initExtensions();
            /*
             * Piter T. de Boer: This method set the URLHandlerFactory. Since the VRS has it's own URLHandlerFactory,
             * this code may NOT be called. By initializing the extensions, but not the procotols, the LoboBrowser panel
             * can be used.
             */
            // PlatformInit.getInstance().addPrivilegedPermission(new RuntimePermission("*"));
            // PlatformInit.getInstance().addPrivilegedPermission(new FilePermission("*","write,execute,delete"));
            // PlatformInit.getInstance().addPrivilegedPermission(new SecurityPermission("*",null));
            // PlatformInit.getInstance().initProtocols();

            // TODO: Security context
            // PlatformInit.getInstance().initSecurity();
            // instance.init(false,false);

            // SSL CaCert context.

            CertificateStore cacerts = context.getCertificateStore();

            initStaticSSLContext(cacerts);

            // initURLStreamFactory();

        }
        catch (Exception e)
        {
            logger.logException(ClassLogger.ERROR, e, "Init error:%s\n", e);
        }

        shutuplogging();
    }

    private static void initStaticSSLContext(CertificateStore cacerts) throws CertificateStoreException
    {

        SSLContext sslContext = cacerts.createSSLContext(SslConst.PROTOCOL_SSLv3);
        SslUtil.setStaticHttpsSslContext(sslContext);

    }

    private static void shutuplogging()
    {
        // Not needed: Use VLET_INSTALL/etc/properties/logging.properties file !

        // // Log4J!
        // org.apache.log4j.Logger.getLogger((org.lobobrowser.main.PlatformInit.class).getName()).setLevel(Level.FATAL);
        //
        // Vector<String> loggers=new Vector<String>();
        //
        // loggers.add(org.lobobrowser.html.style.CSSUtilities.class.getName());
        // loggers.add(com.steadystate.css.parser.SACParser.class.getName());
        // loggers.add(org.lobobrowser.html.js.Executor.class.getName());
        //
        // for (String classname:loggers)
        // {
        // java.util.logging.Logger logger=java.util.logging.Logger.getLogger(classname);
        // while(logger!=null)
        // {
        //
        // // System.err.println("logger='"+logger.getName()+"'");
        // logger.setLevel(java.util.logging.Level.SEVERE);
        // logger=logger.getParent();
        // }
        // }

    }

    public static void initURLStreamFactory()
    {
        // todo: check security context!
        synchronized (LoboBrowserInit.class)
        {
            // never initialize twice !
            if (globalURLStreamFactoryInitialized == true)
            {
                return;
            }

            // After this method, the URL class excepts VRLs as valid URL !
            java.net.URL.setURLStreamHandlerFactory(VRLStreamHandlerFactory.getDefault());

            globalURLStreamFactoryInitialized = true;
        }
    }

}
