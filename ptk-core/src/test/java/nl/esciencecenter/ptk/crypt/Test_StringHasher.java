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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import nl.esciencecenter.ptk.crypt.StringHasher;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.PLogger;

import org.junit.Assert;
import org.junit.Test;

public class Test_StringHasher
{
    static
    {
        PLogger logger=PLogger.getLogger(StringHasher.class);
        logger.setLevelToWarn(); 
    }
    
    @Test
    public void test_MD5() throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        StringHasher hasher = new StringHasher("MD5");
        String salt="";
        //testHash(hasher,null,null);
        testHash(hasher,salt,"","D41D8CD98F00B204E9800998ECF8427E"); 
        testHash(hasher,salt,"0","CFCD208495D565EF66E7DFF9F98764DA"); 
        testHash(hasher,salt,"1","C4CA4238A0B923820DCC509A6F75849B");
        // don't use 12345 as MD5 hashed password ! 
        testHash(hasher,salt,"12345","827CCB0EEA8A706C4C34A16891F84E7B");
        testHash(hasher,salt,"00000000000000000000000000000000","CD9E459EA708A948D5C2F5A6CA8838CF");
    }
    
    @Test
    public void test_MD5Salted() throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        StringHasher hasher = new StringHasher("MD5");
        testHash(hasher,"","","D41D8CD98F00B204E9800998ECF8427E"); 
        testHash(hasher,"hello","","5D41402ABC4B2A76B9719D911017C592");
        testHash(hasher,"hello","salt1","8EE3F8E4B41664B9B943D039BBA41A5C");
        testHash(hasher,"","salt1","55F312F84E7785AA1EFA552ACBF251DB"); 
    }
    
    @Test
    public void test_SHA256Hash() throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        StringHasher hasher = new StringHasher("SHA-256");
        //testHash(hasher,null,null);
        String salt=""; 
        testHash(hasher,salt,"","E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855");
        testHash(hasher,salt,"0","5FECEB66FFC86F38D952786C6D696C79C2DBC239DD4E91B46729D73A27FB57E9"); 
        testHash(hasher,salt,"1","6B86B273FF34FCE19D6B804EFF5A3F5747ADA4EAA22F1D49C01E52DDB7875B4B"); 
        testHash(hasher,salt,"12345","5994471ABB01112AFCC18159F6CC74B4F511B99806DA59B3CAF5A9C173CACFC5"); 
        testHash(hasher,salt,"00000000000000000000000000000000","84E0C0EAFAA95A34C293F278AC52E45CE537BAB5E752A00E6959A13AE103B65A"); 
    }

    @Test
    public void test_SHA256SaltedHash() throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        // The salt String is just appended to the source text: text+salt 
        // So hashing "hello" or the "salt1" String alone will yield the same hash when
        // either used as source text or salt. This is tested here also. 
        
        StringHasher hasher = new StringHasher("SHA-256");
        
        testHash(hasher,"","","E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855");
        
        testHash(hasher,"hello","","2CF24DBA5FB0A30E26E83B2AC5B9E29E1B161E5C1FA7425E73043362938B9824"); 
        testHash(hasher,"","hello","2CF24DBA5FB0A30E26E83B2AC5B9E29E1B161E5C1FA7425E73043362938B9824");
        
        testHash(hasher,"salt1","","DC90CF07DE907CCC64636CEDDB38E552A1A0D984743B1F36A447B73877012C39");
        testHash(hasher,"","salt1","DC90CF07DE907CCC64636CEDDB38E552A1A0D984743B1F36A447B73877012C39");
   
        testHash(hasher,"hello","salt1","98199B80A1D79568AC5C6D56A28D46E27434CE063C36AE7B27D635979ABB9F15");
        testHash(hasher,"hellosalt1","","98199B80A1D79568AC5C6D56A28D46E27434CE063C36AE7B27D635979ABB9F15");
        testHash(hasher,"","hellosalt1","98199B80A1D79568AC5C6D56A28D46E27434CE063C36AE7B27D635979ABB9F15");
        
        testHash(hasher,"","abc","BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD");  
        testHash(hasher,"a","bc","BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD");  
        testHash(hasher,"ab","c","BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD");  
        testHash(hasher,"abc","","BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD");  
       
        testHash(hasher,"","123","A665A45920422F9D417E4867EFDC4FB8A04A1F3FFF1FA07E998E86F7F7A27AE3");
        testHash(hasher,"1","23","A665A45920422F9D417E4867EFDC4FB8A04A1F3FFF1FA07E998E86F7F7A27AE3");
        testHash(hasher,"12","3","A665A45920422F9D417E4867EFDC4FB8A04A1F3FFF1FA07E998E86F7F7A27AE3");
        testHash(hasher,"123","","A665A45920422F9D417E4867EFDC4FB8A04A1F3FFF1FA07E998E86F7F7A27AE3");
    }
    
    @Test
    public void testSHA256Truncated() throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        StringHasher hasher = new StringHasher("SHA-256");
        
        // echo -n hello | sha256sum
        String hash32="2CF24DBA5FB0A30E26E83B2AC5B9E29E1B161E5C1FA7425E73043362938B9824"; 
        testHash(hasher,"","hello",32,hash32);
        
        for (int i=32; i>4;i--)
        {
            String shortHash=createExorHash(hash32,i); 
            testHash(hasher,"","hello",i,shortHash);
        }
     }

    // ===
    // Helper Methods 
    // === 
    
    protected String createExorHash(String hexString, int len)
    {
        byte bytes[]=StringUtil.parseBytesFromHexString(hexString); 
        byte result[]=new byte[len];
        // needed? 
        // for (int i=0;i<len;i++)
        //    result[i]=0; 
        
        for (int i=0;i<bytes.length;i++)
            result[i%len]^=bytes[i]; // exor and wrap around len
        
        String shortHash=StringUtil.toHexString(result);
        return shortHash; 
        
    }
    
    public void testHash(StringHasher hasher, String salt, String text, String expectedHash)
    {
        String digest=hasher.createHashToHexString(salt,text,-1); 
        Assert.assertEquals("Salted hash not the same.",expectedHash,digest);
    }
    
    public void testHash(StringHasher hasher, String salt,String text,int maxLen,String expectedHash)
    {
        //hasher.setSalt(""); 
        String digest=hasher.createHashToHexString(salt,text,maxLen); 
        Assert.assertEquals("Salted hash not the same (maxLen="+maxLen+").",expectedHash,digest);
    }
 }
