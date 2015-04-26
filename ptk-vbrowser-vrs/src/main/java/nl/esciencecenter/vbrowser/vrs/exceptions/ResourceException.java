package nl.esciencecenter.vbrowser.vrs.exceptions;

import nl.esciencecenter.vbrowser.vrs.VPath;

public class ResourceException extends VrsException
{
    private static final long serialVersionUID = 197118582694653944L;

    protected VPath sourcePath; 
    
    public ResourceException(VPath sourcePath, String message, Throwable cause, String name)
    {
        super(message, cause, name);
    };

    public VPath getSource()
    {
        return sourcePath; 
    }
    
}
