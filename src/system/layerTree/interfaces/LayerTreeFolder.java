package system.layerTree.interfaces;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import system.layerTree.data.TreeUnit;
import java.util.LinkedList;

// marks layer as capable as acting as a folder

public interface LayerTreeFolder {

    //NOTE: parent information stored in Renderable class it seems

    public LinkedList<TreeUnit> getChildren();
    public void clearChildren();
}
