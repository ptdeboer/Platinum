package nl.esciencecenter.vbrowser.vrs.io;

import java.io.InputStream;
import java.io.OutputStream;

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsIOException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public interface VInputStreamCreator
{
    public InputStream createInputStream(VRL vrl) throws VrsException;
    
}
