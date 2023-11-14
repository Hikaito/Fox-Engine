package system.setting;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

// similar to control, but designed to be more immutable
// holds design globals, may be moved to Control later

public class CoreGlobal {
    public static final int LEFT_BASE_PAD = 40;
    public static final int LEFT_INCREMENTAL_PAD = 50;
    public static final int LEFT_PAD = LEFT_BASE_PAD + LEFT_INCREMENTAL_PAD;
    public static final int TREE_LEVEL_PAD = 30;
    public static final int MAX_HEIGHT_TREE_BAR = 50;
    public static final int MAX_WIDTH = 9000;

    public static final int EDITOR_WINDOW_HEIGHT = 600;
    public static final int EDITOR_WINDOW_WIDTH = 1000;
    public static final int EDITOR_WINDOW_IMAGE = 500;
    public static final int RECONCILIATION_WINDOW_HEIGHT = 600;
    public static final int RECONCILIATION_WINDOW_WIDTH = 1200;
    //public static final int RECONCILIATION_WINDOW_IMAGE = 500;
    public static final int WARNING_WINDOW_HEIGHT = 200;
    public static final int WARNING_WINDOW_WIDTH = 500;
}
