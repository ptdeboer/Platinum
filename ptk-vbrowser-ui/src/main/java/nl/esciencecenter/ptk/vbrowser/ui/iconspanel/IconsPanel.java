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

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.object.Disposable;
import nl.esciencecenter.ptk.vbrowser.ui.UIGlobal;
import nl.esciencecenter.ptk.vbrowser.ui.actions.KeyMappings;
import nl.esciencecenter.ptk.vbrowser.ui.actions.UIAction;
import nl.esciencecenter.ptk.vbrowser.ui.actions.UIActionListener;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.ViewNodeContainerDragListener;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.ViewNodeDropTarget;
import nl.esciencecenter.ptk.vbrowser.ui.model.*;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNodeDataSourceProvider;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class IconsPanel extends JPanel implements ListDataListener, ViewNodeContainer, Disposable, UIActionListener {

    /**
     * private UIModel for this icon panel.
     */
    private UIViewModel uiModel;
    private IconLayoutManager layoutManager;
    private IconsPanelController iconsPanelController;

    private IconListModel iconModel;
    private BrowserInterface masterBrowser;

    private IconsPanelUpdater iconsPanelUpdater;
    private ViewContainerEventAdapter viewComponentHandler;
    private ViewNodeContainerDragListener dragListener;

    public IconsPanel(BrowserInterface browser, ProxyDataSource viewNodeSource) {
        init(browser, viewNodeSource);
    }

    private void init(BrowserInterface browser, ProxyDataSource dataSource) {
        this.masterBrowser = browser;
        this.uiModel = UIViewModel.createIconsModel(48);
        this.iconsPanelUpdater = new IconsPanelUpdater(this, dataSource);
        this.iconModel = new IconListModel();
        this.iconModel.addListDataListener(this);

        initGui();

        // Add listeners last:
        this.iconsPanelController = new IconsPanelController(browser, this);
        this.viewComponentHandler = new ViewContainerEventAdapter(this, iconsPanelController);
        // Mouse and Focus
        MouseAndFocusFollower mouseFocusListener = new MouseAndFocusFollower();
        this.addFocusListener(mouseFocusListener);
        this.addMouseListener(mouseFocusListener);
        // Generic Event Handler
        this.addMouseListener(this.viewComponentHandler);

        // Selection and action key mappings.
        KeyMappings.addSelectionKeyMappings(this, true);
        KeyMappings.addMovementKeyMappings(this, true);
        KeyMappings.addActionKeyMappings(this, true);

        this.setFocusable(true);
        initDND();
    }

    private void initDND() {
        // [DnD]
        // Important:
        // IconsPanel is dragsource for ALL icons it contains
        // this to support multiple selections!

        DragSource dragSource = DragSource.getDefaultDragSource();
        dragListener = new ViewNodeContainerDragListener(); // is (this) needed
        // ?;
        // this.dsListener = MyDragSourceListener.fsutil();
        // component, action, listener
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE,
                dragListener);

        // IconPanel canvas can receive contents !
        this.setTransferHandler(getPlatform().getTransferHandler());

        // DnD is closely linked to the KeyMapping! (CTRL-C, CTRL-V)
        // Add Copy/Paste Menu shortcuts to this component:
        KeyMappings.addCopyPasteKeymappings(this);

        // canvas is drop target:
        this.setDropTarget(new ViewNodeDropTarget(this));

    }

    public BrowserPlatform getPlatform() {
        return this.masterBrowser.getPlatform();
    }

    public boolean isFocusable() {
        return true;
    }

    private void initGui() {


        // layoutmanager:
        this.layoutManager = new IconLayoutManager(this.uiModel);
        this.setLayout(layoutManager);
        // this.setLayout(new FlowLayout());
        this.setBackground(uiModel.getCanvasBGColor());
    }

    public void updateUIModel(UIViewModel model) {
        this.uiModel = model;
        this.layoutManager.setUIModel(model);
        this.iconsPanelUpdater.updateRoot();
    }

    public IconListModel getModel() {
        return this.iconModel;
    }

    public UIViewModel getUIViewModel() {
        return this.uiModel;
    }

    public JComponent getComponent() {
        return this;
    }

    public IconItem getComponent(int index) {
        // downcast from Component:
        return (IconItem) super.getComponent(index);
    }

    /**
     * Set Root Node and update UI
     */
    public void setDataSource(ProxyNode node, boolean update) {
        if (node == null) {
            setDataSource((ProxyNodeDataSourceProvider) null, true); // reset/clear
            return;
        }

        ProxyNodeDataSourceProvider dataSource = new ProxyNodeDataSourceProvider(node);
        this.setDataSource(dataSource, update);
    }

    public void setDataSource(ProxyNodeDataSourceProvider dataSource, boolean update) {
        this.iconsPanelUpdater.setDataSource(dataSource, update);
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
        log.debug("intervalAdded():[{},{}]", e.getIndex0(), e.getIndex1());
        int start = e.getIndex0();
        int end = e.getIndex1();

        uiUpdate(false, start, end);
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        log.debug("contentsChanged():[{},{}]", e.getIndex0(), e.getIndex1());
        updateAll();
    }

    protected void updateAll() {
        uiUpdate(true, 0, -1);
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        log.debug("intervalRemoved():[{},{}]", e.getIndex0(), e.getIndex1());

        int start = e.getIndex0(); // inclusive start
        int end = e.getIndex1(); // inclusive end

        for (int i = start; i <= end; i++) {
            delete(i);
        }
    }

    protected void delete(int index) {
        log.debug("delete #{}", index);
        // remove and leave empty spot:
        Component comp = this.getComponent(index);
        this.remove(comp);
        comp.setEnabled(false);
        comp.setVisible(false);
        this.repaint();
    }

    private void uiUpdate(final boolean clear, final int start, final int _end) {
        if (UIGlobal.isGuiThread() == false) {
            Runnable updater = new Runnable() {
                @Override
                public void run() {
                    uiUpdate(clear, start, _end);
                }

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
        for (int i = start; i <= end; i++) {
            IconItem comp = getModel().getElementAt(i);
            // check anyway
            if (this.hasComponent(comp) == false)
                this.addIconItem(comp);
        }

        // already UI here !
        this.revalidate();
        this.repaint();
    }

    public void uiRepaint() {
        if (UIGlobal.isGuiThread() == false) {
            Runnable updater = new Runnable() {
                @Override
                public void run() {
                    repaint();
                }

            };

            UIGlobal.swingInvokeLater(updater);
            return;
        }

        this.repaint();
    }

    public boolean hasComponent(IconItem theComp) {
        for (Component comp : this.getComponents()) {
            if (comp.equals(theComp))
                return true;
        }

        return false;
    }

    @Override
    public ViewNode getViewNode() {
        return this.iconsPanelUpdater.getRootNode();
    }

    @Override
    public ViewNode getNodeUnderPoint(Point p) {
        Component comp = this.getComponentAt(p);

        if (comp instanceof IconItem) {
            return ((IconItem) comp).getViewNode();
        }

        // Canvas Select:
        if (comp == this) {
            return this.getViewNode();
        }

        return null;
    }

    @Override
    public void clearNodeSelection() {
        for (IconItem item : this.getIconItems()) {
            item.setSelected(false);
        }
    }

    @Override
    public List<ViewNode> getNodeSelection() {
        ArrayList<ViewNode> items = new ArrayList();

        for (IconItem item : this.getIconItems()) {
            if (item.isSelected())
                items.add(item.getViewNode());
        }
        return items;
    }

    @Override
    public void setNodeSelection(ViewNode node, boolean isSelected) {
        log.debug("updateSelection {}={}", node, isSelected);
        if (node == null)
            return; // canvas click;

        IconItem item = getIconItem(node.getVRL());
        if (item != null)
            item.setSelected(isSelected);
    }

    public IconItem getIconItem(VRL locator) {
        for (IconItem item : this.getIconItems()) {
            if (item.hasLocator(locator))
                return item;
        }
        return null;
    }

    public IconItem getIconItem(ViewNode node) {
        for (IconItem item : this.getIconItems()) {
            if (item.hasViewNode(node))
                return item;
        }
        return null;
    }

    public List<IconItem> getIconItems() {
        ArrayList<IconItem> itemList = new ArrayList<IconItem>();
        // filter out icon items;
        Component[] comps = this.getComponents();

        for (Component comp : comps)
            if (comp instanceof IconItem)
                itemList.add((IconItem) comp);

        return itemList;
    }

    @Override
    public void setNodeSelectionRange(ViewNode node1, ViewNode node2, boolean selected) {
        log.debug("setSelectionRange:[{},{}]={}", node1, node2, selected);

        boolean mark = false;

        // mark range [node1,node2] (inclusive)
        // mark range [node2,node1] (inclusive)
        for (IconItem item : this.getIconItems()) {
            boolean last = false;

            // check both directions !
            if (mark == false) {
                if (item.hasViewNode(node1))
                    mark = true;

                if (item.hasViewNode(node2))
                    mark = true;
            } else {
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
    public JPopupMenu createNodeActionMenuFor(ViewNode node, boolean canvasMenu) {
        return this.masterBrowser.createActionMenuFor(this, node, canvasMenu);
    }

    public void addIconItem(IconItem item) {
        // add + update
        super.add(item);
        item.setViewComponentEventAdapter(this.viewComponentHandler);
    }

    @Override
    public boolean requestFocus(boolean value) {
        if (value == true)
            return this.requestFocusInWindow();
        return false;
    }

    @Override
    public ViewNodeContainer getViewContainer() {
        return null;
    }

    public BrowserInterface getMasterBrowser() {
        return this.masterBrowser;
    }

    public ProxyDataSource getDataSource() {
        return this.iconsPanelUpdater.getDataSource();
    }

    public DragGestureListener getDragGestureListener() {
        return this.dragListener;
    }

    @Override
    public void dispose() {
    }

    @Override
    public BrowserInterface getBrowserInterface() {
        return this.masterBrowser;
    }

    @Override
    public Rectangle findBoundsOfSelectionNode(ViewNode node) {
        IconItem item = this.getIconItem(node);
        if (item == null) {
            return null;
        }
        return item.getBounds();
    }

    @Override
    public JComponent getJComponent() {
        return this;
    }

    @Override
    public void uiActionPerformed(UIAction action, ActionEvent event) {
        if (performMoveActions(action)) {
            return;
        }
        // Delegate. need better api.
        this.viewComponentHandler.uiActionPerformed(action, event);
    }

    private boolean performMoveActions(UIAction action) {
        if (action == KeyMappings.LEFT) {
            moveSelection(-1, 0);
            return true;
        } else if (action == KeyMappings.RIGHT) {
            moveSelection(+1, 0);
            return true;
        } else if (action == KeyMappings.UP) {
            moveSelection(0, -1);
            return true;
        } else if (action == KeyMappings.DOWN) {
            moveSelection(0, +1);
            return true;
        }
        return false;
    }

    private void moveSelection(int dx, int dy) {
        log.debug("MoveFocus: {},{}", dx, dy);
        IconItem item = findIconWithFocus();
        // unselect
        log.debug("has focus={}", item);

        int row = item.getRow();
        int col = item.getColumn();
        int newRow = row + dy;
        int newCol = col + dx;
        IconItem nextItem = findIconByRowCol(newRow, newCol);

        if (nextItem != null) {
            nextItem.requestFocusInWindow();
            this.clearNodeSelection();
            this.setNodeSelection(nextItem.getViewNode(), true);
        } else {
            log.debug("Row/Col out of bounds.");
        }
    }

    private IconItem findIconByRowCol(int newRow, int newCol) {
        for (IconItem item : this.getIconItems()) {
            if (item.hasRowCol(newRow, newCol)) {
                return item;
            }
        }
        return null;
    }


    private IconItem findIconWithFocus() {
        for (IconItem icon : this.getIconItems()) {
            if (icon.hasFocus()) {
                return icon;
            }
        }
        return null;
    }

    public void updateFocus(boolean value) {
        log.debug("Set Border:{}", value);
        if (value) {
            setBorder(BorderFactory.createLineBorder(getPlatform().getGuiSettings().getFocusBorderColor()));
        } else {
            setBorder(null);
        }
    }
}
