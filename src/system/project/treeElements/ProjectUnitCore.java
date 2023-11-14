package system.project.treeElements;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public abstract class ProjectUnitCore {

    //constructor
    public ProjectUnitCore(String type){
        this.type = type;
    }

    //type differentiation [used for saving and loading]
    @Expose
    @SerializedName(value = "type")
    private final String type;
    public String getType(){return type;}

    //alias (user-generated name)
    @Expose
    @SerializedName(value = "alias")
    protected String title;
    public void setTitle(String title){this.title = title;}
    public String getTitle(){return title;}

    // necessary for tree
    @Override
    public String toString(){return title;}

    // tree operations
    protected ProjectUnitCore parent = null;
    public ProjectUnitCore getParent(){return parent;}
    public void setParent(ProjectUnitCore obj){parent = obj;}
}
