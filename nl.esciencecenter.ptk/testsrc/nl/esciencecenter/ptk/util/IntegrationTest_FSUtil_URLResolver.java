package nl.esciencecenter.ptk.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import nl.esciencecenter.ptk.io.FSNode;
import nl.esciencecenter.ptk.io.FSUtil;
import nl.esciencecenter.ptk.net.URIUtil;
import nl.esciencecenter.ptk.util.ResourceLoader.URLResolver;

import org.junit.Assert;
import org.junit.Test;

import settings.Settings;

/**
 * Combined use of FSUtil with URLResolver to ensure consistency between created FSNodes and resolved URLs.<br>
 * To make sure URLs are normalized the Java recommended construction URI.toURL() is used to compare URLs.
 */
public class IntegrationTest_FSUtil_URLResolver
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
            String testDirstr = settings.getLocalTestDir() + "/urlResolver";
            testDir = FSUtil.getDefault().newFSNode(testDirstr);

            if (testDir.exists() == false)
            {
                testDir.mkdir();
                Assert.assertTrue("Test dir must exist:" + testDir, testDir.exists());
            }
        }

        return testDir;
    }

    // ========================================================================
    // Actual Test Scenarios
    // ========================================================================

    @Test
    public void test_CreateAndResolve() throws Exception
    {
        FSNode baseDir = FSUtil_getCreateTestDir();
        test_CreateAndResolve(baseDir); 
    }
    
//    @Test
//    public void test_CreateAndResolveCDrive() throws Exception
//    {
//        FSNode baseDir = FSUtil.getDefault().newFSNode("file:/C:/");
//        test_CreateAndResolve(baseDir); 
//    }
    
    public void test_CreateAndResolve(FSNode baseDir) throws Exception
    {
        boolean isWindows=settings.isWindows(); 
        

        testCreateResolve(baseDir, "file1", true, true);
        testCreateResolve(baseDir, "file2 space", true, true);
        testCreateResolve(baseDir, " prefixSpaced3", true, true);
        
        if (isWindows==false)
        {
        	// postfix spaced not allowed under windows... 
        	testCreateResolve(baseDir, "postfixSpaced4 ", true, true);
        }
        
        FSNode subDir = baseDir.resolvePath("subDir1").mkdir();
        testCreateResolve(baseDir, "subDir1/subFile5", true, true);

        FSNode subDirSpaced = baseDir.resolvePath("subDir Spaced6").mkdir();
        testCreateResolve(baseDir, "subDir Spaced6/subFile7", true, true);
        testCreateResolve(baseDir, "subDir Spaced6/subFile Spaced8", true, true);
        if (isWindows)
        {
            // check backslash here. Backslashes should be auto converted to URL/URI forward slash. 
            testCreateResolve(baseDir, "subDir Spaced6\\File9", true, true);
            testCreateResolve(baseDir, "subDir Spaced6\\Spaced File10", true, true);
        }
        
        subDir.delete();
        subDirSpaced.delete();

        // -------------------
        // special characters
        // -------------------

        testCreateResolve(baseDir, "infix%special1", true, true);
        testCreateResolve(baseDir, "infix&special2", true, true);
        testCreateResolve(baseDir, "postfixSpecial3~", true, true);
        testCreateResolve(baseDir, "postfixSpecial4~1", true, true);

        // -------------------
        // Windows shares and paths 
        // -------------------

        testCreateResolve(baseDir, "WindowsShare$", false, true);

    }

    protected void testCreateResolve(FSNode baseDir, String relativePath, boolean isFilePath, boolean create) throws Exception
    {
        //
        // I) Create file first. URLs must always point to existing files.
        // 
        FSNode filePath = baseDir.resolvePath(relativePath);
        if (filePath.exists() == false)
        {
            if (isFilePath)
            {
                filePath.create();
            }
            else
            {
                filePath.mkdir(); 
            }
        }

        //
        // Use normalized URI from FSNode as reference here and not the URL.
        //
        URI normalizedFSNodeURI = normalize(filePath.getURI()); 
        
        //
        // II) Resolve URL manually using absolute path:
        //
        String baseUrlStr = baseDir.getPathname();
        // avoid double slashes here.
        if (baseUrlStr.endsWith("/"))
        {
            baseUrlStr = baseUrlStr.substring(0, baseUrlStr.length() - 1);
        }

        URI baseUri = normalize(new URI("file:" + baseUrlStr)); 
        URI expectedUri = normalize(URIUtil.resolvePathURI(baseUri, relativePath)); // use URI resolve as standard here!

        Assert.assertEquals("Pre URLSolver: File URI from FSNode doesn't match expected URI from URIUtil.resolvePathURI():", expectedUri, normalizedFSNodeURI);

        //
        // III) test URLResolver with specified URL base path:
        //
        
        URL urls[] = new URL[] { baseUri.toURL() };
        URLResolver resolver = new URLResolver(null, urls);
        URL resolvedUrl = resolver.resolveUrlPath(relativePath);

        // URLs only works when file actually exists: 
        Assert.assertNotNull("URLResolver couldn't resolve URL. Has the file been created ? [base,relativePath]=[" + baseUri + ","
                + relativePath + "]", resolvedUrl);

        //
        // Check decoded URL paths here only as URI and URL may have different authentication parts.
        //
        Assert.assertEquals("Resulted URL from URLResolver doesn't match expected", expectedUri.toURL().getPath(), resolvedUrl.getPath());
        Assert.assertEquals("File URL from FSNode doesn't match resolved URL", normalizedFSNodeURI.toURL().getPath(), resolvedUrl.getPath());

        String decodedPath=resolvedUrl.getPath(); 
        outPrintf("resolveURL[baseUrl,relativePath]=['%s','%s']=>%s (Decoded path='%s')\n", baseUri, relativePath, resolvedUrl,decodedPath);
        
        // -------------
        // POST: cleanup
        // -------------
        filePath.delete();
        Assert.assertFalse("After deleting, test file may not exist:" + filePath, filePath.exists());

    }


    protected URI normalize(URI orgUri) throws URISyntaxException
    {
        URI uri=orgUri.normalize();
        URI newUri=uri; 
        String auth=uri.getAuthority(); 
        String scheme=uri.getScheme(); 
        
        String path=uri.getPath(); 
        if (path.endsWith("/"))
        {
            path=path.substring(0, path.length()-1);
        }
        
        //normalize 'file:/' and 'file:///' which are equivalent but not equal. To get the file:/// in an URI
        // recreated it with an empty auth part. 
        newUri=new URI(uri.getScheme(),uri.getUserInfo(),uri.getHost(),uri.getPort(),path,uri.getQuery(),uri.getFragment()); 
        

        return newUri;
    }

    public static void outPrintf(String format, Object... args)
    {
        System.out.printf(format, args);
    }

}
