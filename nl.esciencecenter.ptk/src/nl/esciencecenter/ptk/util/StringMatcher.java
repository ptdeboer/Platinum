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

package nl.esciencecenter.ptk.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StringMatcher
{
    // === Static === 
        
    public static StringMatcher createREMatcher(String regularExpr,boolean matchCase)
    {
        return new StringMatcher(regularExpr,matchCase); 
    }
    
    public static StringMatcher createWildcardMatcher(String wildCardExpr,boolean matchCase)
    {
        return new StringMatcher(Wildcard2Regex.wildcardToRegex(wildCardExpr),matchCase); 
    }

    // === Instancee === 

    protected Pattern pattern=null; 
    
    protected int flags=0;

    public StringMatcher(String pattern)
    {
        this.flags=0;
        this.pattern=Pattern.compile(pattern, flags);
    }
    
    public StringMatcher(String pattern, boolean matchCase)
    {
        this.flags=0;
        if (matchCase==false)
            this.flags|=Pattern.CASE_INSENSITIVE; 
        this.pattern=Pattern.compile(pattern, flags);
    }
    
    public boolean matches(String value, boolean completeMatch)
    {
        Matcher m=pattern.matcher(value);  
        boolean prefixMatch=m.matches();
        //m.toMatchResult()
        return prefixMatch; 
    }


}
