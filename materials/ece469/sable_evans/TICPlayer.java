
/**
 * TICPlayer.java
 *
 *  An interface that all players must implement.  Contains only 
 *  one function: getMove.  getMove will return a TICMove given 
 *  a TICState.  
 * 
 *  Also have a cleanUp() function, called when the player is 
 *  not going to be used any more.
 *
 * Created: Sun Apr 25 22:52:00 1999
 *
 * @author David Evans
 * @version
 */

import TICMove;
import TICState;

public interface TICPlayer  {
    public TICMove chooseMove(TICState b);
    public void cleanUp();
} // TICPlayer
