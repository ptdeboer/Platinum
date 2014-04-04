package nl.esciencecenter.ptk.vbrowser.ui.dailog;

import nl.esciencecenter.ptk.vbrowser.ui.attribute.AttributeEditorForm;
import nl.esciencecenter.ptk.vbrowser.ui.attribute.AttributeViewer;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class ShowAttributeEditor
{

    /**
     * Auto-generated main method to display this JDialog
     */
    public static void main(String[] args)
    {
        try
        {
            AttributeSet attrs = new AttributeSet();

            attrs.put(new Attribute("longValue", (long) 10));
            attrs.put(new Attribute("StringValue", "String Value"));
            attrs.put(new Attribute("enum", new String[]
            { "aap", "noot", "mies" }, 1));

            attrs.put(new Attribute("VRLValue", new VRL("http://www.cnn.com/hello")));

            attrs.put(new Attribute("Attr-big-Text",
                    "Testing LONG Text,\n Testing LONG text\n"
                            + "***********************************************\n"
                            + "***********************************************\n"
                            + "***********************************************\n"
                            + "***********************************************\n"
                    ), true);

            for (String name : attrs.keySet())
            {
                attrs.setEditable(name, true);
            }

            Attribute[] newAttrs = AttributeEditorForm.editAttributes("Edit Attributes", attrs.toArray(), true);

            if (newAttrs==null)
            {
                System.out.printf("Cancelled!");
            }
            else for (Attribute attr : newAttrs)
            {
                System.out.printf(" -%s\n", attr);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}
