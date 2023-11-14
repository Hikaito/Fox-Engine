package system.gui;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import system.layerTree.data.Folder;
import system.layerTree.data.LayerCore;

import java.awt.*;

// class with static GUI methods
public class GuiOperations {
    // region static assignment functions ------------------------
    public static Color treeColorAssign(LayerCore obj){
        // if obj is folder
        if (obj instanceof Folder) return Color.LIGHT_GRAY;

        // if obj is clip enabled
        if(obj.getClipToParent()) return Color.darkGray;

        // if obj is normal layer
        return Color.gray;
    }

    //select color based on level (depreciated)
    public static Color levelShade(int level){
        switch (level){
            case 0:
                return Color.white;
            case 1:
                return Color.LIGHT_GRAY;
            case 2:
                return Color.gray;
            case 3:
                return Color.darkGray;
            default:
                return Color.black;
        }
    }

    //formatColor: small helper method for creating pretty hex for color integers with the proper leading zeros
    public static String formatColor(Color color){
        return String.format("x%06x", (color.getRGB() & 0x00ffffff));
    }

    //expand toggle text: small helper function for selecting text
    public static String toggleMenuComplexity(boolean expand){
        if (expand) return "Less";
        else return "More";
    }
    // endregion
}
