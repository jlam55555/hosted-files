
/**
 * TICGame.java
 *
 * This class actually does the game playing.  It contains the two 
 * players, the BoardPanel, and a TICState that represents the current
 * state of the game (and is mirrored on the display TICBoardPanel.)
 * The TICGame class does the control flow of the game, knows when the
 * game is over, who won, and so on.
 *
 * Created: Tue Apr 27 11:16:05 1999
 *
 * @author David Evans
 * @version
 */

import java.util.*;

public class TICGame implements Runnable {
    private TICBoardPanel boardPanel;
    private TICMessagePanel message;
    private TICState gameState;
    private TICPlayer redPlayer;
    private TICPlayer blackPlayer;
    private TICControl controller;
    private Thread gameThread; 
    private Vector gameHistory;
    private TICDisplay soundMaker;
    private boolean boardDirtyBit = false;  /*  This is true if we have
						given a hint and should
						clean up the flashing
						squares on the board on 
						the next turn
					    */
    private boolean firstUndo = true;  /* If this is true, make undo
					  go back two moves: the first
					  undo has to delete two states,
					  subsequent ones only 1 (since
					  they have not added the state to
					  the history through the playGame
					  function that adds the state to
					  the history as the first thing
					  it does
				       */
    /**
     *  TICGame constructor.  We require a TICBoardPanel to be
     *  passed in because we keep that inside of us, and play the 
     *  game on that board.  We want a reference to the message panel
     *  so we know where to say stuff.  
     * 
     *  We also want two players, but we will create those.  They will
     *  be either human or computer, depending on what TICControl says
     *  we should make.  Then we will make those. 
     *
     *  We also have a TICState, but that is local, and we know how it 
     *  starts out: in the initial checkers game state.  
     */
    public TICGame(TICBoardPanel b, TICMessagePanel mp, 
		   TICDisplay td) {
	soundMaker = td;
	boardPanel = b;
	message = mp;
	gameState = new TICState();
	//  Set the board up for the initial state 
	gameState.initializeBoard();
	
	//  Have the TICBoardPanel display the current state
	boardPanel.displayState(gameState);
	
	/*  Have to set up TICControl so that it has a panel to choose 
	    who plays what colors.  Then we have to query it, and find
	    out what players to make.  Then we have to make those 
	    players.  Then set their colors.  
	*/
	
	//  The two players are created at playGame();	
    }

    /** 
     *  debugMode() go to debug mode
     */
    public void debugMode() {
		boardPanel.setDebugging(true);
    }

    public void playClip(int i) {
	soundMaker.playClip(i);
    }

    /**
     * normalMode() go to normal mode
     */
    public void normalMode() {
		boardPanel.setDebugging(false);
    }

    /**
     *  pauseGame() pause the game in progress
     */
    public void pauseGame() {
	gameThread.suspend();
    }

    /**
     * resumeGame() resumes the game in progress
     */
    public void resumeGame() {
	gameThread.resume();
    }
    
    /**
     *  resetGame(TICController t) resets the game.
     *  If there is a game in progress, it stops it.  Then resets
     *  the board to the initial state, and waits.
     */
    public void resetGame(TICControl t) {
	if (gameThread != null) {
	    gameThread.stop();
	    gameThread = null;
	}
	// reset the board state
	gameState.initializeBoard();
	boardPanel.displayState(gameState);

	//  Tell the players they are dead.  
	redPlayer.cleanUp();
	blackPlayer.cleanUp();

	//  Reset the history
	gameHistory = null;
	
	//  Done.  Just wait now.	
    }

	/**
	 *  registerHumanListener(TICHumLis l) tells the board to register l as the
	 *  listener for the human for each square of the board 
	 */
	public void registerHumanListener(TICHumLis l) {
		boardPanel.registerHumanListener(l);
	}

    /**
     * playGame(TICController t) plays the game.
     * 
     *  We take the reference t to the TICController so that we can 
     *  grab options from the game options card.  
     *
     *  The steps in playing a game:  We already have a board in an
     *  initial state (well, maybe - we might have modded it via 
     *  debug mode) and two computer players.  We have to check and
     *  create the players (for now we make two computer players in 
     *  the constructor for testing purposes - might not do that later)
     *  then alternate turns between them.
     */

    public void playGame(TICControl t) {
	controller = t;  // Set up our controller

	//  If we have a red or a black player, tell them to clean up
	if (redPlayer != null) {
	    redPlayer.cleanUp();
	    redPlayer = null;
	}
	if (blackPlayer != null) {
	    blackPlayer.cleanUp();
	    blackPlayer = null;
	}
	
	//  If there is a game thread running already, kill it
	if (gameThread != null) {
	    gameThread.stop();
	    gameThread = null;
	}

	//  Create a new history of pieces
	gameHistory = new Vector();

	//  Create the players.  Check to see who is what.
	redPlayer = controller.createNewPlayer(TICConstants.REDPLAYER);
	blackPlayer = controller.createNewPlayer(TICConstants.BLACKPLAYER);
	
	//  We now have the two players of the correct type.  Let 
	//  them go at it.  Red starts first because the initial state
	//  defaults to that.
	if (gameState.getColor() == TICConstants.REDPLAYER) {
	    message.print("Red moves first.");
	} else {
	    message.print("Black moves first.");
	}

	/* We have to make a thread to play the game in the
	   background while we process clicks and stuff - otherwise
	   we don't get any time to display the board or anything!!

	   All of the game playing stuff is in run().  
	*/
	
	gameThread = new Thread( this );
	gameThread.start();

    }
    
    public void run() {
	
	/* Lets play while the game is not over.
	   The game is over when there are no moves for a player
	   when it is that player's turn.  countMoves() will return 0
	*/
	boolean gameOver = false;
	TICMove theMove; 

	while (!gameOver) {
	    if (gameState.getColor() == TICConstants.REDPLAYER) {
		message.print("Red's turn.");
	    } else {
		message.print("Black's turn.");
	    }
	    //  Check to see if the game is over
	    //  Create the moves
	    gameState.createMoves();
	    
	    //  Insert this state into the history
	    gameHistory.addElement( gameState );

	    //  Check to see that we have some move
	    if (gameState.countMoves() == 0) {
		gameOver = true;
		message.print("Game Over man, game over.");
		if ((redPlayer instanceof TICComputerPlayer &&
		    blackPlayer instanceof TICHumanPlayer) ||
		    (blackPlayer instanceof TICComputerPlayer &&
		     redPlayer instanceof TICHumanPlayer)) {
		    //  We have a human and a computer.
		    //  Make sure the winner is human
		    if (whoseTurn() instanceof TICComputerPlayer) {
			//  Not bad for a human
			soundMaker.playClip(3);
		    } else {
			//  Game over man, game over
			soundMaker.playClip(4);
		    }
		} else {
		    //  Game over man, game over
		    soundMaker.playClip(4);
		}
		break;
	    }
	    //  Some move exists.  So, let the player choose.
	    if (gameState.getColor() == TICConstants.REDPLAYER) {
		theMove = redPlayer.chooseMove( gameState );
	    } else {
		theMove = blackPlayer.chooseMove( gameState );
	    }

	    //  Need to apply the move to the gameState
	    gameState = gameState.makeMove( theMove );
	    //  Update the display of the boardPanel
	    boardPanel.displayState( gameState );
	    //  Check to see if the game ended.
	    gameState.createMoves();	    
	    if (gameState.countMoves() == 0) {
		gameOver = true;
		message.print("Game Over man, game over.");
		if ((redPlayer instanceof TICComputerPlayer &&
		    blackPlayer instanceof TICHumanPlayer) ||
		    (blackPlayer instanceof TICComputerPlayer &&
		     redPlayer instanceof TICHumanPlayer)) {
		    //  We have a human and a computer.
		    //  Make sure the winner is human
		    if (whoseTurn() instanceof TICComputerPlayer) {
			//  Not bad for a human
			soundMaker.playClip(3);
		    } else {
			//  Game over man, game over
			soundMaker.playClip(4);
		    }
		} else {
		    //  Game over man, game over
		    soundMaker.playClip(4);
		}
	    }	    
	}
	/*  done running */
    }

    /**
     * gameHistoryLen() return the length of the game history 
     * vector
     */ 
    public int gameHistoryLen() {
	if (gameHistory == null) {
	    return 0;
	}
	return gameHistory.size();
    }

    /**
     *  goToTurn(int t) go to turn t in the history
     */
    public void goToTurn(int t) {
	gameState = (TICState) gameHistory.elementAt(t);
	//  Now delete all of the elements greater than this
	//  state -- unless this is state 0, in which case we
	//  had better leave it in there!
	System.out.println("Going to turn "+ t+" with " + gameHistory.size() + "turns");
	System.out.println("States: "+gameHistory);
	while (gameHistory.size() > t) {
	    System.out.println("Deleted state "+ (gameHistory.size() - 1));
	    gameHistory.removeElementAt( gameHistory.size() - 1);
	}
	System.out.println("Displaying state "+t);
	
	boardPanel.displayState(gameState);
	//  stop the current game thread if it is running
	if (gameThread != null) {
	    gameThread.stop();
	    gameThread = null;
	} 
	//  create a new game from this point on 
	gameThread = new Thread( this );

    }

    public void continueGame() {
	playClip(6);
	if (gameThread == null) {
	    playGame(controller);
	} else {
	    gameThread.start();
	}

	//  Reset the firstUndo value
	firstUndo = true;
    }

    /**
     *  getBoard() returns the current board associated with 
     *  this game
     */
    public TICBoardPanel getBoard() {
	return boardPanel;
    }

    /**
     * getState() returns the current state associated with 
     * this game.  Note that this will get the state from 
     * the board that is displayed ON THE SCREEN!  Thus, it 
     * will not contain the turn information correctly.  Unless
     * something changes in the future.
     */
    public TICState getState() {
	return gameState;
    }

    /**
     *  chooseMove() will choose a move from a computer player.
     *  This is for testing only right now.
     */
    public void chooseMove() {
	TICMove choice = redPlayer.chooseMove(gameState);
	if (choice == null) {
	    message.print("No move was found");
	} else {
	    choice.printMove();
	}
    }

    /**
     *  updateState() is a function that looks at the current state
     *  of the BoardPanel and makes that the board setup the board
     *  setup of the current board.  This is used for cheating / debugging:
     *  whenever a square is changed through the debugging interface,
     *  the square tells the game to update it's board to reflect
     *  the change.  
     *
     *  For human players, we will have a different UI that only 
     *  allows them to make legal moves. 
     */
    public void updateState() {
	int saveOurColor = gameState.getColor();
	gameState = boardPanel.generateState();
	gameState.setColor(saveOurColor);	
    }

    /**
     *  whoseTurn() looks at the gameState member, determines which
     *  player is playing right now, and returns a reference to that
     *  player.  It is used in this manner: when there is a click on 
     *  the board, all players (all human players?) have registered a
     *  mouse listener on each square, so the players receive an event
     *  on their listener.  We need to determine if it is that players
     *  turn or not, and can do so with whoseTurn().  If it is that
     *  players turn, the player can act accordingly.  
     */
    public TICPlayer whoseTurn() {
	if (gameState.getColor() == TICConstants.REDPLAYER) {
	    // It is Red's turn.  
	    return redPlayer;
	} else {
	    // It must be Black's turn
	    return blackPlayer;
	}
    }
    
    public TICPlayer getBlackPlayer() {
	return blackPlayer;
    }

    public TICPlayer getRedPlayer() {
	return redPlayer;
    }

    public void setTurn(int t) {
	gameState.setColor(t);
    }

    public void setBoardDirtyBit(boolean d) {
	boardDirtyBit = d;
    }

    public boolean getBoardDirtyBit() {
	return boardDirtyBit;
    }
    
    public boolean getFirstUndo() {
	return firstUndo;
    }

    public void setFirstUndo(boolean fu) {
	firstUndo = fu;
    }

} // TICGame
