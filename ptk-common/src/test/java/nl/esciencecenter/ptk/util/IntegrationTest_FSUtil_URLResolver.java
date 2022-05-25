package nl.esciencecenter.ptk.util;

import nl.esciencecenter.ptk.io.FSPath;
import nl.esciencecenter.ptk.net.URIFactory;
import nl.esciencecenter.ptk.net.URIUtil;
import org.junit.Assert;
import org.junit.Test;
import settings.Settings;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.LinkOption;

/**
 * Test FSUtil with URLResolver to ensure consistency between created FSNodes and resolved URLs.<br>
 * To make sure URLs are normalized the Java recommended construction URI.toURL() is used to compare
 * URIs.
 */
public class IntegrationTest_FSUtil_URLResolver {
    protected Settings settings = Settings.getInstance();

    protected FSPath testDir = null;

    // =================
    // FSUtil methods
    // =================

    public FSPath FSUtil_getCreateTestDir() throws Exception {

        if (testDir == null) {
            testDir = settings.getFSUtil_testDir(true);
        }

        return testDir;
    }

    // ========================================================================
    // Actual Test Scenarios
    // ========================================================================

    @Test
    public void test_CreateAndResolve() throws Exception {
        FSPath baseDir = FSUtil_getCreateTestDir();
        test_CreateAndResolve(baseDir);

        FSPath subDir = baseDir.resolve("sub dir");
        if (subDir.exists()) {
            outPrintf("Warning:test directory already exists:%s\n", subDir);
        } else {
            settings.getFSUtil().mkdir(subDir);
        }

        Assert.assertTrue("Sub-directory must exists:" + subDir, subDir.exists());

        test_CreateAndResolve(subDir);

        subDir.delete(LinkOption.NOFOLLOW_LINKS);

        Assert.assertFalse("After deletion, sub-directory may not exit anymore:" + subDir, subDir.exists());

    }

    public void test_CreateAndResolve(FSPath baseDir) throws Exception {
        boolean isWindows = settings.isWindows();

        testCreateResolve(baseDir, "file1", true, true);
        testCreateResolve(baseDir, "file2 space", true, true);
        testCreateResolve(baseDir, " prefixSpaced3", true, true);

        if (isWindows == false) {
            // postfix spaced not allowed under windows... 
            testCreateResolve(baseDir, "postfixSpaced4 ", true, true);
        }

        FSPath subDir = settings.getFSUtil().mkdir(baseDir.resolve("subDir1"));
        testCreateResolve(baseDir, "subDir1/subFile5", true, true);

        FSPath subDirSpaced = settings.getFSUtil().mkdir(baseDir.resolve("subDir Spaced6"));
        testCreateResolve(baseDir, "subDir Spaced6/subFile7", true, true);
        testCreateResolve(baseDir, "subDir Spaced6/subFile Spaced8", true, true);
        if (isWindows) {
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

    protected void testCreateResolve(FSPath baseDir, String relativePath, boolean isFilePath, boolean create)
            throws Exception {
        //
        // I) Create file first. URLs must always point to existing files.
        // 
        FSPath filePath = baseDir.resolve(relativePath);
        if (filePath.exists() == false) {
            if (isFilePath) {
                filePath.create();
            } else {
                settings.getFSUtil().mkdir(filePath);
            }
        }

        //
        // Use normalized URI from FSNode as reference here and not the URL.
        //
        URI normalizedFSNodeURI = normalize(filePath.toURI());

        //
        // II) Manually create absolute and encoded URI 
        //
        String baseUrlStr = baseDir.getPathname();
        // avoid double slashes here.
        if (baseUrlStr.endsWith("/")) {
            baseUrlStr = baseUrlStr.substring(0, baseUrlStr.length() - 1);
        }

        URI baseUri = normalize(new URI("file:" + URIFactory.encodePath(baseUrlStr)));
        URI expectedUri = normalize(URIUtil.resolvePathURI(baseUri, relativePath)); // use URI resolve as standard here!

        Assert.assertEquals(
                "Pre URLSolver: File URI from FSNode doesn't match expected URI from URIUtil.resolvePathURI():",
                expectedUri, normalizedFSNodeURI);

        //
        // III) test URLResolver with specified URL base path:
        //

        // -------------
        // POST: cleanup
        // -------------
        filePath.delete();
        Assert.assertFalse("After deleting, test file may not exist:" + filePath, filePath.exists());

    }

    protected URI normalize(URI orgUri) throws URISyntaxException {
        URI uri = orgUri.normalize();
        URI newUri = uri;
        String auth = uri.getAuthority();
        String scheme = uri.getScheme();

        String path = uri.getPath();
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        //normalize 'file:/' and 'file:///' which are equivalent but not equal. To get the file:/// in an URI
        // recreated it with an empty auth part. 
        newUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path, uri.getQuery(),
                uri.getFragment());

        return newUri;
    }

    public static void outPrintf(String format, Object... args) {
        System.out.printf(format, args);
    }

}
