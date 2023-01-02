
/**
 * TICState.java
 *
 * The TICState is the representation of a board that we use
 * for the AI processing.  It is just an (8x8) array of ints.
 *
 * Created: Sun Apr 25 22:57:18 1999
 *
 * @author David Evans & Carl Sable
 * @version
 */

import java.util.*;
import java.awt.*;
import TICConstants;

public class TICState extends Object implements Cloneable {
    public int[][] board;
    private int color;
    private Vector legalMoves;
    private int numMoves;

    public TICState() {
	board = new int[TICConstants.ROWS][TICConstants.COLS];
	numMoves = 0;
	legalMoves = null;
	color = TICConstants.REDPLAYER;
    }

    public Object clone() {
	TICState newState = new TICState();
	int i, j;

	newState.color = color;
	newState.legalMoves = (Vector) legalMoves.clone();
	newState.numMoves = numMoves;
	for (i = 0; i < TICConstants.ROWS; i++) {
	    for (j = 0; j < TICConstants.COLS; j++) {
		newState.board[i][j] = board[i][j];
	    }
	}

	return (Object) newState;
    }

    public void initializeBoard() {
	int i;

	for (i = 0; i <= 6; i += 2) {
	    board[0][i] = TICConstants.EMPTY;
	    board[0][i+1] = TICConstants.RED;
	    board[1][i] = TICConstants.RED;
	    board[1][i+1] = TICConstants.EMPTY;
	    board[2][i] = TICConstants.EMPTY;
	    board[2][i+1] = TICConstants.RED;

	    board[3][i] = TICConstants.EMPTY;
	    board[3][i+1] = TICConstants.EMPTY;
	    board[4][i] = TICConstants.EMPTY;
	    board[4][i+1] = TICConstants.EMPTY;

	    board[5][i] = TICConstants.BLACK;
	    board[5][i+1] = TICConstants.EMPTY;
	    board[6][i] = TICConstants.EMPTY;
	    board[6][i+1] = TICConstants.BLACK;
	    board[7][i] = TICConstants.BLACK;
	    board[7][i+1] = TICConstants.EMPTY;
	}

	setColor(TICConstants.REDPLAYER);
    }

    public int getColor() {
	return color;
    }

    public void setColor(int c) {
	color = c;
    }
    
    public void createMoves() {
	Point[] redPieces = new Point[TICConstants.NUMPIECES];
	Point[] blackPieces = new Point[TICConstants.NUMPIECES];
	int numRedPieces = 0;
	int numBlackPieces = 0;
	int i, j;

	for (i = 0; i < TICConstants.ROWS; i++) {
	    for (j = 0; j < TICConstants.COLS; j++) {
		if ((board[i][j] == TICConstants.RED) ||
		    (board[i][j] == TICConstants.REDKING)) {

		    redPieces[numRedPieces] = new Point(i, j);

		    numRedPieces += 1;
		}

		if ((board[i][j] == TICConstants.BLACK) ||
		    (board[i][j] == TICConstants.BLACKKING)) {

		    blackPieces[numBlackPieces] = new Point(i, j);

		    numBlackPieces += 1;
		}
	    }
	}

	legalMoves = new Vector();
	numMoves = 0;

	if (color == TICConstants.REDPLAYER) {
	    for (i = 0; i < numRedPieces; i++) {
		addJumps(redPieces[i]);
	    }
	    if (numMoves == 0) {
		for (i = 0; i < numRedPieces; i++) {
		    addNormal(redPieces[i]);
		}
	    }
	}
	else if (color == TICConstants.BLACKPLAYER) {
	    for (i = 0; i < numBlackPieces; i++) {
		addJumps(blackPieces[i]);
	    }
	    if (numMoves == 0) {
		for (i = 0; i < numBlackPieces; i++) {
		    addNormal(blackPieces[i]);
		}
	    }
	}
    }

    /* Looks at the given piece on the current board and determines
       all legal jumps that this piece can make. */
    private void addJumps(Point piece) {
	/* movePossiblePaths is a vector of moves, not necessarily
	   legal.  They are starting paths of legal moves. */
	Vector movePossiblePaths = new Vector();
	/* alreadyJumpedLists is a vector of vectors.  Each path in 
	   movePossiblePaths has a corresponding vector here representing 
	   the pieces that have already been jumped in creating the path. */
	Vector alreadyJumpedLists = new Vector();

	int player = 0;
	if ((board[piece.x][piece.y] == TICConstants.RED)
	    || (board[piece.x][piece.y] == TICConstants.REDKING))
	    player = TICConstants.REDPLAYER;
	else if ((board[piece.x][piece.y] == TICConstants.BLACK)
		 || (board[piece.x][piece.y] == TICConstants.BLACKKING))
	    player = TICConstants.BLACKPLAYER;

	TICMove path = new TICMove(piece.x, piece.y);
	movePossiblePaths.addElement(path);
	alreadyJumpedLists.addElement(null);

	/* Record piece and make square empty in case loop of jumps
	   is possible. */
	int type = board[piece.x][piece.y];
	board[piece.x][piece.y] = TICConstants.EMPTY;

	TICMove movePossiblePath;
	Vector alreadyJumpedList;
	int current = 0;
	int numExpansions;
	while (current < movePossiblePaths.size()) {
	    movePossiblePath = (TICMove) movePossiblePaths.elementAt(current);
	    alreadyJumpedList = (Vector) alreadyJumpedLists.elementAt(current);

	    numExpansions = 0;
	    Point loc 
		= movePossiblePath.getElementAt(movePossiblePath.size()-1);

	    if ((player == TICConstants.REDPLAYER)
		&& (loc.x < TICConstants.ROWS - 2)
		&& (loc.y > 1)
		&& ((board[loc.x+1][loc.y-1] 
		     == TICConstants.BLACK)
		    || (board[loc.x+1][loc.y-1] 
			== TICConstants.BLACKKING))
		&& (!alreadyJumped(alreadyJumpedList, loc.x+1, loc.y-1))
		&& (board[loc.x+2][loc.y-2] == TICConstants.EMPTY)) {

		path = (TICMove) movePossiblePath.clone();

		Vector list;
		if (alreadyJumpedList == null)
		    list = new Vector();
		else
		    list = (Vector) alreadyJumpedList.clone();

		path.addElement(new Point(loc.x+2, loc.y-2));
		movePossiblePaths.addElement(path);
		list.addElement(new Point(loc.x+1, loc.y-1));
		alreadyJumpedLists.addElement(list);

		numExpansions += 1;    
	    }

	    if ((player == TICConstants.REDPLAYER)
		&& (loc.x < TICConstants.ROWS - 2)
		&& (loc.y < TICConstants.COLS - 2)
		&& ((board[loc.x+1][loc.y+1] 
		     == TICConstants.BLACK)
		    || (board[loc.x+1][loc.y+1] 
			== TICConstants.BLACKKING))
		&& (!alreadyJumped(alreadyJumpedList, loc.x+1, loc.y+1))
		&& (board[loc.x+2][loc.y+2] == TICConstants.EMPTY)) {

		path = (TICMove) movePossiblePath.clone();

		Vector list;
		if (alreadyJumpedList == null)
		    list = new Vector();
		else
		    list = (Vector) alreadyJumpedList.clone();

		path.addElement(new Point(loc.x+2, loc.y+2));
		movePossiblePaths.addElement(path);
		list.addElement(new Point(loc.x+1, loc.y+1));
		alreadyJumpedLists.addElement(list);

		numExpansions += 1;    
	    }

	    if ((type == TICConstants.REDKING)
		&& (loc.x > 1)
		&& (loc.y > 1)
		&& ((board[loc.x-1][loc.y-1] 
		     == TICConstants.BLACK)
		    || (board[loc.x-1][loc.y-1] 
			== TICConstants.BLACKKING))
		&& (!alreadyJumped(alreadyJumpedList, loc.x-1, loc.y-1))
		&& (board[loc.x-2][loc.y-2] == TICConstants.EMPTY)) {

		path = (TICMove) movePossiblePath.clone();

		Vector list;
		if (alreadyJumpedList == null)
		    list = new Vector();
		else
		    list = (Vector) alreadyJumpedList.clone();

		path.addElement(new Point(loc.x-2, loc.y-2));
		movePossiblePaths.addElement(path);
		list.addElement(new Point(loc.x-1, loc.y-1));
		alreadyJumpedLists.addElement(list);

		numExpansions += 1;    
	    }

	    if ((type == TICConstants.REDKING)
		&& (loc.x > 1)
		&& (loc.y < TICConstants.COLS - 2)
		&& ((board[loc.x-1][loc.y+1] 
		     == TICConstants.BLACK)
		    || (board[loc.x-1][loc.y+1] 
			== TICConstants.BLACKKING))
		&& (!alreadyJumped(alreadyJumpedList, loc.x-1, loc.y+1))
		&& (board[loc.x-2][loc.y+2] == TICConstants.EMPTY)) {

		path = (TICMove) movePossiblePath.clone();

		Vector list;
		if (alreadyJumpedList == null)
		    list = new Vector();
		else
		    list = (Vector) alreadyJumpedList.clone();

		path.addElement(new Point(loc.x-2, loc.y+2));
		movePossiblePaths.addElement(path);
		list.addElement(new Point(loc.x-1, loc.y+1));
		alreadyJumpedLists.addElement(list);

		numExpansions += 1;    
	    }

	    if ((type == TICConstants.BLACKKING)
		&& (loc.x < TICConstants.ROWS - 2)
		&& (loc.y > 1)
		&& ((board[loc.x+1][loc.y-1] 
		     == TICConstants.RED)
		    || (board[loc.x+1][loc.y-1] 
			== TICConstants.REDKING))
		&& (!alreadyJumped(alreadyJumpedList, loc.x+1, loc.y-1))
		&& (board[loc.x+2][loc.y-2] == TICConstants.EMPTY)) {

		path = (TICMove) movePossiblePath.clone();

		Vector list;
		if (alreadyJumpedList == null)
		    list = new Vector();
		else
		    list = (Vector) alreadyJumpedList.clone();

		path.addElement(new Point(loc.x+2, loc.y-2));
		movePossiblePaths.addElement(path);
		list.addElement(new Point(loc.x+1, loc.y-1));
		alreadyJumpedLists.addElement(list);

		numExpansions += 1;    
	    }

	    if ((type == TICConstants.BLACKKING)
		&& (loc.x < TICConstants.ROWS - 2)
		&& (loc.y < TICConstants.COLS - 2)
		&& ((board[loc.x+1][loc.y+1] 
		     == TICConstants.RED)
		    || (board[loc.x+1][loc.y+1] 
			== TICConstants.REDKING))
		&& (!alreadyJumped(alreadyJumpedList, loc.x+1, loc.y+1))
		&& (board[loc.x+2][loc.y+2] == TICConstants.EMPTY)) {

		path = (TICMove) movePossiblePath.clone();

		Vector list;
		if (alreadyJumpedList == null)
		    list = new Vector();
		else
		    list = (Vector) alreadyJumpedList.clone();

		path.addElement(new Point(loc.x+2, loc.y+2));
		movePossiblePaths.addElement(path);
		list.addElement(new Point(loc.x+1, loc.y+1));
		alreadyJumpedLists.addElement(list);

		numExpansions += 1;    
	    }

	    if ((player == TICConstants.BLACKPLAYER)
		&& (loc.x > 1)
		&& (loc.y > 1)
		&& ((board[loc.x-1][loc.y-1] 
		     == TICConstants.RED)
		    || (board[loc.x-1][loc.y-1] 
			== TICConstants.REDKING))
		&& (!alreadyJumped(alreadyJumpedList, loc.x-1, loc.y-1))
		&& (board[loc.x-2][loc.y-2] == TICConstants.EMPTY)) {

		path = (TICMove) movePossiblePath.clone();

		Vector list;
		if (alreadyJumpedList == null)
		    list = new Vector();
		else
		    list = (Vector) alreadyJumpedList.clone();

		path.addElement(new Point(loc.x-2, loc.y-2));
		movePossiblePaths.addElement(path);
		list.addElement(new Point(loc.x-1, loc.y-1));
		alreadyJumpedLists.addElement(list);

		numExpansions += 1;    
	    }

	    if ((player == TICConstants.BLACKPLAYER)
		&& (loc.x > 1)
		&& (loc.y < TICConstants.COLS - 2)
		&& ((board[loc.x-1][loc.y+1] 
		     == TICConstants.RED)
		    || (board[loc.x-1][loc.y+1] 
			== TICConstants.REDKING))
		&& (!alreadyJumped(alreadyJumpedList, loc.x-1, loc.y+1))
		&& (board[loc.x-2][loc.y+2] == TICConstants.EMPTY)) {

		path = (TICMove) movePossiblePath.clone();

		Vector list;
		if (alreadyJumpedList == null)
		    list = new Vector();
		else
		    list = (Vector) alreadyJumpedList.clone();

		path.addElement(new Point(loc.x-2, loc.y+2));
		movePossiblePaths.addElement(path);
		list.addElement(new Point(loc.x-1, loc.y+1));
		alreadyJumpedLists.addElement(list);

		numExpansions += 1;    
	    }

	    /* If we are not dealing with the first possible path
	       (i.e. we have more than starting point only) and we
	       could not expand further (i.e. are at end of jump)
	       then this represents a legal jump. */
	    if ((numExpansions == 0) && (current > 0)) {
		legalMoves.addElement(movePossiblePath);
		numMoves += 1;
	    }

	    current += 1;
	}

	/* Put back piece. */
	board[piece.x][piece.y] = type;
    }

    /* Determines if the point x,y is in the list, assumed to represent
       a list of locations already jumped for a given path */
    private boolean alreadyJumped(Vector list, int x, int y) {
	int i;
	Point p;

	if (list == null)
	    return false;

	for (i = 0; i < list.size(); i++) {
	    p = (Point) list.elementAt(i);

	    if ((p.x == x) && (p.y == y))
		return true;
	}

	return false;
    }

    /* Looks at the given piece on the current board and determines
       all legal non-jumps that this piece can make (assuming there
       are no jumps possible, in which case non-jumps are not allowed). */
    private void addNormal(Point piece) {
	int player = 0;
	if ((board[piece.x][piece.y] == TICConstants.RED)
	    || (board[piece.x][piece.y] == TICConstants.REDKING))
	    player = TICConstants.REDPLAYER;
	else if ((board[piece.x][piece.y] == TICConstants.BLACK)
		 || (board[piece.x][piece.y] == TICConstants.BLACKKING))
	    player = TICConstants.BLACKPLAYER;

	if ((player == TICConstants.REDPLAYER)
	    && (piece.x < TICConstants.ROWS - 1)
	    && (piece.y > 0)
	    && (board[piece.x+1][piece.y-1] == TICConstants.EMPTY)) {

	    TICMove path = new TICMove(piece.x, piece.y);

	    path.addElement(new Point(piece.x+1, piece.y-1));
	    legalMoves.addElement(path);
	    numMoves += 1;
	}

	if ((player == TICConstants.REDPLAYER)
	    && (piece.x < TICConstants.ROWS - 1)
	    && (piece.y < TICConstants.COLS - 1)
	    && (board[piece.x+1][piece.y+1] == TICConstants.EMPTY)) {

	    TICMove path = new TICMove(piece.x, piece.y);

	    path.addElement(new Point(piece.x+1, piece.y+1));
	    legalMoves.addElement(path);
	    numMoves += 1;
	}

	if ((board[piece.x][piece.y] == TICConstants.REDKING)
	    && (piece.x > 0)
	    && (piece.y > 0)
	    && (board[piece.x-1][piece.y-1] == TICConstants.EMPTY)) {

	    TICMove path = new TICMove(piece.x, piece.y);

	    path.addElement(new Point(piece.x-1, piece.y-1));
	    legalMoves.addElement(path);
	    numMoves += 1;
	}

	if ((board[piece.x][piece.y] == TICConstants.REDKING)
	    && (piece.x > 0)
	    && (piece.y < TICConstants.COLS - 1)
	    && (board[piece.x-1][piece.y+1] == TICConstants.EMPTY)) {

	    TICMove path = new TICMove(piece.x, piece.y);

	    path.addElement(new Point(piece.x-1, piece.y+1));
	    legalMoves.addElement(path);
	    numMoves += 1;
	}

	if ((board[piece.x][piece.y] == TICConstants.BLACKKING)
	    && (piece.x < TICConstants.ROWS - 1)
	    && (piece.y > 0)
	    && (board[piece.x+1][piece.y-1] == TICConstants.EMPTY)) {

	    TICMove path = new TICMove(piece.x, piece.y);

	    path.addElement(new Point(piece.x+1, piece.y-1));
	    legalMoves.addElement(path);
	    numMoves += 1;
	}

	if ((board[piece.x][piece.y] == TICConstants.BLACKKING)
	    && (piece.x < TICConstants.ROWS - 1)
	    && (piece.y < TICConstants.COLS - 1)
	    && (board[piece.x+1][piece.y+1] == TICConstants.EMPTY)) {

	    TICMove path = new TICMove(piece.x, piece.y);

	    path.addElement(new Point(piece.x+1, piece.y+1));
	    legalMoves.addElement(path);
	    numMoves += 1;
	}

	if ((player == TICConstants.BLACKPLAYER)
	    && (piece.x > 0)
	    && (piece.y > 0)
	    && (board[piece.x-1][piece.y-1] == TICConstants.EMPTY)) {

	    TICMove path = new TICMove(piece.x, piece.y);

	    path.addElement(new Point(piece.x-1, piece.y-1));
	    legalMoves.addElement(path);
	    numMoves += 1;
	}

	if ((player == TICConstants.BLACKPLAYER)
	    && (piece.x > 0)
	    && (piece.y < TICConstants.COLS - 1)
	    && (board[piece.x-1][piece.y+1] == TICConstants.EMPTY)) {

	    TICMove path = new TICMove(piece.x, piece.y);

	    path.addElement(new Point(piece.x-1, piece.y+1));
	    legalMoves.addElement(path);
	    numMoves += 1;
	}
    }

    /* Return the list of legal moves calculated by CreateMoves */
    public Vector getMoves() {
	return legalMoves;
    }

    /* Return the number of legal moves found by CreateMoves */
    public int countMoves() {
	return numMoves;
    }

    /* Debug routine to print out the legal moves */
    public void printMoves() {
	int i;
	TICMove move;

	for (i = 0; i < numMoves; i++) {
	    move = (TICMove) legalMoves.elementAt(i);
	    System.out.print("Legal move # " + i + ": ");
	    move.printMove();
	}
    }

    /* Starting from state, make move, and return resulting state. */
    public TICState makeMove(TICMove move) {
	TICState stateNew;
	Point point, pointLast;
	int iPoint;
	int pieceMoved = 0;

	stateNew = (TICState) this.clone();

	for (iPoint = 0; iPoint < move.size(); iPoint++) {
	    point = move.getElementAt(iPoint);
	    if (iPoint == 0) {
		pieceMoved = stateNew.board[point.x][point.y];
		stateNew.board[point.x][point.y] = TICConstants.EMPTY;
	    }
	    else {
		pointLast = move.getElementAt(iPoint-1);
		if (((pointLast.x == point.x+2) 
		     && (pointLast.y == point.y-2))
		    || ((pointLast.x == point.x+2)
			&& (pointLast.y == point.y+2))
		    || ((pointLast.x == point.x-2)
			&& (pointLast.y == point.y-2))
		    || ((pointLast.x == point.x-2)
			&& (pointLast.y == point.y+2))) {

		    int jumpedX = (pointLast.x + point.x) / 2;
		    int jumpedY = (pointLast.y + point.y) / 2;
		    stateNew.board[jumpedX][jumpedY] = TICConstants.EMPTY;
		}

		if (iPoint == move.size() - 1) {
		    int pieceFinal;

		    if ((color == TICConstants.REDPLAYER)
			&& (pieceMoved == TICConstants.RED)
			&& (point.x == 7))
			pieceFinal = TICConstants.REDKING;
		    else if ((color == TICConstants.BLACKPLAYER)
			     && (pieceMoved == TICConstants.BLACK)
			     && (point.x == 0))
			pieceFinal = TICConstants.BLACKKING;
		    else
			pieceFinal = pieceMoved;

		    stateNew.board[point.x][point.y] = pieceFinal;
		}
	    }
	}

	if (color == TICConstants.REDPLAYER)
	    stateNew.color = TICConstants.BLACKPLAYER;
	else
	    stateNew.color = TICConstants.REDPLAYER;

	return stateNew;
    }

    public String toString() {
	int i, j;
	String stateString = new String(); 

        for (i = 0; i < TICConstants.ROWS; i++) {
	    for (j = 0; j < TICConstants.COLS; j++) {
		stateString = stateString + "----";
	    }
	    stateString = stateString + "-\n";
	    for (j = 0; j < TICConstants.COLS; j++) {
		if (board[i][j] == TICConstants.RED)
		    stateString = stateString + "| r ";
		else if (board[i][j] == TICConstants.REDKING)
		    stateString = stateString + "| R ";
		else if (board[i][j] == TICConstants.BLACK)
		    stateString = stateString + "| b ";
		else if (board[i][j] == TICConstants.BLACKKING)
		    stateString = stateString + "| B ";
		else if (board[i][j] == TICConstants.EMPTY)
		    stateString = stateString + "|   ";
		else
		    stateString = stateString + "|BUG";
	    }
	    stateString = stateString + "|\n";
	}
	for (j = 0; j < TICConstants.COLS; j++) {
	    stateString = stateString + "----";
	}
	stateString = stateString + "-\n";

	if (color == TICConstants.REDPLAYER)
	    stateString = stateString + "Red's turn.\n";
	else if (color == TICConstants.BLACKPLAYER)
	    stateString = stateString + "Black's turn.\n";
	else
	    stateString = stateString + "Bug! Neither player has turn.\n";
	return stateString;
    }

    public void printState() {
	int i, j;

	System.out.println("Current state:");
        for (i = 0; i < TICConstants.ROWS; i++) {
	    for (j = 0; j < TICConstants.COLS; j++) {
		System.out.print("----");
	    }
	    System.out.println("-");
	    for (j = 0; j < TICConstants.COLS; j++) {
		if (board[i][j] == TICConstants.RED)
		    System.out.print("| r ");
		else if (board[i][j] == TICConstants.REDKING)
		    System.out.print("| R ");
		else if (board[i][j] == TICConstants.BLACK)
		    System.out.print("| b ");
		else if (board[i][j] == TICConstants.BLACKKING)
		    System.out.print("| B ");
		else if (board[i][j] == TICConstants.EMPTY)
		    System.out.print("|   ");
		else
		    System.out.print("|BUG");
	    }
	    System.out.println("|");
	}
	for (j = 0; j < TICConstants.COLS; j++) {
	    System.out.print("----");
	}
	System.out.println("-");

	if (color == TICConstants.REDPLAYER)
	    System.out.println("Red's turn.");
	else if (color == TICConstants.BLACKPLAYER)
	    System.out.println("Black's turn.");
	else
	    System.out.println("Bug! Neither player has turn.");
    }

    /**
     *  onLegalPath(TICMove toCheck) checks to see if the move proposed in toCheck would
     *  make the state into a state that is a legal state -- ie it is either a legal move
     *  or it is a state that is an intermediate state for some legal move.
     */
    public boolean onLegalPath(TICMove toCheck) {
	//  Do we need to createMoves() here?  I don't think so
	Vector moves = (Vector) getMoves().clone();
	//  We want to check for each point of toCheck that it is part of some legal move
	for (int i=0; i<toCheck.size(); i++) {
	    //  Check to see if this point of the move is also a point in other moves
	    //  Remove the moves that do not have this point at this sequence. 
	    for (int movesIndex=0; movesIndex<moves.size(); movesIndex++) {
		TICMove currentMove = (TICMove) moves.elementAt(movesIndex);
		if (currentMove != null) {
		    //  The move is not null, so it is still a candidate. 
		    //  If it is null, it was removed previously.
		    if (((Point) toCheck.getElementAt(i)).x == ((Point)currentMove.getElementAt(i)).x &&
			((Point) toCheck.getElementAt(i)).y == ((Point)currentMove.getElementAt(i)).y) {
			//  The point matches, we are ok  -- do nothing
		    } else {
			//  The point does not match, we need to remove this move from
			//  the list of candidate moves we are matching to
			//  Can not really remove it since we don't want to change size of
			//  the vector as we go.  Make it null.  compact the vector later.
			moves.setElementAt(null,movesIndex);
		    }
		}
	    }
	}
	//  Count all non-null elements
	int count=0;
	for (int i=0; i<moves.size(); i++) {
	    if (moves.elementAt(i) != null) {		
		count++;
	    }
	}
	//  Rather, we have counted all of the non-null moves.  
	//  We have removed all moves from moves that do not have all the points
	//  from toCheck on it.  If there are any moves left, they have all of 
	//  toCheck's points on it, and therefor toCheck is either a legal move or
	//  it is an intermediate state in a legal move.
	if (count > 0) {
	    return true;
	}
	return false;
    }
	
    /**
     *  legalMove(TICMove toCheck) returns a boolean value that indicates whether or not
     *  the given move is a legal move or not.
     */
    public boolean legalMove(TICMove toCheck) {
	Vector moves = getMoves();
	//  We want to check to see if toCheck equals some move or not
	for (int i=0; i<moves.size(); i++) {
	    if ( ((TICMove) moves.elementAt(i)).equals( toCheck )) {
		return true;
	    }
	}
	return false;
    }
} // TICState
