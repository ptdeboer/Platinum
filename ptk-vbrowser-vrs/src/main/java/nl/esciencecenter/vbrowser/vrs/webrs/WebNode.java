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

package nl.esciencecenter.vbrowser.vrs.webrs;

import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.io.IOUtil;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.web.ResponseInputStream;
import nl.esciencecenter.ptk.web.ResponseOutputStream;
import nl.esciencecenter.ptk.web.WebClient;
import nl.esciencecenter.ptk.web.WebException;
import nl.esciencecenter.vbrowser.vrs.VRS;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsIOException;
import nl.esciencecenter.vbrowser.vrs.io.VStreamAccessable;
import nl.esciencecenter.vbrowser.vrs.node.VPathNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.io.IOException;
import java.util.Map;

import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.*;

/**
 * Class represents a HTTP reference
 */
public class WebNode extends VPathNode implements VStreamAccessable {

    // =====
    // Class
    // =====

    static private final StringList attributeNames = new StringList(ATTR_RESOURCE_TYPE, ATTR_NAME, ATTR_HOSTNAME, ATTR_PORT,
            ATTR_ICON, ATTR_PATH, ATTR_MIMETYPE, ATTR_CHARSET, ATTR_LOCATION);

    // ========
    // Instance
    // ========

    private String mimeType = null;

    private final WebResourceSystem httprs;

    private boolean isHTTPS = false;

    protected WebClient getWebClient() {
        return this.httprs.getWebClient();
    }

    private void init(VRL loc) throws VrsException {
        this.isHTTPS = StringUtil.equalsIgnoreCase(loc.getScheme(), VRS.HTTPS_SCHEME);
    }

    public WebNode(WebResourceSystem httprs, VRL loc) throws VrsException {
        super(httprs, loc);
        this.httprs = httprs;
        init(loc);
    }

    @Override
    public String getResourceType() {
        return VRS.HTTP_SCHEME;
    }

    public ResponseInputStream createInputStream() throws VrsException {
        try {
            return getWebClient().doGetInputStream(getVRL().toURI());
        } catch (Exception e) {
            throw new VrsIOException(e.getMessage(), e);
        }
    }

    /**
     * Get mimetype as reported by remote Server. Open Connection and tries get the ContentType
     * header from the request.
     */
    @Override
    public String getMimeType() throws VrsException {
        if (mimeType != null) {
            return mimeType;
        }

        String str;
        try {
            ResponseInputStream inps = createInputStream();
            str = inps.getMimeType();
            IOUtil.autoClose(inps);
        } catch (VrsIOException e) {
            throw e;
        }

        if (str == null) {
            return "text/html";
        }

        String[] strs = str.split(";");

        if (strs.length < 1) {
            mimeType = str;
        } else {
            mimeType = strs[0];
        }

        if (mimeType == null) {
            return "text/html";
        }

        return mimeType;
    }

    /**
     * Get the names of the attributes this resource has
     */
    public Map<String, AttributeDescription> getAttributeDescriptions() {
        return AttributeDescription.createMap(attributeNames, AttributeType.STRING, false);
    }

    // === Misc === 

    //    // method needs by streamread/write interface 
    //    public int getOptimalWriteBufferSize()
    //    {
    //        return VRS.DEFAULT_STREAM_WRITE_CHUNK_SIZE;
    //    }
    //
    //    public int getOptimalReadBufferSize()
    //    {
    //        return VRS.DEFAULT_STREAM_READ_CHUNK_SIZE;
    //    }

    public String getIconURL() {
        // doesn't have to be connected. 
        VRL vrl;

        try {
            vrl = this.getVRL().resolvePath("favicon.ico");
            if (exists(vrl.getPath())) {
                return vrl.toString();
            }
        } catch (VRLSyntaxException e) {
            e.printStackTrace();
        }

        vrl = getVRL().replacePath("favicon.ico");

        if (exists(vrl.getPath())) {
            return vrl.toString();
        }

        return null; // no icon
    }

    public ResponseOutputStream createOutputStream(boolean append) throws VrsException {
        if (append == true) {
            throw new VrsException("Appending OutputStream not supported!");
        }

        VRL vrl = getVRL();
        String queryStr = vrl.getPath();

        if (vrl.getFragment() != null) {
            queryStr += queryStr + "#" + vrl.getFragment();
        }

        if (vrl.getQuery() != null) {
            queryStr += queryStr + "?" + vrl.getQuery();
        }

        StringHolder statusH = new StringHolder();
        try {
            return getWebClient().doPutOutputStream(queryStr, statusH);
        } catch (WebException e) {
            throw new VrsIOException(e.getMessage(), e);
        }
    }

    public WebResourceSystem getHTTPRS() {
        return this.httprs;
    }

    public boolean exists(String query) {
        try {
            ResponseInputStream inps = getWebClient().doGetInputStream(query);
            inps.autoClose();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
