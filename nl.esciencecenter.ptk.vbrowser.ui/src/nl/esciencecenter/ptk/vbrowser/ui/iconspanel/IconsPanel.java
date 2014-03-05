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

package nl.esciencecenter.ptk.vbrowser.ui.iconspanel;

import java.awt.Component;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import nl.esciencecenter.ptk.object.Disposable;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.UIGlobal;
import nl.esciencecenter.ptk.vbrowser.ui.actions.KeyMappings;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.ViewNodeContainerDragListener;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.ViewNodeDropTarget;
import nl.esciencecenter.ptk.vbrowser.ui.model.DataSource;
import nl.esciencecenter.ptk.vbrowser.ui.model.UIViewModel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewContainerEventAdapter;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeContainer;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNodeDataSource;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class IconsPanel extends JPanel implements ListDataListener, ViewNodeContainer, Disposable
{
    private static final long serialVersionUID = -8822489309726132852L;

    private static ClassLogger logger;

    static
    {
        logger = ClassLogger.getLogger(IconsPanel.class);
    }

    /** IconPanel owns UIModel */
    private UIViewModel uiModel;

    private IconLayoutManager layoutManager;

    private IconsPanelController iconsPanelController;

    private IconListModel iconModel;

    private BrowserInterface masterBrowser;

    private IconsPanelUpdater iconsPanelUpdater;

    private ViewContainerEventAdapter viewComponentHandler;

    private nl.esciencecenter.ptk.vbrowser.ui.dnd.ViewNodeContainerDragListener dragListener;

    public IconsPanel(BrowserInterface browser, DataSource viewNodeSource)
    {
        init(browser, viewNodeSource);
    }

    private void init(BrowserInterface browser, DataSource dataSource)
    {
        this.masterBrowser = browser;
        this.uiModel = UIViewModel.createIconsModel(48);
        this.iconsPanelUpdater = new IconsPanelUpdater(this, dataSource);
        this.iconModel = new IconListModel();
        this.iconModel.addListDataListener(this);

        initGui();

        // add listener last:
        this.iconsPanelController = new IconsPanelController(browser, this);
        this.viewComponentHandler = new ViewContainerEventAdapter(this, iconsPanelController);
        // Mouse and Focus
        this.addMouseMotionListener(viewComponentHandler);
        this.addMouseListener(this.viewComponentHandler);
        this.addFocusListener(this.viewComponentHandler);

        this.setFocusable(true);

        initDND();
    }

    private void initDND()
    {
        // [DnD]
        // Important:
        // IconsPanel is dragsource for ALL icons it contains
        // this to support multiple selections!

        DragSource dragSource = DragSource.getDefaultDragSource();
        dragListener = new ViewNodeContainerDragListener(); // is (this) needed
                                                            // ?;
        // this.dsListener = MyDragSourceListener.getDefault();
        // component, action, listener
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dragListener);

        // IconPanel canvas can receive contents !
        this.setTransferHandler(getPlatform().getTransferHandler());

        // DnD is closely linked to the KeyMapping! (CTRL-C, CTRL-V)
        // Add Copy/Paste Menu shortcuts to this component:
        KeyMappings.addCopyPasteKeymappings(this);
        KeyMappings.addSelectionKeyMappings(this);

        // canvas is drop target:
        this.setDropTarget(new ViewNodeDropTarget(this));

    }

    public BrowserPlatform getPlatform()
    {
        return this.masterBrowser.getPlatform();
    }

    public boolean isFocusable()
    {
        return true;
    }

    private void initGui()
    {
        // layoutmanager:
        this.layoutManager = new IconLayoutManager(this.uiModel);
        this.setLayout(layoutManager);
        // this.setLayout(new FlowLayout());
        this.setBackground(uiModel.getCanvasBGColor());
    }

    public void updateUIModel(UIViewModel model)
    {
        this.uiModel = model;
        this.layoutManager.setUIModel(model);
        this.iconsPanelUpdater.updateRoot();
    }

    public IconListModel getModel()
    {
        return this.iconModel;
    }

    public UIViewModel getUIViewModel()
    {
        return this.uiModel;
    }

    public JComponent getComponent()
    {
        return this;
    }

    public IconItem getComponent(int index)
    {
        // downcast from Component:
        return (IconItem) super.getComponent(index);
    }

    /**
     * Set Root Node and update UI
     */
    public void setDataSource(ProxyNode node, boolean update)
    {
        if (node == null)
        {
            setDataSource((ProxyNodeDataSource) null, true); // reset/clear
            return;
        }

        ProxyNodeDataSource dataSource = new ProxyNodeDataSource(node);
        this.setDataSource(dataSource, update);
    }

    public void setDataSource(ProxyNodeDataSource dataSource, boolean update)
    {
        this.iconsPanelUpdater.setDataSource(dataSource, update);
    }

    @Override
    public void intervalAdded(ListDataEvent e)
    {
        logger.debugPrintf("intervalAdded():[%d,%d]\n", e.getIndex0(), e.getIndex1());
        int start = e.getIndex0();
        int end = e.getIndex1();

        uiUpdate(false, start, end);
    }

    @Override
    public void contentsChanged(ListDataEvent e)
    {
        logger.debugPrintf("contentsChanged():[%d,%d]\n", e.getIndex0(), e.getIndex1());
        updateAll();
    }

    protected void updateAll()
    {
        uiUpdate(true, 0, -1);
    }

    @Override
    public void intervalRemoved(ListDataEvent e)
    {
        logger.errorPrintf("FIXME: intervalRemoved():[%d,%d]\n", e.getIndex0(), e.getIndex1());
    }

    private void uiUpdate(final boolean clear, final int start, final int _end)
    {
        if (UIGlobal.isGuiThread() == false)
        {
            Runnable updater = new Runnable()
            {
                @Override
                public void run()
                {
                    uiUpdate(clear, start, _end);
                };
            };

            UIGlobal.swingInvokeLater(updater);
            return;
        }

        if (clear)
            this.removeAll();

        // -1 -> update all
        int end = _end;

        if (end < 0)
            end = getModel().getSize() - 1; // end is inclusive
        else
            end = _end;

        // Appends range at END of component list !
        // Assumes the range [start,end] has NOT been added yet
        for (int i = start; i <= end; i++)
        {
            IconItem comp = getModel().getElementAt(i);
            // check anyway
            if (this.hasComponent(comp) == false)
                this.addIconItem(comp);
        }

        // already UI here !
        this.revalidate();
        this.repaint();
    }

    public void uiRepaint()
    {
        if (UIGlobal.isGuiThread() == false)
        {
            Runnable updater = new Runnable()
            {
                @Override
                public void run()
                {
                    repaint();
                };
            };

            UIGlobal.swingInvokeLater(updater);
            return;
        }

        this.repaint();
    }

    public boolean hasComponent(IconItem theComp)
    {
        for (Component comp : this.getComponents())
        {
            if (comp.equals(theComp))
                return true;
        }

        return false;
    }

    @Override
    public ViewNode getViewNode()
    {
        return this.iconsPanelUpdater.getRootNode();
    }

    @Override
    public ViewNode getNodeUnderPoint(Point p)
    {
        Component comp = this.getComponentAt(p);

        if (comp instanceof IconItem)
        {
            return ((IconItem) comp).getViewNode();
        }

        // Canvas Select:
        if (comp == this)
        {
            return this.getViewNode();
        }

        return null;
    }

    @Override
    public void clearNodeSelection()
    {
        for (IconItem item : this.getIconItems())
        {
            item.setSelected(false);
        }
    }

    @Override
    public ViewNode[] getNodeSelection()
    {
        Vector<ViewNode> items = new Vector<ViewNode>();

        for (IconItem item : this.getIconItems())
        {
            if (item.isSelected())
                items.add(item.getViewNode());
        }

        ViewNode nodes[] = new ViewNode[items.size()];
        nodes = items.toArray(nodes);

        return nodes;
    }

    @Override
    public void setNodeSelection(ViewNode node, boolean isSelected)
    {
        logger.debugPrintf("updateSelection %s=%s\n", node, isSelected);
        if (node == null)
            return; // canvas click;

        IconItem item = getIconItem(node.getVRL());
        if (item != null)
            item.setSelected(isSelected);
    }

    public IconItem getIconItem(VRL locator)
    {
        for (IconItem item : this.getIconItems())
        {
            if (item.hasLocator(locator))
                return item;
        }
        return null;
    }

    public IconItem getIconItem(ViewNode node)
    {
        for (IconItem item : this.getIconItems())
        {
            if (item.hasViewNode(node))
                return item;
        }
        return null;
    }

    public IconItem[] getIconItems()
    {
        ArrayList<IconItem> itemList = new ArrayList<IconItem>();
        // filter out icon items;
        Component[] comps = this.getComponents();

        for (Component comp : comps)
            if (comp instanceof IconItem)
                itemList.add((IconItem) comp);

        IconItem items[] = new IconItem[itemList.size()];
        for (int i = 0; i < itemList.size(); i++)
            items[i] = itemList.get(i);

        return items;
    }

    @Override
    public void setNodeSelectionRange(ViewNode node1, ViewNode node2, boolean selected)
    {
        logger.debugPrintf("setSelectionRange:[%s,%s]=%s\n", node1, node2, selected);

        boolean mark = false;

        // mark range [node1,node2] (inclusive)
        // mark range [node2,node1] (inclusive)
        for (IconItem item : this.getIconItems())
        {
            boolean last = false;

            // check both directions !
            if (mark == false)
            {
                if (item.hasViewNode(node1))
                    mark = true;

                if (item.hasViewNode(node2))
                    mark = true;
            }
            else
            {
                if (item.hasViewNode(node1))
                    mark = false;

                if (item.hasViewNode(node2))
                    mark = false;

                last = true;
            }

            if (mark || last)
                item.setSelected(selected);

        }

    }

    @Override
    public JPopupMenu createNodeActionMenuFor(ViewNode node, boolean canvasMenu)
    {
        return this.masterBrowser.createActionMenuFor(this, node, canvasMenu);
    }

    public void addIconItem(IconItem item)
    {
        // add + update
        super.add(item);
        item.setViewComponentEventAdapter(this.viewComponentHandler);
    }

    @Override
    public boolean requestFocus(boolean value)
    {
        if (value == true)
            return this.requestFocusInWindow();
        return false;
    }

    @Override
    public boolean requestNodeFocus(ViewNode node, boolean value)
    {
        if (value == true)
        {
            IconItem item = this.getIconItem(node);
            // forward request to focus subsystem!
            if (item != null)
                return item.requestFocusInWindow();
        }

        return false;
    }

    @Override
    public ViewNodeContainer getViewContainer()
    {
        return null;
    }

    public BrowserInterface getMasterBrowser()
    {
        return this.masterBrowser;
    }

    public DataSource getDataSource()
    {
        return this.iconsPanelUpdater.getDataSource();
    }

    public DragGestureListener getDragGestureListener()
    {
        return this.dragListener;
    }

    @Override
    public void dispose()
    {
    }

    @Override
    public BrowserInterface getBrowserInterface()
    {
        return this.masterBrowser; 
    }
}
