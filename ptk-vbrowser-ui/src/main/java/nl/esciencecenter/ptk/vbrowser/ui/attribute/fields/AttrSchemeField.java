package nl.esciencecenter.ptk.vbrowser.ui.attribute.fields;
///*
// * Copyright 2006-2010 Virtual Laboratory for e-Science (www.vl-e.nl)
// * Copyright 2012-2013 Netherlands eScience Center.
// *
// * Licensed under the Apache License, Version 2.0 (the "License").
// * You may not use this file except in compliance with the License. 
// * You may obtain a copy of the License at the following location:
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * 
// * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
// * ---
// */
//// source:
//
//package nl.esciencecenter.ui.proto.panels.fields;
//
//import javax.swing.InputVerifier;
//import javax.swing.JComponent;
//
//import nl.esciencecenter.ptk.data.StringList;
//import nl.esciencecenter.ptk.ui.SimpelUI;
//import nl.esciencecenter.ptk.ui.UI;
//import nl.esciencecenter.vbrowser.vrs.VRS;
//
//public class AttrSchemeField extends AttrEnumField
//{
//    private static final long serialVersionUID = 1688960537562990298L;
//
//    public AttrSchemeField()
//    {
//        super();
//        init();
//    }
//
//    public AttrSchemeField(String name, String values[])
//    {
//        super(name, values);
//        init();
//    }
//
//    protected void init()
//    {
//        setInputVerifier(new InputVerifier()
//        {
//            public boolean verify(JComponent input)
//            {
//                if (!(input instanceof AttrSchemeField))
//                    return true; // give up focus
//                return ((AttrSchemeField) input).isEditValid();
//            }
//        });
//    }
//
//    protected boolean isEditValid()
//    {
//        String schemes[] = VRS.getRegistry().getDefaultSchemeNames();
//        String scheme = VRS.getRegistry().getDefaultScheme(getValue());
//
//        if (StringList.hasEntry(schemes, scheme) == false)
//        {
//            boolean keep = getMasterUI().askYesNo("Not supported scheme",
//                    "The scheme: '" + scheme + "' is not recognised. Keep it anyway  ? ", false);
//
//            if (keep)
//                return true;
//            else
//                return false; // try again
//        }
//        else
//            return true;
//
//    }
//
//    private UI getMasterUI()
//    {
//        return new SimpelUI();
//    }
//
//}
