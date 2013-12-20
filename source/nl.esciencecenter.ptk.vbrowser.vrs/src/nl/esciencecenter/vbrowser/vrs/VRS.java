package nl.esciencecenter.vbrowser.vrs;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import nl.esciencecenter.vbrowser.vrs.registry.Registry;

public class VRS
{
    public final static String FILE_SCHEME="file"; 
    public final static String HTTP_SCHEME="http"; 
    public final static String HTTPS_SCHEME="https"; 
    public final static String GFTP_SCHEME="gftp"; 
    public final static String GSIFTP_SCHEME="gsiftp";
    public final static String SFTP_SCHEME="sftp"; 
    public final static String SSH_SCHEME="ssh";
    public final static String SRB_SCHEME="srb";
    public final static String IRODS_SCHEME="irods";
        
    private static Map<String,Integer> defaultPorts; 
    
    static
    {
        staticInit(); 
    }
    
    private static void staticInit()
    {
        defaultPorts=new HashMap<String,Integer>(); 
        defaultPorts.put(FILE_SCHEME,0); 
        defaultPorts.put(HTTP_SCHEME,80); 
        defaultPorts.put(HTTPS_SCHEME,443); 
        defaultPorts.put(GFTP_SCHEME,2811); 
        defaultPorts.put(GSIFTP_SCHEME,2811); 
        defaultPorts.put(SFTP_SCHEME,22); 
        defaultPorts.put(SSH_SCHEME,22); 
        // defaultPorts.put(IRODS_SCHEME,0); 
    }
    
    public static Registry getRegistry()
    {
        return Registry.getInstance(); 
    }
    
    public static VRSClient createVRSClient()
    {
         return new VRSClient(new VRSContext());    
    }

    public static VRSContext createVRSContext(Properties properties)
    {
        return new VRSContext(properties); 
    }
    
    public static VRSClient createVRSClient(Properties properties)
    {
         return new VRSClient(new VRSContext(properties));    
    }

    public static int getDefaultPort(String scheme)
    {
        Integer value=defaultPorts.get(scheme); 
        if (value!=null)
            return value; 
        
        return -1; 
    }

}
