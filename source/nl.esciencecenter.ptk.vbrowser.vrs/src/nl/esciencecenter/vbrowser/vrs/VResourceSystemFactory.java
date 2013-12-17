package nl.esciencecenter.vbrowser.vrs;

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public interface VResourceSystemFactory
{
    public String[] getSchemes(); 

    public String createResourceSystemId(VRL vrl);
    
    public VResourceSystem createResourceSystemFor(VRL vrl) throws VrsException; 
 
    
}
