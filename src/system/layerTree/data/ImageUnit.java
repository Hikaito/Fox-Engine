package system.layerTree.data;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import system.Program;
import system.backbone.Copy;
import system.project.treeElements.ProjectFile;
import java.awt.image.BufferedImage;

public class ImageUnit implements Copy<ImageUnit> {
    // variable for storing image data
    protected BufferedImage imageSource = new BufferedImage(Program.getDefaultCanvasWidth(), Program.getDefaultCanvasHeight(), BufferedImage.TYPE_INT_ARGB);   //image raw

    // variable for first load, marked as true by default; controls object loading
    protected boolean firstLoad = true;

    // project file code
    @Expose
    @SerializedName(value = "image_code")
    private long fileCode = Program.getDefaultFileCode();

    // variable for object title
    private String title = Program.getDefaultRenderableTitle();

    //region initializer and copy -----------------------------
    // constructor with no file
    public ImageUnit(){    }

    // initializer for file code
    public ImageUnit(long fileCode){
        this.fileCode = fileCode;
    }

    // copy function
    @Override
    public ImageUnit copy(){
        return new ImageUnit(this.fileCode);
    }

    //endregion
    //region accessors -------------------------------

    // accessor for image title
    public String getTitle(){
        // if file isn't loaded, load in file to initialize file
        if (firstLoad){
            initialize();
            firstLoad = false;  // deactivate reload
        }

        return title;
    }

   // accessor for image
    public BufferedImage get(){
        // if file isn't loaded, load in file to initialize file
        if (firstLoad){
            initialize();
            firstLoad = false;  // deactivate reload
        }

        return imageSource;
    }

    // accessor for code
    public long getFileCode(){return fileCode;}

    //endregion
    //region mutators -------------------------------

    // change image
    public void setImage(long filecode){
        this.fileCode = filecode;   // change image code
        firstLoad = true;   // reset load in
    }

    // initialize: reset image and title if applicable
    private void initialize(){
        ProjectFile file = Program.getImageFile(fileCode);
        BufferedImage imageFile = Program.getImage(fileCode);
        if (file != null && imageFile != null){
            imageSource = imageFile;    // set image
            title = file.getTitle();    // set image title
        }
        else{title = Program.getDefaultRenderableTitle();}     // set title to default if no title
    }

    //endregion
}
