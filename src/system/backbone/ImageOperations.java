package system.backbone;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import java.awt.*;
        import java.awt.image.BufferedImage;

public class ImageOperations {
    //operation for adding a background to any transparent areas
    public static BufferedImage colorBackground(BufferedImage source, Color color){
        //make container image
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        // generate stacking
        Graphics g = result.getGraphics();    // get graphics for image

        //copy alpha and alter color
        int colorInt = color.getRGB();

        // stack color
        // generate image and fill with the color
        BufferedImage colorFill = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        // optimization step
        int width = colorFill.getWidth();
        int height = colorFill.getHeight();

        //for each row
        for (int i = 0; i < width; i++){
            //for each column
            for(int j = 0; j < height; j++){
                colorFill.setRGB(i, j, 0xff000000 | colorInt);   //add full alpha to new color
            }
        }

        // draw base
        g.drawImage(colorFill, 0, 0, null);

        // draw image
        g.drawImage(source, 0, 0, null);

        // clean up
        g.dispose();

        //return
        return result;
    }

    //operation for making a color copy of an image's alpha
    public static BufferedImage colorAlpha(BufferedImage source, Color color){
        //make container image
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        // optimization step
        int width = result.getWidth();
        int height = result.getHeight();

        //copy alpha and alter color
        int colorInt = color.getRGB();
        //for each row
        for (int i = 0; i < width; i++){
            //for each column
            for(int j = 0; j < height; j++){
                int data = source.getRGB(i, j) & 0xff000000;    //isolate alpha
                result.setRGB(i, j, data | (colorInt & 0x00ffffff));   //add alpha to new color; note: color alpha default is 255
            }
        }

        //return
        return result;
    }

    //operation for adjusting the alpha of an image by a factor
    public static BufferedImage shiftAlpha(BufferedImage source, double factor){
        //make container image
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        // optimization step
        int width = result.getWidth();
        int height = result.getHeight();

        //for each row
        for (int i = 0; i < width; i++){
            //for each column
            for(int j = 0; j < height; j++){
                int data = source.getRGB(i, j);
                int alpha = (data >> 24) & 0xff;    //isolate alpha
                alpha *= factor;
                //constrain results
                if (alpha > 255) alpha = 255;
                if (alpha < 0) alpha = 0;
                alpha = (alpha & 0xff) << 24; //shift back to position
                result.setRGB(i, j, alpha | (data & 0x00ffffff));   //add alpha to new color; note: color alpha default is 255
            }
        }

        //return
        return result;
    }

    //operation for clipping mask
    public static BufferedImage clipMerge(BufferedImage source, BufferedImage overlay){
        //make container image
        BufferedImage result = new BufferedImage(Math.max(source.getWidth(), overlay.getWidth()),
                Math.max(source.getHeight(), overlay.getHeight()), BufferedImage.TYPE_INT_ARGB);

        //merge layers
        Graphics g = result.getGraphics(); //get graphics
        g.drawImage(source, 0, 0, null); //FIXME this line isn't useful
        g.drawImage(overlay, 0, 0, null);
        g.dispose(); //clean up after self

        // optimization step
        int width = source.getWidth();
        int height = source.getHeight();

        //for each row
        for (int i = 0; i < width; i++){
            //for each column
            for(int j = 0; j < height; j++){
                int data = source.getRGB(i, j) & 0xff000000;    //isolate alpha
                int color = result.getRGB(i, j) & 0x00ffffff;   //isolate color
                result.setRGB(i, j, data | color);   //add alpha to new color; note: color alpha default is 255
            }
        }

        //return
        return result;
    }

    //operation for intersection mask
    //takes the information in top and clips so it has the min alpha between top and mask
    public static BufferedImage clipIntersect(BufferedImage mask, BufferedImage top){
        //make container image
        BufferedImage result = new BufferedImage(Math.max(mask.getWidth(), top.getWidth()),
                Math.max(mask.getHeight(), top.getHeight()), BufferedImage.TYPE_INT_ARGB);

        //draw graphics
        Graphics g = result.getGraphics(); //get graphics
        g.drawImage(top, 0, 0, null);
        g.dispose(); //clean up after self

        int minWidth = Math.min(mask.getWidth(), top.getWidth());
        int minHeight = Math.min(mask.getHeight(), top.getHeight());

        //clip alpha
        //for each row
        for (int i = 0; i < minWidth; i++){
            //for each column
            for(int j = 0; j < minHeight; j++){
                //set alpha to smaller alpha
                int alphaA = top.getRGB(i, j) & 0xff000000;  //get top image alpha
                int alphaB = mask.getRGB(i, j) & 0xff000000;    //get bottom image alpha

                //if alpha a is smaller, set to alpha a by doing nothing, since alpha a is already the alpha
                //needs unsigned compare or it will misinterpret a larger number as smaller due to sign
                //if alpha B is smaller, set to alpha B
                if (Integer.compareUnsigned(alphaA, alphaB) > 0){ result.setRGB(i,j, alphaB | (result.getRGB(i,j) & 0x00ffffff));}
            }
        }

        //return
        return result;
    }
}
