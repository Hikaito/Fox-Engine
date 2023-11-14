package system.project;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import system.Program;
import system.backbone.ColorAdapter;
import system.layerTree.RendererTypeAdapter;
import system.layerTree.data.Folder;
import system.layerTree.data.Layer;
import system.layerTree.data.TreeUnit;
import system.backbone.FileOperations;
import system.backbone.VersionNumber;
import system.gui.WarningWindow;
import system.project.treeElements.ProjectFolderInterface;
import system.project.treeElements.*;
import system.gui.project.ProjectEditor;
import system.gui.project.ProjectFileSelection;
import system.gui.project.ProjectReconciliation;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

public class ProjectManager {

    // UUID methods
    public String getUUID(){return root.getUUID();}
    public boolean matchesUUID(String other){
        return root.getUUID().equals(other);
    }

    public String getTitle(){return root.getTitle();}

    // reject if program number is lower than project
    public static boolean evaluateProjectNumber(VersionNumber program, VersionNumber project){
        if(program.compareTo(project) < 0) return false;
        return true;
    }

    // project root
    protected ProjectRoot root;
    public ProjectRoot getRoot(){return root;}

    // tree generation
    public DefaultMutableTreeNode getTree(){
        return root.getTree();
    }

    // updates menu offshoot entities
    public void updateDependencies(){
        buildDictionary();
        root.generateMenu();
    }

    //region dictionary----------------------------
    // dictionary
    protected HashMap<Long, ProjectFile> dictionary;

    //build dictionary
    public void buildDictionary(){
        dictionary = new HashMap<>();   // generate new dictionary
        addDict(root);  // build from root
    }

    // build dictionary recursive
    protected void addDict(ProjectUnitCore root){
        // case file: add to dictionary
        if (root instanceof ProjectFile){
            ProjectFile unit = (ProjectFile) root;  //cast object
            dictionary.put(unit.getID(), unit);
        }
        //recurse to all children for file types
        else if (root instanceof ProjectFolderInterface){
            ProjectFolderInterface unit = (ProjectFolderInterface) root;  //cast object
            for (ProjectUnitCore child: unit.getChildren()){
                addDict(child);
            }
        }
    }

    //query dictionary; returns null if none found
    public ProjectFile getFile(long code){
        // return null if nocode
        if(code == Program.getDefaultFileCode()) return null;

        return dictionary.get(code);
    }
    // endregion

    // save file------------------------------------
    // GSON builder
    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()     // only includes fields with expose tag
            .setPrettyPrinting()                        // creates a pretty version of the output
            .registerTypeAdapter(Color.class, new ColorAdapter())   // register color adapter
            .registerTypeAdapter(TreeUnit.class, new RendererTypeAdapter())   // register sorting deserializer for renderer
            .registerTypeAdapter(ProjectUnitCore.class, new ProjectTypeAdapter())   // registers abstract classes
            .create();

    // function for saving file
    public boolean saveFile(){
        // derive json
        String json = gson.toJson(root);

        //generate absolute path to location
        String path = Paths.get(root.getPath(), Program.getProjectFile()).toString();

        // write json to file
        try {
            FileWriter out = new FileWriter(path, false);
            out.write(json);
            out.close();
            // log action
            Program.log("File '" + Program.getProjectFile() + "' saved as project file at " + path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // initialization function----------------------------
    public void initializeEmpty(){
        root = new ProjectRoot("Null Project");          //create project root

        Program.log("Project initialized from filesystem.");
    }

    public void initializeProject(String fullpath){
        // attempt opening
        ProjectRoot tempRoot = readTree(fullpath);  // read file

        // test version number
        if(!evaluateProjectNumber(Program.getProgramVersion(), tempRoot.getProgramVersion())){
            WarningWindow.warningWindow("Project version invalid. Program version: " + Program.getProgramVersion() + "; Project version: " + tempRoot.getProgramVersion());
            initializeEmpty();
            return;
        }

        // open directory
        root = tempRoot;            // if project version acceptible, save project
        root = readTree(fullpath);

        Program.log("Project at "+ fullpath + " imported.");

        // open file editing dialogue
        // dictionary generated on window close
        try {
            ProjectEditor window = new ProjectEditor(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected ProjectRoot readTree(String fullpath){
        // open directory
        File file = new File(fullpath);             // create file object to represent path
        String title = file.getName();             //set project name to folder title
        ProjectRoot tempRoot = new ProjectRoot(title);          //create project root
        tempRoot.setPath(fullpath);                     //set directory path

        // recurse for each item in folder on created folder as parent
        File[] files = file.listFiles();
        assert files != null;
        for(File child: files){
            initializeTree(tempRoot.getChildren(), child, tempRoot);
        }
        // generate parents when complete
        tempRoot.generateParents();

        Program.log("Project at "+ fullpath + " read from filesystem.");

        return tempRoot;
    }

    protected void initializeTree(LinkedList<ProjectUnitCore> folder, File file, ProjectRoot temproot){
        //recursive case: folder; repeat call for each file inside
        if (file.isDirectory()){
            // exclude internal folders
            if(file.getAbsolutePath().endsWith(Program.getSaveFolder())
                    || file.getAbsolutePath().endsWith(Program.getRenderFolder())
                    || file.getAbsolutePath().endsWith(Program.getTemplateFolder())){
                return;
            }

            // create a folder node and add to tree
            ProjectFolder folderObj = new ProjectFolder(file.getName(), temproot.getNextID());
            folder.addLast(folderObj);

            // recurse for each item in folder on created folder as parent
            File[] files = file.listFiles();
            assert files != null;
            for(File child: files){
                initializeTree(folderObj.getChildren(), child, temproot);
            }
        }
        // base case: file. add file if a valid file is found
        else{
            // if the file has a valid extension
            if (FileOperations.validExtension(file.getName(), Program.getFileExtensionLoadImage())) {
                //create a file node and add to tree
                ProjectFile newFile = new ProjectFile(FileOperations.trimDirectory(temproot.getPath(),file.getAbsolutePath()).toString(),
                        FileOperations.stripExtension(file.getName(), Program.getFileExtensionLoadImage()),
                        temproot.getNextID());
                newFile.setValid(true);
                folder.addLast(newFile);
            }
        }
    }

    // loading function: folder selection
    public void loadProject(){

        String text = FileOperations.selectFolder(System.getProperty("user.dir"));   // get project, starting at local directory
        // if a directory was selected
        if(text != null){
            // autosave file
            Program.generateManagerFile(Program.getAutosavePath("PROJECT_CHANGE")); // autosave layer tree
            loadProject(text);   // load project
            Program.newFileNoWarning(); // remove null file NOTE it is importannt that this occur after the project change
            Program.redrawAllRegions();
            Program.redrawMenuBar();
            Program.setProjectPath(text);   // set new project path
        }
    }

    // loading function
    public void loadProject(String path){
        String fullpath = Paths.get(path, Program.getProjectFile()).toString();    //generate absolute path to project

        //load root
        try {
            String json = new String(Files.readAllBytes(Paths.get(fullpath)));  // read json from file
            ProjectRoot tempRootLoad = gson.fromJson(json, ProjectRoot.class);  // convert json to object of interest
            Program.log("Project loaded from " + fullpath);

            // set root path
            tempRootLoad.setPath(path);

            // evaluate project number
            if(!evaluateProjectNumber(Program.getProgramVersion(), tempRootLoad.getProgramVersion())){
                WarningWindow.warningWindow("Project version invalid. Program version: " + Program.getProgramVersion() + "; Project version: " + tempRootLoad.getProgramVersion());
                initializeEmpty();
                return;
            }

            root = tempRootLoad;    // keep load

            // load project tree
            ProjectRoot tempRoot = readTree(path);

            // place
            SortedSet<ProjectFile> discoveredFiles = new TreeSet<>();
            SortedSet<ProjectFile> existingFiles = new TreeSet<>();

            //traverse trees
            fillFileList(tempRoot, discoveredFiles);
            fillFileList(root, existingFiles);

            //eliminate common files
            //common files list exists to prevent concurrent removal and traversal errors
            SortedSet<ProjectFile> commonFiles = new TreeSet<>();
            // find files that are common
            for(ProjectFile file:existingFiles){
                // if a file was found, remove it from both lists
                if (discoveredFiles.contains(file)){
                    commonFiles.add(file);  //add to common list
                }
            }
            // remove common files
            for(ProjectFile file:commonFiles){
                // if a file was found, remove it from both lists
                discoveredFiles.remove(file);
                existingFiles.remove(file);
                file.setValid(true);
            }

            // open reconciliation dialogue
            // if files remain in either list, open window
            if (discoveredFiles.size() != 0 || existingFiles.size() != 0){
                // dictionary generated on window close
                ProjectReconciliation window = new ProjectReconciliation(this, existingFiles, discoveredFiles);
            }
            else{
                root.generateParents(); //generate parents to menu
                updateDependencies(); // generate dictionary if all is well
            }

            // save new location
            Program.setProjectPath(path);

        }
        // return null if no object could be loaded; initialize instead
        catch (IOException e) {
            Program.log("Project file could not be read. Source: " + fullpath);
            initializeProject(path);
        }
    }

    // tree traversal function for collecting files into a sorted list
    protected void fillFileList(ProjectUnitCore root, SortedSet<ProjectFile> files){
        // if file, add to files and return
        if(root instanceof ProjectFile){
            files.add((ProjectFile) root);
            return;
        }
        // if folder, generate children list
        LinkedList<ProjectUnitCore> children;
        if (root instanceof ProjectRoot) children = ((ProjectRoot) root).getChildren();
        else if (root instanceof ProjectFolder) children = ((ProjectFolder) root).getChildren();
        else return;

        // recurse on children
        for (ProjectUnitCore child:children){
            fillFileList(child, files);
        }
    }

    public String getAbsolutePath(ProjectFile file){
        return Paths.get(root.getPath(), file.getPath()).toString();
    }

    public static void mergeFile(ProjectFile keep, ProjectFile flake){
        keep.setValid(flake.isValid());     // copy validity
        keep.setPath(flake.getPath());      // copy path
    }

    public void addFile(ProjectFile file){
        file.setValid(true);    //set valid as true
        file.setID(root.getNextID());   // assign a proper ID value
        root.getChildren().addLast(file);   // add element to root list
        file.setParent(root);               // register root as parent
    }

    public void openEditor(){
        try {
            //generates dictionary on close
            ProjectEditor window = new ProjectEditor(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // selector for layer
    public void openSelector(Layer layer){
        try {
            ProjectFileSelection window = new ProjectFileSelection(this, layer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // selector for folder
    public void openSelector(Folder folder){
        try {
            ProjectFileSelection window = new ProjectFileSelection(this, folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JMenu getMenu(){
        return root.generateMenu();
    }

}
