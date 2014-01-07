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

package nl.esciencecenter.ptk.task;

import java.net.URI;

/** 
 * Transfer Specific Monitor. 
 * Adds more meta fields specific for (VFS) File Transfers. 
 * 
 * @author Piter T. de Boer. 
 */
public class TransferMonitor extends TaskMonitorAdaptor
{
    private static int transferCounter=0; 
    
    private int transferId=0; 
    
    private URI sources[];

    private URI dest;

    private String actionStr;

    private int sourcesDone;

    public TransferMonitor(String action, URI sourceUris[], URI destVri)
    {
        this.transferId=transferCounter++; 
        this.actionStr=action; 
        this.sources=sourceUris;
        this.dest=destVri; 
    }

    public String getID()
    {
        return "transfer:#"+transferId; 
    }
    
    public URI getDestination()
    {
        return dest;
    }

    public URI getSource()
    {
    	if ((sources!=null) && (sources.length>0))
    		return sources[0];
    	return null;
    }

    public int getTotalSources()
    {
       if (sources!=null)
    	   return sources.length; 
       return 0; 
    }
        
    public void setSources(URI sources[])
    {
        this.sources=sources; 
    }

    public int getSourcesDone()
    {
        return sourcesDone; 
    }

    public void updateSourcesDone(int done)
    {
       this.sourcesDone=done; 
    }

    public String getActionType()
    {
        return actionStr;
    }
    
    
}
