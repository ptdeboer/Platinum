package nl.esciencecenter.vbrowser.vrs.sftp;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.ptk.crypt.Secret;
import nl.esciencecenter.ptk.exec.ChannelOptions;
import nl.esciencecenter.ptk.exec.ShellChannel;
import nl.esciencecenter.ptk.exec.ShellChannelFactory;
import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.io.VShellChannelCreator;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class SSHShellChannelFactory implements ShellChannelFactory
{
    private final static Logger logger=LoggerFactory.getLogger(SSHShellChannelFactory.class); 
    
    protected VRSClient vrsClient;
    
    public SSHShellChannelFactory(VRSContext context)
    {
        this.vrsClient=new VRSClient(context); 
    }

    public ShellChannel createChannel(java.net.URI uri, String user, Secret password, ChannelOptions options) throws IOException
    {
        try
        {
            VRL vrl=new VRL(uri);
            VResourceSystem vrs  = this.vrsClient.getVResourceSystemFor(vrl);
            
            logger.error(">>> Found vrs:{}", vrs);
            
            if (vrs instanceof VShellChannelCreator) { 
                return ((VShellChannelCreator)vrs).createShellChannel(vrl);
            }
            else {
                throw new IOException("ShellChannel not supported for resource:"+uri);
            }
        }
        catch (VrsException e)
        {
            throw new IOException("Failed to createShellChannel to:"+uri,e); 
        } 
    }
    
    
}
