package nl.esciencecenter.ptk.data;

import nl.esciencecenter.ptk.data.HashMapList;

import org.junit.Assert;
import org.junit.Test;

public class Test_HashMapList
{

    @Test
    public void testIndex()
    {
        HashMapList<String,String> hash=new HashMapList<String,String>(); 
        
        int n=1000;
        String keys[]=new String[n];
        String vals[]=new String[n];
               
        for (int i=0;i<100;i++)
        {
            keys[i]="k"+i;
            vals[i]="v"+i; 
            
            hash.put(keys[i],vals[i]); 
        }
        
        String arr[]=new String[]{};
     
        arr=hash.getKeyArray(arr);
        String keyArr[]=hash.getKeyArray(new String[0]);
        String valsArr[]=hash.toArray(new String[0]);
        
        for (int i=0;i<100;i++)
        {
            Assert.assertEquals("Value at #"+i+" not correct.", vals[i],hash.get(keys[i])); 
            Assert.assertEquals("Key #"+i+" not correct.",keys[i],hash.getKey(i));
            Assert.assertEquals("getKeyArray() has wrong value at #"+i,keys[i],keyArr[i]);
            Assert.assertEquals("toArray() has wrong value at #"+i,vals[i],valsArr[i]);
        }
    }
        
    @Test
    public void testInserts()
    {
        
    }

}
