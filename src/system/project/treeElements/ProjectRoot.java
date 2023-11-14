package system.project.treeElements;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import system.Program;
import system.backbone.VersionNumber;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.LinkedList;

public class ProjectRoot extends ProjectUnitCore implements ProjectFolderInterface {

    // constructor
    public ProjectRoot(){
        super("root");  //set type to root
        this.title = "Project";
        this.programVersion = Program.getProgramVersion();  // save program version
        uuid = java.util.UUID.randomUUID().toString();  // generate UUID value
    }

    // longer constructor
    public ProjectRoot(String title){
        super("root");  //set type to root
        this.title = title;     //set title
        this.programVersion = Program.getProgramVersion();  // save program version
        uuid = java.util.UUID.randomUUID().toString();  // generate UUID value
    }

    // unique project identifier
    @Expose
    @SerializedName(value = "UUID")
    protected final String uuid;
    public String getUUID(){return uuid;}

    // count for layer ID [saves space as it is smaller]
    @Expose
    @SerializedName(value = "current_layer_id")
    protected long layerIDIterate = 0;
    public long getNextID(){
        return layerIDIterate++;
    }
    public long checkID(){return layerIDIterate;}

    //directory of project (set at load)
    //path for file
    protected String path = "";
    public String getPath(){return path;}
    public void setPath(String path){this.path = path;}

    // program path
    @Expose
    @SerializedName(value="program_version")
    protected VersionNumber programVersion;
    public VersionNumber getProgramVersion(){return programVersion;}

    //region children-------------------------------------
    //Contain Children
    @Expose
    @SerializedName(value = "subitems")
    public LinkedList<ProjectUnitCore> subitems = new LinkedList<>();
    @Override
    public LinkedList<ProjectUnitCore> getChildren(){return subitems;}

    // parent generation
    public void generateParents(){
        for(ProjectUnitCore unit: subitems){
            unit.parent = this;
            if (unit instanceof ProjectFolder) ((ProjectFolder)unit).generateParents();
        }
    }
    //endregion

    //region tree-------------------------
    // generate GUI tree
    public DefaultMutableTreeNode getTree(){
        // create tree root
        DefaultMutableTreeNode cat = new DefaultMutableTreeNode(this);

        //recurse to children
        for(ProjectUnitCore child: subitems){
            if (child instanceof ProjectCore) ((ProjectCore)child).getTree(cat);
        }

        return cat;
    }
    //endregion

    JMenu menu = null;

    //build menu for menubar
    public JMenu generateMenu(){
        //create menu element for self; initalize when necessary
        if(menu == null){
            menu = new JMenu(getTitle());
        }
        else{
            menu.removeAll();
            menu.setText(getTitle());
        }

        //recursively register children
        for (ProjectUnitCore unit : subitems){
            if (unit instanceof ProjectFolder) ((ProjectFolder)unit).generateMenu(menu);
            else if (unit instanceof ProjectFile) ((ProjectFile)unit).generateMenu(menu);
        }

        menu.revalidate();
        return menu;
    }
}
