package system.layerTree.data;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import system.Program;

import java.awt.*;
import java.util.LinkedList;

public class SelectionManager {

    // region static entities------------------------------------

    // enumerator for selection type
    public enum selectionPriority{NONE, PRIMARY, SECONDARY, MANY}

    // endregion
    // region initialization------------------------------------

    // selection list
    private final LinkedList<TreeUnit> selection = new LinkedList<>();    // list for selected objects
    private selectionPriority currentSelectType = selectionPriority.NONE;   // state of selection
    // endregion
    //region mutation functions------------------------------------

    //fixme add functionality for some alterations to affect many selections at once?

    // add selection
    public void addSelection(TreeUnit obj){
        selection.addLast(obj);
        currentSelectType = getSelectionType();
        Program.redrawLayerEditor();    // redraw gui
    }

    // remove selection
    public void removeSelection(TreeUnit obj){
        // if object is in list, remove from list
        if (selection.contains(obj)){
            selection.remove(obj);
            currentSelectType = getSelectionType();
            Program.redrawLayerEditor();    // redraw gui
        }
    }

    // delete selection: clear list and reset selection type
    public void clearSelection(){
        selection.clear();
        currentSelectType = selectionPriority.NONE;
        Program.redrawLayerEditor();    // redraw gui
    }

    //endregion
    //region accessor functions-----------------------------------

    // retrieve selection value
    public selectionPriority getSelectionStatus(){return currentSelectType;}

    // retrieve the top item
    public TreeUnit peekTop(){
        return selection.peekFirst();
    }

    // check selection list for selection type
    private selectionPriority getSelectionType(){
        if (selection.size() == 0) return selectionPriority.NONE;
        if (selection.size() == 1) return selectionPriority.PRIMARY;
        if (selection.size() == 2) return selectionPriority.SECONDARY;
        return selectionPriority.MANY;
    }

    // check the type of selection
    public selectionPriority checkSelection(TreeUnit obj){
        // if no objects or object not selected, then selection is none
        if (currentSelectType == selectionPriority.NONE || !selection.contains(obj)) return selectionPriority.NONE;
        // if many objects, check if query is one
        if (currentSelectType == selectionPriority.MANY && selection.contains(obj)) return selectionPriority.MANY;
        // else if first object, primary
        if ((currentSelectType == selectionPriority.PRIMARY || currentSelectType == selectionPriority.SECONDARY )
                && selection.peek() == obj) return selectionPriority.PRIMARY;
        // else if second object, secondary
        return selectionPriority.SECONDARY;
    }

    // assign color for an object's selection status
    public Color selectColor(TreeUnit obj){
        selectionPriority select = checkSelection(obj); // get selection status
        // return appropriate color
        if (select == selectionPriority.NONE) return null;
        if (select == selectionPriority.PRIMARY) return Program.getPrimarySelectionColor();
        if (select == selectionPriority.SECONDARY) return Program.getSecondarySelectionColor();
        if (select == selectionPriority.MANY) return Program.getManySelectionColor();
        return null;    //base color
    }

    //endregion
}
