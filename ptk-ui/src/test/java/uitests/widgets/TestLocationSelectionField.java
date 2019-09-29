//package uitests.widgets;
//
//import java.awt.BorderLayout;
//
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//
//import nl.esciencecenter.ptk.ui.dnd.DnDFlavors;
//import nl.esciencecenter.ptk.ui.widgets.LocationSelectionField;
//import nl.esciencecenter.ptk.ui.widgets.URIDropHandler;
//import nl.esciencecenter.ptk.ui.widgets.LocationSelectionField.LocationType;
//import nl.esciencecenter.ptk.util.logging.PLogger;
//
//public class TestLocationSelectionField {
//    public static void main(String args[]) {
//        JFrame frame = new JFrame();
//        JPanel panel = new JPanel();
//        panel.setLayout(new BorderLayout());
//        frame.getContentPane().add(panel);
//        //
//        PLogger.getLogger(DnDFlavors.class).setLevelToDebug();
//        PLogger.getLogger(URIDropHandler.class).setLevelToDebug();
//
//        LocationSelectionField selField = new LocationSelectionField(LocationType.DirType);
//        selField.setLocationText("file:/thisisalongdirectoryname/directory/subdirectory/");
//        panel.add(selField, BorderLayout.CENTER);
//        //
//        frame.pack();
//        frame.setVisible(true);
//    }
//}
