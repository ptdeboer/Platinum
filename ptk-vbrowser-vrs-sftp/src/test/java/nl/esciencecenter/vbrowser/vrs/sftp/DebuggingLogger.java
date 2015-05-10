package nl.esciencecenter.vbrowser.vrs.sftp;

import com.jcraft.jsch.Logger;

public class DebuggingLogger implements Logger {

    @Override
    public boolean isEnabled(int level) {
        return true;
    }

    @Override
    public void log(int level, String message) {
        System.err.printf("%s\n", message);
    }

}
