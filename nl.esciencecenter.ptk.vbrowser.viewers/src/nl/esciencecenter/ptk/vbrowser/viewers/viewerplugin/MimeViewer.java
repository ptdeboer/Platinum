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
