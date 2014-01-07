package nl.esciencecenter.vbrowser.vrs;

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public interface VResourceSystem
{
    public VRL getServerVRL();
    
    public VRL resolveVRL(String path) throws VrsException; 
    
    public VPath resolvePath(String path) throws VrsException; 
    
    public VPath resolvePath(VRL vrl) throws VrsException; 
    
}
