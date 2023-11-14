package system.gui.editorPane;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import system.Program;
import system.layerTree.data.Folder;
import system.layerTree.data.Layer;

import javax.swing.*;
import java.awt.*;

public class LayerEditorPane {

    // create core panel
    public static JPanel corePanel(){
        // create panel
        JPanel panel = new JPanel();

        // create alignment
        FlowLayout layout = new FlowLayout(FlowLayout.LEFT); // align left
        panel.setLayout(layout);
        layout.setVgap(0);      // change padding [remove excess space]

        // background color
        panel.setBackground(Color.lightGray);   // 'whitespace' color

        // spacing requirement (keeps it from being cropped with resize)
        //panel.setPreferredSize(new Dimension(300, 100));

        return panel;
    }

    // factory for layer editor pane
    public static JPanel layerEditorPane(Layer layer){
        // create panel
        JPanel panel = corePanel();

        // contents
        panel.add(new JLabel("Layer:"));                    // label
        panel.add(GuiFieldControls.makeTitleField(layer));      // create title
        GuiFieldControls.addColorChooser(panel, layer);         //add color chooser
        panel.add(GuiFieldControls.makeColorToggle(layer));     //toggle use color
        panel.add(GuiFieldControls.makeAlpha(layer));           //add alpha button
        panel.add(GuiFieldControls.makeClippingToggle(layer));  //add clipping toggle selection
        GuiFieldControls.addLocationControls(panel, layer);     //add hierarchy controls
        panel.add(GuiFieldControls.makeVisibleToggle(layer));  //add visibility toggle
        panel.add(GuiFieldControls.makeDelete(layer));          //add delete button
        panel.add(GuiFieldControls.makeDuplicate(layer));       //add duplicate
        panel.add(GuiFieldControls.loadFileAsLayerButton(layer));   // add button to load file

        return panel;
    }

    // factory for folder editor pane
    public static JPanel folderEditorPane(Folder layer){
        // create panel
        JPanel panel = corePanel();

        // contents
        panel.add(new JLabel("Folder:"));                   // label
        panel.add(GuiFieldControls.makeTitleField(layer));      // create title
        panel.add(GuiFieldControls.makeAlpha(layer));           //add alpha button
        panel.add(GuiFieldControls.makeClippingToggle(layer));  //add clipping toggle selection
        GuiFieldControls.addLocationControls(panel, layer);     //add hierarchy controls
        panel.add(GuiFieldControls.makeVisibleToggle(layer));  //add visibility toggle
        panel.add(GuiFieldControls.makeDelete(layer));          //add delete button
        panel.add(GuiFieldControls.makeDeleteAll(layer));       //add delete all
        panel.add(GuiFieldControls.makeDuplicate(layer));       //add duplicate
        panel.add(GuiFieldControls.makeIntersectClipToggle(layer)); // add intersect clipping toggle
        // if an intersect image is selected, display these choices
        if(layer.hasIntersectImage()){
            panel.add(GuiFieldControls.revokeIntersectMaskButton(layer));   // remove file option
            panel.add(new JLabel(layer.getIntersectTitle()));  // add selected file infographic
        }
        // if no intersect image is selected, display these choices
        else{
            panel.add(GuiFieldControls.loadFileAsIntersectMaskButton(layer));   // add file selection button
        }

        return panel;
    }
}
