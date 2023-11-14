package system.layerTree.interfaces;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import java.awt.image.BufferedImage;

// marks an object as valid for image rendering purposes

public interface Renderable {
    public BufferedImage getImage();
    public boolean getRenderState();
    public void requireRender();
    public void unrequireRender();
}
