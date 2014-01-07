/*
 * Copyright 2012-2013 Netherlands eScience Center.
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

import java.io.IOException;

public class WebException extends IOException 
{
    private static final long serialVersionUID = 6695170599026563455L;
    
    public static enum Reason
    {
        // Exception Reasons; 
        UNAUTHORIZED("Unauthorized."),
        FORBIDDEN("Forbidden."),
        
        CONNECTION_EXCEPTION("Connection Exception."),
        UNKNOWN_HOST("Hostname or server not found."),
        NO_ROUTE_TO_HOST_EXCEPTION("No route to host."),
        CONNECTION_TIME_OUT("Connection timeout."),
        
        RESOURCE_NOT_FOUND("Resource not found."),
        RESOURCE_ALREADY_EXISTS("Resource already exists."),
        
        HTTP_ERROR("HTTP Error."), 
        HTTP_CLIENTEXCEPTION("HTTP Client Exception."), 
        HTTPS_SSLEXCEPTION("HTTPS SSL Exception."),
        IOEXCEPTION("IOException."), 
        INVALID_REQUEST("Invalid Request"), 
        INVALID_RESPONSE("Invalid Response"),
        URI_EXCEPTION("URI Syntax Exception."), 
        ;
        
        // === Instance === 
        
        private String text=null;
        
        private Reason(String text)
        {
            this.text=text; 
        }
        
        public String getText()
        {
            return this.text; 
        }
    }
    
    // === Instance ===
    
    private Reason reason=null; 
    
    private int httpCode=0; 
    
    private String serverResponse=null;

    private String serverResponseMimeType=null; 
    
    public WebException()
    {
        super(); 
    }
    
    public WebException(String message)
    {
        super(message); 
    }
    
    public WebException(String message,Throwable cause)
    {
        super(message,cause); 
    }
    
    public WebException(Reason reason,String message,Throwable cause)
    {
        super(message,cause); 
        this.reason=reason; 
    }
   
    public WebException(Reason reason, int httpCode, String message)
    {
        super(message); 
        this.reason=reason;
        this.httpCode=httpCode; 
    }
    
    /** 
     * Create WebException from HTML error response. 
     * Typically the call itself succeed (no exception raised), but the web server responded with a HTTP error code and a HTML error text. 
     * @param reason - Enum type of recognized reason. 
     * @param httpCode - HTTP Status code (404,500,etc). 
     * @param message - Human readable exception message 
     * @param htmlResponse -  exact HTML formatted response from web server. 
     */
    public WebException(Reason reason, int httpCode, String message,String htmlResponse)
    {
        super(message); 
        this.reason=reason;
        this.httpCode=httpCode; 
        this.serverResponseMimeType="text/html"; 
        this.serverResponse=htmlResponse; 
    }
    
    /** 
     * Create WebException from HTML error response. 
     * Typically the call itself succeed (no exception raised), but the web server responded with a HTTP error code and a HTML error text. 
     * @param reason - Enum type of recognized reason. 
     * @param httpCode - HTTP Status code (404,500,etc). 
     * @param message - Human readable exception message 
     * @param htmlResponse -  exact HTML formatted response from web server. 
     */
    public WebException(Reason reason, int httpCode, String message,String responseType,String htmlResponse)
    {
        super(message); 
        this.reason=reason;
        this.httpCode=httpCode; 
        this.serverResponseMimeType=responseType;
        this.serverResponse=htmlResponse; 
    }
    
    
    public Reason getReason()
    {
        return this.reason; 
    }
    
    /**
     * Returns HTTP Error/Result Code. 
     * Returns 0 if unknown or no HTTP Status Code.  
     */ 
    public int getHttpStatus()
    {
        return this.httpCode;
    }

    /** 
     * If the Remote Server responded with a (HTML) formatted error text, this method will return it. 
     * @return - (html) formatted server response text. 
     */
    public String getServerResponse()
    {
        return this.serverResponse; 
    }
    
    /** 
     * If the Remote Server responded with a (HTML) formatted error text, this method will return the actual mime type of the response. 
     * @return - mime-type of server response. Typically "text/html".   
     */
    public String getResponseMimeType()
    {
        return this.serverResponseMimeType; 
    }
    
    public String toString()
    {
        String str = getClass().getName();
        String message = getLocalizedMessage();
        
        str+="["+getReason()+"]";
        if (message!=null)
        {
            str+=":"+message;
        }
        
        return str;  
    }
}
