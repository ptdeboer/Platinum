package nl.esciencecenter.vbrowser.vrs.sftp;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class UserRobot implements UserInfo, UIKeyboardInteractive {

    protected String user;
    protected char passwd[];
    protected char passphrase[];
    protected boolean answerYes;

    public UserRobot(String user, char[] passwd, char[] passphrase, boolean answerYes) {
        this.user = user;
        this.passwd = passwd;
        this.passphrase = passphrase;
        this.answerYes = answerYes;
    }

    @Override
    public String getPassphrase() {
        outPrintf("getPassphrase()\n");
        return new String(passphrase);
    }

    @Override
    public String getPassword() {
        outPrintf("getPasswd()\n");
        return new String(passwd);
    }

    @Override
    public boolean promptPassphrase(String message) {
        outPrintf("promptPassphrase:%s\n", message);
        return (passphrase != null);
    }

    @Override
    public boolean promptPassword(String message) {
        outPrintf("promptPassword:%s\n", message);
        return (passwd != null);
    }

    @Override
    public boolean promptYesNo(String message) {
        outPrintf("Answer yes/no:%s\n", message);
        return answerYes;
    }

    @Override
    public void showMessage(String message) {
        outPrintf("%s\n", message);
    }

    public static void outPrintf(String format, Object... args) {
        System.out.printf(format, args);
    }

    @Override
    public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompts,
            boolean[] echo)
    {
        System.out.printf(" Destination:%s\n",destination);
        System.out.printf("        name:%s\n",name);
        System.out.printf(" instruction:%s\n",instruction);

        for (String prompt:prompts) {
            System.out.printf(" - prompt:'%s'\n",prompt);
        }

        if ((prompts.length==1) && (prompts[0].compareToIgnoreCase("Password:")==0))
        {
            return new String[]{new String (passwd)};
        }

        return null;
    }
}
