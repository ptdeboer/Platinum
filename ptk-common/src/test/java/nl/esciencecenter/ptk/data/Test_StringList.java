package nl.esciencecenter.ptk.data;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class Test_StringList {

    @Test
    public void testConstructors() {
        StringList list = new StringList();
        Assert.assertEquals("New StringList() must be empty", 0, list.size());

        list = new StringList(new String[0]);
        Assert.assertEquals("New StringList(String[0]) must be empty", 0, list.size());

        list = new StringList("foo");
        Assert.assertEquals("New StringList must contain one entry", 1, list.size());
        Assert.assertEquals("StringList element at #0 doesn't match", "foo", list.get(0));

    }

    @Test
    public void testArrayLists() {
        testToAndFromArray(new String[]{"foo"});
        testToAndFromArray(new String[]{"foo", "bar"});
        testToAndFromArray(new String[]{"foo", "bar", "fum"});
    }

    protected void testToAndFromArray(String[] array) {
        int len = array.length;

        StringList list = new StringList(array);
        Assert.assertEquals("StringList size must match array size.", array.length, list.size());

        for (int i = 0; i < len; i++) {
            Assert.assertEquals("StringList element at #" + i + "doesn't match.", array[i], list.get(i));
        }

        String[] otherArray = list.toArray();
        for (int i = 0; i < len; i++) {
            Assert.assertEquals("Recreate array element at #" + i + "doesn't match original.", array[i], otherArray[i]);
        }

        // ArrayList doesn't support wrap()
        List<String> arrayList = new ArrayList<String>();
        for (int i = 0; i < array.length; i++) {
            arrayList.add(array[i]);
        }

        // Create StringList from ArrayList<String> which is the super type of StringList. 
        StringList listFromArrayList = new StringList(arrayList);
        for (int i = 0; i < len; i++) {
            Assert.assertEquals("StringList from ArrayList's element at #" + i + "doesn't match original.", arrayList.get(i), listFromArrayList.get(i));
        }

    }


    @Test
    public void testListSort() {
        // Check QSort unit tests: 
        testListSort(new String[]{"a"}, new String[]{"a"}, false);
        testListSort(new String[]{"a", "b"}, new String[]{"a", "b"}, false);
        testListSort(new String[]{"b", "a"}, new String[]{"a", "b"}, false);

        testListSort(new String[]{"a", "b", "c"}, new String[]{"a", "b", "c"}, false);
        testListSort(new String[]{"a", "c", "b"}, new String[]{"a", "b", "c"}, false);
        testListSort(new String[]{"b", "a", "c"}, new String[]{"a", "b", "c"}, false);
        testListSort(new String[]{"b", "c", "a"}, new String[]{"a", "b", "c"}, false);
        testListSort(new String[]{"c", "a", "b"}, new String[]{"a", "b", "c"}, false);
        testListSort(new String[]{"c", "b", "a"}, new String[]{"a", "b", "c"}, false);

    }

    protected void testListSort(String[] source, String[] expected, boolean ignoreCase) {
        StringList list = new StringList(source);
        list.sort(ignoreCase);

        int len = source.length;

        for (int i = 0; i < len; i++) {
            Assert.assertEquals("Sorted String list's element at #" + i + "doesn't match expected.", list.get(i), expected[i]);
        }
    }

    @Test
    public void testNullValues() {
        StringList list = new StringList((String) null);
        Assert.assertEquals("New StringList(null) must copntain one null entry", 1, list.size());
        Assert.assertEquals("StringList(null) must copntain one null entry at (0)", null, list.get(0));

        list = new StringList(null, null);
        Assert.assertEquals("New StringList(null) must copntain 2 entry", 2, list.size());
        Assert.assertEquals("StringList(null) must contain null entry at (0)", null, list.get(0));
        Assert.assertEquals("StringList(null) must contain null entry at (1)", null, list.get(1));

    }

    @Test
    public void testNullArrayLists() {
        // Null entries are allowed: 
        testToAndFromArray(new String[]{null});
        testToAndFromArray(new String[]{null, null});
        testToAndFromArray(new String[]{null, null, null});
    }

}
