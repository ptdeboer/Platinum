package nl.esciencecenter.ptk.web;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import nl.esciencecenter.ptk.object.Disposable;
import org.apache.http.client.methods.HttpPut;

/** 
 * Managed ResponseOutputStream. 
 * Under construction. 
 */
public class ResponseOutputStream extends OutputStream implements WebStream, Disposable, AutoCloseable
{
    protected WebClient webClient;
    protected HttpPut putMethod; 
    protected OutputStream sourceStream;
    private URI uri;
    
    public ResponseOutputStream(WebClient client,HttpPut putMethod) throws IllegalStateException, IOException
    {
        this.webClient=client;
        this.putMethod=putMethod; 
        this.uri=putMethod.getURI(); 
    }

    @Override
    public void write(int b) throws IOException
    {
        sourceStream.write(b); 
    }

    @Override
    public void write(byte bytes[],int offset,int numBytes) throws IOException
    {
        sourceStream.write(bytes,offset,numBytes); 
    }

    @Override
    public void flush() throws IOException
    {
        sourceStream.flush(); 
    }

    /** 
     * Close the underlying InputStream. 
     * If the InputStream was already closed or an IOException occure this method will return false. 
     * If the close was successful the method return true
     * @returns - true if the close was successful, false if the stream was already close or an Exception occured.
     */
    public boolean autoClose()
    {
        if ((this.sourceStream==null) && (putMethod==null)) 
        {
            return false;
        }
                
        try
        {
            close(); 
            //webClient.getLogger().debugPrintf("autoClose(): successful for:"+this);
            return true; 
        }
        catch (IOException e)
        {
            return false;
        }
    }
    
    public void dispose()
    {
        autoClose(); 
    }
    
    public void finalize()
    {
        autoClose();
    }
    
    public String toString()
    {
        return "<ResponseOutputStream:> open="+(sourceStream==null?"open":"close")+", uri="+uri;
    }

}
