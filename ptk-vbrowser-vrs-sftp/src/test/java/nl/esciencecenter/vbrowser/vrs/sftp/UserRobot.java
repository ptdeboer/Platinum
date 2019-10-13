package nl.esciencecenter.vbrowser.vrs.sftp;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserRobot implements UserInfo, UIKeyboardInteractive {

    protected String user;
    protected char[] passwd;
    protected char[] passphrase;
    protected boolean answerYes;

    public UserRobot(String user, char[] passwd, char[] passphrase, boolean answerYes) {
        this.user = user;
        this.passwd = passwd;
        this.passphrase = passphrase;
        this.answerYes = answerYes;
    }

    @Override
    public String getPassphrase() {
        log.info("getPassphrase()");
        return new String(passphrase);
    }

    @Override
    public String getPassword() {
        log.info("getPasswd()");
        return new String(passwd);
    }

    @Override
    public boolean promptPassphrase(String message) {
        log.info("promptPassphrase:{}", message);
        return (passphrase != null);
    }

    @Override
    public boolean promptPassword(String message) {
        log.info("promptPassword:{}", message);
        return (passwd != null);
    }

    @Override
    public boolean promptYesNo(String message) {
        log.info("Answer yes/no:{}", message);
        return answerYes;
    }

    @Override
    public void showMessage(String message) {
        log.info("{}", message);
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

        if ((prompts.length == 1) && (prompts[0].compareToIgnoreCase("Password:") == 0)) {
            return new String[]{new String(passwd)};
        }

        return null;
    }
}
