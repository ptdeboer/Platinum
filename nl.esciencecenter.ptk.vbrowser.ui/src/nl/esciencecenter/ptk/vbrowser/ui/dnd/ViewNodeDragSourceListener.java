package nl.esciencecenter.ptk.vbrowser.ui.dnd;

import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

/** 
 * DragSourceListener for ViewNodes. <br>
 * This way a ViewNode source can interact with Drag and Drops. 
 * ViewNodeDropTarget does most of the work when dragging and dropping, but when dragging into the local desktop, 
 * the 'drag' is out of reach of the VBrowser. 
 * 
 */
public class ViewNodeDragSourceListener implements DragSourceListener
{
    public ViewNodeDragSourceListener()
    {
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde)
    {
        DnDUtil.debugPrintf("dragSource:dragEnter():%s\n", dsde);
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde)
    {
        DnDUtil.debugPrintf("dragSource:dragOver():%s\n", dsde);
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde)
    {
        DnDUtil.debugPrintf("dragSource:dropActionChanged():%s\n", dsde);
    }

    @Override
    public void dragExit(DragSourceEvent dse)
    {
        DnDUtil.debugPrintf("dragSource:dragExit():%s\n", dse);
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde)
    {
        DnDUtil.debugPrintf("dragSource:dragDropEnd():%s\n", dsde);
    }

}
