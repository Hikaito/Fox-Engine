package system.gui.mainWindow;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import system.Program;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MainWindow {

    // region initialization ------------------------------------------------

    // state variables
    private JFrame jFrame;              //overall window

    //constructor
    public MainWindow() throws IOException {
        //initialize frame
        jFrame = new JFrame(Program.getWindowTitle());   //initialize window title
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //instate close when exited
        jFrame.setSize(Program.getDefaultWindowWidth(), Program.getDefaultWindowHeight());   //initialize size

        //generate components that have dependencies
        imageLabel = new ImagePanel(); //generate canvas image container

        //draw window components
        drawAllInitialize();

        jFrame.setVisible(true);    //initialize visibility (needs to be at the end of initialization)
    }

    // endregion
    // region GUI update ----------------------------------------------

    //destroy window
    public void dispose(){jFrame.dispose();}

    //redraw canvas function (recalculates image)
    public void redrawCanvas(){
        imageLabel.setImage(Program.getCanvasImage()); //set new image to image label
        imageLabel.revalidate();    //redraw image label
    }

    //repaint layer editor
    public void repaintLayerEditor(){
        if (editorSidebar != null) editorSidebar.repaint();
    }

    //function that draws all contents to begin with
    private void drawAllInitialize(){
        //draw window components
        jFrame.setJMenuBar(menuBar);    //register menu bar as component
        generateMenu(menuBar); //generate menu bar
        generateSplitFrame(); //generate content
    }

    //external accessor for redrawing menu bar component
    public void regenMenuBar(){
        menuBar.removeAll();    //clear menu bar
        generateMenu(menuBar);  //rebuild menu bar items
        menuBar.revalidate();   //force redraw of menu bar
    }

    // message pass zoom
    public void zoomInCanvas(){
        imageLabel.zoomIn();
        //imageLabel.setIcon(imageLabel.zoomIn()); fixme does nothing?
    }

    // message pass zoom
    public void zoomOutCanvas(){
        imageLabel.zoomOut();
        //imageLabel.setIcon(imageLabel.zoomOut()); fixme does nothing?
    }

    // endregion
    // region component generation----------------------------------------------
    private ImagePanel imageLabel;                                  //label that holds the scaleable image for the canvas
    private JScrollPane imageScroll = new JScrollPane();            //scroll pane that holds the label with the canvas image
    private JMenuBar menuBar = new JMenuBar();                      //menu bar object
    private JPanel editorSidebar = null;

    //generate menu bar: construct top menu bar in given menu bar container
    protected void generateMenu(JMenuBar menuBar){
        JMenu menu1 = MenuPane.generateFileMenu();
        menuBar.add(menu1); //add menu component

        //Generate menu: view
        JMenu menu2 = MenuPane.generateViewMenu();
        menuBar.add(menu2); //add menu

        // Generate menu: project
        JMenu menu3 = MenuPane.generateProjectMenu();
        menuBar.add(menu3);

        //generate project resources menu
        menuBar.add(Program.getProjectMenu());
        menuBar.revalidate();
        menuBar.repaint();
    }

    //generate main area of frame: split panels (Generates main window)
    protected void generateSplitFrame(){
        //generate main components
        JScrollPane right = generateImagePanel();    //generate canvas half
        JPanel left = generateControlPane();  //generate layer editor half

        //configure for panel splits
        Double weight = 0.0;    //weight for resizing, 1.0 for canvas on right
        right.setMinimumSize(new Dimension(150, 50)); //minimum size for canvas area
        left.setPreferredSize(new Dimension(500, 7000));
        left.setMinimumSize(new Dimension(300, 50)); //minimum size for layer editor area

        //create split plane: right half is image canvas, left half is layer editor
        JSplitPane child = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);    //create splitter
        jFrame.add(child);  //add splitter to frame

        //resize weight: fill in image space when expanded; gives all weight to canvas
        child.setResizeWeight(weight);
    }

    //generate canvas scroll pane image region (Generates right side)
    protected JScrollPane generateImagePanel(){
        //image loading
        imageLabel = new ImagePanel();
        imageScroll = new JScrollPane(imageLabel);

        //set scroll acceleration to something more reasonable
        imageScroll.getVerticalScrollBar().setUnitIncrement(Program.getScrollMouseAcceleration());
        imageScroll.getHorizontalScrollBar().setUnitIncrement(Program.getScrollMouseAcceleration());

        return imageScroll;    //return scroll pane
    }

    //generate layer editor area of frame (Generates left side)
    protected JPanel generateControlPane(){
        // generate overall panel
        editorSidebar = new JPanel();
        editorSidebar.setLayout(new BoxLayout(editorSidebar, BoxLayout.Y_AXIS));  //stack components

        // add components
        editorSidebar.add(MainWindowComponents.makeZoomPanel(imageLabel));   //add zoom panel
        editorSidebar.add(MainWindowComponents.makeButtonRow());             // add global button row
        editorSidebar.add(Program.getLayerEditPanel());      // add layer editor
        editorSidebar.add(MainWindowComponents.makeLayerTreePanel());

        return editorSidebar;
    }

    // endregion
}