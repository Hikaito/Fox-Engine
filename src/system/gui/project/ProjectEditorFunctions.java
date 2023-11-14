package system.gui.project;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import system.backbone.FileOperations;
import system.project.treeElements.ProjectFolderInterface;
import system.project.ProjectManager;
import system.setting.CoreGlobal;
import system.Program;
import system.gui.GuiOperations;
import system.backbone.ImageOperations;
import system.backbone.EventE;
import system.gui.WarningWindow;
import system.project.treeElements.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class ProjectEditorFunctions {

    //region shared properties--------------------------------------

    //generate title label
    public static JLabel makeTitle(ProjectUnitCore unit){
        return new JLabel(unit.getTitle());
    }

    // generate title text field
    public static JTextField makeTitleField(DefaultMutableTreeNode unit, JTree tree){
        ProjectUnitCore obj = (ProjectUnitCore) unit.getUserObject();

        JTextField title = new JTextField(20); //generate text field with preferred size
        title.setText(obj.getTitle());

        // action
        title.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // update unit title
                obj.setTitle(title.getText());

                //update tree title
                tree.updateUI();
            }
        });

        return title;
    }

    //region movement of layers=============================================
    // make button for moving objects
    public static JButton makeMoveIn(DefaultMutableTreeNode select, JTree tree, ProjectEditor caller){
        // create button
        JButton button = new JButton("Move Element Into Folder");

        // disable if illegal-----------------------
        boolean enabled = true;

        // get core [current layer]
        ProjectUnitCore core = (ProjectUnitCore) select.getUserObject();
        ProjectUnitCore sibling = null;

        // generate parent children
        LinkedList<ProjectUnitCore> children = null;
        if (core.getParent() instanceof ProjectFolderInterface) children = ((ProjectFolderInterface) core.getParent()).getChildren();  // get parent
        else enabled = false;   //should be impossible

        // continue if children
        if (children != null){
            int coreIndex = children.indexOf(core);

            // if index isn't base, continue
            if (coreIndex == 0) enabled = false;
            else{
                sibling = children.get(coreIndex - 1);
                //check if folder
                if (!(sibling instanceof ProjectFolder)) enabled = false;
            }
        }

        //if button is enabled, add action--------------------
        if (enabled){

            //effectively final variables
            LinkedList<ProjectUnitCore> finalChildren = children;
            ProjectFolder folderSibling = (ProjectFolder) sibling;

            //button action: on selection
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    // move in greater tree
                    finalChildren.remove(core);  //remove object
                    folderSibling.getChildren().addLast(core);  //add to sibling
                    core.setParent(folderSibling); //set sibling as parent

                    // move in gui tree
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) select.getParent();
                    DefaultMutableTreeNode sibling = select.getPreviousSibling();
                    parent.remove(select);
                    sibling.add(select);

                    //redraw tree
                    tree.updateUI();
                    //redraw editor
                    caller.regenerateEditorPane(null);
                }
            });
        }
        // if button is disabled, instead make it look disabled
        else{
            button.setBackground(Color.GRAY);
        }

        return button;
    }

    // make button for moving objects
    public static JButton makeMoveOut(DefaultMutableTreeNode select, JTree tree, ProjectEditor caller){
        // create button
        JButton button = new JButton("Move Element Out Of Folder");

        // disable if illegal-----------------------
        boolean enabled = true;

        // get core [current layer]
        ProjectUnitCore core = (ProjectUnitCore) select.getUserObject();
        LinkedList<ProjectUnitCore> children = null;
        LinkedList<ProjectUnitCore> olderChildren = null;

        //get parent
        ProjectUnitCore parent = core.getParent();
        if (parent instanceof ProjectRoot) enabled = false; //if parent is root, disable moving out
        else{
            //generate children of grandparent
            if (core.getParent() instanceof ProjectFolderInterface) children = ((ProjectFolderInterface) core.getParent()).getChildren();                 //prepare folder
            else enabled = false;   //should be impossible

            //generate older children
            if (parent.getParent() instanceof ProjectFolderInterface) olderChildren = ((ProjectFolderInterface) parent.getParent()).getChildren();                 //case root
            else enabled = false;   //should be impossible
        }

        //if button is enabled, add action--------------------
        if (enabled){

            //effectively final variables
            LinkedList<ProjectUnitCore> finalChildren = children;
            LinkedList<ProjectUnitCore> finalOlderChildren = olderChildren;

            //button action: on selection
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    // move out in greater tree
                    finalChildren.remove(core);
                    finalOlderChildren.addLast(core);
                    core.setParent(parent.getParent()); //set parent

                    // move in gui tree
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) select.getParent();
                    DefaultMutableTreeNode grandparent = (DefaultMutableTreeNode) parent.getParent();
                    parent.remove(select);
                    grandparent.add(select);

                    //redraw tree
                    tree.updateUI();
                    //redraw editor
                    caller.regenerateEditorPane(null);
                }
            });
        }
        // if button is diabled, instead make it look disabled
        else{
            button.setBackground(Color.GRAY);
        }

        return button;
    }

    // make button for moving objects
    public static JButton makeMoveUp(DefaultMutableTreeNode select, JTree tree, ProjectEditor caller){
        // create button
        JButton button = new JButton("Move Element Up");

        // disable if illegal-----------------------
        boolean enabled = true;

        // get core [current layer]
        ProjectUnitCore core = (ProjectUnitCore) select.getUserObject();

        // generate parent children
        LinkedList<ProjectUnitCore> children = null;
        if (core.getParent() instanceof ProjectFolderInterface) children = ((ProjectFolderInterface) core.getParent()).getChildren();
        else enabled = false;   //should be impossible

        // continue if children
        if (children != null){
            int coreIndex = children.indexOf(core);

            // if index isn't base, continue
            if (coreIndex == 0) enabled = false;
        }

        //if button is enabled, add action--------------------
        if (enabled){

            //effectively final variables
            LinkedList<ProjectUnitCore> finalChildren = children;

            //button action: on selection
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    // move in greater tree
                    int coreIndex = finalChildren.indexOf(core);    //get index
                    finalChildren.remove(core);  //remove object
                    finalChildren.add(coreIndex - 1, core);  //add as lower index

                    // move in gui tree
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) select.getParent();
                    int treeIndex = parent.getIndex(select);
                    parent.remove(select);
                    parent.insert(select, treeIndex-1);

                    //redraw tree
                    tree.updateUI();
                    //redraw editor
                    caller.regenerateEditorPane(select);
                }
            });
        }
        // if button is diabled, instead make it look disabled
        else{
            button.setBackground(Color.GRAY);
        }

        return button;
    }

    // make button for moving objects
    public static JButton makeMoveDown(DefaultMutableTreeNode select, JTree tree, ProjectEditor caller){
        // create button
        JButton button = new JButton("Move Element Down");

        // disable if illegal-----------------------
        boolean enabled = true;

        // get core [current layer]
        ProjectUnitCore core = (ProjectUnitCore) select.getUserObject();

        // generate parent children
        LinkedList<ProjectUnitCore> children = null;
        if (core.getParent() instanceof ProjectFolderInterface) children = ((ProjectFolderInterface) core.getParent()).getChildren();                 //case root
        else enabled = false;   //should be impossible

        // continue if children
        if (children != null){
            int coreIndex = children.indexOf(core);

            // if index isn't end, continue
            if (coreIndex == children.size() - 1) enabled = false;
        }

        //if button is enabled, add action--------------------
        if (enabled){

            //effectively final variables
            LinkedList<ProjectUnitCore> finalChildren = children;

            //button action: on selection
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    // move in greater tree
                    int coreIndex = finalChildren.indexOf(core);    //get index
                    finalChildren.remove(core);  //remove object
                    finalChildren.add(coreIndex + 1, core);  //add as lower index

                    // move in gui tree
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) select.getParent();
                    int treeIndex = parent.getIndex(select);
                    parent.remove(select);
                    parent.insert(select, treeIndex+1);

                    //redraw tree
                    tree.updateUI();
                    //redraw editor
                    caller.regenerateEditorPane(select);
                }
            });
        }
        // if button is diabled, instead make it look disabled
        else{
            button.setBackground(Color.GRAY);
        }

        return button;
    }

    //endregion

    // make button for deleting objects
    public static JButton makeRemove(DefaultMutableTreeNode select, JTree tree, ProjectEditor caller){
        // create button
        JButton button = new JButton("Delete Element");

        //button action: on selection
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //invoke action with warning
                WarningWindow.warningWindow("Are you sure you want to delete this node?", new DeleteEvent(select, tree, caller));

            }
        });

        return button;
    }

    // action enactor for removing an element from the tree
    public static class DeleteEvent implements EventE {
        DefaultMutableTreeNode select;
        JTree tree;
        ProjectEditor caller;
        public DeleteEvent(DefaultMutableTreeNode select, JTree tree, ProjectEditor caller){
            this.select = select;
            this.tree = tree;
            this.caller = caller;
        }

        @Override
        public void enact() {
            ProjectUnitCore core = (ProjectUnitCore) select.getUserObject();
            DefaultMutableTreeNode treeParent = (DefaultMutableTreeNode) select.getParent();

            //get parent kid list
            LinkedList<ProjectUnitCore> parentKids;
            ProjectUnitCore parent = core.getParent();
            if (parent instanceof ProjectFolderInterface) parentKids = ((ProjectFolderInterface)parent).getChildren();
            else return;

            // copy children to parent
            if (core instanceof ProjectFolder){
                //copy project children------------------------
                LinkedList<ProjectUnitCore> children = ((ProjectFolder) core).getChildren();
                while(children.size() != 0){
                    ProjectUnitCore child = children.removeFirst();
                    child.setParent(core.getParent());
                    parentKids.addLast(child); //add first element to end of parent
                }

                //copy tree children------------------------
                while(select.getChildCount() != 0){
                    DefaultMutableTreeNode kid = (DefaultMutableTreeNode) select.getFirstChild();   //get first child
                    select.remove(0);   //remove first child
                    treeParent.add(kid);    //add child to parent
                }
            }

            //remove from real tree
            parentKids.remove(core);

            //remove from fake tree
            treeParent.remove(select);

            //redraw tree
            tree.updateUI();
            //redraw editor
            caller.regenerateEditorPane(null);
        }
    }

    //endregion
    //region file properties--------------------------------------

    // file selection
    // generate title text field
    public static JPanel makeFileSelectionField(DefaultMutableTreeNode unit, EventE event){

        //create panel
        JPanel panel = new JPanel();
        panel.setBackground(Color.white);

        //get core
        if (!(unit.getUserObject() instanceof ProjectFile)) return panel;   // exit early if wrong type of file
        ProjectFile core = (ProjectFile) unit.getUserObject();          // get core

        // create file
        panel.add(new JLabel(core.getPath()));

        // create button
        JButton button = new JButton("Change");
        panel.add(button);
        // action
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // select file
                String file = FileOperations.selectFile(Program.getProjectPath());  // select file from project path

                // force file to be within project
                if(!file.contains(Program.getProjectPath())){
                    WarningWindow.warningWindow("Selected file not in project. This file cannot be used.");
                    return;
                }

                // strip file from directory
                file = String.valueOf(FileOperations.trimDirectory(Program.getProjectPath(), file));

                // change element path
                core.setPath(file);

                // redraw
                event.enact();
            }
        });

        return panel;
    }

    //endregion
    //region color ------------------------------

    //generate color toggle for layer
    public static JCheckBox makeColorToggle(ProjectFile unit, EventE event){
        //create toggle; set value to existing clipping values
        JCheckBox clipBox = new JCheckBox("use color", unit.getUseColor());

        //on selection, toggle using original color
        clipBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                unit.setUseColor(!unit.getUseColor());  // change use color by default

                // redraw
                event.enact();
            }
        });

        return clipBox;
    }

    //generate color toggle for layer
    public static JCheckBox makeBackgroundToggle(EventE editor, boolean[] toggle){
        //create toggle; set value to existing clipping values
        JCheckBox clipBox = new JCheckBox("red backdrop", toggle[0]);

        //on selection, toggle using original color
        clipBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggle[0] = !toggle[0]; // invert selection

                // redraw
                editor.enact();
            }
        });

        return clipBox;
    }

    //add color chooser to field (and helper)
    public static void addColorChooser(JPanel innerField, ProjectFile unit, EventE redraw){addColorChooser(innerField, 1, Color.BLACK, unit, redraw);}

    //text positions: 0 = none, 1 = left, 2 = right
    public static void addColorChooser(JPanel innerField, int textPosition, Color textColor, ProjectFile unit, EventE redraw){
        //create button with image color
        JButton colorButton = new JButton("     ");
        colorButton.setBackground(unit.getColor());    //set color

        //make label for color text NOTE: text field is selectable
        JTextField colorLabel = new JTextField(GuiOperations.formatColor(unit.getColor())); //get color label from unit color
        colorLabel.setEditable(false);
        colorLabel.setForeground(textColor);  //set text color to make it visible on darker bg

        //positioning of text
        switch(textPosition){
            //no text
            case 0:
                innerField.add(colorButton);
                break;
            //text to left
            case 1:
                innerField.add(colorButton);
                innerField.add(colorLabel);
                break;
            //text to right
            case 2:
                innerField.add(colorLabel);
                innerField.add(colorButton);
                break;
        }

        //on button click, change color
        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                //FIXME the show dialogue creates a new window, but it disregards any custom works. To do: rework so only HSV pane
                //FIXME generates an error now when exiting from the color selector without making a selection

                //Collect color from color selection dialogue; resets unit color
                Color selection = JColorChooser.showDialog(null,"Select a color", unit.getColor());
                //System.out.println(selection);

                //if color was a color, alter color choices
                if(selection == null) return;
                //Program.addUndo(unit.setColor(selection));
                unit.setColor(selection);

                //process color change in GUI
                colorButton.setBackground(unit.getColor());    //reset color for button
                colorLabel.setText(GuiOperations.formatColor(unit.getColor())); //reset color label
                innerField.revalidate(); //repaint

                // regenerate editor pane
                redraw.enact();
            }
        });
    }
    //endregion
    //region images ------------------------
    // generate image
    // image display
    // if file is valid, load image
    public static void loadImage(ProjectFile file, String filepath, JPanel destination, Boolean colorBackground){
        // if the file has an image to generate, generate
        if (file.isValid()){
            // load the file if the file exists
            try {
                BufferedImage image = ImageIO.read(new File(filepath));

                // if use color activated, redraw with color
                if(file.getUseColor()) image = ImageOperations.colorAlpha(image, file.getColor());

                // draw with background
                if (colorBackground) image = ImageOperations.colorBackground(image, Color.red);

                //resize image
                double imageH = image.getHeight();
                double imageW = image.getWidth();

                double ratio = imageH / imageW;

                int MaxSize = CoreGlobal.EDITOR_WINDOW_IMAGE;
                double resizeRatio = 1;

                // if taller than wide, use tallness
                if (ratio > 1){
                    resizeRatio = imageH / MaxSize;
                }
                //else use wideness
                else resizeRatio = imageW / MaxSize;

                //create resized image
                Image resize = image.getScaledInstance((int) (imageW / resizeRatio), (int) (imageH / resizeRatio), Image.SCALE_SMOOTH);

                destination.add(new JLabel(new ImageIcon(resize)));
            }
            // if loading fails, log failure and remove the valid mark
            catch (IOException e) {
                Program.log("File '" + filepath + "' could not be loaded as image.");
                file.setValid(false);
                destination.add(new JLabel("file image failed to load"));   // add no image text
            }
        }
    }

    //endregion
    //region root properties--------------------------------------

    // make add folder button [use by root or folder only]
    public static JButton makeAddFolder(DefaultMutableTreeNode select, JTree tree, ProjectRoot root){
        // create button
        JButton button = new JButton("Add Folder Unit");

        //button action: on selection, toggle expansion
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectUnitCore core = (ProjectUnitCore) select.getUserObject();    // get core

                //get children
                LinkedList<ProjectUnitCore> children;
                if (core instanceof ProjectFolderInterface) children = ((ProjectFolderInterface) core).getChildren();         //case root
                else return;

                //create new unit
                ProjectFolder newUnit = new ProjectFolder("new folder", root.getNextID());    //create new folder

                //add to project and to tree
                children.addLast(newUnit);   //add to project
                newUnit.setParent(core);
                select.add(new DefaultMutableTreeNode(newUnit));//add to gui tree

                //redraw tree
                tree.updateUI();
            }
        });

        return button;
    }

    // make add file button [use by root or folder only]
    public static JButton makeAddFile(DefaultMutableTreeNode select, JTree tree, ProjectRoot root){
        // create button
        JButton button = new JButton("Add File Unit");

        //button action: on selection, toggle expansion
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectUnitCore core = (ProjectUnitCore) select.getUserObject();    // get core

                //get children
                LinkedList<ProjectUnitCore> children;
                if (core instanceof ProjectFolderInterface) children = ((ProjectFolderInterface) core).getChildren();         //case root
                else return;

                //create new unit
                ProjectFile newUnit = new ProjectFile( "new file", root.getNextID());    //create new file

                //add to project and to tree
                children.addLast(newUnit);   //add to project
                newUnit.setParent(core);
                select.add(new DefaultMutableTreeNode(newUnit));//add to gui tree

                //redraw tree
                tree.updateUI();
            }
        });

        return button;
    }

    // save project
    public static JButton makeSave(ProjectManager source){
        // create button
        JButton button = new JButton("Save Project");

        //button action: on selection, toggle expansion
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //save file
                source.saveFile();

                WarningWindow.warningWindow("Project file saved.");
            }
        });

        return button;
    }

    // endregion
    //region file selection --------------------------------------

    // event class
    public static abstract class FileSelectionEvent{
        public abstract void makeSelection(long fileId);
    }

    // select file: layer
    public static JButton makeSelection(ProjectFile unit, FileSelectionEvent select, EventE event){
        // create button
        JButton button = new JButton("Select File");

        //button action: on selection, toggle expansion
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //set layer
//                BufferedImage image = Program.getImage(unit.getID());
//                //if image loaded properly, initalize a node
//                if (image != null){
//                    //layer.setImage(image, layer.getTitle(), unit.getID());
//
//                }
//                else{
//                    Program.log("File missing for element " + unit.getTitle() + " at " + unit.getPath());
//                }
                select.makeSelection(unit.getID());

                event.enact();  //close window
            }
        });

        return button;
    }
    //endregion
}
