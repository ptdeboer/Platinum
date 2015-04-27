package nl.esciencecenter.vbrowser.vrs.sftp;

import nl.esciencecenter.ptk.data.SecretHolder;
import nl.esciencecenter.ptk.ui.UI;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * User UI Bindings between Sftp UserInfo and VRSContext UI.
 *
 * @author Piter T. de Boer.
 */
public class UserUI implements UserInfo, UIKeyboardInteractive
{

    protected UI contextUI;

    public UserUI(UI ui)
    {

        contextUI = ui;
    }

    @Override
    public String getPassphrase()
    {
        SecretHolder secretH = new SecretHolder();
        contextUI.askAuthentication("Provide Passphrase please", secretH);
        return new String(secretH.getChars());
    }

    @Override
    public String getPassword()
    {
        SecretHolder secretH = new SecretHolder();
        contextUI.askAuthentication("Provide Password please", secretH);
        return new String(secretH.getChars());
    }

    @Override
    public boolean promptPassphrase(String message)
    {
        // boolean opt = contextUI.askYesNo("promptPassphrase", message, true);
        // return opt;
        return true;
    }

    @Override
    public boolean promptPassword(String message)
    {
        // boolean opt = contextUI.askYesNo("promptPassword", message, true);
        // return opt;
        return true;
    }

    @Override
    public boolean promptYesNo(String message)
    {
        boolean opt = contextUI.askYesNo("Question", message, true);
        return opt;
    }

    @Override
    public void showMessage(String message)
    {
        contextUI.showMessage("Sftp", message, false);
    }

    @Override
    public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompts,
            boolean[] echo)
    {
        System.out.printf(" Destination:%s\n", destination);
        System.out.printf("        name:%s\n", name);
        System.out.printf(" instruction:%s\n", instruction);

        for (String prompt : prompts)
        {
            System.out.printf(" - prompt:'%s'\n", prompt);
        }

        if ((prompts.length == 1) && (prompts[0].toLowerCase().startsWith(("password:"))))
        {
            String pwd = getPassword();
            return new String[] {
                pwd
            };
        }

        return null;
    }
}
