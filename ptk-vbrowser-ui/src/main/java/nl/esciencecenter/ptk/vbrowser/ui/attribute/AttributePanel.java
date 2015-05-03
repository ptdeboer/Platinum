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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import nl.esciencecenter.ptk.ui.fonts.FontUtil;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.ptk.vbrowser.ui.attribute.fields.AttrEnumField;
import nl.esciencecenter.ptk.vbrowser.ui.attribute.fields.AttrIntField;
import nl.esciencecenter.ptk.vbrowser.ui.attribute.fields.AttrParameterField;
import nl.esciencecenter.ptk.vbrowser.ui.attribute.fields.AttrPortField;
import nl.esciencecenter.ptk.vbrowser.ui.properties.UIProperties;
import nl.esciencecenter.ptk.vbrowser.ui.properties.UIPropertyNames;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Attribute Panel.
 */
public class AttributePanel extends JPanel
{
    private static final PLogger logger=PLogger.getLogger(AttributePanel.class); 
        
    private static final long serialVersionUID = -7067255541137647977L;

    private static final int default_row_gap = 4;
    private static final int default_column_gap = 8;
    private static final int default_border_width = 5;
    private static final int default_border_height = 5;
    private static final int default_min_field_width = 100;
    private static final int default_max_field_width = 300;

    /**
     * Attribute Set. Edit operations on this set should reflect Panel and vice versa !
     */
    protected AttributeSet attributes = null;

    private boolean isEditable = false;

    private boolean hasChangedAttributes = false;

    private int maxFieldWidth;

    private JComponent[] jFields;

    private JLabel[] jLabels;

    private boolean useFormLayout = true;

    private int rowOffset;

    private FormLayout formLayout;

    private Vector<AttributePanelListener> listeners = new Vector<AttributePanelListener>();

    public void initGui()
    {
        formLayout = new FormLayout(
                "10px,right:pref:grow, 5px, fill:pref:grow,10px",
                "10px,center:pref:grow, max(p;5px),max(p;5px), max(p;5px), center:pref:grow,10px");

        this.setLayout(formLayout);
    }

    /**
     * Default constructor.
     */
    public AttributePanel()
    {
        // super();
        Attribute attrs[] = new Attribute[4];
        String options[] = { "Option1", "Option2" };

        attrs[0] = new Attribute("textAttribute", "text");
        attrs[1] = new Attribute("booleanAttribute", true);
        attrs[2] = new Attribute("numberAttribute", 1);
        attrs[3] = new Attribute("selectAttribute", options, 0);
        initGui();

        setAttributes(new AttributeSet(attrs));
    }

    private void init(AttributeSet set, boolean editable)
    {
        this.isEditable = editable;
        initGui();

        if (set == null)
        {
            // allowed: means creat empty window, attribute will follow !
            return;
        }
        
        setAttributes(set, editable);
    }

    public AttributePanel(Attribute attrs[])
    {
        super();
        init(new AttributeSet(attrs), isEditable);
    }

    public AttributePanel(Attribute attrs[], boolean isEditable)
    {
        super();
        init(new AttributeSet(attrs), isEditable);
    }

    public AttributePanel(AttributeSet set, boolean isEditable)
    {
        super();
        init(set, isEditable);
    }

    public AttributePanel(AttributeSet set)
    {
        super();
        init(set, true);
    }
    
    public void setAttributes(AttributeSet attributeSet)
    {
        setAttributes(attributeSet, this.isEditable);
    }


    /**
     * Creates labels & fields. 
     * @param attributeSet Attributes to edit. 
     * @param editable whether to view (false) or edit (true) these attributes. 
     */
    public void setAttributes(AttributeSet orgSet, boolean editable)
    {
        this.isEditable = editable;

        removeAll();

        if (orgSet == null)
        {
            validate();
            repaint();
            return;
        }

        int len = 0;
        len = orgSet.size();
        // else this method will remove old attributes

        attributes = new AttributeSet();

        // first make left column of fields,
        // next make right column of values
        // do the layout myself:

        int xpos = default_border_width;
        int ypos = default_border_height;

        int ypositions[] = new int[len];
        int maxx = 0;
        int maxy = 0;

        jLabels = new JLabel[len];

        for (int i = 0; i < len; i++)
        {
            // filter out null Attributes !
            if (orgSet.elementAt(i) != null)
            {
                /*
                 * if ( (editable==false) ||(editable & orgAttrs[i].isEditable()) )
                 */
                Attribute attr = orgSet.elementAt(i);
                attributes.put(attr);
            }
        }

        // new attribute size
        len = attributes.size();

        // changed.setSize(len);
        // update form layout columns nr:
        setFormRows(attributes.size());

        // Field Names
        for (int i = 0; i < len; i++)
        {
            Attribute attr = attributes.elementAt(i);
            // debug("Adding attribute:"+attr);

            String name = attr.getName();
            String type = attr.getType().toString();

            JLabel label = new JLabel(name);
            label.setToolTipText("type:" + type + ",name:" + name);

            ypositions[i] = ypos;

            if (useFormLayout)
            {
                if (Attribute.isSectionName(name))
                    add(label, new CellConstraints(
                            "2," + (i + rowOffset) + ",1, 1, default, default"));
                else
                    add(label, new CellConstraints(
                            "2," + (i + rowOffset) + ",1, 1, default, default"));
            }
            else
            {
                label.setLocation(xpos, ypos);

                // since I am doing the layout I have to set my childrens size
                // also !
                label.setSize(label.getPreferredSize());
                this.add(label);
            }

            ypos += label.getSize().getHeight() + default_row_gap;

            if ((xpos + label.getSize().width) > maxx)
                maxx = xpos + label.getSize().width;

            if ((ypos + label.getSize().height) > maxy)
                maxy = ypos + label.getSize().height;

        }

        xpos = maxx + default_column_gap;

        ypos = 10;
        UIProperties guiSettings = new UIProperties(); // defaults !
        Color editBGC = guiSettings.getColor(UIPropertyNames.TEXTFIELD_EDITABLE_BG_COLOR, Color.WHITE);
        Color editFGC = guiSettings.getColor(UIPropertyNames.TEXTFIELD_EDITABLE_FG_COLOR, Color.BLACK);
        Color nonEditBGC = guiSettings.getColor(UIPropertyNames.TEXTFIELD_NON_EDITABLE_BG_COLOR, Color.LIGHT_GRAY);
        Color nonEditFGC = guiSettings.getColor(UIPropertyNames.TEXTFIELD_NON_EDITABLE_BG_COLOR, Color.BLACK);

        // === Create Fields ===
        jFields = new JComponent[len];

        for (int i = 0; i < len; i++)
        {
            Attribute attr = attributes.elementAt(i);
            String value = attr.getStringValue();
            String name = attr.getName();
            AttributeType type = attr.getType();
            boolean isSection = Attribute.isSectionName(name);

            if (isSection)
                continue;

            JComponent jField = null;

            // create field listener for this attribute field
            AttributeFieldListener attributeListener = new AttributeFieldListener(name);
            // only create combo box for editable attributes:

            if ((attr.isEditable())
                    && (this.isEditable)
                    && ((type == AttributeType.ENUM)
                    || (type == AttributeType.BOOLEAN)
                    ))
            {
                String vals[] = attr.getEnumValues();

                if (vals == null)
                {
                    logger.errorPrintf("Error no enumvalues for attribute:%s\n", attr.getName());
                    vals = new String[1];
                    vals[0] = "<NULL>";
                }

                AttrEnumField enumField = new AttrEnumField();
                enumField.setValues(vals);

                enumField.setSelectedIndex(attr.getEnumIndex());

                jField = enumField;

                enumField.setFont(FontUtil.createFont("dialog"));
                enumField.setEditable(this.isEditable && attr.isEditable());
                enumField.setActionCommand(name);
                enumField.addActionListener(attributeListener);
                enumField.addMouseListener(attributeListener);
            }
            else if ((attr.isEditable())
                    && (this.isEditable)
                    && (StringUtil.equals(name, "port")
                    ))
            {
                JTextField textField = new AttrPortField(name, value);
                textField.addActionListener(attributeListener);
                textField.addMouseListener(attributeListener);
                textField.setActionCommand(name);
                textField.setEditable(this.isEditable && attr.isEditable());

                // todo: color profiles:
                if (textField.isEditable())
                {
                    textField.setBackground(editBGC);
                    textField.setForeground(editFGC);

                }
                else
                {
                    textField.setBackground(nonEditBGC);
                    textField.setForeground(nonEditFGC);
                }

                jField = textField;
            }

            else if ((attr.isEditable())
                    && (this.isEditable)
                    && ((type == AttributeType.INT)
                    || (type == AttributeType.LONG)
                    ))
            {
                JTextField textField = new AttrIntField(value, attr.getIntValue());
                textField.addActionListener(attributeListener);
                textField.addMouseListener(attributeListener);
                textField.setActionCommand(name);
                textField.setEditable(this.isEditable && attr.isEditable());

                if (textField.isEditable())
                {
                    textField.setBackground(editBGC);
                    textField.setForeground(editFGC);

                }
                else
                {
                    textField.setBackground(nonEditBGC);
                    textField.setForeground(nonEditFGC);
                }

                jField = textField;
            }
            else
            {
                JTextField textField = new AttrParameterField(value);
                textField.addActionListener(attributeListener);
                textField.addMouseListener(attributeListener);
                textField.setActionCommand(name);
                textField.setEditable(this.isEditable && attr.isEditable());

                if (textField.isEditable())
                {
                    textField.setBackground(editBGC);
                    textField.setForeground(editFGC);

                }
                else
                {
                    textField.setBackground(nonEditBGC);
                    textField.setForeground(nonEditFGC);
                }

                jField = textField;
            }
            //
            // Set optional tooltiptest if attribute specifies 'mini' help text
            // !
            //
            // String text=attr.getHelpText();
            // if (text!=null)
            // jField.setToolTipText(text);

            jField.addFocusListener(attributeListener);
            ypos = ypositions[i];
            // jField.setBackground(GuiSettings.current.textfield_non_editable_background_color);
            // since I am doing the layout I have to set my childrens size also
            // !

            if (useFormLayout)
            {
                add(jField, new CellConstraints(
                        "4," + (i + rowOffset) + ",1, 1, default, default"));
            }
            else
            {
                jField.setLocation(xpos, ypos);
                jField.setSize(jField.getPreferredSize());
                // since I am doing the layout I have to set my childrens size
                // also !
                jField.setSize(jField.getPreferredSize());
                this.add(jField);
            }

            if (jField.getSize().width > maxFieldWidth)
                maxFieldWidth = jField.getSize().width;

            jFields[i] = jField;

            if ((xpos + jField.getSize().width) > maxx)
                maxx = xpos + jField.getSize().width;

            if ((ypos + jField.getSize().height) > maxy)
                maxy = ypos + jField.getSize().height;
        }

        // if (maxFieldWidth>this.default_max_field_width)
        // maxFieldWidth=default_max_field_width;

        // update fields width to maximum field width
        if (useFormLayout == false)
            for (int i = 0; i < len; i++)
            {
                // null fields due to null attributes
                Dimension size = jFields[i].getSize();

                if (size.width < default_min_field_width)
                {
                    size.width = default_min_field_width;
                }

                if ((jFields[i].getLocation().x + size.width) > maxx)
                    maxx = jFields[i].getLocation().x + size.width;

                // if (size.width>default_max_field_width)
                // size.width=default_max_field_width;

                jFields[i].setSize(size);

            }

        // UIGlobal.debugPrintln("AttributePanel","maxx,maxy="+maxx+","+maxy);

        // add border size

        maxx += default_border_width;
        maxy += default_border_height;

        if (maxx >= 800)
            maxx = 800;

        if (useFormLayout)
        {
            this.doLayout();
            Dimension prefSize = this.getPreferredSize();

            // debug("Pre validate: preferred LayoutSize="+this.getLayout().preferredLayoutSize(this));
            // debug("Pre validate: preferred size="+getPreferredSize());
            this.validate();

            // debug("Post validate: preferred Panel LayoutSize="+this.getLayout().preferredLayoutSize(this));
            // debug("Post validate: preferred Panel size="+getPreferredSize());
            this.setPreferredSize(this.getLayout().preferredLayoutSize(this));
            // if (prefSize.width>800)
            // prefSize.width=800;

            // this.setSize(prefSize);
            // this.setMaximumSize(prefSize);
        }
        else
        {
            // this.setSize(maxx,maxy);
            // this.setPreferredSize(getSize());
        }
        // for some reason the FormLayout doesn't update the JPanel's
        // preferredsize:

        // debug("preferred size="+getPreferredSize());
        // debug("preferred LayoutSize="+this.getLayout().preferredLayoutSize(this));

        // 
        this.revalidate();
        // repaint later!:
        this.repaint();
    }


    private void setFormRows(int rows)
    {
        // let upper/lower borders grow:
        String defStr = "8px,center:pref:grow,";

        rowOffset = 3;// first 2 rows are fillers, so 3rd is the first!

        // nr of rows:
        for (int i = 0; i < rows + 1; i++)
        {
            // rows have a minimal 8px in heights
            defStr += "max(p;8px),";
            // defStr+="100px,";
        }

        // let upper/lower borders grow, but must have minimal 8 pixels
        defStr += "center:pref:grow,8px";

        // 4 columns: 2 border and 2 'middle'

        FormLayout formLayout = new FormLayout(
                // "10px,right:pref:none, 5px, fill:pref:grow,10px",
                "10px,right:pref:none, 5px, fill:min(800px;p):grow,10px",
                defStr);

        this.setLayout(formLayout);
    }

    /**
     * Enables all editable attributes (VAttribute.isEditable() still must return TRUE
     * 
     * @param val
     */
    /*
     * public void setEditable(boolean val) { this.isEditable=val;
     * 
     * if (jFields==null) return; // not initialized yet
     * 
     * for (int i=0;i<jFields.length;i++) { if ((jFields[i]!=null) && (attributes.elementAt(i).isEditable())) { if
     * (jFields[i] instanceof JTextField) { ((JTextField)jFields[i]).setEditable(val); } jFields[i].setBackground
     * (GuiSettings.current.textfield_editable_background_color); } } }
     */

    public class AttributeFieldListener implements ActionListener, FocusListener, MouseListener
    {
        // the attribute name I am listener for:
        String attributeName = null;

        public AttributeFieldListener(String attrname)
        {
            attributeName = attrname;
        }

        public void actionPerformed(ActionEvent e)
        {
            updateComp(e.getSource());
        }

        public void focusGained(FocusEvent e)
        {
            // Global.debugPrintln("FieldListener","focusGained:"+e);
        }

        public void focusLost(FocusEvent e)
        {
            updateComp(e.getSource());
        }

        private void updateComp(Object comp)
        {
            // Global.debugPrintln(this,"updateComp:"+comp);

            String value = null;

            if (comp instanceof JTextField)
            {
                JTextField field = (JTextField) comp;
                value = field.getText();
            }
            else if (comp instanceof JComboBox)
            {
                Object val = ((JComboBox) comp).getSelectedItem();
                value = (String) val;
            }
            else
            {
                // Global.errorPrintln(this,"focusLost:unknown component"+comp);
                return;
            }

            // ignore event on non editable attributes
            if (isEditable(attributeName))
                setAttribute(attributeName, value);
        }

        public void mouseClicked(MouseEvent e)
        {
            JComponent source = (JComponent) e.getSource();

            if (e.getClickCount() == 2)
            {
                Attribute attr = getAttribute(this.attributeName);
                Attribute newattr = AttributeViewer.editAttribute(attr, attr.isEditable());

                // Global.debugPrintln(this,"New attribute="+newattr);

                if ((newattr != null) && (source instanceof JTextField))
                {
                    String valtxt = newattr.getStringValue();
                    ((JTextField) source).setText(valtxt);
                    setAttribute(attributeName, valtxt);

                }

                /*
                 * if (newattr!=null) setAttribute(newattr.getName(),newattr.getValue());
                 */
            }

        }

        public void mouseEntered(MouseEvent e)
        {
            // highlight ? ((JComponent)e.getSource()).requestFocusInWindow();
        }

        public void mouseExited(MouseEvent e)
        {
        }

        public void mousePressed(MouseEvent e)
        {
        }

        public void mouseReleased(MouseEvent e)
        {
        }
    }

    public boolean isEditable(String name)
    {
        if (name == null)
            return false;

        if (attributes.containsKey(name))
            return attributes.get(name).isEditable();

        return false;
    }

    /** Update Attribute in AttributeSet */
    public void setAttribute(String name, String value)
    {
        // UIGlobal.debugPrintf(this,"setAttribute %s=%s\n",name,value);

        if (name == null)
        {
            // Global.warnPrintf(this,"setAttribute: NULL name!\n");
            return;
        }

        if (attributes.containsKey(name))
        {
            String oldValue = null;

            // check for new value:
            oldValue = attributes.getStringValue(name);

            if ((oldValue != null) && (oldValue.compareTo(value) != 0))
            {
                attributes.set(name, value);
                // really a new value !
                // filter out focus and other event which do not
                // change the value
                this.hasChangedAttributes = true;
                notifyAttributeChangeEvent(name);
            }
        }
        else
        {
            logger.debugPrintf("Attribute not found:%s\n", name);
        }

    }

    private void notifyAttributeChangeEvent(String name)
    {
        for (AttributePanelListener listener : listeners)
        {
            Attribute attr = attributes.get(name);
            listener.notifyAttributeChanged(attr);
        }
    }

    public boolean hasChangedAttributes()
    {
        return this.hasChangedAttributes;
    }

    /**
     * return updated attributes. null Attribute are filtered out !
     */
    public Attribute[] getAttributes()
    {
        if (attributes == null)
            return null;

        return attributes.toArray(new Attribute[]
        {});
    }

    public Attribute getAttribute(String name)
    {
        if (attributes == null)
            return null;

        return attributes.get(name);
    }

    /** Modal show editor Panel */
    public static Attribute[] showEditor(AttributeSet attrs)
    {
        JDialog dialog = new JDialog();
        Attribute newAttrs[] = null;

        AttributePanel panel = new AttributePanel(attrs);

        dialog.add(panel);
        dialog.setModal(true);
        dialog.setVisible(true);
        newAttrs = panel.getAttributes();

        return newAttrs;
    }

    /**
     * Method to call when attributes are updated outside the main GUI event thread. Uses SwingUtilities.invokeLater()
     * to update the specified attributes.
     * 
     * @param attrs
     */
    public void asyncSetAttributes(final AttributeSet set)
    {
        final AttributePanel apanel = this;

        Runnable runT = new Runnable()
        {
            public void run()
            {
                apanel.setAttributes(set);
            }
        };

        SwingUtilities.invokeLater(runT);

    }

    public Attribute[] getChangedAttributes()
    {
        return this.attributes.getChangedAttributesArray();
    }

    public static void showEditor(Attribute attrs[])
    {
        showEditor(new AttributeSet(attrs));
    }

    public void addAttributeListener(AttributePanelListener listener)
    {
        synchronized (listeners)
        {
            listeners.add(listener);
        }
    }
}
