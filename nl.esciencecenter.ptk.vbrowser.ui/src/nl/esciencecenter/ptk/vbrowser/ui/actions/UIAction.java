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

package nl.esciencecenter.ptk.vbrowser.ui.actions;

import javax.swing.AbstractAction;
//import javax.swing.Action;
import javax.swing.Action;

public abstract class UIAction extends AbstractAction // implements javax.swing.Action
{
    private static final long serialVersionUID = -2196060232819798830L;
    
    public UIAction(String name)
    {
        super(name); 
        //putValue(Action.NAME,name); 
    }
    

    public String getName()
    {
        Object obj=this.getValue(Action.NAME);
        if (obj!=null)
            return obj.toString(); 
        return null;
    }
    
//
//    public Object getValue(String s)
//    {
//        if (s == "Name")
//            return name;
//        else
//            return null;
//    }
//
//    public void putValue(String s, Object obj)
//    {
//    }
//
//    public void setEnabled(boolean flag)
//    {
//    }
//
//    public final boolean isEnabled()
//    {
//        return isEnabled(null);
//    }
//
//    public boolean isEnabled(Object obj)
//    {
//        return true;
//    }
//
//    public void addPropertyChangeListener(PropertyChangeListener propertychangelistener)
//    {
//    }
//
//    public void removePropertyChangeListener(PropertyChangeListener propertychangelistener)
//    {
//    }

    
}
