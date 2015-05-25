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
import java.awt.Image;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import nl.esciencecenter.ptk.data.HashMapList;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.ui.image.ImagePane;
import nl.esciencecenter.ptk.ui.image.ImagePane.ImageWaiter;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerJPanel;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Implementation of an Image Viewer.<br>
 */
public class ImageViewer extends ViewerJPanel {
    private static final long serialVersionUID = 5768234709523116729L;

    /** The mimetypes I can view */
    private static String mimeTypes[] = { "image/gif", "image/jpeg", "image/bmp", "image/png" };

    private static double zoomOutFactors[] = { 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1 };

    private static double zoomInFactors[] = {
            // 100,125,150,200,300,400,500,600,800,1000%
            1.25, 1.5, 1.75, 2, 2.25, 2.5, 2.75, 3, 4, 5, 6, 7, 8, 9, 10 };

    // ====================================================================
    //
    // ====================================================================
    ImagePane imagePane = null;

    // private ImageIcon icon=null;
    JScrollPane scrollPane;

    // private int offsetx;
    // private int offsety;

    int zoomIndex = 0;

    double currentZoomFactor = 1.0;

    boolean fitToScreen = false;

    private Image orgImage;

    // private JLabel imageLabel; // store image in Label Component

    @Override
    public String[] getMimeTypes() {
        return mimeTypes;
    }

    public void initGui() {
        {
            this.setSize(800, 600);

            BorderLayout thisLayout = new BorderLayout();
            this.setLayout(thisLayout);
            // this.setLayout(null); // absolute layout

            {
                this.scrollPane = new JScrollPane();
                this.scrollPane.setSize(800, 600);
                this.getContentPanel().add(scrollPane, BorderLayout.CENTER); // addToRootPane(imagePane,BorderLayout.CENTER);
                {
                    this.imagePane = new ImagePane();
                    scrollPane.setViewportView(imagePane);
                    this.imagePane.setSize(800, 600);
                    this.imagePane.setLocation(0, 0);
                }
            }

            // imagePane.setVisible(true);

            // this.imageLabel=new JLabel("ImageLabel:Loading image...");
            // this.add(this.imageLabel,BorderLayout.CENTER);
            // this.imageLabel.setLocation(0,0);
            this.setToolTipText(getViewerName());
        }

        // listeners:
        {
            new ImageViewerController(this);
        }
    }

    @Override
    public void doInitViewer() {
        initGui();
    }

    @Override
    public void doStopViewer() {
        // this.muststop=true;
        // this.imagePane.signalStop();
    }

    @Override
    public void doDisposeViewer() {
        // Help the garbage collector, images can be big:
        if (imagePane!=null){ 
            this.imagePane.dispose();
            this.remove(imagePane);
        }
        this.imagePane = null;
        // this.remove(imageLabel);
        // this.imageLabel=null;
    }

    @Override
    public String getViewerName() {
        // remove html color codes:
        return "ImageViewer";
    }

    @Override
    public void doStartViewer(VRL vrl, String optionalMethod) {
        doUpdate(vrl);
    }

    public void doUpdate(VRL vrl) {
        try {
            loadImage(vrl);
        } catch (Exception e) {
            notifyException("Failed to load image:" + vrl, e);
        }
    }

    public void loadImage(VRL vrl) throws Exception {
        if (vrl == null) {
            return;
        }

        notifyBusy(true);

        try {

            // load image and wait:
            // this.imagePane.loadImage(location,true);
            loadImage(vrl, false);

            // keep original image for zoom purposes;
            this.orgImage = imagePane.getImage();

            this.fitToScreen = false;

            this.zoomIndex = 0;
        } catch (Exception e) {
            throw new VrsException(e);
        } finally {
            notifyBusy(false);
        }
    }

    public void loadImage(VRL vrl, boolean wait) throws Exception {
        InputStream inps = getResourceHandler().openInputStream(vrl);

        Image image;
        image = ImageIO.read(inps);

        try {
            inps.close();
        } catch (Exception e) {
            ;
        }

        if (image == null) {
            throw new IOException("Failed to load image: Image loader returned NULL for:"
                    + vrl.toURI());
        }

        imagePane.setImage(image, wait);
    }
   
    /**
     * I manage my own scrollpane for panning/autoscrolling
     */
    public boolean haveOwnScrollPane() {
        return true;
    }

    /**
     * Get ScrollPane ViewPosition
     */
    public Point getViewPosition() {
        JViewport viewP = this.scrollPane.getViewport();
        return viewP.getViewPosition();
    }

    /**
     * Set ScrollPane ViewPosition
     */
    public void setViewPosition(int newx, int newy) {
        // /UIGlobal.debugPrintf(this,"moveViewPoint:"+newx+","+newy);

        JViewport viewP = this.scrollPane.getViewport();
        Dimension scrollPaneSize = scrollPane.getSize();

        // calculate maximum viewpiont size :
        Dimension maxP = scrollPane.getViewport().getView().getSize();
        maxP.width -= scrollPaneSize.width;
        maxP.height -= scrollPaneSize.height;

        if (newx > maxP.width)
            newx = maxP.width;

        if (newy > maxP.height)
            newy = maxP.height;

        viewP.setViewPosition(new Point(newx, newy));

        // this.offsetx=newx;
        // this.offsety=newy;
    }

    // ====
    // Zoom
    // ====

    private Object zoomTaskMutex = new Object();

    private Runnable zoomTask = null;

    private Thread zoomThread = null;

    public void zoomIn() {
        this.fitToScreen = false;
        if (zoomIndex < zoomInFactors.length) {
            zoomIndex++;
            doZoom();
        }
    }

    public void zoomOut() {
        this.fitToScreen = false;

        if (zoomIndex > -zoomOutFactors.length) {
            zoomIndex--;
            doZoom();
        }
    }

    private boolean doMoreZoom = false;

    /**
     * Perform Zoom: Schedule background task to do the zooming
     */
    protected void doZoom() {
        double zoomFactor = 1.0;

        final Image sourceImage = this.orgImage;
        if (sourceImage == null) {
            errorPrintf("doZoom(): NULL source image!\n");
            return;
        }

        int h = sourceImage.getHeight(null);
        int w = sourceImage.getWidth(null);

        if (fitToScreen == true) {
            double aspect = (double) w / (double) h;

            Dimension targetSize = this.scrollPane.getSize();
            double targetAspect = ((double) targetSize.width) / (double) targetSize.height;

            if (aspect > targetAspect) {
                // fit width
                w = targetSize.width;
                h = (int) (targetSize.width / aspect);
            } else {
                // fit height
                h = targetSize.height;
                w = (int) (targetSize.height * aspect);
            }
        } else {
            if (zoomIndex > 0)
                zoomFactor = zoomInFactors[zoomIndex - 1];

            if (zoomIndex < 0)
                zoomFactor = zoomOutFactors[-zoomIndex - 1];

            h = (int) (h * zoomFactor);
            w = (int) (w * zoomFactor);
        }

        final int newHeight = h;
        final int newWidth = w;

        Dimension currentSize = imagePane.getImageSize();

        // check bogus update ! (Current size already done)
        if ((currentSize.width == newWidth) && (currentSize.height == newHeight))
            return;

        synchronized (zoomTaskMutex) {
            if (zoomTask != null) {
                // check if previous zoom thread is stil active:
                if ((zoomThread != null) && (zoomThread.isAlive() == true)) {
                    // flag zoom not done !
                    this.doMoreZoom = true;
                    // IGlobal.infoPrintln(this,"Warning: Previous zoom task still running");
                    return;
                }

                // cleanup:

                this.zoomTask = null;
                this.zoomThread = null;
            }

            zoomTask = new Runnable() {
                public void run() {
                    if ((newHeight <= 0) || (newWidth <= 0)) {
                        // UIGlobal.infoPrintln(this,"*** Warning: zoomIn cancelled: image not ready");
                        return;
                    }

                    notifyBusy(true);

                    Image tempImage = sourceImage.getScaledInstance(newWidth, newHeight,
                            Image.SCALE_FAST);

                    notifyBusy(false);

                    ImageWaiter w = new ImageWaiter(tempImage);

                    try {
                        // Wait for ALL bits doesn't work anymore ???
                        w.waitForCompletion(false);
                        updateNewZoomImage(tempImage);
                    } catch (Exception e) {
                        notifyException("Failed to zoom", e);
                    }

                    //
                    // check more zoom, but only AFTER this current thread
                    // has fininshed !!!
                    //
                    checkMoreZoom();
                }
            };

            zoomThread = new Thread(zoomTask);
            zoomThread.start();
        }// synchronized(zoomTaskMutex);
    }

    private void checkMoreZoom() {
        Runnable checkZoomTask = new Runnable() {
            public void run() {

                synchronized (zoomTaskMutex) {
                    if (doMoreZoom == true) {
                        doMoreZoom = false;
                        doZoom();
                    }
                }
            }
        };

        SwingUtilities.invokeLater(checkZoomTask);
    }

    protected void updateNewZoomImage(Image img) throws Exception {
        // new image, image should already be done !
        // no checking needed

        this.imagePane.setImage(img, false);
    }

    public void resetZoom() {
        this.zoomIndex = 0;
        this.fitToScreen = false;
        this.doZoom();
    }

    public void toggleFitToScreen() {
        fitToScreen = (fitToScreen == false);
        doZoom();
    }

    public void reset() {
        this.resetZoom();
    }

    @Override
    public Map<String, List<String>> getMimeMenuMethods() {
        // Use HashMapList to keep order of menu entries: first is default(!)

        Map<String, List<String>> mappings = new HashMapList<String, List<String>>();

        for (int i = 0; i < mimeTypes.length; i++) {
            List<String> list = new StringList(new String[] { "view:View Image" });
            mappings.put(mimeTypes[i], list);
        }

        return mappings;
    }

}
