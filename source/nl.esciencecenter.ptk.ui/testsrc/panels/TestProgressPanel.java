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

package panels;

import java.util.Date;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.task.ActionTask;
import nl.esciencecenter.ptk.ui.panels.monitoring.ProgresPanel;

public class TestProgressPanel
{

    /**
     * Auto-generated main method to display this 
     * JPanel inside a new JFrame.
     */
     public static void main(String[] args) 
     {
         JFrame frame = new JFrame();
         final ProgresPanel panel=new ProgresPanel(); 
         frame.getContentPane().add(panel);
         
         final long startTime=System.currentTimeMillis();
         
         ActionTask task=new ActionTask(null,"StatusPanel tester")
         {
             public void doTask()
             {
                 //Presentation pres=Presentation.createDefault(); 

                 panel.setTotal(1000);
                 
                 for (int i=0;i<=1000;i++)
                 {
                     // panel.setProgress(i); 
                     panel.setProgress((double)i/1000.0); 
                     panel.setProgressText(" "+i+" out of:"+1000); 
                     
                     long deltaTime=System.currentTimeMillis()-startTime; 
                     panel.setTimeText(Presentation.createRelativeTimeString(deltaTime,false)); 
                     
                     try
                     {
                         Thread.sleep(50);
                     }
                     catch (InterruptedException e)
                     {
                         e.printStackTrace();
                     }
                     
                     if (this.isCancelled())
                     {
                         return; 
                     }
                 }

             }

             @Override
             public void stopTask()
             {
                 
             }
         };
         
         task.startTask();
         
         frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         frame.pack();
         frame.setVisible(true);
         
     }
     
}
