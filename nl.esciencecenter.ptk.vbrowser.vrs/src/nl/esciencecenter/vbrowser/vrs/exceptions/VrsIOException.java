package nl.esciencecenter.vbrowser.vrs.exceptions;

import java.io.IOException;

public class VrsIOException extends VrsException 
{
    private static final long serialVersionUID = -789106405456659909L;

    public VrsIOException(IOException e)
    {
        super(e.getMessage(),e); 
    }
    
}
