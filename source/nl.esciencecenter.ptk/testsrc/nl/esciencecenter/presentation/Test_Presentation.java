package nl.esciencecenter.presentation;

import java.util.Date;

import nl.esciencecenter.ptk.presentation.Presentation;

import org.junit.Assert;
import org.junit.Test;

public class Test_Presentation
{

    public void doTestPresentationDateTimeString(String datestr,boolean includeTimeZone)
    {
        Date date=Presentation.createDateFromNormalizedDateTimeString(datestr);
        String reversestr=Presentation.createNormalizedDateTimeString(date,includeTimeZone); 
        
        Assert.assertEquals("Normalized datetime strings should be the same",datestr,reversestr); 
    }
    
    @Test
    public void testPresentationDateTimeString_noTimezone() 
    {
        // text exception: 
        // doTestPresentationDateTimeString("0000-01-01 00:00:00.000",false); // year zero is year -1 
        doTestPresentationDateTimeString("0001-01-01 00:00:00.000",false);
        doTestPresentationDateTimeString("1970-01-13 01:23:45.678",false);  
        doTestPresentationDateTimeString("999999-12-31 23:59:59.999",false);  
    }
    
    @Test
    public void testPresentationDateTimeString_NegativeNoTimezone() 
    {
        // negative time is B.C. 
        doTestPresentationDateTimeString("-0001-01-01 00:00:00.000",false);
        doTestPresentationDateTimeString("-1970-01-13 01:23:45.678",false);  
        doTestPresentationDateTimeString("-999999-12-31 23:59:59.999",false);  
    }     
    
    @Test
    public void testPresentationDateTimeString_GMT() 
    {
        // text exception: 
        //testPresentationDateTimeString("000000-00-00 00:00:00.000");
        doTestPresentationDateTimeString("0001-01-01 00:00:00.000 GMT",true);
        doTestPresentationDateTimeString("1970-01-13 01:23:45.678 GMT",true);  
        doTestPresentationDateTimeString("999999-12-31 23:59:59.999 GMT",true);  
    }
    
}
