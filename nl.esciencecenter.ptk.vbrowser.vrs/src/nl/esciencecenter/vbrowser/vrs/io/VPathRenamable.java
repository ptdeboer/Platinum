package nl.esciencecenter.vbrowser.vrs.io;

import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;

/** 
 * Virtual Paths which can renamed implement this interface.
 *  
 * @author Piter T. de Boer
 */
public interface VPathRenamable
{
    
    /**
     * Rename this (virtual) path to another (virtual path. 
     * @param other -Other VPath
     * @return other path if rename was succesfull.  
     */ 
    public VPath renameTo(VPath other) throws VrsException; 
    
}
