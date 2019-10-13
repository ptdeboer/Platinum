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

import nl.esciencecenter.ptk.data.SecretHolder;

import javax.swing.*;

/**
 * UI Interface for UI Callbacks. This could be the VBrowser or another Application with an UI
 * interface. For example the VRSContext has an optional UI configured.
 */
public interface UI {

    int YES_OPTION = JOptionPane.YES_OPTION;

    int NO_OPTION = JOptionPane.NO_OPTION;

    int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;

    /**
     * Whether user interaction is possible. Before calling any of the UI method, check this method
     * first.
     *
     * @return false for robots and non-interactive scripts/applications, true for interactive
     * applications.
     */
    boolean isEnabled();

    /**
     * Display message dialog or message to print to console.
     */
    void showMessage(String title, String message, boolean modal);

    /**
     * Simple Yes/No prompter
     *
     * @return true for yes, false for no or <code>defaultValue</code> value to return if there is no
     * UI present or it is currently disabled.
     */
    boolean askYesNo(String title, String message, boolean defaultValue);

    /**
     * Simple OK/Cancel prompter.
     *
     * @return true for OK, false for cancel or defaultValue value to return if there is no UI
     * present or it is currently disabled.
     */
    boolean askOkCancel(String title, String message, boolean defaultValue);

    /**
     * Simple Yes/No/Cancel prompter. Returns JOptionPane.CANCEL_OPTION if no UI present
     *
     * @see javax.swing.JOptionPane
     */
    int askYesNoCancel(String title, String message);

    /**
     * Ask for password, passphrase or other 'secret' String
     */
    boolean askAuthentication(String message, SecretHolder secretHolder);

    /**
     * Ask for a single String input message, like for example a name or other value.
     *
     * @param title           - title to display
     * @param message         - Input message
     * @param optDefaultValue - option default value
     * @return String or null
     */
    String askInput(String title, String message, String optDefaultValue);

}
