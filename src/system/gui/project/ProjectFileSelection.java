package system.gui.project;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import system.project.ProjectManager;
import system.setting.CoreGlobal;
import system.layerTree.data.Folder;
import system.layerTree.data.Layer;
import system.backbone.EventE;
import system.project.treeElements.ProjectFile;
import system.project.treeElements.ProjectFolder;
import system.project.treeElements.ProjectRoot;
import system.project.treeElements.ProjectUnitCore;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class ProjectFileSelection {

    private final JFrame jFrame;
    protected ProjectManager source;
    JPanel editorPane = null;  // used for resetting display
    JPanel imageDisplay = null;     // used for resetting display
    JSplitPane splitPane = null;
    DefaultMutableTreeNode treeRoot = null;
    JTree tree;


    // used for drawing background decision
    private final boolean[] color;
    private Layer layer = null;
    private Folder folder = null;
    private boolean isLayer = false;

    //constructor
    public ProjectFileSelection(ProjectManager source, Layer layer) throws IOException {
        // initialize color decision
        color= new boolean[1];
        color[0] = true;
        this.layer = layer;
        isLayer = true;

        //initialize frame
        this.source = source;

        //overall window
        jFrame = new JFrame("Select File From Project");   //initialize window title
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //instate window close when exited
        jFrame.setSize(CoreGlobal.EDITOR_WINDOW_WIDTH,CoreGlobal.EDITOR_WINDOW_HEIGHT);   //initialize size
        jFrame.setLocationRelativeTo(null); //center window

        //label region
        imageDisplay = new JPanel();
        imageDisplay.setLayout(new GridBagLayout());    //center items

        //editor region
        editorPane = new JPanel();
        //editorPane.setLayout(new BoxLayout(editorPane, BoxLayout.Y_AXIS));
        editorPane.setLayout(new FlowLayout()); //fixme I want this to be vertical but I'm not willing to fight it

        //prepare regions
        regenerateEditorPane(null); // initialize to null selection

        // tree region
        treeRoot = source.getTree();
        tree = generateTree();

        JSplitPane secondaryPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imageDisplay, editorPane);
        //new JScrollPane(editorPane,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER ));
        secondaryPane.setResizeWeight(.7);

        //Main split pane; generate tree [needs to initialize under editor for null selection reasons]
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), secondaryPane);
        splitPane.setResizeWeight(.3);
        jFrame.add(splitPane);


        jFrame.setVisible(true);    //initialize visibility (needs to be at the end of initialization)
    }

    public ProjectFileSelection(ProjectManager source, Folder folder) throws IOException {
        // initialize color decision
        color= new boolean[1];
        color[0] = true;
        this.folder = folder;
        isLayer = false;

        //initialize frame
        this.source = source;

        //overall window
        jFrame = new JFrame("Select File From Project");   //initialize window title
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //instate window close when exited
        jFrame.setSize(CoreGlobal.EDITOR_WINDOW_WIDTH,CoreGlobal.EDITOR_WINDOW_HEIGHT);   //initialize size
        jFrame.setLocationRelativeTo(null); //center window

        //label region
        imageDisplay = new JPanel();
        imageDisplay.setLayout(new GridBagLayout());    //center items

        //editor region
        editorPane = new JPanel();
        //editorPane.setLayout(new BoxLayout(editorPane, BoxLayout.Y_AXIS));
        editorPane.setLayout(new FlowLayout()); //fixme I want this to be vertical but I'm not willing to fight it

        //prepare regions
        regenerateEditorPane(null); // initialize to null selection

        // tree region
        treeRoot = source.getTree();
        tree = generateTree();

        JSplitPane secondaryPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imageDisplay, editorPane);
        //new JScrollPane(editorPane,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER ));
        secondaryPane.setResizeWeight(.7);

        //Main split pane; generate tree [needs to initialize under editor for null selection reasons]
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), secondaryPane);
        splitPane.setResizeWeight(.3);
        jFrame.add(splitPane);


        jFrame.setVisible(true);    //initialize visibility (needs to be at the end of initialization)
    }

    public static class CloseWindow implements EventE {
        JFrame source;

        public CloseWindow(JFrame source){
            this.source = source;
        }

        @Override
        public void enact() {
            // simulate press of x button
            source.dispatchEvent(new WindowEvent(source, WindowEvent.WINDOW_CLOSING));
        }
    }

    // variable used for selection memory
    ProjectUnitCore activeSelection = new ProjectFolder("NULL", -1);  // empty file that is overwritten

    protected JTree generateTree(){
        // tree panel generation
        JTree tree = new JTree(treeRoot);                       // make tree object
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);   // set tree as selectable, one at a time
        //selection listener
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                // on selection, call selection update
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();  //get last selected item

                regenerateEditorPane(node);

            }
        });

        return tree;
    }

    // class for regenerating editor pane
    public static class RegenerateEvent implements EventE {

        private final ProjectFileSelection editor;

        public RegenerateEvent(ProjectFileSelection editor){
            this.editor = editor;
        }

        @Override
        public void enact() {
            editor.regenerateEditorPane();
        }
    }

    protected void regenerateEditorPane(){
        regenerateEditorPane((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
    }
    protected void regenerateEditorPane(DefaultMutableTreeNode selection){
        //if no selection, render for no selection
        if(selection == null){
            generateNullDetails();  // if no selection, render for no selection
            activeSelection = null; //update selection
            return;
        }

        // render selection
        ProjectUnitCore select = (ProjectUnitCore) selection.getUserObject();

        // if selection has not changed, do nothing
        //if (select == activeSelection) return; //removed so this can be used to intentionally refresh data

        //otherwise generate appropriate details
        if (select == null) generateNullDetails();
        else if (select instanceof ProjectFile) generateFileDetails(selection);
        else if (select instanceof ProjectFolder) generateFolderDetails(selection);
        else if (select instanceof ProjectRoot) generateRootDetails(selection);

        // update selection memory
        activeSelection = select;
    }

    // editor pane file
    protected void generateFileDetails(DefaultMutableTreeNode select){

        ProjectFile file = (ProjectFile) select.getUserObject();

        //refresh items in image pane
        imageDisplay.removeAll();
        editorPane.removeAll();

        // load image
        ProjectEditorFunctions.loadImage(file, source.getAbsolutePath(file), imageDisplay, color[0]);

        //editor pane
        editorPane.add(new JLabel("File"));

        // title
        editorPane.add(ProjectEditorFunctions.makeTitle((ProjectUnitCore) select.getUserObject()));
        editorPane.add(ProjectEditorFunctions.makeBackgroundToggle(new RegenerateEvent(this), color));   // add color use toggle

        // file version
        if(isLayer){
            //fixme add more features
            // use color
            // color
            // use color in the back
            // reset image properties

            // select image
            editorPane.add(ProjectEditorFunctions.makeSelection((ProjectFile) select.getUserObject(), new SelectFileLayer(layer), new CloseWindow(jFrame)));
        }
        // folder version
        else{
            // select image
            editorPane.add(ProjectEditorFunctions.makeSelection((ProjectFile) select.getUserObject(), new SelectFileFolder(folder), new CloseWindow(jFrame)));
        }

        //repaint
        imageDisplay.revalidate();
        imageDisplay.repaint();
        editorPane.revalidate();
        editorPane.repaint();

    }

    // layer version
    public static class SelectFileLayer extends ProjectEditorFunctions.FileSelectionEvent{
        private Layer layer;
        private long fileId;
        SelectFileLayer(Layer layer){
            this.layer = layer;
        }

        @Override
        public void makeSelection(long fileId) {
            this.layer.setImage(fileId);
        }
    }

    // folder version
    public static class SelectFileFolder extends ProjectEditorFunctions.FileSelectionEvent{
        private Folder folder;
        private long fileId;
        SelectFileFolder(Folder folder){
            this.folder = folder;
        }

        @Override
        public void makeSelection(long fileId) {
            this.folder.setIntersectImage(fileId);
        }
    }

    // folder version

    // editor pane folder
    protected void generateFolderDetails(DefaultMutableTreeNode select){

        //refresh items in image pane
        imageDisplay.removeAll();
        editorPane.removeAll();

        //image display
        imageDisplay.add(new JLabel("no image"));   // add no image text

        //editor pane
        editorPane.add(new JLabel("Folder"));
        editorPane.add(ProjectEditorFunctions.makeTitle((ProjectUnitCore) select.getUserObject()));

        //repaint
        imageDisplay.revalidate();
        imageDisplay.repaint();
        editorPane.revalidate();
        editorPane.repaint();
    }

    // editor pane root
    protected void generateRootDetails(DefaultMutableTreeNode select){

        // get item inside
        ProjectRoot unit = (ProjectRoot) select.getUserObject();

        //refresh items in image pane
        imageDisplay.removeAll();
        editorPane.removeAll();

        //image display
        imageDisplay.add(new JLabel("no image"));   // add no image text

        //editor pane
        editorPane.add(new JLabel("Project"));
        editorPane.add(ProjectEditorFunctions.makeTitle((ProjectUnitCore) select.getUserObject()));

        // location pane
        JPanel title = new JPanel();
        title.add(new JLabel(unit.getPath()));
        title.setBackground(Color.white);
        editorPane.add(title);


        //repaint
        imageDisplay.revalidate();
        imageDisplay.repaint();
        editorPane.revalidate();
        editorPane.repaint();

    }

    // empty editor pane
    protected void generateNullDetails(){

        //refresh items in image pane
        imageDisplay.removeAll();
        editorPane.removeAll();

        //image display
        imageDisplay.add(new JLabel("[no item selected]"));   // add no image text

        //editor pane
        editorPane.add(new JLabel("[no element selected]"));

        //repaint
        imageDisplay.revalidate();
        imageDisplay.repaint();
        editorPane.revalidate();
        editorPane.repaint();

    }

}
