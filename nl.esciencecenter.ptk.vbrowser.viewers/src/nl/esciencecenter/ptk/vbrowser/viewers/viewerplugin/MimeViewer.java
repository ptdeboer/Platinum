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

package nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin;

import java.util.List;
import java.util.Map;

/** 
 * Interface for content viewers.
 * Viewer registry binds MimeTypes from getMimeTypes() to this viewer. 
 */
public interface MimeViewer
{
    
    public String getViewerName(); 
    
    /**
     * Supported mime types. One viewer may support multiple mime types. 
     * For example { "text/plain", "text/html" } 
     * @return
     */
    public String[] getMimeTypes();

    /**
     *  Returns menu mapping per MimeType to a list of menu methods.<br>
     *  Map structure ::= <code> Map&lt;MimeType, List&lt;MenuMethod&gt;&gt; </code>  
     *  <br>
     *  For example: { "text/plain" , {"View Text","Edit Text"}} 
     */ 
    public Map<String,List<String>> getMimeMenuMethods(); 
    
}
