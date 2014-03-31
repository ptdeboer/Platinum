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
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class ProxyNodeTableDataProducer implements TableDataProducer
{
    private static ClassLogger logger;

    static
    {
        logger = ClassLogger.getLogger(ProxyNodeTableDataProducer.class);
    }

    // ========================================================================
    // Instance
    // ========================================================================

    private ProxyNodeDataSource dataSource;

    private ResourceTableModel tableModel;

    private UIViewModel uiModel;

    private ViewNode rootNode;

    public ProxyNodeTableDataProducer(ProxyNode pnode,
            ResourceTableModel resourceTableModel)
    {
        this.dataSource = new ProxyNodeDataSource(pnode);
        this.tableModel = resourceTableModel;
        this.uiModel = UIViewModel.createTableModel();
    }

    public ProxyNodeTableDataProducer(ProxyNodeDataSource dataSource,
            ResourceTableModel resourceTableModel)
    {
        this.dataSource = dataSource;
        this.tableModel = resourceTableModel;
        this.uiModel = UIViewModel.createTableModel();
    }

    public void createTable(boolean headers, boolean data) throws ProxyException
    {
        this.tableModel.setRootViewNode(dataSource.getRoot(uiModel));

        if (headers)
            initHeaders();

        if (data)
            updateData();
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
                // Check default Presenation form Scheme+Type;
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
            rootNode = this.dataSource.getRoot(uiModel);

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
                headers.remove(name);
        }

        return headers;
    }

    public int insertHeader(String headerName, String newName, boolean insertBefore)
    {
        int index = tableModel.insertHeader(headerName, newName, insertBefore);
        // will update data model, Table View will follow AFTER TableStructureEvent
        // has been handled.
        fetchAttribute(newName);
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
        this.fetchAttribute(newName);
    }

    // ========================================================================
    // Background Data Fetchers
    // ========================================================================

    private void updateData()
    {
        tableModel.clearData();

        // allowed at init time!
        if (dataSource == null)
            return;

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
                            return;
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

    protected void fetchAttribute(String attrName)
    {
        fetchAttributes(new StringList(attrName));
    }

    private void fetchAttributes(final List<String> attrNames)
    {
        if (dataSource == null)
            return;

        final ResourceTableModel model = tableModel;

        UITask task = new UITask(null, "Test get ProxyNode data")
        {
            boolean mustStop = false;

            public void doTask()
            {
                //
                // Iterate over current Rows
                //
                final String[] keys = model.getRowKeys();

                for (String rowKey : keys)
                {
                    if (mustStop == true)
                        return;

                    ViewNode node = model.getViewNode(rowKey);

                    try
                    {
                        if (node != null)
                            updateNodeAttributes(node, attrNames);
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
            return;
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

}
