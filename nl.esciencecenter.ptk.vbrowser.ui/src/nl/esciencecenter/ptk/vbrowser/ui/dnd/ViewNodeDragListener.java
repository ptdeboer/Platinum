package nl.esciencecenter.ptk.vbrowser.ui.dnd;

import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

public class ViewNodeDragListener implements DragSourceListener
{
    public ViewNodeDragListener()
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
