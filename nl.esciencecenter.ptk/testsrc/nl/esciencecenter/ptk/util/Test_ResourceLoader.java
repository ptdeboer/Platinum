package nl.esciencecenter.ptk.util;

import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;

import nl.esciencecenter.ptk.io.FSNode;
import settings.Settings;

public class Test_ResourceLoader
{
    
    
    protected Settings settings = Settings.getInstance();

    protected FSNode testDir = null;

    // =================
    // FSUtil methods
    // =================

    public FSNode FSUtil_getCreateTestDir() throws Exception
    {
        
        if (testDir == null)
        {
            testDir=settings.getFSUtil_testDir(true); 
        }
        
        return testDir;
    }
    
    public String createTestFile(String name,String content) throws Exception
    {
        // Use plain old Java File interface, can not use ResouceLoader/FSUtil here.  
        
        FSNode dir=FSUtil_getCreateTestDir();
        
        java.io.File file=new java.io.File(dir.getPathname()+"/"+name);  
        
        java.io.RandomAccessFile randomFile=new RandomAccessFile(file,"rw"); 
        randomFile.write(content.getBytes(Charset.defaultCharset())); 
        randomFile.close(); 
        return file.getCanonicalPath(); 
    }
    
    public ResourceLoader createResourceLoader() throws MalformedURLException, Exception
    {
        // use test Dir to resolve To. 
        java.net.URL baseUrl=FSUtil_getCreateTestDir().getURL();
        return new ResourceLoader(null,new java.net.URL[]{baseUrl}); 
    }

    
    // ========================================================================
    // Tests  
    // ======================================================================== 
    
    
    // ResourceLoader must be able to resolve the test dir. 
    @Test 
    public void testResolveTestDir() throws Exception
    {
        FSNode testDir= FSUtil_getCreateTestDir();
        String subDir=testDir.getBasename(); 
        
        String dirname=testDir.getDirname();
        // create base URL using parent directory 
        ResourceLoader loader=new ResourceLoader(null,new java.net.URL[]{new java.net.URL("file:"+dirname)}); 
          
        java.net.URL url=loader.resolveUrl(subDir);
        Assert.assertNotNull("Got NULL URL. Failed to resolve URL:"+subDir,url);
        Assert.assertEquals("Resolved path from URL must match File Path\n", testDir.getPathname(),url.getPath());
    }
    
    @Test
    public void readText() throws Exception
    {
        test_readText("testReadText","12345"); 
        test_readText("test ReadText","12345");
        test_readText("C:/test/Test ReadText","12345");
    }
    
    protected void test_readText(String pathName,String contents) throws Exception
    {
        String actualPath=createTestFile(pathName,contents);
        
        ResourceLoader loader=createResourceLoader();
        java.net.URL url=new java.net.URL("file:"+actualPath); 
        Assert.assertNotNull("Got NULL URL. Failed to resolve URL:"+pathName+" (actualPath="+actualPath,url);
        
        String readBack=new ResourceLoader().readText(url);
        Assert.assertEquals("Read back contents should be the same", contents,readBack); 
    }
    
}
