package nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin;

import java.util.List;
import java.util.Map;

public interface MimeViewer
{
    
    String getName(); 
    
    String[] getMimeTypes();

    /**
     *  Returns Mapping or mimeType to a list of meny methods 
     *  For example "text/plain" -> {"View Text","Edit Text"}
     */ 
    Map<String,List<String>> getMimeMenuMethods(); 
    
}
