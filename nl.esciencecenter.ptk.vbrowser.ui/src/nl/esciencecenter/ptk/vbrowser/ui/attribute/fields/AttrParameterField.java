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

package nl.esciencecenter.ptk.vbrowser.ui.attribute.fields;

import javax.swing.JTextField;

import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;

/**
 * An Attribute Parameter Field is an managed Field for a (VRS) Attribute.
 */
public class AttrParameterField extends JTextField implements IAttributeField
{
    public static enum AttrFieldType
    {
        INT,STRING,STRINGENUM,VRL,BOOLEAN,CHECKBOX;
    }
    
    private static final long serialVersionUID = 8089690166449438100L;

    public AttrParameterField()
    {
        // names indicates abtract class, should be subclassed. 
        super("<AttrParameterField>");
    }

    /** Default is String Parameter */
    public AttrParameterField(String value)
    {
        super(value);
    }

    public AttrParameterField(String name, String value)
    {
        super(value);
        this.setName(name);
    }

    public String getViewerName()
    {
        return super.getName();
    }

    public String getValue()
    {
        return super.getText();
    }

    public AttributeType getVAttributeType()
    {
        return AttributeType.STRING;
    }

    public void updateFrom(Attribute attr)
    {
        setText(attr.getStringValue());
    }

    public void setEditable(boolean editable)
    {
        super.setEditable(editable);
    }

}
