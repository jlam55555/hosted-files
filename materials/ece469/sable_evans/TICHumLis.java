
	
/**
 * TICHumLis.java
 *
 *  A class that listens to squares for the human player and informs it of
 *  clicks and stuff.  
 *
 * Created: Wed Apr 28 14:56:07 1999
 *
 * @author David Evans
 * @version
 */
 
import java.awt.event.*;
import java.awt.*;
import java.awt.Point;
 
public class TICHumLis extends MouseAdapter {
    private TICHumanPlayer player;  /*  A handle on the player we are 
					associated with
				    */    
    private TICGame game;  /*  A handle on the game */
    								 
    public TICHumLis(TICHumanPlayer p, TICGame g) {
	player = p;
	game = g;
    }						 
    							 
    public void mouseClicked(MouseEvent e) {
	TICSquarePanel square = (TICSquarePanel) e.getComponent();
	TICMessagePanel mp = square.getBoardPanel().getMessagePanel();

	//  We want to check to see if this click is for us    		
	if (game.whoseTurn() == player) {
	    //  It is our turn!  Yeah!
	    //  Check to see if this would be a complete legal move
	    TICMove msfCopy = (TICMove) player.getMoveSoFar().clone();
	    msfCopy.addElement( square.getPoint() );
	    if (game.getState().legalMove(msfCopy)) {
    		//  This is a legal move.  Let's do it.
    		//  Remove any selections from the board
		TICMove msf = player.getMoveSoFar();
		TICBoardPanel bp = square.getBoardPanel();
		Point current;
		for (int i=0; i<msf.size(); i++) {
		    current = msf.getElementAt(i);
		    bp.board[current.x][current.y].setBackgroundState(TICConstants.NORMAL);
		    bp.board[current.x][current.y].repaint();
		}
		//  Add the square just picked that completed the move
		player.addToMoveSoFar( square.getPoint() );
		//  Tell the player it has a good move
		player.setMoveDone( true );
		return;
	    }
	    //  Check to see if this proposed move is on a legal path legal
	    if (game.getState().onLegalPath(msfCopy)) {
 		//  Check to see if we have selected an initial square or not
		if (player.getMoveSoFar().size() == 0) {
		    // Check to see if this square starts a legal move
		    // Useful code snippet for selection below
		    // Change the state of the square to selected
		    square.setBackgroundState(TICConstants.SELECTED);
			
		    // Unselect the previous square
		    TICSquarePanel prev = square.getBoardPanel().getSelectedSquare();
		    if (prev != null && prev != e.getComponent() &&
			prev.getBackgroundState() != TICConstants.POSSIBLE_DESTINATION_ON &&
			prev.getBackgroundState() != TICConstants.POSSIBLE_DESTINATION_OFF) {
			prev.setBackgroundState(TICConstants.NORMAL);
			prev.repaint();
		    }
		    // Tell the board that WE are selected
		    square.getBoardPanel().setSelectedSquare( (TICSquarePanel) e.getComponent());
		    mp.print("Selected initial square.");
		} else { 
		    //  We are not the first square, nor a complete move square (done above)
		    //  We want to make the background of this sucker flash
		    square.setBackgroundState(TICConstants.PIECE_CAN_MOVE_ON);
		    mp.print("Selected intermediate square.");
		} // Not a first square
		//  In any case, this is a legal move, so let's add it to the
		//  moveSoFar
		player.addToMoveSoFar( square.getPoint() );
	    } else {
		//  not a legal move
		mp.print("Not a legal move.");
		//  Remove any selections from the board
		TICMove msf = player.getMoveSoFar();
		if (msf != null) {
		    TICBoardPanel bp = square.getBoardPanel();
		    Point current;
		    for (int i=0; i<msf.size(); i++) {
			current = msf.getElementAt(i);
			bp.board[current.x][current.y].setBackgroundState(TICConstants.NORMAL);
			bp.board[current.x][current.y].repaint();
		    }
		}
		//  Clear out the moveSoFar in the player.
		player.resetMoveSoFar();
		mp.print("Please select again");
	    }
	} // our turn 
    }  // mouse click
}
