/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.ptk.ui;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.crypt.Secret;
import nl.esciencecenter.ptk.data.SecretHolder;
import nl.esciencecenter.ptk.util.StringUtil;

import javax.swing.*;
import java.io.Console;

/**
 * Command Line UI redirects UI request to command line. Uses System.console and System.err for
 * stdin, stdout and stderr respectively.
 */
@Slf4j
public class ConsoleUI implements UI {

    private Console console;

    public ConsoleUI() {
        console = System.console();
        if (console == null) {
            log.warn("Warning: No System.console()");
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void showMessage(String title, String message, boolean modal) {
        if (console == null) {
            log.warn("NO CONSOLE:showMessage:[{}]:{}", title, message);
        } else {
            console.printf("[%s]\n%s\n", title, message);
        }
    }

    @Override
    public boolean askYesNo(String title, String message, boolean defaultValue) {
        if (console == null) {
            log.warn("NO CONSOLE:askYesNo:[{}]:{}", title, message);
        } else {
            console.printf("[%s]\n%s\n Y)es/N)o?", title, message);
        }
        String str = getInput("");

        if (str == null) {
            return defaultValue;
        }
        return StringUtil.equalsIgnoreCase(str, "Y", "YES");
    }

    @Override
    public boolean askOkCancel(String title, String message, boolean defaultValue) {
        if (console == null) {
            log.warn("NO CONSOLE:askOkCancel:[{}]:{}", title, message);
        } else {
            console.printf("[%s]\n%s\n O)k/C)ancel?", title, message);
        }
        String str = getInput("");

        if (str == null) {
            return defaultValue;
        }
        return StringUtil.equalsIgnoreCase(str, "O", "OK");
    }

    @Override

    public int askYesNoCancel(String title, String message) {
        if (console == null) {
            log.warn("NO CONSOLE:askYesNoCancel:[{}]:{}", title, message);
        } else {
            console.printf("[%s]\n%s\n Y)es/N)o/C)ancel?", title, message);
        }
        String str = getInput("");

        if (str == null) {
            return 0;
        }

        boolean yes = StringUtil.equalsIgnoreCase(str, "Y", "YES");
        boolean no = StringUtil.equalsIgnoreCase(str, "N", "No");
        @SuppressWarnings("unused")
        boolean cancel = StringUtil.equalsIgnoreCase(str, "C", "Cancel");

        if (yes) {
            return JOptionPane.YES_OPTION;
        } else if (no) {
            return JOptionPane.NO_OPTION;
        } else {
            return JOptionPane.CANCEL_OPTION;
        }
    }

    @Override
    public boolean askAuthentication(String message, SecretHolder secretHolder) {
        if (console == null) {
            log.warn("NO CONSOLE:askAuthentication:[{}]:{}", message);
        } else {
            console.printf("[Authentication Needed]\n%s\n", message);
        }

        char[] chars = this.getPrivateInput("?");
        if (chars == null || chars[0] == 0) {
            return false;
        }
        secretHolder.value = new Secret(chars);
        return true;
    }

    public String getInput(String prompt) {
        if (console == null) {
            log.warn("NO CONSOLE:getInput:{}", prompt);
            return null;
        }
        console.printf("%s", prompt);
        return console.readLine();
    }

    public char[] getPrivateInput(String prompt) {
        if (console == null) {
            log.warn("NO CONSOLE:getPrivateInput:{}", prompt);
            return null;
        }
        console.printf("%s", prompt);
        return console.readPassword();
    }

    @Override
    public String askInput(String title, String message, String defaultValue) {
        if (console == null) {
            log.warn("NO CONSOLE:askInput:[{}}:{}", title,message);
            return null;
        }
        console.printf("[%s]\n%s\n '%s'?", title, message, defaultValue);
        return getInput("");
    }

}
