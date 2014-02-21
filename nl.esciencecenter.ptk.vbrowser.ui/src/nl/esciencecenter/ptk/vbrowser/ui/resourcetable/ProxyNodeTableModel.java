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

package nl.esciencecenter.ptk.vbrowser.ui.resourcetable;
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
//package nl.esciencecenter.vbrowser.vb2.ui.resourcetable;
//
//import nl.esciencecenter.ptk.data.StringList;
//import nl.esciencecenter.ptk.presentation.Presentation;
//import nl.esciencecenter.ptk.util.logging.ClassLogger;
//import nl.esciencecenter.vbrowser.vb2.ui.model.UIViewModel;
//import nl.esciencecenter.vbrowser.vb2.ui.proxy.ProxyNode;
//import nl.esciencecenter.vbrowser.vb2.ui.tasks.UITask;
//import nl.esciencecenter.vbrowser.vrs.data.Attribute;
//import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
//import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
//
///** 
// * Direct ResourceTableModel implementation for ProxyNode resources. 
// * Could use/implement ExtendedDataSource.  
// */
//public class ProxyNodeTableModel extends ResourceTableModel 
//{
//    private static final long serialVersionUID = 8402972068504033387L;
//    
//    private static ClassLogger logger;
//    
//    static
//    {
//        logger=ClassLogger.getLogger(ProxyNodeTableModel.class); 
//        logger.setLevelToDebug();
//    }
//    // === === //
//    
//    private ProxyNode pnode;
//
//    private UIViewModel uiModel;
//    
//    public ProxyNodeTableModel(UIViewModel uiModel,ProxyNode node)
//    {
//        this.pnode=node; 
//        this.uiModel=uiModel; 
//        init(); 
//    }
//    
//    public ProxyNodeTableModel(ProxyNode node)
//    {
//        this.pnode=node; 
//        this.uiModel=UIViewModel.createTableModel(); 
//        init(); 
//    }
//    
//    public void init()
//    {
//        if (pnode==null)
//        {
//            logger.debugPrintf("init(): >>> NULL ProxyNode <<<\n");
//            this.setHeaders(new String[0]); 
//            return; 
//        }
//        
//        // dummy tabel: 
//        StringList headers=new StringList();
//        
//        Presentation pres = pnode.getPresentation(); 
//        StringList allHeaders=new StringList(); 
//        
//        try
//        {
//            allHeaders.add(pnode.getAttributeNames());
//        }
//        catch(Exception e)
//        {
//            handle(e,"Couldn't get resource attribute names\n");
//        }
//
//        // set default attributes if there is no presentation or headers
//        // defined in the presentation  
//        if ((pres==null) || (headers.size()<=0)) 
//        {
//            headers.addAll(allHeaders); 
//        }
//        else
//        {
//            String[] names = pres.getChildAttributeNames();
//            for (String name:names)
//                headers.add(name);
//        }
//        
//        logger.debugPrintf("    headers = %s\n",headers.toString()); 
//        logger.debugPrintf("all headers = %s\n",allHeaders.toString()); 
//        
//        this.setHeaders(headers.toArray()); 
//        this.setAllHeaders(allHeaders); 
//        
//        fetchData(); 
//    }
//
//    private void fetchData()
//    {
//        // allowed at init time! 
//        if (pnode==null)
//        {
//            logger.debugPrintf("fetchData(): NULL pnode\n"); 
//            return; 
//        }
//        
//        this.clearData();
//        
//        UITask task=new UITask(null,"Test get ProxyNode data")
//        {
//            boolean mustStop=false; 
//            
//            public void doTask()
//            {
//                try
//                {
//                    ProxyNode nodes[];
//                    
//                    try
//                    {
//                        nodes = pnode.getChilds(); 
//                        logger.debugPrintf("fetchData(): #%d nodes\n",(nodes!=null)?nodes.length:"0"); 
//                    }
//                    catch (Exception e)
//                    {
//                        handle(e,"Couldn't fetch childs\n"); 
//                        return; 
//                    }
//                    
//                    for (ProxyNode node:nodes)
//                    {
//                        if (mustStop==true)
//                            return; 
//                        
//                        AttributeSet set=new AttributeSet();
//                        
//                        //prefill: 
//                        
//                        set.put("name",node.getVRL().getBasename()); 
//                        set.put("type",node.getResourceType()); 
//                        set.put("location",node.getVRL()); 
//                        
//                        addRow(node.getVRL().toString(),set);  
//                    }
//                    
//                    for (ProxyNode node:nodes)
//                    {
//                        if (mustStop==true)
//                            return; 
//                     
//                        VRL vrl=node.getVRL(); 
//                        
//                        String hdrs[] = getHeaders();   
//                        Attribute[] attrs;
//                        try
//                        {
//                            attrs = node.getAttributes(hdrs);
//                            setValues(vrl.toString(),attrs); 
//                        }
//                        catch (Exception e)
//                        {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//                catch(Throwable t)
//                {
//                    handle(t,"Failed to fetch table data\n");
//                }
//            }
//
//            public void stopTask()
//            {
//                
//            }
//        };
//        
//        task.startTask();
//    }
//
//    protected void handle(Throwable t,String format,Object... args)
//    {
//        logger.logException(ClassLogger.ERROR,t,format,args); 
//        t.printStackTrace();
//    }
//
//    public int insertHeader(String headerName, String newName, boolean insertBefore)
//    {
//        int index=super.insertHeader(headerName, newName, insertBefore); 
//        // will update data model, Table View will follow AFTER TableStructureEvent 
//        // has been handled. 
//        fetchAttribute(newName);
//        return index; 
//    }
//
//    private void fetchAttribute(String newName)
//    {
//        StringList attrNames=new StringList(newName); 
//        fetchAttributes(attrNames); 
//    }
//    
//    private void fetchAttributes(final StringList attrNames)
//    {
//        if (pnode==null)
//            return; 
//        
//        ProxyNode nodes[];
//        
//        try
//        {
//            // TODO: ViewFilter! 
//            nodes = pnode.getChilds();
//        }
//        catch (Exception e)
//        {
//            handle(e,"Couldn't fetch childs\n"); 
//            return; 
//        }
//        
//        for (ProxyNode node:nodes)
//        {
//            Attribute[] attrs;
//            try
//            {
//                attrs = node.getAttributes(attrNames.toArray());
//                setValues(node.getVRL().toString(),attrs);  
//            }
//            catch (Exception e)
//            {
//                handle(e,"Couldn't get Attributes\n"); 
//            } 
//        }
//    }
//    
//    
//    
//}
