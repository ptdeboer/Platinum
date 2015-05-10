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

package nl.esciencecenter.ptk.web;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;

import org.apache.http.client.utils.URIBuilder;

/**
 * Simple {Name,Value} parameter list for URI Queries. <br>
 * These will be appended to an URI after the "?" part as "&lt;Name&gt;=&lt;Value&gt;&amp;,... Note:
 * to stay compatible with actual URI encoding, apaches URI Encodig is used. This encoding is not
 * equivalent with Java's URI and URL encoding.
 */
public class URIQueryParameters extends LinkedHashMap<String, String> {

    private static final long serialVersionUID = -2339762351557005626L;

    public URIQueryParameters() {
        super();
    }

    public boolean isEmpty() {
        return (this.keySet().size() <= 0);
    }

    /**
     * Create URI encoded query String from this parameter list.
     * 
     * @return URI query string.
     * @throws UnsupportedEncodingException
     */
    public String toQueryString() throws UnsupportedEncodingException {
        // Use Apache URI builder !
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setHost("host");
        uriBuilder.setScheme("scheme");
        uriBuilder.setPath("/");
        //
        for (String key : this.keySet()) {
            uriBuilder.setParameter(key, this.get(key));
        }
        // todo: better URI query encoding.
        return uriBuilder.toString().substring("scheme://host/?".length());
    }

}
