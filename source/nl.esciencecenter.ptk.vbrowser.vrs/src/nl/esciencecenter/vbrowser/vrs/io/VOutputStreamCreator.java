package nl.esciencecenter.vbrowser.vrs.io;

import java.io.OutputStream;

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsIOException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public interface VOutputStreamCreator
{
    
    public OutputStream createOutputStream(VRL vrl) throws VrsIOException, VrsException;  
    
}
