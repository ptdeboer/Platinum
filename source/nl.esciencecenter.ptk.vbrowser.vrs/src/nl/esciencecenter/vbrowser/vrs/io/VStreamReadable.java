package nl.esciencecenter.vbrowser.vrs.io;

import java.io.InputStream;

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;

public interface VStreamReadable
{
    
    public InputStream createInputStream() throws VrsException; 
    
}
