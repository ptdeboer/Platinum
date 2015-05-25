package nl.esciencecenter.vbrowser.vrs.mimetypes;

import org.junit.Assert;
import org.junit.Test;

public class TestMimeTypes {

    
    @Test
    public void testDefaultMimeTypes() {
       
        MimeTypes mimeTypes=new MimeTypes();
        
        assertMimeType(mimeTypes,"test.pdf","application/pdf");
        assertMimeType(mimeTypes,"test.txt","text/plain");
        assertMimeType(mimeTypes,"test.html","text/html");
        assertMimeType(mimeTypes,"test.gif","image/gif");
        assertMimeType(mimeTypes,"test.png","image/png");

    }
    
    protected void assertMimeType(MimeTypes mimeTypes,String filename, String expected) { 
        
        String mimeType=mimeTypes.getMimeType(filename);
        Assert.assertEquals("Expected mimetype doesn't match",expected,mimeType);
        
                
    }
    
}
