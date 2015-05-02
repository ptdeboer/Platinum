package uitests.dnd;

import javax.swing.JFrame;

public class TestDropTextPanel
{
    public static JFrame createTestFrame()
    {
        
        JFrame frame=new JFrame(); 
        DnDTestPanel panel=new DnDTestPanel(); 
        panel.setSize(400, 400);
        frame.add(panel);
        frame.pack(); 
        frame.setSize(400,400); 
        frame.setVisible(true);
        return frame; 
    }
    
    public static void main(String args[])
    {
        createTestFrame(); 
        
    }


    
}

