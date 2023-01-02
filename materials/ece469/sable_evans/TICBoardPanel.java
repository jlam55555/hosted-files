
/**
 * TICBoardPanel.java
 *
 *
 * Created: Sat Apr 24 15:27:26 1999
 *
 * @author David Evans
 * @version
 */

import java.awt.*;
import TICSquarePanel;
import TICConstants;
import TICState;
import TICGame;

public class TICBoardPanel extends Panel {
    private TICMessagePanel messagePanel;
    private TICSquarePanel selectedSquare;
    private TICDrawThread flasher; 
    private TICGame game; //  The game that we are controlled by
    /**
     *  What should we do when a square is clicked on?
     */
    private int clickingFunction = TICConstants.NORMAL;
    private boolean debugging = false;  //  If true, we are debugging.  False, human is playing.
    public TICSquarePanel[][] board = new TICSquarePanel[TICConstants.ROWS][TICConstants.COLS];

    public TICBoardPanel(TICMessagePanel mp) {
	messagePanel = mp;
	// Set this panel up for a row x col board
	setLayout(new GridLayout(TICConstants.ROWS, TICConstants.COLS));	

	// Set the size up.  What size are we?
	Dimension prefSize = getPreferredSize();
	setSize(prefSize);
	Dimension mySize = getSize();

	boolean light = false;
	for (int i=0; i<TICConstants.ROWS; i++) {
	    for (int j=0; j<TICConstants.COLS; j++) {
		// Create the squares, alternating light and dark
		if (light == true) {
		    board[i][j] = new TICSquarePanel(TICConstants.LIGHT, 
						     i, j, messagePanel,
						     this);
		} else {
		    board[i][j] = new TICSquarePanel(TICConstants.DARK,
						     i, j, messagePanel,
						     this);
		}
		//  Set the size of the square
		board[i][j].setSize(mySize.width / TICConstants.COLS, 
				    mySize.height / TICConstants.ROWS);
		//  Set a name for the square
		board[i][j].setName("["+i+","+j+"]");
		//  add the square to the board		
		add(board[i][j]);
		if (j == TICConstants.COLS - 1) {
		    // We are at the end of the row, want to flip value 
		    // of light so that we alternate colors in next row.
		    if (light == false) {
			light = true;
		    } else {
			light = false;
		    }
		}
		// Switch the color of the next square
		if (light == false) {
		    light = true;
		} else {
		    light = false;
		}
	    }
	}
	//  Done initializing the array of board pieces. 
	//  Create the board drawing (flasher) thread
	flasher = new TICDrawThread();
	flasher.start();
    }

	public boolean getDebugging() {
		return debugging; 
	}

	public void setDebugging(boolean d) {
		debugging = d;
	}

	public void registerHumanListener(TICHumLis hl) {
		//  We have to register this listener on every square
		for (int i=0; i<TICConstants.ROWS; i++) {
			for (int j=0; j<TICConstants.COLS; j++) {
				board[i][j].addMouseListener( hl );
			}
		}
		//  It has been done. 
	}

    public void setSelectedSquare(TICSquarePanel p) {
	selectedSquare = p;
    }

    public TICSquarePanel getSelectedSquare() {
	return selectedSquare;
    }

    public void paint(Graphics g) {
	// To paint the board: Paint each of the pieces
	for (int i=0; i<TICConstants.ROWS; i++) {
	    for (int j=0; j<TICConstants.COLS; j++) {
		board[i][j].paint(g);
		g.drawString(""+i+","+j, 50,50);
	    }
	}
    }
    
    public TICMessagePanel getMessagePanel() {
     	return messagePanel;
    }

    /**
     *  Set what happens when we click on a square.  The int f 
     *  is a constant from TICConstants that is one of the square
     *  related ones (ie, red, black, redking, etc.)  When clicked,
     *  a square will do that.  If it is normal, it will clear out
     *  all the flags on the square.
     */
    public void setClickFunction(int f) {
	clickingFunction = f;
    }

    public int getClickFunction() {
	return clickingFunction;
    }

    /** 
     *  generateState() will generate a TICState representation
     *  of the board that is currently being displayed on the screen
     */
    TICState generateState() {
	TICState state = new TICState();

	for (int i=0; i<TICConstants.ROWS; i++) {
	    for (int j=0; j<TICConstants.COLS; j++) {
		state.board[i][j] = board[i][j].getState();
	    }
	}

	return state;
    }
    
    /**
     *  TICGame returns the game object that controls this board
     */
    public TICGame getGame() {
	return game;
    }
    
    public void setGame(TICGame g) {
	game = g;
    }
    
    /**
     *  displayState(TICState state) will display the board and state
     *  represented by the TICState state parameter
     *
     *  If the game.getBoardDirtyBit == true, then we will display
     *  all of the squares and make their backgrounds NORMAL - this  is 
     *  clean out the flashing squares from hints.  Also might clean up
     *  the trouble with going back turns.  
     */

    void displayState(TICState state) {
	boolean repaint = false;
	for (int i=0; i<TICConstants.ROWS; i++) {
	    for (int j=0; j<TICConstants.COLS; j++) {
		
		if (board[i][j].getState() != state.board[i][j]) {
		    repaint = true;
		}
		board[i][j].setState(state.board[i][j]);
		if (game == null || game.getBoardDirtyBit()) {
		    repaint = true;
		    board[i][j].setBackgroundState(TICConstants.NORMAL);
		}
		if (repaint) {
		    board[i][j].repaint();
		}
		repaint = false;
	    }
	}	
	
	//  Clears out the dirty bit
	if (game != null) {
	    game.setBoardDirtyBit( false );
	}

    }

    /**
     *  TICDrawThread inner class that checks the board and takes care
     *  of any pieces that have backgrounds that should be flashing.  Those
     *  are PIECE_CAN_MOVE and POSSIBLE_DESTINATION.  The state is toggled from
     *  the on and the off states, and the piece is told to refresh itself every 
     *  time the state is toggled.  This thread will look at all the squares and
     *  check to see if they need to be drawn or not.
     */
    class TICDrawThread extends Thread {
    	private long sleepTime = 500;  // How many milliseconds to sleep
    	private boolean running = true;  // True while we should run

    	public void run() {
	    int i, j, bstate;
	    while (running) {
		try {
    		    // messagePanel.print("draw thread going to sleep");
		    sleep(sleepTime);  // Sleep for a bit
		} catch (InterruptedException e) {
		    // I don't know what to do when I am interrupted.  
		    // I will do nothing.
		}
		// Now check to see what squares we need to change
		for (i=0; i<TICConstants.ROWS; i++) {
		    for (j=0; j<TICConstants.COLS; j++) {
			bstate = board[i][j].getBackgroundState();
			if (bstate == TICConstants.PIECE_CAN_MOVE_ON ||
			    bstate == TICConstants.PIECE_CAN_MOVE_OFF ||
			    bstate == TICConstants.POSSIBLE_DESTINATION_ON ||
			    bstate == TICConstants.POSSIBLE_DESTINATION_OFF) {
			    if (bstate == TICConstants.PIECE_CAN_MOVE_ON) {
				board[i][j].setBackgroundState(TICConstants.PIECE_CAN_MOVE_OFF);
			    } else if (bstate == TICConstants.PIECE_CAN_MOVE_OFF) {
				board[i][j].setBackgroundState(TICConstants.PIECE_CAN_MOVE_ON);
			    } else if (bstate == TICConstants.POSSIBLE_DESTINATION_ON) {
				board[i][j].setBackgroundState(TICConstants.POSSIBLE_DESTINATION_OFF);
			    } else if (bstate == TICConstants.POSSIBLE_DESTINATION_OFF) {
				board[i][j].setBackgroundState(TICConstants.POSSIBLE_DESTINATION_ON);
			    }
			    // Now repaint the square
			    board[i][j].repaint();
			}
		    }
		}    
	    }
    	}
    	
    	public void stopRunning() {
	    // Set our running variable to false.  
	    // Don't know if we will ever do this. 
	    running = false;	
    	}
    }  // End of TICDrawThread inner class
    
} // TICBoardPanel
