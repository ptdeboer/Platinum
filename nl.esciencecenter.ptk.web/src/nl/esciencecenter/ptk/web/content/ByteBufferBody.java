package nl.esciencecenter.ptk.web.content;

import java.io.IOException;
import java.io.OutputStream;

import nl.esciencecenter.ptk.web.PutMonitor;

import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.content.AbstractContentBody;

/**
 * Byte Buffer body which can be monitored during the upload. 
 */
public class ByteBufferBody extends AbstractContentBody
{
    private final byte[] data;
    
    private final String filename;

    private PutMonitor putMonitor;

    public ByteBufferBody(final byte[] data, final String mimeType, final String filename, PutMonitor optPutMonitor)
    {
        super(mimeType);
        if (data == null)
        {
            throw new IllegalArgumentException("byte[] may not be null");
        }
        this.data = data;
        this.filename = filename;
        this.putMonitor=optPutMonitor; 
    }

    public ByteBufferBody(final byte[] data, final String filename)
    {
        this(data, "application/octet-stream", filename,null);
    }

    public String getFilename()
    {
        return filename;
    }

    public void writeTo(final OutputStream out) throws IOException
    {
        int chunkSize=32*1024; // 32k chunks 
        
        int numWritten=0;
        while(numWritten<data.length)
        {
            int numToWrite=data.length-numWritten; 
            if (numToWrite>chunkSize)
            {
                numToWrite=chunkSize; 
            }
            
            out.write(data, (int)numWritten,(int)numToWrite);
            numWritten+=numToWrite; 
            if (putMonitor!=null)
            {
                putMonitor.bytesWritten(numWritten); 
            }
        }

        if (putMonitor!=null)
        {
            putMonitor.putDone(); 
        }
       
    }

    public String getCharset()
    {
        return null;
    }

    public String getTransferEncoding()
    {
        return MIME.ENC_BINARY;
    }

    public long getContentLength()
    {
        return data.length;
    }

}
