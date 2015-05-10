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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.net.ssl.SSLContext;

import nl.esciencecenter.ptk.crypt.Secret;
import nl.esciencecenter.ptk.data.SecretHolder;
import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.io.FSPath;
import nl.esciencecenter.ptk.io.FSUtil;
import nl.esciencecenter.ptk.ssl.CertificateStore;
import nl.esciencecenter.ptk.ssl.CertificateStoreException;
import nl.esciencecenter.ptk.ssl.SslConst;
import nl.esciencecenter.ptk.ui.SimpelUI;
import nl.esciencecenter.ptk.ui.UI;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.ptk.web.WebConfig.AuthenticationType;
import nl.esciencecenter.ptk.web.WebException.Reason;
import nl.esciencecenter.ptk.web.content.ByteBufferBody;
import nl.esciencecenter.ptk.web.content.FSNodeBody;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;

/**
 * Generic Rest and Web Service client.<br>
 * Updated methods to HttpClient 4.2.
 * 
 * @see WebConfig class which holds most configurable settings for the WebClient class.
 */
public class WebClient {

    private static PLogger logger = PLogger.getLogger(WebClient.class);

    // === Static ===

    // === Factory methods ===

    public static WebClient createFor(URI uri) throws WebException {
        return new WebClient(new WebConfig(uri, AuthenticationType.NONE, false));
    }

    public static WebClient createFor(URI uri, AuthenticationType authenticationType)
            throws WebException {
        return new WebClient(new WebConfig(uri, authenticationType, false));
    }

    public static WebClient createMultiThreadedFor(URI uri, AuthenticationType authenticationType)
            throws WebException {
        return new WebClient(new WebConfig(uri, authenticationType, true));
    }

    // === Instance ===

    // config:
    private WebConfig config;

    private java.net.URI serverUri;

    private UI ui = null;

    private ResourceLoader resourceLoader;

    private CertificateStore certStore;

    // status:
    private DefaultHttpClient httpClient;

    private String jsessionID;

    private int lastHttpStatus;

    private URI serviceUri;

    protected WebClient() {
        httpClient = null;
    }

    public WebClient(WebConfig config) throws WebException {
        init(config);
    }

    protected void init(WebConfig config) throws WebException {
        logger.debugPrintf("init():New WebClient for:%s\n", serverUri);

        if (config == null) {
            throw new NullPointerException("Can not have NULL WebConfig.");
        }

        if (config.getAllowUserInteraction()) {
            ui = new SimpelUI();
        }

        this.config = config;
        this.httpClient = null;
        this.resourceLoader = new ResourceLoader();

        try {
            this.serverUri = config.getServerURI();
            this.serviceUri = config.getServiceURI();
        } catch (URISyntaxException e) {
            throw new WebException(Reason.URI_EXCEPTION, e.getMessage(), e);
        }
    }

    /**
     * Returns Server URI without service path.
     * 
     * @return Server URI without service path.
     */
    public java.net.URI getServerURI() {
        return this.serverUri;
    }

    /**
     * Return Service URI including service path.
     * 
     * @return Service URI including service path.
     */
    public URI getServiceURI() {
        return serviceUri;
    }

    public String getJSessionID() {
        return this.jsessionID;
    }

    public UI getUI() {
        return this.ui;
    }

    public boolean hasUI() {
        return (this.ui != null);
    }

    /**
     * Specify UI interface, set to null if no UI interactions are allowed.
     * 
     * @param newUI
     *            - new UI interface or null to disable UI actions.
     */
    public void setUI(UI newUI) {
        this.ui = newUI;
    }

    public void setCredentials(String username, Secret password) {
        config.setCredentials(username, password);
    }

    public void setCertificateStore(CertificateStore certStore) throws CertificateStoreException {
        initSSL(certStore, false);
    }

    public String getUsername() {
        return config.getUsername();
    }

    /**
     * The session is authenticated if it has a valid JSessionID.
     * 
     * @return - true if this session has an authentication JSessionID.
     */
    public boolean isAuthenticated() {
        return (StringUtil.isEmpty(jsessionID) == false);
    }

    public String getCharacterEncoding() {
        // Default for URIs.
        return "UTF-8";
    }

    public void setJSessionID(String jsession) {
        this.jsessionID = jsession;
    }

    // ========================================================================
    // Initialization/Authentication
    // ========================================================================

    public boolean connect() throws WebException {
        //
        httpClient = new DefaultHttpClient();

        if (config.isMultiThreaded()) {
            ClientConnectionManager connectionManager = new PoolingClientConnectionManager();
            httpClient = new DefaultHttpClient(connectionManager);
        }

        try {

            if (config.isHTTPS()) {
                if (this.certStore == null) {
                    logger.warnPrintf("Warning: HTTPS procotol with no CertificateStore\n");
                } else {
                    initSSL(this.certStore, true);
                }
            }

            if ((config.getUseBasicAuthentication()) && (config.hasPassword() == false)
                    && (this.jsessionID == null) && (this.hasUI())) {
                String user = getUsername();

                SecretHolder secret = new SecretHolder();
                boolean result = uiPromptPassfield(
                        "Password for user:" + user + "@" + config.getHostname() + ":"
                                + config.getPort(), secret);

                if (result == false) {
                    return false; // cancelled.
                }

                this.config.password = secret.get();
            }

            // httpClient.getCredentialsProvider().setCredentials(new
            // AuthScope(config.getHostname(), config.getPort()),
            // new UsernamePasswordCredentials(config.getUsername(), new
            // String(config.getPasswordChars())));

            if (config.useJSession()) {
                initJSession(false);
                return true;
            } else {
                // check service URI.
                HttpGet httpget = new HttpGet(serviceUri);
                logger.debugPrintf("connect(): Executing request: %s\n", httpget.getRequestLine());
                HttpResponse response = httpClient.execute(httpget);
                logger.debugPrintf("connect(): ------------ Response ----------\n");
                StringHolder textH = new StringHolder();
                this.lastHttpStatus = handleStringResponse("connect()" + serviceUri, response,
                        textH, null);

                return true;
            }
        } catch (ClientProtocolException e) {
            throw new WebException(WebException.Reason.HTTP_CLIENTEXCEPTION, e.getMessage(), e);
        } catch (javax.net.ssl.SSLPeerUnverifiedException e) {
            throw new WebException(WebException.Reason.HTTPS_SSLEXCEPTION,
                    "SSL Verification Error:" + e.getMessage(), e);
        } catch (WebException e) {
            throw (e); // pass
        } catch (IOException e) {
            throw new WebException(WebException.Reason.IOEXCEPTION, e.getMessage(), e);
        } finally {
            // check connected state...
        }
    }

    protected void initJSession(boolean deletePrevious) throws WebException {
        //
        logger.debugPrintf("initJSession(). Using JESSION URI init string:%s\n",
                config.jsessionInitPart);

        if (deletePrevious) {
            this.jsessionID = null;
        }

        String uri = null;

        // re-use JSESSIONID:
        if (this.jsessionID != null) {

            BasicClientCookie cookie = new BasicClientCookie(WebConst.COOKIE_JSESSIONID, jsessionID);
            cookie.setPath(config.servicePath);
            cookie.setDomain(config.serverHostname);
            logger.infoPrintf(" - Using JSessionID   = %s\n", jsessionID);
            logger.debugPrintf(" - Cookie Domain/Path = %s/%s\n", cookie.getDomain(),
                    cookie.getPath());

            this.httpClient.getCookieStore().addCookie(cookie);

            return;
        } else {
            logger.debugPrintf("initJSession():NO JSESSIONID\n");
        }

        try {
            uri = getServerURI().toString();

            // put slash between parts:
            if ((uri.endsWith("/") == false) && (config.servicePath.startsWith("/") == false)) {
                uri = uri + "/";
            }

            uri = uri + config.servicePath + "/" + config.jsessionInitPart;

            HttpPost postMethod = new HttpPost(uri);
            // get.setFollowRedirects(true);

            int result = executeAuthenticatedPut(postMethod, null, null);

            List<Cookie> cookies = this.httpClient.getCookieStore().getCookies();

            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(WebConst.COOKIE_JSESSIONID)) {
                    this.jsessionID = cookie.getValue();
                    logger.infoPrintf(" - new JSessionID     = %s\n", jsessionID);
                    logger.debugPrintf(" - Cookie Domain/Path = '%s','%s'\n", cookie.getDomain(),
                            cookie.getPath());
                }
            }
            checkHttpStatus(result, "initJSession(): Couldn't initialize JSessionID.", null, null);

            // all ok here.
        } catch (WebException e) {
            Reason reason = e.getReason();
            if (reason == Reason.FORBIDDEN) {
                throw new WebException(reason, "Failed to authenticate: Forbidden.\n"
                        + e.getMessage(), e);
            } else if (reason == Reason.UNAUTHORIZED) {
                if (this.config.useAuthentication() == false) {
                    throw new WebException(reason,
                            "Need proper authentication for this service, but authentication is disabled for:"
                                    + this.getServiceURI() + ".\n" + e.getMessage(), e);
                }
                throw new WebException(reason,
                        "Failed to authenticate: User or password wrong for URI:"
                                + this.getServiceURI() + ".\n" + e.getMessage(), e);
            } else {
                throw new WebException(reason, "Failed to initialize JSession to: "
                        + this.getServiceURI() + "\n" + e.getMessage(), e);
            }
        }
    }

    public void disconnect() throws WebException {
        //
        this.deleteJSession();
        if (httpClient == null)
            return;
        // Multi-threaded connection mananager:
        // When the HttpClient instance is no longer needed,
        // shut down the connection manager to ensure
        // immediate deallocation of all system resources
        httpClient.getConnectionManager().shutdown();
        this.httpClient = null;
    }

    protected void deleteJSession() throws WebException {
        //
        logger.debugPrintf("deleteJSession()\n");

        if (this.jsessionID == null) {
            logger.debugPrintf("deleteJSession(): sessionID already invalidated\n");
            return;
        }

        String uri;

        try {
            uri = getServerURI().toString();

            // put slash between parts:
            if ((uri.endsWith("/") == false) && (config.servicePath.startsWith("/") == false)) {
                uri = uri + "/";
            }

            uri = uri + config.servicePath + "/" + config.jsessionInitPart;

            HttpDelete delMethod = new HttpDelete(uri);
            // get.setFollowRedirects(true);

            int result = this.executeDelete(delMethod, null, null);
            checkHttpStatus(result, "deleteJSession(): Couldn't invalidate JSessionID.", null, null);
            // all ok here.
            jsessionID = null; // cleared
        } catch (WebException e) {
            Reason reason = e.getReason();

            if (reason == Reason.FORBIDDEN) {
                throw new WebException(reason, "Failed to authenticate: Forbidden.\n"
                        + e.getMessage(), e);
            } else if (reason == Reason.UNAUTHORIZED) {
                throw new WebException(reason,
                        "Failed to authenticate: User or password is wrong.\n" + e.getMessage(), e);
            } else {
                throw new WebException(reason, "Failed to initialize JSession:" + e.getMessage(), e);
            }
        }
    }

    protected void initSSL(CertificateStore certStore, boolean initHTTPSClient)
            throws CertificateStoreException {
        this.certStore = certStore;
        if (initHTTPSClient) {
            initHTTPS();
        }
    }

    protected void initHTTPS() throws CertificateStoreException {
        // Create SSL Socket factory with custom Certificate Store.
        // Default protocol is TLS (newer when SSL).
        // SSLContext sslContext = certStore.createSSLContext("SSLv3");
        SSLContext sslContext = certStore.createSSLContext(SslConst.PROTOCOL_TLS);
        AbstractVerifier verifier;

        if (config.sslOptions.disable_strict_hostname_checking) {
            verifier = new AllowAllHostnameVerifier();
        } else {
            verifier = new StrictHostnameVerifier();
        }

        // Create and register HTTPS socket factory
        SSLSocketFactory socketFactory = new SSLSocketFactory(sslContext, verifier);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("https", config.getPort(), socketFactory));
        ClientConnectionManager manager = this.httpClient.getConnectionManager();
        manager.getSchemeRegistry().register(new Scheme("https", config.getPort(), socketFactory));
    }

    // ========================================================================
    // Get,Put,Post and Delete methods
    // ========================================================================

    /**
     * Execute Put or Post method and handle the (optional) String response. Returns actual HTTP
     * Status. Method does not do any (http) response status handling. If the Put method succeeded
     * but the HTTP status != 200 then this value is returned and no Exception is thrown.
     * 
     * @param putOrPostmethod
     *            - HttpPut or HttpPost method to execute
     * @param responseTextHolder
     *            - Optional StringHolder for the Response: Put and Post might nor return any
     *            response.
     * @param contentTypeHolder
     *            - Optional StringHolder for the contentType (mimeType).
     * 
     * @return actual HTTP Status as returned by server.
     * @throws WebException
     *             if a communication error occurred.
     */
    protected int executeAuthenticatedPut(HttpRequestBase putOrPostmethod,
            StringHolder responseTextHolder, StringHolder contentTypeHolder) throws WebException {
        //
        if (this.httpClient == null) {
            throw new NullPointerException("HTTP Client not properly initialized: httpClient==null");
        }
        logger.debugPrintf("executePutOrPost():'%s'\n", putOrPostmethod.getRequestLine());

        boolean isPut = (putOrPostmethod instanceof HttpPut);
        boolean isPost = (putOrPostmethod instanceof HttpPost);

        if ((isPut == false) && (isPost == false)) {
            throw new IllegalArgumentException(
                    "Method class must be either HttpPut or HttpPost. Class="
                            + putOrPostmethod.getClass());
        }

        String actualUser;

        try {
            HttpResponse response;

            if (config.getUseBasicAuthentication()) {
                actualUser = config.getUsername();
                String passwdStr = new String(config.getPasswordChars());
                logger.debugPrintf("Using basic authentication, user=%s\n", actualUser);

                Header authHeader = new BasicHeader("Authorization", "Basic "
                        + (StringUtil.base64Encode((actualUser + ":" + passwdStr).getBytes())));
                putOrPostmethod.addHeader(authHeader);
            }

            response = httpClient.execute(putOrPostmethod);

            int status = handleStringResponse(
                    "executePutOrPost():" + putOrPostmethod.getRequestLine(), response,
                    responseTextHolder, contentTypeHolder);

            this.lastHttpStatus = status;
            return status;
        } catch (ClientProtocolException e) {
            if ((config.getPort() == 443) && config.isHTTP()) {
                throw new WebException(WebException.Reason.HTTP_CLIENTEXCEPTION,
                        "HTTP Protocol error: Trying to speak plain http to (SSL) port 443?\n"
                                + e.getMessage(), e);
            } else if (config.isHTTPS()) {
                throw new WebException(WebException.Reason.HTTP_CLIENTEXCEPTION,
                        "HTTPS Protocol error:" + e.getMessage(), e);
            } else {
                throw new WebException(WebException.Reason.HTTP_CLIENTEXCEPTION,
                        "HTTP Protocol error:" + e.getMessage(), e);
            }

        } catch (javax.net.ssl.SSLPeerUnverifiedException e) {
            throw new WebException(WebException.Reason.HTTPS_SSLEXCEPTION,
                    "SSL Error:Remote host not authenticated or couldn't verify host certificate.\n"
                            + e.getMessage(), e);
        } catch (javax.net.ssl.SSLException e) {
            // Super class
            throw new WebException(WebException.Reason.HTTPS_SSLEXCEPTION,
                    "SSL Error:Remote host not authenticated or couldn't verify host certificate.\n"
                            + e.getMessage(), e);
        } catch (java.net.NoRouteToHostException e) {
            throw new WebException(WebException.Reason.NO_ROUTE_TO_HOST_EXCEPTION,
                    "No route to host: Server could be down or unreachable.\n" + e.getMessage(), e);
        } catch (IOException e) {
            throw new WebException(WebException.Reason.IOEXCEPTION, e.getMessage(), e);
        }
    }

    /**
     * Execute Get Method and handle the String response. Returns actual HTTP Status. Does not do
     * any HTTP status handling.
     * 
     * @param getMethod
     *            - HttpGet method to execute
     * @param responseTextHolder
     *            - Optional StringHolder for the Response
     * @param contentTypeHolder
     *            - Optional StringHolder for the contentType.
     * @return - actual HTTP Status as returned by server.
     * @throws WebException
     *             if a communication error occurred.
     */
    protected int executeGet(HttpGet getMethod, StringHolder responseTextHolder,
            StringHolder contentTypeHolder) throws WebException {
        //
        logger.debugPrintf("executeGet():'%s'\n", getMethod.getRequestLine());

        if (this.httpClient == null) {
            throw new NullPointerException("HTTP Client not properly initialized: httpClient==null");
        }

        try {
            HttpResponse response;
            response = httpClient.execute(getMethod);

            logger.debugPrintf("--------------- HttpGet Response -------------------------\n");
            logger.debugPrintf("%s\n", response.getStatusLine());

            int status = handleStringResponse("HttpGet:" + getMethod.getRequestLine(), response,
                    responseTextHolder, contentTypeHolder);

            this.lastHttpStatus = status;
            return status;

        } catch (ClientProtocolException e) {
            throw new WebException(WebException.Reason.HTTP_CLIENTEXCEPTION, e.getMessage(), e);
        } catch (IOException e) {
            throw new WebException(WebException.Reason.IOEXCEPTION, e.getMessage(), e);
        } finally {
            getMethod.releaseConnection();
        }
    }

    protected int executeDelete(HttpDelete delMethod, StringHolder responseTextHolder,
            StringHolder contentTypeHolder) throws WebException {
        //
        logger.debugPrintf("executeDelete():'%s'\n", delMethod.getRequestLine());

        try {
            HttpResponse response;
            response = httpClient.execute(delMethod);

            logger.debugPrintf("--------------- HttpDelete Response -------------------------\n");
            logger.debugPrintf("%s\n", response.getStatusLine());

            int status = handleStringResponse("HttpDelete:" + delMethod.getRequestLine(), response,
                    responseTextHolder, contentTypeHolder);
            this.lastHttpStatus = status;
            return status;

        } catch (ClientProtocolException e) {
            throw new WebException(WebException.Reason.HTTP_CLIENTEXCEPTION, e.getMessage(), e);
        } catch (IOException e) {
            throw new WebException(WebException.Reason.IOEXCEPTION, e.getMessage(), e);
        } finally {
            delMethod.releaseConnection();
        }
    }

    protected int handleStringResponse(String queryStr, HttpResponse response,
            StringHolder responseTextHolder, StringHolder contentTypeHolder) throws WebException {
        //
        HttpEntity entity = response.getEntity();

        StatusLine status = response.getStatusLine();
        int statusCode = status.getStatusCode();
        this.lastHttpStatus = statusCode;

        if (entity == null) {
            throw new WebException(WebException.Reason.IOEXCEPTION, lastHttpStatus,
                    "Response Entity is NULL");
        }

        InputStream contentInputstream = null;
        Header contentEncoding = entity.getContentEncoding();
        Header contentType = entity.getContentType();
        String contentTypeValue = null;

        // String contentEncodingValue = null;
        // if (contentEncoding != null)
        // contentEncodingValue = contentEncoding.getValue();

        if (contentType != null) {
            contentTypeValue = contentType.getValue();
        }

        logger.debugPrintf("--------------- Response -------------------------\n");
        logger.debugPrintf("> Response Status line      = %s\n", response.getStatusLine());
        logger.debugPrintf("> Response content length   = %d\n", entity.getContentLength());
        logger.debugPrintf("> Response content type     = %s\n", contentTypeValue);
        logger.debugPrintf("> Response content encoding = %s\n", contentEncoding);

        for (Header header : new Header[] { contentType, contentEncoding }) {
            if (header != null) {
                logger.debugPrintf("  - Header '%s'='%s'\n", header.getName(), header.getValue());
                HeaderElement[] els = header.getElements();
                for (HeaderElement el : els) {
                    logger.debugPrintf("  - - HeaderElement %s=%s\n", el.getName(), el.getValue());
                }
            }
        }

        try {
            contentInputstream = entity.getContent();
            String text = resourceLoader.readText(contentInputstream, "UTF-8");

            try {
                contentInputstream.close();
            } catch (IOException e) {
                logger.logException(PLogger.DEBUG, e, "IOException when closing InputStream:%s\n");
            }

            logger.debugPrintf(">>> ------------ String response -------------\n");
            logger.debugPrintf("%s\n", text);
            logger.debugPrintf(">>> ------------ String Response -------------\n");

            if (responseTextHolder != null)
                responseTextHolder.value = text;

            if (contentTypeHolder != null)
                contentTypeHolder.value = contentTypeValue;
        } catch (IOException e) {
            throw new WebException(WebException.Reason.IOEXCEPTION, e.getMessage(), e);
        }

        org.apache.http.Header[] headers = response.getAllHeaders();

        for (int i = 0; i < headers.length; i++) {
            logger.debugPrintf("  - Header: '%s'='%s'\n", headers[i].getName(),
                    headers[i].getValue());
        }

        return statusCode;
    }

    public int doGet(String query, StringHolder resultTextHolder, StringHolder contentTypeHolder)
            throws WebException {
        return doGet(resolve(query), resultTextHolder, contentTypeHolder);
    }

    /**
     * Performs a managed Http Get request using the relative query String.
     * 
     * @param query
     *            - Relative URI part to put after the service Uri.
     * @param resultTextHolder
     *            - StringHolder for the Get Response.
     * @param contentTypeHolder
     *            - Optional StringHolder for the contentType (mimetype).
     * @return - Filtered HTTP Status
     * @throws WebException
     *             if a common HTTP error occurred.
     */
    public int doGet(URI uri, StringHolder resultTextHolder, StringHolder contentTypeHolder)
            throws WebException {
        HttpGet getMethod = new HttpGet(uri);
        // EXECUTE
        int result = executeGet(getMethod, resultTextHolder, contentTypeHolder);
        // POST
        checkHttpStatus(result, "doGet(): Failed for uri:'" + uri + "'.", resultTextHolder,
                contentTypeHolder);
        return result;
    }

    public ResponseInputStream doGetInputStream(String query) throws WebException {
        return doGetInputStream(resolve(query));
    }

    public ResponseInputStream doGetInputStream(URI uri) throws WebException {
        //
        HttpGet getMethod = new HttpGet(uri);

        try {
            logger.debugPrintf("doGetInputStream()[thread:%d]:'%s'\n", Thread.currentThread()
                    .getId(), getMethod.getRequestLine());

            HttpResponse response;
            response = httpClient.execute(getMethod);

            logger.debugPrintf("--------------- HttpGet Response -------------------------\n");
            logger.debugPrintf(" - statusLine: %s\n", response.getStatusLine());

            HttpEntity entity = response.getEntity();

            if (entity == null) {
                logger.debugPrintf("NULL Entity\n");
                getMethod.releaseConnection();
                int httpStatus = response.getStatusLine().getStatusCode();
                throw new WebException(WebException.Reason.IOEXCEPTION, httpStatus,
                        "Response Entity is NULL");
            }

            // Status must be ok, for the content to be streamed:
            handleResponseStatus(response, "doGetInputStream:'" + getMethod + "' for:+" + uri
                    + "\n");
            //
            ResponseInputStream responseInps = new ResponseInputStream(this, getMethod, entity);

            return responseInps;
        } catch (ClientProtocolException e) {
            throw new WebException(WebException.Reason.HTTP_CLIENTEXCEPTION, e.getMessage(), e);
        } catch (IOException e) {
            throw new WebException(WebException.Reason.IOEXCEPTION, e.getMessage(), e);
        }
    }

    /**
     * Performs a managed Http DELETE request using the relative query String.
     * 
     * @param query
     *            - Relative URI part to put after the service Uri.
     * @param resultTextHolder
     *            - StringHolder for the DELETE Response.
     * @param contentTypeHolder
     *            - Optional StringHolder for the contentType (mimetype).
     * @return - Filtered HTTP Status
     * @throws WebException
     *             if a common HTTP error occurred.
     */
    public int
            doDelete(String query, StringHolder resultTextHolder, StringHolder contentTypeHolder)
                    throws WebException {
        //
        URI uri = null;
        // PRE
        uri = resolve(query);
        HttpDelete delMethod = new HttpDelete(uri);
        // EXECUTE
        int result = executeDelete(delMethod, resultTextHolder, contentTypeHolder);
        // POST
        checkHttpStatus(result, "doDelete(): Failed for query:'" + query + "'.", resultTextHolder,
                contentTypeHolder);
        return result;
    }

    public int doPut(String query, StringHolder resultTextHolder, StringHolder contentTypeHolder)
            throws WebException {
        return doPut(resolve(query), resultTextHolder, contentTypeHolder);
    }

    /**
     * Performs a managed Http Put request using the relative query String.
     * 
     * @param query
     *            - Relative URI part to put after the service Uri.
     * @param resultTextHolder
     *            - StringHolder for the Get Response.
     * @param contentTypeHolder
     *            - Optional StringHolder for the contentType.
     * @return - Filtered HTTP Status. Recognized error codes are transformed to WebExceptions.
     * @throws WebException
     *             if a common HTTP error occurred.
     */
    public int doPut(URI uri, StringHolder resultTextH, StringHolder contentTypeH)
            throws WebException {
        // Pre:
        HttpPut putMethod = new HttpPut(uri);
        // Method:
        int status = executePut(putMethod, resultTextH, contentTypeH);
        // Status:
        return checkHttpStatus(status, "doPut:'" + uri, resultTextH, contentTypeH);
    }

    public int doPutFile(String query, String filePath, StringHolder resultStrH,
            PutMonitor putMonitor) throws WebException {
        return doPutFile(resolve(query), filePath, resultStrH, putMonitor);
    }

    /**
     * Performs a managed HttpPut with a file as attachment. Recognized HTTP Error codes are
     * transformed to Web Exceptions.
     * 
     * @param query
     *            - relative url to use in HttpPut.
     * @param file
     *            - java File to upload. Must exists
     * @return filtered Http Status. Common error codes are transformed to (Web)Exceptions
     * @throws WebException
     */
    public int doPutFile(URI uri, String filePath, StringHolder resultStrH, PutMonitor putMonitor)
            throws WebException {
        //
        logger.debugPrintf("doPutFile() file='%s' to:%s\n", filePath, uri);
        MultipartEntity multiPart = new MultipartEntity();

        // Java.io.File:
        // FileBody fileB = new FileBody(file);
        // Add the part to the MultipartEntity.

        // FSNode:
        FSPath node;
        try {
            node = FSUtil.getDefault().newFSPath(filePath);
        } catch (IOException e) {
            throw new WebException(Reason.IOEXCEPTION, "Failed to resolve File:" + filePath, e);
        }
        StringHolder contentTypeH = new StringHolder();
        FSNodeBody fileB = new FSNodeBody(node, putMonitor);
        multiPart.addPart("file", fileB);

        HttpPut putMethod = new HttpPut(uri);
        putMethod.setEntity(multiPart);

        // Execute
        int status = executePut(putMethod, resultStrH, contentTypeH);
        // Status:
        return checkHttpStatus(status, "doPutFile:'" + uri + "', file=" + filePath, resultStrH,
                contentTypeH);
    }

    public int doPutBytes(String query, byte bytes[], String optMimeType, StringHolder resultStrH,
            PutMonitor optPutMonitor) throws WebException {
        return doPutBytes(resolve(query), bytes, optMimeType, resultStrH, optPutMonitor);
    }

    /**
     * Performs a managed HttpPut with a byte array as attachment. Recognized HTTP Error codes are
     * transformed to Web Exceptions.
     * 
     * @param query
     *            - relative url to use in HttpPut.
     * @param bytes
     *            - byes to be uploaded as attachemnt.
     * @param mimeType
     *            - optional mimeType of content, if null "application/octet-stream" is used.
     * @param file
     *            - java File to upload. Must exists
     * @return filtered Http Status. Common error codes are transformed to (Web)Exceptions
     * @throws WebException
     */
    public int doPutBytes(URI uri, byte bytes[], String mimeType, StringHolder resultStrH,
            PutMonitor optPutMonitor) throws WebException {
        //
        if (bytes == null) {
            throw new NullPointerException("Argument by bytes[] is NULL!");
        }

        logger.debugPrintf("doPutBytes() numBytes=%s to:%s\n", bytes.length, uri);

        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        StringHolder contentTypeH = new StringHolder();
        HttpPut putMethod = new HttpPut(uri.toString());
        MultipartEntity multiPart = new MultipartEntity();
        ByteBufferBody byteBody = new ByteBufferBody(bytes, mimeType, "bytes", optPutMonitor);

        // Add the part to the MultipartEntity.
        multiPart.addPart("bytes", byteBody);
        putMethod.setEntity(multiPart);

        // Execute Method:
        int status = executePut(putMethod, resultStrH, contentTypeH);
        // Status
        return this.checkHttpStatus(status, "doPutBytes() uri=" + uri, resultStrH, contentTypeH);
    }

    public ResponseOutputStream doPutOutputStream(String query, StringHolder resultStrH)
            throws WebException {
        URI uri = resolve(query);
        HttpPut putMethod = new HttpPut(uri);
        logger.debugPrintf("doPutOutputStream():'%s'\n", putMethod.getRequestLine());
        throw new WebException("Not Yet Supported.");
    }

    /**
     * Perform a managed Http Put with a String as Attachment.
     */
    public int doPutString(String query, String text, StringHolder resultTextH, boolean inbody)
            throws WebException {
        //
        HttpPut putMethod = null;
        URI uri = resolve(query);
        StringHolder contentTypeH = new StringHolder();
        putMethod = new HttpPut(uri.toString());
        MultipartEntity multiPart = new MultipartEntity();
        StringBody stringB;
        //
        try {
            stringB = new StringBody(text);
            multiPart.addPart("caption", stringB);
        } catch (UnsupportedEncodingException e) {
            new WebException(WebException.Reason.IOEXCEPTION, "Unsupported Encoding Exception:"
                    + e.getMessage(), e);
        }
        //
        putMethod.setEntity(multiPart);
        // Method
        int status = executePut(putMethod, resultTextH, contentTypeH);
        // Status
        return this.checkHttpStatus(status, "doPutString():" + query, resultTextH, contentTypeH);
    }

    /**
     * Actual call to HttpClient.execute. Handles response and closed the connection.
     */
    protected int executePut(HttpPut putMethod, StringHolder resultStrH, StringHolder contentTypeH)
            throws WebException {
        //
        try {
            HttpResponse response;
            response = httpClient.execute(putMethod);

            if (resultStrH == null) {
                resultStrH = new StringHolder();
            }

            URI uri = putMethod.getURI();
            int status = handleStringResponse("executePut():" + uri, response, resultStrH,
                    contentTypeH);
            this.lastHttpStatus = status;

            logger.debugPrintf("v() result='%s'\n", resultStrH.value);

            return status;
        } catch (IOException e) {
            throw new WebException(WebException.Reason.IOEXCEPTION,
                    "IOException:" + e.getMessage(), e);
        } finally {
            putMethod.releaseConnection();
        }
    }

    // ========================================================================
    // Other
    // ========================================================================

    /**
     * Resolve relative URI against this Service URI of the client.
     * 
     * @param querystr
     *            - relative URI part.
     * @return absolute resolved URI
     * @throws URISyntaxException
     *             if querystr contains illegal characters.
     */
    public URI resolve(String querystr) throws WebException {
        //
        try {
            // absolute server URI no service path.
            String uriStr = this.getServerURI().toString();

            if (uriStr.endsWith("/") == false) {
                uriStr += "/";
            }

            if (querystr.startsWith("/")) {
                // absolute query, replace path+query.
                uriStr = uriStr + querystr;
            } else {
                // relative query, include service path.
                uriStr = uriStr + "/" + config.servicePath + "/" + querystr;
            }

            logger.debugPrintf("resolve(): '%s'\n", querystr);

            URI uri = new URI(uriStr);

            logger.debugPrintf("resolve(): ... => '%s'\n", uri);

            return uri;
        } catch (URISyntaxException e) {
            throw new WebException("URISyntaxException:Failed to parse Query:" + querystr, e);
        }
    }

    public String getCookieValue(String name) {
        List<Cookie> cookies = httpClient.getCookieStore().getCookies();

        if ((cookies == null || cookies.size() <= 0))
            return null;

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private void handleResponseStatus(HttpResponse response, String message) throws WebException {
        StatusLine statusLine = response.getStatusLine();
        int status = statusLine.getStatusCode();
        this.lastHttpStatus = status;
        this.checkHttpStatus(status, message, null, null);
    }

    /**
     * Check common HTTP Status Error and throw appropriate Exception.
     * 
     * @param httpStatus
     *            - actual HTTP Status code
     * @param message
     * @param contentTypeHolder
     * @throws WebException
     *             Matching WebException
     */
    private int checkHttpStatus(int httpStatus, String message, StringHolder responseH,
            StringHolder contentTypeH) throws WebException {
        String response = null;
        String responseType = null;

        if (responseH != null) {
            response = responseH.value;
        }

        if (contentTypeH != null) {
            responseType = contentTypeH.value;
        }

        if (response != null) {
            message = message + "\n--- response[type=" + responseType + "] ---\n" + response;
        }

        if (httpStatus == org.apache.http.HttpStatus.SC_NOT_FOUND) {
            throw new WebException(WebException.Reason.RESOURCE_NOT_FOUND, httpStatus, message
                    + "\nReason:Resource Not Found.", responseType, response);
        } else if (httpStatus == org.apache.http.HttpStatus.SC_UNAUTHORIZED) {
            throw new WebException(WebException.Reason.UNAUTHORIZED, httpStatus, message
                    + "\nReason:Unauthorized", responseType, response);
        } else if (httpStatus == org.apache.http.HttpStatus.SC_FORBIDDEN) {
            throw new WebException(WebException.Reason.UNAUTHORIZED, httpStatus, message
                    + "\nReason:Forbidden", responseType, response);
        } else if (httpStatus == org.apache.http.HttpStatus.SC_EXPECTATION_FAILED) {
            throw new WebException(WebException.Reason.INVALID_REQUEST, httpStatus, message
                    + "\nReason:Expectation Failed/Invalid request.", responseType, response);
        } else if (isHttpStatusOK(httpStatus) == false) {
            throw new WebException(WebException.Reason.HTTP_ERROR, httpStatus, message + "Error ("
                    + httpStatus + "):" + httpStatus, responseType, response);
        }

        return httpStatus;
    }

    public boolean isHttpStatusOK(int status) {
        // check common HTTP status codes:
        switch (status) {
            case (HttpStatus.SC_CREATED):
            case (HttpStatus.SC_OK):
            case (HttpStatus.SC_CONTINUE):
            case (HttpStatus.SC_ACCEPTED): {
                return true;
            }
            default: {

            }
        }
        // 2XX == OK
        if ((status >= 200) && (status < 299))
            return true;

        // 4XX == Permanent failure:
        if ((status >= 400) && (status < 499))
            return false;

        // 5XX == Temporary failure:
        if ((status >= 500) && (status < 599))
            return false;

        return false;
    }

    /**
     * An WebClient is a statefull client. Current HTTP status can returned by calling this method.
     * 
     * @return
     */
    public int getLastHttpStatus() {
        return this.lastHttpStatus;
    }

    public boolean uiPromptPassfield(String message, SecretHolder secretHolder) {
        if (getUI() == null) {
            return false; // no UI present!
        }

        boolean result = getUI().askAuthentication(message, secretHolder);

        if (result == true) {
            if (secretHolder.value == null) {
                return false;
            }
        }

        return result;
    }

    // === Misc. ===

    public String toString() {
        return "WebClient:[uri:" + this.getServiceURI() + ", isAuthenticated:"
                + this.isAuthenticated() + "]";
    }

    protected PLogger getLogger() {
        return logger;
    }

}
