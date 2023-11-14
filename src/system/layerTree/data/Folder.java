package system.layerTree.data;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

// object representing a folder

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import system.Program;
import system.backbone.Copy;
import system.layerTree.interfaces.LayerTreeFolder;
import system.layerTree.interfaces.Renderable;
import system.backbone.ImageOperations;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.ListIterator;

public class Folder extends LayerCore implements Copy<Folder>, Renderable, LayerTreeFolder {

    //region constructor and Copy interface------------------------------------

    //constructor
    public Folder(){
        super("folder");    //fixme remove this element entirelly later

        //folder
        displayChildren = true;
        clipIntersect = false;
    }

    //copy interface
    @Override
    public Folder copy() {
        return copy(true);
    }

    //internal copy
    // true = use copy title, false = true title
    public Folder copy(boolean label){
        Folder obj = new Folder();

        //LayerCORE
        obj.clipToParent = clipToParent;
        obj.visible = visible;
        obj.alpha = alpha;
        // use copy title if indicated
        if(label)   obj.title = title + " (copy)";
        else    obj.title = title;

        //Folder
        obj.displayChildren = displayChildren;
        // clone internal elements
        LinkedList<TreeUnit> children = getChildren();
        LinkedList<TreeUnit> newChildren = obj.getChildren();
        // copy each applicable object
        for(TreeUnit child:children){
            if (child instanceof Copy){
                TreeUnit clone = (TreeUnit) ((Copy<?>) child).copy();   // copy object
                clone.setParent(obj);   // set parent
                newChildren.addLast(clone); // add object to new list
            }
        }
        obj.clipIntersect = clipIntersect;
        obj.intersectImage = intersectImage.copy();

        // rendering
        //fixme i think this can be optimized for large folders by copying the original image object
        obj.requireRender();

        return obj;
    }
    //endregion
    //region intersect clipping----------------------------------

    // Purpose: controls intersect clipping False = no clipping, True = clipping
    @Expose
    @SerializedName(value = "intersect_clip")
    protected boolean clipIntersect;

    // accessor
    public boolean getIntersectClip(){
        return clipIntersect;
    }
    public boolean hasIntersectImage(){
        if (intersectImage == null) return false;
        return true;
    }


    // mutator (no argument = toggle)
    public void setIntersectClip(){
        // invert value if permission exists; otherwise revoke intersect
        if (intersectImage == null){
            clipIntersect = false;
        }
        else{
            clipIntersect = !clipIntersect;
        }

        // mark for rerendering and render
        Program.markImageForRender(this);   // mark tree for rerender
        Program.redrawAllRegions();             // signal for redraw
    }

    // Purpose: set and store image
    @Expose
    @SerializedName(value = "intersect_image")
    protected ImageUnit intersectImage = null;

    // set intersect image
    public void setIntersectImage(long fileCode){
        intersectImage = new ImageUnit(fileCode);   // create image unit
        clipIntersect = true;       // automatically use clipping

        // mark for rerendering and render
        Program.markImageForRender(this);   // mark tree for rerender
        Program.redrawAllRegions();             // signal for redraw
    }

    // remove intersect image
    public void revokeIntersectImage(){
        intersectImage = null;  // revoke image
        clipIntersect = false;  // revoke clip permission

        // mark for rerendering and render
        Program.markImageForRender(this);   // mark tree for rerender
        Program.redrawAllRegions();             // signal for redraw
    }

    // accessor for intersect image title
    public String getIntersectTitle(){
        if (intersectImage != null) return intersectImage.getTitle();
        return "no image selected";
    }

    //endregion
    //region renderable interface----------------------------------

    //requires render boolean
    protected boolean requiresRender = true;
    protected BufferedImage imageSource = new BufferedImage(Program.getDefaultCanvasWidth(), Program.getDefaultCanvasHeight(), BufferedImage.TYPE_INT_ARGB);   //image raw
    protected BufferedImage imageAlpha = imageSource;    //colored image with alpha mask applied

    // get image
    @Override
    public BufferedImage getImage(){
        // regenerate sources if marked as changed
        if (requiresRender){
            //fixme add internal booleans for marking alpha editing to skip steps

            // regenerate source image-----------------------------------------
            // get largest size among children that generate an image; also generates any rendering
            int maxH = 1;
            int maxW = 1;
            BufferedImage temp;
            LinkedList<TreeUnit> children = getChildren();
            for(TreeUnit child: children){
                if (child instanceof Renderable){
                    temp = ((Renderable)child).getImage();
                    if (temp.getWidth() > maxW) maxW = temp.getWidth();
                    if (temp.getHeight() > maxH) maxH = temp.getHeight();
                }
            }

            // generate stacking
            temp = new BufferedImage(maxW, maxH, BufferedImage.TYPE_INT_ARGB);
            Graphics g = temp.getGraphics();    // get graphics for image

            // for each child, iterating from reverse, draw graphics if applicable.
            // Note: there are two 'currents' for handling clipping
            ListIterator<TreeUnit> iterator = children.listIterator(children.size());
            BufferedImage cliptemp = null;

            // while children exist, iterate
            while(iterator.hasPrevious()){
                TreeUnit child = iterator.previous();   // get child
                // if child is renderable
                if (child instanceof Renderable){

                    // if current is set to clip, apply to cliptemp layer
                    if (child instanceof LayerCore && ((LayerCore) child).getClipToParent()){
                        // draw to cliptemp if cliptemp exists
                        if (cliptemp != null){
                            //cliptemp = ImageOperations.clipIntersect(cliptemp, ((Renderable) child).getImage()); fixme this is masking
                            cliptemp = ImageOperations.clipMerge(cliptemp, ((Renderable) child).getImage());
                        }
                    }
                    // if current is not set to clip, make new cliptemp layer and apply cliptemp layer
                    else{
                        // if there is a cliptemp, draw to image
                        if (cliptemp != null) g.drawImage(cliptemp, 0, 0, null);

                        // collect image to cliptemp
                        cliptemp = ((Renderable)child).getImage();
                    }
                }
            }
            //cleanup cliptemp
            if (cliptemp != null){g.drawImage(cliptemp, 0, 0, null);}

            g.dispose(); //clean up after self
            imageSource = temp;

            // mark requirement regenerate imageAlpha
            requiresAlphaRerender = true;

            //resolve require render
            unrequireRender();
        }
        if (requiresAlphaRerender){
            //regenerate imageAlpha; simply preserve source if alpha is full
            if (alpha == 1.0) imageAlpha = imageSource;
            else imageAlpha = ImageOperations.shiftAlpha(imageSource, alpha);

            // add intersection mask as appropriate
            if(hasIntersectImage() && clipIntersect){
                imageAlpha = ImageOperations.clipIntersect(intersectImage.get(), imageAlpha);
            }

            requiresAlphaRerender = false;
        }

        // if not visible, return empty buffered image
        if (!visible) return new BufferedImage(imageAlpha.getWidth(), imageAlpha.getHeight(), BufferedImage.TYPE_INT_ARGB);

        // otherwise return colored image
        return imageAlpha;
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
    //region LayerTreeFolder interface and associates---------------------------------

    //children
    @Expose
    @SerializedName(value = "children")
    private LinkedList<TreeUnit> children = new LinkedList<>();

    // region display children boolean---------------------------------
    @Expose
    @SerializedName(value = "display_children")
    public boolean displayChildren = true;

    //accessor and mutator for children visibility
    public Boolean getChildrenVisiblity(){return displayChildren;}
    public void toggleChildrenVisibility(){
        displayChildren = !displayChildren;
    }
    //endregion

    // grant access to children list
    @Override
    public LinkedList<TreeUnit> getChildren(){
        return children;
    }

    // clear children list
    @Override
    public void clearChildren(){
        children = new LinkedList<>();
        // .clear() does not explicitly clear
    }
    //endregion
}
