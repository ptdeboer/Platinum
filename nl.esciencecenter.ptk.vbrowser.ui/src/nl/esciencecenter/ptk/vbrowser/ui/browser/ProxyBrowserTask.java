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

package nl.esciencecenter.ptk.vbrowser.ui.browser;

import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.task.ITaskSource;
import nl.esciencecenter.ptk.vbrowser.ui.tasks.UITask;

public abstract class ProxyBrowserTask extends UITask 
{
	private ProxyBrowserController browserController=null;

    public ProxyBrowserTask(ProxyBrowserController browserController,String taskName) 
	{
		super(browserController.getTaskWatcher(),taskName);
		this.browserController=browserController; 
	}
	
    public ProxyBrowserTask(ProxyBrowserController browserController, String taskName, ITaskMonitor monitor)
    {
        super(browserController.getTaskWatcher(), taskName, monitor);
        this.browserController=browserController; 
    }
    
	@Override
	protected void stopTask() throws Exception
	{
	    browserController.messagePrintf(this,"StopTask NOT implemented for:%s\n",this); 
	}

}
