/*
 * Copyrighted 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").  
 * You may not use this file except in compliance with the License. 
 * For details, see the LICENCE.txt file location in the root directory of this 
 * distribution or obtain the Apache License at the following location: 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
 * ---
 */
// source: 

package nl.esciencecenter.ptk.crypt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

import nl.esciencecenter.ptk.util.StringUtil;

//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;

/**
 * String Encrypter/Decryptor class. 
 * Can also be used to encrypt/decrypt byte arrays.
 */
public class StringCrypter
{
    public static class EncryptionException extends Exception
    {
        private static final long serialVersionUID = 1L;

        public EncryptionException(Throwable t)
        {
            super(t);
        }
        
        public EncryptionException(String message, Throwable cause)
        {
            super(message,cause);
        }
    }

    
    public static class DecryptionFailedException extends EncryptionException
    {
        private static final long serialVersionUID = 1L;

        public DecryptionFailedException(Throwable t)
        {
            super(t);
        }
        
        public DecryptionFailedException(String message, Throwable cause)
        {
            super(message,cause);
        }
    }
    
    // ========================================================================
    // Class Constants  
    // ========================================================================
    
    public static final String CHARSET_UTF8 = "UTF-8";

    public static Secret getAppKey1()
    {
        // 'legacy app key 1'
        return new Secret("123CSM34567890ENCRYPTION".toCharArray());
        //  return new Secret("123CSM34567890ENCRYPTIONC3PR4KEY5678901234567890".toCharArray());

    }
   
    // ========================================================================
    // Instance 
    // ========================================================================

    private KeySpec keySpec;

    private SecretKeyFactory keyFactory;

    private Cipher cipher;

    private Charset charSet;
    
    private MessageDigest keyHasher;

    /**
     * Whether to use the plain character bytes from a password string instead of a hashing function.
     * This option is for legacy applications.
     */ 
    private boolean usePlainCharBytes=false;

    private CryptScheme cryptScheme;
    
//    public StringEncrypter() throws EncryptionException
//    {
//        init(getDefaultKey(),DESEDE_ENCRYPTION_SCHEME, StringHasher.SHA_256,CHARSET_UTF8);
//    }

    public StringCrypter(Secret encryptionKey) throws EncryptionException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        init(encryptionKey,CryptScheme.DESEDE_ECB_PKCS5,StringHasher.SHA_256, CHARSET_UTF8);
    }
    
    public StringCrypter(Secret encryptionKey, CryptScheme encryptionScheme) throws EncryptionException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        init(encryptionKey,encryptionScheme,StringHasher.SHA_256,CHARSET_UTF8);
    }
    
    public StringCrypter(Secret encryptionKey, CryptScheme encryptionScheme,String keyHashingScheme, String charEncoding) throws EncryptionException, NoSuchAlgorithmException, UnsupportedEncodingException
    { 
        init(encryptionKey,encryptionScheme,keyHashingScheme,charEncoding);
    }
    
    public StringCrypter(byte encryptionKey[], CryptScheme encryptionScheme,String keyHashingScheme, String charEncoding) throws EncryptionException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        setCharacterEncoding(charEncoding);
        keyHasher = MessageDigest.getInstance(keyHashingScheme); 
        initKey(encryptionKey,null,encryptionScheme); 
    }
    
    private void init(Secret encryptionKey,CryptScheme encryptionScheme,String keyHasherScheme,String charEncoding) throws EncryptionException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        if (encryptionKey == null)
        {
            throw new IllegalArgumentException("Encryption key was null");
        }
        
//        // DESedeKeySpec might complain with a rather cryptic Exception otherwise 
//        if ((keyHasherScheme==null) && (encryptionKey.trim().length() < MINIMUM_CRYPT_KEY_LENGTH))
//        {
//            throw new IllegalArgumentException("Encryption key was less than "+MINIMUM_CRYPT_KEY_LENGTH+" characters");
//        }
        
        if (keyHasherScheme==null)
        {
            this.usePlainCharBytes=true; 
        }
        else    
        {
            keyHasher= MessageDigest.getInstance(keyHasherScheme);
        }
            
        setCharacterEncoding(charEncoding);
        byte[] keyAsBytes = createKeyDigest(encryptionKey); 
        initKey(keyAsBytes,null,encryptionScheme);
        
    }
    
    public void setUsePlainCharBytes(boolean value)
    {
        this.usePlainCharBytes=value;  
    }
    
    /** 
     * Use hash algorithm to create an unsalted key digest from the password/passkey.  
     */ 
    public byte[] createKeyDigest(Secret password)
    {
        ByteBuffer bbuf=password.toByteBuffer(charSet); 
 
        // Legacy code, not recommended
        if (this.usePlainCharBytes)
            return bbuf.array(); 
        
        // return MD5 or SHA-256 hash. 
        this.keyHasher.reset(); 
        this.keyHasher.update(bbuf);
        byte keyBytes[]=keyHasher.digest();
        
        return keyBytes;
    }

    @SuppressWarnings("deprecation")
    protected void initKey(byte rawKey[],byte IV[],CryptScheme encryptionScheme) throws EncryptionException
    {
        if (rawKey==null)
        {
            throw new NullPointerException("Encryption key is null!");
        }
        
        this.cryptScheme=encryptionScheme; 
        String cipherScheme=encryptionScheme.getCipherScheme(); 
        int keyLen=encryptionScheme.getKeyLength();
        
        try
        {

            // IV only needed for CBC, not ECB:
            // IvParameterSpec ivSpec=null;
            //          
            // if (IV!=null)
            //    ivSpec = new IvParameterSpec(IV); 
            switch(encryptionScheme)
            {
                case DESEDE_ECB_PKCS5:
                {
                    keySpec = new DESedeKeySpec(rawKey);
                    keyFactory = SecretKeyFactory.getInstance(encryptionScheme.getCipherScheme());
                    cipher = Cipher.getInstance(encryptionScheme.getConfigString());
                    break; 
                }
                case AES128_ECB_PKCS5:
                //case AES192_ECB_PKCS5:
                case AES256_ECB_PKCS5:
                {
                    byte subkey[]=null; 
                    
                    if (rawKey.length<keyLen)
                    {
                        throw new EncryptionException ("AES Key length to short. Length="+rawKey.length+", must be at least:"+keyLen,null); 
                    }
                    
                    subkey=new byte[keyLen];  
                    for (int i=0;i<keyLen;i++)
                    {
                            subkey[i]=rawKey[i];
                    }
                    
                    keySpec = new SecretKeySpec(subkey,cipherScheme);
                    // Not needed. Directly use keysSpec as SecretKey for AES ! 
                    // SecretKeyFactory.getInstance(encryptionScheme.getSchemeName());
                    keyFactory = null;
                    cipher = Cipher.getInstance(encryptionScheme.getConfigString());
                    break; 
                }
                default: 
                {
                    throw new IllegalArgumentException("Encryption scheme not supported: " + encryptionScheme);
                }
            } // switch 
            
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
        catch (NoSuchPaddingException e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
        catch (InvalidKeyException e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
    }
    
    /**
     * Specify which characters set is used to get the bytes from password Strings. 
     * Default is UTF-8  
     */ 
    public void setCharacterEncoding(String charSetStr) throws UnsupportedEncodingException
    {
        charSet=Charset.forName(charSetStr);
        
        if (charSet==null)
            throw new UnsupportedEncodingException("No such Character encoding:"+charSetStr);
    }
    
    /** 
     * @see #setCharacterEncoding(String)
     */ 
    public Charset getCharacterEncoding()
    {
        return this.charSet; 
    }

    /**
     * Encrypts String and returns encoded result as base64 encoded String. 
     * This increases the String size by ~33%.  
     */
    public String encryptToBase64(String unencryptedString) throws EncryptionException
    {
        byte ciphertext[]=encrypt(unencryptedString);
        return StringUtil.base64Encode(ciphertext);
    }
    
    /**
     * Encrypts String and returns encoded result as bytes.
     */
    public byte[] encrypt(String unencryptedString) throws EncryptionException
    {
        if (StringUtil.isWhiteSpace(unencryptedString))
        {
            throw new IllegalArgumentException("unencrypted string was null or contains only whitespace.");
        }
        
        return encrypt(unencryptedString.getBytes(charSet));
    }
    
    public byte[] encrypt(byte bytes[]) throws EncryptionException
    {
        try
        {
            SecretKey key = null;
            
            if (keyFactory!=null)
            {
                key=keyFactory.generateSecret(keySpec);
            }
            else if (keySpec instanceof SecretKey)
            {
                key=(SecretKey)keySpec; // key is already a SecretKey ! (for example an AES key) 
            }
            else
            {
                throw new NullPointerException("KeyFactory isn't initialized and key specification is not a valid SecretKey!"); 
            }
            
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] ciphertext = cipher.doFinal(bytes);
            return ciphertext;
        }
        catch (InvalidKeySpecException e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
        catch (InvalidKeyException e)
        {
            if (this.cryptScheme.getCipherScheme().toUpperCase().startsWith("AES"))
            {
                if (e.getMessage().contains("Illegal key size"))
                {
                    
                    throw new EncryptionException("Illegal keysize for AES. Are Unlimited Strength Jurisdiction Policy Files for AES installed?\n"
                                                  +"Error="+e.getMessage(),e);
                }
            }
            throw new EncryptionException(e.getMessage(),e);
        }
        catch (IllegalBlockSizeException e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
        catch (BadPaddingException e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
    }

    /** 
     * Decrypt base64 encoded and encrypted String
     */ 
    public String decryptString(String base64String) throws EncryptionException
    {
        if (StringUtil.isWhiteSpace(base64String))
        {
            throw new IllegalArgumentException("Encrypted String was null or empty");
        }
        
        try
        {
            byte[] cleartext= StringUtil.base64Decode(base64String);
            byte[] ciphertext=decrypt(cleartext);
            return new String(ciphertext,charSet);
        }
        catch (IOException e)
        {
            throw new DecryptionFailedException("Base 64 decoding failed.\n"+e.getMessage(),e);  
        }

    }

    /** 
     * Decrypt hexadecimal encoded and encrypted String and return decoded String.  
     */ 
    public String decryptHexEncodedString(String hexEncodedString) throws EncryptionException
    {
        if (StringUtil.isWhiteSpace(hexEncodedString))
        {
            throw new IllegalArgumentException("Encrypted String was null or empty. Must be hexadecimal encoded String.");
        }
        
        byte[] cleartext= StringUtil.parseBytesFromHexString(hexEncodedString); 
        byte[] ciphertext=decrypt(cleartext); 
        
        return new String(ciphertext,charSet);
        
    }
    
    /**
     * Decrypt base64 encoded and encrypted String and return as bytes.
     */ 
    public byte[] decrypt(byte crypt[]) throws EncryptionException
    {
        if (crypt==null)
        {
            throw new NullPointerException("Byte array can't be null."); 
        }
        
        
        try
        {
            SecretKey key =null; 
            
            if (keyFactory!=null)
            {
                key=keyFactory.generateSecret(keySpec);
            }
            else if (keySpec instanceof SecretKey)
            {
                key=(SecretKey)keySpec; // key is already a SecretKey ! (for example an AES key) 
            }
            else
            {
                throw new NullPointerException("KeyFactory isn't initialized and key specification is not a valid SecretKey!"); 
            }
            
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(crypt);
        }
        catch (javax.crypto.BadPaddingException e)
        {
            throw new DecryptionFailedException("Decryption Failed: Bad or invalid key",e); 
        }
        catch (Exception e)
        {
            throw new EncryptionException(e.getMessage(),e);
        }
    }
  
}