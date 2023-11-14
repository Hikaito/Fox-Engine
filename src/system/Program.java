package system;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import system.layerTree.RendererTypeAdapter;
import system.backbone.ColorAdapter;
import system.layerTree.data.*;
import system.gui.mainWindow.MainWindow;
import system.undo.Receipt;
import system.undo.UndoRedoStack;
import system.setting.UserSetting;
import system.backbone.FileOperations;
import system.backbone.VersionNumber;
import system.project.ProjectManager;
import system.project.treeElements.ProjectFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

// initialization for actual program
// control object whose purpose it is to send commands to all other things [message passing core]

// convention: all file alteration functions must report action to the log function

public class Program {

    //region initialization------------------------------------------

    // "global" variables
    private static UserSetting userSetting;
    private static MainWindow window;
    private static LayerManager layerManager;
    private static UndoRedoStack undoRedoStack;
    private static ProjectManager projectManager;

    // function to launch, control, and represent program window
    public static void main(String[] args){

        // load user settings
        userSetting = UserSetting.get();
        if (!userSetting.pathDefaultProject.equals("")) FileOperations.generateDirectories();  // refresh directories if a project was selected

        // load project
        projectManager = new ProjectManager();  // generate manager
        // if no project, then initialize null
        if(userSetting.pathDefaultProject.equals("")) projectManager.initializeEmpty();
        else projectManager.loadProject(getProjectPath());    // load project

        // initialize project
        layerManager = new LayerManager();
        layerManager.setProjectID(projectManager.getUUID());
        layerManager.setProjectTitle(projectManager.getTitle());

        // initialize undo/redo stack
        undoRedoStack = new UndoRedoStack(userSetting.undoMax);

        //initialize frame
        try {
            window = new MainWindow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //endregion
    //region utility functions---------------------------------------------------

    // Version numbers
    private static VersionNumber programVersionNumber = new VersionNumber("1.0.0");
    public static VersionNumber getProgramVersion(){return programVersionNumber;}

    // function for log output
    private static final PrintStream logStreamOut = System.out;  // stream for log outputs
    public static void log(String input){
        logStreamOut.println(input);
    }

    // window title
    public static String getWindowTitle(){return "Fox Engine";}

    // GSON builder
    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()     // only includes fields with expose tag
            .setPrettyPrinting()                        // creates a pretty version of the output
            .registerTypeAdapter(Color.class, new ColorAdapter())   // register color adapter
            .registerTypeAdapter(TreeUnit.class, new RendererTypeAdapter())   // register sorting deserializer for renderer
            .create();

    //endregion
    //region MainWindow operations --------------------------------------------

    // composite function: re-render both canvas and editors
    public static void redrawAllRegions(){
        redrawCanvas();
        redrawLayerEditor();
    }

    // composite function: re-render editor panes
    public static void redrawLayerEditor(){
        layerManager.rebuildLayerTree();
        layerManager.rebuildEditorPanel();
        window.repaintLayerEditor();    //clears lingering removed elements from underlying rectangles
    }

    // vanilla functions
    public static void redrawCanvas(){
        window.redrawCanvas();
    } // re-render the canvas image from layer tree
    public static void redrawMenuBar(){
        window.regenMenuBar();
    } // rerender menu bar
    public static void zoomInCanvas(){ window.zoomInCanvas(); }
    public static void zoomOutCanvas(){ window.zoomOutCanvas();}

    //endregion
    //region ProjectManager -----------------------------

    // loads the image associated with a filecode
    public static String getProjectID(){return projectManager.getUUID();}
    public static ProjectFile getImageFile(long fileCode){ return projectManager.getFile(fileCode);}
    public static BufferedImage getImage(long fileCode){
        //get file
        ProjectFile file = projectManager.getFile(fileCode);
        if (file == null) return null;
        String path = projectManager.getAbsolutePath(file); // get image path
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void saveProject() {projectManager.saveFile();}
    public static void loadProject(){projectManager.loadProject();}
    public static void editProject(){projectManager.openEditor();}
    public static void selectFileFromProject(Layer layer){projectManager.openSelector(layer);}
    public static void selectIntersectClippingFile(Folder folder){projectManager.openSelector(folder);}
    public static JMenu getProjectMenu(){
        return projectManager.getMenu();
    }

    // endregion
    // region SelectionManger [via LayerManager] -----------------------

    public static void addSelection(TreeUnit obj){layerManager.addSelection(obj);}
    public static void removeSelection(TreeUnit obj){layerManager.removeSelection(obj);}
    public static void clearSelection(){layerManager.clearSelection();}
    public static TreeUnit peekSelectionTop(){return layerManager.peekSelectionTop();}
    public static SelectionManager.selectionPriority getSelectionStatus(){return layerManager.getSelectionStatus();}
    public static SelectionManager.selectionPriority checkSelection(TreeUnit obj){return layerManager.checkSelection(obj);}
    public static Color selectionColor(TreeUnit unit){return layerManager.selectionColor(unit);}

    // endregion
    // region LayerManager operations ---------------------------------

    //generate a save file for the project manager
    public static boolean generateManagerFile(String path){
        // derive json
        String json = gson.toJson(layerManager);

        // write json to file
        try {
            FileWriter out = new FileWriter(path, false);
            out.write(json);
            out.close();
            Program.log("LayerManager Save File '" + path + "' successfully written.");
        } catch (IOException e) {
            //e.printStackTrace(); fixme
            Program.log("LayerManager Save File '" + path + "' could not be written.");
            return false;
        }

        return true;
    }

    // open a save and generate a manager to hold it; does not assign a manager
    public static LayerManager loadManagerFile(String path){

        // return control object loaded from file if possible
        try {
            String json = new String(Files.readAllBytes(Paths.get(path)));
            return gson.fromJson(json, LayerManager.class);
        }

        // return null if no object could be loaded
        catch (IOException e) {
            Program.log("LayerManager Save File '" + path + "' could not be read.");
            return null;
        }
    }

    // open a save file and load it as the layer manager
    public static void loadFileAsManager(String path){
        LayerManager temp = loadManagerFile(path);  // load file
        if (temp == null) return;   // reject failed loads

        //if successful, perform program transition
        layerManager.replaceManager(temp);
        redrawAllRegions();
        Program.log("LayerManager Save File '" + path + "' loaded as layer manager.");
    }

    // open a save file and merge it into the existing tree
    public static void mergeFileAsManager(String path){
        LayerManager temp = loadManagerFile(path);  // load file
        if (temp == null) return;   // reject failed loads

        //if successful, perform program transition
        layerManager.addManager(temp);
        redrawAllRegions();
        Program.log("LayerManager Save File '" + path + "' loaded as layer manager.");
    }

    // vanilla functions
    public static void clearFile(){layerManager.clear();}
    public static void moveLayer(TreeUnit unit, LayerManager.direction dir){layerManager.moveLayer(unit, dir);}
    public static void addFolder(){layerManager.addFolder();}
    public static void addLayer(){layerManager.addLayer();}
    public static void addLayer(long code, boolean useColor, Color color){layerManager.addLayer(code, useColor, color);}
    public static void duplicateTreeUnit(TreeUnit unit){layerManager.duplicate(unit);}
    public static void deleteTreeUnit(TreeUnit unit, Boolean bool){layerManager.delete(unit, bool);}
    public static void markImageForRender(TreeUnit unit){layerManager.markImageForRender(unit);}
    public static void markParentForRender(TreeUnit unit){layerManager.markParentForRender(unit);}
    public static BufferedImage getCanvasImage(){return layerManager.stackImage();}
    public static JPanel getLayerTreePanel(){return layerManager.getLayerTreePanel();}
    public static JPanel getLayerEditPanel(){return layerManager.getLayerEditorPanel();}

    //endregion
    //region file operations ---------------------------------------------------

    // file editing
    public static void loadTemplate(){ FileOperations.loadTemplate();}
    public static void addTemplate(){ FileOperations.addTemplate();}
    public static void saveTemplate(){ FileOperations.saveTemplate();}
    public static void loadImage(){ FileOperations.loadImage();}
    public static void saveFile(){ FileOperations.saveFile();}
    public static void exportImage(){ FileOperations.exportDialogue();}
    public static void newFile(){ FileOperations.newFile(true);}
    public static void newFileNoWarning(){ FileOperations.newFile(false);}

    // endregion
    // region undo/redo ---------------------------------------------------

    public static void addUndo(Receipt task){
        undoRedoStack.add(task);
    }
    public static void redo(){
        undoRedoStack.redo();
    }
    public static void undo(){
        undoRedoStack.undo();
    }

    // endregion
    // region UserSetting object accessors and mutators ---------------------------------------------------

    public static int getScrollMouseAcceleration(){return userSetting.canvasMouseAcceleration;}
    public static double getCanvasScaleFactor(){return userSetting.canvasScaleFactor;}
    public static int getCanvasSliderMin(){return userSetting.canvasSliderMin;}
    public static int getCanvasSliderMax(){return userSetting.canvasSliderMax;}
    public static int getCanvasSliderNeutral(){return userSetting.canvasSliderNeutral;}
    public static int getDefaultCanvasWidth(){return userSetting.defaultCanvasWidth;}
    public static int getDefaultCanvasHeight(){return userSetting.defaultCanvasHeight;}
    public static int getDefaultWindowWidth(){return userSetting.defaultWindowWidth;}
    public static int getDefaultWindowHeight(){return userSetting.defaultWindowHeight;}
    public static int getUndoMax(){return userSetting.undoMax;}
    public static Color getPrimarySelectionColor(){return userSetting.colorPrimarySelection;}
    public static Color getSecondarySelectionColor(){return userSetting.colorSecondarySelection;}
    public static Color getManySelectionColor(){return userSetting.colorManySelection;}
    public static String getDefaultRenderableTitle(){return userSetting.defaultRenderableTitle;}
    public static long getDefaultFileCode() {return userSetting.defaultFileCode;}
    public static String getSaveFileExtension(){return userSetting.fileExtensionSave;}
    public static String getImageFileExtension(){return userSetting.fileExtensionRender;}
    public static String getImageExportFormat(){return userSetting.imageRenderFormat;}
    public static String getAutosavePath(String file){
        //path format: "file.txt_AUTO.txt" in the autosave folder
        return Paths.get( Program.getSaveFolderPath(), file + userSetting.filenameExtensionAutosave + userSetting.fileExtensionSave).toString();
    }
    public static String getFileExtensionLoadImage(){return userSetting.fileExtensionLoadImage;}
    public static String getProjectFile(){return userSetting.projectFileName;}
    public static String getProjectPath(){ return userSetting.pathDefaultProject;}
    public static void setProjectPath(String path){
        userSetting.pathDefaultProject = path;    // set variable
        userSetting.exportJson();   // export object
        FileOperations.generateDirectories();  // refresh directories
    }
    public static String getSaveFolder(){return userSetting.pathExportSaveFolder;}
    public static String getSaveFolderPath(){return Paths.get(userSetting.pathDefaultProject, userSetting.pathExportSaveFolder).toString();}
    public static String getTemplateFolder(){return userSetting.pathSourceTemplateFolder;}
    public static String getTemplateFolderPath(){return Paths.get(userSetting.pathDefaultProject, userSetting.pathSourceTemplateFolder).toString();}
    public static String getRenderFolder(){return userSetting.pathExportImageFolder;}
    public static String getExportFolderPath(){return Paths.get(userSetting.pathDefaultProject, userSetting.pathExportImageFolder).toString();}

    // endregion
}
