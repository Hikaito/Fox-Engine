package system.project.treeElements;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.LinkedList;

public class ProjectFolder extends ProjectCore implements ProjectFolderInterface {

    //constructor
    public ProjectFolder(String title, long id){
        super("folder", id);
        this.title = title;
    }

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
    public void getTree(DefaultMutableTreeNode top){
        // add self to tree
        DefaultMutableTreeNode cat = new DefaultMutableTreeNode(this);
        top.add(cat);

        //recurse to children
        for(ProjectUnitCore child: subitems){
            if (child instanceof ProjectCore) ((ProjectCore)child).getTree(cat);
        }
    }
    //endregion

    //build menu for menubar
    public void generateMenu(JMenu root){
        // add self to menu
        JMenu menu = new JMenu(this.getTitle());
        root.add(menu);

        //recursively register children
        for (ProjectUnitCore unit : subitems){
            if (unit instanceof ProjectFolder) ((ProjectFolder)unit).generateMenu(menu);
            else if (unit instanceof ProjectFile) ((ProjectFile)unit).generateMenu(menu);
        }
    }

}
