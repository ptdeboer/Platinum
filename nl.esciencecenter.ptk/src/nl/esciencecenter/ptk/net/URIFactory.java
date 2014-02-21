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

package nl.esciencecenter.ptk.net;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import nl.esciencecenter.ptk.object.Duplicatable;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.URLUTF8Encoder;

/**
 * Generic URI Factory. Most methods are shadowed from URI so this class can
 * also be used as an URI Proxy object replacing java.net.URI. <br>
 * Use this factory to chain a sequence of URI modification methods. Use
 * toURI() to create the resulting URI.
 * <p>
 * For example:<br>
 * <code>
 *  String baseUri=...; <br>
 *  URI theUri = new URIFactory(baseUri).setHostname("remote").appendPath("webService").uriResolve("?query").toURI(); 
 * </code>
 * 
 * @author Piter T. de Boer
 */
public final class URIFactory implements Serializable, Cloneable, Duplicatable<URIFactory>
{
    private static final long serialVersionUID = 282430272904693557L;

    /** Path seperator character for URIs = '/' */
    public final static char URI_SEP_CHAR = '/';

    /** Windows backslash or '\\' */
    public final static char DOS_SEP_CHAR = '\\';

    /** Seperator character but as String ( "/" ) */
    public final static String URI_SEP_CHAR_STR = "" + URI_SEP_CHAR;

    public final static char QUERY_CHAR = '?';

    public final static char FRAGMENT_CHAR = '#';

    /**
     * Default URI attribute separator '&amp;' in URL:
     * http://../?ArgumentA=1&amp;ArgumentB=2"
     */
    public static final String ATTRIBUTE_SEPERATOR = "&";

    /**
     * URI list seperator ';' used to parse (create) URI string representations
     */
    public static final String URI_LIST_SEPERATOR = ";";

    // =======================================================================
    // Class Methods
    // =======================================================================

    public static String uripath(String orgpath)
    {
        return uripath(orgpath, true);
    }

    public static String uripath(String orgpath, boolean makeAbsolute)
    {
        // use local File Seperator to normalize path.
        return uripath(orgpath, makeAbsolute, java.io.File.separatorChar);
    }

    /**
     * Produce URI compatible path and do other normalizations.
     * 
     * <ul>
     * <li>Flip Local File Separator char <code>localSepChar</code> to (URI
     * compatible) forward slashes
     * <li>Change DOS paths into absolute DOS paths: for example: 'c:' into
     * '/c:/'
     * <li>Prefixes all paths with '/' to make it absolute , unless
     * makeAbsolute=false
     * </ul>
     * 
     * @param orgpath
     *            - The original DOS or linux path.
     * @param makeAbsolute
     *            - prefix optional relative paths with '/' to make them
     *            absolute.
     * @param localSepChar
     *            - separator char to 'flip' to URI separator char '/' .
     * 
     * @return The normalized and decoded URI path.
     */
    public static String uripath(String orgpath, boolean makeAbsolute, char localSepChar)
    {
        if (orgpath == null)
            return ""; // default path="";

        if (orgpath.length() == 0)
            return ""; // default path="";

        String newpath = orgpath;

        // Ia) If file seperator is '\', replace with URI forward slash.
        newpath = orgpath.replace(DOS_SEP_CHAR, URI_SEP_CHAR);

        // Ib) strip optional double slashes "c:\subdir\/path" =>
        // "c:/subdir//path" => c:/subdir/path
        newpath = newpath.replaceAll(URI_SEP_CHAR + "+", URI_SEP_CHAR_STR);

        // II) Convert relative path to absolute by inserting '/'
        // c:/subdir/path => /c:/subdir/path
        if (makeAbsolute == true)
        {
            if (newpath.charAt(0) != URI_SEP_CHAR)
            {
                newpath = URI_SEP_CHAR + newpath;
            }
        }

        //
        // III) Windows conversion
        // "C:" => /C:/" is always absolute !

        // Canonical paths vs. Absolute paths:
        /*
         * Yet another windows relative path hack: Windows interprets "C:..."
         * also as relative if the current directory on "C:" is 'C:/windows',
         * the path "C:subdir" will result in "C:/windows/subdir"
         * 
         * Add extra '/' to make it absolute C:/ Note that java (under windows)
         * accepts paths like '/c:/dir' !
         */

        // IIIa insert '/' if path starts with "C:" or "[a-zA-Z]:"
        // IIIb convert "/c:path => /c:/path"

        String dosPrefixRE = "[/]*[a-zA-Z]:.*"; // beware of optional slash
                                                // before path here:

        // Detect DOS path by matching against "C:" like paths.
        // Since single character schemes in URIs do not exist, this RE will
        // match against DOS paths:

        if ((newpath.length() >= 2) && (newpath.matches(dosPrefixRE)))
        {
            // Since "C:" is already an absolute path, prefix it with "/" as
            // follows "C:" => "/C:"
            if (newpath.charAt(0) != '/')
            {
                newpath = "/" + newpath;
            }

            // insert "/" between ":" and path as follows: "/C:<path>" =>
            // "/C:/<path>"
            if ((newpath.length() >= 4) && (newpath.charAt(2) == ':') && (newpath.charAt(3) != URI_SEP_CHAR))
            {
                newpath = "/" + newpath.charAt(1) + ":/" + newpath.substring(3);
            }
        }

        // Convert (exact matching) DOS relative path "/C:" to absolute "/C:/"
        // The actual relative path of the drive could be resolved here.
        if ((newpath.length() == 3) && (newpath.charAt(2) == ':'))
        {
            newpath = newpath + URI_SEP_CHAR;
        }
        else if ((newpath.length() == 4) && (newpath.charAt(3) == URI_SEP_CHAR))
        {
            // keep "/C:/..."
        }
        else if ((newpath.length() > 1) && (newpath.charAt(newpath.length() - 1) == URI_SEP_CHAR))
        {
            // Strip last '/' if it isn't an absolute (Windows) drive path path
            // like example: '/C:/'
            newpath = newpath.substring(0, newpath.length() - 1);
        }

        // Extra: If during the conversion double slashes "//" have been
        // created, reduced
        // them to a single one:
        newpath = newpath.replaceAll("/+", "/");

        // Debug("uri path="+newpath);
        return newpath;
    }

    /**
     * Remove extension part of filename.
     * 
     * @param filename
     *            filename of full path of file.
     * @return stripped filename withot extension.
     */
    public static String stripExtension(String filename)
    {
        if (filename == null)
            return null;

        int index = filename.length();
        index--;

        // scan last part of path

        while ((index >= 0) && (filename.charAt(index) != '.'))
        {
            index--;
        }

        // index now points to '.' char or is -1; (before beginning of the name)

        if (index < 0)
            return filename; // no dot => NO extension !

        return filename.substring(0, index);
    }

    /**
     * Return extension part of filename.
     */
    public static String extension(String filename)
    {
        if (filename == null)
            return null;

        int index = filename.length();
        index--;

        // scan last part of path

        while ((index >= 0) && (filename.charAt(index) != '.'))
        {
            index--;
        }

        // index now points to '.' char or is -1; (before beginning of the name)
        index++; // skip '.';

        return filename.substring(index, filename.length());
    }

    /**
     * Encode String to URI compatible values. Uses % encoding.
     * 
     * @param rawString
     *            - actual string
     * @return URI encode String.
     */
    public static String encodeQuery(String rawString)
    {
        String encoded = URLUTF8Encoder.encode(rawString);
        return encoded;
    }

    public static String encodePath(String rawString)
    {
        String encoded = URLUTF8Encoder.encode(rawString);
        return encoded;
    }

    /**
     * Returns basename part (last part) of path String.
     */
    public static String basename(String path)
    {
        // default cases: null,empty and root path:

        if (path == null)
            return null;

        if (path.equalsIgnoreCase(""))
            return "";

        int index = 0;
        int strlen = path.length();

        index = strlen - 1;// start at end of string

        if (path.equalsIgnoreCase(URI_SEP_CHAR_STR))
            return URI_SEP_CHAR_STR;

        // special case, path ENDS with '/' which must be ignored

        if (path.charAt(index) == URI_SEP_CHAR)
        {
            index--;
            strlen--;
        }

        while ((index >= 0) && (path.charAt(index) != URI_SEP_CHAR))
        {
            index--;
        }

        index++;

        // index points to character after '/' or is zero
        return path.substring(index, strlen);
    }

    /**
     * Returns the dirname part of the URI compatible path ! (parent directory
     * path) of path. Note: use URI.uripath to sanitize and normalize a path!
     * <p>
     * Special cases:
     * <ul>
     * <li>dirname of null is null
     * <li>dirname of the empty string "" is ""
     * <li>The dirname of "/" = "/"
     * </ul>
     * 
     * @see basename
     */
    public static String dirname(String path)
    {
        // null path
        if (path == null)
            return null;

        int index = 0;
        int strlen = path.length();

        index = strlen - 1;// start at end of string

        // Empty and root path:

        // relative path, cannot return dirname
        if (path.equalsIgnoreCase(""))
            return "";

        // root path of root is root itself
        if (path.equalsIgnoreCase(URI_SEP_CHAR_STR))
            return URI_SEP_CHAR_STR;

        // special case, path ENDS with '/' which must be ignored
        if (path.charAt(index) == URI_SEP_CHAR)
        {
            index--;
            strlen--;
        }

        // move backwards to first seperator
        while ((index >= 0) && (path.charAt(index) != URI_SEP_CHAR))
        {
            index--;
        }

        if (index == 0)
        {
            // first char (path[0]) == '/' => special case root dir encountered:
            return URI_SEP_CHAR_STR;
        }
        else if (index < 0)
        {
            // no seperator found: this means this is a RELATIVE path with no
            // parent dirname !
            return "";
        }

        // index points to character '/'.

        return path.substring(0, index);
    }

    /**
     * Create URIFactory from Opaque URI. Only the scheme is parsed. The part
     * after the scheme is used as-is.
     * 
     * @param opaqueUri
     *            - Opaque URI String.
     * @return URIFactory constructed from Opaque URI.
     */
    public static URIFactory createOpaque(String opaqueUri)
    {
        int index = opaqueUri.indexOf(':');
        if (index < 0)
        {
            return new URIFactory(null, opaqueUri); // relative URI)
        }

        String scheme = opaqueUri.substring(0, index);
        String ssp = opaqueUri.substring(index + 1, opaqueUri.length());
        // Initialize with scheme + schemespefic part only.
        URIFactory uriFac = new URIFactory();
        uriFac.init(scheme, null, null, 0, ssp, null, null, true);
        return uriFac;
    }

    // =======================================================================
    // Instance
    // =======================================================================

    private String scheme;

    private String hostname;

    private int port;

    private String userInfo;

    /**
     * Contains path <em>or</em> scheme specific part without authorization,etc
     */
    private String pathOrReference;

    private String query;

    private String fragment;

    // === Field guards === //

    private boolean isOpaque = false;

    public URIFactory(URI uri)
    {
        if (uri == null)
            return;

        init(uri);
    }

    protected URIFactory()
    {
        ;
    }

    public URIFactory(final String uristr) throws URISyntaxException
    {
        init(uristr);
    }

    /**
     * Constructs Opaque URI, keeping schemeSpecificPart 'as-is'. URI Fields may
     * be parsed.
     */
    public URIFactory(String scheme, String schemeSpecificPart)
    {
        init(scheme, null, null, -1, schemeSpecificPart, null, null, true);
    }

    public URIFactory(String scheme, String host, int port, String path)
    {
        init(scheme, null, host, port, path, null, null, false);
    }

    public URIFactory(String scheme, String userInfo, String host, int port, String path)
    {
        init(scheme, userInfo, host, port, path, null, null, false);
    }

    public URIFactory(String scheme, String userInfo, String host, int port, String path, String query, String fragment)
    {
        init(scheme, userInfo, host, port, path, query, fragment, false);
    }

    public URIFactory(final URL url) throws URISyntaxException
    {
        init(url.toURI());
    }

    public URIFactory(final URIFactory uri)
    {
        this.copyFrom(uri);
    }

    /**
     * Initialize by smartly parsing the provided String. Checks whether the
     * string is encoded or decoded.
     * 
     * @param uriStr
     *            - Relative or Absolute URI String. Might be URI encoded.
     * @throws URISyntaxException
     */
    protected void init(final String uriStr) throws URISyntaxException
    {
        if (uriStr == null)
        {
            throw new URISyntaxException("<NULL>", "URI String can not be null");
        }
        
        URISyntaxException ex;

        // java.net.URI parsing is bo
        int index = uriStr.indexOf(':');

        String sspStr = null;

        if (index >= 0)
            sspStr = uriStr.substring(index + 1, uriStr.length());

        if (sspStr != null)
        {
            // Parse: "scheme:", "scheme:/" "scheme://". Triple slash is parsed as empty authority URI
            if (StringUtil.isEmpty(sspStr) || StringUtil.equals(sspStr, "/") || StringUtil.equals(sspStr, "//"))
            {
                // create scheme only URI

                this.scheme = uriStr.substring(0, index);
                
                if ("/".equals(sspStr))
                {
                    this.pathOrReference="/"; 
                }
                // check scheme:// 
                
                return;
            }
        }

        try
        {
            URI uri = new URI(uriStr);
            // use URI initializer: contains patches for URI parsing ! (better
            // use initializors!)
            init(uri);
            return;
        }
        catch (URISyntaxException e1)
        {
            ex = e1;
            // Be Flexible: could be not encoded string: try to encode it first
            // There is no way to detect whether the string is encoded if it
            // contains a valid '%'.
            try
            {
                // Try to encode the String.

                URI uri = new URI(encodePath(uriStr));
                init(uri); // use URI initializer
                return;
            }
            catch (URISyntaxException e2)
            {
                ex = e2;
            }
        }

        throw ex;
    }

    /**
     * Initialize Factory using provided URI object.
     */
    protected void init(final URI _uri)
    {
        // All URIs are normalized!
        URI uri = _uri.normalize();

        String newScheme = uri.getScheme();

        boolean hasAuth = StringUtil.notEmpty(uri.getAuthority());

        String newUserInf = uri.getUserInfo();

        // Resolve hostname here ?
        String newHost = uri.getHost();

        int newPort = uri.getPort();

        // ===========
        // Hack to get path,Query and Fragment part from SchemeSpecific part if
        // the URI is a reference URI.
        // The java.net.URI doesn't parse "file:relativePath?query" correctly
        // ===========
        String pathOrRef = null;
        String newQuery = null;
        String newFraq = null;

        if ((hasAuth == false) && (uri.getRawSchemeSpecificPart().startsWith("/") == false))
        {
            // Parse Reference URI or Relative URI which is not URL compatible:
            // hack to parse query and fragment from non URL compatible
            // URI
            try
            {
                // check: "ref:<blah>?query#frag"
                URI tempUri = null;

                tempUri = new URI(newScheme + ":/" + uri.getRawSchemeSpecificPart());
                String path2 = tempUri.getPath();
                // get path but skip added '/':
                // set Path but keep as Reference ! (path is reused for that)

                pathOrRef = path2.substring(1, path2.length());
                newQuery = tempUri.getQuery();
                newFraq = tempUri.getFragment();
            }
            catch (URISyntaxException e)
            {
                // Global.errorPrintStacktrace(e);
                // Reference URI: ref:<blahblahblah> without starting '/' !
                // store as is without parsing:
                pathOrRef = uri.getRawSchemeSpecificPart();
            }
        }
        else
        {
            // plain PATH URI: get Path,Query and Fragment.
            pathOrRef = uri.getPath();
            newQuery = uri.getQuery();
            newFraq = uri.getFragment();
        }

        // ================================
        // use normalized initializer
        // ================================
        init(newScheme, newUserInf, newHost, newPort, pathOrRef, newQuery, newFraq, false);
    }

    /**
     * Master Initializer. All fields are given and no exception is thrown.
     * Fields should be decoded. * Path can be relative or absolute but must be
     * normalized with forward slashes.
     */
    private void init(String newscheme, String userinf, String newhost, int newport, String newpath, String newquery,
            String newfrag, boolean isOpaque)
    {
        this.isOpaque = isOpaque;

        // must be null or uri will add empty values
        if (StringUtil.isEmpty(newhost))
            newhost = null; // null => no hostname

        if (StringUtil.isEmpty(userinf))
            userinf = null; // null => no userinfo

        if (StringUtil.isEmpty(newquery))
            newquery = null; // null => no userinfo

        if (StringUtil.isEmpty(newfrag))
            newfrag = null; // null => no userinfo

        // ===
        // AUTHORITY ::= [ <userinfo> '@' ] <hostname> [ ':' <port> ]
        // ===

        // ===
        // Feature: Strip ':' after scheme
        // ===
        if ((newscheme != null) && (newscheme.endsWith(":")))
        {
            newscheme = newscheme.substring(0, newscheme.length() - 1);
        }

        // Store duplicates of Strings
        this.scheme = StringUtil.duplicate(newscheme);

        // authority
        this.userInfo = StringUtil.duplicate(userinf);
        this.hostname = StringUtil.duplicate(newhost);
        this.port = newport;

        // ===
        // Relative URIs do not have a Scheme nor Authority !
        // examples: "dirname/tmp", "#label", "?query#fragment","local.html",
        //
        // Paths:
        // Sanitize path, but keep relative paths or reference paths intact
        // if there is no authority !

        if (isOpaque == false)
        {
            newpath = uripath(newpath, this.hasAuthority());
        }

        // parts
        this.pathOrReference = StringUtil.duplicate(newpath);
        this.query = StringUtil.duplicate(newquery);
        this.fragment = StringUtil.duplicate(newfrag);
    }

    @Override
    public URIFactory clone()
    {
        return duplicate();
    }

    /**
     * Return full copy of this object
     */
    @Override
    public URIFactory duplicate()
    {
        URIFactory fac = new URIFactory();
        fac.copyFrom(this);
        return fac;
    }

    @Override
    public boolean shallowSupported()
    {
        return false;
    }

    @Override
    public URIFactory duplicate(boolean shallow)
    {
        return duplicate();
    }

    // ========================================================================
    // Setters/Getters/Changers of actuals fields.
    // ========================================================================

    public URIFactory setScheme(String newScheme)
    {
        // check ?
        this.scheme = newScheme;
        return this;
    }

    public URIFactory setUserInfo(String user)
    {
        this.userInfo = user;
        return this;
    }

    public URIFactory setHostname(String newHostname)
    {
        // check ?
        this.hostname = newHostname;
        return this;
    }

    public URIFactory setPath(String newPath)
    {
        // check ?
        setPath(newPath, hasAuthority());
        return this;
    }

    public URIFactory setPort(int newPort)
    {
        // check
        this.port = newPort;
        return this;
    }

    public URIFactory setQuery(String newQuery)
    {
        this.query = newQuery;
        return this;
    }

    public URIFactory setFragment(String newFragment)
    {
        this.fragment = newFragment;
        return this;
    }

    /** Set new Path and format it to default URI path */
    public URIFactory setPath(String path, boolean makeAbsolute)
    {
        // decode and normalize path
        this.pathOrReference = uripath(path, makeAbsolute);
        return this;
    }

    protected void copyFrom(URIFactory loc)
    {
        this.isOpaque = loc.isOpaque;

        // duplicate Strings !
        this.scheme = StringUtil.duplicate(loc.scheme);
        this.userInfo = StringUtil.duplicate(loc.userInfo);
        this.hostname = StringUtil.duplicate(loc.hostname);
        this.port = loc.port;
        this.pathOrReference = StringUtil.duplicate(loc.pathOrReference);
        this.query = StringUtil.duplicate(loc.query);
        this.fragment = StringUtil.duplicate(loc.fragment);
    }

    public String getBasename()
    {
        return basename(pathOrReference);
    }

    /**
     * returns basename part (last part) of this location.
     * 
     * @param withExtension
     *            whether to keep the extension.
     */
    public String getBasename(boolean withExtension)
    {
        String name = basename(getPath());

        if (withExtension == true)
            return name;
        else
            return stripExtension(name);
    }

    public String getPath()
    {
        return pathOrReference;
    }

    public String getHostname()
    {
        return hostname;
    }

    public int getPort()
    {
        return port;
    }

    public String getFragment()
    {
        return this.fragment;
    }

    /**
     * @return decoded reference component of URI
     */
    public String getReference()
    {
        return pathOrReference;
    }

    public boolean isAbsolute()
    {
        return (scheme != null);
    }

    public boolean isRelative()
    {
        return (scheme == null);
    }

    /**
     * Returns true if the URI is Opaque. This means the scheme specific part
     * isn't parsed and must be used 'as-is'.
     * 
     * @return true if the URI is opaque, false otherwise.
     */
    public boolean isOpaque()
    {
        return this.isOpaque;
    }

    public boolean hasScheme(String otherScheme)
    {
        return StringUtil.equals(this.scheme, otherScheme);
    }

    public String getScheme()
    {
        return this.scheme;
    }

    public boolean hasAuthority()
    {
        return StringUtil.notEmpty(this.userInfo) || StringUtil.notEmpty(this.hostname) || (this.port > 0);
    }

    public boolean hasUserInfo()
    {
        return (userInfo != null);
    }

    /**
     * Get last part of filename starting from a '.' but ignore fragment and/or
     * query part !
     */
    public String getExtension()
    {
        return extension(getPath());
    }

    public String getQuery()
    {
        return this.query;
    }
    
    public String getUserInfo()
    {
        return this.userInfo;
    }

    /** Returns dirname part of path */
    public String getDirname()
    {
        return dirname(this.pathOrReference);
    }

    /**
     * Returns array of path elements. For example "/dev/zero" = {"dev","zero"}
     */
    public String[] getPathElements()
    {
        String path = getPath();

        if (path == null)
            return null;

        // strip starting '/' to avoid empty path as first path element
        if (path.startsWith(URI_SEP_CHAR_STR))
            path = path.substring(1);

        return path.split(URI_SEP_CHAR_STR);
    }

    // ========================================================================
    // Extra URIFactory methods, change fields but return this URIFactory in
    // most
    // cases, but might create a new Factory instance.
    // ========================================================================

    /**
     * Appends plain string to this URI's String representation. Does
     * <em>NOT</em> check for fragments or query strings. When appending file
     * system paths use explicit appendPath()!
     */
    public URIFactory appendString(String substr) throws URISyntaxException
    {
        return new URIFactory(this.toString() + substr);
    }

    /**
     * Creates new location by appending path to this one added seperator char
     * between path elements. To added query and/or fragments string use
     * append().
     */
    public URIFactory appendPath(String dirname)
    {
        String oldpath = getPath();
        String newpath = null;

        if (oldpath == null)
            oldpath = "";

        if (StringUtil.isEmpty(dirname))
        {
            return this; // nothing to append
        }
        else if (dirname.charAt(0) == URI_SEP_CHAR)
        {
            newpath = oldpath + dirname;
        }
        else if (dirname.charAt(0) == DOS_SEP_CHAR)
        {
            newpath = oldpath + URI_SEP_CHAR + dirname.substring(1);
        }
        else
        {
            newpath = oldpath + URI_SEP_CHAR + dirname;
        }

        // sanitize path:
        setPath(newpath, true);

        return this;
    }

    /**
     * Use URI.resolve() to resolve this relative path. Note that URI by default
     * strips the last part of the complete filepath. Use resovePath() to do an
     * actual path resolving.
     * 
     * @throws URISyntaxException
     */
    public URIFactory uriResolve(String reluri) throws URISyntaxException
    {
        if ((reluri == null) || (reluri == ""))
            return this;

        // Legacy: check for Query and Fragment resolving:
        char c0 = reluri.charAt(0);

        // java.net.URI handles this incorrect:
        if (c0 == '#')
            return this.setFragment(reluri.substring(1, reluri.length()));

        if (c0 == '?')
        {
            reluri = reluri.substring(1);
            String strs[] = reluri.split("#");
            if ((strs == null) || (strs.length <= 0))
            {
                this.setQuery(null); // empty query)
                this.setFragment(null);
                return this;
            }
            else if (strs.length == 1)
            {
                this.setQuery(strs[0]);
                this.setFragment(null);
                return this;
            }
            else if (strs.length == 2)
            {
                this.setQuery(strs[0]);
                this.setFragment(strs[1]);
                return this;
            }
            else
            {
                throw new URISyntaxException(reluri, "Can't parse mutliple query and/or fragment parts.", 0);
            }
        }

        init(toURI().resolve(encodePath(reluri)));
        return this;
    }

    /**
     * Resolve path and return new decoded filepath. Ignores Query and Fragment
     * parts and uses decoded path element from URI.
     * 
     * @return Returns normalized (not encoded) URI path with forward slashes.
     */
    public String resolvePath(String relpath) throws URISyntaxException
    {
        // append kludge file to trigger uri to resolving actual path.
        String kludgePath = this.getPath() + "/dummy.html";
        URI uri = new URI("file", kludgePath, null);
        // Use encoded path here to allow for strange character
        URI newUri = uri.resolve(encodePath(uripath(relpath, false)));
        return uripath(newUri.getPath());
    }

    public URIFactory getParent()
    {
        URIFactory fac = duplicate();
        fac.pathOrReference = dirname(this.pathOrReference);
        return fac;
    }

    // ========================================================================
    // URI Formatter methods
    // ========================================================================

    public URI toURI() throws URISyntaxException
    {
        if (this.isOpaque)
        {
            // Apparently 'fragment' is not part of the schemeSpecificPart
            new URI(this.scheme, this.pathOrReference, this.fragment);
        }

        if (this.hasAuthority())
        {
            return new URI(this.scheme, this.userInfo, this.hostname, this.port, this.getPath(), this.query,
                    this.fragment);

        }
        else
        {
            // Reference or path only URI: 
            return new URI(scheme,null,null,-1,pathOrReference,query,fragment); 
        }
    }

    /**
     * calls toURI().toURL()
     */
    public URL toURL() throws MalformedURLException, URISyntaxException
    {
        return toURI().toURL();
    }

    /**
     * Explicit encode URI fields.
     */
    public URI toEncodedURI() throws URISyntaxException
    {
        return new URI(scheme, userInfo, hostname, port, encodePath(pathOrReference), encodeQuery(query), encodeQuery(fragment));
    }

    public String toString()
    {
        return toNormalizedString();
    }

    /**
     * This method returns the <em>Decoded</em> URI string. For an URI
     * compatible string (with %XX encoding) use toURI().toString() or
     * toURIString() !
     * 
     * @return normalized and decoded URI String.
     */
    public String toNormalizedString()
    {
        // suport relative VRLs!
        String str = "";

        if (this.isAbsolute() == true)
            str += scheme + ":";

        if (hasAuthority())
        {
            str += URI_SEP_CHAR_STR + URI_SEP_CHAR_STR;

            if ((userInfo != null) && (userInfo.compareTo("") != 0))
                str += userInfo + "@";

            if (hostname != null)
            {
                str += hostname;
                if (port > 0)
                    str += ":" + port;
            }
        }

        // path could start without "/" !
        if (pathOrReference != null)
        {
            // should not be possible:
            if ((hasAuthority()) && (pathOrReference.startsWith(URI_SEP_CHAR_STR) == false))
            {
                // relative path but there muse be a '/ 'between host and path.
                str += URI_SEP_CHAR; // still end with '/' for consistancy !
            }

            str += pathOrReference;
        }
        else
        {
            str += URI_SEP_CHAR; // still end with '/' for consistancy !
        }

        if (query != null)
            str += "?" + query;

        if (fragment != null)
            str += "#" + fragment;

        return str;
    }

    public String getDosPath()
    {
        String newPath = this.getPath();

        // Check '/C:' and remove leading slash.

        if (newPath.length() >= 3)
        {
            if ((newPath.charAt(0) == '/') && (newPath.charAt(2) == ':'))
            {
                char drive = newPath.charAt(1); // A,B,C,...,Z
                newPath = newPath.substring(1);
            }
        }
        // explicit flip slashes
        newPath = newPath.replace('/', '\\');
        return newPath;
    }

    // ===
    // Generated Methods
    // ===

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fragment == null) ? 0 : fragment.hashCode());
        result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
        result = prime * result + (isOpaque ? 1231 : 1237);
        result = prime * result + ((pathOrReference == null) ? 0 : pathOrReference.hashCode());
        result = prime * result + port;
        result = prime * result + ((query == null) ? 0 : query.hashCode());
        result = prime * result + ((scheme == null) ? 0 : scheme.hashCode());
        result = prime * result + ((userInfo == null) ? 0 : userInfo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        URIFactory other = (URIFactory) obj;
        if (fragment == null)
        {
            if (other.fragment != null)
                return false;
        }
        else if (!fragment.equals(other.fragment))
            return false;
        if (hostname == null)
        {
            if (other.hostname != null)
                return false;
        }
        else if (!hostname.equals(other.hostname))
            return false;
        if (isOpaque != other.isOpaque)
            return false;
        if (pathOrReference == null)
        {
            if (other.pathOrReference != null)
                return false;
        }
        else if (!pathOrReference.equals(other.pathOrReference))
            return false;
        if (port != other.port)
            return false;
        if (query == null)
        {
            if (other.query != null)
                return false;
        }
        else if (!query.equals(other.query))
            return false;
        if (scheme == null)
        {
            if (other.scheme != null)
                return false;
        }
        else if (!scheme.equals(other.scheme))
            return false;
        if (userInfo == null)
        {
            if (other.userInfo != null)
                return false;
        }
        else if (!userInfo.equals(other.userInfo))
            return false;
        return true;
    }

}
