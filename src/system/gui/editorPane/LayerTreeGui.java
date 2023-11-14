package system.gui.editorPane;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import system.gui.GuiOperations;
import system.layerTree.data.TreeUnit;
import system.setting.CoreGlobal;
import system.Program;
import system.layerTree.interfaces.LayerTreeFolder;
import system.layerTree.data.Folder;
import system.layerTree.data.Layer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// factory for gui objects of layer gui tree
public class LayerTreeGui {

    // generate base unit used by both folder and layer bar
    public static JPanel makeFiletreeBar(int level){
        JPanel panel = new JPanel();    // generate panel

        //establish container that layer display goes into
        FlowLayout layout = new FlowLayout(FlowLayout.LEFT);    // align added elements left
        panel.setLayout(layout);
        layout.setVgap(0);  // change padding [remove excess space]
        panel.setOpaque(false); // transparent space

        //gives maximum sizes to display bars
        panel.setMaximumSize(new Dimension(CoreGlobal.MAX_WIDTH, CoreGlobal.MAX_HEIGHT_TREE_BAR));

        // add spacing pad for level indentation //fixme move to gui construction probably
        panel.setBorder(new EmptyBorder(0, CoreGlobal.TREE_LEVEL_PAD * (level - 1), 0, 0));

        return panel;
    }

    // element representing folder
    public static JPanel makeFolderBar(Folder unit, int level, Color color){

        JPanel panel = makeFiletreeBar(level);    // generate panel template

        // prepare internal field
        JPanel core = new JPanel();

        // prepare layout
        BoxLayout simpleLayout = new BoxLayout(core, BoxLayout.X_AXIS); //assign layout for item placement
        core.setLayout(simpleLayout);
        core.setAlignmentX(Component.LEFT_ALIGNMENT);       //left align
        core.setAlignmentY(Component.BOTTOM_ALIGNMENT);    //align up

        // set color for border from parameters
        if (color == null) core.setBorder(BorderFactory.createLineBorder(Color.black));
        else core.setBorder(BorderFactory.createLineBorder(color));

        // set color for background from layer properties
        core.setBackground(GuiOperations.treeColorAssign(unit));

        // prepare container contents
        panel.add(core);
        core.add(GuiFieldControls.makeSelectionButton(unit));
        core.add(GuiFieldControls.makeVisibilityToggleButton(unit));  //add visibility toggle
        JLabel lab = GuiFieldControls.makeClippingLabel(unit); // add clipped label if layer is clipped
        if (lab != null){lab.setForeground(Color.white);core.add(lab);}
        // offer children expansion only if children exist
        if(unit.getChildren().size() > 0){
            core.add(GuiFieldControls.makeChildrenExpandButton(unit));
        }
        core.add(GuiFieldControls.makeTitleField(unit));
        core.add(GuiFieldControls.makeButtonDown(unit));
        core.add(GuiFieldControls.makeButtonUp(unit));
        core.add(GuiFieldControls.makeButtonOut(unit));
        core.add(GuiFieldControls.makeButtonIn(unit));

        return panel;
    }

    // element representing layer
    public static JPanel makeLayerBar(Layer unit, int level, Color color){

        JPanel panel = makeFiletreeBar(level);    // generate panel template

        // prepare internal field
        JPanel core = new JPanel();

        // prepare layout
        BoxLayout simpleLayout = new BoxLayout(core, BoxLayout.X_AXIS); //assign layout for item placement
        core.setLayout(simpleLayout);
        core.setAlignmentX(Component.LEFT_ALIGNMENT);       //left align
        core.setAlignmentY(Component.BOTTOM_ALIGNMENT);    //align up

        // set color for border from parameters
        if (color == null) core.setBorder(BorderFactory.createLineBorder(Color.black));
        else core.setBorder(BorderFactory.createLineBorder(color));

        // set color for background from layer properties
        core.setBackground(GuiOperations.treeColorAssign(unit));

        // prepare core field container contents
        panel.add(core);
        core.add(GuiFieldControls.makeSelectionButton(unit));
        core.add(GuiFieldControls.makeVisibilityToggleButton(unit));  //add visibility toggle
        JLabel lab = GuiFieldControls.makeClippingLabel(unit); // add clipped label if layer is clipped
        if (lab != null){lab.setForeground(Color.white);core.add(lab);}
        GuiFieldControls.addColorChooser(core, 0, Color.white, unit);
        core.add(GuiFieldControls.makeTitleField(unit));
        core.add(GuiFieldControls.makeButtonDown(unit));
        core.add(GuiFieldControls.makeButtonUp(unit));
        core.add(GuiFieldControls.makeButtonOut(unit));
        core.add(GuiFieldControls.makeButtonIn(unit));

        return panel;
    }

    //recursive builder for layer tree gui
    public static void buildPanel(JPanel box, int level, LayerTreeFolder unit){

        //for each child, recurse
        for (TreeUnit child: unit.getChildren()) {
            // case: Child is Folder
            if (child instanceof Folder){

                // add folder element
                box.add(LayerTreeGui.makeFolderBar((Folder) child,level, Program.selectionColor(child)));

                //recurse to children, put children in layer box; display children only if children exist and are to be displayed
                if(((Folder)child).displayChildren && ((LayerTreeFolder)child).getChildren().size() > 0){
                    // make a containment box for the folder
                    JPanel layerBox = new JPanel();
                    // prepare layouts
                    BoxLayout layout = new BoxLayout(layerBox, BoxLayout.Y_AXIS);
                    layerBox.setLayout(layout);
                    // prepare border
                    layerBox.setBorder(BorderFactory.createLineBorder(Color.white)); //draw element borders
                    layerBox.setBackground(Color.lightGray);
                    //FIXME does nothing
                    //layerBox.setBackground(Color.lightGray); // give folder a backdrop color
                    //layerBox.setOpaque(true);
                    //FIXME add level heights
                    // introduce left padding

                    //FIXME add the indentations
                    //Dimension leftPad1 = new Dimension((50 * (level - 1)), 0);
                    //layerBox.setPreferredSize(leftPad1);

                    box.add(layerBox);

                    buildPanel(layerBox, level + 1, (LayerTreeFolder) child);
                }
            }

            // case: Child is Layer
            if (child instanceof Layer){
                //recurse to children, but only if more is selected FIXME this should not exist
                //if(((LayerCORE)child).displayChildren) buildPanel(box, level + 1, ((LayerCORE)child)); //HERE

                //add layers to gui last if is not folder
                box.add(LayerTreeGui.makeLayerBar((Layer) child,level, Program.selectionColor(child)));
            }

        }

    }
}
