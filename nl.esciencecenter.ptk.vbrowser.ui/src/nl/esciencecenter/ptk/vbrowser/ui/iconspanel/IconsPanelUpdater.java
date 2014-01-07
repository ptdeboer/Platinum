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

package nl.esciencecenter.ptk.vbrowser.ui.iconspanel;

import nl.esciencecenter.ptk.vbrowser.ui.model.DataSource;
import nl.esciencecenter.ptk.vbrowser.ui.model.UIViewModel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNodeEvent;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNodeEventListener;

public class IconsPanelUpdater implements ProxyNodeEventListener 
{
	private DataSource dataSource;
	
	private IconsPanel iconsPanel;

	private ViewNode rootNode;

	public IconsPanelUpdater(IconsPanel panel,DataSource dataSource) 
	{
		this.iconsPanel=panel; 
		this.dataSource=dataSource;
	    setDataSource(dataSource,true); 
	}
	

	public ViewNode getRootNode()
	{
		return rootNode; 
	}
	
	public UIViewModel getUIModel()
	{
		return this.iconsPanel.getUIViewModel(); 		
	}
	
	public void setDataSource(DataSource dataSource,boolean update)
	{
		// unregister
		if (this.dataSource!=null)
			this.dataSource.removeDataSourceListener(this); 
		
		this.dataSource=dataSource; 
		
		//register
		if (this.dataSource!=null)
			this.dataSource.addDataSourceListener(this); 
		
		if ((update) && (dataSource!=null))
			updateRoot(); 
	}

	@Override
	public void notifyDataSourceEvent(ProxyNodeEvent e) 
	{
		System.err.println("FIXME: DataSourceEvent:"+e); 
	}

	
	protected void updateRoot()
	{
		try
		{
		    if (dataSource==null)
		    {
		        updateChilds(null);// clear/reset; 
		        return;
		    }
		    
			this.rootNode=this.dataSource.getRoot(getUIModel()); 
			ViewNode[] childs = this.dataSource.getChilds(getUIModel(), rootNode.getVRL(),0,-1,null); 
			updateChilds(childs); 
		}
		catch (ProxyException e)
		{
			handle("Updating root location.",e); 
		}
	}

	private void updateChilds(ViewNode[] childs) 
	{
		this.iconsPanel.getModel().setChilds(createIconItems(childs));   
	}

	private IconItem[] createIconItems(ViewNode[] nodes)
	{
		if (nodes==null)
			return null; 
		
		int len=nodes.length; 
		
		IconItem items[]=new IconItem[len];
		
		for (int i=0;i<len;i++)
		{
			items[i]=createIconItem(nodes[i]); 
		}
		
		return items; 
	}

	protected IconItem createIconItem(ViewNode node)
    {
	    IconItem item=new IconItem(iconsPanel,getUIModel(),node);
	    item.initDND(iconsPanel.getPlatform().getTransferHandler(),iconsPanel.getDragGestureListener());
	    return item;
    }

    private void handle(String actionText, ProxyException e)
	{
    	this.iconsPanel.getMasterBrowser().handleException(actionText,e);
		
	}

	public DataSource getDataSource() 
	{
		return this.dataSource; 
	}
}
