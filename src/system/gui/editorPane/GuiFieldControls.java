package system.gui.editorPane;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import system.Program;
import system.gui.GuiOperations;
import system.layerTree.data.LayerManager;
import system.layerTree.data.SelectionManager;
import system.layerTree.data.Folder;
import system.layerTree.data.Layer;
import system.layerTree.data.LayerCore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// field controls for Layer objects

public class GuiFieldControls {

    //region core unit manipulation ========================================

    //region clone and delete--------------------------------------
    //add delete button: adds deletion button to field
    public static JButton makeDelete(LayerCore layer){
        //add button
        JButton button = new JButton("delete");

        //on button click, deploy action
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                // request action from layer manager [handles redraw]
                Program.deleteTreeUnit(layer, false);
            }
        });

        return button;
    }

    //add delete all button: adds deletion of children button to field
    public static JButton makeDeleteAll(LayerCore layer){
        //add button
        JButton button = new JButton("delete (deep)");

        //on button click, deploy action
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                // request action from layer manager [handles redraw]
                Program.deleteTreeUnit(layer, true);
            }
        });

        return button;
    }

    //add duplicate button
    public static JButton makeDuplicate(LayerCore layer){
        //add button
        JButton button = new JButton("clone");

        //on button click, deploy action
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                // request action from layer manager [handles redraw]
                Program.duplicateTreeUnit(layer);
            }
        });

        return button;
    }
    //endregion
    //region movement control ---------------------------
    //function for adding location controls to panel [adds buttons to field]
    public static void addLocationControls(JPanel innerField, LayerCore layer){
        //generate panel for holding buttons
        JPanel buttonBox = new JPanel();
        innerField.add(buttonBox);

        //add buttons for location movement-----------
        buttonBox.add(makeButtonOut(layer));    //register button
        buttonBox.add(makeButtonUp(layer));

        //label for level
        //JLabel levelLabel = new JLabel("level " + 0); //fixme 0 was level
        //buttonBox.add(levelLabel);

        buttonBox.add(makeButtonDown(layer));
        buttonBox.add(makeButtonIn(layer));
    }

    //get button for in
    public static JButton makeButtonIn(LayerCore layer){
        //in: button to signal moving deeper in hierarchy
        JButton buttonIn = new JButton("->");

        buttonIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // request action from layer manager [handles redraw]
                Program.moveLayer(layer, LayerManager.direction.IN);
            }
        });

        return buttonIn;
    }

    //get button for down
    public static JButton makeButtonDown(LayerCore layer){
        //down: button to signal moving down in hierarchy
        JButton buttonDown = new JButton("v");

        buttonDown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // request action from layer manager [handles redraw]
                Program.moveLayer(layer, LayerManager.direction.DOWN);
            }
        });

        return buttonDown;
    }

    //get button for up
    public static JButton makeButtonUp(LayerCore layer){
        //up: button to signal moving up in hierarchy
        JButton buttonUp = new JButton("^");

        buttonUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // request action from layer manager [handles redraw]
                Program.moveLayer(layer, LayerManager.direction.UP);
            }
        });

        return buttonUp;
    }

    ///get button for out
    public static JButton makeButtonOut(LayerCore layer){
        //out: button to signal moving out of the hierarchy
        JButton buttonOut = new JButton("<-");  //generate button

        buttonOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // request action from layer manager [handles redraw]
                Program.moveLayer(layer, LayerManager.direction.OUT);
            }
        });

        return buttonOut;
    }

    //endregion
    //region selection ---------------------------
    // function for selection
    public static JButton makeSelectionButton(LayerCore layer){
        // pick text
        String text = "Deselect";
        // test for selection: selection type is select if not selected
        if (Program.checkSelection(layer) == SelectionManager.selectionPriority.NONE)
            text = "Select";

        //out: button to signal moving out of the hierarchy
        JButton buttonOut = new JButton(text);  //generate button

        buttonOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //check selection value
                // select if unselected
                if (Program.checkSelection(layer) == SelectionManager.selectionPriority.NONE)
                    Program.addSelection(layer);

                    //otherwise unselect if selected
                else Program.removeSelection(layer);

                //on click, move out in hierarchy
                Program.redrawLayerEditor();   //redraw layer map
                //Program.redrawLayerEditor();
                //layer.signalRedraw(); //redraw canvas //fixme idk about this
            }
        });

        return buttonOut;
    }
    //endregion

    //endregion
    //region common element manipulation ========================================

    //region title------------------------
    //generate title label
    public static JLabel makeTitle(LayerCore layer){
        return new JLabel(layer.getTitle());
    }

    // generate title text field
    public static JTextField makeTitleField(LayerCore layer){
        JTextField title = new JTextField(10); //generate text field with preferred size
        title.setText(layer.getTitle());

        // action
        title.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Program.addUndo(layer.setTitle(title.getText()));
                layer.setTitle(title.getText());

                Program.redrawLayerEditor();
            }
        });

        return title;
    }
    //endregion-
    //region clipping-------------------------------
    //add clipping toggle to field
    public static JCheckBox makeClippingToggle(LayerCore layer){
        //create toggle; set value to existing clipping values
        JCheckBox clipBox = new JCheckBox("clip", layer.getClipToParent());

        //on selection, toggle clipping in layer
        clipBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Program.addUndo(layer.setClipToParent());
                layer.setClipToParent();
               // Program.redrawAllRegions();
            }
        });

        return clipBox;
    }

    //generate clipping label
    public static JLabel makeClippingLabel(LayerCore layer){
        if (layer.getClipToParent()) return new JLabel("clipped");
        else return null;
    }
    //endregion
    //region visibility ----------------------
    //make toggle to visibility
    public static JCheckBox makeVisibleToggle(LayerCore layer){
        //create toggle; set value to existing values
        JCheckBox clipBox = new JCheckBox("Visible", layer.getVisible());

        //on selection, toggle visibility
        clipBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Program.addUndo(layer.setViewAlpha());
                layer.setVisible();
                //Program.redrawAllRegions();
                //layer.signalRedraw(); //redraw canvas
            }
        });

        return clipBox;
    }
    //endregion
    //region visibilty toggle button-------------------------
    //add duplicate button
    public static JButton makeVisibilityToggleButton(LayerCore layer){
        // text
        String text;
        if (layer.getVisible() == false) text = "-";
        else text = "\uD83D\uDC41"; //👁

        //add button
        JButton button = new JButton(text);

        //on button click, deploy action
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                layer.setVisible();
                //Program.redrawAllRegions();
            }
        });

        return button;
    }
    //endregion
    //region alpha -------------------------
    //add alpha edit box
    public static JPanel makeAlpha(LayerCore layer){
        //add alpha box
        JPanel alphaPanel = new JPanel();

        //create field for alpha text entering with a label
        JFormattedTextField alphaInput = new JFormattedTextField(layer.getAlpha()); //creates field with default alpha
        JLabel alphaLabel = new JLabel("alpha");    //creates label
        alphaPanel.add(alphaLabel);
        alphaPanel.add(alphaInput);
        //when change is detected, adjust alpha
        alphaInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //if input is valid, set input to alpha. Otherwise, set alpha to input.
                try{
                    //parse double; if a valid number, establish range
                    Double input = Double.parseDouble(alphaInput.getText());
                    layer.setAlpha(input);  //setAlpha performs validity checking
                    //layer.signalRedraw(); //redraw canvas
                }
                catch(Exception ee){
                    alphaInput.setValue(layer.getAlpha());
                }
               // Program.redrawAllRegions();
            }
        });
        alphaInput.setColumns(3);

        return alphaPanel;
    }
    //endregion
    //endregion
    //region layer only manipulation ========================================

    //region color ------------------------------

    //generate color toggle for layer
    public static JCheckBox makeColorToggle(Layer layer){
        //create toggle; set value to existing clipping values
        JCheckBox clipBox = new JCheckBox("use color", layer.getUseColor());

        //on selection, toggle using original color
        clipBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Program.addUndo(layer.setUseColor());
                layer.setUseColor();
                //Program.redrawAllRegions(); //redraw editor and canvas
            }
        });

        return clipBox;
    }

    //add color chooser to field (and helper)
    public static void addColorChooser(JPanel innerField, Layer layer){addColorChooser(innerField, 1, Color.BLACK, layer);}

    //text positions: 0 = none, 1 = left, 2 = right
    public static void addColorChooser(JPanel innerField, int textPosition, Color textColor, Layer layer){
        //create button with image color
        JButton colorButton = new JButton("     ");
        colorButton.setBackground(layer.getColor());    //set color

        //make label for color text NOTE: text field is selectable
        JTextField colorLabel = new JTextField(GuiOperations.formatColor(layer.getColor())); //get color label from unit color
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

                //Collect color from color selection dialogue; resets layer color
                Color selection = JColorChooser.showDialog(null,"Select a color", layer.getColor());
                //System.out.println(selection);

                //if color was a color, alter color choices
                if(selection == null) return;
                //Program.addUndo(layer.setColor(selection));
                layer.setColor(selection);

                //process color change in GUI
                colorButton.setBackground(layer.getColor());    //reset color for button
                colorLabel.setText(GuiOperations.formatColor(layer.getColor())); //reset color label
                innerField.revalidate(); //repaint

                Program.redrawAllRegions(); //redraw canvas
            }
        });
    }
    //endregion
    //region file loading------------------------

    // load file as layer: open file selection dialogue
    public static JButton loadFileAsLayerButton(Layer layer){
        // pick text
        String text = "Pick File";

        //button
        JButton buttonOut = new JButton(text);  //generate button

        buttonOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //open editor
                Program.selectFileFromProject(layer);
            }
        });

        return buttonOut;
    }

    //endregion
    //endregion
    //region folder only manipulation ========================================

    //region children expansion ------------------------------
    //generate button to toggle expansion of children layer visibility [folder]
    public static JCheckBox makeChildrenExpand(Folder layer){
        //create toggle button
        JCheckBox expandToggle = new JCheckBox("Children", layer.getChildrenVisiblity());

        //on selection, toggle expansion
        expandToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                layer.toggleChildrenVisibility();   //toggle expand fixme system.undo?
                //redraw field
                Program.redrawLayerEditor();
            }
        });

        // return toggle
        return expandToggle;
    }

    // generate button for show/hide children [folder]
    // expand or contract children visibility
    public static JButton makeChildrenExpandButton(Folder layer){
        //create toggle button; toggle text contents
        String text;
        if (layer.displayChildren) text = " v ";
        else text = " ^ ";

        // create button
        JButton expandToggle = new JButton(text);

        //button action: on selection, toggle expansion
        expandToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                layer.toggleChildrenVisibility();   //toggle expand fixme system.undo
                //redraw field
                Program.redrawLayerEditor();
            }
        });

        return expandToggle;
    }
    //endregion

    // region intersect clipping--------------------------
    // load file as clipping mask
    public static JButton loadFileAsIntersectMaskButton(Folder folder){
        // pick text
        String text = "Pick File";

        //button
        JButton buttonOut = new JButton(text);  //generate button

        buttonOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //open editor
                Program.selectIntersectClippingFile(folder);
            }
        });

        return buttonOut;
    }

    // remove file button
    public static JButton revokeIntersectMaskButton(Folder folder){
        // pick text
        String text = "Revoke";

        //button
        JButton buttonOut = new JButton(text);  //generate button

        buttonOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //deselect
                folder.revokeIntersectImage();
            }
        });

        return buttonOut;
    }

    //add clipping toggle to field
    public static JCheckBox makeIntersectClipToggle(Folder folder){
        //create toggle; set value to existing clipping values
        JCheckBox clipBox = new JCheckBox("intersect clip", folder.getIntersectClip());

        //on selection, toggle clipping in layer
        clipBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                folder.setIntersectClip();
            }
        });

        return clipBox;
    }
    //endregion

    //endregion
}