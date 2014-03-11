package nl.esciencecenter.ptk.vbrowser.ui;

import java.awt.Color;
import java.util.Map;

import nl.esciencecenter.vbrowser.vrs.VRSProperties;

public class UIProperties extends VRSProperties
{
    private static final long serialVersionUID = -3984791544175204642L;

    // ==============
    // Property Names
    // ==============
    
    public static final String TEXTFIELD_EDITABLE_BG_COLOR = "ui.textfield.editable.bgcolor";

    public static final String TEXTFIELD_EDITABLE_FG_COLOR = "ui.textfield.editable.fgcolor";

    public static final String TEXTFIELD_NON_EDITABLE_BG_COLOR = "ui.textfield.non_editable.bgcolor";

    public static final String TEXTFIELD_NON_EDITABLE_FG_COLOR = "ui.textfield.non_editable.fgcolor";

    public static final String LABEL_SELECTED_BG_COLOR = "ui.label.selected.bgcolor";
    
    public static final String LABEL_SELECTED_FG_COLOR = "ui.label.selected.fgcolor"; 

    // ========================
    // Default Property Values
    // ========================
    
    private static Color default_label_selected_bg_color = Color.darkGray;

    private static Color default_label_selected_fg_color = Color.black;
    
    // ========
    // Instance
    // ========

    public UIProperties()
    {
        super("UIProperties");
    }
    
    public UIProperties(UIProperties parent)
    {
        super("UIProperties",parent);
    }
    
    public UIProperties(Map<? extends Object, Object> properties)
    {
        super("UIProperties", properties, false);
    }

    public UIProperties(String name, Map<? extends Object, Object> properties)
    {
        super(name, properties, false);
    }

    public UIProperties duplicate()
    {
        return new UIProperties(getName(), getProperties());
    }

    public Color getSelectedBGColor()
    {
        return getColor(LABEL_SELECTED_BG_COLOR,default_label_selected_bg_color); 
    }
    
    public Color getSelectedFGColor()
    {
        return  getColor(LABEL_SELECTED_FG_COLOR,default_label_selected_fg_color); 
    }
  
    public Color getColor(String name,Color defaultValue)
    {
        Object value = this.get(name); 
        if (value==null)
        {
            return defaultValue; 
        }
        
        if (value instanceof Color)
        {
            return (Color)value; 
        }
        // auto decode 
        Color color=Color.decode(value.toString());
        this.set(name,color);
        return color; 
    }

    public void setColor(String name,Color defaultValue)
    {
        this.set(name, defaultValue); 
    }
    
}

