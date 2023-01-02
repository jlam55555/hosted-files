
/**
 * TICComputerPlayer.java
 *
 *
 * Created: Mon Apr 26 22:54:41 1999
 *
 * @author David Evans and Carl Sable
 * @version
 */

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import TICConstants;

public class TICComputerPlayer implements TICPlayer {
    private TICMove moveChoice = null;
    private TICMove moveFinal = null;
    private int scoreFinal = 0;
    private int secStart = 0;
    private int secMid = 0;
    private int secStop = 0;
    private int timeLimit = TICConstants.TIME_SEARCH_DEFAULT;
    private int drawTimeLimit = TICConstants.DRAW_TIME_DEFAULT;
    private boolean reachedLimit = false;
    private boolean doRandomize = true;
    private Random r = new Random();
    private TICCpOptsFr options;

    public TICComputerPlayer() {
	options = new TICCpOptsFr( true );
    }

    public TICComputerPlayer(boolean showOpts) {
	options = new TICCpOptsFr( showOpts );
    }

    public void setOptionsTitle(String title) {
	options.setTitle(title);
    }

    /* Choose the best move based on our search to a given depth. */
    public TICMove chooseMove(TICState state, int depth) {
	int result;
	int secs;
	Date d;

	/* If only one legal move, just take it! */
	state.createMoves();
	if (state.countMoves() == 1)
	    return ((TICMove) state.getMoves().elementAt(0));

	d = new Date();
	secs = d.getHours()*60*60 + d.getMinutes() * 60 + d.getSeconds();
	secStart = secs;
	secMid = secStop = -1;

	result = searchAlphaBeta(state, 
				 TICConstants.NEGATIVE_INFINITY, 
				 TICConstants.INFINITY, 
				 depth);

	d = new Date();
	secs = d.getHours()*60*60 + d.getMinutes() * 60 + d.getSeconds();
	System.out.println("After depth " + depth + " took time " + (secs - secStart));
	System.out.println("The score right now is " + result);

	return moveChoice;
    }

    /* Offer a hint to the player. */
    public TICMove getHint(TICState state) {
	return chooseMove(state, TICConstants.DEPTH_HINT);
    }

    /* Choose the best move based on our search. */
    public TICMove chooseMove(TICState state) {
	int result = 0;
	Date d;
	int secs;
	int depth = TICConstants.DEPTH_START_ITERATIVE;

	/* If only one legal move, just take it! */
	state.createMoves();
	if (state.countMoves() == 1) {
	    System.out.println("Making a forced move, no evaluation.");
	    return ((TICMove) state.getMoves().elementAt(0));
	}

	/*  Else there are lots of moves.  First, decide if we 
	    are searching to a depth, or searching for a time
	*/
	if (options.getSearchType().equals("Depth")) {
	    return chooseMove(state, options.getDepthLimit() );
	}

	/*  Else do it by time */

	d = new Date();
	secs = d.getHours()*60*60 + d.getMinutes() * 60 + d.getSeconds();
	secStart = secs;
	secMid = secs + getTimeLimit() / 2;
	secStop = secs + getTimeLimit() - 1;

	scoreFinal = 0;
	while ((secs >= secStart) && (secs <= secMid)) {
	    reachedLimit = false;
	    result = searchAlphaBeta(state, 
				     TICConstants.NEGATIVE_INFINITY, 
				     TICConstants.INFINITY, 
				     depth);
	    if (!reachedLimit 
		|| (depth == TICConstants.DEPTH_START_ITERATIVE)) {
		moveFinal = moveChoice;
		scoreFinal = result;
	    }
	    if (reachedLimit) {
		System.out.println("Did not complete depth " + depth);
	    }

	    d = new Date();
	    secs = d.getHours()*60*60 + d.getMinutes() * 60 + d.getSeconds();
	    System.out.println("After depth " + depth + " took time " + (secs - secStart));

	    depth += 1;
	}

	System.out.println("Total time to choose move = " + (secs - secStart));
	System.out.println("The score right now is " + scoreFinal);

	return moveFinal;
    }

    /* Considers accepting a draw (offered to given color). */
    public boolean considerDraw(TICState state, int color) {
	int result = 0;
	Date d;
	int secs;
	int depth = 4;
	int i, j;
	int numRedNormals = 0;
	int numBlackNormals = 0;
	int numRedKings = 0;
	int numBlackKings = 0;
	int redCloseFactor = 0;
	int blackCloseFactor = 0;
	int redBackRow = 0;
	int blackBackRow = 0;
	int redKingFactor = 0;
	int blackKingFactor = 0;

	d = new Date();
	secs = d.getHours()*60*60 + d.getMinutes() * 60 + d.getSeconds();
	secStart = secs;
	secMid = secs + getDrawTimeLimit() / 2;
	secStop = secs + getDrawTimeLimit() - 1;

	scoreFinal = 0;
	while ((secs >= secStart) && (secs <= secMid)) {
	    reachedLimit = false;
	    result = searchAlphaBeta(state, 
				     TICConstants.NEGATIVE_INFINITY, 
				     TICConstants.INFINITY, 
				     depth);
	    if (!reachedLimit) {
		scoreFinal = result;
	    }
	    else {
		System.out.println("Did not complete depth " + depth);
	    }

	    d = new Date();
	    secs = d.getHours()*60*60 + d.getMinutes() * 60 + d.getSeconds();
	    System.out.println("After depth " + depth + " took time " + (secs - secStart));

	    depth += 1;
	}

	System.out.println("Total time to do search = " + (secs - secStart));
	System.out.println("The score right now is " + scoreFinal);

	for (i = 0; i < TICConstants.ROWS; i++) {
	    for (j = 0; j < TICConstants.COLS; j++) {
		if (state.board[i][j] == TICConstants.RED) {
		    numRedNormals += 1;
		    redCloseFactor += i;
		    if (i == 0)
			redBackRow += 1;
		}
		if (state.board[i][j] == TICConstants.BLACK) {
		    numBlackNormals += 1;
		    blackCloseFactor += TICConstants.ROWS - i - 1;
		    if (i == TICConstants.ROWS - 1)
			blackBackRow += 1;
		}
		if (state.board[i][j] == TICConstants.REDKING) {
		    numRedKings += 1;
		    redKingFactor += 7 - Math.abs(i - j);
		}
		if (state.board[i][j] == TICConstants.BLACKKING) {
		    numBlackKings += 1;
		    blackKingFactor += 7 - Math.abs(i - j); 
		}
	    }
	}

	if (color == TICConstants.REDPLAYER) {
	    if (scoreFinal < -29000000)
		return true;
	    else if ((scoreFinal < -10000000)
		     && (numRedNormals + numRedKings
			 <= numBlackNormals + numBlackKings))
		return true;
	    else if ((scoreFinal < 150000)
		     && (numRedNormals + numRedKings
			 <= numBlackNormals + numBlackKings)
		     && (numRedNormals + numRedKings <= 3)
		     && (redKingFactor - blackKingFactor
			 < (numRedNormals + numRedKings) * 2 - 1))
		return true;
	    else if ((scoreFinal < 10000000)
		     && (numRedNormals + numRedKings == 1))
		return true;
	    else return false;
	}
	else {
	    if (scoreFinal > 29000000)
		return true;
	    else if ((scoreFinal > 10000000)
		     && (numRedNormals + numRedKings
			 >= numBlackNormals + numBlackKings))
		return true;
	    else if ((scoreFinal > -150000)
		     && (numRedNormals + numRedKings
			 >= numBlackNormals + numBlackKings)
		     && (numBlackNormals + numBlackKings <= 3)
		     && (blackKingFactor - redKingFactor
			 < (numBlackNormals + numBlackKings) * 2 - 1))
		return true;
	    else if ((scoreFinal > -10000000)
		     && (numBlackNormals + numBlackKings == 1))
		return true;
	    else return false;
	}

    }

    public void setTimeLimit(int t) {
	timeLimit = t;
    }

    public int getTimeLimit() {
	timeLimit = options.getTimeLimit();
	return timeLimit;
    }

    public void setDrawTimeLimit(int t) {
	drawTimeLimit = t;
    }

    public int getDrawTimeLimit() {
	return drawTimeLimit;
    }

    /* Our heurisitc.  See comments below. */
    private int evaluateBoard(TICState state) {
	int score = 0;
	int numRedNormals = 0;
	int numBlackNormals = 0;
	int numRedKings = 0;
	int numBlackKings = 0;
	int redCloseFactor = 0;
	int blackCloseFactor = 0;
	int redBackRow = 0;
	int blackBackRow = 0;
	int redKingFactor = 0;
	int blackKingFactor = 0;
	int pieceCheck, pieceEnemy, playerCheck, playerEnemy, sign;
	int posCheck, posOther, posF11, posF12, posF21, posF22, posF31, posF32;
	int i, j;

	for (i = 0; i < TICConstants.ROWS; i++) {
	    for (j = 0; j < TICConstants.COLS; j++) {
		if (state.board[i][j] == TICConstants.RED) {
		    numRedNormals += 1;
		    redCloseFactor += i;
		    if (i == 0)
			redBackRow += 1;
		}
		if (state.board[i][j] == TICConstants.BLACK) {
		    numBlackNormals += 1;
		    blackCloseFactor += TICConstants.ROWS - i - 1;
		    if (i == TICConstants.ROWS - 1)
			blackBackRow += 1;
		}
		if (state.board[i][j] == TICConstants.REDKING) {
		    numRedKings += 1;
		    redKingFactor += 7 - Math.abs(i - j);
		}
		if (state.board[i][j] == TICConstants.BLACKKING) {
		    numBlackKings += 1;
		    blackKingFactor += 7 - Math.abs(i - j); 
		}
	    }
	}

	if (numRedNormals + numRedKings == 0) {
	    score = TICConstants.NEGATIVE_INFINITY + 10;
	    if (getRandomize())
		score -= Math.abs(r.nextInt()) % 10;
	    return score;
	}
	else if (numBlackNormals + numBlackKings == 0) {
	    score = TICConstants.INFINITY - 10;
	    if (getRandomize())
		score += Math.abs(r.nextInt()) % 10;
	    return score;
	}

	/* Each factor below is checked in order of importance.
	   Each factor only matters if all above factors are equal. */

	/* Kings worth 5, normal pieces worth 3! */
	score = 5 * (numRedKings - numBlackKings);
	score += 3 * (numRedNormals - numBlackNormals);

	/* Normal pieces closer to being kings are better... */
	/* ...but also nice to protect the back row. */
	score *= 100;
	score += (redCloseFactor - blackCloseFactor);
	if (redBackRow == 4)
	    score += 8;
	else if ((redBackRow == 3) && (numBlackNormals > 1))
	    score += 3;
	if (blackBackRow == 4)
	    score -= 8;
	else if ((blackBackRow == 3) && (numRedNormals > 1))
	    score -= 3;

	/* Player ahead likes to trade. */
	score *= 100;
	if (score > 0) {
	    /* Red is ahead */
	    score -= (5 * numRedKings + 3 * numRedNormals);
	}
	else if (score < 0) {
	    /* Black is ahead */
	    score += (5 * numBlackKings + 3 * numBlackNormals);
	}
	if ((numRedKings + numRedNormals >= 3)
	    && (numBlackKings + numBlackNormals == 1))
	    score += 5;
	else if ((numRedKings + numRedNormals == 2)
		 && (numBlackKings + numBlackNormals == 1))
	    score += 2;
	else if ((numRedKings + numRedNormals >= 4)
		 && (numBlackKings + numBlackNormals == 2))
	    score += 3;
	else if ((numRedKings + numRedNormals == 3)
		 && (numBlackKings + numBlackNormals == 2))
	    score += 1;
	if ((numBlackKings + numBlackNormals >= 3)
	    && (numRedKings + numRedNormals == 1))
	    score -= 5;
	else if ((numBlackKings + numBlackNormals == 2)
		 && (numRedKings + numRedNormals == 1))
	    score -= 2;
	else if ((numBlackKings + numBlackNormals >= 4)
		 && (numRedKings + numRedNormals == 2))
	    score -= 3;
	else if ((numBlackKings + numBlackNormals == 3)
		 && (numRedKings + numRedNormals == 2))
	    score -= 1;

	/* Kings like to be in "better" diagonal, especially corners. */
	score *= 100;
	score += (redKingFactor - blackKingFactor);
	for (i = 0; i < 8; i++) {
	    if (i % 2 == 0) {
		pieceCheck = TICConstants.REDKING;
		pieceEnemy = TICConstants.BLACKKING;
		playerCheck = TICConstants.REDPLAYER;
		playerEnemy = TICConstants.BLACKPLAYER;
		sign = +1;
	    }
	    else {
		pieceCheck = TICConstants.BLACKKING;
		pieceEnemy = TICConstants.REDKING;
		playerCheck = TICConstants.BLACKPLAYER;
		playerEnemy = TICConstants.BLACKPLAYER;
		sign = -1;
	    }
	    if (i < 2) {
		posCheck = state.board[0][1];
		posOther = state.board[1][0];
		posF11 = state.board[1][2];
		posF12 = state.board[2][3];
		posF21 = state.board[2][1];
		posF22 = state.board[3][2];
		posF31 = state.board[3][4];
		posF32 = state.board[4][3];
	    }
	    else if (i < 4) {
		posCheck = state.board[1][0];
		posOther = state.board[0][1];
		posF11 = state.board[2][1];
		posF12 = state.board[3][2];
		posF21 = state.board[1][2];
		posF22 = state.board[2][3];
		posF31 = state.board[3][4];
		posF32 = state.board[4][3];
	    }
	    else if (i < 6) {
		posCheck = state.board[7][6];
		posOther = state.board[6][7];
		posF11 = state.board[6][5];
		posF12 = state.board[5][4];
		posF21 = state.board[5][6];
		posF22 = state.board[4][5];
		posF31 = state.board[4][3];
		posF32 = state.board[3][4];
	    }
	    else {
		posCheck = state.board[6][7];
		posOther = state.board[7][6];
		posF11 = state.board[5][6];
		posF12 = state.board[4][5];
		posF21 = state.board[6][5];
		posF22 = state.board[5][4];
		posF31 = state.board[4][3];
		posF32 = state.board[3][4];
	    }

	    if ((posCheck == pieceCheck)
		&& (posOther == TICConstants.EMPTY)
		&& (((playerCheck == TICConstants.REDPLAYER)
		     && (score <= 0)) ||
		    ((playerCheck == TICConstants.BLACKPLAYER)
		     && (score >= 0)))) {
		score += 25 * sign;
		if (((state.getColor() == playerCheck)
		     && (posF11 == pieceEnemy)
		     && (posF12 == pieceEnemy))
		    || ((state.getColor() == playerEnemy)
			&& (posF21 == pieceEnemy)
			&& (posF22 == pieceEnemy)))
		    score -= 22 * sign;
		else {
		    if (posF11 == pieceEnemy)
			score -= 5 * sign;
		    if (posF12 == pieceEnemy)
			score -= 5 * sign;
		    if (posF21 == pieceEnemy)
			score -= 5 * sign;
		    if (posF22 == pieceEnemy)
			score -= 5 * sign;
		    if (posF31 == pieceEnemy)
			score -= 2 * sign;
		    if (posF32 == pieceEnemy)
			score -= 2 * sign;
		}
	    }
	}

	if (getRandomize()) {
	    /* Last  digit will be random, way to choose between
	       moves of equal evaluation! */
	    score *= 10;
	    if (score >= 0)
		score += Math.abs(r.nextInt()) % 10;
	    else
		score -= Math.abs(r.nextInt()) % 10;
	}

	return score;
    }

    public void setRandomize(boolean f) {
	doRandomize = f;
    }

    public boolean getRandomize() {
	return doRandomize;
    }

    /* MiniMax search with alpha-beta pruning */
    private int searchAlphaBeta(TICState state, 
				int alpha, 
				int beta, 
				int depth) {
	Date d;
	int secs;

	if (secStop > 0) {
	    d = new Date();
	    secs = d.getHours()*60*60 + d.getMinutes() * 60 + d.getSeconds();
	    if (secs >= secStop) {
		/* Our time limit is up! */
		reachedLimit = true;
		return 0;
	    }
	}

	if (depth == 0) {
	    moveChoice = null;
	    return evaluateBoard(state);
	}
	else {
	    Vector legalMoves;
	    int numMoves;
	    int color;
	    int i;
	    TICMove moveTmp = null;

	    color = state.getColor();

	    state.createMoves();
	    legalMoves = state.getMoves();
	    numMoves = state.countMoves();

	    if (numMoves == 0) {
		int score = 0;

		if (color == TICConstants.REDPLAYER) {
		    moveChoice = null;
		    score = TICConstants.NEGATIVE_INFINITY + 10;
		    if (getRandomize())
			score -= Math.abs(r.nextInt()) % 10;
		    return score;
		}
		else {
		    moveChoice = null;
		    score = TICConstants.INFINITY - 10;
		    if (getRandomize())
			score += Math.abs(r.nextInt()) % 10;
		    return score;

		}
	    }

	    TICState stateTmp;
	    TICMove moveCur;
	    int result;
	    for (i = 0; i < numMoves; i++) {
		moveCur = (TICMove) legalMoves.elementAt(i);
		stateTmp = state.makeMove(moveCur);

		/*
		  System.out.println("depth = " + depth +", possible move:");
		  moveCur.printMove();
		  System.out.println("Resulting state is:");
		  stateTmp.printState();
		  int tmpRes = evaluateBoard(stateTmp);
		  System.out.println("Heuristic says " + tmpRes);
		*/

		result = searchAlphaBeta(stateTmp,
					 alpha,
					 beta,
					 depth-1);

		if (color == TICConstants.REDPLAYER) {
		    if (result > alpha) {
			moveTmp = moveCur;
			alpha = result;
		    }
		    if (alpha >= beta) {
			moveChoice = moveTmp;
			return beta;
		    }
		}
		else {
		    if (result < beta) {
			moveTmp = moveCur;
			beta = result;
		    }
		    if (beta <= alpha) {
			moveChoice = moveTmp;
			return alpha;
		    }
		}
	    }

	    moveChoice = moveTmp;
	    if (color == TICConstants.REDPLAYER)
		return alpha;
	    else
		return beta;
	}
    }
    
    /**
     *  We are being shut down.  Kill our options window
     */
    public void cleanUp() {
	options.dispose();
    }

    /**
     *  inner class TICCpOptsFr is a class that pops up a frame 
     *  for setting options for this computer player.  
     */
    public class TICCpOptsFr extends Frame {
	private int WIDTH = 300;
	private int HEIGHT = 200;
	Choice searchLimit;
	TextField depthLimit;
	TextField timeLimit;

	public TICCpOptsFr(boolean vis) {
	    super("Computer Player Options");
	    
	    //  Make it resizeable
	    setResizable(true);
	    setSize(WIDTH, HEIGHT);
	    
	    //  Set the layout to be a grid layout
	    setLayout( new GridLayout( 4, 2 ) );

	    //  We need a text input for time
	    Label timeLabel = new Label("Seconds to search for");
	    timeLimit = new TextField("10", 3);
	    add(timeLabel);
	    add(timeLimit);

	    //  We need a text input for depth
	    Label depthLabel = new Label("Depth to search to");
	    depthLimit = new TextField("7", 2);
	    add(depthLabel);
	    add(depthLimit);

	    //  We need a choice to choose from time or depth
	    Label searchLabel = new Label("Limit search by");
	    searchLimit = new Choice();
	    searchLimit.add("Time");
	    searchLimit.add("Depth");	    
	    add(searchLabel);
	    add(searchLimit);

	    //  Add a listener so that we can die
	    addWindowListener( new TICOFDie() );

	    setVisible( vis );
	}

	int getTimeLimit() {
	    //  Get the time limit.
	    //  Check the text of the box.  If it is nonsensical,
	    //  reset to default.  If it is < min or > max, reset.
	    String timeText = timeLimit.getText();
	    int time;
	    try {
		time = Integer.parseInt(timeText);
	    } catch (NumberFormatException e) {
		time = TICConstants.TIME_SEARCH_DEFAULT;
	    }
	    if (time < TICConstants.TIME_SEARCH_MIN) {
		time = TICConstants.TIME_SEARCH_MIN;
	    }else if (time > TICConstants.TIME_SEARCH_MAX) {
		time = TICConstants.TIME_SEARCH_MAX;
	    }
	    timeLimit.setText(""+time);
	    
	    return time;
	}

	int getDepthLimit() {
	    //  Get the depth limit.
	    //  Check the text of the box.  If it is nonsensical,
	    //  reset to default.  If it is < min or > max, reset.
	    String depthText = depthLimit.getText();
	    int depth;
	    try {
		depth = Integer.parseInt(depthText);
	    } catch (NumberFormatException e) {
		depth = TICConstants.DEPTH_SEARCH_DEFAULT;
	    }
	    if (depth < TICConstants.DEPTH_SEARCH_MIN) {
		depth = TICConstants.DEPTH_SEARCH_MIN;
	    }else if (depth > TICConstants.DEPTH_SEARCH_MAX) {
		depth = TICConstants.DEPTH_SEARCH_MAX;
	    }
	    depthLimit.setText(""+depth);
	    
	    return depth;	    
	}

	String getSearchType() {
	    return searchLimit.getSelectedItem();
	}
	
	/**
	 *  Inner class TICOFDie
	 *  A class to listen for the close window event and do it.
	 */
	class TICOFDie implements java.awt.event.WindowListener {
	    public TICOFDie() {
		
	    }
	    
	    public void windowClosing(WindowEvent e) {
		//  Kill this window
		TICCpOptsFr.this.dispose();
	    }
	    
	    public void windowActivated(WindowEvent e) {}
	    public void windowClosed(WindowEvent e) {}
	    public void windowDeactivated(WindowEvent e) {}
	    public void windowDeiconified(WindowEvent e) {}
	    public void windowIconified(WindowEvent e) {}
	    public void windowOpened(WindowEvent e) {}
	}  //  The TICOFDie inner class        

    }  //  The TICOptsFr inner class

} // TICComputerPlayer
