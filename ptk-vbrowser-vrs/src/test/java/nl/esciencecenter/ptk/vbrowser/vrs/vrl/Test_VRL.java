/*
 * Copyrighted 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").  
 * You may not use this file except in compliance with the License. 
 * For details, see the LICENCE.txt file location in the root directory of this 
 * distribution or obtain the Apache License at the following location: 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
 * ---
 */
// source: 

package nl.esciencecenter.ptk.vbrowser.vrs.vrl;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.net.URIFactory;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Test VRL.
 */
public class Test_VRL {

    @Test
    public void testBasename() throws VRLSyntaxException {
        VRL vrl = new VRL("file://localhost/dirname/basename.ext");

        Assert.assertEquals("Basename (with extension) doesn't match.", "basename.ext",
                vrl.getBasename(true));
        Assert.assertEquals("Basename (without extension) doesn't match.", "basename",
                vrl.getBasename(false));
        Assert.assertEquals("Extension part doesn't match.", "ext", vrl.getExtension());
    }

    // VRL String must be normalized!
    private void testConstructor(String vrlStr) throws VRLSyntaxException {
        VRL newVRL = new VRL(vrlStr);
        Assert.assertEquals("create VRL does not match it's original string", vrlStr,
                newVRL.toString());
    }

    // VRL String must be normalized!
    private void testConstructor(String message, VRL vrl, String vrlStr) throws VRLSyntaxException {
        Assert.assertEquals(message, vrlStr, vrl.toString());
    }

    @Test
    public void testConstructors() throws Exception {
        // NOTE: use normalized URI strings here:
        testConstructor("file:/path");
        testConstructor("http://host:9909/path");
        testConstructor("https://server:1234/path?urlAttribute=value");

        // normalized: use decoded strings:
        testConstructor("gfile://HOST:1234/path");

        // normalized: use decoded strings:
        testConstructor("gfile://HOST:1234/A path");

        testConstructor("Constructor VRL(\"file\",null,\"/etc\") does not match", new VRL("file",
                null, "/etc"), "file:/etc");
        testConstructor("Constructor VRL(\"file\",null,\"/etc\") does not match", new VRL("file",
                null, "etc"), "file:etc");

        VRL local = new VRL("file", null, "dirname/etc");

        // if (local.toString().compareTo("file://localhost/etc")!=0)
        Assert.assertEquals("Constructor VRL(\"file\",null,\"/etc\") does not match",
                "file:dirname/etc", local.toString());

        local = new VRL("file", null, null);
        Assert.assertEquals("Constructor VRL(\"file\",null,null) does not match", "file:",
                local.toString());

        local = new VRL("file:///");
        Assert.assertEquals("local file VRL does not match", "file:/", local.toString());

        local = new VRL("file:/");
        Assert.assertEquals("local file VRL does not match", "file:/", local.toString());

        // test scheme with ":" appended
        local = new VRL("file:", null, "/etc");
        if (local.toString().compareTo("file://localhost/etc") != 0)
            Assert.assertEquals("added colon is not ignored.", "file:/etc", local.toString());

        // negative port must be filtered out.
        local = new VRL("file", null, -1, "/etc");
        if (local.toString().compareTo("file://localhost/etc") != 0)
            Assert.assertEquals("Negative port number is not ignored.", "file:/etc",
                    local.toString());

        // zero port must be filtered out.
        local = new VRL("file", null, 0, "/etc");
        if (local.toString().compareTo("file://localhost/etc") != 0)
            Assert.assertEquals("Zero port number is not ignored.", "file:/etc", local.toString());
    }

    @Test
    public void testDosRelative() throws Exception {
        //
        // DOS relative paths
        //

        VRL parent = new VRL("file:///C:");
        VRL relvrl = new VRL("subdir");
        VRL resolved = parent.resolvePath(relvrl);
        Assert.assertEquals("resolved DOS VRL turned out wrong", "file:/C:/subdir",
                resolved.toString());

        parent = new VRL("file://WinHost/C:");
        relvrl = new VRL("subdir");
        resolved = parent.resolvePath(relvrl);
        Assert.assertEquals("resolved DOS VRL turned out wrong", "file://WinHost/C:/subdir",
                resolved.toString());

        parent = new VRL("file:/C:");
        relvrl = new VRL("subdir");
        resolved = parent.resolvePath(relvrl);
        Assert.assertEquals("resolved DOS VRL turned out wrong", "file:/C:/subdir",
                resolved.toString());

        parent = VRL.createDosVRL("file:///C:\\");
        relvrl = VRL.createDosVRL(".\\subdir");
        resolved = parent.resolvePath(relvrl);
        Assert.assertEquals("resolved DOS VRL turned out wrong", "file:/C:/subdir",
                resolved.toString());

        parent = VRL.createDosVRL("file:///C:\\Windos XP\\Stuffdir\\");
        relvrl = VRL.createDosVRL(".\\subdir\\subsubdir");
        resolved = parent.resolvePath(relvrl);
        Assert.assertEquals("resolved DOS VRL turned out wrong",
                "file:/C:/Windos XP/Stuffdir/subdir/subsubdir", resolved.toString());

        parent = VRL.createDosVRL("file:/C:\\Windos XP\\Stuffdir\\");
        relvrl = VRL.createDosVRL(".\\subdir\\subsubdir");
        resolved = parent.resolvePath(relvrl);
        Assert.assertEquals("resolved DOS VRL turned out wrong",
                "file:/C:/Windos XP/Stuffdir/subdir/subsubdir", resolved.toString());

        parent = VRL.createDosVRL("file:///C:\\subdir\\");
        resolved = parent.resolvePath("subsubdir");
        Assert.assertEquals("resolved DOS VRL turned out wrong", "file:/C:/subdir/subsubdir",
                resolved.toString());

        resolved = parent.resolvePath("../twindir");
        Assert.assertEquals("resolved DOS VRL turned out wrong", "file:/C:/twindir",
                resolved.toString());

        resolved = parent.resolvePath("./../twindir2");
        Assert.assertEquals("resolved DOS VRL turned out wrong", "file:/C:/twindir2",
                resolved.toString());

        // TODO:
        // query+index
        // loc=parent.resolve("?query");
        // Assert.assertEquals("resolved VRL turned out wrong","file://hostname/parentpath/base.html?query#index",loc.toString());

    }

    /** Test Duplicate Path VRLs */
    @Test
    public void testDuplicatePath() throws VRLSyntaxException {
        //
        // Regression bug !
        //
        String prefix = "scheme://host:1234";
        VRL path = new VRL(prefix + "/absolutepath");

        // starting a path without an "/" creates wrong VRLs

        String relPath = "relative";
        VRL newvrl = path.replacePath(relPath);

        // between host:port and relpath there must be an extra slash !
        Assert.assertEquals("Extra slash isn't inserted.", newvrl.toString(), prefix + "/"
                + relPath);

        //
        // test duplicate equals hand hashcode !
        //

        VRL dupl = path.duplicate();
        Assert.assertEquals("Duplicate doesn't return same VRL.", path.toString(), dupl.toString());
        // test hashcode of duplicate
        Assert.assertEquals("Hashcode Failure !", path.hashCode(), dupl.hashCode());
        // test equals of duplicate
        Assert.assertTrue("equals() method Failure !", path.equals(dupl));

    }

    /** Test Duplicate Reference VRLs */
    @Test
    public void testDuplicateReference() throws VRLSyntaxException {
        //
        // Regression bug !
        //
        String prefix = "scheme:reference?hello";
        VRL ref1 = new VRL(prefix);

        Assert.assertEquals("Invalid reference VRL.", "scheme:reference?hello", ref1.toString());

        // check wether relative reference paths are kept sane:
        String relPath = "relative";
        VRL newvrl = ref1.replacePath(relPath);

        // duplicate path should insert extra slash (or else it isn't a path);
        Assert.assertEquals("Relative reference must be kept relative.", "scheme:relative?hello",
                newvrl.toString());

        //
        // Test duplicate equals hashcode !
        //

        VRL dupl = ref1.duplicate();
        Assert.assertEquals("Duplicate doesn't return same VRL.", ref1.toString(), dupl.toString());
        // test hashcode of duplicate
        Assert.assertEquals("Hascode Failure !", ref1.hashCode(), dupl.hashCode());
        // test equals of duplicate
        Assert.assertTrue("equals() method Failure !", ref1.equals(dupl));

    }

    @Test
    public void testEncoding() throws Exception {
        // Test encoded String to decoded path:
        VRL vrl = new VRL("http://domain.com.fake/Hoi%20Piepeloi");
        String path = vrl.getPath(); // decoded path
        Assert.assertEquals("Encoded path does not decode correctly.", "/Hoi Piepeloi", path);

        // Test decoded String to decoded path:
        vrl = new VRL("http://domain.com.fake/Hoi Piepeloi");
        path = vrl.getPath(); // decoded path
        Assert.assertEquals("Encoded path does not decode correctly.", "/Hoi Piepeloi", path);

        // test non encoded constructor to Decoded String representation
        // Also a relative path will be cast to an absolute path.
        vrl = new VRL("sftp", "dummyhost", 6666, "Spaced Relative Path");

        // VRL to string returns DECODED URI
        String vrlstr = vrl.toString();
        Assert.assertEquals("VRL constructor does not have a decoded path.",
                "sftp://dummyhost:6666/Spaced Relative Path", vrlstr);

        // URI string returns ENCODED URI
        // Currently this is my (VRL) definitation.
        String uristr = vrl.toURI().toString();
        Assert.assertEquals("URI string encoded path.",
                "sftp://dummyhost:6666/Spaced%20Relative%20Path", uristr);
    }

    @Test
    public void testEquals() throws VRLSyntaxException {
        // Test whether port<0 equals port ==0!
        VRL vrl1 = new VRL("file:///localpath");
        VRL vrl2 = new VRL("file:/localpath");
        Assert.assertEquals("Both triple and single slashed VRLs must match", vrl1, vrl2);

        // Test whether port<0 equals port ==0!
        vrl1 = new VRL("gftp", "elab", -1, "/path");
        vrl2 = new VRL("gftp", "elab", 0, "/path");
        Assert.assertEquals(
                "Port numbers less then 0 should match against port numbers equal to 0!", vrl1,
                vrl2);

        vrl1 = new VRL("gftp", "elab", -1, "/path");
        vrl2 = new VRL("gftp", "elab", -2, "/path");
        Assert.assertEquals("Port numbers less then 0 should match against any other ports<0 ",
                vrl1, vrl2);

        vrl1 = new VRL("gftp", "elab", -1, "/path");
        vrl2 = new VRL("gftp", "elab", -1, "/path");
        Assert.assertEquals("Port numbers less then 0 should match against any itself", vrl1, vrl2);
    }

    @Test
    public void testGetParent() throws Exception {
        VRL local = new VRL("file", null, "/etc");
        VRL parent = local.getParent();

        Assert.assertEquals("Method getParent does not return root path:" + parent, "/",
                parent.getPath());

        parent = parent.getParent();

        Assert.assertEquals("Method isRootPath of root should return true.", true,
                parent.isRootPath());
    }

    @Test
    public void testLocalhostEqualsNULLHost() throws VRLSyntaxException {
        // check triple vs single slash.
        VRL vrl1 = new VRL("file:///localpath");
        VRL vrl2 = new VRL("file:/localpath");
        Assert.assertEquals("Both triple and single slashed VRLs must match", vrl1, vrl2);
        //
        // vrl1 = new VRL("gftp","localhost" ,0,"/path");
        // vrl2 = new VRL("gftp",null,0,"/path");
        // Assert.assertEquals("localhost must match NULL hostname:",vrl1,vrl2);
    }

    /**
     * Regressions unitTests for addPath:
     * 
     * @throws Exception
     */
    @Test
    public void testLocationAddPath() throws Exception {
        VRL loc = new VRL("myvle:");
        VRL newLoc = loc.appendPath("testpath");
        Assert.assertEquals("added path not expected path", "/testpath", newLoc.getPath());

        // extra slashes:
        loc = new VRL("myvle:///");
        newLoc = loc.appendPath("testpath");
        Assert.assertEquals("added path not expected path", "/testpath", newLoc.getPath());

        // tests with null path
        loc = new VRL(null, null, (String) null);
        newLoc = loc.appendPath("testpath");
        Assert.assertEquals("added path to <null> path not expected path", "/testpath",
                newLoc.getPath());

        // unitTests with extra slashes
        loc = new VRL("myvle:////parent");
        newLoc = loc.appendPath("testpath");
        Assert.assertEquals("extra slashes I: path is not expected path", "/parent/testpath",
                newLoc.getPath());

        // unitTests with extra slashes
        loc = new VRL("myvle:///parent");
        newLoc = loc.appendPath("/testpath");
        Assert.assertEquals("extra slashes II: path is not expected path", "/parent/testpath",
                newLoc.getPath());

        // unitTests with extra slashes
        loc = new VRL("myvle:///parent");
        newLoc = loc.appendPath("//testpath");
        Assert.assertEquals("extra slashes II: path is not expected path", "/parent/testpath",
                newLoc.getPath());

        // unitTests with extra slashes
        loc = new VRL("myvle:///parent");
        newLoc = loc.appendPath("/testpath/");
        Assert.assertEquals("extra slashes III: path is not expected path", "/parent/testpath",
                newLoc.getPath());
    }

    @Test
    public void testMyVLe() throws Exception {
        String myScheme = "myvle";

        VRL loc = new VRL(myScheme + ":");

        Assert.assertEquals("MyVLe location must have MyVLE scheme type", myScheme, loc.getScheme());

        loc = new VRL("myvle:/");
        Assert.assertEquals("MyVLe location must have MyVLE scheme type", myScheme, loc.getScheme());

        loc = new VRL("myvle://");
        Assert.assertEquals("MyVLe location must have MyVLE scheme type", myScheme, loc.getScheme());

        loc = new VRL("myvle:///");
        Assert.assertEquals("MyVLe location must have MyVLE scheme type", myScheme, loc.getScheme());
    }

    @Test
    public void testNewLocationFromLocalTildeExpansion() throws Exception {
        VRL loc = new VRL("file:/~");
    }

    @Test
    public void testNewLocationFromNullString() throws Exception {
        VRL loc = new VRL(null, null, (String) null);

        Assert.assertEquals("NULL location, shoud have NULL userinfo", null, loc.getUserinfo());
        Assert.assertEquals("NULL location, shoud have 0 port", true, loc.getPort() <= 0);
        Assert.assertEquals("NULL location, should have null path or empty string", true,
                StringUtil.isEmpty(loc.getPath()));
    }

    @Test
    public void testPathEncoding() throws Exception, URISyntaxException {
        // Special character to test.
        // All should be encoded except '?#&'.
        // - Character '@' should be encoded if not before hostname. 
        // - Character '&' should be encoded if it is part of the path, but not if it used in 
        //   the query part as attribute separator. 

        String chars = "`'\"~!$%^*()_+-={}[]|;:'<>,@?#&";

        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            // System.out.println("Char="+c+",str="+estr);

            // check URI encoding with VRL encoding

            try {
                // Make sure VRL and URI use same encoding:
                URI uri = new URI("aap", "noot", "/" + c, null);
                VRL vrl = new VRL("aap", "noot", "/" + c);

                // if c is in "#?&" then it will be recognised as query of
                // fragment seperator
                VRL vrl2;
                if ((c == '#') || (c == '?') || (c == '&'))
                    vrl2 = new VRL("aap://noot/" + URIFactory.encodePath("" + c));
                else
                    vrl2 = new VRL("aap://noot/" + c);

                VRL vrl3 = new VRL("aap://noot/" + URIFactory.encodePath("" + c));

                Assert.assertEquals("Encoded URI does not match VRL", uri.toString(), vrl.toURI()
                        .toString());
                Assert.assertEquals("Decoded VRL path does not match. ", "/" + c, vrl.getPath());
                Assert.assertEquals("Decoded URI path does not match. ", "/" + c, uri.getPath());
                Assert.assertEquals("VRL constructors do not match.", vrl.toString(),
                        vrl2.toString());
                Assert.assertEquals("VRL constructors do not match.", vrl.toString(),
                        vrl3.toString());

            } catch (URISyntaxException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    /**
     * Regression for query encoding bugs
     * 
     * @throws VRLSyntaxException
     */
    @Test
    public void testQueryEncoding() throws VRLSyntaxException {
        String qstr = "property=hoipiepeloi";

        VRL vrl = new VRL("scheme:?" + qstr);
        Assert.assertEquals("Returned query string doesn't match", qstr, vrl.getQuery());
        vrl = new VRL("scheme:/?" + qstr);
        Assert.assertEquals("Returned query string doesn't match", qstr, vrl.getQuery());
        vrl = new VRL("scheme://host/?" + qstr);
        Assert.assertEquals("Returned query string doesn't match", qstr, vrl.getQuery());
        // vrl=new VRL("scheme:reference?"+qstr);
        // Assert.assertEquals("Returned query string doens't match",qstr,vrl.getQuery());

    }

    @Test
    public void testRelativePaths() throws Exception {
        VRL vrl = new VRL("file:///home/");
        VRL newvrl = vrl.resolvePath("/etc");
        if (StringUtil.compare(newvrl.toString(), "file:/etc") != 0)
            Assert.assertEquals("new absolute VRL does not match", "file:///etc", newvrl.toString());

        // resolve absolute path:
        newvrl = vrl.resolvePath("etc");
        // must be "file:///etc"
        Assert.assertEquals("new absolute VRL does not match", newvrl.toString(), "file:/home/etc");

        VRL relvrl = new VRL("../relative directory");
        Assert.assertTrue("VRL should be relative", relvrl.isRelative());
        Assert.assertTrue("VRL should be relative", relvrl.isAbsolute() == false);

        relvrl = new VRL("/absolute path/but relative URI!");
        Assert.assertTrue("VRL should be relative", relvrl.isRelative());
        Assert.assertTrue("VRL should be relative", relvrl.isAbsolute() == false);

        relvrl = new VRL("/absolute path/but relative URI!");
        Assert.assertTrue("VRL should be relative", relvrl.isRelative());
        // url with scheme without host= absolute

        relvrl = new VRL("file:///aap");
        Assert.assertTrue("VRL should be absolute", relvrl.isAbsolute());
        Assert.assertTrue("VRL should be absolute", relvrl.isRelative() == false);

        relvrl = new VRL("subdir");
        VRL parent = new VRL("gftp://hostname/parentpath");

        VRL loc2 = parent.resolvePath(relvrl);
        Assert.assertEquals("resolved VRL turned out wrong", "gftp://hostname/parentpath/subdir",
                loc2.toString());

        String subFile = "./other.html";
        loc2 = parent.resolvePath(subFile);
        Assert.assertEquals("resolved VRL turned out wrong",
                "gftp://hostname/parentpath/other.html", loc2.toString());

        parent = new VRL("gftp://hostname/parentpath/base.html");
        relvrl = new VRL("../subdir");
        loc2 = parent.resolvePath(relvrl);
        Assert.assertEquals("resolved VRL turned out wrong", "gftp://hostname/parentpath/subdir",
                loc2.toString());

        relvrl = new VRL("../../subdir");
        loc2 = parent.resolvePath(relvrl);
        Assert.assertEquals("resolved VRL turned out wrong", "gftp://hostname/subdir",
                loc2.toString());

    }

    @Test
    public void testReleativeIndex() throws VRLSyntaxException {
        VRL parent = new VRL("gftp://hostname/parentpath/base.html");

        // index
        VRL loc2 = parent.uriResolve("#index");
        Assert.assertEquals("resolved VRL turned out wrong",
                "gftp://hostname/parentpath/base.html#index", loc2.toString());
    }

    @Test
    public void testReleativeQuery() throws VRLSyntaxException {
        VRL parent = new VRL("http://hostname/parentpath/base.html");

        // index
        VRL loc2 = parent.uriResolve("?query");
        Assert.assertEquals("resolved VRL turned out wrong",
                "http://hostname/parentpath/base.html?query", loc2.toString());

        // index
        loc2 = parent.uriResolve("?query&par=1;");
        Assert.assertEquals("resolved VRL turned out wrong",
                "http://hostname/parentpath/base.html?query&par=1;", loc2.toString());

        // index
        VRL loc3 = parent.uriResolve("?query&par=2#index1");
        Assert.assertEquals("resolved VRL turned out wrong",
                "http://hostname/parentpath/base.html?query&par=2#index1", loc3.toString());
    }

    @Test
    public void testResolvePathWithSpaces() throws Exception {
        String path = "A File";
        VRL parent = new VRL("gftp://hostname/parentpath");
        VRL loc2 = parent.resolvePath(path);
        Assert.assertEquals("resolved VRL turned out wrong", "gftp://hostname/parentpath/A File",
                loc2.toString());

        path = "../A File";
        parent = new VRL("gftp://hostname/parentpath");
        loc2 = parent.resolvePath(path);
        Assert.assertEquals("resolved VRL turned out wrong", "gftp://hostname/A File",
                loc2.toString());

        path = "A Dir/A File";
        parent = new VRL("gftp://hostname/parentpath");
        loc2 = parent.resolvePath(path);
        Assert.assertEquals("resolved VRL turned out wrong",
                "gftp://hostname/parentpath/A Dir/A File", loc2.toString());

        path = "../A Dir/A File";
        parent = new VRL("gftp://hostname/parentpath");
        loc2 = parent.resolvePath(path);
        Assert.assertEquals("resolved VRL turned out wrong", "gftp://hostname/A Dir/A File",
                loc2.toString());

    }

    // some check to ensure URI<->VRL consistancy !
    @Test
    public void testJavaURICompatibility() throws Exception {
        // scheme/host/path/fragment
        URI uri = new URI("file", "", "/etc", (String) null);
        VRL vrl = new VRL("file", "", "/etc");

        if (vrl.toURI().compareTo(uri) != 0)
            Assert.assertEquals("VRL not similar to URI", vrl.toString(), uri.toString());

        // scheme/host/path/fragment
        uri = new URI("file", null, "/etc", (String) null);
        vrl = new VRL("file", null, "/etc");

        if (vrl.toURI().compareTo(uri) != 0)
            Assert.assertEquals("VRL not similar to URI", vrl.toString(), uri.toString());
    }

    @Test
    public void testURIReferences() throws VRLSyntaxException {
        String guidStr = "guid:aapnootmies";
        VRL guiVRL = new VRL(guidStr);
        Assert.assertEquals("Simple GUID VRL does not match original string", guidStr,
                guiVRL.toString());

        guidStr = "guid:10293847565647382910";
        guiVRL = new VRL(guidStr);
        Assert.assertEquals("Simple GUID VRL does not match original string", guidStr,
                guiVRL.toString());

        guidStr = "guid:/AAPNOOTMIES";
        guiVRL = new VRL(guidStr);
        Assert.assertEquals("Simple GUID VRL does not match original string", guidStr,
                guiVRL.toString());

        guidStr = "guid:1234567890-ABCDEFGHIJKLMNOPQRSTUVWXYZ-_-abcdefghijklmnopqrstuvwxyz";
        guiVRL = new VRL(guidStr);
        Assert.assertEquals("GUID VRL does not match original string", guidStr, guiVRL.toString());
    }

    @Test
    public void testUserinfo() throws VRLSyntaxException {
        VRL vrl = new VRL("file://user@server/path");

        Assert.assertEquals("getUserinfo() must return user part.", "user", vrl.getUserinfo());
        Assert.assertEquals("getUsername() must return user part.", "user", vrl.getUsername());
        Assert.assertEquals("getPassword() must return NULL.", null, vrl.getPassword());

        vrl = new VRL("file://user:password@server/path");

        Assert.assertEquals("getUserinfo() must return user:password part.", "user:password",
                vrl.getUserinfo());
        Assert.assertEquals("getUsername() must return user part.", "user", vrl.getUsername());
        Assert.assertEquals("getPassword() must return password part.", "password",
                vrl.getPassword());

        vrl = new VRL("file://user.domain:password@server/path");

        Assert.assertEquals("getUserinfo() must return user.domain:password part.",
                "user.domain:password", vrl.getUserinfo());
        Assert.assertEquals("getUsername() must return user.domain part.", "user.domain",
                vrl.getUsername());
        Assert.assertEquals("getPassword() must return password part.", "password",
                vrl.getPassword());

        vrl = new VRL("file://:password@server/path");

        Assert.assertEquals("getUserinfo() must return :password part.", ":password",
                vrl.getUserinfo());
        Assert.assertEquals("getUsername() must return null userpart.", null, vrl.getUsername());
        Assert.assertEquals("getPassword() must return password part.", "password",
                vrl.getPassword());

    }

    /**
     * Regressions unitTests for windows locations:
     * 
     * @throws Exception
     */
    @Test
    public void testWinDosLocations() throws Exception {
        // make relative paths absolute
        VRL loc = new VRL("file:///C:");
        Assert.assertEquals("added path didn't result in expected path", "/C:/", loc.getPath());

        // make relative paths absolute
        loc = new VRL("file:///c:hoi");
        Assert.assertEquals("added path didn't result in expected path", "/c:/hoi", loc.getPath());

        // backaslashes to slashes:
        loc = VRL.createDosVRL("file:///c:\\hoi\\");
        Assert.assertEquals("added path to <null> didn't result in expected path", "/c:/hoi",
                loc.getPath());

        // make relative paths absolute
        loc = VRL.createDosVRL("file:///a:");
        VRL newLoc = loc.appendPath("testpath");
        Assert.assertEquals("added path didn't result in expected path", "/a:/testpath",
                newLoc.getPath());

        // make relative paths absolute
        loc = VRL.createDosVRL("file:///a:");
        newLoc = loc.appendPath("\\testpath");
        Assert.assertEquals("added path didn't result in expected path", "/a:/testpath",
                newLoc.getPath());
    }

    @Test
    public void testJDBCvrls() throws Exception {
        String uristrs[] = new String[] { "jdbc:mysql://host:13/dbname",
                "jdbc:mysql://host:13/dbname", "jdbc:postgresql://localhost/test",
                "jdbc:dbproto:localdb", "jdbc:dbproto:localdb?parameter1=value1&parameter2=value2",
                "jdbc:oracle:thin:@server.domain:1521:dbclass1" };

        for (String uristr : uristrs) {
            java.net.URI uri1 = new java.net.URI(uristr);
            Assert.assertEquals("JDBC URI must match with original String value.", uristr,
                    uri1.toString());

            // jdbc URIs must be treated as Opaque: 
            VRL vrl1 = VRL.createOpaque(uristr);
            Assert.assertTrue("JDBC VRL must be Opaque", vrl1.isOpaque());
            Assert.assertEquals("Opaque JDBC VRL must match with opaque URI", uristr,
                    vrl1.toString());
        }

    }

    @Test
    public void testRegressionInfoRootToURI() throws Exception {
        // Bug in previous infors implementation: 
        String uriStr = "info:/";

        VRL vrl = new VRL(uriStr);
        URI uri = vrl.toURI();

        String uriStr2 = uri.toString();

        Assert.assertEquals("String converted to URI must match", uriStr, uriStr2);
    }

    @Test
    public void testRegressionLocalFileURI() throws Exception {

    }

    @Test
    public void testRegressionLocalFileURI_authorization() throws Exception {

        // If compatible to URI/URL, the actual authorization must be removed.  

        //        VRL vrl = new VRL("file://user@host.domain:1234/Directory/AFile");
        //        VRL localVrl = new VRL("file:/Directory/AFile");
        //        Assert.assertEquals("Local File URI must match.", localVrl, vrl);
        //        java.net.URL localFileUrl=vrl.toURL(); 
        //        Assert.assertEquals("Local File VRL must match local File URL. Java URL removes Authority information", localVrl,localFileUrl); 
        //
        //        vrl = new VRL("file://user@host.domain:1234/Directory/A File");
        //        localVrl = new VRL("file:/Directory/A File");
        //        Assert.assertEquals("Local File URI must match.", localVrl, vrl);
        //        localFileUrl=vrl.toURL();
        //        Assert.assertEquals("Local File VRL must match local File URL. Java URL removes Authority information", localVrl,localFileUrl); 

    }

    @Test
    public void testTildeInFilename() throws VRLSyntaxException {
        String subPath = "prefix~postfix";
        String baseDir = "/home/user";
        String compositePath = baseDir + "/" + subPath;

        VRL baseVrl = new VRL("file:" + baseDir);
        VRL resolvedVrl = baseVrl.resolvePath(subPath);
        VRL expectedVrl = new VRL("file:" + compositePath);

        Assert.assertEquals("VRL doesn't match expected:", expectedVrl, resolvedVrl);
        Assert.assertEquals("VRL path doesn't match expected:", compositePath,
                resolvedVrl.getPath());

    }
}
