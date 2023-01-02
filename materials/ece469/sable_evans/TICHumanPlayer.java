
/**
 * TICHumanPlayer.java
 *
 *  We are a human player.  We listen to squares on the board
 *  and make moves when clicks happen.  We have to coordinate
 *  those clicks though.  
 *
 * Created: Wed Apr 28 14:56:07 1999
 *
 * @author David Evans
 * @version
 */

import java.awt.Point;

public class TICHumanPlayer implements TICPlayer {
    private TICMove moveSoFar; //  The move we have computed so far				
    private boolean moveDone;

    public TICHumanPlayer() {
	moveSoFar = new TICMove();
	moveDone = false;
    }

    public void cleanUp() {
	//  Aaaaarrggg!  I'm melting....
    }
    
    public TICMove getMoveSoFar() {
    	return moveSoFar;
    }
    
    public void resetMoveSoFar() {
    	moveSoFar = new TICMove();
    }
    
    public void addToMoveSoFar(Point p) {
    	moveSoFar.addElement(p);
    }
    
    public void setMoveDone(boolean b) {
    	moveDone = b;
    }
    
    public TICMove chooseMove(TICState theGame) {
	moveDone = false;
	moveSoFar = new TICMove();  // Don't want to continue from last move...

	while (!moveDone) {
    			
	}
	//  The move has been done - print out that we did it.

	return moveSoFar;
    }
     
} // TICHumanPlayer
