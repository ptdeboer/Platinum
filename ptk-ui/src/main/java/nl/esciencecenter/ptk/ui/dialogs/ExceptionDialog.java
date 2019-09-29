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

package nl.esciencecenter.ptk.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Simple Exception Dialog Author P.T. de Boer
 */
public class ExceptionDialog extends javax.swing.JDialog implements ActionListener {
       public static void show(Component parent, Throwable ex) {
        show(parent, null, ex, false);
    }

    public static void show(final Component parent, final String title, final Throwable e,
            final boolean modal) {
        // allow for asynchronous call, but dialog MUST be shown during GUI thread ! 
        // invoke show dialog during GUI thread: 

        if (SwingUtilities.isEventDispatchThread() == false) {
            ////System.err.println("Calling swing invoke later"); 
            Runnable showtask = new Runnable() {
                public void run() {

                    //System.err.println("after swing invoke later"); 

                    show(parent, title, e, modal);
                }
            };

            SwingUtilities.invokeLater(showtask);
            return;
        }

        ExceptionDialog inst;

        // if model==true dialog parent must be set for advanced modality! 

        if (parent instanceof Dialog) {
            inst = new ExceptionDialog(((Dialog) parent), title, e, modal);
        } else if (parent instanceof Frame) {
            inst = new ExceptionDialog(((Frame) parent), title, e, modal);
        } else {
            inst = new ExceptionDialog((Frame) null, title, e, modal);
        }

        //GuiSettings.setToOptimalWindowSize(inst);

        //if (UIGlobal.isApplet()==false)
        inst.setAlwaysOnTop(true);

        inst.setModal(modal); // will halt after 'setVisible()'
        inst.setVisible(true);
        // next works only when not modal: 
        inst.requestFocus(); // first set focus to Dialog 
        inst.okButton.requestFocus(); // then set focus to OK 
    }

    // ========================================================================
    // 
    // ========================================================================

    private JTextArea errorText;
    private JPanel leftPane;
    private JTextArea debugTextArea;
    private JScrollPane debugScrollPane;
    private JTabbedPane mainTabPanel;
    private JTextField mainHeaderTextField;
    private JPanel errorPanel;
    private JLabel errorTextLabel;
    private JScrollPane errorScrollPane;
    private JPanel rightPane;
    private JPanel topPane;
    private JButton okButton;
    private JPanel buttonPanel;
    private String message = "No message";

    protected ExceptionDialog(Throwable e) {
        super();
        init(null, e);
    }

    public void setDebugText(String txt) {
        debugTextArea.setText(txt);
        debugTextArea.setSize(debugTextArea.getPreferredSize());
        // update viewport information:
    }

    public ExceptionDialog(Dialog parent, String title, Throwable e, boolean modal) {
        // for chained modal dailogs, parent can NOT be NULL 
        super(parent, title, modal);
        init(title, e);
        this.setLocationRelativeTo(parent);
    }

    public ExceptionDialog(Frame frame, String title, Throwable e, boolean modal) {
        // for chained modal dailogs, parent can NOT be NULL 
        super(frame, title, modal);
        init(title, e);
        this.setLocationRelativeTo(frame);
    }

    private void init(String title, Throwable e) {
        // WindowRegistry.register(this);
        initGUI();
        String name = title;
        if (name == null) {
            name = e.getClass().getSimpleName();
        }

        this.mainHeaderTextField.setText(name);
        String txt = e.getMessage();
        if (txt == null)
            txt = "[No Text]";

        // Turn on AUTO WRAPPING ! 
        this.errorText.setLineWrap(true);
        this.errorText.setWrapStyleWord(true);
        // Set of columns vs rows 

        this.errorText.setText(txt);
        //this.errorText.setColumns((int)(Math.round(Math.sqrt(txt.length())))); 

        // === Debug Text: 
        // get the whole bubs:
        setDebugText(getChainedStackTraceText(e));

        // update to the max!
        Dimension pref = this.errorText.getPreferredSize();

        // can happen when an error occures at init time, be robuust:

        //        if (UIGlobal.getGuiSettings()!=null) 
        //        {
        //           if (pref.width>UIGlobal.getGuiSettings().max_dialog_text_width)
        //              pref.width=UIGlobal.getGuiSettings().max_dialog_text_width;
        //        }
        //        
        //        this.errorText.setSize(pref);

        // update panels: 
        // this.errorPanel.setSize(this.errorPanel.getPreferredSize());

        // center in frame: 
        //this.setLocation(frame.getLocation().x+frame.getSize().width/2-this.getSize().width/2,
        //        frame.getLocation().y+frame.getSize().height/2-this.getSize().height/2);

        // GuiSettings.setToOptimalWindowSize(this); 

        //  show me 
        //this.setVisible(true);
    }

    private void initGUI() {
        try {
            BorderLayout thisLayout = new BorderLayout();
            this.getContentPane().setLayout(thisLayout);
            {
                topPane = new JPanel();
                this.getContentPane().add(topPane, BorderLayout.NORTH);
                //topPane.setMaximumSize(new java.awt.Dimension(8, 8));
                // topPane.setMinimumSize(new java.awt.Dimension(8, 8));
                //topPane.setPreferredSize(new java.awt.Dimension(282, 22));
                {
                    errorTextLabel = new JLabel();
                    topPane.add(errorTextLabel);
                    errorTextLabel.setText("Exception");
                }

            }
            {
                leftPane = new JPanel();
                this.getContentPane().add(leftPane, BorderLayout.WEST);
                //leftPane.setPreferredSize(new java.awt.Dimension(9, 64));
                //leftPane.setMaximumSize(new java.awt.Dimension(8, 8));
            }
            {
                rightPane = new JPanel();
                this.getContentPane().add(rightPane, BorderLayout.EAST);
                //rightPane.setMaximumSize(new java.awt.Dimension(8, 8));
                //rightPane.setMinimumSize(new java.awt.Dimension(8, 8));
                // rightPane.setPreferredSize(new java.awt.Dimension(10, 64));
            }
            {
                buttonPanel = new JPanel();
                this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
                //buttonPanel.setPreferredSize(new java.awt.Dimension(282, 26));
                {
                    okButton = new JButton();
                    buttonPanel.add(okButton);
                    okButton.setText("Close");
                    //okButton.setPreferredSize(new java.awt.Dimension(92, 18));
                    okButton.addActionListener(this);
                }
            }
            {
                mainTabPanel = new JTabbedPane();
                this.getContentPane().add(mainTabPanel);
                //mainTabPanel.setPreferredSize(new java.awt.Dimension(327, 131));
                {
                    errorPanel = new JPanel();
                    mainTabPanel.addTab("Error", null, errorPanel, null);
                    BorderLayout mainPanelLayout = new BorderLayout();
                    errorPanel.setLayout(mainPanelLayout);
                    //errorPanel.setPreferredSize(new java.awt.Dimension(303, 89));
                    errorPanel.setToolTipText("The error which occured");
                    {
                        mainHeaderTextField = new JTextField();
                        errorPanel.add(mainHeaderTextField, BorderLayout.NORTH);
                        mainHeaderTextField.setText("Text");
                        mainHeaderTextField.setEditable(false);
                        mainHeaderTextField.setFocusable(false);
                        mainHeaderTextField.setAutoscrolls(false);
                        mainHeaderTextField.setToolTipText("The Error which occured");
                        //mainHeaderTextField.setPreferredSize(new java.awt.Dimension(303, 24));
                        mainHeaderTextField.setFont(new java.awt.Font("Dialog", 1, 14));
                    }
                    {
                        errorScrollPane = new JScrollPane();
                        errorPanel.add(errorScrollPane, BorderLayout.CENTER);
                        errorScrollPane.setAutoscrolls(true);
                        errorScrollPane.getVerticalScrollBar().setAutoscrolls(true);
                        errorScrollPane.getHorizontalScrollBar().setAutoscrolls(true);
                        {
                            errorText = new JTextArea();
                            errorScrollPane.setViewportView(errorText);
                            errorText.setText(message);
                            //errorText.setSize(getPreferredSize()); 
                            errorText.setEditable(false);
                            errorText
                                    .setToolTipText("Extra information provided by the implementation.");
                            errorText.setWrapStyleWord(true);

                        }
                        {
                        }
                    }
                }
                {
                    debugScrollPane = new JScrollPane();
                    mainTabPanel.addTab("debug", null, debugScrollPane, null);
                    debugScrollPane.setToolTipText("Provides debug information");
                    {
                        debugTextArea = new JTextArea();
                        debugScrollPane.setViewportView(debugTextArea);
                        debugTextArea.setText("(No debug info)");
                        debugTextArea
                                .setToolTipText("Debug information provided by the exception.");
                        //debugTextArea.setPreferredSize(new java.awt.Dimension(296,42));
                    }
                }
            }

            this.setSize(600, 400);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {
        this.dispose();
    }

    /**
     * Return the stacktrace, including nested Exceptions ! as single String
     */
    public static String getChainedStackTraceText(Throwable e) {
        String text = "";

        Throwable parent = null;
        Throwable current = e;
        int index = 0;

        // === get whole exception chain:

        do {
            if (index > 0)
                text += "--- Nested Exception Caused By: ---\n";

            text += "Exception[" + index + "]:" + current.getClass().getName() + "\n";
            text += "message=" + current.getMessage() + "\n";

            StackTraceElement[] els = current.getStackTrace();

            if (els != null)
                for (int i = 0; i < els.length; i++)
                    text += "[" + i + "]" + els[i] + "\n";

            // get next in exception chain:
            parent = current;
            current = current.getCause();
            index++;
        } while ((current != null) && (current != parent) && (index < 100));

        return text;
    }

}
