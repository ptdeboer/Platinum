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
