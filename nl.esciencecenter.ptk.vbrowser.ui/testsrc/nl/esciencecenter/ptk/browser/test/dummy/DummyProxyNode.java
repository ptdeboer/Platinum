/*
 * Copyrighted 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").  
 * You may not use this file except in compliance with the License. 
 * For details, see the LICENCE.txt file location in the root directory of this 
 * distribution or obtain the Apache License at the following location: 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
 * ---
 */
// source: 

package nl.esciencecenter.ptk.browser.test.dummy;

import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.data.LongHolder;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactory;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class DummyProxyNode extends ProxyNode
{
    static private Presentation dummyPresentation;

    static private StringList attrNames=null; 
    
    static
    {
        attrNames=new StringList(new String[]{"attr1","attr2","attr3","attr4"});
        
        dummyPresentation=Presentation.createDefault();
        for (int i=0;i<attrNames.size();i++)
            dummyPresentation.setAttributePreferredWidths(attrNames.get(i),new int[]{42,42+i*42,42+4*42}); 
        dummyPresentation.setChildAttributeNames(attrNames); 
        
    }

    // --- 
    
    private DummyProxyNode parent;
    private List<DummyProxyNode> childs;
	private boolean isComposite=true; 
	private String mimetype="text/plain";
	
    protected DummyProxyNode createChild(String childname)
    {
        return new DummyProxyNode(this,getVRL().appendPath(childname)); 
    }
    
    public DummyProxyNode(DummyProxyFactory dummyProxyFactory, VRL locator)
    {
        super(dummyProxyFactory,locator); 
        this.parent=null; 
    }
    
    protected DummyProxyNode(DummyProxyNode parent,VRL locator)
    {
        super(parent.getProxyFactory(),locator);
        this.parent=parent; 
        init(); 
    }

    private void init()
    {
    	;
    }
    
    @Override
    public boolean isBusy()
    {
        return false;
    }

    @Override
    public String getName()
    {
        return "DummyProxy:"+this.getID(); 
    }

    @Override
    public boolean hasChildren()
    {   
        return true; 
    }

    @Override
    public boolean isComposite()
    {
        return isComposite; 
    }

    @Override
    protected ProxyNode doGetParent() throws ProxyException
    {
        return getProxyFactory().doOpenLocation(this.locator.getParent());
    }
    
    @Override
    public String getMimeType()
    {
        return this.mimetype; 
    }

    @Override
    public List<? extends ProxyNode> doGetChilds(int offset, int range,LongHolder numChildsLeft)
    {
        if (childs==null)
        {	
        	
        	childs=new ArrayList<DummyProxyNode>(); 
            childs.add(createChild("child-"+id+".1")); 
            childs.add(createChild("child-"+id+".2")); 
            childs.add(createChild("child-"+id+".3")); 
            
            DummyProxyNode node = createChild("leaf-"+id+".4");
            node.isComposite=false; 
            node.mimetype="text/html"; 
            childs.add(node); 
            
            node = createChild("leaf-"+id+".5");
            node.isComposite=false; 
            node.mimetype="text/rtf"; 
            childs.add(node); 
        }
                
        return subrange(childs,offset,range);  
    }

	@Override
	protected String doGetMimeType() throws ProxyException 
	{
		return this.mimetype; 
	}

	@Override
	protected boolean doGetIsComposite() throws ProxyException
	{
		return this.isComposite; 
	}

    @Override
    protected String doGetName() 
    {
        return this.getVRL().getBasename(); 
    }

    @Override
    protected String doGetResourceType() 
    {
        return "DummyType"; 
    }

    @Override
    protected String doGetResourceStatus() 
    {
        return "NOP";
    }

    @Override
    protected List<String> doGetChildTypes() 
    {
        return new StringList("DummyType"); 
    }

    @Override
    protected List<String> doGetAttributeNames() throws ProxyException
    {
        return attrNames.clone();
    }

    @Override
    protected List<Attribute> doGetAttributes(List<String> names) throws ProxyException
    {
        ArrayList<Attribute> attrs=new ArrayList<Attribute>(names.size()); 
        
        for (int i=0;i<names.size();i++)
            attrs.add(new Attribute(names.get(i),"Value:"+names.get(i)));  
        
        return attrs;
        
    }

    @Override
    protected Presentation doGetPresentation()
    {
        return dummyPresentation; 
    }

}
