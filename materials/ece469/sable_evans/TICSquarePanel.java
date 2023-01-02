
/**
 * TICSquarePanel.java
 *
 *
 * Created: Sat Apr 24 15:46:25 1999
 *
 * @author David Evans
 * @version
 */

import java.awt.*;
import java.lang.*;
import java.awt.event.*;
import TICConstants;

public class TICSquarePanel extends Panel {
    // Variables 
    private int color;
    private int row;
    private int col;
    private boolean drawLabels = false; 
    private int state = TICConstants.EMPTY;
    private int background = TICConstants.NORMAL;
    private TICMessagePanel messagePanel;
    private TICBoardPanel bp; // a pointer to the board that contains us

    public TICSquarePanel() {
	color = TICConstants.LIGHT;
	row = -1;
	col = -1;
    }

    public TICSquarePanel(int co, int r, int c, TICMessagePanel mp,
			  TICBoardPanel board) {
	color = co;
	row = r;
	col = c;
	bp = board;

	// Register the mouse listener inner class on us
	addMouseListener(new TICSqML());
		
	messagePanel = mp; 
    }

    public TICBoardPanel getBP() {
	return bp;
    }

    public void setState(int s) {
	state = s;
	//  Our state has changed: we should alert the game
	// of that in case we are cheating or debugging.
	if (getBP().getGame() != null) {
	    getBP().getGame().updateState();
	}
    }

	public TICBoardPanel getBoardPanel() {
		return bp;
	}

    public int getState() {
	return state;
    }
    
    public Point getPoint() {
    	return new Point(row, col);
    }

    public void setBackgroundState(int b) {
	background = b;
    }

    public int getBackgroundState() {
	return background;
    }

    public String pieceName(int p) {
	if (p == TICConstants.RED) {
	    return "Red piece";
	} else if (p == TICConstants.REDKING) {
	    return "Red king";
	} else if (p == TICConstants.BLACK) {
	    return "Black piece";
	} else if (p == TICConstants.BLACKKING) {
	    return "Black king";
	}

	return ""+state;
    }
    
    public void paint(Graphics g) {
	int checkerWidth, checkerHeight, x, y;

	if (color == TICConstants.LIGHT) {
	    g.setColor(Color.lightGray);
	} else {
	    g.setColor(Color.white);
	}
	
	// Check to see if we should flash something
	if (background == TICConstants.POSSIBLE_DESTINATION_ON ||
		background == TICConstants.PIECE_CAN_MOVE_ON) {
		g.setColor(Color.pink);	
	}
	// Check to see if we are highlighted.  If so, make color
	// something else.  Yellow.
	if (background == TICConstants.HIGHLIGHTED) {
	    g.setColor(Color.yellow);
	} else if (background == TICConstants.SELECTED) {
	    g.setColor(Color.magenta);
	}

	Dimension d = getSize();
	g.fillRect(0, 0, d.width, d.height);

	// Draw a checker on the board if there is one.
	// We will use a normal circle for a normal checker, 
	// a 3-d one for a king.  For now.
	if (state == TICConstants.BLACK || 
	    state == TICConstants.BLACKKING ||
	    state == TICConstants.RED ||
	    state == TICConstants.REDKING) {
	    if (state == TICConstants.BLACK || 
		state == TICConstants.BLACKKING) {
		// Are we a black thing or a red thing?
		g.setColor(Color.black);
	    } else {
		// Must be red
		g.setColor(Color.red);
	    }
	    // How big should the checker be?  
	    // We will say 80% of the max radius
	    Double tmp = new Double(d.width * 0.8);
	    checkerWidth = tmp.intValue();
	    tmp = new Double(d.height * 0.8);
	    checkerHeight = tmp.intValue();
	    // Where should we place the checker?  20% in...
	    x = (d.width - checkerWidth) / 2;
	    y = (d.height - checkerHeight) / 2;
	    if (state == TICConstants.BLACK || 
		state == TICConstants.RED) {		
		// We must be a normal piece of some kind
		g.fillOval(x, y, checkerWidth, checkerHeight);
	    } else {
		// We must be a king of some kind
		// Make the circle, then put a white circle inside it
		g.fillOval(x, y, checkerWidth, checkerHeight);
		g.setColor(Color.white);
		//  Make a white circle inside that is 1/3 the radius
		checkerWidth = checkerWidth / 3;
		checkerHeight = checkerHeight / 3;
		x = (d.width - checkerWidth) / 2;
		y = (d.height - checkerHeight) / 2;
		g.fillOval(x, y, checkerWidth, checkerHeight);
	    }
	}

	if (drawLabels == true) {
	    // Some debugging stuff here.  Put coords on the board.
	    g.setColor(Color.blue);	
	    g.drawString(""+row+","+col+","+state, 1, d.height - 5);
	}
    }
    
    /**
     * Inner class to implement a mouse listener interface
     */
    class TICSqML extends java.awt.event.MouseAdapter {
	TICSquarePanel us;
	public void mouseClicked(MouseEvent e) {
	    // Check to see with board what it is that we are 
	    // supposed to be doing right now.
	    us = (TICSquarePanel) e.getComponent();
	    int action = getBP().getClickFunction();
	    if (getBP().getDebugging() == true) {
		if (action == TICConstants.BLACK ||
		    action == TICConstants.BLACKKING ||
		    action == TICConstants.RED ||
		    action == TICConstants.REDKING ||
		    action == TICConstants.NORMAL) {
		    //  We should place a piece, or clear one out
		    if (action == TICConstants.NORMAL) {
			action = TICConstants.EMPTY;
		    }
		    setState(action);
		}
		if (action == TICConstants.SELECTED ||
		    action == TICConstants.HIGHLIGHTED ||
		    action == TICConstants.PIECE_CAN_MOVE_ON ||
		    action == TICConstants.POSSIBLE_DESTINATION_ON ||
		    action == TICConstants.NORMAL) {
		    // Make the background something.
		    setBackgroundState(action);
		}
	    } 

	    us.repaint();
	    e.consume();
	}
	public void mouseEntered(MouseEvent e) {
	    // Set the color to highlighted if normal
	    us = (TICSquarePanel) e.getComponent();
	    if (us.getBackgroundState() == TICConstants.NORMAL) {
		us.setBackgroundState(TICConstants.HIGHLIGHTED);
		// Re-paint the square
		us.repaint();
	    }
	    e.consume();
	}
	public void mouseExited(MouseEvent e) {
	    // Set the color back to normal -- only if it is 
	    // highlighted.  It could be selected, which we don't
	    // want to change...
	    us = (TICSquarePanel) e.getComponent();
	    if (us.getBackgroundState() == TICConstants.HIGHLIGHTED) {
		us.setBackgroundState(TICConstants.NORMAL);
	    }
	    us.repaint();
	    e.consume();
	}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}       
    }
    
} // TICSquarePanel
