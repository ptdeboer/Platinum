package nl.esciencecenter.ptk.vbrowser.ui.resourcetable;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

public class TableColumnUpdater implements TableColumnModelListener {
    protected ResourceTable resourceTable;

    public TableColumnUpdater(ResourceTable resourceTable) {
        this.resourceTable = resourceTable;
    }

    @Override
    public void columnAdded(TableColumnModelEvent e) {
    }

    @Override
    public void columnRemoved(TableColumnModelEvent e) {
    }

    @Override
    public void columnMoved(TableColumnModelEvent e) {
    }

    @Override
    public void columnMarginChanged(ChangeEvent e) {
        javax.swing.table.DefaultTableColumnModel colummodel = (DefaultTableColumnModel) e
                .getSource();

        for (int i = 0; i < colummodel.getColumnCount(); i++) {
            TableColumn column = colummodel.getColumn(i);
            int w = column.getWidth();
            String name = column.getHeaderValue().toString();

            // Global.log.debug(this,"columnMarginChanged name:{}={}",name,w);
            // store the new column width in the table Presentation:

            resourceTable.columnMarginChanged(name, w);
        }

    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent e) {
    }

}
