//package nl.esciencecenter.ptk.util;
//
//import java.io.File;
//import java.io.RandomAccessFile;
//
//import nl.esciencecenter.ptk.io.FSPath;
//import nl.esciencecenter.ptk.net.URIFactory;
//import nl.esciencecenter.ptk.util.logging.PLogger;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import settings.Settings;
//
//public class Test_ResourceLoader
//{
//	static
//	{
//		PLogger.getLogger(ResourceLoader.class).setLevelToInfo(); 
//	}
//	
//	private static void outPrintf(String format,Object... args)
//	{
//		System.out.printf(format,args); 
//	}
//	
//    protected Settings settings = Settings.getInstance();
//
//    protected FSPath testDir = null;
//
//    // =================
//    // FSUtil methods
//    // =================
//
//    public FSPath FSUtil_getCreateTestDir() throws Exception
//    {
//        
//        if (testDir == null)
//        {
//            testDir=settings.getFSUtil_testDir(true); 
//        }
//        
//        return testDir;
//    }
//
//    /** 
//     * Create test file using java.io.File interface. Do not use FSUtil nor ResourceLoader. 
//     */
//    public String createTestFile(String name,String content) throws Exception
//    {
//        // Use plain old Java File interface, can not use ResourceLoader/FSUtil here.  
//        
//        FSPath dir=FSUtil_getCreateTestDir();
//        java.io.File file=new java.io.File(dir.getPathname()+"/"+name);  
//        
//        java.io.RandomAccessFile randomFile=new RandomAccessFile(file,"rw"); 
//        randomFile.write(content.getBytes("UTF-8")); 
//        randomFile.close(); 
//        return file.getCanonicalPath(); 
//    }
//
//    /** 
//     * Read test file using java.io.File interface. Do not use FSUtil nor ResourceLoader. 
//     */
//    public String readTestFile(String filePath) throws Exception
//    {
//        java.io.File file=new java.io.File(filePath);  
//        int size=(int)file.length(); 
//        byte bytes[]=new byte[(int)size]; 
//        
//        java.io.RandomAccessFile randomFile=new RandomAccessFile(file,"r"); 
//        randomFile.read(bytes,0,size); 
//        randomFile.close(); 
//        
//        return new String(bytes,"UTF-8"); 
//    }
//    
//    // ========================================================================
//    // Tests  
//    // ========================================================================   
//    
//    // ResourceLoader must be able to resolve the test dir. 
//    @Test 
//    public void testResolveTestDir() throws Exception
//    {
//        FSPath testDir= FSUtil_getCreateTestDir();
//        String subDirname=testDir.getBasename(); 
//        FSPath parentDir=testDir.getParent();
//        
//        java.net.URL baseUrl=parentDir.toURI().toURL(); 
//        java.net.URL webUrl=parentDir.getURL(); 
//        
//        System.err.printf(" baseUrl = '%s'\n",baseUrl); 
//        System.err.printf(" webUrl  = '%s'\n",webUrl); 
//        
//        ResourceLoader loader=new ResourceLoader(null,new java.net.URL[]{baseUrl}); 
//        java.net.URL url=loader.resolveUrl(subDirname);
//        Assert.assertNotNull("Got NULL URL. Failed to resolve URL:"+subDirname,url);
//        Assert.assertEquals("Resolved path from URL must match File Path\n", testDir.getPathname(),url.getPath());
//        
//    }
//    
//    @Test 
//    public void testResolveFile() throws Exception
//    {
//        FSPath testDir= FSUtil_getCreateTestDir();
//        String fileName="subFile"; 
//        FSPath file=testDir.resolve(fileName).create();
//        
//        ResourceLoader loader=new ResourceLoader(null,new java.net.URL[]{testDir.getURL()}); 
//        java.net.URL url=loader.resolveUrl(fileName);
//        Assert.assertNotNull("Got NULL URL. Failed to resolve URL:"+fileName,url);
//        Assert.assertEquals("Resolved path from URL must match File Path\n", file.getPathname(),url.getPath());
//        
//    }
//    
//    @Test
//    public void test_readText() throws Exception
//    {
//        test_readText("testReadText","12345a"); 
//        test_readText("test ReadText","12345b");
//        test_readText("test&ReadText","12345c");
//        test_readText("test~ReadText","12345d");
//        test_readText("testReadDosText~1","12345e");
//        test_readText("test%ReadText","12345f");
//    }
//    
//    protected void test_readText(String pathName,String contents) throws Exception
//    {
//        String localPath=createTestFile(pathName,contents);
//        // normalize as this is used in URIs: 
//        String actualUriPath=URIFactory.uripath(localPath, true, File.separatorChar); 
//        
//        outPrintf("pathName => actualPath ='%s' => '%s'\n",pathName,actualUriPath); 
//              
//        java.net.URL url=new java.net.URL("file:"+localPath); 
//        Assert.assertNotNull("Got NULL URL. Failed to resolve URL:"+pathName+" (actualPath="+localPath,url);
//        // read from URL
////        String readBack=new ResourceLoader().readText(url);
////        Assert.assertEquals("Read back contents should be the same", contents,readBack); 
//        // read from URI
////        java.net.URI uri=new java.net.URI("file",null,actualUriPath, null,null);
////        String readBackUri=new ResourceLoader().readText(uri);
////        Assert.assertEquals("Read back contents should be the same", contents,readBackUri); 
//    
//    }   
//    
//    @Test
//    public void test_writeText() throws Exception
//    {
//        test_writeText("testRWText","12345a"); 
//        test_writeText("test RWText","12345b");
//        test_writeText("test&RWText","12345c");
//        test_writeText("test~RWText","12345d");
//        test_writeText("testRWDosText~1","12345e");
//        test_writeText("test%RWText","12345f");
//    }
//    
//    protected void test_writeText(String subPath,String contents) throws Exception
//    {
//        FSPath node=FSUtil_getCreateTestDir(); 
//        FSPath fileNode=node.resolve(subPath);
//        // use FS util to create normalized URL 
//        java.net.URL fileUrl=fileNode.getURL(); 
//        String localPath=fileNode.getPathname(); 
//        String actualUriPath=URIFactory.uripath(localPath, true, File.separatorChar); 
//        
//        // URL Loaders need explicit directory URL where the path ends with a slash.  
//        java.net.URL baseUrl=FSUtil_getCreateTestDir().getURL();
//        ResourceLoader loader= new ResourceLoader(null,new java.net.URL[]{baseUrl});
//        
////        loader.writeTextTo(fileUrl, contents, "UTF-8");
////        outPrintf("written text to: %s\n",fileUrl); 
//        
//        // after write, file exists and can be resolved: 
//        java.net.URL resolvedUrl=loader.resolveUrl(subPath);
//        Assert.assertNotNull("Got NULL URL. Failed to resolve URL [baseUrl,subPath] = ['"+baseUrl+"','"+subPath+"'] (actualPath="+fileNode.getPathname(),resolvedUrl);
//        
//        // readback
//        String readBack=this.readTestFile(fileNode.getPathname()); 
//        Assert.assertEquals("Read back contents should be the same", contents,readBack); 
//        
////        // write to URI
////        String uriPath=actualUriPath+"a"; 
////        java.net.URI uri=new java.net.URI("file",null,uriPath, null,null);
////        loader.writeTextTo(uri, contents, "UTF-8");
////        // readback 
////        String readBackUri=this.readTestFile(uriPath); 
////        Assert.assertEquals("Read back contents should be the same", contents,readBackUri); 
//            
//
//    }
//}
