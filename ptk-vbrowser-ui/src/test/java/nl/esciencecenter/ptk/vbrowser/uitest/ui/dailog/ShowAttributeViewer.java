package nl.esciencecenter.ptk.vbrowser.uitest.ui.dailog;

import nl.esciencecenter.ptk.vbrowser.ui.attribute.AttributeViewer;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;

public class ShowAttributeViewer
{

    /**
     * Auto-generated main method to display this JDialog
     */
    public static void main(String[] args)
    {
        AttributeViewer inst = new AttributeViewer();
        inst.setVisible(true);
        
        AttributeViewer.viewAttribute(new Attribute("Attr-Long",(long)10));
        AttributeViewer.editAttribute(new Attribute("Attr-big-Text",
                "Testing LONG Text,\n Testing LONG text\n"
                +"***********************************************\n"
                +"***********************************************\n"
                +"***********************************************\n"
                +"***********************************************\n"
                ),true);
    }

    
}
