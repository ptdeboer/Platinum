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

package nl.esciencecenter.ptk.presentation;

import java.awt.Color;
import java.util.Map;

import nl.esciencecenter.ptk.object.Duplicatable;

/**
 * UI Presentation information for Attributes in the VBrowser.
 */
public class AttributePresentation implements Duplicatable<AttributePresentation> {

    public static class ColorMap {

        protected Color foreground = null;

        protected Color background = null;

        protected Map<String, Color> statusColors = null;
    }

    public static class PreferredSizes implements Duplicatable<PreferredSizes> {

        int minimum = -1;

        int preferred = -1;

        int maximum = -1;

        protected PreferredSizes() {
        }

        public PreferredSizes(int minWidth, int prefWidth, int maxWidth) {
            this.minimum = minWidth;
            this.preferred = prefWidth;
            this.maximum = maxWidth;
        }

        public int getMinimum() {
            return minimum;
        }

        public int getMaximum() {
            return maximum;
        }

        public int getPreferred() {
            return preferred;
        }

        public int[] getValues() {
            return new int[] { minimum, preferred, maximum };
        }

        /**
         * Set [minimum,preferred,maximum] values
         */
        public void setValues(int[] values) {
            this.minimum = values[0];
            this.preferred = values[1];
            this.maximum = values[2];
        }

        public String toString() {
            return "PreferredSizes:[" + minimum + "," + preferred + "," + maximum + "]";
        }

        public PreferredSizes duplicate() {
            return duplicate(false);
        }

        @Override
        public boolean shallowSupported() {
            return false;
        }

        @Override
        public PreferredSizes duplicate(boolean shallow) {
            return new PreferredSizes(minimum, preferred, maximum);
        }

        public void setPreferred(int w) {
            preferred = w;
        }
    }

    // ============
    // Instance
    // ============

    protected AttributePresentation.PreferredSizes widths = null;

    protected boolean attributeFieldResizable = true;

    public AttributePresentation.PreferredSizes getWidths() {
        return widths;
    }

    protected AttributePresentation() {
    }

    @Override
    public boolean shallowSupported() {
        return false;
    }

    @Override
    public AttributePresentation duplicate() {
        return duplicate(false);
    }

    public AttributePresentation duplicate(boolean shallow) {
        AttributePresentation dup = new AttributePresentation();
        dup.copyFrom(this);
        return dup;
    }

    protected void copyFrom(AttributePresentation other) {
        this.widths = other.widths.duplicate();
        this.attributeFieldResizable = other.attributeFieldResizable;
    }

    /**
     * @return {Minimal,Preferred, and Maximum} size triple.
     */
    public int[] getWidthValues() {
        if (widths == null) {
            return null;
        }
        return widths.getValues();
    }

    public void setWidthValues(int[] values) {
        if (this.widths == null) {
            this.widths = new AttributePresentation.PreferredSizes(values[0], values[1], values[2]);
        } else {
            this.widths.setValues(values);
        }
    }

    @Override
    public String toString() {
        return "AttributePresentation:[widths=" + widths + ", attributeFieldResizable="
                + attributeFieldResizable + "]";
    }

}