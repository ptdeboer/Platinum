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

import javax.swing.JOptionPane;

import nl.esciencecenter.ptk.data.SecretHolder;

/**
 * UI Interface for UI Callbacks. 
 */
public interface UI
{
    public static int YES_OPTION = JOptionPane.YES_OPTION;
    
    public static int NO_OPTION = JOptionPane.NO_OPTION;
    
    public static int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;
    
    /**
     * Whether user interaction is possible. 
     * @return false for robots and non-interactive scripts/applications, true for interactive applications.  
     */ 
    public boolean isEnabled();
    
    /** 
     * Display message dialog or message to print to console.  
     */ 
    public void showMessage(String title,String message,boolean modal);

     /**
     * Simple Yes/No prompter 
     * @param true for yes, false for no or <code>defaultValue</code> value to return if there is no UI present 
     *        or it is currently disabled. 
     */ 
    public boolean askYesNo(String title,String message, boolean defaultValue);

    /**
     * Simple OK/Cancel prompter.  
     * @param true for OK, false for cancel or defaultValue value to return if there is no UI present 
     *        or it is currently disabled. 
     */ 
    public boolean askOkCancel(String title,String message, boolean defaultValue);

    /**
     * Simple Yes/No/Cancel prompter. 
     * Returns JOptionPane.CANCEL_OPTION if no UI present
     * @see JOptionPane for return values 
     */ 
    public int askYesNoCancel(String title,String message);

    /** 
     * Ask for password, passphrase or other 'secret' String 
     */ 
    public boolean askAuthentication(String message, SecretHolder secretHolder);
    
    /**
     * Ask for a single String input message, like for example a name or other value. 
     * @param title - title to display
     * @param message - Input message 
     * @param optDefaultValue - option default value 
     * @return String or null 
     */
    public String askInput(String title, String message, String optDefaultValue); 
    
//    /**
//     * Simple formatted Input Dialog. Method is wrapper for JOptionPane ! 
//     * See  JOptionPane.showConfirmDialog() for options.
//     * 
//     * @return JOptionPane.OK_OPTION if successful. 
//     *         Parameter inputFields can contain modified (Swing) objects.  
//     */ 
//    public int askInput(String title, Object[] inputFields, int jOptionPaneOption);
    
}
