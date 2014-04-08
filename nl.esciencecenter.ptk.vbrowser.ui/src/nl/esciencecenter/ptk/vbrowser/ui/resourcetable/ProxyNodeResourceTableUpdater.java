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

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.task.ITaskSource;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserTask;
import nl.esciencecenter.ptk.vbrowser.ui.browser.ProxyBrowserController;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyDataSource;
import nl.esciencecenter.ptk.vbrowser.ui.model.UIViewModel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeContainer;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNodeDataSourceProvider;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTableModel.RowData;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.event.VRSEvent;
import nl.esciencecenter.vbrowser.vrs.event.VRSEvent.VRSEventType;
import nl.esciencecenter.vbrowser.vrs.event.VRSEventListener;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class ProxyNodeResourceTableUpdater implements VRSEventListener
{
    private static ClassLogger logger;

    static
    {
        logger = ClassLogger.getLogger(ProxyNodeResourceTableUpdater.class);
        logger.setLevelToDebug();
    }

    // ========================================================================
    // Instance
    // ========================================================================

    private ProxyDataSource dataSource;

    private ResourceTableModel tableModel;

    private UIViewModel uiModel;

    private ViewNode rootNode;

    private ViewNodeContainer tableContainer;

    /** 
     * Create ProxyNode data Updater from ProxyNode 
     */
    public ProxyNodeResourceTableUpdater(ViewNodeContainer tableContainer, ProxyNode pnode,
            ResourceTableModel resourceTableModel)
    {
        this.tableContainer = tableContainer;
        init(new ProxyNodeDataSourceProvider(pnode), resourceTableModel);
    }

    /** 
     * Create Table from default ProxyDataSource. 
     */
    public ProxyNodeResourceTableUpdater(ViewNodeContainer tableContainer, ProxyDataSource dataSource,
            ResourceTableModel resourceTableModel)
    {
        this.tableContainer = tableContainer;
        init(dataSource, resourceTableModel);
    }
    
    public BrowserInterface getMasterBrowser()
    {
        if (tableContainer == null)
        {
            return null; // only allowed during testing.
        }

        return this.tableContainer.getBrowserInterface();
    }

    protected ITaskSource getTaskSource()
    {
        BrowserInterface masterB = getMasterBrowser();

        if (masterB instanceof ProxyBrowserController)
        {
            return masterB.getTaskSource();
        }

        return null;
    }

    protected void init(ProxyDataSource nodeDataSource, ResourceTableModel resourceTableModel)
    {
        this.tableModel = resourceTableModel;
        this.uiModel = UIViewModel.createTableModel();
        setDataSource(nodeDataSource);
    }

    protected void setDataSource(ProxyDataSource nodeDataSource)
    {
        if (dataSource != null)
        {
            dataSource.removeDataSourceEventListener(this);
        }
        this.dataSource = nodeDataSource;
        // receive events
        if (dataSource != null)
        {
            this.dataSource.addDataSourceEventListener(this);
            this.rootNode = null;
        }
        else
        {
            logger.errorPrintf("FIXME: null ProxyNodeDataSource\n");
            tableModel.clearData();
        }
    }

    public void createTable(boolean headers, boolean data) throws ProxyException
    {
        this.rootNode = dataSource.getRoot(uiModel);
        this.tableModel.setRootViewNode(dataSource.getRoot(uiModel));

        if (headers)
        {
            initHeaders();
        }

        if (data)
        {
            updateData();
        }
    }

    public Presentation getPresentation()
    {
        // Custom Presentation:
        Presentation pres = null;

        try
        {
            pres = dataSource.getPresentation();

            if (pres != null)
            {
                logger.debugPrintf("Using Presentation from DataSource:%s\n", dataSource);
            }
            else
            {
                ViewNode rootNode = getRootViewNode();

                logger.debugPrintf("Using default Presentation for:%s\n", rootNode);
                // Check default Presentation form Scheme+Type;
                pres = Presentation.getPresentationForSchemeType(rootNode.getVRL().getScheme(), rootNode.getResourceType(), true);
            }

        }
        catch (ProxyException e)
        {
            logger.logException(ClassLogger.ERROR, e, "Failed to get Presenation from dataSource:%s\n", dataSource);
            handle("Couldn't get presentation\n", e);
        }

        return pres;
    }

    protected void initHeaders() throws ProxyException
    {
        if (dataSource == null)
        {
            // clear:
            tableModel.setHeaders(new StringList());
            return;
        }

        String[] names = null;
        String iconAttributeName = null;

        Presentation pres = getPresentation();

        // set default attributes
        if (pres == null)
        {
            logger.debugPrintf("No Presentation(!) for ViewNode:%s\n", rootNode);
        }
        else
        {
            names = pres.getPreferredChildAttributeNames();
            // update icon attribute name:
            iconAttributeName = pres.getIconAttributeName();
            logger.debugPrintf("Using headers from Presentation.getChildAttributeNames():%s\n", new StringList(names));
        }

        if (names == null)
        {
            logger.warnPrintf("No Headers for:%s\n", this);
            return;
        }

        StringList headers = new StringList();

        for (String name : names)
        {
            headers.add(name);
        }

        filterHeaders(headers);
        tableModel.setHeaders(headers);

        // Specify icon column:
        if (iconAttributeName != null)
        {
            tableModel.setIconHeaderName(iconAttributeName);
        }
    }

    protected ViewNode getRootViewNode()
    {
        if (rootNode == null)
        {
            logger.errorPrintf("*** Null RootNode!\n");
        }
        return rootNode;
    }

    protected VRL getRootVRI()
    {
        return getRootViewNode().getVRL();
    }

    public void refreshRoot()
    {
        updateData();
    }

    protected StringList filterHeaders(StringList headers)
    {
        for (String name : headers.toArray())
        {
            // MetaAttribute seperators (legacy);
            if (name.startsWith("["))
            {
                headers.remove(name);
            }
        }

        return headers;
    }

    public int insertHeader(String headerName, String newName, boolean insertBefore)
    {
        int index = tableModel.insertHeader(headerName, newName, insertBefore);
        // will update data model, Table View will follow AFTER TableStructureEvent
        // has been handled.
        updateAttribute(newName);
        return index;
    }

    protected void handle(String message, Throwable t)
    {
        logger.logException(ClassLogger.ERROR, t, "Exception:%s\n", t);
        BrowserInterface masterB = getMasterBrowser();
        if (masterB != null)
        {
            masterB.handleException(message, t);
        }
        else
        {
            logger.errorPrintf("Error: %s\n", message);
            t.printStackTrace();
        }
    }

    //@Override
    public void updateColumn(String newName)
    {
        this.updateAttribute(newName);
    }

    public ViewNode[] getChilds()
    {
        try
        {
            return dataSource.getChilds(uiModel, getRootVRI(), 0, -1, null);
        }
        catch (ProxyException e)
        {
            handle("Couldn't get childs\n", e);
        }
        return null;
    }

    protected void updateRow(String rowKey)
    {
        List<String> hdrs = tableModel.getHeaders();
        updateAttributes(new String[]
        { rowKey }, hdrs);
    }

    protected void updateAttribute(String attrName)
    {
        updateAttributes(tableModel.getRowKeys(), new StringList(attrName));
    }

    protected void updateNodeAttributes(ViewNode viewNode, List<String> attrNames) throws ProxyException
    {
        List<Attribute> attrs = dataSource.getAttributes(viewNode.getVRL(), attrNames);

        RowData row = tableModel.getRow(viewNode.getVRL().toString());

        if (row == null)
        {
            return;
        }

        row.setViewNode(viewNode);
        row.setValues(attrs);
    }

    protected int createRow(ViewNode viewNode) throws ProxyException
    {
        AttributeSet set = new AttributeSet();
        return tableModel.addRow(viewNode, set);
    }

    public ProxyNode getRootProxyNode()
    {
        return this.dataSource.getRootNode();
    }

    @Override
    public void notifyVRSEvent(VRSEvent e)
    {
        // handle event and update table:
        logger.debugPrintf("notifyVRSEvent():%s\n", e);

        VRL parentVRL = e.getParent();

        // Check parent if given.
        if ((parentVRL != null) && (parentVRL.equals(this.getRootVRI()) == false))
        {
            logger.debugPrintf("VRSEvent not for me, other parent=%s\n", e);
            return; // not for me.
        }

        VRL vrls[] = e.getResources();
        VRSEventType eventType = e.getType();

        switch (eventType)
        {
            case RESOURCES_ADDED:
            {
                if (parentVRL == null)
                {
                    logger.errorPrintf("FIXME: Cannot added resource if parent is not given!\n");
                    return;
                }
                addRows(vrls, false);
                break;
            }
            case RESOURCES_DELETED:
            {
                removeRows(vrls);
                break;
            }
            case ATTRIBUTES_CHANGED:
            case REFRESH_RESOURCES:
            {
                refresh(vrls);
                break;
            }
            case RESOURCES_RENAMED:
            {
                VRL newVrls[] = e.getOtherResources();
                renameRows(vrls, newVrls);
                break;
            }
            default:
            {
                logger.errorPrintf("EventType not supported:%s\n", e);
                break;
            }
        }
    }

    protected void removeRows(VRL[] vrls)
    {
        for (VRL vrl : vrls)
        {
            this.tableModel.delRow(vrl);
        }
    }

    protected void renameRows(VRL[] oldVrls, VRL[] newVrls)
    {
        for (int i = 0; i < oldVrls.length; i++)
        {
            VRL vrl = oldVrls[i];

            if (oldVrls[i].equals(newVrls[i]))
            {
                logger.debugPrintf("Refreshing row:%s\n", oldVrls[i]);
                // update cell values.
                refreshRows(new VRL[] { oldVrls[i] });
            }
            else
            {
                logger.debugPrintf("Replacing row:%s=>%s\n", oldVrls[i], newVrls[i]);
                if (tableModel.hasRow(tableModel.createRowKey(vrl)))
                {
                    replaceRow(oldVrls[i],newVrls[i]); 
                }
                else
                {
                    logger.warnPrintf("Resource not in table (row not found):%s\n", vrl);
                }
            }
        }
    }

    private void refresh(final VRL[] vrls)
    {

        if ((vrls == null) || (vrls.length <= 0))
        {
            return;
        }

        VRL rowVRLs[] = new VRL[vrls.length];
        int rowCount = 0;

        // filter out VRLs:
        for (VRL vrl : vrls)
        {
            if (vrl.equals(this.getRootVRI()))
            {
                refreshRoot();
            }

            if (tableModel.hasRow(tableModel.createRowKey(vrl)))
            {
                rowVRLs[rowCount++] = vrl;
            }
        }

        if (rowCount > 0)
        {
            addRows(rowVRLs, true);
        }
    }

    private void refreshRows(final VRL[] vrls)
    {
        addRows(vrls, false);
    }

    // ========================================================================
    // Background Data Fetchers
    // ========================================================================

    private void updateData()
    {
        tableModel.clearData();

        // allowed at init time!
        if (dataSource == null)
        {
            logger.warnPrintf("Init: NULL dataSource!\n");
            return;
        }

        BrowserTask task = new BrowserTask(this.getTaskSource(), "Test get ProxyNode data")
        {
            public void doTask()
            {
                try
                {
                    ViewNode nodes[];

                    try
                    {
                        nodes = getChilds();
                    }
                    catch (Exception e)
                    {
                        handle("Couldn't fetch childs\n", e);
                        return;
                    }

                    // no data:
                    if (nodes == null)
                    {
                        logger.debugPrintf("No Nodes for:" + this);
                        return;
                    }

                    for (ViewNode node : nodes)
                    {
                        if (isCancelled() == true)
                        {
                            return;
                        }

                        createRow(node);
                    }

                    StringList allAttributes = new StringList();

                    for (ViewNode node : nodes)
                    {
                        if (isCancelled() == true)
                        {
                            return;
                        }
                        try
                        {
                            List<String> hdrs = tableModel.getHeaders();
                            updateNodeAttributes(node, hdrs);
                            allAttributes.add(dataSource.getAttributeNames(node.getVRL()), true);
                        }
                        catch (ProxyException e)
                        {
                            handle("Couldn't update node attributes of:" + node, e);
                        }
                    }

                    // Keep all attribute names which are actually availabl from the nodes.
                    allAttributes = filterHeaders(allAttributes);
                    tableModel.setAllAttributeNames(allAttributes);

                }
                catch (Throwable t)
                {
                    handle("Failed to fetch table data\n", t);
                    this.setException(t);
                }
            }
        };

        task.startTask();
    }

    private void updateAttributes(final String rowKeys[], final List<String> attrNames)
    {
        final ResourceTableModel model = tableModel;

        BrowserTask task = new BrowserTask(this.getTaskSource(), "updateAttributes() #rowKeys=" + rowKeys.length + ",#attrNames="
                + attrNames.size())
        {
            public void doTask()
            {
                for (String rowKey : rowKeys)
                {
                    if (this.isCancelled())
                    {
                        return;
                    }

                    ViewNode node = model.getViewNode(rowKey);

                    try
                    {
                        if (node != null)
                        {
                            updateNodeAttributes(node, attrNames);
                        }
                    }
                    catch (ProxyException e)
                    {
                        handle("Couldn't update node attributes of:" + node, e);
                    }
                }
            }
        };

        task.startTask();
    }

    private void addRows(final VRL[] vrls, final boolean mergeRows)
    {
        if (dataSource == null)
        {
            return;
        }

        BrowserTask task = new BrowserTask(this.getTaskSource(), "ProxyNodeTableUpdater:addChilds() #childs=" + vrls.length)
        {
            public void doTask()
            {
                try
                {
                    ViewNode[] nodes = dataSource.createViewNodes(uiModel, vrls);
                    
                    for (ViewNode node : nodes)
                    {
                        String rowKey = tableModel.createRowKey(node);
                        RowData rowData = tableModel.getRow(rowKey);

                        if ((rowData != null) && (mergeRows))
                        {
                            // merge
                            rowData.setViewNode(node);
                        }
                        else
                        {
                            // create/replace complete row: 
                            tableModel.addRow(node, new AttributeSet());
                        }
                    }
                    
                    updateAttributes(tableModel.createRowKeys(vrls), tableModel.getHeaders()); 
                    
                    // update row data.
                }
                catch (ProxyException e)
                {
                    handle("Failed to get new Nodes\n", e);
                }
            }

        };

        task.startTask();

    }
    
    private void replaceRow(final VRL oldVrl,final VRL newVrl)
    {
        if (dataSource == null)
        {
            return;
        }

        BrowserTask task = new BrowserTask(this.getTaskSource(), "Replacing (updating) row:"+oldVrl+"to:"+newVrl)
        {
            public void doTask()
            {
                try
                {
                    String oldKey=tableModel.createRowKey(oldVrl); 
                    String newKey=tableModel.createRowKey(newVrl);
                    
                    ViewNode[] newNodes = dataSource.createViewNodes(uiModel, new VRL[]{newVrl});
                    
                    Integer index=tableModel.getRowIndex(oldKey);
                    
                    if (index==null)
                    {
                        logger.errorPrintf("replaceRow(): couldn't find original row:"+oldVrl); 
                        return; 
                    }
                    
                    tableModel.replaceRow(index,newNodes[0],new AttributeSet()); 
                    updateAttributes(new String[]{newKey}, tableModel.getHeaders()); 
                    
                    // update row data.
                }
                catch (ProxyException e)
                {
                    handle("Failed to replace row:"+oldVrl+"=>"+newVrl, e);
                }
            }

        };

        task.startTask();

    }

}
