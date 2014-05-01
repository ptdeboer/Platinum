package nl.esciencecenter.ptk.ui;

import nl.esciencecenter.ptk.data.SecretHolder;

import org.junit.Assert;
import org.junit.Test;

public class InteractiveRun_SimpelUI
{

      @Test
      public void testAskInput()
      {
          testAskYesNo("Yes",true); 
          testAskYesNo("No",false); 
      }

      
//    @Test
//    public void testAskInput()
//    {
//        testAskYesNo("Yes",true); 
//        testAskYesNo("yes",true);
//        testAskYesNo("Y",true);
//        testAskYesNo("y",true);
//        testAskYesNo("No",false);
//        testAskYesNo("no",false);
//        testAskYesNo("n",false);
//        testAskYesNo("N",false);
//    }
    
    protected void testAskYesNo(String answer, boolean expectedResult)
    {
        SimpelUI clui=new SimpelUI(); 
        
        boolean result=clui.askYesNo("testAskYesNo","Please enter '"+answer+"'",!expectedResult); 
        		
        Assert.assertEquals("Wrong result for expected answer:'"+answer+"'",expectedResult,result); 
    }
    
    @Test
    public void testPrivateInput()
    {
        SimpelUI clui=new SimpelUI();

        SecretHolder secretHolder=new SecretHolder(); 
        clui.askAuthentication("Type password:  'Geheim'", secretHolder); 
                
        char[] chars = secretHolder.getChars(); 
        
        Assert.assertEquals("Wrong secret input","Geheim",new String(chars));  

    }
    
    @Test
    public void testInput()
    {
        SimpelUI clui=new SimpelUI();
        
        String title="Please change value into 'New Value'";
        String value= "Old Value"; 
        
        String newValue=clui.askInput(title, title,  value); 
        
        Assert.assertEquals("Wrong input value","New Value",newValue); 
        
    }
}
