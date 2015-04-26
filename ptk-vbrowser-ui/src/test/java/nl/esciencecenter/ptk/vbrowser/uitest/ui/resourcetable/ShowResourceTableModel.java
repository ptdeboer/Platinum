package nl.esciencecenter.ptk.vbrowser.uitest.ui.resourcetable;


import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTableModel;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTableModel.RowData;

import org.junit.Assert;
import org.junit.Test;

public class ShowResourceTableModel
{

    // ================
    // Asserts
    // ================

    static protected void assertEmpty(ResourceTableModel model, String headers[])
    {
        Assert.assertEquals("Empty model should have 0 rows.", 0, model.getRowCount());
        Assert.assertEquals("Number of header doesn't match.", headers.length, model.getColumnCount());

        String actualHeaders[] = model.getHeaders();

        for (int i = 0; i < headers.length; i++)
        {
            Assert.assertEquals("Header list entry #" + i + "doesn't match", headers[i], actualHeaders[i]);
        }
    }

    static String createRowKey(int rowIndex)
    {
        return  "key"+rowIndex;  
    }

    // ================ 
    // Tests 
    // ================

    @Test
    public void test_CreateNew()
    {
        testNewTable(new String[]{"header"}); 
        testNewTable(new String[]{"header1","header2"});
        // nill table(!) 
        testNewTable(new String[]{""}); 
    }

    public void testNewTable(String headers[])
    {
        ResourceTableModel model = new ResourceTableModel(headers);
        assertEmpty(model, headers);
    }

    @Test
    public void test_CreateNill()
    {
        int numHeaders = 0;

        ResourceTableModel nillModel = new ResourceTableModel(false);

        Assert.assertEquals("Empty model should have 0 rows.", 0, nillModel.getRowCount());
        Assert.assertEquals("Number of header doesn't match.", numHeaders, nillModel.getColumnCount());

        nillModel = new ResourceTableModel(true);

        Assert.assertEquals("Empty model should have 0 rows.", 1, nillModel.getRowCount());
        Assert.assertEquals("Number of header doesn't match.", numHeaders, nillModel.getColumnCount());
    }

    @Test
    public void test_AddRows()
    {
        testTableRows(new String[]{"headerA"},new String[][]{{"value1"}});
        testTableRows(new String[]{"headerB"},new String[][]{{"value1"},{"value2"},{"value3"}});
        testTableRows(new String[]{"headerC"},new String[][]{{"1"},{"2"},{"3"}});
        // nill row
        testTableRows(new String[]{""},new String[][]{{"value"}});

        // nill row
        testTableRows(new String[]{"headerD"},new String[][]{{""}});
        // nill table
        testTableRows(new String[]{""},new String[][]{{""}});
        // --- 
        // multi data
        // ---
        testTableRows(new String[]{"headerA","headerB"},new String[][]{{"1","2"},{"4","5"}});
        testTableRows(new String[]{"headerA","headerB","headerC"},new String[][]{{"1","2","3"},{"4","5","6"}});
    }

    public void testTableRows(String headers[], String rowData[][])
    {
        ResourceTableModel tableModel=testAddTableRows(headers,rowData); 

        testDeleteTableRows(tableModel,headers,rowData);
    }

    protected ResourceTableModel testAddTableRows(String headers[], String rowData[][])
    {
        int numRows = rowData.length;

        ResourceTableModel model = new ResourceTableModel(headers);
        assertEmpty(model, headers);

        for (int i = 0; i < numRows; i++)
        {
            String key = createRowKey(i);
            // assert non existing row 
            Assert.assertNull("New rowkey already defined!",model.getRow(key));  
            Assert.assertEquals("Non existant rowkey must return -1 as index number", -1,model.getRowIndex(key)); 

            // create row. 
            int rowIndex = model.createRow(key);
            int rowKeyIndex = model.getRowIndex(key);
            Assert.assertEquals("Index of new added row must match row number", i, rowIndex);
            Assert.assertEquals("New index of key:'" + key + "'must match row number", i, rowKeyIndex);

            String rowValues[] = rowData[i];

            for (int j = 0; j < rowValues.length; j++)
            {
                String name = headers[j];
                model.setValue(key, name, rowValues[j]);
                String newValue = model.getAttrStringValue(key, name);
                Assert.assertEquals("Cell data doesn't match, [row,col]=[" + i + "," + j + "]", rowValues[j], newValue);
            }
        }

        return model; 
    }

    protected void testDeleteTableRows(ResourceTableModel tableModel, String headers[], String rowData[][])
    {
        int numRows=rowData.length; 
        int numToDeleted=tableModel.getRowCount(); 

        // check consistancy. 
        Assert.assertEquals("Number of rows to delete must match actual number of rows", numRows, numToDeleted);
        Assert.assertEquals("Number of header doesn't match.", headers.length, tableModel.getColumnCount());


        for (int i=0;i<numRows;i++)
        {
            // PRE: 
            String key = createRowKey(i);
            RowData rowObj=tableModel.getRow(key);
            Assert.assertNotNull("Before deletion, actual the actual row to be deleted must exist",rowObj);
            // DEL: 
            tableModel.delRow(key); 
            // POST: 
            Assert.assertEquals("After a row deletion, number of rows shoud be less",(numRows-i-1),tableModel.getRowCount()); 
            rowObj = tableModel.getRow(key); 
            Assert.assertNull("After row deletion, getRow(key) must return null",rowObj);
        }

    }

}
