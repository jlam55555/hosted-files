
/**
 * TITTitlePanel.java
 *
 *
 * Created: Sat Apr 24 14:47:52 1999
 *
 * @author David Evans
 * @version 0.1
 */

import java.awt.*;
import java.awt.event.*;
import java.lang.*;

public class TICTitlePanel extends Canvas implements Runnable {
    private Font textFont;
    private FontMetrics textFontMetrics;
    private boolean fontsSetup = false;
    private boolean scrolling = false;  // Not scrolling
    private int scrolly = 0;  // Where have we scrolled to
    private int currentIndex = 0;  // Current string index
    private String[] title = {"TIC-TAC-TOE", 
			      "TIC", "Isn't", "Checkers", "-",
			      "Tactical", "Analysis", "Coordinator", "-",
			      "To", "Obliterate", "Enemies",
			      "Wouldn't you prefer a nice game of Checkers?",
			      "It can even play on a 10\" by 10\" board!",
			      "TIC-TAC-TOE by",
			      "Carl Sable",
			      "Dave Evans",
			      "I scroll", "Therefore, I am"};

    public TICTitlePanel() {
	setBackground(Color.white);
	addMouseListener( new TitleML() );
	Dimension mySize = getSize();
	setSize(mySize.width, TICConstants.TITLE_PANEL_HEIGHT);
	scrolly = TICConstants.TITLE_PANEL_HEIGHT / 2;
    }

    public TICTitlePanel(String t) {
	setBackground(Color.white);
	this.title[0] = t;
	addMouseListener( new TitleML() );
	Dimension mySize = getSize();
	setSize(mySize.width, TICConstants.TITLE_PANEL_HEIGHT);
	scrolly = TICConstants.TITLE_PANEL_HEIGHT / 2;
    }

    public void setTitle(String t) {
	this.title[0] = t;
    }

    private void setFonts (Graphics g) {
	if (fontsSetup) return;
	textFont = new Font("System", Font.PLAIN, 14);
	textFontMetrics = g.getFontMetrics(textFont);
	fontsSetup = true;
    }
    
    public void paint(Graphics g) {
	// Set up the fonts if they have not been already
	setFonts(g);	
	// Find out where to draw the string so it is centered
	Dimension ourSize = getSize();
	int titleWidth = textFontMetrics.stringWidth(title[currentIndex]);
	int client_width = ourSize.width;
	int client_height = ourSize.height;
	int titleX = (client_width - titleWidth) / 2;
	int titleY = client_height - scrolly;
	
	g.drawString(title[currentIndex], titleX, titleY);
    }

    public void init() {

    }
    
    public void run() {
	//  Do our scrolling in here.
	//  First, change the scrolly value.  
	while (scrolling) {
	    scrolly++;  
	    //  If scrolly is more than our height, make it zero,
	    //  go to next string
	    if (scrolly > getSize().height) {
		scrolly = 0 - getSize().height;
		//  Now get the next string
		currentIndex++;
		if (currentIndex >= title.length) {
		    currentIndex = 0;
		}
	    }
	    //  Now repaint the panel
	    repaint();
	    //  Now sleep for a while
	    try {
		java.lang.Thread.sleep(100);
	    } catch (Exception uhoh) {
		//  Got some exception.  Don't do anything, we were 
		// just trying to sleep.
	    }
	}
    }

    public void toggleScroll() {
	if (scrolling == true) {
	    scrolling = false;
	} else {
	    scrolling = true;
	}
    }

    /**
     *  inner class TitleML a mouse listener for this pane. 
     *  if there is a click on us, start us scrolling!
     */
    class TitleML extends MouseAdapter {
	public TitleML() {
	    //  Not really anything to do.  
	}
	public void mouseClicked(MouseEvent e) {
	    //  They clicked the mouse.  Tell the panel to start
	    //  scrolling
	    ((TICTitlePanel) e.getComponent()).toggleScroll();
	    new Thread( TICTitlePanel.this ).start();
	}
    }

} // TICTitlePanel
