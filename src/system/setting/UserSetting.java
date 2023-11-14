package system.setting;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

// settings container class
// loads, saves, etc the settings
// on change to folder paths, call generateDirectories [makes sure all referenced path directories actually exist]

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import system.Program;
import system.backbone.ColorAdapter;
import system.layerTree.RendererTypeAdapter;
import system.layerTree.data.TreeUnit;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UserSetting {
    // region save/load functionality --------------------------------

    // default control file
    private final static String fileName = "settings.json";

    // GSON builder
    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()                 // only includes fields with expose tag
            .setPrettyPrinting()                                    // creates a pretty version of the output
            .registerTypeAdapter(Color.class, new ColorAdapter())   // register color adapter
            .registerTypeAdapter(TreeUnit.class, new RendererTypeAdapter())   // register sorting deserializer for renderer
            .create();

    // Factory: return control object; generate if it does not exist.
    public static UserSetting get(){
        // get object
        UserSetting out = importJson();

        // generate new object if absent
        if (out == null){
            out = new UserSetting();
            out.exportJson();
        }

        // return object
        return out;
    }

    // load settings: loads object from file; returns null if failed
    public static UserSetting importJson(){
        // return control object loaded from file if possible
        try {
            String json = new String(Files.readAllBytes(Paths.get(fileName)));
            return gson.fromJson(json, UserSetting.class);

        }
        // return null if no object could be loaded
        catch (IOException e) {
            Program.log("Settings file could not be read.");
            return null;
        }
    }

    // export settings: Writes object to file; returns true for success and false for failure
    public boolean exportJson(){
        // derive json
        String json = UserSetting.gson.toJson(this);

        // write json to file
        try {
            FileWriter out = new FileWriter(fileName, false);
            out.write(json);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    //endregion
    //region user quality of life setting variables

    // canvas zoom acceleration
    @Expose
    @SerializedName(value = "mouse_zoom_acceleration")
    public int canvasMouseAcceleration = 10;

    // canvas zoom acceleration
    @Expose
    @SerializedName(value = "canvas_zoom_scale_factor")
    public double canvasScaleFactor = .2;

    // canvas zoom acceleration
    @Expose
    @SerializedName(value = "canvas_slider_min")
    public int canvasSliderMin = 1;

    // canvas zoom acceleration
    @Expose
    @SerializedName(value = "canvas_slider_max")
    public int canvasSliderMax = 20;

    // canvas zoom acceleration
    @Expose
    @SerializedName(value = "canvas_slider_neutral")
    public int canvasSliderNeutral = 5;

    // default x size for canvas
    @Expose
    @SerializedName(value = "default_canvas_width")
    public int defaultCanvasWidth = 1;      //1000

    // default x size for canvas
    @Expose
    @SerializedName(value = "default_canvas_height")
    public int defaultCanvasHeight = 1;     //1250

    //value for initial window size width
    @Expose
    @SerializedName(value = "default_window_width")
    public int defaultWindowWidth = 1200;

    //value for initial window size height
    @Expose
    @SerializedName(value = "default_window_height")
    public int defaultWindowHeight = 900;

    // number of elements in undo/redo stack
    //fixme unimplemented
    @Expose
    @SerializedName(value = "undo_redo_max")
    public int undoMax = 75;

    //selection color for one object
    @Expose
    @SerializedName(value = "selection_color_primary")
    public Color colorPrimarySelection = Color.yellow;

    //selection color for second object
    @Expose
    @SerializedName(value = "selection_color_secondary")
    public Color colorSecondarySelection = Color.blue;

    //selection color for many objects
    @Expose
    @SerializedName(value = "selection_color_tertiary")
    public Color colorManySelection = Color.cyan;

    //endregion
    //region filename conventions--------------------------

    // default name for new renderable objects
    @Expose
    @SerializedName(value = "default_renderable_name")
    public String defaultRenderableTitle = "unnamed";

    // default code for nocode objects
    @Expose
    @SerializedName(value = "default_file_code")
    public long defaultFileCode = -1;

    //extension for file saving
    @Expose
    @SerializedName(value = "save_file_extension")
    public String fileExtensionSave = ".txt";

    //extension for saving image file
    @Expose
    @SerializedName(value = "rendered_file_extension")
    public String fileExtensionRender = ".png";

    //format for image exporting
    @Expose
    @SerializedName(value = "render_file_format")
    public String imageRenderFormat = "png";

    //format for autosave file alteration
    @Expose
    @SerializedName(value = "autosave_filename_extension")
    public String filenameExtensionAutosave = "_AUTO";

    //format requiried on loaded images
    @Expose
    @SerializedName(value = "file_extension_load_image")
    public String fileExtensionLoadImage = ".png";

    //file for project settings
    @Expose
    @SerializedName(value = "project_file_name")
    public String projectFileName = "project.json";

    //endregion
    //region folder conventions--------------------------

    //folder for source images (project folder)
    @Expose
    @SerializedName(value = "default_project_folder")
    public String pathDefaultProject = "";

    //folder for exporting images
    @Expose
    @SerializedName(value = "folder_image_export")
    public String pathExportImageFolder = "renders";

    //folder for source templates
    @Expose
    @SerializedName(value = "folder_template")
    public String pathSourceTemplateFolder = "templates";

    //folder for saving save files to
    @Expose
    @SerializedName(value = "folder_save_export")
    public String pathExportSaveFolder = "saves";

    //endregion
}