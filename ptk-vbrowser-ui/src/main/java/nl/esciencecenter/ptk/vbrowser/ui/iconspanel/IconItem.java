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
import nl.esciencecenter.ptk.ui.fonts.FontInfo;
import nl.esciencecenter.ptk.vbrowser.ui.actions.KeyMappings;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.ViewNodeDragSourceListener;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.ViewNodeDropTarget;
import nl.esciencecenter.ptk.vbrowser.ui.model.*;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;

/**
 * Icon Item. Combines Icon with a label.
 */
@Slf4j
public class IconItem extends JLabel implements ViewNodeComponent {

    private FontInfo fontInfo;
    private int max_icon_width;
    private ViewNode viewNode;
    private boolean selected;
    private UIViewModel uiModel;
    private int row;
    private int column;

    public IconItem(ViewNodeContainer parent, UIViewModel uiModel, ViewNode item) {
        init(parent, uiModel, item);
    }

    private void init(ViewNodeContainer parent, UIViewModel uiModel, ViewNode node) {
        this.viewNode = node;
        this.uiModel = uiModel;
        this.max_icon_width = uiModel.getMaxIconLabelWidth();
        this.setIcon(viewNode.getIcon());
        updateLabelText(viewNode.getName(), false);
        //
        boolean visible = true;

        // Label Font + Text
        {
            // move to UIModel ?
            fontInfo = FontInfo.getFontInfo(FontInfo.FONT_ICON_LABEL);
            setForeground(this.fontInfo.getForeground());
            this.setFont(fontInfo.createFont());
        }

        // Label placement:
        if (uiModel.getIconLabelPlacement() == UIViewModel.UIDirection.VERTICAL) {
            if (visible) {
                this.setIconTextGap(8);
            } else {
                this.setIconTextGap(4);
            }

            this.setVerticalAlignment(JLabel.TOP);
            this.setHorizontalAlignment(JLabel.CENTER);
            this.setVerticalTextPosition(JLabel.BOTTOM);
            this.setHorizontalTextPosition(JLabel.CENTER);
        } else {
            this.setIconTextGap(4);
            this.setVerticalAlignment(JLabel.CENTER);
            this.setHorizontalAlignment(JLabel.LEFT);
            this.setVerticalTextPosition(JLabel.CENTER);
            this.setHorizontalTextPosition(JLabel.RIGHT);
        }

        // === Listeners ===
        this.setFocusable(true);
        // handle own focus events.
        MouseAndFocusFollower mouseFocusListener = new MouseAndFocusFollower();
        this.addFocusListener(mouseFocusListener);
        this.addMouseListener(mouseFocusListener);
    }

    public void setViewComponentEventAdapter(ViewContainerEventAdapter handler) {
        // IconItem handles focus event, VCEAdaptor handles mouse events (?)
        // this.addFocusListener(handler);
        this.addMouseListener(handler);
        //this.addKeyListener(handler);
    }

    protected void initDND(TransferHandler transferHandler, DragGestureListener dragListener) {
        // One For All: Transfer Handler:
        // icon.setTransferHandler(VTransferHandler.fsutil());

        // reuse draglistener from iconsPanel:
        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE,
                dragListener);

        dragSource.addDragSourceListener(new ViewNodeDragSourceListener());

        // Specify DROP target:
        this.setDropTarget(new ViewNodeDropTarget(this));
        // Have to set Keymapping to my component
        KeyMappings.addCopyPasteKeymappings(this);

        this.setTransferHandler(transferHandler);
    }

    public void updateLabelText(String text, boolean hasFocus) {
        if (text == null)
            text = "";

        String htmlText = "<html>";

        if (hasFocus)
            htmlText += "<u>";

        // from model ?
        if (max_icon_width <= 0)
            this.max_icon_width = 180;

        // Calculate Font Size
        Font font = getFont();
        FontMetrics fmetric = getFontMetrics(font);

        // get 'widest'character
        int charWidth = fmetric.charWidth('w'); // leniance
        // System.err.println("charwidth="+charWidth);

        // int lines=0;
        int len = text.length();
        // int width=0;
        int i = 0;

        int currentLineWidth = 0;

        while (i < len) {
            switch (text.charAt(i)) {
                // Filter out special HTML characters:
                // Only a small set needs to be filtered for now:
                case '/':
                case '!':
                case '@':
                case '#':
                case '$':
                case '%':
                case '^':
                case '&':
                case '*':
                case '<':
                case '>':
                case '-':
                    // use numerical ASCII value:
                    String str = "&#" + (int) text.charAt(i) + ";";
                    htmlText += str;
                    break;
                default:
                    htmlText += text.charAt(i);
                    break;
            }

            currentLineWidth += charWidth;
            if (currentLineWidth > max_icon_width) {
                htmlText += "<br>"; // Hard Break!
                currentLineWidth = 0;
            }

            i++;
        }

        if (hasFocus)
            htmlText += "</u>";

        htmlText += "</html>";

        setText(htmlText);

    }

    public ViewNode getViewNode() {
        return this.viewNode;
    }

    public boolean hasLocator(VRL locator) {
        return this.viewNode.getVRL().equals(locator);
    }

    public void updateFocus(boolean hasFocus) {
        log.debug("Update focus:{}:{}", viewNode.getName(), hasFocus);
        updateIcon(selected, hasFocus);
        updateLabelText(viewNode.getName(), hasFocus);
        // updateFocusBorder(hasFocus);
        repaint();
    }

    @Override
    public UIViewModel getUIViewModel() {
        return this.uiModel;
    }

    public boolean hasViewNode(ViewNode node) {
        return this.viewNode.equals(node);
    }

    @Override
    public ViewNodeContainer getViewContainer() {
        // parent MUST be IconPanel;
        Container parent = this.getParent();
        return (IconsPanel) parent;
    }

    public void setSelected(boolean val) {
        this.selected = val;
        updateIcon(selected, false);
        this.repaint();
    }

    protected void updateIcon(boolean isSelected, boolean hasFocus) {
        if (isSelected) {
            if (hasFocus) {
                setIcon(viewNode.getIcon(ViewNode.SELECTED_FOCUS_ICON));
                // textLabel.setOpaque(true);
                // prev_label_color=textLabel.getForeground();
                setForeground(uiModel.getFontHighlightColor());
            } else {
                setIcon(viewNode.getIcon(ViewNode.SELECTED_ICON));
                // textLabel.setOpaque(true);
                // prev_label_color=textLabel.getForeground();
                setForeground(uiModel.getFontHighlightColor());
            }
        } else {
            if (hasFocus) {
                setIcon(viewNode.getIcon(ViewNode.FOCUS_ICON));
                // textLabel.setOpaque(false);
                setForeground(uiModel.getFontHighlightColor());
            } else {
                setIcon(viewNode.getIcon());
                // textLabel.setOpaque(false);
                setForeground(uiModel.getFontColor());
            }
        }
    }

    public boolean isSelected() {
        return this.selected;
    }

    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return size;
    }

    public void setRowColumn(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public boolean hasRowCol(int row2, int col2) {
        return ((row == row2) && (column == col2));
    }

}
