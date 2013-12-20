package nl.esciencecenter.vbrowser.vrs.io;

import java.io.InputStream;
import java.io.OutputStream;

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;

public interface VStreamWritable
{
    
    public OutputStream createOutputStream() throws VrsException; 
    
}
