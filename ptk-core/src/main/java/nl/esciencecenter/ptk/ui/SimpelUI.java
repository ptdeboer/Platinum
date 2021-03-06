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

import javax.swing.*;

/**
 * Simple UI Adapter Object.
 */
@Slf4j
public class SimpelUI implements UI {

    private boolean enabled = true;

    public SimpelUI() {
    }

    public void setEnabled(boolean enable) {
        this.enabled = enable;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void showMessage(String title, String message, boolean modal) {
        log.debug("SimpelUI(enabled={}):{}", enabled, message);
        //
        if (enabled == false) {
            return;
        }
        JOptionPane.showMessageDialog(null, message);
    }

    public boolean askYesNo(String title, String message, boolean defaultValue) {
        log.debug("SimpelUI(enabled={}):askYesNo:[{}]:{}", enabled, title, message);
        //
        if (enabled == false) {
            return defaultValue;
        }
        int result = JOptionPane.showOptionDialog(null, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE, null, null, JOptionPane.NO_OPTION);

        return (result == JOptionPane.YES_OPTION);
    }

    @Override
    public boolean askOkCancel(String title, String message, boolean defaultValue) {
        log.debug("SimpelUI(enabled={}):askOkCancel:[{}]:{}", enabled, title, message);
        //
        if (enabled == false) {
            return defaultValue;
        }

        int result = JOptionPane.showOptionDialog(null, message, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE, null, null, JOptionPane.NO_OPTION);

        return (result == JOptionPane.OK_OPTION);
    }

    // @return JOptionPane.YES_OPTION JOptionPane.NO_OPTION or
    // JOptionPane.CANCEL_OPTION
    public int askYesNoCancel(String title, String message) {
        log.debug("SimpelUI(enabled={}):askYesNoCancel:[{}]:{}", enabled, title, message);
        //
        if (enabled == false) {
            return JOptionPane.CANCEL_OPTION;
        }

        int result = JOptionPane.showOptionDialog(null, message, title, JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE, null, null, JOptionPane.CANCEL_OPTION);

        return result;
    }

    public int askInput(String title, Object[] inputFields, int optionPaneOption) {
        log.debug("SimpelUI(enabled={}):askInput:[{}]:{}", enabled, title, inputFields);
        //
        if (enabled == false) {
            return CANCEL_OPTION;
        }

        return JOptionPane.showConfirmDialog(null, inputFields, title, optionPaneOption);
    }

    public String askInput(String title, String message, String defaultValue) {
        log.debug("SimpelUI(enabled={}):askInput:[{}]:{}", enabled, title, message);

        //
        if (enabled == false) {
            return null;
        }
        // Thanks to Swing's serialization, we can send Swing Components !
        JTextField textField = new JTextField(20);
        if (defaultValue != null) {
            textField.setText(defaultValue);
        }

        Object[] inputFields = {message, textField};
        int result = JOptionPane.showConfirmDialog(null, inputFields, title, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            return textField.getText();
        } else {
            return null;
        }
    }

    // Wrapper for JOptionPane.
    public int showOptionDialog(String title, Object message, int optionType, int messageType, Icon icon,
                                Object[] options, Object initialValue) {
        log.debug("SimpelUI(enabled={}):showOptionDialog:[{}]:{}", enabled, title, message);
        //
        if (enabled == false) {
            return CANCEL_OPTION;
        }
        return JOptionPane.showOptionDialog(null, message, title, optionType, messageType, icon, options, initialValue);
    }

    public boolean askAuthentication(String message, SecretHolder secret) {
        log.debug("SimpelUI(enabled={}):askAuthentication:{}", enabled, message);
        //
        if (enabled == false) {
            return false;
        }
        // Thanks to Swing's serialization, we can send Swing Components !
        JTextField passwordField = new JPasswordField(20);
        Object[] inputFields = {message, passwordField};
        int result = JOptionPane.showConfirmDialog(null, inputFields, "Authentication Required",
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            secret.value = Secret.wrap(passwordField.getText().toCharArray());
            return true;
        }

        return false;
    }

}
