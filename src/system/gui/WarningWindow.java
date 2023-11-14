package system.gui;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import system.backbone.EventE;
import system.setting.CoreGlobal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WarningWindow {
    //warning window that invokes an action on acceptance
    public static void warningWindow(String text, EventE event){
        JFrame frame = new JFrame();

        //overall window
        JFrame jFrame = new JFrame("Warning");   //initialize window title
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //instate window close when exited
        jFrame.setSize(CoreGlobal.WARNING_WINDOW_WIDTH,CoreGlobal.WARNING_WINDOW_HEIGHT);   //initialize size
        jFrame.setLocationRelativeTo(null); //center window

        // main region
        JPanel region = new JPanel();
        region.setLayout(new BoxLayout(region,BoxLayout.Y_AXIS));   //stack items vertically
        jFrame.add(region);

        // text region
        region.add(new JLabel(text));

        // buttons region
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout()); // host items horizontally
        region.add(buttons);

        //cancel button
        JButton cancelButton = new JButton("Cancel");
        //on selection, toggle using original color
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFrame.dispose();
            }
        });
        buttons.add(cancelButton);

        //accept button
        //cancel button
        JButton acceptButton = new JButton("Accept");
        //on selection, toggle using original color
        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //set values
                if (event != null) event.enact();
                jFrame.dispose();
            }
        });
        buttons.add(acceptButton);

        jFrame.setVisible(true);    //initialize visibility (needs to be at the end of initialization)

        //[it should probably lock other windows and also spawn in the middle]
    }

    //warning window that pops up but doesn't do anything
    public static void warningWindow(String text){
        JFrame frame = new JFrame();

        //overall window
        JFrame jFrame = new JFrame("Warning");   //initialize window title
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //instate window close when exited
        jFrame.setSize(CoreGlobal.WARNING_WINDOW_WIDTH,CoreGlobal.WARNING_WINDOW_HEIGHT);   //initialize size
        jFrame.setLocationRelativeTo(null); //center window

        // main region
        JPanel region = new JPanel();
        region.setLayout(new BoxLayout(region,BoxLayout.Y_AXIS));   //stack items vertically
        jFrame.add(region);

        // text region
        region.add(new JLabel(text));

        // buttons region
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout()); // host items horizontally
        region.add(buttons);

        //accept button
        //cancel button
        JButton acceptButton = new JButton("Accept");
        //on selection, toggle using original color
        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //set values
                jFrame.dispose();
            }
        });
        buttons.add(acceptButton);

        jFrame.setVisible(true);    //initialize visibility (needs to be at the end of initialization)

        //[it should probably lock other windows and also spawn in the middle]
    }
}
