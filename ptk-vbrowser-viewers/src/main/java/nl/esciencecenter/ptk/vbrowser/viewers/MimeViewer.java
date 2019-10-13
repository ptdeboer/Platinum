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

package nl.esciencecenter.ptk.vbrowser.viewers;

import java.util.List;
import java.util.Map;

/**
 * Interface for content viewers. Viewer Registry binds MimeTypes from getMimeTypes() to this
 * viewer. A list of optional Menu mapping can be supplied by <code>getMimeMenuMethods</code>.
 */
public interface MimeViewer {

    String getViewerName();

    /**
     * @return Supported mime types. One viewer may support multiple mime types. For example {
     * "text/plain", "text/html" }. The order is preferred first.
     */
    String[] getMimeTypes();

    /**
     * @return Returns the mapping of a menu entry per MimeType to a list of menu methods.<br>
     * Mapping is ::= <code> Map&lt;MimeType, List&lt;MenuMethod&gt;&gt; </code> <br>
     * For example: { "text/plain" , {"View Text:viewText","Edit Text:editText"}}<br>
     * Menu methods should be human readable, the actual method name after the colon may be
     * omitted. In that case the plain text menu name will be used as Method Name. This is
     * the optional method name when startViewer(...) is invoked.
     */
    Map<String, List<String>> getMimeMenuMethods();

}
