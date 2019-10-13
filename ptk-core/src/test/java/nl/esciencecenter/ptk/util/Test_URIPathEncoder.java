package nl.esciencecenter.ptk.util;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class Test_URIPathEncoder {
    @Test
    public void testSpaces() {
        testEncode("spaced path", "spaced%20path");
    }

    public void testEncode(String raw, String expected) {
        String encoded = URIPathEncoder.encode(raw);
        Assert.assertEquals("Encoded path does not match expected.", expected, encoded);
    }

    @Test
    public void testPathEncoding() throws Exception {
        String chars = "`'\"~!%^*()_+-={}[]|;:'<>,@?#&";

        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            try {

                String expectedPath = "/dir/path" + c + "subpath";
                String encodedPath = "dir/path" + URIPathEncoder.encode("" + c) + "subpath";
                // create URI from encoded path
                URI uri = new URI("scheme://host/" + encodedPath);
                // check decoded URI path 
                Assert.assertEquals("Encoded URI Path doesn't match expected path", uri.getPath(), expectedPath);

            } catch (URISyntaxException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }
}
