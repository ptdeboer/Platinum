package nl.esciencecenter.ptk.ui;

import nl.esciencecenter.ptk.data.SecretHolder;

/** 
 * Dummy non interactive UI, returns false, cancel or default value. 
 */
public class DummyUI implements UI
{

    @Override
    public boolean isEnabled()
    {
        return false; 
    }

    @Override
    public void showMessage(String title, String message, boolean modal)
    {
    }

    @Override
    public boolean askYesNo(String title, String message, boolean defaultValue)
    {
        return false;
    }

    @Override
    public boolean askOkCancel(String title, String message, boolean defaultValue)
    {
        return false;
    }

    @Override
    public int askYesNoCancel(String title, String message)
    {
        return CANCEL_OPTION; 
    }

    @Override
    public boolean askAuthentication(String message, SecretHolder secretHolder)
    {
        return false;
    }

    @Override
    public String askInput(String title, String message)
    {
        return null;
    }

}
