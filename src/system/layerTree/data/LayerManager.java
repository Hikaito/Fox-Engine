package system.layerTree.data;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import system.gui.editorPane.LayerTreeGui;
import system.gui.editorPane.LayerEditorPane;
import system.backbone.Copy;
import system.layerTree.interfaces.LayerTreeFolder;
import system.layerTree.interfaces.Renderable;

import javax.swing.*;
        import java.awt.*;
        import java.awt.image.BufferedImage;
import java.util.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import system.Program;
import system.backbone.VersionNumber;
import system.gui.WarningWindow;

public class LayerManager {

    //region internal state variables ------------------------------------
    // enum to describe whether to insert at the top or the bottom
    public enum position{TOP, BOTTOM};
    public enum direction {UP, DOWN, IN, OUT};
    public position defaultLocation = position.TOP; // default location fixme control object

    // project root
    @Expose
    @SerializedName(value = "root")
    private Folder root = new Folder(); //root level layer: sentinel object that doesn't hold information

    // project ID: should not change
    @Expose
    @SerializedName(value = "project_ID")
    private String projectID = Program.getProjectID();
    public void setProjectID(String id){ projectID = id; }

    // project name
    //fixme can be used for default file save name
    //fixme currently no way to change this
    @Expose
    @SerializedName(value = "project_name")
    private String projectTitle = "[no title]";
    public void setProjectTitle(String title){ projectTitle = title; }

    // version number
    @Expose
    @SerializedName(value="program_version")
    protected VersionNumber programVersion = Program.getProgramVersion();   // default to program version
    public VersionNumber getProgramVersion(){return programVersion;}

    // reject if program number is lower than project
    public static boolean evaluateProgramNumber(VersionNumber program, VersionNumber project){
        if(program.compareTo(project) < 0) return false;
        return true;
    }

    //constructor
    public LayerManager(){
        initializeGUI();
    }

    //endregion
    //region filemanager management---------------------------------

    //copy existing file into manager (add)
    public void addManager(LayerManager obj){
        // test for project number
        if(!projectID.equals(obj.projectID)){
            WarningWindow.warningWindow("Project ID's don't match. \nMerge could not occur.");
            return;
        }

        // test for valid program number. Otherwise warn user
        if(!evaluateProgramNumber(Program.getProgramVersion(), obj.getProgramVersion())){
            WarningWindow.warningWindow("File version invalid. Program version: " + Program.getProgramVersion() + "; Project version: " + obj.getProgramVersion());
            return;
        }

        // merge
        obj.root.setTitle("Imported Project");  // give folder a name
        addChild(root, obj.root);   // import folder
        generateTreeParents();
    }

    //gut existing manager and replace with new information (replace)
    public void replaceManager(LayerManager obj){
        // test for project number
        if(!projectID.equals(obj.projectID)){
            WarningWindow.warningWindow("Project ID's don't match. \nLoad could not occur.");
            return;
        }

        // test for valid program number. Otherwise warn user
        if(!evaluateProgramNumber(Program.getProgramVersion(), obj.getProgramVersion())){
            WarningWindow.warningWindow("File version invalid. Program version: " + Program.getProgramVersion() + "; Project version: " + obj.getProgramVersion());
            return;
        }

        // replace all relevant variables
        clear();    //prepare layer for new information
        root = obj.root;
        projectID = obj.projectID;
        projectTitle = obj.projectTitle;
        generateTreeParents();
    }

    //endregion
    //region selection (message passing)------------------------------------

    private final SelectionManager selectionManager = new SelectionManager(); // initialize
    // message passing
    public void addSelection(TreeUnit obj){selectionManager.addSelection(obj);}
    public void removeSelection(TreeUnit obj){selectionManager.removeSelection(obj);}
    public void clearSelection(){selectionManager.clearSelection();}
    public TreeUnit peekSelectionTop(){return selectionManager.peekTop();}
    public SelectionManager.selectionPriority getSelectionStatus(){return selectionManager.getSelectionStatus();}
    public SelectionManager.selectionPriority checkSelection(TreeUnit obj){return selectionManager.checkSelection(obj);}
    public Color selectionColor(TreeUnit unit){return selectionManager.selectColor(unit);}

    //endregion
    //region layer manipulation-----------------------------------
    // fixme add undoredo to most of these functions

    //clear the entire file
    public void clear(){
        Program.clearSelection();               // clear selection; this is technically a deletion
        root.clearChildren();                   // remove all elements from root
        setProjectID(Program.getProjectID());   // reset UUID, for file clearing for new projects
        markImageForRender(root);               //remark image for rendering
        Program.redrawAllRegions();             // redraw canvas and gui
    }

    //traverse layer - move down: moves layer down in parent's child hierarchy, if there is space to move
    //traverse layer - move up: moves layer up in parent's child hierarchy, if there is space to move
    //traverse layer - move in: moves layer to children list of closest sibling; ignores if there is no suitable sibling
    //traverse layer - move down: moves layer to be a sibling of the current parent; ignores if parent is root
    //note: does not ensure layer is a part of the system; also re-renders unnecessarily even in the case of impossible moves
    public void moveLayer(TreeUnit layer, direction dir){

        // reject if layer has no parent [not seeded into tree] (also catches root condition)
        if (layer.getParent() == null) return;

        //mark image for regeneration if applicable
        if (layer instanceof Renderable) markImageForRender(layer);

        // get layer index
        LinkedList<TreeUnit> children = (layer.getParent()).getChildren();
        int index = children.indexOf(layer);

        // if place below
        if (dir == direction.DOWN){
            //if there is only one child layer or the layer is at the bottom, ignore request
            if(index + 1 == children.size() || children.size() == 1) return;

            //remove the layer and reinsert at one index down
            children.remove(layer);
            children.add(index + 1, layer);
        }
        // if place above
        else if (dir == direction.UP){
            //If there is only one child or the layer is already at the top, ignore request
            if(index == 0 || children.size() == 1) return;

            //remove layer from heirarchy and reinsert at one index higher
            children.remove(layer);
            children.add(index - 1, layer);
        }
        // if move in
        else if (dir == direction.IN){

            //if there are no older siblings, ignore request
            if(index == 0){return;}

            // if there is a valid sibling, move the layer to the bottom of the sibling
            TreeUnit sibling = children.get(index-1);
            if(sibling instanceof LayerTreeFolder){
                children.remove(layer);
                addChild((LayerTreeFolder) sibling, layer, position.BOTTOM);
            }
        }
        // else if move out
        else if (dir == direction.OUT){
            TreeUnit parent = (TreeUnit) layer.getParent();
            LayerTreeFolder grandparent = parent.getParent();  // null parents already excluded

            //if layer is at max level, ignore request
            if(grandparent == null){ return; }

            //otherwise move layer out one and redraw GUI
            children.remove(layer);  //remove layer from parent
            addChildAtSibling(parent, layer, position.BOTTOM); // shove layer behind parent
        }

        //mark image for regeneration in new location if applicable
        if (layer instanceof Renderable) markImageForRender(layer);

        //redraw layer editor gui
        Program.redrawAllRegions();
    }

    //add folder: adds new folder type layer
    public void addFolder(){
        // add folder to root
        addChild(root, new Folder());
    }

    //add layer: adds new layer at the end of the root layer child list
    public void addLayer(){
        // add layer to root
        addChild(root, new Layer());
    }

    //fixme is this used?
    //add layer with image: adds new layer at the front of the root layer child list and initializes metadata
    public void addLayer(long code, boolean useColor, Color color){
        Layer layer = new Layer();  //create layer
        layer.setImage(code); //set image
        //add color and useColor states
        layer.setColor(color);
        layer.setUseColor(useColor);
        addChild(root, layer, position.TOP); //add to list; adds at front
    }

    // add renderable to parent [helper function and main function that specifies insertion location]
    public void addChild(LayerTreeFolder parent, TreeUnit child){
        addChild(parent, child, defaultLocation);
    }
    public void addChild(LayerTreeFolder parent, TreeUnit child, position place){
        child.setParent(parent);    // set parent object for child
        // toggle response based on default settings
        if (place == position.BOTTOM){
            parent.getChildren().addLast(child);
        }
        else{
            // top
            parent.getChildren().addFirst(child);
        }

        // rebuild gui
        markImageForRender(child);
        Program.redrawAllRegions();
    }

    // add renderable to sibling
    public void addChildAtSibling(TreeUnit sibling, TreeUnit child, position place){
        // set parent
        LinkedList<TreeUnit> children = (sibling.getParent()).getChildren();
        child.setParent(sibling.getParent());

        // toggle location
        if (place == position.TOP){
            children.add(children.indexOf(sibling), child);
        }
        else{
            //below
            children.add(children.indexOf(sibling) + 1, child);
        }

        // rebuild gui
        markImageForRender(child);
        Program.redrawAllRegions();
    }

    //clone layer
    // automatically deep deletes, since there's not really a reason to shallow delete
    public void duplicate(TreeUnit layer){
        // remove selection
        Program.clearSelection();

        //clone element
        if (layer instanceof Copy){
            TreeUnit clone = (TreeUnit) ((Copy<?>) layer).copy();
            addChildAtSibling(layer, (TreeUnit) clone, position.BOTTOM);

            markImageForRender(clone);
        }

        //redraw layer GUI and canvas GUI
        Program.redrawLayerEditor();
        Program.redrawCanvas();
    }

    //remove layer: removes selected layer from layer hierarchy; parent inherits children
    //note: does not ensure layer is a part of the system
    // deep = true: delete all nested elements
    // deep = false: do not delete nested elements
    public void delete(TreeUnit layer, boolean deep){
        // remove selection
        Program.clearSelection();

        // mark for rerender
        markImageForRender(layer);

        //get layer parent children list
        LinkedList<TreeUnit> children = layer.getParent().getChildren();

        // deep deletion
        if(deep){
            //remove layer from parent
            children.remove(layer);
        }
        // shallow deletion
        else{
            //get index of layer
            int index = children.indexOf(layer);

            //remove layer from parent
            LayerTreeFolder parent = layer.getParent();
            children.remove(layer);

            // transfer children if there are children
            if(layer instanceof Folder){
                //transfer children from layer to parent (in reverse order to the parent's old index)
                //Note: this can be improved by shifting all items at once
                LinkedList<TreeUnit> list = ((Folder)layer).getChildren();
                ListIterator<TreeUnit> iterator = list.listIterator(list.size());
                //iterate  over children
                while(iterator.hasPrevious()){
                    children.add(index, iterator.previous());
                }

                // reset parents of deleted elements
                for(TreeUnit child : children){
                    child.setParent(parent);
                }

            }
        }

        //redraw layer GUI and canvas GUI
        Program.redrawAllRegions();
    }

    //endregion
    //region image rendering and helper functions-----------------------------------

    // resets all renderable children's parents [helper]
    public void generateTreeParents(){
        ((TreeUnit)root).setParent(null);   // reset root to orphan
        generateTreeParents(root);  // recurse on root
    }

    // recursive function
    private void generateTreeParents(LayerTreeFolder parent){
        LinkedList<TreeUnit> children = parent.getChildren();
        // for each offspring, reset parent
        for(TreeUnit child : children){
            child.setParent( parent);   // set parent
            // if also filetreefolder, recurse
            if (child instanceof LayerTreeFolder){
                generateTreeParents((LayerTreeFolder) child);
            }
        }
    }

    // pings children image in need of regeneration up the tree to the root
    public void markImageForRender(TreeUnit obj){
        // mark current layer as re-render
        if (obj instanceof Renderable) ((Renderable) obj).requireRender();

        // mark all applicable parents: starting with the current image, pass the render mark up the tree
        LayerTreeFolder parent = obj.getParent();
        while(parent != null){
            if (parent instanceof Renderable) ((Renderable) parent).requireRender();
            parent = ((TreeUnit) parent).getParent();
        }
    }

    // markImageForRerender but skips the first layer
    public void markParentForRender(TreeUnit obj){
        // mark current layer as re-render
        if (obj.getParent() != null) markImageForRender((TreeUnit) obj.getParent());
    }

    //public interface to get layer stack
    public BufferedImage stackImage(){
        return ((Renderable)root).getImage();
    }
    //endregion
    //region GUI layer layout render-------------------------------

    //internal state variables
    private JPanel layerTreePanel = new JPanel();      //panel for holding layer gui
    private JPanel layerEditPanel = new JPanel();  //panel for holding editor information

    //global accessors
    public JPanel getLayerTreePanel(){return layerTreePanel;}
    public JPanel getLayerEditorPanel(){return layerEditPanel;}

    // initializations
    private void initializeGUI(){
        // layerTreePanel intializations
        layerTreePanel.setLayout(new BoxLayout(layerTreePanel, BoxLayout.Y_AXIS));  //create box layout in vertical: stack children elements
        //layerTreePanel.setBackground(Color.lightGray);

        // layerEditPanel initializations
        layerEditPanel.setMinimumSize(new Dimension(100, 300));
        layerEditPanel.setMaximumSize(new Dimension(9000, 300));
        //layerEditPanel.setBorder(BorderFactory.createLineBorder(Color.darkGray)); //draw element borders
        layerEditPanel.setLayout(new BoxLayout(layerEditPanel, BoxLayout.Y_AXIS)); //set layout to vertical box, so items stack vertically

        //layerEditPanel.setBackground(Color.BLACK);
        // spacing requirement (keeps it from being cropped with resize)
        layerEditPanel.setPreferredSize(new Dimension(300, 100));
    }

    // regenerate tree gui
    public void rebuildLayerTree(){
        //clear gui
        layerTreePanel.removeAll();

        //recursively build gui
        LayerTreeGui.buildPanel(layerTreePanel, 1, ((Folder)root));

        //refresh gui
        layerEditPanel.revalidate();    //redraws components
        layerEditPanel.repaint();       //repaints base rectangle when elements are removed
    }

    // regenerate editor gui
    public void rebuildEditorPanel(){
        // remove elements in panel
        layerEditPanel.removeAll();

        //Selection type: single object
        if(Program.getSelectionStatus() == SelectionManager.selectionPriority.PRIMARY){
            TreeUnit top = Program.peekSelectionTop();  // get selected element

            // object type: Layer
            if(top instanceof Layer){
                //LayermapLayerEditPane.add(layerEditPanel, (LayerCORE) top);
                layerEditPanel.add(LayerEditorPane.layerEditorPane((Layer) top));
            }

            // object type: Folder
            else if(top instanceof Folder){
                layerEditPanel.add(LayerEditorPane.folderEditorPane((Folder) top));
            }
        }

        //selection type: 2 objects
        //fixme

        //refresh gui
        layerEditPanel.revalidate();    //redraws components
        layerEditPanel.repaint();       //repaints base rectangle when elements are removed
    }

    //endregion
}
