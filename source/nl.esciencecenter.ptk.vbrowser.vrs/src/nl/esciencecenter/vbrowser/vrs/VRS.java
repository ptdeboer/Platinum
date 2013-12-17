package nl.esciencecenter.vbrowser.vrs;

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
        
    public static Registry getRegistry()
    {
        return Registry.getInstance(); 
    }
    
    public static VRSClient createVRSClient()
    {
         return new VRSClient(new VRSContext(null));    
    }

    public static VRSContext createVRSContext(Properties properties)
    {
        return new VRSContext(properties); 
    }
    
    public static VRSClient createVRSClient(Properties properties)
    {
         return new VRSClient(new VRSContext(properties));    
    }

}
