package nl.esciencecenter.vbrowser.vrs.localfs;

import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class LocalFSFileSystemFactory implements VResourceSystemFactory
{
    private LocalFileSystem localfs; 
    
    public LocalFSFileSystemFactory() throws VrsException
    {
        localfs=new LocalFileSystem(); 
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
    public VResourceSystem createResourceSystemFor(VRL vrl) throws VrsException
    {
        if ("file".equals(vrl.getScheme())==false)
        {
            throw new VrsException("Only support local file system URI:"+vrl);
        }
        
        return localfs;
    }

}
