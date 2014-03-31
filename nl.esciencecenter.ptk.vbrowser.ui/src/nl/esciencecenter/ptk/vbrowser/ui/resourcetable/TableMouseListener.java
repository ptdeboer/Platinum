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

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.GuiSettings;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewContainerEventAdapter;

/** 
 * Extends default ViewContianerEventAdapter with Header clicks and other Table specific events. 
 */
public class TableMouseListener extends ViewContainerEventAdapter
{
    private final static ClassLogger logger = ClassLogger.getLogger(TableMouseListener.class);

    private ResourceTable table;

    public TableMouseListener(ResourceTable source, ResourceTableControler controller)
    {
        super(source, controller);
        table = source;
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        // ------------------
        // Check Header click
        // ------------------

        logger.errorPrintf("Event:%s\n", e);
        if (isHeader(e) && (table.getGuiSettings().isSelection(e)))
        {
            // Header Click:
            String name = this.getColumnNameOf(e);
            if (name != null)
            {
                String prevCol = this.table.getSortColumnName();
                boolean reverse = false;

                // click on already sorted column name -> reverse sorting.
                if (StringUtil.compare(prevCol, name) == 0)
                {
                    reverse = (table.getColumnSortOrderIsReversed() == false);
                }
                this.table.doSortColumn(name, reverse);
            }
        }
        else
        {
            super.doMouseClicked(e);
        }
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        // ------------------
        // Check Header click
        // ------------------

        Component comp = (Component) e.getSource();
        Point clickPoint = e.getPoint();

        GuiSettings uiSettings = table.getGuiSettings();

        // boolean ctrl=((e.getModifiersEx() & e.CTRL_DOWN_MASK) !=0);

        // Show Header Popup!
        if (isHeader(e) && (uiSettings.isPopupTrigger(e)))
        {
            String name = this.getColumnNameOf(e);
            if (name != null)
            {
                HeaderPopupMenu popupMenu = new HeaderPopupMenu(table, name);
                popupMenu.show(comp, e.getX(), e.getY());
            }
            else
            {
                // debug("No Column Header name!:"+e);
            }
        }
        else if (comp.equals(table))
        {
            super.doMousePressed(e);
            // ViewNode actionSourceNode=this.getViewNodeAt(e.getX(),e.getY());
            //
            // if (uiSettings.isPopupTrigger(e))
            // {
            // JPopupMenu popMenu = this.table.getBrowserInterface().createActionMenuFor(table, actionSourceNode,
            // false);
            // if (popMenu!=null)
            // {
            // popMenu.show(comp,e.getX(),e.getY());
            // }
            // }
        }
        else if (comp.equals(table.getParent()))
        {
            // super.doMousePressed(e);
            // ViewNode containerNode = table.getViewNode();
            // if (uiSettings.isPopupTrigger(e))
            // {
            // JPopupMenu popMenu = this.table.getBrowserInterface().createActionMenuFor(table, containerNode, true);
            // if (popMenu!=null)
            // {
            // popMenu.show(comp,e.getX(),e.getY());
            // }
            //
            // }
        }
    }

    private String getColumnNameOf(MouseEvent e)
    {
        // Warning: Apply coordinates to VIEW model !
        TableColumnModel columnModel = table.getColumnModel();

        int colnr = columnModel.getColumnIndexAtX(e.getX());

        if (colnr < 0)
            return null;

        TableColumn column = columnModel.getColumn(colnr);
        String name = (String) column.getHeaderValue();
        return name;
    }

    protected boolean isHeader(MouseEvent e)
    {
        if (e.getSource() instanceof JTableHeader)
            return true;
        else
            return false;
    }

}
