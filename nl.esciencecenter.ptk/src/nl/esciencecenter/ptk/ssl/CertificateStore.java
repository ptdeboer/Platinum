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

package nl.esciencecenter.ptk.ssl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import nl.esciencecenter.ptk.crypt.Secret;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.io.FSUtil;
import nl.esciencecenter.ptk.io.exceptions.FileURISyntaxException;
import nl.esciencecenter.ptk.io.local.LocalFSNode;
import nl.esciencecenter.ptk.net.URIFactory;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;

/**
 * This class manages a Java Keystore which contains X509Certificates. <br>
 * Added support for PEM and DER Certificates so that grid certificates can be stored
 * as well in one single 'cacerts' file.
 * 
 * @author Piter T. de Boer
 */
public class CertificateStore
{
    /**
     * This is the default password for java 'cacerts' file located in for
     * example JAVA_HOME/jre/lib/security/cacerts. 
     */
    public static final String DEFAULT_PASSPHRASE = "changeit";

    /**
     * CaCert handling policy. Set to "false" to ignore signing policies.
     */
    public static final String PROP_SSL_CACERT_POLICY = "ssl.cacert.policy";

    public static final String OPT_INTERACTIVE = "interactive";

    public static final String OPT_STOREACCEPTED = "storeAccepted";

    public static final String OPT_ALWAYSACCEPT = "alwaysAccept";

    /**
     * Custom property ssl.cacerts.policy.interactive
     */
    public static final String SSL_CACERT_POLICY_INTERACTIVE = PROP_SSL_CACERT_POLICY + "." + OPT_INTERACTIVE;

    /**
     * Custom property ssl.cacerts.policy.storeAccepted
     */
    public static final String SSL_CACERT_POLICY_STOREACCEPTED = PROP_SSL_CACERT_POLICY + "." + OPT_STOREACCEPTED;

    /**
     * Custom property sssl.cacerts.policy.alwaysAccept
     */
    public static final String SSL_CACERT_POLICY_ALWAYSACCEPT = PROP_SSL_CACERT_POLICY + "." + OPT_ALWAYSACCEPT;

    /**
     * Global 'cacerts' instance is loaded from classpath. Cannot be saved!
     */
    private static CertificateStore instance = null;

    static ClassLogger logger = null;

    static
    {
        logger = ClassLogger.getLogger(CertificateStore.class);
    }

    /**
     * Interactive CaCert handling options.
     */
    public static class CaCertOptions
    {
        /**
         * Whether to ask user. If interactive==false, the fields "alwaysAccept"
         * and "storeAccepted" are used to control whether to accept and store
         * the certificate.
         */
        public boolean interactive = true;

        /**
         * If interactive==false, alwaysAccept is used
         */
        public boolean alwaysAccept = true;

        /**
         * If interactive==false, storeAccepted is used
         */
        public boolean storeAccepted = true;

        public CaCertOptions()
        {
            ; // default
        }
    }

    /**
     * Load default 'cacerts' file from classpath or creates an empty one if autoinit==true.
     * @return new CertificateStore either loaded from classpathor a new empty keystore.  
     */
    public static synchronized CertificateStore getDefault(boolean autoinit) throws CertificateStoreException
    {
        if (instance == null)
        {
            CertificateStore certStore = new CertificateStore();
            
            // Null keystore -> load default from classpath or create new empty
            // keystore.
            certStore.loadKeystore(null, new Secret(DEFAULT_PASSPHRASE.toCharArray()), autoinit);
            
            // don't auto save default cert store from classpath.
            certStore.setIsPersistant(false);
            
            // assign only AFTER Succesfull creation !
            instance = certStore;
        }

        return instance;
    }

    public static CertificateStore createFrom(KeyStore keyStore,Secret passwd)
    {
        CertificateStore certStore = new CertificateStore();
        certStore.setKeyStore(keyStore, passwd); 
        // don't auto save default cert store from classpath.
        certStore.setIsPersistant(false);
        return certStore;
    }
    
    // ==== class methods === //

    /**
     * Loads default 'cacerts' on classpath or creates an empty one if none can
     * be found and autoinit==true. 
     */
    public static KeyStore loadDefaultKeystore(Secret secret, boolean autoinit) throws CertificateStoreException
    {
        if ( (secret == null) || (secret.isEmpty()) )
        {
            throw new NullPointerException("Keystore cannot have NULL password.");
        }
        
        try
        {
            // Check classpath to resolve 'cacerts' file. 
            // This file might be a non writable location. 
            URL url = Thread.currentThread().getContextClassLoader().getResource("cacerts");
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

            if (url != null)
            {
                logger.infoPrintf("Loading default keystore from classpath:%s\n", url);
                keystore.load(url.openStream(), secret.getChars());
            }
            else if (autoinit)
            {
                logger.warnPrintf("Creating EMPTY KeyStore!\n");
                // As documented: supply null input stream to create a new (empty) KeyStore.
                keystore.load(null, secret.getChars());
            }
            else
            {
                return null;
            }

            return keystore;

        }
        catch (IOException e)
        {
            throw new CertificateStoreException("Could not create new key store.", e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new CertificateStoreException("NoSuchAlgorithmException: Could not create new key store.", e);
        }
        catch (CertificateException e)
        {
            throw new CertificateStoreException("CertificateException:Could create new key store.", e);
        }
        catch (KeyStoreException e)
        {
            throw new CertificateStoreException("KeyStoreException: Could not create new empty store at.", e);
        }
    }

    private static KeyStore createEmptyKeyStore(Secret passwd) throws CertificateStoreException
    {
        try
        {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(null, passwd.getChars());
            return keystore;
        }
        catch (Exception e)
        {
            throw new CertificateStoreException("Could not create new (empty) key store.\n"+e.getMessage(),e); 
        }
    }
    
    /**
     * Loads CertificateStore from specified location using the specifed
     * password to unlock it. If the keystore doesn't exist and autoInitiliaze
     * is true a default keystore will be created. Set persistant to true to
     * autosave changes to this location.
     * 
     * @param keyStoreLocation
     * @param passthing
     *            password or passphrase.
     * @param autoInitiliaze
     *            create new (empty) keystore if keystore can't be loaded
     * @param persistant
     *            whether to auto save new added certificates.
     * @return new CertificateStore object or null when autoInitialize==false
     *         and keystore doesn't exists.
     * @throws CertificateStoreException
     */
    public static CertificateStore loadCertificateStore(String keyStoreLocation, 
            Secret passthing,
            boolean autoInitiliaze, 
            boolean persistant) throws CertificateStoreException
    {
        CertificateStore certStore = new CertificateStore();
        certStore.loadKeystore(keyStoreLocation, passthing, autoInitiliaze);
        certStore.setIsPersistant(persistant);

        return certStore;
    }

    /**
     * Create non persistent internal CertificateStore.
     */
    public static CertificateStore createInternal(Secret passwd) throws CertificateStoreException
    {
        if (passwd == null)
        {
            throw new NullPointerException("Keystore cannot have NULL password.");
        }
        
        CertificateStore certStore = new CertificateStore();
        // Default empty non persistante key store!
         
        certStore.setKeyStore(createEmptyKeyStore(passwd),passwd);
        // certStore.setPassphrase(passwd);
        certStore.setIsPersistant(false);
        certStore.setCustomCerticateDirectories(null);

        return certStore;
    }

    public static boolean hasCertExtension(String filename)
    {
        if (StringUtil.isEmpty(filename))
            return false;
        filename = filename.toLowerCase();
        if (filename.endsWith(".0"))
            return true;
        if (filename.endsWith(".crt"))
            return true;
        if (filename.endsWith(".pem"))
            return true;
        if (filename.endsWith(".der"))
            return true;
        return false;
    }

    public static boolean isPasswordException(Exception e)
    {
        Throwable cause = e;

        while (cause != null)
        {
            String msg = cause.getMessage();

            if ((msg != null) && (cause instanceof java.security.UnrecoverableKeyException))
            {
                msg = msg.toLowerCase();
                if (msg.contains("password verification failed"))
                    return true;
            }

            cause = cause.getCause();
        }

        return false;
    }
    
    // ============================= //
    // Inner Classes
    // ============================= //

    /**
     * TrustManager which captures the certificate chain for inspection.
     */
    public class SavingTrustManager implements X509TrustManager
    {
        private X509Certificate[] chain;

        SavingTrustManager()
        
        {
            ; //
        }

        public X509Certificate[] getAcceptedIssuers()
        {
            return null; // throw new UnsupportedOperationException();
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
        {
            throw new UnsupportedOperationException();
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
        {
            this.chain = chain;

            // auto initialize:
            try
            {
                getTrustManager(false).checkClientTrusted(chain, authType);
            }
            catch (Exception e)
            {
                throw new CertificateException(e);
            }
        }

        public X509Certificate[] getChain()
        {
            return chain;
        }
    }

    // =============================
    // Instance
    // =============================

    /** Default Options */
    private CaCertOptions cacertOptions = new CaCertOptions();

    /**
     * The Java KeyStore to manage.
     */
    private KeyStore _keyStore = null;

    /**
     * Auto initializing default TrustManager
     */
    private X509TrustManager defaultTrustManager = null;

    private SavingTrustManager savingTrustManager = null;

    private Secret keystorePassword =new Secret(DEFAULT_PASSPHRASE.toCharArray());

    private String keyStoreLocation = null;

    private boolean isPersistent = true;

    private Object keyStoreMutex = new Object();

    private String userPrivateKeyAlias = null;

    /**
     * Custom certificate directories to be added. This directories are
     * automatically rescanned when a new keystore is loaded.
     */
    private URL customCertificateDirectories[] = null;

    protected CertificateStore()
    {
        init();
    }

//    public CertificateStore(KeyStore keyStore, Secret passwd)
//    {
//        init(); 
//        setKeyStore(keyStore,passwd);
//    }

    protected void init()
    {
        // (re-)initialize empty store;
        keyStoreLocation = null;
        userPrivateKeyAlias = null;
        this._keyStore = null;
        this.isPersistent=false;
        this.keyStoreLocation=null; 
    }

    public void setKeyStore(KeyStore keystore, Secret passwd)
    {
        this._keyStore = keystore;
        this.keystorePassword = passwd;
    }

    public void setPassphrase(Secret passphrase)
    {
        this.keystorePassword = passphrase;
    }

    /**
     * Returns current persistent keystore location. If loaded from the
     * classpath this location is null.
     */
    public String getKeyStoreLocation()
    {
        return keyStoreLocation;
    }

    public void setKeyStoreLocation(String fileName)
    {
        keyStoreLocation = fileName;
    }

    public void setCustomCerticateDirectories(URL dirUrls[])
    {
        this.customCertificateDirectories = dirUrls;
    }

    /**
     * Set this to true to auto save new added Certificates to the KeyStore.
     */
    public void setIsPersistant(boolean val)
    {
        this.isPersistent = val;
    }

    /**
     * Whether the KeyStore is persistent. 
     * A KeyStore can only be persistent if it has a storage location.
     */
    public boolean isPersistant()
    {
        return ((this.keyStoreLocation != null) && (isPersistent));
    }

    /**
     * Reload this KeyStore.
     */
    public boolean reloadKeystore() throws CertificateStoreException
    {
        return this.loadKeystore(keyStoreLocation, this.getPassphrase(), true);
    }

    /**
     * Returns configured passphrase for this KeyStore. 
     */
    private Secret getPassphrase()
    {
        return this.keystorePassword;
    }

    public boolean loadKeystore(String keystoreLocation, 
            Secret secret, 
            boolean autoInitialize)
            throws CertificateStoreException
    {
        if ((secret == null) || (secret.isEmpty()))
        {
            throw new NullPointerException("Password can not be null.");
        }
        
        this.keystorePassword = secret;
        this.keyStoreLocation = keystoreLocation;

        FSUtil fsUtil = FSUtil.getDefault();

        // thread save!
        synchronized (keyStoreMutex)
        {
            this.keyStoreLocation = keystoreLocation;

            char[] passphrase = secret.getChars();

            LocalFSNode keyStoreFile = null;

            try
            {
                if (keystoreLocation != null)
                {
                    keyStoreFile = FSUtil.getDefault().newLocalFSNode(keystoreLocation);
                }
                
                // check user copy of cacerts
                if ((keyStoreFile != null) && (keyStoreFile.exists()))
                {
                    logger.debugPrintf("Loading Existing KeyStore: %s\n", keyStoreFile);
                    _keyStore = null;
    
                    // Try to load:
                    if (fsUtil.existsFile(keyStoreLocation, true))
                    {
                        InputStream in = null;
    
                        try
                        {
                            _keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    
                            in = fsUtil.createInputStream(keyStoreLocation);
                            _keyStore.load(in, passphrase);
                            in.close();
                        }
                        catch (Exception e1)
                        {
                            logger.logException(ClassLogger.WARN, e1, "Warning: Couldn't read keystore\n");
                            // password error: DO NOT AUTOINITIALIZE;
                            _keyStore = null;
    
                            if (isPasswordException(e1))
                            {
                                throw new CertificateStoreException("Invalid password for keystore."
                                        + "please update password or remove keystore file:" + keystoreLocation, e1);
                            }
                        }
                    }
                }
            }
            catch (FileURISyntaxException e)
            {
                throw new CertificateStoreException("Syntax Error: Invalid keyStore location:"+keystoreLocation, e);
            }
            
            // pass CertificateStoreExceptions

            if (_keyStore == null)
            {
                // EMPTY keystore !
                try
                {
                    _keyStore = loadDefaultKeystore(secret, autoInitialize);
                }
                catch (Exception e2)
                {
                    logger.logException(ClassLogger.ERROR, e2, "Couldn't create empty keystore.\n");
                    throw new CertificateStoreException("Couldn't create empty keystore!", e2);
                }
            }

        } // END sychronized(keyStoreMutex) //

        reloadCustomCertificates();

        checkKeyStore();
        return true;
    }

    public void reloadCustomCertificates()
    {
        if (customCertificateDirectories != null)
        {
            loadCustomCertificates(customCertificateDirectories);
        }
        else
        {
            logger.debugPrintf("reloadCustomCertificates():No custom certificates.\n");
        }
    }

    /**
     * Add extra certificates form specified locations.
     *  
     * @param certificateDirectories - directories which contains custom certificates.  
     */
    protected void loadCustomCertificates(URL certificateDirectories[])
    {
        FSUtil fsUtil = FSUtil.getDefault();

        for (URL url : certificateDirectories)
        {
            String dir = url.getPath();
            logger.infoPrintf(" - checking custom certificate folder:%s\n", dir);

            if (fsUtil.existsDir(dir)==false)
            {
                logger.warnPrintf(" - ignoring non existing custom certificate folder:%s\n", dir);
                continue; 
            }
           
            String files[]=null;
            
            try
            {
                files = fsUtil.list(dir);
            }
            catch (FileURISyntaxException e)
            {
               logger.logException(ClassLogger.ERROR, e,"Syntax Error on location:%s", dir);
               return; 
            }
            catch (IOException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            
            if ((files == null) || (files.length<=0)) 
            {
                continue; 
            }   
            
            for (String file : files)
            {
                if (hasCertExtension(file))
                {
                    try
                    {
                        // Add, but do not save now, only after reading all the certificates. 
                        this.addPEMCertificate(file, false);
                        logger.infoPrintf(" - > added Custom Certificate:%s\n", file);
                    }
                    catch (Exception e)
                    {
                        logger.logException(ClassLogger.INFO, e,
                                "Warning: Failed to load Custom Certificate (ignoring):%s\n", file);
                    }   
                }   
            }   
        }
    }

    public KeyStore getKeyStore()
    {
        return this._keyStore;
    }

    /**
     * Gets current trust manager. When certificates have changed, the trust
     * manager must be recreated. Set reload==true to trigger re-initialization.
     */
    public X509TrustManager getTrustManager(boolean reinit) throws CertificateStoreException
    {
        try
        {
            if ((reinit == false) && (defaultTrustManager != null))
                return defaultTrustManager;

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            // INIT
            if (this._keyStore == null)
                reloadKeystore();

            tmf.init(getKeyStore());

            defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];

            return defaultTrustManager;
        }
        catch (Exception e)
        {
            throw new CertificateStoreException("Failure to create new TrustManager.", e);
        }
    }

    /**
     * SSLContext factory method to create a custom SSLContext using this
     * certificate store. 
     * 
     * @param sslProtocol - the SSL protocol for example "SSLv3" or "TLS". 
     */
    public SSLContext createSSLContext(String sslProtocol) throws CertificateStoreException
    {

        String userKeyAlias = getFirstKeyAlias();

        // KeyManager[] myKeymanagers=null;
        KeyManager myKeymanager = null;

        if (userKeyAlias != null)
        {
            // Add
            Certificate[] certificateChain = this.getCertificateChain(userKeyAlias);
            PrivateKey privateKey = this.getPrivateKey(getFirstKeyAlias());

            // Multiple private keys could be added here!
            if ((certificateChain != null) && (privateKey != null))
            {
                logger.infoPrintf("Using default user private key:%s\n", userKeyAlias);
                myKeymanager = new PrivateX509KeyManager(certificateChain, privateKey);
            }
            else
            {
                logger.warnPrintf("Couldn't find user private key alias:%s", userKeyAlias);
            }
        }
        else
        {
            logger.infoPrintf("NO user private key alias specified. Will not use (private key) user authentication !\n");
        }

        return createSSLContext(myKeymanager, sslProtocol);
    }

    /**
     * SSLContext factory method to create a custom SSLContext using this
     * certificate store. Adds a private key manager to the SSLContext.
     * 
     * @param sslProtocol - SSL protocol to use, for example "SSLv3".
     */
    public SSLContext createSSLContext(KeyManager privateKeyManager, String sslProtocol)
            throws CertificateStoreException
    {
        try
        {
            SSLContext sslContext = SSLContext.getInstance(sslProtocol);

            defaultTrustManager = getTrustManager(false);

            savingTrustManager = new SavingTrustManager();

            sslContext.init(new KeyManager[]
            { privateKeyManager }, new TrustManager[]
            { savingTrustManager }, null);

            return sslContext;
        }
        catch (Exception e)
        {
            throw new CertificateStoreException("Failure to intialize SSLContext.", e);
        }
    }

    /** 
     * Save keystore if this keystore is persistent and there is a keyStoreLocation. 
     */
    protected void autoSaveKeystore() throws CertificateStoreException
    {
        if ((isPersistent == true) && (this.keyStoreLocation!=null)) 
        {
            saveKeystoreTo(keyStoreLocation, keystorePassword);
        }
        else
        {
            logger.debugPrintf("Will not autosave non persistant keystore:%s\n",(keyStoreLocation==null)?"<No KeyStore location>":keyStoreLocation); 
        }
    }

    protected void saveKeystoreTo(String location, Secret password) throws CertificateStoreException
    {
        logger.debugPrintf("saveKeyStoreTo:%s\n",location); 
         
        if ((location == null) && (this.keyStoreLocation == null))
        {
            logger.warnPrintf("saveKeyStore: couldn't save keystore: No location defined!\n");
            return;
        }

        if (password == null)
            password = this.keystorePassword;

        if (location == null)
            location = this.getKeyStoreLocation();

        logger.infoPrintf("Saving keyStore to:%s\n", location);

        try
        {
            FileOutputStream fout = new FileOutputStream(new java.io.File(location));

            synchronized (keyStoreMutex)
            {
                _keyStore.store(fout, password.getChars());
            }

            try
            {
                fout.close();
            }
            catch (Exception e)
            {
                ;
            }
        }
        catch (IOException e)
        {
            throw new CertificateStoreException("IO Error when saving keystore to file:" + location, e);
        }
        catch (KeyStoreException e)
        {
            throw new CertificateStoreException("KeyStoreException:When saving keystore to file:" + location, e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new CertificateStoreException("NoSuchAlgorithmException: When saving keystore to file:" + location, e);
        }
        catch (CertificateException e)
        {
            throw new CertificateStoreException("CertificateException: When saving keystore to file:" + location, e);
        }
    }

    public X509Certificate[] getX509Certificates() throws CertificateStoreException
    {
        return getTrustManager(false).getAcceptedIssuers();
    }

    /**
     * DER encoded Certificate .cer .crt .der
     * @throws Exception 
     */
    public X509Certificate addDERCertificate(String filename, boolean autoSave) throws CertificateStoreException  
    {
        X509Certificate x590;
        
        try
        {
            x590 = CertUtil.loadDERCertificate(filename);
            String alias = URIFactory.basename(filename);
            return addCertificate(alias, x590, autoSave);
            
        }
        catch (FileURISyntaxException e)
        {
            throw new CertificateStoreException("FileURISyntaxException: Couldn't not load certificate. Invalid file/URI:"+filename+"\n"+e.getMessage(),e); 
        }
        catch (IOException e)
        {
            throw new CertificateStoreException("IOException: Couldn't not load certificate:"+filename+"\n"+e.getMessage(),e); 
        }
        catch (CertificateException e)
        { 
            throw new CertificateStoreException("CertificateException: Couldn't not load certificate:"+filename+"\n"+e.getMessage(),e); 
        }
    }

    /**
     * Load .pem Certificate file
     * 
     * @param save
     */
    public X509Certificate addPEMCertificate(String filename, boolean autoSave) throws CertificateStoreException
    {
        X509Certificate x590;
        try
        {
            x590 = CertUtil.loadPEMCertificate(filename);
            String alias = URIFactory.basename(filename);
            return addCertificate(alias, x590,autoSave);
        }
        catch (FileURISyntaxException e)
        {
            throw new CertificateStoreException("FileURISyntaxException: Couldn't not load certificate. Invalid file/URI:"+filename+"\n"+e.getMessage(),e); 
        }
        catch (IOException e)
        {
            throw new CertificateStoreException("IOException: Couldn't not load certificate:"+filename+"\n"+e.getMessage(),e); 
        }
        catch (CertificateException e)
        { 
            throw new CertificateStoreException("CertificateException: Couldn't not load certificate:"+filename+"\n"+e.getMessage(),e); 
        }
    }

    /**
     * Add String encoded DER certificate to certificate store.
     */
    public X509Certificate addDERCertificate(String alias, String derEncodedString, boolean autoSave) throws CertificateStoreException
    {
        try
        {
            X509Certificate x590 = CertUtil.createDERCertificateFromString(derEncodedString);
            return addCertificate(alias, x590, autoSave);
        }
        catch (CertificateException e)
        { 
            throw new CertificateStoreException("CertificateException: Couldn't add certificate:"+alias+"\n"+e.getMessage(),e); 
        }
        catch (UnsupportedEncodingException e)
        {
            throw new CertificateStoreException("UnsupportedEncodingException: Couldn't add certificate:"+alias+"\n"+e.getMessage(),e); 
        }
    }

    public void addCACertificate(X509Certificate cert, boolean save) throws CertificateStoreException
    {
        addCertificate(cert.getIssuerDN().getName(), cert, save);
    }
    
    /**
     * Store X509Certificate with the speficate alias to the KeyStore.
     *  
     * @param autoSave
     * @throws CertificateStoreException 
     */
    public X509Certificate addCertificate(String alias, X509Certificate x590, boolean autoSave) throws CertificateStoreException
    {
        return _addCertificate(alias, x590, autoSave);
    }

    protected X509Certificate _addCertificate(String alias, X509Certificate x590, boolean autoSave) throws CertificateStoreException 
    {
        try
        {
            logger.debugPrintf("+++ Adding cert +++\n");
            logger.debugPrintf(" -  Alias    = %s\n", alias);
            logger.debugPrintf(" -  Subject  = %s\n", x590.getSubjectDN().toString());
            logger.debugPrintf(" -  Issuer   = %s\n", x590.getIssuerDN().toString());
    
            synchronized (keyStoreMutex)
            {
                _keyStore.setCertificateEntry(alias, x590);
            }
    
            // Re-initialize!
            this.defaultTrustManager = null;
            this.getTrustManager(true);
    
            // only autosav when this is a persistant keystore 
            if (autoSave && this.isPersistent)
            {
                autoSaveKeystore();
            }
            return x590; 
        }
        catch (KeyStoreException e)
        {
            throw new CertificateStoreException("KeyStoreException: Couldn't add certificate:"+alias, e);
        }
    }

    /**
     * Returns alias list of certificates stored in this CertificateStore.
     * @throws KeyStoreException 
     * @throws CertificateStoreException 
     */
    public List<String> getAliases() throws CertificateStoreException 
    {
        try
        {
            synchronized (keyStoreMutex)
            {
                Enumeration<String> alss = _keyStore.aliases();
                StringList list = new StringList();
    
                while (alss.hasMoreElements())
                {
                    String alias = alss.nextElement();
                    list.add(alias);
                }
    
                return list;
            }
        }
        catch (KeyStoreException e)
        {
            throw new CertificateStoreException("Couldn't list aliasses.\n"+e.getMessage(), e);
        }
    }

    /**
     * Return first key alias. Typically this is a private user key. Returns
     * NULL if no alias is configured!
     */
    public String getFirstKeyAlias() throws CertificateStoreException
    {
        if (this.userPrivateKeyAlias != null)
            return userPrivateKeyAlias;

        if (this._keyStore == null)
            return null;

        try
        {
            String alias = null;

            for (Enumeration<String> alss = _keyStore.aliases(); alss.hasMoreElements();)
            {
                alias = alss.nextElement();
                if (_keyStore.isKeyEntry(alias))
                    break;
                else
                    alias = null;
            }

            return alias;
        }
        catch (Exception e)
        {
            throw new CertificateStoreException("Couldn't access keystore while searching for aliasses.", e);
        }
    }

    /**
     * Set private key alias for the default user. Use this alias to retrieve
     * the private key.
     */
    public void setUserPrivateKeyAlias(String alias)
    {
        this.userPrivateKeyAlias = alias;
    }

    public Certificate[] getCertificateChain(String alias) throws CertificateStoreException
    {
        synchronized (this._keyStore)
        {
            try
            {
                return this._keyStore.getCertificateChain(alias);
            }
            catch (Exception e)
            {
                throw new CertificateStoreException(
                        "Error accessing KeyStore while getting certificate chain for alias:" + alias, e);
            }
        }
    }

    public Certificate getCertificate(String alias) throws CertificateStoreException
    {
        synchronized (this._keyStore)
        {
            try
            {
                return this._keyStore.getCertificate(alias);
            }
            catch (Exception e)
            {
                throw new CertificateStoreException("Error accessing KeyStore while getting certificate for alias:" + alias, e);
            }
        }
    }
    /**
     * Return private key specified by the alias.
     * 
     * @param alias - the alias of the private user key.
     * @return - The Private Key stored in this KeyStore.
     * @throws CertificateStoreException
     */
    public PrivateKey getPrivateKey(String alias) throws CertificateStoreException
    {
        synchronized (this._keyStore)
        {
            try
            {
                Secret passwd = this.keystorePassword;
                PrivateKey key = (PrivateKey) this._keyStore.getKey(alias, passwd.getChars());
                return key;
            }
            catch (Exception e)
            {
                throw new CertificateStoreException("Error accessing KeyStore while looking for alias" + alias, e);
            }
        }
    }

    protected void checkKeyStore()
    {
        logger.infoPrintf("KeyStore: persistant location:%s\n", this.keyStoreLocation);

        try
        {
            String alias = getFirstKeyAlias();
            if (alias == null)
            {
                logger.infoPrintf("KeyStore: No Private key alias found.\n");
            }
            else
            {
                logger.infoPrintf("KeyStore: Found private key alias=%s\n", alias);

                if (getPrivateKey(alias) == null)
                {
                    logger.infoPrintf("KeyStore: Warning: No Private key detected for alias:%s\n", alias);
                }
                if (_keyStore.getCertificateChain(alias) == null)
                {
                    logger.infoPrintf(
                            "KeyStore: Error: Private key found, but has no Certificate Chaing for alias:%s\n", alias);
                }
                else
                {
                    logger.infoPrintf("KeyStore: Found Certificate chain for Private Key:%s\n", alias);
                }
            }
        }
        catch (Exception e)
        {
            logger.logException(ClassLogger.ERROR, e, "Exception when checking keystore!\n");
        }
    }

    public void setUserPrivateKey(String alias, PrivateKey privKey, Certificate chain[])
            throws CertificateStoreException
    {
        this.addUserPrivateKey(alias, privKey, chain);
    }

    public void addUserPrivateKey(String alias, PrivateKey privKey, Certificate[] chain)
            throws CertificateStoreException
    {
        if (alias == null)
            alias = "mykey";

        synchronized (this._keyStore)
        {
            try
            {
                this._keyStore.setKeyEntry(alias, privKey, this.getPassphrase().getChars(), chain);
            }
            catch (KeyStoreException e)
            {
                throw new CertificateStoreException("Couldn't set private key entry for alias:" + alias, e);
            }
        }

        this.autoSaveKeystore();
    }

    public void changePassword(Secret oldpasswd, Secret newpasswd) throws CertificateStoreException
    {
        // Reload with optional old password.
        // The reload is needed if the password did't match and the password needs to
        // be updated.
        this.loadKeystore(keyStoreLocation, oldpasswd, false);
        this.saveKeystoreTo(keyStoreLocation,newpasswd);
    }

    public KeyManager createPrivateKeyManager(String alias) throws CertificateStoreException
    {
        if (alias == null)
            alias = getFirstKeyAlias();

        Certificate[] certificateChain = this.getCertificateChain(alias);
        PrivateKey privateKey = this.getPrivateKey(getFirstKeyAlias());

        // Multiple private keys could be added here!
        if (privateKey == null)
        {
            logger.infoPrintf("Private key not found:%s\n", alias);
            throw new CertificateStoreException("Couldn't find private key:" + alias);
        }

        // Multiple private keys could be added here!
        if (certificateChain == null)
        {
            logger.infoPrintf("Private key not found:%s\n", alias);
            throw new CertificateStoreException("Couldn't find Certificate Chain for private key alias:" + alias);
        }

        logger.infoPrintf("Using default user private key:%s\n", alias);

        return new PrivateX509KeyManager(certificateChain, privateKey);
    }

    public CaCertOptions getOptions()
    {
        return this.cacertOptions;
    }

    public SavingTrustManager getSavingTrustManager()
    {
        return this.savingTrustManager;
    }

  


}
