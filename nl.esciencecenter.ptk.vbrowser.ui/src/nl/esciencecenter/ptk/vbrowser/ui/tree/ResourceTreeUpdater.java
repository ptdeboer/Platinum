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

package nl.esciencecenter.ptk.vbrowser.ui.tree;

import java.util.List;

import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.model.DataSource;
import nl.esciencecenter.ptk.vbrowser.ui.model.UIViewModel;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNodeEvent;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNodeEventListener;
import nl.esciencecenter.ptk.vbrowser.ui.tasks.UITask;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Gets relevant data from DataSource and updates the ResourceTreeModel.  
 */
public class ResourceTreeUpdater implements ProxyNodeEventListener 
{
	private static ClassLogger logger;

	static
	{
		logger=ClassLogger.getLogger(ResourceTreeUpdater.class); 
	}
	  
	private ResourceTree tree;

	private DataSource viewNodeSource;

	private ViewNode rootItem;     

    public ResourceTreeUpdater(ResourceTree tree,DataSource viewNodeSource) 
    {
    	this.tree=tree;
    	setDataSource(viewNodeSource,false); 
    }
    
    public ResourceTreeModel getModel()
    {
        return tree.getModel(); 
    }
    
    public UIViewModel getUIModel()
    {
        return tree.getUIViewModel(); 
    }
    
    public void setDataSource(DataSource viewNodeSource,boolean update)
    {
        // unregister previous 
        if (viewNodeSource!=null)
            viewNodeSource.removeDataSourceListener(this); 
        
        this.viewNodeSource=viewNodeSource;
        
        // register me as listener 
        if (viewNodeSource!=null)
        {
        	viewNodeSource.addDataSourceListener(this); 
        }
        if (update)
        	updateRoot(); 
    }
    
    public void updateRoot()
    {
    	logger.debugPrintf("updateRoot():\n");
    	
    	UITask task=new UITask("update resource tree model for node"+rootItem)
        {
            @Override
            public void doTask()
            {
            	_updateRoot();                
            }

            @Override
            public void stopTask()
            {
            }
        }; 
        
        task.startTask(); 
    }

    /** Get or repopulate childs nodes */
	public void updateChilds(final ResourceTreeNode node) 
	{
		logger.debugPrintf("updateChilds():%s\n",node.getVRI());
    	
        UITask task = new UITask("update resource tree model for node"+rootItem)
        {
            @Override
            public void doTask()
            {
            	_updateChilds(node);                
            }

            @Override
            public void stopTask()
            {
            }
        }; 
        
        task.startTask(); 
	}
	
    // ========================================================================
    // Actual Update Tasks
    // ========================================================================
    
    private void _updateRoot()
    {
        try
        {
        	this.rootItem=viewNodeSource.getRoot(getUIModel());
        	logger.debugPrintf("updateRoot():%s\n",rootItem.getVRL());
        	
        	logger.debugPrintf("_updateRoot():%s\n",rootItem.getVRL());
    		
        	ResourceTreeNode rtRoot=new ResourceTreeNode(null,rootItem,true); 
        	getModel().setRoot(rtRoot);
        	
        	// pre fetch childs and populate root in advance: 
        	
        	ViewNode[] childs = viewNodeSource.getChilds(getUIModel(),rootItem.getVRL(),0,-1,null); 
        	
        	if (childs==null) 
        	{
        		rtRoot.clear();
        		rtRoot.setPopulated(true); 
        	}
        	else
        	{
        		getModel().setChilds(rtRoot,childs); 
        	}
        }
        catch (ProxyException e)
        {
            handle("Failed to update ResourceTree RootNode.",e); 
        } 
    }

	private void _updateChilds(final ResourceTreeNode node) 
	{
		ViewNode[] childs;
		
        try
        {
            childs = this.viewNodeSource.getChilds(getUIModel(),node.getVRI(),0,-1,null);
            getModel().setChilds(node,childs);
        }
        catch (ProxyException e)
        {
            handle("Failed to update child nodes.",e); 
        } 
 
	}

	private void handle(String actionText,Throwable e)
    {
	    this.tree.getMasterBrowser().handleException(actionText,e); 
    }

    @Override
	public void notifyDataSourceEvent(ProxyNodeEvent e) 
	{
        logger.debugPrintf("notifyDataSourceEvent:%s\n",e);
        
        VRL parent = e.getParent(); 
        VRL[] sources = e.getResources();
        
        switch(e.getType())
        {
            case RESOURCES_DELETED:
                deleteNodes(sources); 
                break; 
            case REFRESH_RESOURCES:
                refreshNodes(sources); 
                break; 
            case RESOURCES_ADDED:
                addNodes(parent,sources); 
                break; 
        }
	}

    private void deleteNodes(VRL[] sources)
    {
        for (VRL loc:sources)
        {
            List<ResourceTreeNode> nodes = getModel().findNodes(loc);
            
            if (nodes!=null)
            {
                for (ResourceTreeNode node:nodes)
                {
                    getModel().deleteNode(node,true); 
                }
            }
        }
    }
    
  
    private void refreshNodes(VRL[] sources)
    {
        for (VRL loc:sources)
        {
            List<ResourceTreeNode> nodes = getModel().findNodes(loc);
            
            if (nodes!=null)
            {
                for (ResourceTreeNode node:nodes)
                {
                    updateChilds(node); // does background refetch   
                }
            }
            
        }
    }
    
    private void addNodes(final VRL parent,final VRL[] sources)
    {
        logger.debugPrintf("addNodes():%s\n",parent);
        
        UITask task = new UITask("update resource tree model for node"+rootItem)
        {
            @Override
            public void doTask()
            {
                _addNodes(parent,sources);                
            }

            @Override
            public void stopTask()
            {
            }
        }; 
        
        task.startTask(); 
    }
    
    private void _addNodes(final VRL parent,final VRL[] sources)
    {
        try
        {
            ViewNode childs[]; 
            
            childs=viewNodeSource.getNodes(getUIModel(),sources); 
            
            List<ResourceTreeNode> nodes = getModel().findNodes(parent);
            
            if (nodes!=null)
                for (ResourceTreeNode node:nodes)
                    getModel().addNodes(node,childs); 
                
        }
        catch (ProxyException e)
        {
            handle("Couldn't add new nodes.",e); 
        } 
    }


}
