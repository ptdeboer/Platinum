package nl.esciencecenter.ptk.web;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;

import org.apache.http.client.utils.URIBuilder;

/**
 * Simple {Name,Value} parameter list for URI Queries.   
 * These will be appended to an URI after the "?" part as "&lt;Name&gt;=&lt;Value&gt;&amp;,...
 * Note: to stay compatible with actula URI encoding, apaches URI Encodig is uses. 
 * This encoding is not equivalent with Java's URI and URL encoding. 
 */
public class URIQueryParameters extends LinkedHashMap<String,String>
{
    private static final long serialVersionUID = -2339762351557005626L;
    
    public URIQueryParameters()
    {
        super(); 
    }
    
    public boolean isEmpty()
    {
        return (this.keySet().size()<=0);  
    }
    
    /** 
     * Create URI encoded query String from this parameter list.  
     * @return URI query string. 
     * @throws UnsupportedEncodingException
     */
    public String toQueryString() throws UnsupportedEncodingException
    {
        // Use Apache URI builder ! 
        
        URIBuilder uriBuilder=new URIBuilder(); 
        uriBuilder.setHost("host"); 
        uriBuilder.setScheme("scheme"); 
        uriBuilder.setPath("/"); 
        
        for (String key:this.keySet())
        {
            uriBuilder.setParameter(key,this.get(key)); 
        }
        // todo: better URI query encoding. 
        return uriBuilder.toString().substring("scheme://host/?".length());  
    }

    
}
