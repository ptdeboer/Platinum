package nl.esciencecenter.vbrowser.vrs.sftp;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import nl.esciencecenter.ptk.data.SecretHolder;
import nl.esciencecenter.ptk.ui.UI;

/**
 * User UI Bindings between Sftp UserInfo+UIKeyboardInteractive and VRSContext UI.
 */
public class UserUI implements UserInfo, UIKeyboardInteractive {

    protected UI contextUI;

    public UserUI(UI ui) {

        contextUI = ui;
    }

    @Override
    public String getPassphrase() {
        SecretHolder secretH = new SecretHolder();
        contextUI.askAuthentication("Provide Passphrase please", secretH);
        return new String(secretH.getChars());
    }

    @Override
    public String getPassword() {
        SecretHolder secretH = new SecretHolder();
        contextUI.askAuthentication("Provide Password please", secretH);
        return new String(secretH.getChars());
    }

    public String askPassword(String message) {
        SecretHolder secretH = new SecretHolder();
        contextUI.askAuthentication(message, secretH);
        if (secretH.isSet() == false) {
            return null;
        }
        return new String(secretH.getChars());
    }

    @Override
    public boolean promptPassphrase(String message) {
        // boolean opt = contextUI.askYesNo("promptPassphrase", message, true);
        // return opt;
        return true;
    }

    @Override
    public boolean promptPassword(String message) {
        // boolean opt = contextUI.askYesNo("promptPassword", message, true);
        // return opt;
        return true;
    }

    @Override
    public boolean promptYesNo(String message) {
        boolean opt = contextUI.askYesNo("Question", message, true);
        return opt;
    }

    @Override
    public void showMessage(String message) {
        contextUI.showMessage("Sftp", message, false);
    }

    @Override
    public String[] promptKeyboardInteractive(String destination, String name, String instruction,
                                              String[] prompts, boolean[] echo) {
        System.out.printf(" Destination:%s\n", destination);
        System.out.printf("        name:%s\n", name);
        System.out.printf(" instruction:%s\n", instruction);

        for (String prompt : prompts) {
            System.out.printf(" - prompt:'%s'\n", prompt);
        }

        if (prompts.length == 1) {
            if (prompts[0].toLowerCase().startsWith(("password:"))) {
                String pwd = askPassword("Please provide Password for:" + destination);
                if (pwd == null)
                    return null;
                return new String[]{pwd};
            } else {
                String pwd = askPassword("Authentication needed for:" + destination + "\n" + prompts[0]);
                if (pwd == null)
                    return null;
                return new String[]{pwd};

            }
        }

        return null;
    }
}
