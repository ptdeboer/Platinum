package nl.esciencecenter.ptk.crypt;

/**
 * Pre configured encryption schemes.  
 * <p>
 * Jackson POJO directives: enum classes are automatically converted. 
 */
public enum CryptScheme
{
    /** 
     * Triple DES (E-D-E), Electronic Cook Book and PKC5 Padding.
     */ 
    DESEDE_ECB_PKCS5("DESede","DESede","DESede/ECB/PKCS5Padding",24),
    
    /**
     * Single DES, Electronic Cook Book and PKC5 Padding. 
     * @deprecated Do not use single DES
     */ 
    DES_ECB_PKCS5("DES","DES","DES/ECB/PKCS5Padding",16),
    
    /** 
     * AES-128 Encryption, ECB and PKC5 PAddding. 
     */
    AES128_ECB_PKCS5("AES-128","AES","AES/ECB/PKCS5Padding",16),

    /** 
     * AES-192 Encryption. Need unlimited policy files for bit keys > 128  
     */
    AES192_ECB_PKCS5("AES-192","AES","AES/ECB/PKCS5Padding",24),

    /**
     * AES-256 Encryption. Need unlimited policy files for bit keys > 128 
     */
    AES256_ECB_PKCS5("AES-256","AES","AES/ECB/PKCS5Padding",32),

    ; 
    // === //
    
    /** 
     * Encryption Scheme alias. 
     */ 
    protected String schemeAlias;

    /** 
     * Type of Scheme 
     */ 
    protected String schemeFamily;

    
    /** 
     * Full Configuration String 
     */ 
    protected String configString;
    
    /**
     * Significant key length in bytes. For Triple DES, this is 24 
     * For AES-128 this is 16, for AES-256 this 32.
     * Keys longer than this length might be truncated. 
     */ 
    protected int keyLength; 
        
    private CryptScheme(String shemeAlias,String schemeFamily,String configName,int keyLength)
    {
        this.schemeAlias=shemeAlias; 
        this.schemeFamily=schemeFamily; 
        this.configString=configName;
        this.keyLength=keyLength; 
    }
    
    /** 
     * @return Used encryption scheme symbolic name or alias. 
     */
    public String getSchemeAlias()
    {
        return schemeAlias; 
    }
        
    /** 
     * @return Actually used Cipher Scheme (DES,AES)
     */
    public String getCipherScheme()
    {
        return schemeFamily;
    }
    
    /** 
     * Full configuration string for this Encryption scheme. 
     * @return
     */
    public String getConfigString()
    {
        return configString; 
    }
    
    /**
     * Returns significant key length. 
     * Smaller keys are rejected. Longer key might be truncated or XOR-ed with their remainder.  
     * @return Significant Key Length in bytes.  
     */
    public int getKeyLength()
    {
        return keyLength;
    }
    
}
