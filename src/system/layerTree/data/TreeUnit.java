package system.layerTree.data;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import system.layerTree.interfaces.LayerTreeFolder;

public abstract class TreeUnit {

    //Existing Types: layer, folder

    //type differentiation
    @Expose
    @SerializedName(value = "type")
    public final String type;

    protected TreeUnit(String type) {
        this.type = type;
    }


    // parent --------------------------------
    // Purpose: track parent for filestructure tree
    // Note: not saved to JSON file to maintain directed acyclic graph properties; generated on load by V1.Layer Manager

    private LayerTreeFolder parent = null;

    // accessor
    public LayerTreeFolder getParent(){
        return parent;
    }

    // mutator
    public void setParent(LayerTreeFolder source){
        parent = source;
    }

}
