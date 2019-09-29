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

import javax.swing.JCheckBox;

import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;

/**
 * Maps Boolean Attribute to checkbox field.
 */
public class AttrCheckBoxField extends JCheckBox implements IAttributeField {

    public AttrCheckBoxField() {
        super();
    }

    public AttrCheckBoxField(String name, boolean value) {
        super();
        this.setName(name);
        this.setSelected(value);
    }

    public String getViewerName() {
        return super.getName();
    }

    public String getValue() {
        return "" + this.isSelected();
    }

    public boolean getBooleanValue() {
        return this.isSelected();
    }

    public void updateFrom(Attribute attr) {
        this.setSelected(attr.getBooleanValue());
    }

    public AttributeType getVAttributeType() {
        return AttributeType.BOOLEAN;
    }

    public void setEditable(boolean flag) {
        this.setEnabled(flag);
    }

}
