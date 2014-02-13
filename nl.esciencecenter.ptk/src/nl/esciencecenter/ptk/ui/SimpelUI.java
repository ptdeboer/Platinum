/*
 * Copyrighted 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").  
 * You may not use this file except in compliance with the License. 
 * For details, see the LICENCE.txt file location in the root directory of this 
 * distribution or obtain the Apache License at the following location: 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
 * ---
 */
// source: 

package nl.esciencecenter.ptk.ui;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import nl.esciencecenter.ptk.crypt.Secret;
import nl.esciencecenter.ptk.data.SecretHolder;
import nl.esciencecenter.ptk.util.logging.ClassLogger;

/**
 * Simple UI Object.
 */
public class SimpelUI implements UI
{
    private static ClassLogger logger;

    static
    {
        logger = ClassLogger.getLogger(SimpelUI.class);
    }

    private boolean enabled = true;

    public SimpelUI()
    {
    }

    public void setEnabled(boolean enable)
    {
        this.enabled = enable;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void showMessage(String title, String message, boolean modal)
    {
        if (enabled == false)
        {
            logger.infoPrintf("showMessage(): UI disabled, message=%s\n", message);
            return;
        }

        JOptionPane.showMessageDialog(null, message);
    }

    public boolean askYesNo(String title, String message, boolean defaultValue)
    {
        if (enabled == false)
        {
            logger.infoPrintf("askYesNo(): UI disabled, [title]:message=[%s]:%s\n", title,message);
            return defaultValue;
        }

        int result = JOptionPane.showOptionDialog(null, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                null, null, JOptionPane.NO_OPTION);

        return (result == JOptionPane.YES_OPTION);
    }

    @Override
    public boolean askOkCancel(String title, String message, boolean defaultValue)
    {
        if (enabled == false)
        {
            logger.infoPrintf("askOkCancel(): UI disabled, [title]:message=[%s]:%s\n", title,message);
            return defaultValue;
        }

        int result = JOptionPane.showOptionDialog(null, message, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
                null, null, JOptionPane.NO_OPTION);

        return (result == JOptionPane.OK_OPTION);
    }

    // @return JOptionPane.YES_OPTION JOptionPane.NO_OPTION or
    // JOptionPane.CANCEL_OPTION
    public int askYesNoCancel(String title, String message)
    {
        if (enabled == false) 
        { 
            logger.infoPrintf("askYesNoCancel(): UI disabled, [title]:message=[%s]:%s\n", title,message);
            return JOptionPane.CANCEL_OPTION;
        }
        
        int result = JOptionPane.showOptionDialog(null, message, title,
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
                null, null, JOptionPane.CANCEL_OPTION);

        return result;
    }

    public int askInput(String title, Object[] inputFields, int optionPaneOption)
    {
        if (enabled == false)
        {
            logger.infoPrintf("askInput(): UI disabled, [title]=[%s]\n", title);
            return CANCEL_OPTION;
        }

        return JOptionPane.showConfirmDialog(null,
                inputFields,
                title,
                optionPaneOption);
    }

    public String askInput(String title, String message)
    {
        if (enabled == false)
        {
            logger.infoPrintf("askInput(): UI disabled, [title]:message=[%s]:%s\n", title,message);
            return null;
        }

        // Thanks to Swing's serialization, we can send Swing Components !
        JTextField textField = new JTextField(20);
        Object[] inputFields =
        { message, textField };

        int result = JOptionPane.showConfirmDialog(null,
                inputFields,
                title,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.OK_OPTION)
        {
            return textField.getText();
        }
        else
        {
            return null;
        }
    }

    // Wrapper for JOptionPane.
    public int showOptionDialog(String title,
            Object message,
            int optionType,
            int messageType,
            Icon icon,
            Object[] options,
            Object initialValue)
    {
        if (enabled == false)
        {
            logger.infoPrintf("showOptionDialog(): UI disabled, [title]:message=[%s]:%s\n", title,message);
            return CANCEL_OPTION;
        }

        return JOptionPane.showOptionDialog(null,
                message,
                title,
                optionType,
                messageType,
                icon,
                options,
                initialValue);
    }

    public boolean askAuthentication(String message,
            SecretHolder secret)
    {
        if (enabled == false)
        {
            return false;
        }

        // Thanks to Swing's serialization, we can send Swing Components !
        JTextField passwordField = new JPasswordField(20);
        Object[] inputFields =
        { message, passwordField };

        int result = JOptionPane.showConfirmDialog(null,
                inputFields,
                "Authentication Required",
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.OK_OPTION)
        {
            secret.value = Secret.wrap(passwordField.getText().toCharArray());
            return true;
        }

        return false;
    }

}
