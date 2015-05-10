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

package nl.esciencecenter.ptk.vbrowser.ui.resourcetable;

import java.util.List;

import javax.swing.AbstractListModel;
import nl.esciencecenter.ptk.data.StringList;

public class HeaderModel extends AbstractListModel<String> {
    private static final long serialVersionUID = -4513306632211174045L;

    private StringList values;

    private boolean isEditable = true;

    public HeaderModel() {
        init(new StringList()); // empty list
    }

    public HeaderModel(String values[]) {
        init(new StringList(values));
    }

    @Override
    public String getElementAt(int index) {
        return values.get(index);
    }

    private void init(StringList stringList) {
        this.values = stringList;
        System.err.printf(values.toString(">>>", ",", "\n"));
    }

    @Override
    public int getSize() {
        return values.size();
    }

    public void setValues(String vals[]) {
        values = new StringList(vals);
        this.fireContentsChanged(this, 0, values.size() - 1);
    }

    public void setValues(List<String> vals) {
        values = new StringList(vals);
        this.fireContentsChanged(this, 0, values.size() - 1);
    }

    /**
     * @return copy of headers as array.
     */
    public String[] toArray() {
        return this.values.toArray();
    }

    public int indexOf(String name) {
        return this.values.indexOf(name);
    }

    /**
     * Inserts newHeader after 'header' of before 'header'. Fires intervalAdded event
     */
    public int insertHeader(String header, String newHeader, boolean insertBefore) {
        int index = -1;

        if (insertBefore)
            index = this.values.insertBefore(header, newHeader);
        else
            index = this.values.insertAfter(header, newHeader);

        this.fireIntervalAdded(this, index, index);

        return index;
    }

    public int remove(String value) {
        int index = this.indexOf(value);
        if (index < 0)
            return -1;

        this.values.remove(index);
        this.fireIntervalRemoved(this, index, index);
        return index;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean val) {
        this.isEditable = val;
    }

    public boolean contains(String name) {
        return values.contains(name);
    }

}
