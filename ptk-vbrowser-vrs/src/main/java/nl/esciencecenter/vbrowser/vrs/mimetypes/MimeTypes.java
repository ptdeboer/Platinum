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

package nl.esciencecenter.vbrowser.vrs.mimetypes;

import lombok.extern.slf4j.Slf4j;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

//import net.sf.jmimemagic.Magic;
//import net.sf.jmimemagic.MagicMatch;

/**
 * MimeType util class.
 */
@Slf4j
public class MimeTypes {
    // default mime types.
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String MIME_TEXT_HTML = "text/html";
    public static final String MIME_OCTET_STREAM = "application/octet-stream";
    public static final String MIME_BINARY = MIME_OCTET_STREAM;

    /**
     * Singleton instance.
     */
    private static MimeTypes instance;

    public static MimeTypes getDefault() {
        if (instance == null) {
            instance = new MimeTypes();
        }

        return instance;
    }

    // ========================================================================
    // Instance
    // ========================================================================

    /**
     * Mime type file type map
     */
    private MimetypesFileTypeMap typemap = null;

    public MimeTypes() {
        init();
    }

    private void init() {
        try {
            // Load default mime.types file from classpath.
            String confFile = "mime.types";
            URL result = getClass().getClassLoader().getResource(confFile);

            if (result == null) {
                confFile = "default_mime.types";
                result = getClass().getClassLoader().getResource(confFile);
            }

            if (result != null) {
                InputStream inps = result.openStream();
                typemap = new MimetypesFileTypeMap(inps);
            } else {
                log.warn("Couldn't locate ANY mime.types file on classpath");
                typemap = new MimetypesFileTypeMap();
            }
        } catch (IOException e) {
            log.warn("Couldn't initialize default mimetypes:" + e.getMessage(), e);
            // empty one !
            this.typemap = new MimetypesFileTypeMap();
        }
    }

    /**
     * Add extra mime type definitions.
     */
    public void addMimeTypes(String mimeTypes) {
        String[] lines = mimeTypes.split("\n");
        if (lines != null)
            for (String line : lines) {
                log.debug("Adding user mime.type:" + line);
                typemap.addMimeTypes(line);
            }
    }

    /**
     * Returns mimetype string by checking the extension or name of the file
     */
    public String getMimeType(String path) {
        if (path == null)
            return null;

        return typemap.getContentType(path);
    }

}
