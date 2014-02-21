/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

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
