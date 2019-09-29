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

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import nl.esciencecenter.vbrowser.vrs.data.AttributeType;

public class AttrIntField extends AttrParameterField {

    public AttrIntField() {
        super("<AttrIntField>");
        init();
    }

    public AttrIntField(String name, String value) {
        super();
        this.setName(name);
        this.setText(value);
        init();
    }

    public AttrIntField(String name, int dummyValue) {
        super();
        this.setName(name);
        this.setText("" + dummyValue);
        init();
    }

    protected void init() {
        setInputVerifier(new InputVerifier() {
            public boolean verify(JComponent input) {
                if (!(input instanceof AttrIntField))
                    return true; // give up focus
                return ((AttrIntField) input).isEditValid();
            }
        });
    }

    // Set text without checking !
    protected void setActualText(String txt) {
        super.setText(txt);
    }

    public boolean isInteger(String txt) {
        try {
            Integer.parseInt(txt);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    protected boolean isEditValid() {
        if (isInteger(getText()))
            return true;

        int val = JOptionPane.showConfirmDialog(this.getRootPane(),
                "Value is not an valid integer", "Invalid Integer", JOptionPane.OK_CANCEL_OPTION);
        if (val == JOptionPane.CANCEL_OPTION) {
            this.setActualText("0");
            return true; // reset
        } else {
            return false; // try again
        }
    }

    public AttributeType getVAttributeType() {
        return AttributeType.INT;
    }

    public void setText(String txt) {
        try {
            int i = Integer.parseInt(txt);
            setValue(i);
        } catch (NumberFormatException e) {
            setActualText("");
        }
    }

    public void setValue(int val) {
        setActualText("" + val);
    }

    public int getIntValue() {
        try {
            int i = Integer.parseInt(getText());
            return i;
        } catch (NumberFormatException e) {
            setActualText("");
        }
        return -1; // N/A
    }

}
