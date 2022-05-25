package nl.esciencecenter.ptk.data;

import org.junit.Assert;
import org.junit.Test;

public class Test_HashMapList {

    @Test
    public void testIndex() {
        HashMapList<String, String> hash = new HashMapList<>();

        int n = 1000;
        String[] keys = new String[n];
        String[] vals = new String[n];

        for (int i = 0; i < 100; i++) {
            keys[i] = "k" + i;
            vals[i] = "v" + i;

            hash.put(keys[i], vals[i]);
        }

        String[] arr = new String[]{};
        arr = hash.keySet().toArray(new String[0]);

        // must return keys in correct order !
        Object[] actualKeys = hash.keySet().toArray();
        String[] actualValues = hash.toArray(new String[0]);

        for (int i = 0; i < 100; i++) {
            Object actualKey = actualKeys[i];
            Assert.assertEquals("Value at #" + i + " not correct.", vals[i], hash.get(keys[i]));
            Assert.assertEquals("Key #" + i + " not correct.", keys[i], actualKey);
            Assert.assertEquals("getKeyArray() has wrong value at #" + i, keys[i], actualKeys[i]);
            Assert.assertEquals("toArray() has wrong value at #" + i, vals[i], actualValues[i]);
        }
    }

    @Test
    public void testInserts() {

        HashMapList<String, String> hash = new HashMapList<String, String>();
        hash.put("aap", "nut");

    }

}
