package system.gui.mainWindow;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import system.Program;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// class with some components for the main window class (simplify construction)
public class MainWindowComponents {

    // region component groups =========================

    // zoom slider panel
    public static JPanel makeZoomPanel(ImagePanel imageLabel){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createLineBorder(Color.darkGray));  //add outline
        panel.setMaximumSize(new Dimension(9000, 100));

        panel.add(new JLabel("Canvas Zoom"));
        //create slider element from scalable image settings
        panel.add(MainWindowComponents.makeZoomSlider(imageLabel));

        return panel;
    }

    // button bar panel
    public static JPanel makeButtonRow(){
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.darkGray));  //add outline
        panel.setMaximumSize(new Dimension(9000, 100));

        // add function buttons
        panel.add(MainWindowComponents.makeDeselectAllButton());  // add deselect all button
        panel.add(MainWindowComponents.makeNewFolderButton());        //add new folder button
        panel.add(MainWindowComponents.makeNewLayerButton());        //add new layer button
        panel.add(MainWindowComponents.makeExportButton());       // add export button

        return panel;
    }

    // layertree
    public static JScrollPane makeLayerTreePanel(){
        // create scroll panel for tree
        JScrollPane layerTreeScroll = new JScrollPane(Program.getLayerTreePanel()); //create scrollbox for these elements

        //set scroll acceleration to something more reasonable
        layerTreeScroll.getVerticalScrollBar().setUnitIncrement(Program.getScrollMouseAcceleration());
        layerTreeScroll.getHorizontalScrollBar().setUnitIncrement(Program.getScrollMouseAcceleration());

        return layerTreeScroll;
    }

    // endregion

    //region functionality components =========================

    // generate canvas zoom slider
    public static JSlider makeZoomSlider(ImagePanel target){
        // generate slider with range determined by slider default settings
        JSlider slider = new JSlider(Program.getCanvasSliderMin(), Program.getCanvasSliderMax());

        //initialize sliderposition for zoom to neutral fixme probably bugged at redraw
        //slider.setValue((int) imageLabel.getZoomValue());
        slider.setValue(Program.getCanvasSliderNeutral());

        //initialize labels for slider
        slider.setMajorTickSpacing(Program.getCanvasSliderMax() / 5);  //place major ticks every n increments
        slider.setMinorTickSpacing(2); //place major ticks every n increments
        slider.setPaintTicks(true); //paint ticks
        slider.setPaintLabels(true);    //paint labels
        slider.setMinimumSize(new Dimension(100, 50));  //enforce a minimum size for the slider

        //on change event, change zoom of image
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                //if the slider is still evaluating, ignore event (minimize calculations)
                if (!slider.getValueIsAdjusting()){
                    //if slider is not evaluating, adjust image zoom to slider value
                    target.setZoom(slider.getValue());
                    //imageLabel.setIcon(canvasImage.setZoom(slider.getValue()));
                }
            }
        });

        return slider;
    }

    // generate add folder button
    public static JButton makeNewFolderButton(){
        JButton button = new JButton("+ Folder");   //make button

        //on selection, issue command
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Program.addFolder();
            }
        });

        return button;
    }

    // generate add layer button
    public static JButton makeNewLayerButton(){
        JButton button = new JButton("+ Layer");   //make button

        //on selection, issue command
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Program.addLayer();
            }
        });

        return button;
    }


    // generate deselect all button
    public static JButton makeDeselectAllButton(){
        JButton button = new JButton("Deselect All");   //make button

        //on selection, issue command
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Program.clearSelection();
            }
        });

        return button;
    }

    // generate export button
    public static JButton makeExportButton(){
        JButton button = new JButton("Export");   //make button

        //on selection, issue command
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Program.exportImage();
            }
        });

        return button;
    }

    //endregion

}
