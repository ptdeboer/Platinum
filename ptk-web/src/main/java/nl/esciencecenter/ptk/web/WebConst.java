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

public class WebConst {

    public static final int HTTP_PORT = 80;

    public static final int HTTPS_PORT = 443;

    public static final String HTTP_SCHEME = "http";

    public static final String HTTPS_SCHEME = "https";

    public static final String COOKIE_JSESSIONID = "JSESSIONID";

    public static final String SESSION_EXPIRATION_TIME = "SESSION_EXPIRATION_TIME";

    public static final String CHARSET_ISO_8859_1 = "ISO-8859-1";

    public static final String MIMETYPE_TEXT_PLAIN = "text/plain";

    public static final String MIMETYPE_TEXT_HTML = "text/html";

    public static final String MIMETYPE_APP_JSON = "application/json";

    /**
     * MimeType for CSV which can be mapped to MS Excel MimeType:
     */
    public static final String MIMETYPE_CSV_MS_EXCEL = "application/vnd.ms-excel";

    /**
     * Scheme Types
     */
    public enum Schemes {
        HTTP, HTTPS
    }

    public static String getHTTPStatusString(int httpStatus) {
        return "" + httpStatus;
    }

}
