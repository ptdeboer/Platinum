/*
 * Copyright 2006-2010 Virtual Laboratory for e-Science (www.vl-e.nl)
 * Copyright 2012-2013 Netherlands eScience Center.
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

package nl.esciencecenter.ptk.vbrowser.ui.attribute;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import nl.esciencecenter.ptk.ui.fonts.FontUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.UIGlobal;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.data.AttributeUtil;

public class AttributeEditorForm extends JDialog
{
    private static final long serialVersionUID = 9136623460001660679L;
    
    private static final ClassLogger logger=ClassLogger.getLogger(AttributeEditorForm.class);

    // ---
    // package protected 
    // ---
    protected AttributePanel infoPanel;
    protected JButton cancelButton;
    protected JButton okButton;
    protected JButton resetButton;

    // ui fields
    private JTextField topLabelTextField;
    private JPanel buttonPanel;
    private AttributeEditorController formController;
    private String titleName;
    private boolean isEditable;

    // ---
    // Copy of previous attributes ! 
    // --- 
    protected Attribute[] originalAttributes;

    private void initGUI(Attribute attrs[])
    {
        try
        {
            this.setTitle(this.titleName);

            BorderLayout thisLayout = new BorderLayout();
            this.getContentPane().setLayout(thisLayout);
            Container rootContainer = this.getContentPane();

            {
                topLabelTextField = new JTextField();
                rootContainer.add(topLabelTextField, BorderLayout.NORTH);

                topLabelTextField.setText(this.titleName);
                topLabelTextField.setEditable(false);
                topLabelTextField.setFocusable(false);
                topLabelTextField.setBorder(BorderFactory.createEtchedBorder(BevelBorder.RAISED));

                topLabelTextField.setFont(FontUtil.createFont("dialog"));
                topLabelTextField.setHorizontalAlignment(SwingConstants.CENTER);
                topLabelTextField.setName("huh");
            }
            {
                infoPanel = new AttributePanel(attrs, isEditable);
                rootContainer.add(infoPanel, BorderLayout.CENTER);
            }
            {
                buttonPanel = new JPanel();
                rootContainer.add(buttonPanel, BorderLayout.SOUTH);
                {
                    okButton = new JButton();
                    buttonPanel.add(okButton);
                    okButton.setText("Accept");
                    okButton.addActionListener(formController);
                    okButton.setEnabled(this.isEditable);
                }
                {
                    resetButton = new JButton();
                    buttonPanel.add(resetButton);
                    resetButton.setText("Reset");
                    resetButton.addActionListener(formController);
                }
                {
                    cancelButton = new JButton();
                    buttonPanel.add(cancelButton);
                    cancelButton.setText("Cancel");
                    cancelButton.addActionListener(formController);
                }
            }
            // enforce new size. 
            forceSetSizeToPreferred(); 
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // ==========================================================================
    // Constructor
    // ==========================================================================

    private void forceSetSizeToPreferred()
    {
        validate();
        Dimension size = this.getPreferredSize();
        // bug in FormLayout ? Last row is not shown
        // add extra space:
        size.height += 32;
        size.width += 128; // make extra wide
        setSize(size);
    }

    private void init(String titleName, Attribute attrs[])
    {
        this.titleName = titleName;

        attrs = AttributeUtil.duplicateArray(attrs); // use duplicate to edit;

        // Must first create ActionListener since it is used in initGui...
        this.formController = new AttributeEditorController(this);
        this.addWindowListener(formController);

        // only set to editable if there exists at least one editable attribute
        this.isEditable = false;

        for (Attribute attr : attrs)
        {
            if ((attr != null) && (attr.isEditable() == true))
            {
                this.isEditable = true;
            }
        }
        
        initGUI(attrs);
        formController.update();

    }

    public AttributeEditorForm(String titleName, Attribute attrs[])
    {
        super();
        init(titleName, attrs);
    }

    public AttributeEditorForm()
    {
       super(); 
    }

    // ==========================================================================
    //
    // ==========================================================================

    public void setAttributes(Attribute[] attributes)
    {
        this.originalAttributes=attributes; 
        // use duplicate to edit:
        Attribute[] dupAttributes = AttributeUtil.duplicateArray(attributes);
        this.infoPanel.setAttributes(new AttributeSet(dupAttributes), true);

        revalidate();
    }

    public synchronized void Exit()
    {
        // notify waiting threads: waitForDialog().
        this.notifyAll();
        dispose();
    }

    public void dispose()
    {   
        super.dispose();
    }

    public boolean hasChangedAttributes()
    {
        return this.infoPanel.hasChangedAttributes();
    }
    
    // ==========================================================================
    // main
    // ==========================================================================

    /**
     * Static method to interactively ask user for attribute settings.
     */
    public static Attribute[] editAttributes(final String titleName, final Attribute[] attrs,
            final boolean returnChangedAttributesOnly)
    {
        final AttributeEditorForm dialog = new AttributeEditorForm();

        Runnable formTask = new Runnable()
        {
            public void run()
            {
                // perform init during GUI thread
                dialog.init(titleName, attrs);

                // is now in constructor: dialog.setEditable(true);
                // modal=true => after setVisible, dialog will not return until windows closed
                dialog.setModal(true);
                dialog.setAlwaysOnTop(true);
                dialog.setVisible(true);

                synchronized (this)
                {
                    this.notifyAll();
                }
            }
        };

        // Run during gui thread of use Swing invokeLater()
        if (UIGlobal.isGuiThread())
        {
            formTask.run(); // run directly:
        }
        else
        {
            // go background:
            SwingUtilities.invokeLater(formTask);

            synchronized (formTask)
            {
                try
                {
                    formTask.wait();
                }
                catch (InterruptedException e)
                {
                    logger.logException(ClassLogger.ERROR, e, "--- Interupted ---\n");
                }
            }
        }

        // wait
        if (dialog.formController.isOk == true)
        {
            // boolean update=dialog.hasChangedAttributes();
            if (returnChangedAttributesOnly)
            {
                return dialog.infoPanel.getChangedAttributes();
            }
            else
            {
                return dialog.infoPanel.getAttributes();
            }
        }
        else
        {
            // no attributes. 
            return null;
        }
    }


}
