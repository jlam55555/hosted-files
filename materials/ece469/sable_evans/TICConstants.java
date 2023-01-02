
/**
 * TICConstants.java
 *
 * All of the constants that we use are in this class.
 *
 * Created: Sun Apr 25 23:18:41 1999
 *
 * @author David Evans
 * @version
 */

public class TICConstants extends Object {
    // Constants (originally from TICSquarePanel.java)
    public static final int LIGHT = 200;
    public static final int DARK = 201;
    public static final int EMPTY = 300;
    public static final int RED = 301;
    public static final int BLACK = 302;
    public static final int REDKING = 303;
    public static final int BLACKKING = 304;
    public static final int NORMAL = 400;
    public static final int HIGHLIGHTED = 401;
    public static final int SELECTED = 402;
    public static final int PIECE_CAN_MOVE_ON = 403;
    public static final int PIECE_CAN_MOVE_OFF = 404;
    public static final int POSSIBLE_DESTINATION_ON = 405;
    public static final int POSSIBLE_DESTINATION_OFF = 406;
    // Constants (originally from TICBoardPanel.java)
    public static final int ROWS = 8;
    public static final int COLS = 8;
    //  Other constants
    public static final int NUMPIECES = 12;
    public static final int REDPLAYER = 500;
    public static final int BLACKPLAYER = 501;
    //  Constants for the minimax search
    public static final int INFINITY = java.lang.Integer.MAX_VALUE;
    public static final int NEGATIVE_INFINITY = java.lang.Integer.MIN_VALUE;
    //  Constants for Computer Player options
    public static final int TIME_SEARCH_DEFAULT = 10;
    public static final int TIME_SEARCH_MAX = 600;
    public static final int TIME_SEARCH_MIN = 5;
    public static final int DEPTH_SEARCH_DEFAULT = 6;
    public static final int DRAW_TIME_DEFAULT = 30;
    public static final int DEPTH_HINT = 4;
    public static final int DEPTH_SEARCH_MAX = 20;
    public static final int DEPTH_SEARCH_MIN = 1;
    public static final int DEPTH_START_ITERATIVE = 4;
    //  Constant to say how big of a TICTitlePanel to have
    public static final int TITLE_PANEL_HEIGHT = 32;
    /**
     *  An empty constructor that does nothing.  Like the class.
     */

    public TICConstants() {	
    }
    
} // TICConstants

