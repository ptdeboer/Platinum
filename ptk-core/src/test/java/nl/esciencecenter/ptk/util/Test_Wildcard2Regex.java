package nl.esciencecenter.ptk.util;

import org.junit.Test;

import java.util.regex.Pattern;

public class Test_Wildcard2Regex {
    @Test
    public void testWildcards() {
        String test = "123ABC";
        //System.out.println(test);
        testWildcard("1*", test, true);
        testWildcard("?2*", test, true);
        testWildcard("??2*", test, false);
        testWildcard("*A*", test, true);
        testWildcard("*Z*", test, false);
        testWildcard("123*", test, true);
        testWildcard("123", test, false);
        testWildcard("*ABC", test, true);
        testWildcard("*abc", test, false);
        testWildcard("ABC*", test, false);

        // output : 123ABC true true false true false true false true false false
    }


    private void testWildcard(String pattern, String sourceString, boolean matches) {
        boolean result = Pattern.matches(Wildcard2Regex.wildcardToRegex(pattern), sourceString);
        if (matches != result)
            System.err.printf("Invalid match: Pattern:'%s' on string '%s' should return:%s\n", pattern, sourceString, matches);
    }
}
