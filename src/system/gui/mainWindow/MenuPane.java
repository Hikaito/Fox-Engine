package system.gui.mainWindow;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import system.Program;
import system.backbone.EventE;
import system.gui.WarningWindow;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuPane extends JMenuBar {

    // warning objects
    public static class templateLoadWarning implements EventE {
        @Override
        public void enact() {
            Program.loadTemplate();
        }
    }

    public static class loadfileWarning implements EventE{
        @Override
        public void enact(){Program.loadImage();}
    }

    // generate file menu
    public static JMenu generateFileMenu(){
        JMenu menu = new JMenu("File");    // generate menu

        JMenuItem item;

        // action ------------------------------------
        item = new JMenuItem("Load From Template");  // create menu item
        item.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // summon project function
                        WarningWindow.warningWindow("This will delete the current file. Proceed?", new templateLoadWarning());
                    }
                }
        );
        menu.add(item); // add item to menu

        // action ------------------------------------
        item = new JMenuItem("Save As Template");  // create menu item
        item.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // summon project function
                        Program.saveTemplate();
                    }
                }
        );
        menu.add(item); // add item to menu

        // add separator -----------------------------
        menu.addSeparator();

        // action ------------------------------------
        item = new JMenuItem("Add Template To Project");  // create menu item
        item.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // summon project function
                        Program.addTemplate();
                    }
                }
        );
        menu.add(item); // add item to menu

        // action ------------------------------------
        item = new JMenuItem("Load File");  // create menu item
        item.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // summon project function
                        WarningWindow.warningWindow("This will delete the current file. Proceed?", new loadfileWarning());
                    }
                }
        );
        menu.add(item); // add item to menu

        // action ------------------------------------
        item = new JMenuItem("Save File");  // create menu item
        item.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // summon project function
                        Program.saveFile();
                    }
                }
        );
        menu.add(item); // add item to menu

        // action ------------------------------------
        item = new JMenuItem("Export Image");  // create menu item
        item.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // summon project function
                        Program.exportImage();
                    }
                }
        );
        menu.add(item); // add item to menu

        // action ------------------------------------
        item = new JMenuItem("New File");  // create menu item
        item.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // summon project function
                        Program.newFile();
                    }
                }
        );
        menu.add(item); // add item to menu

        return menu;
    }

    // generate view menu
    public static JMenu generateViewMenu(){
        JMenu menu = new JMenu("View");     //generate menu

        JMenuItem item;

        //fixme add reset zoom option

        // action ------------------------------------
        item = new JMenuItem("Zoom In");  // create menu item
        item.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //on selection, reset canvas image box to zoomed version of image
                        Program.zoomInCanvas();
                    }
                }
        );
        menu.add(item); // add item to menu

        // action ------------------------------------
        item = new JMenuItem("Zoom Out");  // create menu item
        item.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //on selection, reset canvas image box to zoomed version of image
                        Program.zoomOutCanvas();
                    }
                }
        );
        menu.add(item); // add item to menu

        return menu;
    }

    // generate project menu
    public static JMenu generateProjectMenu(){
        JMenu menu = new JMenu("Project");    // generate menu

        JMenuItem item;

        // save project changes to project meta file
        item = new JMenuItem("Save Project");  // create menu item
        item.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // summon project function
                        Program.saveProject();
                    }
                }
        );
        menu.add(item); // add item to menu

        // open project editor
        item = new JMenuItem("Edit Project");  // create menu item
        item.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // summon project function
                        Program.editProject();
                    }
                }
        );
        menu.add(item); // add item to menu

        // open project load dialogue
        item = new JMenuItem("Load Different Project");  // create menu item
        item.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // summon project function
                        Program.loadProject();
                    }
                }
        );
        menu.add(item); // add item to menu

        return menu;
    }
}