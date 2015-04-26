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

import java.io.IOException;

/**
 * WebException contains the HTTP Error code and optionally the (html) error
 * response. <br>
 * A WebExeption is thrown when an http call succeed but responded with a HTTP
 * error status (any HTTP error &lt;200 or &gt;299). This Exception retains that
 * information. Use <code>getServerReponse()</code> and
 * <code>getHttpStatus()<code> to check the returned error text and HTTP Response Status.
 * 
 * @author Piter T. de Boer.
 */
public class WebException extends IOException
{
    private static final long serialVersionUID = 6695170599026563455L;

    public static enum Reason
    {
        // IO errors,
        HTTP_ERROR("HTTP Error."),
        HTTP_CLIENTEXCEPTION("HTTP Client Exception."),
        HTTPS_SSLEXCEPTION("HTTPS SSL Exception."),
        IOEXCEPTION("IOException."),
        URI_EXCEPTION("URI Syntax Exception."),
        // Connection error (not HTTP Errors!)
        CONNECTION_EXCEPTION("Connection Exception."),
        UNKNOWN_HOST("Hostname or server not found."),
        NO_ROUTE_TO_HOST_EXCEPTION("No route to host."),
        CONNECTION_TIME_OUT("Connection timeout."),
        // Authentication/Authorization
        UNAUTHORIZED("Unauthorized."),
        FORBIDDEN("Forbidden."),
        // Actual HTTP Errors:
        INVALID_REQUEST("Invalid Request"),
        INVALID_RESPONSE("Invalid Response"),
        //
        RESOURCE_NOT_FOUND("Resource not found."),
        RESOURCE_ALREADY_EXISTS("Resource already exists.");

        // =========
        // Instance
        // =========

        private String text = null;

        private Reason(String text)
        {
            this.text = text;
        }

        public String getText()
        {
            return this.text;
        }
    }

    // === Instance ===

    private Reason reason = null;

    private int httpStatus = 0;

    private String serverResponse = null;

    private String serverResponseMimeType = null;

    public WebException()
    {
        super();
    }

    public WebException(String message)
    {
        super(message);
    }

    public WebException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public WebException(Reason reason, String message, Throwable cause)
    {
        super(message, cause);
        this.reason = reason;
    }

    public WebException(Reason reason, int httpCode, String message)
    {
        super(message);
        this.reason = reason;
        this.httpStatus = httpCode;
    }

    /**
     * Create WebException from (HTML) error response. Typically the call itself
     * succeeded (no exception was raised), but the web server responded with a
     * HTTP error code and a (HTML) error text.
     * 
     * @param reason
     *            - Enum type of recognized reason.
     * @param httpCode
     *            - HTTP Status code (404,500,314,etc).
     * @param message
     *            - Human readable exception message.
     * @param responseType
     *            - mime type of response, for example "text/html".
     * @param htmlResponse
     *            - Formatted response from web server, typically HTML text.
     */
    public WebException(Reason reason, int httpCode, String message, String responseType, String htmlResponse)
    {
        super(message);
        this.reason = reason;
        this.httpStatus = httpCode;
        this.serverResponseMimeType = responseType;
        this.serverResponse = htmlResponse;
    }

    public Reason getReason()
    {
        return this.reason;
    }

    /**
     * Returns HTTP Error/Result Code, returns 0 if unknown or no HTTP Status
     * Code.<br>
     * <strong>note</strong>: Codes in the range:[200,299] arn't really errors.
     */
    public int getHttpStatus()
    {
        return this.httpStatus;
    }

    /**
     * Return Server response if any was given. <br>
     * Check {@link #getResponseMimeType()} what mime-type the error response
     * has. This might be HTML if the Remote Server responded with a (HTML)
     * formatted error text.
     * 
     * @return - (html) formatted server response text.
     */
    public String getServerResponse()
    {
        return this.serverResponse;
    }

    /**
     * If the Remote Server responded with a formatted error text, this method
     * will return the actual mime type of the response, for example "text/html"
     * if the server responded with an HTML error text.
     * 
     * @return - mime-type of server response. Typically "text/html" for HTML
     *         formatted errors.
     */
    public String getResponseMimeType()
    {
        return this.serverResponseMimeType;
    }

    public String toString()
    {
        String str = getClass().getName();
        String message = getLocalizedMessage();

        str += "[" + getReason() + "]";
        if (message != null)
        {
            str += ":" + message;
        }

        return str;
    }
}
