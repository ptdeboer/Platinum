package nl.esciencecenter.vbrowser.vrs.localfs;

import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class LocalFSFileSystemFactory implements VResourceSystemFactory
{
    public LocalFSFileSystemFactory() throws VrsException
    {
    }
    
    @Override
    
    public String[] getSchemes()
    {
        return new String[]{"file"}; 
    }

    @Override
    public String createResourceSystemId(VRL vrl)
    {
        // only one local fs. 
        return "localfs:0";  
    }

    @Override
    public VResourceSystem createResourceSystemFor(VRSContext context,ResourceSystemInfo info,VRL vrl) throws VrsException
    {
        if ("file".equals(vrl.getScheme())==false)
        {
            throw new VrsException("Only support local file system URI:"+vrl);
        }
        
        return new LocalFileSystem(context);
    }

    @Override
    public ResourceSystemInfo updateResourceInfo(VRSContext context,ResourceSystemInfo resourceSystemInfo, VRL vrl)
    {
        // Nothing to be updated. 
        return resourceSystemInfo; 
    }

}
