/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm;


import lombok.extern.slf4j.Slf4j;
import nl.piter.vterm.sys.SysFS;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

@Slf4j
public class SysFSTest {

//    It is intended to support file URIs that take the following forms:
//
//    Local files:
//
//    o  "file:///path/to/file"
//
//    A "traditional" file URI for a local file, with an empty
//    authority.  This is the most common format in use today,
//    despite being technically incompatible with the definition in
//         [RFC1738].
//
//    o  "file:///c:/path/to/file"
//
//    The traditional representation of a local file in a DOS- or
//    Windows-based environment.
//
//    o  "file:/path/to/file"
//
//    A "modern" minimal representation of a local file in a UNIX-
//    like environment, with no authority field and an absolute path
//    that begins with a slash "/".
//
//    o  "file:c:/path/to/file"
//
//    The minimal representation of a local file in a DOS- or
//    Windows-based environment, with no authority field and an
//    absolute path that begins with a drive letter.
//
//    o  "file:///c/path/to/file"
//
//    o  "file:/c/path/to/file"
//
//    o  "file:///c/path/to/file"
//
//    Representations of a local file in a DOS- or Windows-based
//    environment, using alternative representations of drive
//    letters.  These are supported for compatibility with historical
//    implementationsm, but deprecated by this specification.
//
//            o  "file:/c:/path/to/file"
//
//    A representation of a local file in a DOS- or Windows-based
//    environment, with no authority field and a slash preceding the
//    drive letter.  This representation is less common than those
//    above, and is deprecated by this specification.

    @Test
    public void testFilesPaths() throws IOException {
        // under linux any path starting with c: is considered relative:
        testFilePath("c:/path/to/file");
        // ¿illegal in dos?:
        //testFilePath("/c:/path/to/file");
        testFilePath("/c/path/to/file");
        testFilePath("c/relative/path/to/file");
        testFilePath("///c/path/to/file");
        testFilePath("c:relative/path/to/file");
    }

    private void testFilePath(String filePath) {
        URI uri = new SysFS().resolveFileURI(filePath);
        log.info("resolve:'{}'=>'{}'",filePath,uri);
    }

}
