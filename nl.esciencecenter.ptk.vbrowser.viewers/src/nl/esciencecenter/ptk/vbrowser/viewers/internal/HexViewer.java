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

package nl.esciencecenter.ptk.vbrowser.viewers.internal;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.dnd.DropTarget;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import net.sf.jmimemagic.MagicMatchNotFoundException;
import nl.esciencecenter.ptk.data.HashMapList;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.io.RandomReadable;
import nl.esciencecenter.ptk.task.ActionTask;
import nl.esciencecenter.ptk.ui.fonts.FontInfo;
import nl.esciencecenter.ptk.ui.fonts.FontToolBar;
import nl.esciencecenter.ptk.ui.fonts.FontToolbarListener;
import nl.esciencecenter.ptk.ui.widgets.URIDropHandler;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.EmbeddedViewer;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.mimetypes.MimeTypes;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Implementation of a simple Binary Hex Viewer.<br>
 * Show the contents of in hexidecimal form
 * 
 * @author Piter.NL.
 */
public class HexViewer extends EmbeddedViewer implements FontToolbarListener// , ToolPlugin
{
    private static final ClassLogger logger = ClassLogger.getLogger(HexViewer.class);

    // todo: UTF-8 Char Mapping
    public final String specialCharMapping[] =
    {
            "�", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", // 00 - 0F
            "", "", "", "\u240d", "", "", "", "", "", "", "", "", "", "", "", "", // 10-1F
            "", "A", "B", "C", "D", "E", "F", "G", "", "", "", "", "", "", "", "", // 20
            "H", "I", "J", "K", "L", "M", "N", "O", "", "", "", "", "", "", "", "", // 30
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", // 40
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", // 50
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", // 60
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", // 70
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", // 90
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", // a0
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", // b0
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", // c0
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", // d0
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", // e0
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", // f0
    };

    /** unicode for Carriage return C/R */

    public final static String CHAR_CR = "\u240d";

    // public final static String CHAR_TAB = "\u240d";

    /**
     * Needed by swing
     */
    private static final long serialVersionUID = 4959020834564707156L;

    /** The mimetypes I can view */
    private static String mimeTypes[] = { "application/octet-stream", };

    static private boolean default_show_font_toolbar = false;

    public static enum UTFType
    {
        UTF8, UTF16
    };

    public static class UTFDecoder
    {
        public UTFDecoder()
        {
        };

        public UTFDecoder(UTFType type)
        {
        };
    }

    // =======================================================================
    //
    // =======================================================================

    private long offset;

    private long fileOffset; // start of current buffer in file !

    private int maxBufferSize = 1 * 1024 * 1024;

    private byte buffer[] = new byte[0];

    private long length;

    private int wordSize = 2;

    /** Actual bytes per line (nrBytesPerLine) is nrWordPerLine*wordSize */
    private int minimumBytesPerLine = 32;
    
    // ================================
    // Derived/Secondary fields:
    // ================================

    protected int nrBytesPerLine;

    protected int nrWordsPerLine;

    protected int maxRows;

    protected int nrBytesPerView;

    protected long scrollMutiplier = 1;

    protected RandomReadable reader = null;

    // === pacakge protected GUI components ===
    
    protected JTextField offsetField;

    // === GUI components === //

    private HexViewController hexViewController;
    private FontToolBar fontToolBar;

    // === fiels === //
    private JTextArea textArea = null;
    private JToolBar toolBar;
    private JPanel mainPanel;
    private JScrollBar scrollbar;
    private JLabel offsetLabel;
    private JLabel lengthLabel;
    private JTextField lengthField;
    private JLabel magicLabel;
    private JTextField magicField;
    private JPanel toolPanel;
    private JLabel encodingLabel;
    private JTextField encodingField;

    // tasks // 
    
    private ActionTask updateTask;

    public void initGui()
    {
        this.hexViewController = new HexViewController(this);

        {
            this.setLayout(new BorderLayout());
            this.setPreferredSize(new Dimension(800, 600));
        }
        {
            toolPanel = new JPanel();
            toolPanel.setLayout(new FlowLayout());
            this.add(toolPanel, BorderLayout.NORTH);

            // ToolBAr
            {
                toolBar = new JToolBar();
                toolBar.setLayout(new FlowLayout(FlowLayout.LEFT));
                toolPanel.add(toolBar);
                {
                    offsetLabel = new JLabel("Offset:");
                    toolBar.add(offsetLabel);
                }
                {
                    offsetField = new JTextField("0");
                    toolBar.add(offsetField);
                    offsetField.addActionListener(this.hexViewController);
                    // offsetField.setMinimumSize(new Dimension(200,30));
                }
                {
                    lengthLabel = new JLabel("Size:");
                    toolBar.add(lengthLabel);
                }
                {
                    lengthField = new JTextField("?");
                    toolBar.add(lengthField);
                    lengthField.setEditable(false);

                }
                {
                    magicLabel = new JLabel("MagicType:");
                    toolBar.add(magicLabel);
                }
                {
                    magicField = new JTextField("?");
                    toolBar.add(magicField);
                    magicField.setEditable(false);
                    magicField.setSize(120, 32);

                }
                {
                    encodingLabel = new JLabel("Encoding:");
                    toolBar.add(encodingLabel);
                }
                {
                    encodingField = new JTextField("plain");
                    toolBar.add(encodingField);
                    encodingField.setEditable(false);
                    encodingField.setSize(120, 32);

                }
            }
            // FontToolBar
            {
                fontToolBar = new FontToolBar(this);
                toolPanel.add(fontToolBar);
                fontToolBar.setFocusable(false);
                fontToolBar.setVisible(default_show_font_toolbar);
            }
        }

        {
            mainPanel = new JPanel();
            this.add(mainPanel, BorderLayout.CENTER);
            mainPanel.setLayout(new BorderLayout());

            // TextArea
            {
                textArea = new JTextArea();

                mainPanel.add(textArea, BorderLayout.CENTER);

                textArea.setText("Initializing HexViewer...");
                // get default monospaced font style;

                textArea.setEditable(false);
                textArea.addKeyListener(hexViewController);
            }
            {
                scrollbar = new JScrollBar(JScrollBar.VERTICAL, 0, getMinimumBytesPerLine() * this.maxRows, 0, 10240);
                mainPanel.add(scrollbar, BorderLayout.EAST);
                scrollbar.addAdjustmentListener(hexViewController);
                scrollbar.setBlockIncrement(1024 - this.getMinimumBytesPerLine() * 2);
                scrollbar.setFocusable(false);
            }
        }
        // default frame key listener:
        Frame frame = this.getJFrame();

        if (frame != null)
            frame.addKeyListener(this.hexViewController);
        else
            this.addKeyListener(this.hexViewController);

        // update Font settings: fontToolbar & textArea
        FontInfo finfo = FontInfo.getFontInfo("Monospaced");
        this.fontToolBar.setFontInfo(finfo);
        finfo.updateComponentFont(this.textArea);

        initDnD();
    }

    private void initDnD()
    {
        // DROP TARGET
        {
            DropTarget dropTarget = new DropTarget();

            // canvas can receive drop events;
            this.textArea.setDropTarget(dropTarget);
            try
            {
                dropTarget.addDropTargetListener(new URIDropHandler(this.hexViewController));
            }
            catch (TooManyListenersException e)
            {
                logger.errorPrintf("FIXME:TooManyListenersException:%s\n",e);
            }
        }
    }

    @Override
    public void doInitViewer()
    {
        initGui();
    }

    @Override
    public String getViewerName()
    {
        return "Binary Viewer";
    }

    public void doStartViewer(VRL vrl, String optionalMethod)
    {
        doUpdate(vrl);
        this.validate();
    }

    public void doUpdate(final VRL loc)
    {
        debug("updateLocation:" + loc);

        if (loc == null)
            return;

        this.updateTask = new ActionTask(null, "loading:" + getVRL())
        {

            @Override
            protected void doTask()
            {
                try
                {
                    _reload(loc);
                }
                catch (Throwable t)
                {
                    this.setException(t);
                    handle("failed to load:" + getVRL(), t);
                }
            }

            @Override
            public void stopTask()
            {
            }
        };

        updateTask.startTask();
    }

    @Override
    public void doStopViewer()
    {
        if (this.updateTask!=null)
        {
            updateTask.signalTerminate(); 
        }
    }

    public void doDisposeViewer()
    {
        this.textArea = null;
        disposeReader();
    }

    protected void disposeReader()
    {
        if (reader != null)
        {
            try
            {
                reader.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            reader = null;
        }
    }
        
    @Override
    public String[] getMimeTypes()
    {
        return mimeTypes;
    }

    public void setText(String txt)
    {
        textArea.setText(txt);
    }

    public String getText()
    {
        return textArea.getText();
    }

    /** 
     * For binary dropped content. 
     */
    public void setContents(byte[] bytes)
    {
        this.fileOffset = 0;
        this.offset = 0;
        this.buffer = bytes;
        this.length = buffer.length; // update with nrBytes actual read
        _updateMagic();
        redrawContents();
    }

    public boolean isTool()
    {
        return true;
    }

    public String getClassification()
    {
        return "test/Viewers";
    }

    public boolean haveOwnScrollPane()
    {
        return true;
    }
    
    void redrawContents()
    {
        uiRedrawContents(); 
    }
    
    void uiRedrawContents()
    {
        if (SwingUtilities.isEventDispatchThread()==false)
        {
            Runnable runner=new Runnable() {
                public void run() {
                    uiRedrawContents(); 
                }
            };
            
            SwingUtilities.invokeLater(runner);
        }
        
        // ASSERT
        if (buffer == null)
        {
            return;
        }

        // Method globals:
        updateSizes();

        // ================
        // Step II)
        // Running Variables
        //

        int y = 0;
        long index = this.offset;
        // String str="";
        String charStr = "";
        String linestr = "";
        StringBuilder builder = new StringBuilder(getText().length());

        // TermGlobal.debugPrintln("HexViewer","maxLenStr="+maxLenStr);

        int startCharsString = 0;
        String maxLenStr = Long.toHexString(length);
        startCharsString = maxLenStr.length() + 1 + (1 + 2 * getWordSize()) * nrWordsPerLine + 2;

        while ((y < maxRows) && (index < length))
        {
            int bufferIndex = (int) (index - fileOffset); // index in buffer

            // Debug("index       ="+index);
            // Debug("bufferIndex ="+bufferIndex);

            linestr = getPosStr(index, maxLenStr.length()) + " ";

            for (int j = 0; j < nrWordsPerLine; j++)
            {
                linestr += hexStr(buffer, bufferIndex + j * getWordSize(), getWordSize()) + " ";
            }

            if (linestr.length() < startCharsString)
                linestr = fillWithSpaces(linestr, startCharsString);

            charStr = decodeChars(buffer, bufferIndex, nrBytesPerLine, true);

            // for (int
            // j=0;(j<nrBytesPerLine)&&(bufferIndex+j<buffer.length);j++)
            // charStr+=saveChar(buffer[bufferIndex+j]);

            index += nrBytesPerLine;
            linestr += charStr;

            if (y + 1 < maxRows)
                linestr += "\n";

            y++;
            charStr = "";
            builder.append(linestr);
        }

        // 0 contents
        if (length == 0)
        {
            builder.setLength(0);
            builder.append(getPosStr(0, 8) + " ");
        }

        // ===
        // Update GUI Componenets:
        // ===

        setText(builder.toString());
        // this.revalidate();
        // this.requestFrameResizeToPreferred();
        resetFocus();
    }

    protected void updateSizes()
    {
        String maxLenStr = Long.toHexString(length);

        // ===============================================
        // Step I)
        // update sizes/ derived variables :
        // ================================================

        Dimension targetSize = textArea.getSize();
        FontMetrics metrics = textArea.getFontMetrics(textArea.getFont());
        char chars[] =
        { 'w' };
        int charWidth = metrics.charsWidth(chars, 0, 1);
        int charHeight = metrics.getHeight();
        int maxLineChars = targetSize.width / (charWidth);

        // calculate:
        // width=m+1+(1+k)*n+c; k=2w; c=l=n*w
        // width=1+m+n+2nw+nw=1+m+n(1+3w)
        // width-1-m=n(1+3w) => n=(width-1-m)/(1+3w)
        nrWordsPerLine = (maxLineChars - 1 - maxLenStr.length()) / (1 + 3 * getWordSize());
        maxRows = targetSize.height / (charHeight);

        // autocalculate has bugs, use fixed of 32 (is nicer):
        // minimumBytesPerLine=nrWordsPerLine*wordSize;
        // minimumBytesPerLine=32;
        nrWordsPerLine = (int) Math.ceil((float) getMinimumBytesPerLine() / (float) getWordSize());
        nrBytesPerLine = nrWordsPerLine * getWordSize(); // actual bytes per
                                                         // line
        nrBytesPerView = maxRows * nrBytesPerLine;

        long scrollMax = length - nrBytesPerView;
        if (scrollMax < 0)
            scrollMax = 0;

        if (scrollMax >= Integer.MAX_VALUE)
        {
            // Use upper value:
            this.scrollMutiplier = (scrollMax / Integer.MAX_VALUE) + 1;
            scrollMax = scrollMax / scrollMutiplier;
        }

        updateScrollBarRange(0, scrollMax, nrBytesPerView, nrBytesPerLine);
        setScrollBarValue(offset / scrollMutiplier);

        /**
         * if (offset+nrBytesPerView>length) offset=(length-nrBytesPerView);
         * 
         * //offset=offset-offset%wordSize;
         * 
         * if (offset<0) offset=0;
         */

        // update gui fields:
        this.offsetField.setText(getPosStr(offset, maxLenStr.length()));
        this.lengthField.setText(getPosStr(length, maxLenStr.length()));
    }

    protected void setScrollBarValue(long value)
    {
        // Bug: Fix disappearing ScrollBar if offset
        // note: if scrolbar value is exactly Integer.MIN_VALUE the scrollbar is not visible.
        
        if (value >= Integer.MAX_VALUE)
        {
            logger.errorPrintf("Offset exceeds Integer.MAX_VALUE:%d\n",value);
        }
        else if (value <= Integer.MIN_VALUE)
        {
            logger.errorPrintf("Offset exceeds Integer.MIN_VALUE:%d\n,value");
        }

        logger.infoPrintf("setScrollBarValue(): %d\n", value);
        this.scrollbar.setValue((int) value);
    }

    protected void updateScrollBarRange(int min, long max, int blockIncrement, int unitIncrement)
    {
        if (max >= Integer.MAX_VALUE)
        {
            logger.errorPrintf("Maximum exceeds Integer.MAX_VALUE\n");
        }
        else if (max <= Integer.MIN_VALUE)
        {
            logger.errorPrintf("Maximum exceeds Integer.MAX_VALUE\n");
        }

        logger.infoPrintf("updateScrollBarRange(): range=[%d,%d], block,unit=[%d,%d]\n", min, max, blockIncrement, unitIncrement);
        this.scrollbar.setMinimum(min);
        this.scrollbar.setMaximum((int) max);
        this.scrollbar.setBlockIncrement(blockIncrement);
        this.scrollbar.setUnitIncrement(unitIncrement);
    }

    private String fillWithSpaces(String orgstr, int len)
    {
        String newstr = new String(orgstr);

        for (int i = orgstr.length(); i < len; i++)
            newstr += " ";

        return newstr;
    }

    private String getPosStr(long index, int maxLen)
    {
        // start of line:
        String posStr = Long.toHexString(index);
        posStr = "0x" + nulls(maxLen - posStr.length()) + posStr;

        return posStr;
    }

    private String nulls(int n)
    {
        // optimizations (?):
        if (n <= 0)
            return "";
        else if (n == 1)
            return "0";
        else if (n == 2)
            return "00";
        else if (n == 3)
            return "000";
        else if (n == 4)
            return "0000";
        else if (n == 5)
            return "00000";
        else if (n == 6)
            return "000000";
        else if (n == 7)
            return "0000000";
        else if (n == 8)
            return "00000000";

        char chars[] = new char[n + 1];
        for (int i = 0; i < chars.length; i++)
            chars[i] = '0';

        chars[n] = 0;

        return String.valueOf(chars);
    }

    // byte to hexstr !
    public String hexStr(byte[] word, int offset, int wordSize)
    {
        String str = "";

        for (int i = 0; (i < wordSize) && (offset + i < word.length); i++)
        {
            int b = word[offset + i]; // Little Endian

            if (b < 0)
                b = b + 128; // avoid negative hexadecimal

            // preprend
            if (b < 16)
                str += "0" + Integer.toHexString(b);
            else
                str += Integer.toHexString(b);
        }

        return str;
    }

    public String decodeChars(byte buffer[], int start, int len, boolean plainBytes)
    {
        String charStr = "";

        if (plainBytes)
        {
            for (int i = start; (i < start + len) && (i < buffer.length); i++)
            {
                charStr += charStr((int) buffer[i]);
            }
        }

        return charStr;
    }

    public String charStr(int val)
    {
        if (val < 0)
            val = val + 128;

        if ((val < 0) || (val > 255))
        {
            errorPrintf("Error: Character number out of bound:%d\n", val);
            return "?";
        }

        String str = null;
        str = specialCharMapping[val];
        if ((val != 32) && StringUtil.equals(str, ""))
        {
            switch (val)
            {
                case 0:
                    str = " ";
                    break;
                case 0x0d:
                    str = " ";
                    break;
                case '\n':
                    str = CHAR_CR;
                    break;
                case '\t':
                    str = " ";/* CHAR_TAB */
                    ;
                    break;
                default:
                    str = String.valueOf((char) val);
                    break;
            }
        }
        return str;
    }
    
    public void moveToOffset(final long offset)
    {
        if (this.offset == offset)
        {
            logger.debugPrintf("Ignoring moveTo same offset:%d\n", offset);
            return;
        }

        if (updateTask != null)
        {
            if (updateTask.isAlive())
            {
                logger.errorPrintf("FIXME: Already updating! Ignoring move to:%d\n", offset);
                return;
            }
        }

        this.updateTask = new ActionTask(null, "loading:" + getVRL())
        {

            @Override
            protected void doTask() throws VrsException
            {
                _moveToOffset(offset);
            }

            @Override
            public void stopTask()
            {
            }
        };

        updateTask.startTask();
    }

    void debug(String msg)
    {
        logger.debugPrintf("%s\n", msg);
    }

    public void addOffset(int delta)
    {
        moveToOffset(offset + delta);
    }

    public void updateFont(Font font, Map<?, ?> renderingHints)
    {
        // GuiSettings.updateRenderingHints(this, renderingHints); // useAntialising);
        textArea.setFont(font);
        redrawContents();
    }

    /** Resets focus to textArea for key commands */
    public void resetFocus()
    {
        textArea.requestFocus();
    }

    public void toggleFontToolBar()
    {
        if (this.fontToolBar.isVisible())
            this.fontToolBar.setVisible(false);
        else
            this.fontToolBar.setVisible(true);

        this.validate();
        // this.requestFrameResizeToPreferred();
    }

    // public void doMethod(String methodName, ActionContext actionContext) throws VrsException
    // {
    // if (actionContext.getSource() != null)
    // this.updateLocation(actionContext.getSource());
    // }

    /*
     * public Vector<ActionMenuMapping> getActionMappings() { ActionMenuMapping mapping=new
     * ActionMenuMapping("viewBinary", "View Binary (Hex Viewer)","binary");
     * 
     * // '/' is not a RE character Pattern patterns[]=new Pattern[mimeTypes.length];
     * 
     * for (int i=0;i<mimeTypes.length;i++) patterns[i]=Pattern.compile(mimeTypes[i]);
     * 
     * mapping.addMimeTypeMapping(patterns);
     * 
     * Vector<ActionMenuMapping> mappings=new Vector<ActionMenuMapping>(); mappings.add(mapping); return mappings; }
     */

    public long getOffset()
    {
        return offset;
    }

    void setWordSize(int wordSize)
    {
        this.wordSize = wordSize;
    }

    int getWordSize()
    {
        return wordSize;
    }

    void setMinimumBytesPerLine(int minimumBytesPerLine)
    {
        this.minimumBytesPerLine = minimumBytesPerLine;
    }

    int getMinimumBytesPerLine()
    {
        return minimumBytesPerLine;
    }

    public long getNrBytesPerLine()
    {
        return this.nrBytesPerLine;
    }

    @Override
    public Map<String, List<String>> getMimeMenuMethods()
    {
        // Use HashMapList to keep order of menu entries: first is default(!)

        Map<String, List<String>> mappings = new HashMapList<String, List<String>>();

        for (int i = 0; i < mimeTypes.length; i++)
        {
            List<String> list = new StringList(new String[]
            { "view:View Binary" });
            mappings.put(mimeTypes[i], list);
        }

        return mappings;
    }

    // ========================================================================
    // IO Methods/backgrounded methods
    // ========================================================================

    private synchronized void _reload(final VRL loc) throws IOException
    {
        debug("_update:" + loc);

        try
        {
            disposeReader();

            this.reader = this.getResourceHandler().createRandomReader(loc);
            this.length = reader.getLength();
            this.offset = 0;

            // check buffered read
            // Fill Buffer: use direct read (already in background)
            _readBuffer();
            redrawContents();
            _updateMagic();
        }
        catch (Exception e)
        {
            throw new IOException(e);
        }
    }

    public void _moveToOffset(long value)
    {
        if (SwingUtilities.isEventDispatchThread())
        {
            throw new Error("No IO during EventDispatchThread!");
        }

        debug("_moveToOffset:" + value);

        this.offset = value;

        if (offset >= length - nrBytesPerView)
            offset = length - nrBytesPerView;

        if (offset < 0)
            offset = 0;

        debug("new offset=" + offset);

        // move buffer window up (only if buffer doesn't already contain the
        // last
        // part of the file.
        if ((offset > this.fileOffset + buffer.length - this.nrBytesPerView)
                && (fileOffset + this.maxBufferSize < length))
        {

            // read half buffer before and after current offset
            fileOffset = offset - maxBufferSize / 2;

            if (fileOffset < 0)
                fileOffset = 0;

            _readBuffer();
        }

        // OR move buffer window down
        if (offset < this.fileOffset)
        {
            // read half buffer before and after current offset
            fileOffset = offset - maxBufferSize / 2;

            if (fileOffset < 0)
                fileOffset = 0;

            _readBuffer();
        }

        // ===
        // POST MOVE TO
        // ===
        redrawContents(); // (re)draw
    }

    private void _readBytes(RandomReadable reader, long fileOffset, byte[] buffer, int bufferOffset, int numBytes)
            throws IOException, VrsException
    {
        this.getResourceHandler().syncReadBytes(reader, fileOffset, buffer, bufferOffset, numBytes);
    }

    private synchronized void _readBuffer()
    {
        debug("readBuffer:" + offset);

        if (reader == null)
        {
            // no vfile, set to defaults:
            fileOffset = 0;
            length = buffer.length;
            // offset=0;
            return;
        }

        int len = 0;
        len = maxBufferSize;

        // check end of file
        if (fileOffset + len > length)
            len = (int) (length - fileOffset);

        // check buffer size:
        if ((buffer == null) || (len != buffer.length))
            buffer = new byte[len];

        debug("read buffer. offset        =" + offset);
        debug("read buffer. fileOffset    =" + fileOffset);
        debug("read buffer. buffer length =" + buffer.length);

        // fill buffer:
        try
        {
            notifyBusy(true);

            this.setViewerTitle("Reading:" + getVRL());
            // new FileReader(vfile).read(fileOffset,buffer,0,len);
            _readBytes(reader, fileOffset, buffer, 0, len);
            this.setViewerTitle("Inspecting:" + getVRL());
        }
        catch (Exception e)
        {
            this.setViewerTitle("Error reading:" + getVRL());
            notifyException("Failed to read buffer", e);
        }
        finally
        {
            notifyBusy(false);
        }
    }


    public void _updateMagic()
    {
        try
        {
            String magic = MimeTypes.getDefault().getMagicMimeType(buffer);
            logger.infoPrintf("Magic Type=%s\n", magic);
            this.magicField.setText(magic);
        }
        catch (MagicMatchNotFoundException e)
        {
            logger.logException(ClassLogger.ERROR, e, "MagicMatchNotFoundException for:%s\n", getVRL().getPath());
            // this.logger.errorPrintf("Could fing magic for:%s\n".getVRL().getPath());
        }
        catch (Exception e)
        {
            logger.logException(ClassLogger.ERROR, e, "Exception when updating magic\n");
        }

    }
    
}
