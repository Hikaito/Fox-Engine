package system.project.treeElements;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.swing.tree.DefaultMutableTreeNode;

public abstract class ProjectCore extends ProjectUnitCore {

    // constructor
    public ProjectCore(String type, long id){
        super(type);
        this.id = id;
    }

    //id
    @Expose
    @SerializedName(value = "ID")
    protected long id;
    public long getID(){return id;}
    public void setID(long newID){id = newID;}


    // abstract method for tree generation
    abstract public void getTree(DefaultMutableTreeNode top);
}
