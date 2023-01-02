
/**
 * TICMove.java
 *
 *  TICMove represents a single "move" for the checkers game.  
 *  It is essentially a vector of coordinates, the first of which
 *  is the location of the starting piece.  If there is only one other 
 *  element, it is the location moved to - it could be a jump!  If 
 *  there are subsequent elements, they were reached through jumps. 
 * 
 *  The elements in the vector are java.awt.Point objects.  No need
 *  to re-write everything.
 *
 * Created: Sun Apr 25 22:33:00 1999
 *
 * @author David Evans and Carl Sable
 * @version
 */

import java.util.*;
import java.awt.*;

public class TICMove extends Object implements Cloneable {
    private Vector locations;
 
    /**
     *  Creates an empty vector with no moves in it.
     */
    public TICMove() {
	locations = new Vector();
    }

    public Object clone() {
	TICMove newMove = new TICMove();

	newMove.locations = (Vector) locations.clone();

	return (Object) newMove;
    }

    /** 
     *  Creates a Move that has a starting location of row, col
     */
    public TICMove(int row, int col) {
	locations = new Vector();
	locations.addElement(new Point(row, col));
    }
    
    /**
     *  Returns the number of locations in this move
     */
    public int size() {
	return locations.size();
    }

    /**
     *  Set the ith element
     */
    public void setElementAt(Point p, int i) {
	locations.setElementAt(p, i);
    }

    /** 
     *  Get the ith element
     */
    public Point getElementAt(int i) {
	return (Point) locations.elementAt(i);
    }

    /** 
     *  Add an element to the vector of locations at the end
     */
    public void addElement(Point p) {
	locations.addElement(p);
    }

    public void printMove() {
	int i;
	Point p;

	for (i = 0; i < locations.size(); i++) {
	    p = (Point) locations.elementAt(i);
	    if (i == 0)
		System.out.print("Move from (" + p.x + ", " + p.y + ")");
	    else
		System.out.print(" to (" + p.x + ", " + p.y + ")");
	}
	System.out.println("");
    }
    
    public boolean equals(TICMove check) {
    	if (check.size() != locations.size()) {
	    return false;
    	} 
    	for (int i=0; i<locations.size(); i++) {
	    if (((Point) locations.elementAt(i)).x ==  ((Point) check.getElementAt(i)).x &&
		((Point) locations.elementAt(i)).y == ((Point) check.getElementAt(i)).y ) {
		// The x and y coord match, do nothing.
	    } else {
		// X and Y do not match, return false
		return false;
	    }
    	}
    	return true;
    }

    public String toString() {
	Point p;
	String us = new String();
	for (int i=0; i<locations.size(); i++) {
	    p = (Point) locations.elementAt(i);
	    us = us + "["+p.x+","+p.y+"] ";
	}
	return us;
    }

} // TICMove
