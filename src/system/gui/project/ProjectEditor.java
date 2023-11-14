package system.gui.project;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import system.project.ProjectManager;
import system.setting.CoreGlobal;
import system.backbone.EventE;
import system.project.treeElements.ProjectRoot;
import system.project.treeElements.ProjectUnitCore;
import system.project.treeElements.ProjectFile;
import system.project.treeElements.ProjectFolder;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.io.IOException;

public class ProjectEditor {

    protected ProjectManager source;
    JPanel editorPane = null;       // used for resetting display
    JPanel imageDisplay = null;     // used for resetting display
    JSplitPane splitPane = null;
    DefaultMutableTreeNode treeRoot = null;
    JTree tree;

    // used for drawing background decision
    private boolean[] color;

    //constructor
    public ProjectEditor(ProjectManager source) throws IOException {
        // initialize color decision
        color= new boolean[1];
        color[0] = true;

        //initialize frame
        this.source = source;

        //overall window
        JFrame jFrame = new JFrame("Project Editor");   //initialize window title
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //instate window close when exited
        jFrame.setSize(CoreGlobal.EDITOR_WINDOW_WIDTH,CoreGlobal.EDITOR_WINDOW_HEIGHT);   //initialize size
        jFrame.setLocationRelativeTo(null); //center window

        // on close, generate dictionary
        jFrame.addWindowListener(new java.awt.event.WindowAdapter(){
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent){
                source.updateDependencies();   //rebuild dictionary
            }
        });

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

        private final ProjectEditor editor;

        public RegenerateEvent(ProjectEditor editor){
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
        editorPane.add(ProjectEditorFunctions.makeTitleField(select, tree));    // title edit field
        editorPane.add(ProjectEditorFunctions.makeColorToggle(file, new RegenerateEvent(this)));           // use color edit field
        editorPane.add(ProjectEditorFunctions.makeBackgroundToggle(new RegenerateEvent(this), color));   // add color use toggle
        JPanel color = new JPanel();                                //make color generation
        ProjectEditorFunctions.addColorChooser(color, file, new RegenerateEvent(this));
        editorPane.add(color);

        //movement
        editorPane.add(ProjectEditorFunctions.makeMoveIn(select, tree, this));      //add move inward button
        editorPane.add(ProjectEditorFunctions.makeMoveOut(select, tree, this));      //add move outward button
        editorPane.add(ProjectEditorFunctions.makeMoveUp(select, tree, this));      //add move up button
        editorPane.add(ProjectEditorFunctions.makeMoveDown(select, tree, this));      //add move down button

        //other
        editorPane.add(ProjectEditorFunctions.makeFileSelectionField(select, new RegenerateEvent(this)));      //add delete button
        editorPane.add(ProjectEditorFunctions.makeRemove(select, tree, this));      //add delete button

        //repaint
        imageDisplay.revalidate();
        imageDisplay.repaint();
        editorPane.revalidate();
        editorPane.repaint();

    }

    // editor pane folder
    protected void generateFolderDetails(DefaultMutableTreeNode select){

        //refresh items in image pane
        imageDisplay.removeAll();
        editorPane.removeAll();

        //image display
        imageDisplay.add(new JLabel("no image"));   // add no image text

        //editor pane
        editorPane.add(new JLabel("Folder"));
        editorPane.add(ProjectEditorFunctions.makeTitleField(select, tree));
        editorPane.add(ProjectEditorFunctions.makeAddFolder(select, tree, source.getRoot()));   // add folder option
        editorPane.add(ProjectEditorFunctions.makeAddFile(select, tree, source.getRoot()));   // add file option

        //movement
        editorPane.add(ProjectEditorFunctions.makeMoveIn(select, tree, this));      //add move inward button
        editorPane.add(ProjectEditorFunctions.makeMoveOut(select, tree, this));      //add move outward button
        editorPane.add(ProjectEditorFunctions.makeMoveUp(select, tree, this));      //add move up button
        editorPane.add(ProjectEditorFunctions.makeMoveDown(select, tree, this));      //add move down button

        //other
        editorPane.add(ProjectEditorFunctions.makeRemove(select, tree, this));      //add delete button

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
        editorPane.add(ProjectEditorFunctions.makeTitleField(select, tree));
        editorPane.add(ProjectEditorFunctions.makeAddFolder(select, tree, source.getRoot()));   // add folder option
        editorPane.add(ProjectEditorFunctions.makeAddFile(select, tree, source.getRoot()));   // add file option

        // location pane
        JPanel title = new JPanel();
        title.add(new JLabel(unit.getPath()));
        title.setBackground(Color.white);
        editorPane.add(title);

        editorPane.add(ProjectEditorFunctions.makeSave(source));


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
