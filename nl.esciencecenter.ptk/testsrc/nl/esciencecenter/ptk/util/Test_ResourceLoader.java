package nl.esciencecenter.ptk.util;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;

import nl.esciencecenter.ptk.io.FSNode;
import nl.esciencecenter.ptk.net.URIFactory;

import org.junit.Assert;
import org.junit.Test;

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

    /** 
     * Create test file using java.io.File interface. Do not use FSUtil nor ResourceLoader. 
     */
    public String createTestFile(String name,String content) throws Exception
    {
        // Use plain old Java File interface, can not use ResourceLoader/FSUtil here.  
        
        FSNode dir=FSUtil_getCreateTestDir();
        java.io.File file=new java.io.File(dir.getPathname()+"/"+name);  
        
        java.io.RandomAccessFile randomFile=new RandomAccessFile(file,"rw"); 
        randomFile.write(content.getBytes("UTF-8")); 
        randomFile.close(); 
        return file.getCanonicalPath(); 
    }

    /** 
     * Read test file using java.io.File interface. Do not use FSUtil nor ResourceLoader. 
     */
    public String readTestFile(String filePath) throws Exception
    {
        java.io.File file=new java.io.File(filePath);  
        int size=(int)file.length(); 
        byte bytes[]=new byte[(int)size]; 
        
        java.io.RandomAccessFile randomFile=new RandomAccessFile(file,"r"); 
        randomFile.read(bytes,0,size); 
        randomFile.close(); 
        
        return new String(bytes,"UTF-8"); 
    }
    /**
     * Create resource loader for testing with the testDir URL as single URL to resolve to. 
     */
    public ResourceLoader createTestDirResourceLoader() throws MalformedURLException, Exception
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
        String subDirname=testDir.getBasename(); 
        FSNode parentDir=testDir.getParent();

        
        ResourceLoader loader=new ResourceLoader(null,new java.net.URL[]{parentDir.getURL()}); 
        java.net.URL url=loader.resolveUrl(subDirname);
        Assert.assertNotNull("Got NULL URL. Failed to resolve URL:"+subDirname,url);
        Assert.assertEquals("Resolved path from URL must match File Path\n", testDir.getPathname(),url.getPath());
        
    }
    
    @Test 
    public void testResolveFile() throws Exception
    {
        FSNode testDir= FSUtil_getCreateTestDir();
        String fileName="subFile"; 
        FSNode file=testDir.createFile(fileName); 
        
        ResourceLoader loader=new ResourceLoader(null,new java.net.URL[]{testDir.getURL()}); 
        java.net.URL url=loader.resolveUrl(fileName);
        Assert.assertNotNull("Got NULL URL. Failed to resolve URL:"+fileName,url);
        Assert.assertEquals("Resolved path from URL must match File Path\n", file.getPathname(),url.getPath());
        
    }
    
    @Test
    public void test_readText() throws Exception
    {
        test_readText("testReadText","12345a"); 
        test_readText("test ReadText","12345b");
        test_readText("test&ReadText","12345c");
        test_readText("test~ReadText","12345d");
        test_readText("testReadDosText~1","12345e");
        test_readText("test%ReadText","12345f");
    }
    
    protected void test_readText(String pathName,String contents) throws Exception
    {
        String localPath=createTestFile(pathName,contents);
        // normalize as this is used in URIs: 
        String actualUriPath=URIFactory.uripath(localPath, true, File.separatorChar); 
        
        System.out.printf("pathName => actualPath ='%s' => '%s'\n",pathName,actualUriPath); 
              
        java.net.URL url=new java.net.URL("file:"+localPath); 
        Assert.assertNotNull("Got NULL URL. Failed to resolve URL:"+pathName+" (actualPath="+localPath,url);
        // read from URL
        String readBack=new ResourceLoader().readText(url);
        Assert.assertEquals("Read back contents should be the same", contents,readBack); 
        // read from URI
        java.net.URI uri=new java.net.URI("file",null,actualUriPath, null,null);
        String readBackUri=new ResourceLoader().readText(uri);
        Assert.assertEquals("Read back contents should be the same", contents,readBackUri); 
    
    }   
    
    @Test
    public void test_writeText() throws Exception
    {
        test_writeText("testReadText","12345a"); 
        test_writeText("test ReadText","12345b");
        test_writeText("test&ReadText","12345c");
        test_writeText("test~ReadText","12345d");
        test_writeText("testReadDosText~1","12345e");
        test_writeText("test%ReadText","12345f");
    }
    
    protected void test_writeText(String subPath,String contents) throws Exception
    {
        FSNode node=FSUtil_getCreateTestDir(); 
        FSNode fileNode=node.resolvePath(subPath); 
        java.net.URL fileUrl=fileNode.getURL(); 
        String localPath=fileNode.getPathname(); 
        String actualUriPath=URIFactory.uripath(localPath, true, File.separatorChar); 
        
        // write to URL
        ResourceLoader loader=createTestDirResourceLoader();
        java.net.URL resolvedUrl=loader.resolveUrl(subPath); 
        Assert.assertNotNull("Got NULL URL. Failed to resolve URL:"+subPath+" (actualPath="+fileNode.getPathname(),resolvedUrl);
        loader.writeTextTo(resolvedUrl, contents, "UTF-8");
        
        // readback
        String readBack=this.readTestFile(fileNode.getPathname()); 
        Assert.assertEquals("Read back contents should be the same", contents,readBack); 
        
        // write to URI
        String uriPath=actualUriPath+"a"; 
        java.net.URI uri=new java.net.URI("file",null,uriPath, null,null);
        loader.writeTextTo(uri, contents, "UTF-8");
        // readback 
        String readBackUri=this.readTestFile(uriPath); 
        Assert.assertEquals("Read back contents should be the same", contents,readBackUri); 
            

    }
}
