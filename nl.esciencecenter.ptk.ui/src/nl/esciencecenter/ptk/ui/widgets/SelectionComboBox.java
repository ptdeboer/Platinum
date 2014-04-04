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

package nl.esciencecenter.ptk.ui.widgets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * ComboBox adaptor. Manages a default ComboBoxModel,. 
 */
public class SelectionComboBox<T> extends JComboBox<T>
{
    private static final long serialVersionUID = 407894204251955357L;
    
    boolean optionsEditable = false; // whether options are editable

    public SelectionComboBox()
    {
        super();
        init();
    }

    public SelectionComboBox(T[] vals)
    {
        super();
        setValues(vals);
    }

    private void init()
    {
        DefaultComboBoxModel<T> model = new DefaultComboBoxModel<T>();
        setModel(model);
    }

    public DefaultComboBoxModel<T> getModel()
    {
        return (DefaultComboBoxModel<T>) super.getModel();
    }

    public void setValues(T[] values)
    {
        if (values==null)
        {
            throw new NullPointerException("Cannot set null values. Please use empty array values[0] instead."); 
        }
        this.setModel(new DefaultComboBoxModel<T>(values));
    }

    public boolean hasValue(T val)
    {
        return (getModel().getIndexOf(val) >= 0);
    }

    public void addValue(T enumVal)
    {
        getModel().addElement(enumVal);
    }

    public void removeValue(T enumVal)
    {
        getModel().removeElement(enumVal);
    }

    public void setValue(T txt)
    {
        getModel().setSelectedItem(txt);
    }

    public String getName()
    {
        return super.getName();
    }

    public void updateFrom(T value)
    {
        this.setValue(value);
    }

    public void setEditable(boolean flag)
    {
        super.setEditable(flag);
    }

    /**
     * Selectable => drop down option is 'selectable'. optionsEditable = drop down selection entries are editable as
     * well !
     */
    public void setEditable(boolean selectable, boolean optionsEditable)
    {
        this.setEnabled(selectable);
        this.setEditable(optionsEditable);
    }

    
    public Object getSelectedItem()
    {
        return super.getSelectedItem();
    }
    
    public String getSelectedItemString()
    {
        Object obj = super.getSelectedItem();
        if (obj == null)
        {
            return null;
        }
        return obj.toString(); 
    }

    public void setSelectedItem(Object value)
    {
        if (value == null)
        {
            this.setEnabled(false);
            return;
        }

        // explicit convert to String.
        String strValue = value.toString();
        super.setSelectedItem(strValue);
    }
}
