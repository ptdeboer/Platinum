///*
// * Copyrighted 2012-2013 Netherlands eScience Center.
// *
// * Licensed under the Apache License, Version 2.0 (the "License").  
// * You may not use this file except in compliance with the License. 
// * For details, see the LICENCE.txt file location in the root directory of this 
// * distribution or obtain the Apache License at the following location: 
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software 
// * distributed under the License is distributed on an "AS IS" BASIS, 
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// * See the License for the specific language governing permissions and 
// * limitations under the License.
// * 
// * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
// * ---
// */
//// source: 
//
//package nl.esciencecenter.ptk.presentation;
//
//import java.util.Hashtable;
//import java.util.Map;
//
//import javax.swing.JTable;
//
//import nl.esciencecenter.ptk.presentation.Presentation;
//
///**
// * Presentation for UI Objects.
// * 
// * @author P.T. de Boer
// */
//public class UIPresentation extends Presentation
//{
//
//    public static UIPresentation createDefault()
//    {
//        return new UIPresentation(); // return default object;
//    }
//
//    /** 
//     * @see getPresentationFor(String, String, String, boolean)
//     */
//    public static UIPresentation getPresentation(String key, boolean autoCreate)
//    {
//        synchronized (presentationStore)
//        {
//            Presentation pres = presentationStore.get(key);
//            if (pres != null)
//            {
//                if (pres instanceof UIPresentation)
//                {
//                    return (UIPresentation) pres;
//                }
//                else
//                {
//                    // downcast to UIPresentation?
//                }
//            }
//
//            if (autoCreate == false)
//                return null;
//
//            UIPresentation uipres = createDefault();
//            // if (pres!=null)
//            // uipres.copyFrom(pres);
//
//            return uipres;
//        }
//    }
//
//    public static void putUIPresentation(String id, UIPresentation pres)
//    {
//        synchronized (presentationStore)
//        {
//            presentationStore.put(id, pres);
//        }
//    }
//
//    public static UIPresentation getPresentationForSchemeType(String scheme, String type, Boolean autoCreate)
//    {
//        return getPresentation(createKey(scheme, null, type), autoCreate);
//    }
//
//    public static UIPresentation getPresentationFor(String scheme, String host, String type, boolean autoCreate)
//    {
//        return getPresentation(createKey(scheme, host, type), autoCreate);
//    }
//
//    // ========================================================================
//    //
//    // ========================================================================
//
//    protected int columnsAutoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS; // .AUTO_RESIZE_OFF;
//
//    protected Map<String, AttributePresentation> attributePresentations = new Hashtable<String, AttributePresentation>();
//    
//    protected String iconAttributeName="icon"; 
//        
//    public UIPresentation()
//    {
//        super();
//        initDefaults();
//    }
//
//    private void initDefaults()
//    {
//        // this.childAttributeNames=new StringList();
//        //
//        // initChildAttribute(RESOURCE, 80);
//        //
//        // initChildAttribute(ICON, 32);
//        // initChildAttribute("Index", 32);
//        // initChildAttribute(NAME, 200);
//        // initChildAttribute("Type", 90);
//        // initChildAttribute(SCHEME, 48);
//        // initChildAttribute(HOSTNAME, 120);
//        // initChildAttribute(LENGTH, 70);
//        // initChildAttribute(PATH, 200);
//        // initChildAttribute("Status",48);
//        // initChildAttribute("ResourceStatus",48);
//        // initChildAttribute(ACCESS_TIME, 120);
//        // initChildAttribute(MODIFICATION_TIME, 120);
//        // initChildAttribute(CREATION_TIME, 120);
//    }
//
////    private void initChildAttribute(String name, int prefWidth)
////    {
////        this.childAttributeNames.add(name);
////        this.setAttributePreferredWidth(name, prefWidth);
////    }
//
//    /** Method will return null if information hasn't been stored ! */
//    public Integer getAttributePreferredWidth(String name)
//    {
//        AttributePresentation attrPres = this.attributePresentations.get(name);
//        if (attrPres == null)
//            return null;
//
//        if (attrPres.widths == null)
//            return null;
//
//        if (attrPres.widths.preferred < 0)
//            return null;
//
//        return new Integer(attrPres.widths.preferred);
//    }
//
//    /**
//     * Returns Integer[]{<Minimum>,<Preferred>,<Maximum>} Triple. Integer value
//     * is NULL is it isn't defined. Method will always return an Integer array
//     * of size 3, but actual values may be null.
//     * 
//     */
//    public Integer[] getAttributePreferredWidths(String name)
//    {
//        Integer vals[] = new Integer[3];
//
//        AttributePresentation attrPres = this.attributePresentations.get(name);
//        if (attrPres == null)
//            return vals;
//
//        if (attrPres.widths == null)
//            return vals;
//
//        if (attrPres.widths.minimum >= 0)
//            vals[0] = new Integer(attrPres.widths.minimum);
//
//        if (attrPres.widths.preferred >= 0)
//            vals[1] = new Integer(attrPres.widths.preferred);
//
//        if (attrPres.widths.maximum >= 0)
//            vals[2] = new Integer(attrPres.widths.maximum);
//
//        return vals;
//    }
//
//    public void setAttributePreferredWidth(String attrname, int minWidth, int prefWidth, int maxWidth)
//    {
//        AttributePresentation pres = this.attributePresentations.get(attrname);
//
//        if (pres == null)
//            pres = new AttributePresentation();
//
//        pres.widths = new AttributePresentation.PreferredSizes(minWidth, prefWidth, maxWidth);
//
//        this.attributePresentations.put(attrname, pres);
//
//    }
//
//    public void setAttributePreferredWidth(String attrname, int w)
//    {
//        AttributePresentation pres = this.attributePresentations.get(attrname);
//
//        if (pres == null)
//            pres = new AttributePresentation();
//
//        if (pres.widths == null)
//            pres.widths = new AttributePresentation.PreferredSizes(-1, w, -1);
//        else
//            pres.widths.preferred = w;
//
//        this.attributePresentations.put(attrname, pres);// update
//    }
//
//    public void setAttributePreferredWidths(String attrname, int[] values)
//    {
//        AttributePresentation pres = this.attributePresentations.get(attrname);
//
//        if (pres == null)
//            pres = new AttributePresentation();
//
//        if (pres.widths == null)
//            pres.widths = new AttributePresentation.PreferredSizes(values[0], values[1], values[2]);
//        else
//            pres.widths.setValues(values);
//
//        this.attributePresentations.put(attrname, pres);// update
//    }
//
//    public boolean getAttributeFieldResizable(String attrname)
//    {
//        AttributePresentation pres = this.attributePresentations.get(attrname);
//
//        if (pres == null)
//            return true;
//
//        return pres.attributeFieldResizable;
//    }
//
//    public int getColumnsAutoResizeMode()
//    {
//        return this.columnsAutoResizeMode;
//    }
//
//    public void setColumnsAutoResizeMode(int value)
//    {
//        this.columnsAutoResizeMode = value;
//    }
//    
//    public String toString()
//    {
//    	String str="<UIPresentation>{sortOption="+this.sortOption;
//    	if (sortFields==null)
//    	{
//    		str+=",sortFields=<null>";
//    	}
//    	else
//    	{
//    		str+=",sortFields="+sortFields.toString(",");
//    	}
//    	str+="}";
//    	return str; 
//    }
//    
//    public void setIconAttributeName(String name)
//    {
//        iconAttributeName=name; 
//    }
//
//    public String getIconAttributeName()
//    {
//        return iconAttributeName; 
//    }
//}
