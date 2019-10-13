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

package nl.esciencecenter.ptk.ui.image;

import nl.esciencecenter.ptk.data.IntegerHolder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * An ImageSquence is an sequence of images which can be played in a certain order to show an
 * animation. A sequence is list of frames where multiple frames can use the same image or combine
 * them in some manner. The stored frame sequence can optionally hold several animation sequences
 * reusing images. If no frame information is stored, the images are playing in the order they are
 * stored with a default wait time in between.
 */
public class ImageSequence {

    /**
     * Hold information per Animation Frame.
     */
    public static class FrameInfo {
        /**
         * Specified tag which marks the start of a sequence. This way multiple sequences can be
         * stored inside an ImageSequence. Use play(String tag) to start an tagged sequence.
         */
        public String tag = null;

        public boolean lastFrame = false; // last frame of a sequence.  
        /**
         * wait time in milliseconds
         */
        public int waitTimeMs = 0;

        /**
         * If image number is not equal to the frame number this image number must be used. Multiple
         * frames may use the same image number for repeated images in the animations sequence.
         */
        public int imageNr = 0;

        public FrameInfo() {

        }

        public FrameInfo(int imageNr, int delayMillis) {
            this.imageNr = imageNr;
            this.waitTimeMs = delayMillis;
        }

        /**
         * Returns corresponding image number associated with this frame
         */
        public int getImageNr() {
            return imageNr;
        }

        /**
         * Returns wait times in milliseconds
         */
        public int getDelay() {
            return imageNr;
        }
    }

    // === // 
    protected Properties properties = new Properties();

    protected String description = null;

    protected int width = -1;

    protected int height = -1;

    /**
     * List of decoded images. The number of images isn't always the same as the number of frames if
     * one image is used multiple times
     */
    protected List<BufferedImage> images;

    private int loopCount = 0;

    private List<FrameInfo> frameInfo = null;

    public ImageSequence() {
    }

    public ImageSequence(BufferedImage newImage) {
        initImage(newImage); // single image  
    }

    public ImageSequence(List<BufferedImage> images) {
        this(images, null, 0);
    }

    public ImageSequence(List<BufferedImage> images, int loopCount) {
        this(images, null, loopCount);
    }

    public ImageSequence(List<BufferedImage> newImages, List<FrameInfo> infos, int loopCount) {
        initImages(newImages, true);
        this.loopCount = 0;
        this.frameInfo = null;
        initInfos(infos);
    }

    private void initInfos(List<FrameInfo> infos) {
        this.frameInfo = infos;
    }

    public int getLoopCount() {
        return this.loopCount;
    }

    public int getImageCount() {
        if (this.images == null)
            return 0;

        return this.images.size();
    }

    /**
     * Returns Frame Count. If no frame sequence information is defined it returns the number of
     * images.
     *
     * @return
     */
    public int getFrameCount() {
        if (this.frameInfo != null)
            return this.frameInfo.size();

        if (this.images != null)
            return this.images.size();

        return 0;
    }

    /**
     * Returns wait time in milliseconds
     */
    public int getFrameDelay(int n) {
        if (this.frameInfo == null)
            return 0;

        FrameInfo inf = frameInfo.get(0);
        if (inf == null)
            return 0;

        return inf.waitTimeMs;
    }

    /**
     * Use single image as image source
     */
    public void setSingleImage(BufferedImage newImage) {
        this.initImage(newImage);
    }

    /**
     * Adds image to the image list
     */
    public int addImage(BufferedImage newImage) {
        if (this.images == null)
            initImage(newImage); // first image: 

        this.images.add(newImage);
        return this.images.size();
    }

    protected void initImages(List<BufferedImage> newImages, boolean resetFrameInfo) {
        this.images = newImages;

        BufferedImage first = newImages.get(0);

        // make sure first image is loaded: 
        this.height = first.getHeight();
        this.width = first.getWidth();

        if (resetFrameInfo == true) {
            this.frameInfo = null;
            this.loopCount = 0;
        }
    }

    protected void initImage(BufferedImage newImage) {
        List<BufferedImage> list = new ArrayList<BufferedImage>(1);
        list.add(newImage);

        initImages(list, true);

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNrOfImages() {
        if (this.images == null)
            return 0;

        return images.size();
    }

    /**
     * Paints Current Image using the specified sequence information. This method is stateless and
     * uses the sequence information from the arguments. Use this method if the state of the
     * sequence is stored outside this component.
     *
     * @param sequenceName optional named seqeunce, can be 'null' for the default sequence.
     */
    public void paintImage(String sequenceName, int frameNr, Component c, Graphics g, int x, int y) {
        //debugPrintf("paintImage %s#%d: %d,%d@%s\n",sequenceName,frameNr,x,y,c); 
        g.drawImage(getFrameImage(frameNr), x, y, c);
    }

    /**
     * Returns Image associated with the specified frame sequence number. If not frame sequence
     * information is specified it return the image number
     */
    public Image getFrameImage(int n) {
        if (this.frameInfo != null) {
            FrameInfo inf = getFrameInfo(n);

            if (inf == null)
                return null;

            int nr = inf.getImageNr();

            return getImage(nr);
        }

        return this.images.get(n);

    }

    /**
     * Returns whether this sequence has frame information. If not all the images are sequentially
     * played in order. A particular frame sequence might play the stored images in a different
     * order.
     *
     * @return
     */
    public boolean hasFrameInfo() {
        return (this.frameInfo != null);
    }

    public FrameInfo getFrameInfo(int n) {
        if ((this.frameInfo == null) || (n >= frameInfo.size()))
            return null;

        return this.frameInfo.get(n);
    }

    public BufferedImage getImage(int n) {
        if ((this.images == null) || (n >= images.size()))
            return null;

        return this.images.get(n);
    }

    public int findSequence(String tag) {
        if (this.frameInfo == null)
            return -1;

        for (int i = 0; i < this.frameInfo.size(); i++)
            if (tag.equals(frameInfo.get(i).tag))
                return i;

        return -1;
    }

    public boolean calculateNextFrame(String sequenceName, IntegerHolder currentLoop,
                                      IntegerHolder currentFrame, boolean reverse) {
        int loop = 0;
        int frameNr = 0;

        if (currentLoop != null)
            loop = currentLoop.value;

        if (currentFrame != null)
            frameNr = currentFrame.value;

        int fc = getFrameCount();
        int newFrameNr = (fc + frameNr + (reverse ? -1 : 1)) % fc;

        // frame wrap around: check next loop 
        if ((reverse == false) && (newFrameNr < frameNr)) {
            // last loop, and frame wraps around
            if ((loopCount != 0) && (loop >= loopCount)) {
                return false;
            } else {
                // next loop; 
                currentLoop.value++;
            }
        } else if ((reverse == true) && (newFrameNr > frameNr)) {
            // when at loop 0 and framecounter wrap around 
            if ((loopCount != 0) && (loop <= 0)) {
                return false;
            } else {
                // next loop; 
                currentLoop.value--;
            }
        }

        currentFrame.value = newFrameNr;
        return true;
    }

    /**
     * Dispose speeds up garbage collection and flushes image resources !
     */
    public void dispose() {
        if (this.images != null) {
            for (Image image : images)
                image.flush();
            this.images.clear();
            this.images = null;
        }

        if (this.frameInfo != null) {
            this.frameInfo.clear();
            this.frameInfo = null;
        }
    }

    public void setLoopCount(int count) {
        this.loopCount = count;
    }

    public Object getProperty(String name) {
        return properties.getProperty(name);
    }

}
