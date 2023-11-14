package system.project.treeElements;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import system.Program;
import system.backbone.FileOperations;
import system.gui.WarningWindow;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class ProjectFile extends ProjectCore implements Comparable<ProjectFile> {

    // constructor
    public ProjectFile(String title, long id){
        super("file", id);  //super
        this.title = title;
    }

    // longer constructor
    public ProjectFile(String path, String title, long id){
        super("file", id);  //super
        this.path = path;
        this.title = title;
    }

    //path for file
    @Expose
    @SerializedName(value = "filepath")
    protected String path = "";
    public String getPath(){return path;}
    public void setPath(String path){this.path = path;}
    public void setNewPath(String directory, String path){
        // reject if directory doesn't match
        if(!(directory.equals(path.substring(0, directory.length())))){
            WarningWindow.warningWindow("File rejected; must be in root folder.");
            return;
        }

        this.path = FileOperations.trimDirectory(directory, path).toString();  // get relative path
        fileValid = true;
    }

    //file validation function
    protected boolean fileValid = false;
    public boolean isValid(){return fileValid;}
    public void setValid(boolean state){fileValid = state;}

    //boolean for use color
    @Expose
    @SerializedName(value = "use_color")
    protected boolean use_color = true;
    public void setUseColor(boolean set){use_color = set;}
    public boolean getUseColor(){return use_color;}

    //Color to use
    @Expose
    @SerializedName(value = "color")
    public Color color = Color.lightGray;
    public void setColor(Color color){this.color = color;}
    public Color getColor(){return color;}

    // generate GUI tree
    public void getTree(DefaultMutableTreeNode top){
        // add self to tree
        DefaultMutableTreeNode cat = new DefaultMutableTreeNode(this);
        top.add(cat);
    }

    // used for sorting during save and load
    @Override
    public int compareTo(ProjectFile o) {
        return path.compareTo(o.getPath());
    }

    //build menu for menubar
    public void generateMenu(JMenu root){
        // only add to tree if valid file
        if(fileValid){
            // add self to menu
            JMenuItem menu = new JMenuItem(this.getTitle());
            root.add(menu);

            // when clicked, generate new layer with file
            menu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    BufferedImage image = Program.getImage(getID());
                    //if image loaded properly, initalize a node
                    if (image != null){
                        Program.addLayer(id, use_color, color);
                    }
                    else{
                        Program.log("File missing for element " + title + " at " + path);
                    }
                }
            });
        }
    }
}
