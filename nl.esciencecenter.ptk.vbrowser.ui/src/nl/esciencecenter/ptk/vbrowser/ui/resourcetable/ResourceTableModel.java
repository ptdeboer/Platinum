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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.util.QSort;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Generic Resource Table Model.
 * <p>
 * An ViewNode can both be the "row" object or an Attribute Object. Current row "key" is the VRL.
 */
public class ResourceTableModel extends AbstractTableModel implements Iterable<ResourceTableModel.RowData>
{
    private static final long serialVersionUID = 4392816921140385298L;

    private static ClassLogger logger;

    static
    {
        logger = ClassLogger.getLogger(ResourceTableModel.class);
        logger.setLevelToDebug();
    }

    /**
     * Resource Row Data
     */
    public class RowData
    {
        /** RowKey, typically this is the VRL */
        private String rowKey;

        private AttributeSet rowAttributes = new AttributeSet();

        private ViewNode viewNode;

        public RowData(ViewNode viewNode, String rowKey, AttributeSet attrs)
        {
            this.rowKey = rowKey;
            this.viewNode = viewNode;
            if (attrs != null)
            {
                this.rowAttributes = attrs.duplicate(); // empty set!
            }
        }

        public String getKey()
        {
            return rowKey;
        }

        public int getNrAttributes()
        {
            return rowAttributes.size();
        }

        public Attribute getAttribute(String name)
        {
            return rowAttributes.get(name);
        }

        public Attribute getAttribute(int nr)
        {
            String key = rowAttributes.getKey(nr);
            if (key == null)
                return null;
            return rowAttributes.get(key);
        }

        public void updateData(AttributeSet data, boolean merge)
        {
            if (merge == false)
            {
                this.rowAttributes = data.duplicate();
                return;
            }

            // dump:
            this.rowAttributes.putAll(data);
            uiFireRowChanged(this);
        }

        public void setValue(String attrName, String value)
        {
            this.rowAttributes.set(attrName, value);
            uiFireCellChanged(this, attrName);
        }

        public void setObjectValue(String attrName, Object obj)
        {
            this.rowAttributes.setAny(attrName, obj);
            uiFireCellChanged(this, attrName);
        }

        public void setValues(List<Attribute> attrs)
        {
            if (attrs == null)
                return;

            for (Attribute attr : attrs)
                this.rowAttributes.put(attr);

            uiFireRowChanged(this);
        }

        public void removeValue(String attr)
        {
            this.rowAttributes.remove(attr);
        }

        public int getIndex()
        {
            // check entry inside Table Rows !
            synchronized (ResourceTableModel.this.rows)
            {
                return ResourceTableModel.this.rows.indexOf(this);
            }
        }

        public String[] getAttributeNames()
        {
            return this.rowAttributes.getAttributeNames();
        }

        /** Get Optional ViewNode */
        public ViewNode getViewNode()
        {
            return this.viewNode;
        }

        /** Set Optional ViewNode */
        public void setViewNode(ViewNode node)
        {
            this.viewNode = node;
        }
    }

    // ========================================================================
    // Instance
    // ========================================================================

    /**
     * Synchronized Vector which contains rows as shown, default empty NOT null. Also serves as data mutex!
     */
    private Vector<RowData> rows = new Vector<RowData>();

    /**
     * Mapping of Key String to row Index number
     */
    private Map<String, Integer> rowKeyIndex = new Hashtable<String, Integer>();

    // Current HeaderModel default empty, NOT null
    private HeaderModel headers = new HeaderModel();

    private ViewNode rootViewNode;

    private String iconHeaderName = "icon";

    private StringList allAttributeNames = null;

    // For Testing
    protected ResourceTableModel(String[] headers)
    {
        super();
        initHeaders(headers);
    }

    public ResourceTableModel(boolean addNillRow)
    {
        super();
        // Empty but not null model to check initialization.
        String names[] = new String[0];
        headers = new HeaderModel(names);

        // nill attribute set
        if (addNillRow)
        {
            AttributeSet dummySet = new AttributeSet();
            dummySet.set("", "");
            // nill row.
            this.addRow(null, "", dummySet, true);
        }
    }

    @Override
    public int getColumnCount()
    {
        return headers.getSize();
    }

    @Override
    public int getRowCount()
    {
        return rows.size();
    }

    /**
     * Clear Data information. Keeps header information
     */
    public void clearData()
    {
        synchronized (rows) // for rows and keys

        {
            this.rows.clear();
            this.rowKeyIndex.clear();
        }

        this.fireTableDataChanged();
    }

    @Override
    public RowIterator iterator()
    {
        return new RowIterator(this);
    }

    protected void setRootViewNode(ViewNode node)
    {
        this.rootViewNode = node;
    }

    public ViewNode getRootViewNode()
    {
        return rootViewNode;
    }

    public int[] doSortColumn(String name, boolean reverse)
    {
        ClassLogger.getLogger(ResourceTableModel.class).debugPrintf("sortBy:%s , reverse=%s\n", name, reverse);

        int colnr = getHeaderIndex(name);

        if (colnr < 0)
            return null;

        ClassLogger.getLogger(ResourceTableModel.class).debugPrintf("sortBy column number=%d\n", colnr);

        TableRowComparer comparer = new TableRowComparer(name, reverse);
        QSort<RowData> sorter = new QSort<RowData>(comparer);

        int mapping[];

        synchronized (rows)
        {
            // in memory sort !
            mapping = sorter.sort(rows);

            // reINdex key vecto:
            reindexKeyVector();
        }

        this.fireTableDataChanged();
        return mapping;
    }

    /**
     * Reindex key to index mapping.
     */
    protected void reindexKeyVector()
    {
        // Mutex: Lock both rows AND rowKeyIndex !
        synchronized (rows)
        {
            synchronized (rowKeyIndex)
            {
                this.rowKeyIndex.clear();

                int n = rows.size();

                for (int i = 0; i < n; i++)
                {
                    RowData row = this.rows.get(i);
                    this.rowKeyIndex.put(row.getKey(), new Integer(i));
                }
            }
        }
    }

    // ========================================================================
    // Header methods.
    // ========================================================================

    protected void initHeaders(String[] headers)
    {
        this.headers = new HeaderModel(headers);
    }

    public boolean hasHeader(String name)
    {
        return getHeaderModel().contains(name);
    }

    /**
     * Specify icon column header name. For this column the ViewNode will provide the Icon.
     */
    public void setIconHeaderName(String iconName)
    {
        this.iconHeaderName = iconName;
    }

    public void setHeaders(StringList headers)
    {
        this.headers.setValues(headers);
        this.fireTableStructureChanged();
    }

    public int getHeaderIndex(String name)
    {
        return this.headers.indexOf(name);
    }

    public HeaderModel getHeaderModel()
    {
        return this.headers;
    }

    /**
     * Removes header and fires TableStructureChanged event. Actual column data is kept in the model to avoid null
     * pointer bugs.
     * 
     * Method fires TableStructureChanged event which update the actual table. Only after the TableStructureChanged
     * event has been handled.
     */
    public void removeHeader(String headerName)
    {
        this.headers.remove(headerName);
        this.fireTableStructureChanged();
    }

    /**
     * Inserts new header into the headermodel after or before 'headerName'. Method fires TableStructureChanged event
     * which updates the Table. Only after the TableStructureChanged event has been handled, the table column model has
     * added the new Column ! This Table Data Model can already be updated asynchronously after the new header has been
     * added.
     */
    public int insertHeader(String headerName, String newName, boolean insertBefore)
    {
        // update Table Structure
        int index = this.headers.insertHeader(headerName, newName, insertBefore);
        this.fireTableStructureChanged();
        return index;
    }

    /**
     * Add listener to header list model, which controls the column headers. Not that due to the asynchronous nature of
     * Swing Events, the Header Model might already have changed, but the Viewed column model use by Swing might not
     * have.
     */
    public void addHeaderModelListener(ListDataListener listener)
    {
        this.headers.addListDataListener(listener);
    }

    public void removeHeaderModelListener(ListDataListener listener)
    {
        this.headers.removeListDataListener(listener);
    }

    /**
     * All attribute names available from DataModel. Each attribute name can be used as column.
     * 
     * @return all available attribute names as List.
     */
    public List<String> getAllAttributeNames()
    {
        return allAttributeNames;
    }

    /**
     * Allow editable columns by specifying all possible headers
     */
    public void setAllAttributeNames(StringList list)
    {
        this.allAttributeNames = list.duplicate();
    }

    public List<String> getHeaders()
    {
        return headers.toArray();
    }

    // ========================================================================
    // Row methods.
    // ========================================================================

    /**
     * Create row key from VRL.
     */
    public String createRowKey(VRL vrl)
    {
        return vrl.toString();
    }

    /**
     * Create row key from ViewNode, currently the VRL is used as rowKey.
     */
    public String createRowKey(ViewNode viewNode)
    {
        return viewNode.getVRL().toString();
    }

    public String[] createRowKeys(VRL[] vrls)
    {
        String keys[] = new String[vrls.length];
        for (int i = 0; i < vrls.length; i++)
        {
            keys[i] = createRowKey(vrls[i]);
        }

        return keys;
    }

    /**
     * Returns duplicate array containing current row objects.
     */
    public RowData[] getRows()
    {
        synchronized (this.rows)
        {
            int len = this.rows.size();
            RowData rows[] = new RowData[len];
            rows = this.rows.toArray(rows);
            return rows;
        }
    }

    public ViewNode getViewNode(String key)
    {
        RowData row = this.getRow(key);
        if (row == null)
            return null;

        return row.getViewNode();
    }

    /**
     * Create new Rows with empty Row Data
     */
    public void allocRows(List<String> rowKeys)
    {
        synchronized (rows)
        {
            this.rows.clear();
            this.rowKeyIndex.clear();

            for (String key : rowKeys)
            {
                // add to internal data structure only
                addRow(null, key, new AttributeSet(), false);
            }
        }

        this.fireTableDataChanged();
    }

    /**
     * Create new empty row with specified key and empty AttributeSet().
     * 
     * @param key
     *            - rowKey
     * @return index of new row.
     */
    public int createRow(String rowKey)
    {
        return addRow(null, rowKey, new AttributeSet(), false);
    }

    private int addRow(RowData rowData, boolean fireEvent)
    {
        boolean rowExists = false;

        if ((rowData == null) || (rowData.rowKey == null))
        {
            throw new NullPointerException("Cannot add NULL RowData or row with NULL key (use nill rowdata and nill key).");
        }

        Integer index;

        synchronized (rows)
        {
            String key = rowData.rowKey;
            index = rowKeyIndex.get(key);// Note: NULL for non-existing keys!
            if ((index != null) && (index >= 0))
            {
                // row exist, replace!
                rows.set(index, rowData);
                rowExists = true;
            }
            else
            {
                index = rows.size();
                this.rows.add(rowData);
                this.rowKeyIndex.put(rowData.rowKey, new Integer(index));
            }
        }

        logger.debugPrintf("addRow(): %s new row at index %d\n", (rowExists ? "replaced" : "created"), index);

        if (fireEvent)
        {
            this.fireTableRowsInserted(index, index);
        }

        return index;
    }

    // add row to internal data structure
    public int addRow(ViewNode viewNode, String key, AttributeSet attrs, boolean fireEvent)
    {
        return addRow(new RowData(viewNode, key, attrs), fireEvent);
    }

    /**
     * Add new Row and return index to row.
     */
    public int addRow(ViewNode viewNode, AttributeSet attrs)
    {
        return addRow(viewNode, createRowKey(viewNode), attrs, true);
    }

    public int addEmptyRow(String key)
    {
        return addRow(null, key, new AttributeSet(), true);
    }

    public RowData delRow(String key)
    {
        return _delRow(key, true);
    }

    /**
     * Deletes Row. Performance note: Since a delete triggers an update for the used Key->Index mapping. This method
     * takes O(N) time.
     * 
     * @param index
     * @return
     */
    public RowData delRow(int index)
    {
        return this._delRow(index, true);
    }

    /**
     * Deletes Row. Performance note: Since a delete triggers an update for the used Key->Index mapping. This method
     * takes O(N) time. (Where N= nr of rows in table)
     * 
     * @param index
     * @return
     */
    public boolean delRows(int indices[])
    {
        // multi delete to avoid O(N*N) rekeying of key mapping !
        boolean result = this._delRows(indices, false);

        for (int i = 0; i < indices.length; i++)
        {
            this.fireTableRowsDeleted(indices[i], indices[i]);
        }

        return result;
    }

    // delete row from internal data structure
    private RowData _delRow(String key, boolean fireEvent)
    {
        // synchronized for ROWS and rowKeyIndex as well !
        synchronized (rows)
        {
            Integer index = this.rowKeyIndex.get(key);
            if (index == null)
            {
                return null;
            }

            return this._delRow(index, fireEvent);
        }
    }

    /**
     * Delete row from internal data structure. Performance note: here the internal key mapping is regenerated. This
     * take O(N) time.
     * 
     * @param rowIndex
     *            -
     * @param fireEvent
     *            - whether to fire an event.
     * @return
     */
    private RowData _delRow(int rowIndex, boolean fireEvent)
    {
        RowData rowObj = null;

        synchronized (rows)// sync for both rows and rowKeyIndex!
        {
            if ((rowIndex < 0) || (rowIndex >= rows.size()))
            {
                return null;
            }

            rowObj = rows.get(rowIndex);
            String key = rowObj.getKey();
            rows.remove(rowIndex);
            rowKeyIndex.remove(key);

            // update indices: start from 'index'
            // index=0;
            // rowKeyIndex.clear();
            for (int i = rowIndex; i < rows.size(); i++)
            {
                this.rowKeyIndex.put(rows.get(i).getKey(), new Integer(i));
            }
        }

        if (fireEvent)
        {
            this.fireTableRowsDeleted(rowIndex, rowIndex);
        }
        return rowObj;
    }

    /**
     * Multi delete rows from internal data structure. Performance note: here the internal key mapping is regenerated.
     * This take O(N) time.
     * 
     * @param index
     * @return
     */
    private boolean _delRows(int indices[], boolean fireEvent)
    {
        boolean allDeleted = true;
        synchronized (rows)// sync for both rows and rowKeyIndex!
        {
            for (int index : indices)
            {
                if ((index < 0) || (index >= rows.size()))
                {
                    allDeleted = false;
                }

                RowData rowObj = rows.get(index);
                String key = rowObj.getKey();
                rows.remove(index);
                this.rowKeyIndex.remove(key);

                if (fireEvent)
                {
                    // Concurrency note: Within Synchronized(!): Fire event per row
                    this.fireTableRowsDeleted(index, index);
                }
            }
            // within sync(rows)
            this.reindexKeyVector();
        }

        return allDeleted;
    }

    /**
     * Search key and return row index.
     */
    public int getRowIndex(String key)
    {
        if (key == null)
        {
            return -1;
        }

        synchronized (rows)
        {
            Integer index = this.rowKeyIndex.get(key);

            if (index == null)
            {
                return -1;
            }

            return index;
        }
    }

    /**
     * Return Key of Row index.
     */
    public String getRowKey(int index)
    {
        synchronized (this.rows)
        {
            if ((index < 0) || (index >= this.rows.size()))
            {
                return null;
            }

            return this.rows.get(index).getKey();
        }
    }

    /**
     * Return copy of current keys as array.
     */
    public String[] getRowKeys()
    {
        synchronized (this.rows)
        {
            String keys[] = new String[this.rows.size()];
            for (int i = 0; i < this.rows.size(); i++)
            {
                keys[i] = rows.elementAt(i).getKey();
            }
            return keys;
        }
    }

    public RowData getRow(int index)
    {
        synchronized (rows)
        {
            if ((index < 0) || (index >= rows.size()))
            {
                return null;
            }
            return this.rows.get(index);
        }
    }

    public RowData getRow(String key)
    {
        synchronized (rows)
        {
            int index = this.getRowIndex(key);
            if ((index < 0) || (index >= rows.size()))
            {
                return null;
            }
            return this.rows.get(index);
        }
    }

    public boolean hasRow(String key)
    {
        return (getRowIndex(key) >= 0);
    }

    /**
     * Convert row numbers to row keys.
     */
    public String[] getRowKeys(int[] rowNrs)
    {
        String keys[] = new String[rowNrs.length];
        for (int i = 0; i < rowNrs.length; i++)
        {
            keys[i] = this.getRowKey(rowNrs[i]);
        }
        return keys;
    }

    public RowData delRow(VRL vrl)
    {
        // currently the VRL is the key.
        return delRow(createRowKey(vrl));
    }

    public RowData replaceRow(int index, ViewNode viewNode, AttributeSet attributeSet)
    {
        String newKey = createRowKey(viewNode);
        RowData newRow = new RowData(viewNode, newKey, attributeSet);
        return _replaceRow(index, newKey, newRow);
    }

    protected RowData _replaceRow(int index, String newKey, RowData newRow)
    {
        RowData oldRow;

        synchronized (rows)
        {
            oldRow = rows.get(index);
            String oldKey = null;
            if (oldRow != null)
            {
                oldKey = oldRow.rowKey;
            }
            rows.set(index, newRow);

            synchronized (rowKeyIndex)
            {
                rowKeyIndex.remove(oldKey);
                rowKeyIndex.put(newKey, new Integer(index));
            }
        }

        return oldRow;

    }

    // ========================================================================
    // Cell/Value methods.
    // ========================================================================

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        synchronized (rows)
        {
            if ((rowIndex < 0) || rowIndex >= this.rows.size())
            {
                return null;
            }

            if ((columnIndex < 0) || columnIndex >= this.headers.getSize())
            {
                return null;
            }

            RowData rowObj = rows.get(rowIndex);

            if (rowObj == null)
            {
                logger.warnPrintf("getValueAt: Index Out of bounds:[%d,%d]\n", rowIndex, columnIndex);
                return null;
            }

            String header = this.headers.getElementAt(columnIndex);

            Attribute attr = rowObj.getAttribute(header);

            if (attr == null)
            {
                if (header.equals(this.iconHeaderName))
                {
                    ViewNode viewNode = rowObj.getViewNode();
                    if (viewNode != null)
                        return viewNode.getIcon();
                }

                return null;
            }

            return attr;
        }
    }

    public Object getValueAt(String rowKey, String attrName)
    {
        synchronized (rows)
        {
            Attribute attr = getAttribute(rowKey, attrName);
            if (attr == null)
            {
                return null;
            }
            return attr.getValue();
        }
    }

    /**
     * Returns Attribute Value ! use getAttrStringValue for actual string value of attribute
     */
    public Object getValueAt(int rowIndex, String attrName)
    {
        synchronized (rows)
        {
            if ((rowIndex < 0) || rowIndex >= this.rows.size())
            {
                return null;
            }

            RowData row = rows.get(rowIndex);
            Attribute attr = row.getAttribute(attrName);
            // parsing checking ?
            return attr; // attr.getValue();
        }
    }

    /**
     * Return Attribute.getValue() of specified row,attributeName.
     */
    public String getAttrStringValue(String rowKey, String attrName)
    {
        Attribute attr = getAttribute(rowKey, attrName);
        if (attr == null)
        {
            return null;
        }
        return attr.getStringValue();
    }

    public String getAttrStringValue(int row, String attrName)
    {
        Attribute attr = getAttribute(row, attrName);
        if (attr == null)
        {
            return null;
        }
        return attr.getStringValue();
    }

    public boolean setValue(String key, String attrName, String value)
    {
        synchronized (rows)
        {
            int rowIndex = this.getRowIndex(key);
            if (rowIndex < 0)
            {
                return false;
            }

            RowData row = this.rows.get(rowIndex);
            if (row == null)
            {
                return false;
            }
            row.setValue(attrName, value);
            this.fireTableRowsUpdated(rowIndex, rowIndex);
            return true;
        }
    }

    public boolean setValue(String key, Attribute attr)
    {
        ArrayList<Attribute> list = new ArrayList<Attribute>();
        list.add(attr);
        return setValues(key, list);
    }

    public boolean setValues(String key, List<Attribute> attrs)
    {
        synchronized (rows)
        {
            int rowIndex = this.getRowIndex(key);
            if (rowIndex < 0)
            {
                return false;
            }

            RowData row = this.rows.get(rowIndex);
            if (row == null)
            {
                return false;
            }
            row.setValues(attrs);
            this.fireTableRowsUpdated(rowIndex, rowIndex);
            return true;
        }
    }

    public void setValueAt(Object value, int rowNr, int colNr)
    {
        RowData row = getRow(rowNr);

        if (row == null)
        {
            logger.warnPrintf("setValueAt(): Index out of bound:[%d,%d]=%s\n", rowNr, colNr, value);
            return;
        }

        // Let Attribute figure object out!
        row.getAttribute(colNr).setObjectValue(value);

        // optimization note: table will collect multiple events
        // and do the drawing at once.

        this.fireTableCellUpdated(rowNr, colNr);
    }

    public Attribute getAttribute(String rowKey, String attrName)
    {
        synchronized (rows)
        {
            int rowIndex = this.getRowIndex(rowKey);
            if (rowIndex < 0)
            {
                return null;
            }
            return getAttribute(rowIndex, attrName);
        }
    }

    public Attribute getAttribute(int rowIndex, String attrName)
    {
        synchronized (rows)
        {
            RowData row = this.rows.get(rowIndex);
            if (row == null)
            {
                return null;
            }
            return row.getAttribute(attrName);
        }
    }

    public Attribute getAttribute(int row, int col)
    {
        synchronized (rows)
        {
            RowData rowdata = this.rows.get(row);
            if (rowdata == null)
            {
                return null;
            }
            return rowdata.getAttribute(col);
        }
    }

    public boolean isCellEditable(int row, int col)
    {
        Object obj = this.getValueAt(row, col);
        if (obj instanceof Attribute)
        {
            return ((Attribute) obj).isEditable();
        }
        return false;
    }

    /**
     * Return copy of Data as Attribute matrix. Returns: Attribute[row][col].
     */
    public Attribute[][] getAttributeData()
    {
        synchronized (rows)
        {
            int nrRows = this.rows.size();
            if (nrRows <= 0)
            {
                return null;
            }

            // assume symmetrical:
            int nrCols = rows.get(0).getNrAttributes();
            if (nrCols <= 0)
            {
                return null;
            }

            Attribute attrs[][] = new Attribute[nrRows][];
            for (int row = 0; row < nrRows; row++)
            {
                attrs[row] = new Attribute[nrCols];
                for (int col = 0; col < nrCols; col++)
                {
                    attrs[row][col] = this.getAttribute(row, col).duplicate();
                }
            }

            return attrs;
        }
    }

    // ==========================================================================
    // Events
    // ==========================================================================

    public void uiFireRowChanged(RowData row)
    {
        int index = this.getRowIndex(row.getKey());
        this.fireTableRowsUpdated(index, index);
    }

    public void uiFireCellChanged(RowData row, String name)
    {
        int rownr = this.getRowIndex(row.getKey());
        int colnr = getHeaderModel().indexOf(name);

        if ((rownr < 0) || (colnr < 0))
        {
            logger.warnPrintf("Error, couldn't find {row,attr}=%s,%s\n", row.getKey(), name);
            return;
        }
        this.fireTableCellUpdated(rownr, colnr);
    }

    // ==========================================================================
    // Misc.
    // ==========================================================================

}
