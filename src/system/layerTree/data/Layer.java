package system.layerTree.data;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

// object representing a layer

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import system.Program;
import system.backbone.ImageOperations;
import system.backbone.Copy;
import system.layerTree.interfaces.Renderable;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Layer extends LayerCore implements Copy<Layer>, Renderable {

    //region constructor and copy interface--------------
    public Layer(){
        super("layer");
        useColor = true;
        color = Color.darkGray;
    }

    @Override
    public Layer copy() {
        return copy(true);
    }

    //internal copy
    // true = use copy title, false = true title
    public Layer copy(boolean label){
        Layer obj = new Layer();

        //LayerCORE
        obj.clipToParent = clipToParent;
        obj.visible = visible;
        obj.alpha = alpha;
        // use copy title if indicated
        if(label)   obj.title = title + " (copy)";
        else    obj.title = title;

        //Layer
        obj.useColor = useColor;
        obj.color = color;
        obj.setImage(imageSource.getFileCode());

        // rendering
        obj.requireRender();

        return obj;
    }
    //endregion
    // region image---------------------------------------------------

    // Purpose: set and store image
    @Expose
    @SerializedName(value = "image")
    protected ImageUnit imageSource = new ImageUnit();

    public void setImage(long fileCode){
        // set new image from code
        imageSource.setImage(fileCode);

        // change title only if title is still default
        if( title.equals(Program.getDefaultRenderableTitle())){
            title = imageSource.getTitle();
           // this.title = title;
        }

        //resetImageColor();
        // mark for rerendering and render
        Program.markImageForRender(this);   // mark tree for rerender
        Program.redrawAllRegions();             // signal for redraw
    }

    public long getFileCode(){
        return imageSource.getFileCode();
    }

    //endregion
    //region renderable interface----------------------------------

    //requires render boolean
    protected boolean requiresRender = true;
    // buffered image holding
    private BufferedImage imageColor = imageSource.get();           //image with color applied
    protected BufferedImage imageColorAlpha = imageSource.get();    //colored image with alpha mask applied

    // get image
    @Override
    public BufferedImage getImage(){
        // regenerate sources if marked as changed
        if (requiresRender){
            //fixme add internal booleans for marking alpha editing to skip steps

            // regenerate imageColor if a color is used
            if(useColor){imageColor = ImageOperations.colorAlpha(imageSource.get(), color);}
            else{imageColor = imageSource.get();}

            // require alpha rerender
            requiresAlphaRerender = true;

            //resolve require render
            unrequireRender();
        }
        if(requiresAlphaRerender){
            //regenerate imageColorAlpha; simply copy if alpha is full
            if (alpha == 1.0) imageColorAlpha = imageColor;
            else imageColorAlpha = ImageOperations.shiftAlpha(imageColor, alpha);

            requiresAlphaRerender = false;
        }


        // if not visible, return empty buffered image
        if (!visible) return new BufferedImage(imageColorAlpha.getWidth(), imageColorAlpha.getHeight(), BufferedImage.TYPE_INT_ARGB);

        // otherwise return colored image
        return imageColorAlpha;
    }

    // get render state
    @Override
    public boolean getRenderState(){
        return requiresRender;
    }

    // flag for rerender
    @Override
    public void requireRender(){
        requiresRender = true;
    }

    // set render to false
    @Override
    public void unrequireRender() {
        requiresRender = false;
    }

    //endregion
    // region useColor---------------------------------------------------

    // Purpose: boolean for whether or not to use the color to paint over the layer or use the original coloring
    @Expose
    @SerializedName(value = "color_use")
    protected boolean useColor;

    // accessor
    public boolean getUseColor(){
        return useColor;
    }

    // mutator
    public void setUseColor(){
        // invert choice
        setUseColor(!useColor);
    }

    // mutator
    public void setUseColor(boolean state){
        // generate receipt
        //fixme;

        // set value
        useColor = state;

        // mark for rerendering and render
        Program.markImageForRender(this);   // mark tree for rerender
        Program.redrawAllRegions();             // signal for redraw

        //fixme return receipt;
    }

    //endregion
    // region color ---------------------------------------------------

    // Purpose: fill color used to paint over layer
    @Expose
    @SerializedName(value = "color")
    protected Color color;

    // accessor
    public Color getColor(){
        return color;
    }

    // mutator
    public void setColor(Color state){
        // generate receipt
        //fixme

        // change state
        color = state;

        // mark for rerendering and render
        Program.markImageForRender(this);   // mark tree for rerender
        Program.redrawAllRegions();             // signal for redraw

        //fixme return receipt;
    }

}
