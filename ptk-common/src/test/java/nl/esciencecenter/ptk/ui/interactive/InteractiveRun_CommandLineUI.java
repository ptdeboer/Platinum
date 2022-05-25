package nl.esciencecenter.ptk.ui.interactive;

import nl.esciencecenter.ptk.data.SecretHolder;
import nl.esciencecenter.ptk.ui.ConsoleUI;
import org.junit.Assert;
import org.junit.Test;

public class InteractiveRun_CommandLineUI {

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

    protected void testAskYesNo(String answer, boolean expectedResult) {
        ConsoleUI clui = new ConsoleUI();

        boolean result = clui.askYesNo("testAskYesNo", "Please enter '" + answer + "'", !expectedResult);

        Assert.assertEquals("Wrong result for expected answer:'" + answer + "'", expectedResult, result);
    }

    @Test
    public void testPrivateInput() {
        ConsoleUI clui = new ConsoleUI();

        SecretHolder secretHolder = new SecretHolder();
        clui.askAuthentication("Type password:  'Geheim'", secretHolder);

        char[] chars = secretHolder.getChars();

        Assert.assertEquals("Wrong secret input", "Geheim", new String(chars));

    }

    @Test
    public void testInput() {
        ConsoleUI clui = new ConsoleUI();

        String title = "Please provide a value.";
        String value = "The Value";

        String newValue = clui.askInput(title, "Input Message", value);

        System.out.printf("askInput() '%s' => '%s'\n", value, newValue);
    }
}
