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

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyDataSource;
import nl.esciencecenter.ptk.vbrowser.ui.model.UIViewModel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeContainer;
import nl.esciencecenter.ptk.vbrowser.ui.object.UIDisposable;
import nl.esciencecenter.ptk.vbrowser.ui.properties.UIProperties;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.presentation.VRSPresentation;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Generic Resource Table.
 */
@Slf4j
public class ResourceTable extends JTable implements UIDisposable, ViewNodeContainer {

    // default presentation

    private Presentation _presentation = null;

    private boolean isEditable = true;

    protected int defaultColumnWidth = 80;

    protected ResourceTableUpdater dataProducer;

    private TableMouseListener mouseHandler;

    protected ResourceTableControler controller;

    private UIViewModel uiModel;

    private String sortColumnName;

    private boolean columnSortOrderIsReversed;

    public ResourceTable() {
        // defaults
        super(new ResourceTableModel(false));

        init();
    }

    public ResourceTable(BrowserInterface browserController, ResourceTableModel dataModel) {
        // defaults
        super(dataModel);
        this.controller = new ResourceTableControler(this, browserController);
        init();
    }

    public ResourceTableModel getModel() {
        return (ResourceTableModel) super.getModel();
    }

    public void setDataModel(ResourceTableModel dataModel) {
        this.setModel(dataModel);
        initColumns();
    }

    private void init() {
        if (this.uiModel == null) {
            this.uiModel = UIViewModel.createTableModel();
        }

        this.setAutoCreateColumnsFromModel(false);
        // this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        this.setColumnSelectionAllowed(true);
        this.setRowSelectionAllowed(true);

        initColumns();

        // Listeners !
        JTableHeader header = this.getTableHeader();

        mouseHandler = new TableMouseListener(this, controller);

        header.addMouseListener(mouseHandler);
        this.addMouseListener(mouseHandler);
//        this.initFocusCycle();
    }

    // restore default cycle
//    private void initFocusCycle() {
//        Set<AWTKeyStroke> forward = new HashSet<>(
//        this.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
//        forward.add(KeyStroke.getKeyStroke("TAB"));
//        this.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forward);
//        Set<AWTKeyStroke> backward = new HashSet<AWTKeyStroke>();
//        this.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
//        backward.add(KeyStroke.getKeyStroke("shift TAB"));
//        this.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backward);
//    }

    public TableMouseListener getTableMouseHandler() {
        return mouseHandler;
    }

    /**
     * (re)Created columns from headers taken from DataModel
     */
    public void initColumns() {
        // Use Header from DataModel
        String[] headers = getModel().getHeaders();

        log.debug("initColumns(): getHeaders() = {}", headers.toString());

        if ((headers == null) || (headers.length <= 0)) {
            // Use all attribute names.
            headers = getModel().getAllAttributeNames();
            log.debug("initColumns(): getAllHeaders() = {}",
                    new StringList(headers));
        }

        if ((headers == null) || (headers.length <= 0)) {
            headers = new String[0]; // empty but not null
        }

        initColumns(headers);
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean val) {
        this.isEditable = val;
    }

    private void updateCellEditors() {
        // set cell editors:
        TableColumnModel cmodel = getColumnModel();

        int nrcs = cmodel.getColumnCount();

        for (int i = 0; i < nrcs; i++) {
            TableColumn column = cmodel.getColumn(i);
            Object obj = getModel().getValueAt(0, i);

            if (obj instanceof Attribute) {
                Attribute attr = (Attribute) obj;

                if (attr.isEditable() == true) {
                    switch (attr.getType()) {
                        // both boolean and enum use same select box
                        case ENUM:
                        case BOOLEAN: {
                            // debug("setting celleditor to EnumCellEditor of columnr:"+i);
                            column.setCellEditor(new EnumCellEditor(attr.getEnumValues()));
                            break;
                        }
                        case STRING: {
                            column.setCellEditor(new DefaultCellEditor(new JTextField()));
                        }
                        default: {
                            break;
                        }
                    }
                }
            }
        }
    }

    public ResourceTableModel getResourceTableModel() {
        TableModel model = super.getModel();

        if (model instanceof ResourceTableModel) {
            return (ResourceTableModel) model;
        }

        throw new Error("Resource Table NOT initialized with compatible Table Model!:"
                + model.getClass());
    }

    private void initColumns(String[] headers) {
        TableColumnModel columnModel = new DefaultTableColumnModel();

        for (int i = 0; i < headers.length; i++) {
            String headerName = headers[i];
            // debug("Creating new column:"+headers[i]);
            TableColumn column = createColumn(i, headerName);
            //
            columnModel.addColumn(column);
            // move update presentation out of create loop:
        }

        this.setColumnModel(columnModel);
        if (this.isEditable) {
            this.updateCellEditors();
        }

        // fist update columns 
        updatePresentation();
        // now register updater: 
        columnModel.addColumnModelListener(new TableColumnUpdater(this));
    }

    protected void updatePresentation() {
        Presentation pres = getPresentation();
        if (pres == null) {
            log.warn("*** updatePresentation(): NO Presentation!");
            return;
        }

        // auto resize mode of columns.
        setAutoResizeMode(pres.getColumnsAutoResizeMode());
        TableColumnModel model = this.getColumnModel();

        if (model != null) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                TableColumn column = model.getColumn(i);
                Object colName = column.getIdentifier();
                String headerName = colName.toString();

                column.setResizable(pres.getAttributeFieldResizable(headerName, true));
                // update column width from presentation
                Integer prefWidth = pres.getAttributePreferredWidth(headerName);
                if (prefWidth == null) {
                    prefWidth = headerName.length() * 10;// 10 points font ?
                }

                column.setPreferredWidth(prefWidth);
            }
        }

    }

    private TableColumn createColumn(int modelIndex, String headerName) {
        // one renderer per column
        ResourceTableCellRenderer renderer = new ResourceTableCellRenderer();

        TableColumn column = new TableColumn(modelIndex, 10, renderer, null);
        // identifier object= unique headerName !
        column.setIdentifier(headerName);
        column.setHeaderValue(headerName);
        column.setCellRenderer(renderer);
        // update presentation
        Presentation pres = getPresentation();
        Integer size = null;
        if (pres != null) {
            size = pres.getAttributePreferredWidth(headerName);
        }

        if (size != null) {
            column.setWidth(size);
        } else {
            column.setWidth(defaultColumnWidth);
        }

        return column;
    }

    public int getDataModelHeaderIndex(String name) {
        return getModel().getHeaderIndex(name);
    }

    /**
     * Get the header names as shown, thus in the order as used in the VIEW model (Not dataModel).
     */
    public StringList getColumnHeaders() {
        // get columns headers as currently shown in the VIEW model
        TableColumnModel colModel = this.getColumnModel();
        int len = colModel.getColumnCount();
        StringList names = new StringList(len);

        for (int i = 0; i < len; i++) {
            names.add(colModel.getColumn(i).getHeaderValue().toString());
        }

        return names;
    }

    /**
     * Insert new column after specified 'headerName'. This will insert a new headername but use the
     * current header as viewed as new order so the new headers and column order is the same as
     * currently viewed. This because the user might have switched columns in the VIEW order of the
     * table.
     */
    public void insertColumn(String headerName, String newName, boolean insertBefore) {
        if (this.getHeaderModel().isEditable() == false) {
            return;
        }

        // remove column but use order of columns as currently viewed !
        StringList viewHeaders = this.getColumnHeaders();
        if (insertBefore)
            viewHeaders.insertBefore(headerName, newName);
        else
            viewHeaders.insertAfter(headerName, newName);

        // insert empty column and fire change event. This will update the table.
        this.getModel().setHeaders(viewHeaders);

        this.dataProducer.updateColumn(newName);
    }

    public void removeColumn(String headerName, boolean updatePresentation) {
        if (this.getHeaderModel().isEditable() == false) {
            return;
        }

        // remove column but use order of columns as currently viewed !
        StringList viewHeaders = this.getColumnHeaders();
        viewHeaders.remove(headerName);

        // Triggers restructure, and KEEP the current view order of Columns.
        this.getModel().setHeaders(viewHeaders);

        if ((updatePresentation) && (this.getPresentation() != null)) {
            // Keep headers in persistant Presentation.
            this.getPresentation().setPreferredContentAttributeNames(viewHeaders);
            storePresentation();
        }

        this.getModel().fireTableStructureChanged();

    }

    public HeaderModel getHeaderModel() {
        return this.getModel().getHeaderModel();
    }

    public void tableChanged(TableModelEvent e) {

        if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) {
            initColumns();
        }
        super.tableChanged(e);
    }

    /**
     * Has visible column (Must exist in columnmodel!)
     */
    public boolean hasColumn(String headerName) {
        Enumeration<TableColumn> enumeration = getColumnModel().getColumns();
        TableColumn aColumn;
        // int index = 0;

        while (enumeration.hasMoreElements()) {
            aColumn = enumeration.nextElement();
            // Compare them this way in case the column's identifier is null.
            if (StringUtil.equals(headerName, aColumn.getHeaderValue().toString())) {
                return true;
            }
            // index++;
        }

        return false;
    }

    /**
     * Has visible column (Must exist in columnmodel!)
     */
    public TableColumn getColumnByHeader(String headerName) {
        Enumeration<TableColumn> enumeration = getColumnModel().getColumns();

        while (enumeration.hasMoreElements()) {
            TableColumn col = enumeration.nextElement();
            // Compare them this way in case the column's identifier is null.
            if (StringUtil.equals(headerName, col.getHeaderValue().toString())) {
                return col;
            }
        }

        return null;
    }

    /**
     * Return row Key under Point point. Might return NULL
     */
    public String getKeyUnder(Point point) {
        if (point == null)
            return null;
        int row = rowAtPoint(point);
        if (row < 0)
            return null;
        return getModel().getRowKey(row);
    }

    public Presentation getPresentation() {
        return this._presentation;
    }

    public void setPresentation(Presentation presentation, boolean updateUI) {
        this._presentation = presentation;
        if (updateUI) {
            this.updatePresentation();
        }
    }

    public void dispose() {
    }

    /**
     * Update Data Source.
     *
     * @throws ProxyException
     */
    public void setDataSource(ProxyDataSource dataSource, boolean update) {
        ResourceTableModel model = new ResourceTableModel(false);
        this.setModel(model);

        try {
            // Copy current presentation from source.
            Presentation presentation = dataSource.getPresentation();
            if (presentation != null) {
                setPresentation(presentation.duplicate(true), true);
            }
        } catch (ProxyException e) {
            this.controller.handle("Couldn't get presentation!", e);
            e.printStackTrace();
        }

        ResourceTableUpdater updater = new ResourceTableUpdater(this, dataSource, model);
        setDataProducer(updater, update);

    }

    /**
     * Update Data Source from ProxyNode:
     *
     * @throws ProxyException
     */
    public void setDataSource(ProxyNode proxyNode, boolean update) {
        ResourceTableModel model = new ResourceTableModel(false);
        this.setModel(model);

        if (proxyNode != null) {
            // copy current presentation from source.
            Presentation presentation = proxyNode.getPresentation();
            if (presentation != null) {
                setPresentation(presentation.duplicate(true), true);
            }
        }

        ResourceTableUpdater updater = new ResourceTableUpdater(this, proxyNode, model);
        setDataProducer(updater, update);
    }

    public void setDataProducer(ResourceTableUpdater producer, boolean update) {
        this.dataProducer = producer;
        if (update == false)
            return;

        if (dataProducer != null) {
            // recreate table
            try {
                this.dataProducer.createTable(true, true);
            } catch (ProxyException e) {
                handle("DataProducer failed to create actual Table", e);
            }
        } else {
            this.removeAll();
        }
    }

    /**
     * Returns root ViewNode if Model supports this.
     */
    public ViewNode getRootViewNode() {
        return this.getModel().getRootViewNode();
    }

    /**
     * Returns root ViewNode of row key, if model supports this.
     */
    public ViewNode getViewNodeByKey(String key) {
        return this.getModel().getViewNode(key);
    }

    @Override
    public UIViewModel getUIViewModel() {
        return this.uiModel;
    }

    @Override
    public ViewNode getViewNode() {
        return this.getModel().getRootViewNode();
    }

    @Override
    public boolean requestFocus(boolean value) {
        if (value == true) {
            return this.requestFocusInWindow();
        }

        return false; // unfocus not applicable ?
    }

    @Override
    public ViewNodeContainer getViewContainer() {
        return this;
    }

    @Override
    public ViewNode getNodeUnderPoint(Point p) {
        String key = this.getKeyUnder(p);
        return getViewNodeByKey(key);
    }

    @Override
    public JPopupMenu createNodeActionMenuFor(ViewNode node, boolean canvasMenu) {
        // allowed during testing
        if (this.controller.getBrowserInterface() == null) {
            log.warn("getActionMenuFor() no browser registered.");
            return null;
        }
        // node menu
        return this.controller.getBrowserInterface().createActionMenuFor(this, node, canvasMenu);
    }

    @Override
    public void clearNodeSelection() {
        log.error("FIXME:clearNodeSelection()");
    }

    @Override
    public java.util.List<ViewNode> getNodeSelection() {
        log.debug("getNodeSelection()");

        int[] rowNrs = getSelectedRows();
        if ((rowNrs == null) || (rowNrs.length <= 0)) {
            log.debug("getNodeSelection()=NULL");
            return null; // nothing selected
        }

        ResourceTableModel model = getModel();

        String[] keys = model.getRowKeys(rowNrs);

        java.util.List<ViewNode> nodes = new ArrayList<>();

        for (String key : keys) {
            nodes.add(model.getViewNode(key));
        }

        log.debug("getNodeSelection()=#{}", nodes.size());
        return nodes;
    }

    @Override
    public void setNodeSelection(ViewNode node, boolean isSelected) {
        log.debug("getNodeSelection() {}:{}", isSelected, node);

        int[] rowNrs = getSelectedRows();
        if ((rowNrs == null) || (rowNrs.length <= 0)) {
            log.error("getNodeSelection()=NULL");
            if (isSelected) {
                log.error(
                        "***FIXME: node should be selected, but selected rows is EMPTY for node:%s\n",
                        node);
            } else {
                log.debug("No selection, node is already unselected:{}", node);
            }
            return;
        }

        String nodeKey = this.getModel().createRowKey(node);
        int rowIndex = this.getModel().getRowIndex(nodeKey);

        for (int i = 0; i < rowNrs.length; i++) {
            if (rowNrs[i] == rowIndex) {
                if (isSelected) {
                    log.debug("Selected node is in selection range (row#={}):{}",
                            rowNrs[i], node);
                    return;
                }
            }
        }

        if (isSelected == false) {
            log.debug("setNodeSelection():OK node is not in selected rows:{}", node);
        } else {
            log.error(
                    "***FIXME:setNodeSelection(): Row should already be selected but isn't:%s\n",
                    node);
        }

        // Rows AND columns -> SpreadSheet export
        // int[] colsselected=getSelectedColumns();
    }

    @Override
    public void setNodeSelectionRange(ViewNode firstNode, ViewNode lastNode, boolean isSelected) {
        log.error("FIXME:setNodeSelectionRange(): check range: [{},{}]", firstNode,
                lastNode);
    }

    public ResourceTableUpdater getDataProducer() {
        return this.dataProducer;
    }

    private void handle(String action, ProxyException e) {
        controller.handle(action, e);
    }

    public void doSortColumn(String name, boolean reverse) {
        this.getResourceTableModel().doSortColumn(name, reverse);
        this.sortColumnName = name;
        this.columnSortOrderIsReversed = reverse;
    }

    public boolean getColumnSortOrderIsReversed() {
        return this.columnSortOrderIsReversed;
    }

    public String getSortColumnName() {
        return sortColumnName;
    }

    public UIProperties getGuiSettings() {
        return getBrowserInterface().getPlatform().getGuiSettings();
    }

    @Override
    public BrowserInterface getBrowserInterface() {
        return controller.getBrowserInterface();
    }

    @Override
    public Rectangle findBoundsOfSelectionNode(ViewNode node) {
        return new Rectangle(0, 0, 0, 0);
    }

    public ProxyDataSource getDataSource() {
        if (this.dataProducer == null) {
            return null;
        }

        return this.dataProducer.getDataSource();
    }

    public void columnMarginChanged(String name, int w) {
        // buggy, during resizes invalid values are submitted. 
        //log.error("FIXME: columnMarginChanged():{}={}", name, w);
        this.getPresentation().setAttributePreferredWidth(name, w);
        storePresentation();
    }

    public void updateAutoResizeMode(int mode) {
        log.debug("updateAutoResizeMode():{}", mode);
        super.setAutoResizeMode(mode);
        this.getPresentation().setColumnsAutoResizeMode(mode);
        storePresentation();
    }

    protected void storePresentation() {
        ViewNode viewNode = this.getViewNode();
        VRL vrl = viewNode.getVRL();
        String resourceType = viewNode.getResourceType();

        // still buggy:
        VRSPresentation.storePresentation(vrl, resourceType, getPresentation());
        //log.error("--- presentation ---\n{}", getPresentation());
    }

    public JComponent getJComponent() {
        return this;
    }

}
