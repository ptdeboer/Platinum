package nl.esciencecenter.vbrowser.vrs.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.ptk.io.RandomReader;
import nl.esciencecenter.ptk.io.RandomWriter;
import nl.esciencecenter.ptk.io.ResourceProvider;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VRSResourceProvider implements ResourceProvider
{
    protected VRSClient vrsClient; 
    
    public VRSResourceProvider(VRSClient vrsClient)
    {
        this.vrsClient=vrsClient; 
    }

    @Override
    public URI resolvePathURI(String path) throws URISyntaxException
    {
        try
        {
            return vrsClient.resolvePath(path).toURI();
        }
        catch (VRLSyntaxException e)
        {
            throw new URISyntaxException(path,e.getMessage());
        } 
    }

    @Override
    public OutputStream createOutputStream(URI uri) throws IOException
    {
        try
        {
            return vrsClient.createOutputStream(new VRL(uri));
        }
        catch (VrsException e)
        {
            throw new IOException(e.getMessage(),e);
        }
    }

    @Override
    public InputStream createInputStream(URI uri) throws IOException
    {
        try
        {
            return vrsClient.createInputStream(new VRL(uri));
        }
        catch (VrsException e)
        {
            throw new IOException(e.getMessage(),e);
        }
    }

    @Override
    public RandomReader createRandomReader(URI uri) throws IOException
    {
        throw new IOException("not implemented: createRandomReader():"+uri); 
    }

    @Override
    public RandomWriter createRandomWriter(URI uri) throws IOException
    {
        throw new IOException("not implemented: createRandomWriter():"+uri);
    }

}
