package system.gui.mainWindow;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

//build from scaleable image
// generate the scaled canvas image

import system.Program;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImagePanel extends JLabel{

    //internal state
    private double zoom = 1.0;  //zoom setting

    private BufferedImage image;        // source image [unzoomed image]
    private ImageIcon imageIcon;        // image icon [the displayable element]

    //image constructor ---------------------------------------------
    public ImagePanel(){
        // generate arbitrary image
        // NOTE: using setImage causes race condition on initialization with Program
        // set new image
        image = new BufferedImage(1,1,6);

        // get canvas image
        generateIcon();
        setIcon(imageIcon);

    }

    // image from path ---------------------------------------------
    public void setImage(String filepath){
        try {
            image = ImageIO.read(new File(filepath));  //get image
            setImage(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // generate new icon ---------------------------------------------
    private void generateIcon(){
        BufferedImage zoomed = getZoom();   // generate zoomed image
        imageIcon = new ImageIcon(zoomed);  // generate new icon
    }

    // generate a zoomed version of the image ---------------------------------------------
    private BufferedImage getZoom(){
        //create an image for the zoom to go in
        int width = Math.max(1, (int) (zoom * image.getWidth()));
        int height = Math.max(1, (int) (zoom * image.getHeight()));
        BufferedImage zoomed = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        //create a scale transformer
        AffineTransformOp transformOp = new AffineTransformOp(AffineTransform.getScaleInstance(zoom, zoom), AffineTransformOp.TYPE_BILINEAR);
        //FIXME this would be a cool setting to introduce, the transform interpolation

        // generate zoomed image
        zoomed = transformOp.filter(image, zoomed);

        return zoomed;
    }

    // set image from file [returns false for fail and true for success] ---------------------------------------------
    public boolean readImage(String filepath){
        // read file
        try {
            BufferedImage in = ImageIO.read(new File(filepath));  //get image
            setImage(in);       // set image
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // set image [pass in null to keep original image] ---------------------------------------------
    public void setImage(BufferedImage newImage){
        // set new image
        if (newImage != null) image = newImage;

        // get canvas image
        generateIcon();
        setIcon(imageIcon);

        // redraw canvas
        //Program.redrawCanvas();  // redraw canvas
    }

    // some public mutators ---------------------------------------------
    //zoom in on canvas image: called by menu bar options ---------------------------------------------
    public void zoomIn(){
        //generate zoom
        zoom += (zoom * Program.getCanvasScaleFactor());
        setImage((BufferedImage) null);  //return zoomed image
    }

    //zoom out on canvas image: called by menu bar options ---------------------------------------------
    public void zoomOut(){
        //generate zoom
        zoom -= (zoom * Program.getCanvasScaleFactor());
        setImage((BufferedImage) null);  //return zoomed image
    }

    //setzoom: used by sidebar slider ---------------------------------------------
    public void setZoom(int factor){
        //System.out.println("zoom set to " + factor + "/10");
        setZoom(factor / 5.0);
    }

    //set zoom accessor for zoomed image ---------------------------------------------
    public void setZoom(double zoomFactor){
        zoom = zoomFactor;  //set new zoom
        setImage((BufferedImage) null);  //return zoomed image
    }

    //getzoom: used by siderbar slider to initialize slider at redraw
    public double getZoomValue(){
        return zoom;
    }
}
