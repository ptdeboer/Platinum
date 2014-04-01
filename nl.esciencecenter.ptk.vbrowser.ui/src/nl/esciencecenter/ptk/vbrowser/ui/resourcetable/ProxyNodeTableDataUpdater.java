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
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.model.UIViewModel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNodeDataSource;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTableModel.RowData;
import nl.esciencecenter.ptk.vbrowser.ui.tasks.UITask;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.event.VRSEvent;
import nl.esciencecenter.vbrowser.vrs.event.VRSEvent.VRSEventType;
import nl.esciencecenter.vbrowser.vrs.event.VRSEventListener;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class ProxyNodeTableDataUpdater implements TableDataProducer, VRSEventListener
{
    private static ClassLogger logger;

    static
    {
        logger = ClassLogger.getLogger(ProxyNodeTableDataUpdater.class);
        logger.setLevelToDebug();
    }

    // ========================================================================
    // Instance
    // ========================================================================

    private ProxyNodeDataSource dataSource;

    private ResourceTableModel tableModel;

    private UIViewModel uiModel;

    private ViewNode rootNode;

    public ProxyNodeTableDataUpdater(ProxyNode pnode,
            ResourceTableModel resourceTableModel)
    {
        init(new ProxyNodeDataSource(pnode), resourceTableModel);
    }

    public ProxyNodeTableDataUpdater(ProxyNodeDataSource dataSource,
            ResourceTableModel resourceTableModel)
    {
        init(dataSource, resourceTableModel);
    }

    protected void init(ProxyNodeDataSource nodeDataSource, ResourceTableModel resourceTableModel)
    {
        this.dataSource = nodeDataSource;
        this.tableModel = resourceTableModel;
        this.uiModel = UIViewModel.createTableModel();
        // receice events
        this.dataSource.addViewNodeEventListener(this);
    }

    public void createTable(boolean headers, boolean data) throws ProxyException
    {
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
            handle(e, "Couldn't get presentation\n");
        }

        return pres;
    }

    public void initHeaders() throws ProxyException
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

    protected ViewNode getRootViewNode() throws ProxyException
    {
        if (rootNode == null)
        {
            rootNode = this.dataSource.getRoot(uiModel);
        }

        return rootNode;
    }

    private VRL getRootVRI() throws ProxyException
    {
        return getRootViewNode().getVRL();
    }

    private StringList filterHeaders(StringList headers)
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

    protected void handle(Throwable t)
    {
        logger.logException(ClassLogger.ERROR, t, "Exception:%s\n", t);
        t.printStackTrace();
    }

    protected void handle(Throwable t, String format, Object... args)
    {
        logger.logException(ClassLogger.ERROR, t, format, args);
        t.printStackTrace();
    }

    @Override
    public void updateColumn(String newName)
    {
        this.updateAttribute(newName);
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
            return;
        }

        UITask task = new UITask(null, "Test get ProxyNode data")
        {
            boolean mustStop = false;

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
                        handle(e, "Couldn't fetch childs\n");
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
                        if (mustStop == true)
                            return;

                        createRow(node);
                    }

                    StringList allAttributes = new StringList();

                    for (ViewNode node : nodes)
                    {
                        if (mustStop == true)
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
                            handle(e, "Couldn't update node attributes of:" + node);
                        }
                    }

                    // Keep all attribute names which are actually availabl from the nodes.
                    allAttributes = filterHeaders(allAttributes);
                    tableModel.setAllAttributeNames(allAttributes);

                }
                catch (Throwable t)
                {
                    handle(t, "Failed to fetch table data\n");
                }
            }

            public void stopTask()
            {
            }
        };

        task.startTask();
    }

    public ViewNode[] getChilds()
    {
        try
        {
            return dataSource.getChilds(uiModel, getRootVRI(), 0, -1, null);
        }
        catch (ProxyException e)
        {
            handle(e, "Couldn't get childs\n");
        }
        return null;
    }

    protected void updateRow(String rowKey)
    {
        List<String> hdrs = tableModel.getHeaders();
        updateAttributes(new String[] { rowKey }, hdrs);
    }

    protected void updateAttribute(String attrName)
    {
        updateAttributes(tableModel.getRowKeys(), new StringList(attrName));
    }

    private void updateAttributes(final String rowKeys[], final List<String> attrNames)
    {
        final ResourceTableModel model = tableModel;

        UITask task = new UITask(null, "updateAttributes() #rowKeys=" + rowKeys.length + ",#attrNames=" + attrNames.size())
        {
            boolean mustStop = false;

            public void doTask()
            {
                for (String rowKey : rowKeys)
                {
                    if (mustStop == true)
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
                        handle(e, "Couldn't update node attributes of:" + node);
                    }
                }
            }

            public void stopTask()
            {
                this.mustStop = true;
            }
        };

        task.startTask();
    }

    private void updateNodeAttributes(ViewNode viewNode, List<String> attrNames) throws ProxyException
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

    private int createRow(ViewNode viewNode) throws ProxyException
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
        logger.errorPrintf("VRSEVent:%s\n", e);

        VRL targetVRL = e.getParent();

        try
        {
            // Check parent if given.
            if ((targetVRL != null) && (targetVRL.equals(this.getRootVRI()) == false))
            {
                logger.debugPrintf("VRSEvent not for me:%s\n", e);
                return; // not for me.
            }
        }
        catch (ProxyException ex)
        {
            this.handle(ex, "Failed to get root VRL\n");
            return;
        }

        VRL vrls[] = e.getResources();
        VRSEventType eventType = e.getType();

        switch (eventType)
        {
            case RESOURCES_ADDED:
            {
                addRows(vrls);
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
                refreshRows(vrls);
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

    private void removeRows(VRL[] vrls)
    {
        for (VRL vrl : vrls)
        {
            this.tableModel.delRow(vrl);
        }
    }

    private void renameRows(VRL[] oldVrls, VRL[] newVrls)
    {
        for (int i = 0; i < oldVrls.length; i++)
        {
            if (oldVrls[i].equals(newVrls[i]))
            {
                logger.debugPrintf("Refreshing row:%s\n", oldVrls[i]);
                // update cell values.
                refreshRows(new VRL[]
                { oldVrls[i] });
            }
            else
            {
                logger.debugPrintf("Replacing row:%s=>%s\n", oldVrls[i], newVrls[i]);
                removeRows(new VRL[]
                { oldVrls[i] });
                addRows(new VRL[]
                { newVrls[i] });
            }
        }
    }

    private void refreshRows(final VRL[] vrls)
    {
        addRows(vrls);
    }

    private void addRows(final VRL[] vrls)
    {
        if (dataSource == null)
        {
            return;
        }

        UITask task = new UITask(null, "ProxyNodeTableUpdater:addChilds() #childs=" + vrls.length)
        {
            boolean mustStop = false;

            public void doTask()
            {
                try
                {
                    ViewNode[] nodes = dataSource.createViewNodes(uiModel, vrls);

                    for (ViewNode node : nodes)
                    {
                        tableModel.addRow(node, new AttributeSet());
                    }
                    // update row data.
                }
                catch (ProxyException e)
                {
                    handle(e, "Failed to get new Nodes\n");
                }
            }

            public void stopTask()
            {
                this.mustStop = true;
            }
        };

        task.startTask();

    }

}
