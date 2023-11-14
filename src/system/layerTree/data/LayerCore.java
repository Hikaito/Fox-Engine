package system.layerTree.data;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import system.Program;

public class LayerCore extends TreeUnit {
    // region constructor and copy constructor---------------------------

    // constructor
    public LayerCore(String type){
        super(type);    //type used for differentiating different subclasses with reserialization
        // state
        clipToParent = false;
        visible = true;
        alpha = 1.0;
        title = Program.getDefaultRenderableTitle();
    }

    //copy constructor
    public LayerCore copy(){
        LayerCore layer = new LayerCore(this.type);
        layer.clipToParent = clipToParent;
        layer.visible = visible;
        layer.alpha = alpha;
        layer.title = title;
        return layer;
    }

    //endregion
    //region LayerCORE functionality=======================

    //region alpha----------------------------------

    // Purpose: controls alpha (opacity) in blending. Ranges from 0 to 1
    @Expose
    @SerializedName(value = "alpha")
    protected double alpha;

    // this flag is used to skip some rerendering
    protected boolean requiresAlphaRerender = false;

    // accessor
    public double getAlpha(){
        return alpha;
    }

    // mutator
    public void setAlpha(double state){
        // generate receipt
        //LayerCORE.Receipt_Alpha receipt = new LayerCORE.Receipt_Alpha(this, alpha); fixme

        // change alpha
        state = Math.max(0, state); // prevent negative
        state = Math.min(1, state); // prevent greater than 1
        alpha = state;

        // change image
        // edit image

        // mark for rerendering and render
        Program.markParentForRender(this);  // mark tree for rerender
        requiresAlphaRerender = true;           // mark alpha rerender for this item
        Program.redrawAllRegions();             // signal for redraw

        //return receipt; fixme
    }

    //endregion
    //region visibility marker -----------------------------

    // Purpose: controls visibility alpha [false = off, true = on]
    @Expose
    @SerializedName(value = "visibility")
    protected boolean visible;

    // accessor
    public boolean getVisible(){
        return visible;
    }

    // toggle mutator
    public void setVisible(){
        setVisible(!visible);
    }

    // general mutator
    public void setVisible(boolean state){
        // generate receipt
        //fixme LayerCORE.Receipt_ViewAlpha receipt = new LayerCORE.Receipt_ViewAlpha(this, visible);

        // change alpha
        visible = state;


        // change image
        Program.markParentForRender(this);  // mark tree for rerender
        Program.redrawAllRegions();             // signal for redraw

        //signalRedraw(); // redraw canvas image [view alpha does not change the alpha of the image]

        //fixme return receipt;
    }

    //endregion
    //region clip toggle------------------------

    // Purpose: controls clipping to parent. False = no clipping, True = clipping
    @Expose
    @SerializedName(value = "clip")
    protected boolean clipToParent;

    // accessor
    public boolean getClipToParent(){
        return clipToParent;
    }

    // mutator (no argument = toggle)
    public void setClipToParent(){
        // call other function; invert value
        setClipToParent(!clipToParent);
    }

    // mutator (argument = set to argument)
    public void setClipToParent(boolean state){
        // generate receipt
        //fixme

        // set value
        clipToParent = state;

        // mark for rerendering and render
        Program.markParentForRender(this);  // mark tree for rerender
        Program.redrawAllRegions();             // signal for redraw

        // return object
        //fixme return task;
    }

    //endregion
    // region title---------------------------------------------------
    // Purpose: set titles
    @Expose
    @SerializedName(value = "title")
    protected String title;

    // accessor
    public String getTitle(){
        // if title is nothing, return default
        if (title.equals("")) return Program.getDefaultRenderableTitle();

        return title;
    }

    // mutator
    public void setTitle(String state){
        // generate receipt
        //fixme LayerCORE.Receipt_Title receipt = new LayerCORE.Receipt_Title(this, title);

        // set value
        title = state;

        //fixme return receipt;
    }

    //endregion

    //endregion
}
