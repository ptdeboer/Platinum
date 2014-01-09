package nl.esciencecenter.presentation;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import nl.esciencecenter.ptk.presentation.Presentation;

import org.junit.Assert;
import org.junit.Test;

public class Test_Presentation
{

    public void doTestPresentationDateTimeString(String datestr)
    {
        Date date=Presentation.createDateFromNormalizedDateTimeString(datestr);
        String reversestr=Presentation.createNormalizedDateTimeString(date); 

        Assert.assertEquals("Normalized datetime strings should be the same",datestr,reversestr); 
    }

    public void doTestPresentationDateTimeString(String datestr,String timeZone)
    {
        Date date=Presentation.createDateFromNormalizedDateTimeString(datestr);
        String reversestr=Presentation.createNormalizedDateTimeString(date,timeZone); 

        Assert.assertEquals("Normalized datetime strings should be the same",datestr,reversestr); 
        
        Calendar cal=Calendar.getInstance(TimeZone.getTimeZone(timeZone));  
        cal.setTime(date);
        
        String calTZ=cal.getTimeZone().getID(); 
        Assert.assertEquals("Normalized TimeZone must match original TimeZone",timeZone,calTZ); 
        
    }
    
    @Test
    public void testPresentationDateTimeString_noTimezone() 
    {
        // text exception: 
        // doTestPresentationDateTimeString("0000-01-01 00:00:00.000",false); // year zero is year -1 
        doTestPresentationDateTimeString("0001-01-01 00:00:00.000");
        doTestPresentationDateTimeString("1970-01-13 01:23:45.678");  
        doTestPresentationDateTimeString("999999-12-31 23:59:59.999");  
    }
    
    @Test
    public void testPresentationDateTimeString_NegativeNoTimezone() 
    {
        // negative time is B.C. 
        doTestPresentationDateTimeString("-0001-01-01 00:00:00.000");
        doTestPresentationDateTimeString("-1970-01-13 01:23:45.678");  
        doTestPresentationDateTimeString("-999999-12-31 23:59:59.999");  
    }     
    
    @Test
    public void testPresentationDateTimeString_GMT() 
    {
        // text exception: 
        //testPresentationDateTimeString("000000-00-00 00:00:00.000");
        doTestPresentationDateTimeString("0001-01-01 00:00:00.000 GMT","GMT");
        doTestPresentationDateTimeString("1970-01-13 01:23:45.678 GMT","GMT");  
        doTestPresentationDateTimeString("999999-12-31 23:59:59.999 GMT","GMT");  
    }

    @Test
    public void testPresentationDateTimeString_CET() 
    {
        // text exception: 
        //testPresentationDateTimeString("000000-00-00 00:00:00.000");
        doTestPresentationDateTimeString("0001-01-01 00:00:00.000 CET","CET");
        doTestPresentationDateTimeString("1970-01-13 01:23:45.678 CET","CET");  
        doTestPresentationDateTimeString("999999-12-31 23:59:59.999 CET","CET");  
    }
    
}
