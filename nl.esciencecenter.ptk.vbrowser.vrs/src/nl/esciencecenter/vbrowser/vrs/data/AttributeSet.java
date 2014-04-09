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

package nl.esciencecenter.vbrowser.vrs.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.ptk.data.HashMapList;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.object.Duplicatable;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.VRSProperties;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * A VAttributeSet is implemented as an LinkedHashMap with extra set
 * manipulation methods. 
 * <p>
 * About the he set() methods:<br>
 * The set methods only add a new value (using put) to the Hashtable if the
 * VAttribute object entry wasn't stored yet. If the VAttribute object already
 * exists, the Value of that VAttribute will be changed, keeping the original
 * VAttribute object in the Set. The VAttribute has to be Editable.<br>
 * This way it is possible to keep references to the stored VAttribute for
 * advanced manipulation methods.
 * 
 * @see HashMapList
 */
public class AttributeSet extends HashMapList<String, Attribute> 
                          implements Serializable, Cloneable // Duplicatable<AttributeSet>
{
    // ========================================================================
    // Class
    // ========================================================================

    // = serializable
    private static final long serialVersionUID = -4407735257687000157L;

    public static final String ATTR_SETNAME = "setName";

    private static ClassLogger logger = null;

    static
    {
        logger = ClassLogger.getLogger(AttributeSet.class);
    }

    /** Create VAttributeSet from Properties */
    public static AttributeSet createFrom(Properties properties)
    {
        return new AttributeSet(properties);
    }

    // ========================================================================
    // Instance
    // ========================================================================

    /** Optional set Name */
    protected String setName = "";

    // List<?> also matches Vector and ArrayList !
    protected void init(List<Attribute> attrs)
    {
        if (attrs == null)
        {
            this.clear();
            return; // empty set
        }

        for (Attribute attr : attrs)
        {
            if ((attr != null) && (attr.getName() != null))
            {
                this.put(attr.getName(), attr);
            }
        }
    }

    protected void init(Attribute[] attrs)
    {
        if (attrs == null)
        {
            this.clear();
            return; // empty set
        }

        for (Attribute attr : attrs)
        {
            if ((attr != null) && (attr.getName() != null))
            {
                this.put(attr);
            }
        }
    }

    public AttributeSet()
    {
        super(); // empty hastable
    }

    /** Named Attribute Set */
    public AttributeSet(String name)
    {
        super(); // empty hastable
        this.setName = name;
    }

    /**
     * Constructs an VAttributeSet from the Map. Note that VAttributeSet is a
     * map as well, so this contructor can be used as an Copy Constructor.
     * 
     * @param map
     *            source map.
     */
    public AttributeSet(Map<? extends Object, ? extends Object> map)
    {
        init(map);
    }

    /**
     * Constructs an VAttributeSet from the Map. Note that VAttributeSet is a
     * map as well, so this contructor can be used as an Copy Constructor.
     * 
     * @param map
     *            source map.
     */
    public AttributeSet(Collection<Attribute> attrs) 
    {
        init(attrs.toArray(new Attribute[0]));
    }
    
    /**
     * Create attribute set from generic <Key,Value> Map. As key value, the
     * STRING representation of the Key object is used. As value the VAttribute
     * factory createFrom(object) is used.
     */
    private void init(Map<? extends Object, ? extends Object> map)
    {
        // int index=0;

        Set<? extends Object> keys = map.keySet();

        for (Iterator<? extends Object> iterator = keys.iterator(); iterator.hasNext();)
        {
            Object key = iterator.next();
            // Use STRING representation of Key Object !
            String keystr = key.toString();
            Object value = map.get(key);

            Attribute attr;
            // keep my own types:
            if (value instanceof Attribute)
            {
                attr = ((Attribute) value).duplicate();
            }
            else
            {
                // Use VAtribute Factory:
                attr = AttributeUtil.createFrom(keystr, value);
            }

            this.put(attr);
            // index++;
        }
    }

    // ========================================================================
    // Getters/Setters
    // ========================================================================

    /** Sets optional name. null name is allowed */
    public void setName(String newName)
    {
        setName = newName;
    }

    /** Returns optional name. Can be null */
    public String getName()
    {
        return setName;
    }

    /**
     * This method will add the attribute to the hashtable and keep the order in
     * which it is put. If the attribute already has been added the order will
     * be kept.
     */
    public void put(Attribute attr)
    {
        if (attr == null)
        {
            logger.warnPrintf("Attribute is NULL!\n");
            return;
        }
        this.put(attr.getName(), attr);
    }

    /** Combined put() and setEditable() */
    public void put(Attribute attr, boolean editable)
    {
        attr.setEditable(editable);
        this.put(attr);
    }

    public void set(Attribute attribute)
    {
        this.put(attribute);
    }

    public AttributeType getTypeOf(String key)
    {
        Attribute attr=this.get(key);
        
        if (attr==null)
        {
            return null;
        }
        
        return attr.getType(); 
    }
    
    /**
     * Create from Array. Duplicate entries are overwritten. Last entry is kept,
     * NULL entries are skipped.
     */
    public AttributeSet(Attribute[] attrs)
    {
        if (attrs == null)
            return; // empty set

        for (Attribute attr : attrs)
        {
            // filter bogus attributes!
            if ((attr != null) && (attr.getName() != null))
            {
                this.put(attr);
            }
        }
    }

    @Override
    public AttributeSet duplicate(boolean shallow)
    {
        AttributeSet newset = new AttributeSet(this);
        newset.setName(this.getName());
        return newset;
    }

    @Override
    public AttributeSet duplicate()
    {
        return duplicate(false);
    }

    @Override
    public boolean shallowSupported()
    {
        return false;
    }

    /** Returns array of attribute names of the key set. */
    public String[] getAttributeNames()
    {
        return this.getKeyArray(new String[0]);
    }

    /**
     * Returns Object value of Attribute with name 'name' Returns null if the
     * attribute is not in the set.
     */
    public Object getValue(String name)
    {
        Attribute attr = get(name);

        if (attr == null)
        {
            return null;
        }

        return attr.getValue();
    }

    /**
     * Returns String value of Attribute with name 'name' Returns null if the
     * attribute is not in the set.
     */
    public String getStringValue(String name)
    {
        Attribute attr = get(name);

        if (attr == null)
        {
            return null;
        }

        return attr.getStringValue();
    }

    /**
     * Returns String value of Attribute
     * 
     * @param defVal
     *            default value if attribute is not in this set
     */
    public int getIntValue(String name, int defVal)
    {
        Attribute attr = get(name);

        if (attr == null)
            return defVal;

        return attr.getIntValue();
    }

    public long getLongValue(String name, long defVal)
    {
        Attribute attr = get(name);

        if (attr == null)
            return defVal;

        return attr.getLongValue();
    }

    public VRL getVRLValue(String name) throws VRLSyntaxException
    {
        Attribute attr = get(name);

        if (attr == null)
            return null;

        return attr.getVRL();
    }

    /**
     * Returns String value of Attribute
     * 
     * @param defVal
     *            default value if attribute is not in this set
     */
    public int getIntValue(String name)
    {
        Attribute attr = get(name);

        if (attr == null)
            return -1;

        return attr.getIntValue();
    }

    public long getLongValue(String name)
    {
        Attribute attr = get(name);

        if (attr == null)
            return -1;

        return attr.getLongValue();
    }

    /**
     * Helper method used by the set() methods. Returns old value.
     */
    private Object _set(AttributeType optNewType, String name, Object val)
    {
        Attribute orgAttr = this.get(name);

        if (orgAttr == null)
        {
            // set: put new Editable Attribute with specified type:
            Attribute attr = AttributeUtil.createFrom(optNewType, name, val);
            attr.setEditable(true);
            this.put(attr);
            return null;
        }
        else
        {
            Object oldValue = orgAttr.getValue();

            // Update value only.
            orgAttr.setObjectValue(val);

            /** Return Value as Object which type matches the VAttribute Type */
            return oldValue;
        }
    }

    /**
     * Set Attribute Value. Returns previous value if any. The difference
     * between put and set is that this method changes the stored Attribute in
     * the hashtable by using VAttribute.setValue(). It does NOT put a new
     * VAttribute into the hashtable. <br>
     * This means that already stored VAttribute has to be editable!
     * This way the 'changed' flag is updated from the VAttribute. If the named attribute
     * isn't stored, a new attribute will be created and the behavior is similar
     * to put().
     */
    public String set(String name, String val)
    {
        String oldvalue = getStringValue(name);
        _set(AttributeType.STRING, name, val);
        return oldvalue;
    }

    public void set(String attrName, boolean val)
    {
        _set(AttributeType.BOOLEAN, attrName, new Boolean(val));
    }

    public void set(String attrName, int val)
    {
        _set(AttributeType.INT, attrName, new Integer(val));
    }

    public void set(String attrName, long val)
    {
        _set(AttributeType.LONG, attrName, new Long(val));
    }

    public void set(String attrName, VRL vrl)
    {
        _set(AttributeType.VRL, attrName, vrl);
    }
    
    public void setAny(String attrName, Object object)
    {
        _set(AttributeType.ANY,attrName,object);
    }
    
    public boolean getBooleanValue(String name, boolean defaultValue)
    {
        Attribute attr = get(name);

        if (attr == null)
            return defaultValue;

        return attr.getBooleanValue();
    }

    public String toString()
    {
        Attribute[] attrs = toArray(new Attribute[]
        {});

        String str = "{VAttributeSet:" + this.setName + ":[";

        if (attrs != null)
            for (int i = 0; i < attrs.length; i++)
            {
                str += attrs[i] + (i < (attrs.length - 1) ? "," : "");
            }

        str += "]}";

        return str;
    }

    /**
     * Creates deep copy
     */
    public AttributeSet clone()
    {
        return duplicate();
    }

    /**
     * Stored new String Attribute, replacing already stored VAttribute if it
     * already exists.
     * 
     * @see #set(String, String) Use set() method to keep already stored
     *      VAttributes.
     */
    public void put(String name, String value)
    {
        if (value==null)
        {
            this.remove(name); // clear, do not store nill value. 
        }
        else
        {
            put(new Attribute(name, value));
        }
    }

    public void put(String name, VRL vrl)
    {
        if (vrl==null)
        {
            this.remove(name); // clear, do not store nill value. 
        }
        else
        {
            put(new Attribute(name, vrl));
        }
    }

    /**
     * Stored new Integer Attribute, replacing already stored VAttribute if it
     * already exists.
     * 
     * @see #set(String, int) Use set() method to keep already stored
     *      VAttributes.
     */
    public void put(String attrName, int val)
    {
        put(new Attribute(attrName, val));
    }

    /**
     * Stored new boolean Attribute, replacing already stored VAttribute if it
     * already exists.
     * 
     * @see #set(String, boolean) Use set() method to keep already stored
     *      VAttributes.
     */
    public void put(String attrName, boolean val)
    {
        put(new Attribute(attrName, val));
    }

    /**
     * Returns changed attributes as array
     */
    public synchronized Attribute[] getChangedAttributesArray()
    {
        int numChanged = 0;
        int index = 0;

        for (int i = 0; i < this.size(); i++)
        {
            if (this.elementAt(i).hasChanged() == true)
            {
                numChanged++;
            }
        }
        
        Attribute attrs[] = new Attribute[numChanged];

        for (int i = 0; i < this.size(); i++)
        {
            if (this.elementAt(i).hasChanged() == true)
            {
                attrs[index++] = this.elementAt(i);
            }
        }
        
        return attrs;
    }

    /**
     * Set Editable flag of attribute
     */
    public void setEditable(String name, boolean val)
    {
        Attribute attr = this.get(name);

        if (attr == null)
            return;

        attr.setEditable(val);
    }

    public StringList getKeyStringList()
    {
        return new StringList(this.getKeyArray(new String[0]));
    }

    // explicited typed remove. 
    public Attribute remove(String name)
    {
        return super.remove((Object) name);
    }

    /** Remove attribute if name isn't in the key list */
    public void removeIfNotIn(StringList keylist)
    {
        StringList names = this.getKeyStringList();

        // match current attribute against newlist;
        for (String name : names)
        {
            if (keylist.contains(name) == false)
            {
                this.remove(name);
            }
        }
    }

    /**
     * Match this VAttributeSet with template set. If attribute in templateSet
     * is not in this set, it will be copied. If attribute exists in this set,
     * it's type and flags will be copied, but not the actual value unless it is
     * NULL or empty. This allows updating of VAttributeSet while keeping their
     * value. Set boolean remoteOthers to true to remove attribute not in the
     * template set. Is used by ServerInfo to update Server Attributes.
     */
    public void matchTemplate(AttributeSet templateSet, boolean removeOthers)
    {
        StringList names = templateSet.getKeyStringList();

        for (String name : names)
        {
            if (this.containsKey(name) == false)
            {
                this.put(templateSet.get(name));
            }
            else
            {
                // update attribute type and attribute flags.
                // just copy old value into new Attribute
                Attribute newAttr = templateSet.get(name);
                Attribute oldAttr = get(name);
                // force set:
                Object oldVal = oldAttr.getValue();

                if ((oldVal != null) && StringUtil.isEmpty(oldVal.toString()) == false)
                {
                    // update with new value. type is automatfically adjusted. 
                    newAttr.setObjectValue(oldAttr.getValue());
                }
                // else keep new non empty value !
                // Overwrite:
                put(newAttr);
            }
        }

        if (removeOthers)
            removeIfNotIn(names);
    }

    /**
     * Returns sub set of attributes 
     */
    public AttributeSet getAttributes(String[] names)
    {
        AttributeSet subset = new AttributeSet();
        for (String name : names)
        {
            Attribute attr = this.get(name);
            if (attr != null)
                subset.put(attr.duplicate());
        }
        return subset;
    }

    public void add(Attribute[] attrs)
    {
        for (Attribute attr : attrs)
            this.put(attr);
    }

    /**
     * Copy to VRSProperties object. This will remove type data and enum values. 
     * @return VRSProperties object of this AttributeSet. 
     */
    public VRSProperties toVRSProperties()
    {
        VRSProperties props=new VRSProperties(this.getName());
        // respect order of attributes.
        for (String key:this.keySet())
        {
            props.set(key, this.get(key).getValue()); 
        }
        return props; 
    }
 
    public Attribute[] toArray()
    {
        return this.toArray(new Attribute[0]); 
    }
    
    public List<Attribute> toList()
    {
        return new ExtendedList<Attribute>(this.values());  
    }

    public void update(Attribute attr, boolean checkType)
    {
        Attribute org=this.get(attr.getName());
        if (org==null)
        {
            this.put(attr.getName(),attr);
            return; 
        }
        
        AttributeType orgType=org.getType();
        if (checkType)
        {
            // use same type
            org.setValue(orgType, attr.getValue());
        }
        else
        {
            // use new type: 
            org.setValue(attr.getType(), attr.getValue()); 
        }
        
    }

}
