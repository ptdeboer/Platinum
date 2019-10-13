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

package nl.esciencecenter.ptk.vbrowser.ui.widgets;

import nl.esciencecenter.ptk.ui.icons.AnimatedIcon;
import nl.esciencecenter.ptk.ui.icons.IconAnimator;

import javax.swing.*;

/**
 * JLabel subclass with some convenience methods which supports AnimatedIcon as the Icon. This way
 * animated gifs, etc can be stopped, restarted or the animation speed can be changed.
 */
public class JAnimatedLabel extends JLabel {
    public JAnimatedLabel(AnimatedIcon icon) {
        super(icon); // will call setIcon() ! 
    }

    public void setIcon(Icon icon) {
        super.setIcon(icon);
    }

    /**
     * Explicitly set AnimatedIcon
     */
    public void setAnimatedIcon(AnimatedIcon icon) {
        setIcon(icon);
    }

    public void start() {
        if (isAnimated())
            getAnimatedIcon().start();
    }

    public void stop() {
        if (isAnimated())
            getAnimatedIcon().stop();
    }

    public void reset() {
        if (isAnimated())
            getAnimatedIcon().reset();
    }

    public void setAnimationSpeed(double speed) {
        if (isAnimated())
            getAnimatedIcon().setAnimationSpeed(speed);
    }

    public AnimatedIcon getAnimatedIcon() {
        Icon icon = this.getIcon();
        if (icon instanceof AnimatedIcon)
            return (AnimatedIcon) icon;

        return null;
    }

    /**
     * Returns true is the Icon is of AnimatedIcon class
     */
    public boolean isAnimated() {
        return (getAnimatedIcon() != null);
    }

    public void dispose() {
        AnimatedIcon icon = this.getAnimatedIcon();

        // unregister and dispose; 
        if (icon != null) {
            IconAnimator.getDefault().unregister(icon);
            icon.dispose();
        }
    }

}
