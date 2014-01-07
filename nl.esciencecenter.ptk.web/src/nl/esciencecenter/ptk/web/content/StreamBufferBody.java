package nl.esciencecenter.ptk.web.content;
//package nl.esciencecenter.ptk.web;
//
//import java.io.IOException;
//import java.io.OutputStream;
//
//import org.apache.http.entity.mime.MIME;
//import org.apache.http.entity.mime.content.AbstractContentBody;
//
///** 
// * Under Construction:  
// */
//public class StreamBufferBody  extends AbstractContentBody 
//{
//    private long totalWritten=0;
//
//    private PutMonitor putMonitor=null;
//    
//    private int defaultChunkSize=4096; 
//    
//    public StreamBufferBody(final String mimeType, PutMonitor putMonitor) 
//    {
//        super(mimeType);
//        this.putMonitor=putMonitor; 
//    }
//
//    @Override
//    public void writeTo(final OutputStream out) throws IOException 
//    {
//        //Todo: Copy content written by custom OutputStream to the actual upload Stream. 
//    }
//
//    public String getTransferEncoding()
//    {
//        return MIME.ENC_BINARY;
//    }
//
//    public String getCharset() 
//    {
//        return null;
//    }
//
//    public long getContentLength() 
//    {
//        return -1; 
//    }
//    
//    public String getFilename()
//    {
//        return "stream"; 
//    }
//    
//    public long getProgress()
//    {
//        return totalWritten; 
//    }
//    
//    protected void logPrintf(String format,Object... args)
//    {
//        // Delegate to monitor: 
//    }
//    
//}
//
