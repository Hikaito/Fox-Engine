package system.gui.project;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import system.project.ProjectManager;
import system.setting.CoreGlobal;
import system.backbone.EventE;
import system.gui.WarningWindow;
import system.project.treeElements.ProjectFile;
import system.project.treeElements.ProjectFolderInterface;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.SortedSet;

public class ProjectReconciliation {

    protected ProjectManager source;
    SortedSet<ProjectFile> existingSet;
    SortedSet<ProjectFile> discoveredSet;
    JTree existingTree;
    JTree discoveredTree;
    DefaultMutableTreeNode existingRoot;
    DefaultMutableTreeNode discoveredRoot;
    DefaultMutableTreeNode existingSelect = null;
    DefaultMutableTreeNode discoveredSelect = null;

    JPanel imagePanel;
    JPanel editorPanel;
    JFrame jFrame;

    // used for drawing background decision
    private boolean[] color;

    //constructor
    public ProjectReconciliation(ProjectManager source, SortedSet<ProjectFile> existingSet, SortedSet<ProjectFile> newSet) throws IOException {
        // initialize color decision
        color= new boolean[1];
        color[0] = true;

        //initialize frame
        this.source = source;
        this.existingSet = existingSet;
        this.discoveredSet = newSet;

        //overall window
        jFrame = new JFrame("Project Reconciliation");   //initialize window title
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //instate window close when exited
        jFrame.setSize(CoreGlobal.RECONCILIATION_WINDOW_WIDTH,CoreGlobal.RECONCILIATION_WINDOW_HEIGHT);   //initialize size
        jFrame.setLocationRelativeTo(null); //center window

        // on close, generate dictionary
        jFrame.addWindowListener(new java.awt.event.WindowAdapter(){
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent){
                source.updateDependencies();   //rebuild dictionary
            }
        });

        // create tree roots
        existingRoot = generateTreeRoot(existingSet, "Files in Project");
        discoveredRoot = generateTreeRoot(discoveredSet, "Newly Discovered Files");

        // create tree for loaded files
        existingTree = new JTree(existingRoot);                       // make tree object
        existingTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);   // set tree as selectable, one at a time
        //selection listener
        existingTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                // on selection, call selection update
                // if there is a selection
                if (existingTree.getLastSelectedPathComponent() != null){
                    // if the selection is a file node
                    if (((DefaultMutableTreeNode) existingTree.getLastSelectedPathComponent()).getUserObject() instanceof ProjectFile){
                        existingSelect = (DefaultMutableTreeNode) existingTree.getLastSelectedPathComponent();  //select item
                    }
                    else existingSelect = null;
                }
                else existingSelect = null;

                regeneratePanes();
            }
        });

        // create tree for loaded files
        discoveredTree = new JTree(discoveredRoot);                       // make tree object
        discoveredTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);   // set tree as selectable, one at a time
        //selection listener
        discoveredTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                // on selection, call selection update
                // if there is a selection
                if (discoveredTree.getLastSelectedPathComponent() != null){
                    // if the selection is a file node
                    if (((DefaultMutableTreeNode) discoveredTree.getLastSelectedPathComponent()).getUserObject() instanceof ProjectFile){
                        discoveredSelect = (DefaultMutableTreeNode) discoveredTree.getLastSelectedPathComponent();  //select item
                    }
                    else discoveredSelect = null;
                }
                else discoveredSelect = null;

                regeneratePanes();
            }
        });

        // create pane regions
        imagePanel = new JPanel();
        editorPanel = new JPanel();

        // create editor pane
        JSplitPane editorPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorPanel, imagePanel);
        editorPane.setResizeWeight(.5);

        // create left pane
        JSplitPane pane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(existingTree), new JScrollPane(discoveredTree));
        pane1.setResizeWeight(.5);

        // create right pane
        JSplitPane pane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorPane, pane1);
        pane1.setResizeWeight(.5);
        jFrame.add(pane2);

        jFrame.setVisible(true);    //initialize visibility (needs to be at the end of initialization)
    }

    //generate treeroot
    protected DefaultMutableTreeNode generateTreeRoot(SortedSet<ProjectFile> set, String rootText){
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootText); // generate root folder with display text

        // for each element in the set, add an item to the root
        for (ProjectFile child: set){
            root.add(new DefaultMutableTreeNode(child));
        }

        return root;
    }

    // class for regenerating editor pane
    public static class RegenerateEvent implements EventE {

        private final ProjectReconciliation editor;

        public RegenerateEvent(ProjectReconciliation editor){
            this.editor = editor;
        }

        @Override
        public void enact() {
            editor.regeneratePanes();
        }
    }

    // regenerate window elements
    public void regeneratePanes(){

        // clear object panels
        imagePanel.removeAll();
        editorPanel.removeAll();

        // regenerate objects
        // draw image if image is selected
        if (discoveredSelect != null){
            if (discoveredSelect.getUserObject() instanceof ProjectFile){
                ProjectFile file = (ProjectFile) discoveredSelect.getUserObject();
                file.setUseColor(false);    // don't use preview color, since preview fixme make this a button on the left sometime
                ProjectEditorFunctions.loadImage(file, source.getAbsolutePath(file), imagePanel, color[0]);
            }
        }
        drawEditor();   //redraw editor region

        // redraw objects
        imagePanel.revalidate();
        imagePanel.repaint();
        editorPanel.revalidate();
        editorPanel.repaint();
        //foundFilesTree.updateUI();
        //loadedFilesTree.updateUI();
    }

    public void drawEditor(){
        // upper area-------------------------------------
        JPanel topRegion = new JPanel();
        topRegion.setLayout(new BoxLayout(topRegion, BoxLayout.Y_AXIS));
        editorPanel.add(topRegion);       //add to editor panel

        // text for first selection
        JPanel topSelect = new JPanel();
        topSelect.setBackground(Color.white);
        topSelect.setLayout(new BoxLayout(topSelect, BoxLayout.Y_AXIS));
        topRegion.add(topSelect);
        String text;
        if (existingSelect != null && existingSelect.getUserObject() instanceof ProjectFile)
            text = ((ProjectFile) existingSelect.getUserObject()).getTitle();
        else text = "[no file selected]";
        topSelect.add(new JLabel("Project File: " + text));

        // text for secondary selection
        JPanel bottomSelection = new JPanel();
        bottomSelection.setBackground(Color.white);
        bottomSelection.setLayout(new BoxLayout(bottomSelection, BoxLayout.Y_AXIS));
        topRegion.add(bottomSelection);
        String text1, text2;
        if (discoveredSelect != null && discoveredSelect.getUserObject() instanceof ProjectFile){
            text1 = ((ProjectFile) discoveredSelect.getUserObject()).getTitle();
            text2 = ((ProjectFile) discoveredSelect.getUserObject()).getPath();
        }
        else{
            text1 = "[no file selected]";
            text2 = "[no file selected]";
        }
        bottomSelection.add(new JLabel("Discovered File: " + text1));
        bottomSelection.add(new JLabel("Path: " + text2));

        JButton button0 = new JButton("Merge");
        topRegion.add(button0);
        // endable button if merging is possible; otherwise disable button
        if (discoveredSelect != null && existingSelect != null){
            button0.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // get project files
                    ProjectFile keep = (ProjectFile) existingSelect.getUserObject();
                    ProjectFile toss = (ProjectFile) discoveredSelect.getUserObject();

                    // remove from core trees [before changing file paths]
                    existingSet.remove(keep);
                    discoveredSet.remove(toss);

                    //merge
                    ProjectManager.mergeFile(keep, toss);   // copy settings over
                    keep.setValid(true);    // mark as valid

                    // remove from gui trees
                    existingRoot.remove(existingSelect);
                    discoveredRoot.remove(discoveredSelect);

                    //reset selections
                    existingSelect = null;
                    discoveredSelect = null;

                    // if both lists are empty, terminate window
                    if(existingSet.size() == 0 && discoveredSet.size() == 0){
                        WarningWindow.warningWindow("All files reconciled. Remember to save changes.");
                        jFrame.dispose();
                    }

                    //redraw trees
                    existingTree.updateUI();
                    discoveredTree.updateUI();

                    //redraw guis
                    regeneratePanes();
                }
            });
        }
        else{
            button0.setEnabled(false);
        }


        //lower area-------------------------------------
        JPanel lowerRegion = new JPanel();
        lowerRegion.setLayout(new BoxLayout(lowerRegion, BoxLayout.Y_AXIS));
        editorPanel.add(lowerRegion);       //add to editor panel

        JButton button1 = new JButton("Add Discovered Files to Project");
        lowerRegion.add(button1);
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // mark existing files as invalid
                for(ProjectFile child: existingSet){
                    child.setValid(false);
                }

                // add all new files to project
                for(ProjectFile child: discoveredSet){
                    source.addFile(child);
                }

                // termimate window
                jFrame.dispose();
            }
        });

        JButton button3 = new JButton("Ignore Empty Nodes");
        lowerRegion.add(button3);
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(ProjectFile child: existingSet){
                    child.setValid(false);
                }

                // termimate window
                jFrame.dispose();
            }
        });

        JButton button4 = new JButton("Delete Empty Nodes");
        lowerRegion.add(button4);
        button4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //fixme add warning
                // for each file existing, remove from the real tree
                for(ProjectFile child: existingSet){
                    ProjectFolderInterface parent = (ProjectFolderInterface) child.getParent();  //get parent
                    parent.getChildren().remove(child); //remove child from parent
                }

                // termimate window
                jFrame.dispose();
            }
        });

        lowerRegion.add(ProjectEditorFunctions.makeBackgroundToggle(new RegenerateEvent(this), color));   // add color use toggle
    }
}
