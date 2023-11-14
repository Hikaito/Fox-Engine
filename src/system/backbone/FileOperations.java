package system.backbone;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import system.Program;
import system.gui.WarningWindow;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileOperations {

    // region file editing functions--------------------------------------------
    // fixme add redo and undo as appropriate

    //removes extension from file name
    public static String stripExtension(String path, String extension){
        if (path.length() < extension.length()) return path;
        return path.substring(0, path.length() - extension.length());
    }

    // extract file name from directory
    public static String stripDirectory(String path){
        File file = new File(path);
        return file.getName();
    }

    public static boolean validExtension(String path, String extension){
        // get length of extension
        int len = extension.length();

        // if file name is too short, invalid
        if (path.length() < len) return false;

        // otherwise validate for file ending
        String lastLen = path.substring(path.length() - len);   // get the last characters of the extension
        return lastLen.toLowerCase().equals(extension.toLowerCase());
        //returns true if valid ending found and false otherwise
    }

    // file extension validation
    public static String validateExtension(String file, String extension){
        // get length of extension
        int len = extension.length();

        // if file name is too short, extend
        if (file.length() < len) return file+extension;

        // otherwise validate for file ending
        String lastLen = file.substring(file.length() - len);   // get the last characters of the extension
        if (! lastLen.toLowerCase().equals(extension.toLowerCase())) return file+extension;

        // if extension found and valid, return given file name
        return file;
    }

    //get relative path for a file with respect to an absolute path
    public static Path trimDirectory(String directory, String path){
        Path absolute = Paths.get(path);        //get absolute path
        Path relative = Paths.get(directory);   //get directory
        return relative.relativize(absolute);   // return relative path
    }

    // file selection dialogue
    public static String selectFile(String basepath){
        //open file selection dialogue
        JFileChooser selectFile = new JFileChooser(basepath);
        
        // if user approves load operation, return file
        if(selectFile.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) return selectFile.getSelectedFile().getAbsolutePath();
        else return null;
    }

    // folder selection dialogue
    public static String selectFolder(String basepath){
        //open file selection dialogue
        JFileChooser selectFile = new JFileChooser(basepath);
        selectFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // if user approves load operation, return file
        if(selectFile.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) return selectFile.getSelectedFile().getAbsolutePath();
        else return null;
    }

    // replace current file with template file
    public static void loadTemplate(){
        // get user path
        String path = selectFile(Program.getTemplateFolderPath());

        // if valid file selected
        if(path != null){
            //send template to LayerManager to load file
            Program.loadFileAsManager(path);

            // log action
            Program.log("File " + path + " loaded as template.");
        }
    }

    // add selected template to current file
    public static void addTemplate(){
        // get user path
        String path = selectFile(Program.getTemplateFolderPath());

        // if valid file selected
        if(path != null){
            //send template to LayerManager to load file
            Program.mergeFileAsManager(path);

            // log action
            Program.log("File " + path + " added to current file as template.");
        }
    }

    // save file as new template
    public static void saveTemplate(){
        // get user path
        String path = selectFile(Program.getTemplateFolderPath());

        // if valid file selected
        if(path != null){
            // validate for file ending
            path = validateExtension(path, Program.getSaveFileExtension());

            // log action
            Program.log("File " + path + " attempt writing as new template file.");

            //write to file: write layer manager state to a file
            Program.generateManagerFile(path);

            // log action
            Program.log("File " + path + " written as new template file.");
        }
    }

    // load file
    public static void loadImage(){
        // get user path
        String path = selectFile(Program.getSaveFolderPath());

        // if valid file selected
        if(path != null){
            //send template to LayerManager to load file
            Program.loadFileAsManager(path);

            // log action
            Program.log("File " + path + " loaded as file.");
        }
    }

    // save file
    public static void saveFile(){
        // get user path
        String path = selectFile(Program.getSaveFolderPath());

        // if valid file selected
        if(path != null){
            // validate for file ending
            path = validateExtension(path, Program.getSaveFileExtension());

            // log action
            Program.log("File " + path + " attempt writing as save file.");

            //write to file: write layer manager state to a file
            Program.generateManagerFile(path);

            // log action
            Program.log("File " + path + " written as save file.");
        }
    }

    //method for exporting a final image; autosaves a copy as a regular document as well
    public static void exportDialogue(){
        Program.redrawCanvas(); //reset canvas fixme

        // get user path
        String path = selectFile(Program.getExportFolderPath());

        // if valid file selected
        if(path != null){
            // validate for file ending
            path = validateExtension(path, Program.getImageFileExtension());

            //if image can be saved, save the image. Otherwise report an error
            try {
                //write image to selected location
                if (ImageIO.write(Program.getCanvasImage(), Program.getImageExportFormat(), new File(path))){
                    // log action
                    Program.log("Image saved at " + path);

                    //if write was successful, autosave a log as well
                    String autosavePath = FileOperations.stripDirectory(path); // isolate file name
                    autosavePath = FileOperations.stripExtension(autosavePath, Program.getImageFileExtension());   // isolate file extension
                    autosavePath = Program.getAutosavePath(autosavePath);   // add folder and extension

                    //write to file: write layer manager state to a file
                    Program.generateManagerFile(autosavePath);

                    // log action
                    Program.log("Autosave saved at " + autosavePath);
                }
                //if file was not opened, report error
                else{
                    // log action
                    Program.log("Image " + path + " failed to save.");
                }
                //if file had catastrophic failure, report error
            } catch (IOException ioException) {
                // log action
                Program.log("Image " + path + " failed to save; Exception thrown. (may have also been failure of autosave of template)");
            }
        }
    }

    // generate new file
    public static void newFile(boolean doWarning){
        //signal delete canvas; delete file with warning if warning given
        if (doWarning) WarningWindow.warningWindow("This will delete the file. Proceed?", new newFileWarning());
        else new newFileWarning().enact();
    }
    public static class newFileWarning implements EventE {
        @Override
        public void enact() {
            //signal delete canvas
            Program.clearFile();

            // log action
            Program.log("File cleared (layer tree erased).");
        }
    }
    // endregion
    //region folder initialization---------
    //region initialization functions
    //function that generates path folders if they do not exist
    public static void generateDirectories(){
        File file;
        //code tests and generates a directory if necessary for each folder path

        file = new File(Program.getProjectPath()); //get an absolute path file
        if(!file.isDirectory()){
            file.mkdir(); //if file is not a directory, make the directory
            Program.log("Directory generated at '" + file + "'");
        }

        file = new File(Program.getTemplateFolderPath()); //get an absolute path file
        if(!file.isDirectory()){
            file.mkdir(); //if file is not a directory, make the directory
            Program.log("Directory generated at '" + file + "'");
        }

        file = new File(Program.getExportFolderPath()); //get an absolute path file
        if(!file.isDirectory()){
            file.mkdir(); //if file is not a directory, make the directory
            Program.log("Directory generated at '" + file + "'");
        }

        file = new File(Program.getSaveFolderPath()); //get an absolute path file
        if(!file.isDirectory()){
            file.mkdir(); //if file is not a directory, make the directory
            Program.log("Directory generated at '" + file + "'");
        }
    }
    //endregion
}
