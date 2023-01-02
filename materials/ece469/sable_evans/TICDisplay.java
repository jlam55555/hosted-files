/**
 * TICDisplay.java
 *
 *
 * Created: Sat Apr 24 13:36:12 1999
 *
 * @author David Evans
 * @version 0.1 
 */

import java.awt.*;
import java.applet.*;
import TICTitlePanel;
import TICBoardPanel;
import TICMessagePanel;
import TICControl;
import TICGame;
import java.net.*;

public class TICDisplay extends Applet {
    TICTitlePanel myTitle;
    TICBoardPanel myBoardPanel;
    TICMessagePanel myMessagePanel;
    TICControl controller;
    TICGame myGame;
    AudioClip playGame;
    AudioClip strangeGame;
    AudioClip greeting;
    AudioClip notBadHuman;
    AudioClip gameOver;
    AudioClip synthetic;
    AudioClip finishGame;

    public TICDisplay() {
	
    }
    
    public void init() {
	// Set up the layout to be a border layout first
	setLayout(new BorderLayout());

	myMessagePanel = new TICMessagePanel();
	add(myMessagePanel, "South");
	// Set the size of the message panel	
	myMessagePanel.print("TIC Initialized.");

	myBoardPanel = new TICBoardPanel(myMessagePanel);
	add(myBoardPanel, "Center");

	//  Add the title panel
	myTitle = new TICTitlePanel();
	add(myTitle, "North");

	// Create the game
	myGame = new TICGame(myBoardPanel, myMessagePanel, this);
	// Tell the boardPanel about the game that owns it
	myBoardPanel.setGame(myGame);

	// Set up the control frame
	controller = new TICControl(myGame, myMessagePanel, 
				    "TICController"); 

	//  Set up the audio clips
	playGame = getAudioClip(getDocumentBase(), "playgame.au");
	strangeGame = getAudioClip(getDocumentBase(), "strange.au");
	greeting = getAudioClip(getDocumentBase(), "greeting.au");
	notBadHuman = getAudioClip(getDocumentBase(), "notbad.au");
	gameOver = getAudioClip(getDocumentBase(), "gameover.au");
	synthetic = getAudioClip(getDocumentBase(), "synthe.au");
	finishGame = getAudioClip(getDocumentBase(), "finish.au");

	playGame.play();
	
	myMessagePanel.print("Shall we play a game?");
    }

    public void playClip(int i) {
	AudioClip theClip = playGame;
	
	switch (i) {
	case 0: 
	    theClip = playGame;
	    break;
	case 1:
	    theClip = strangeGame;
	    break;
	case 2:
	    theClip = greeting;
	    break;
	case 3:
	    theClip = notBadHuman;
	    break;
	case 4:
	    theClip = gameOver;
	    break;
	case 5:
	    theClip = synthetic;
	    break;
	case 6:
	    theClip = finishGame;
	}
	theClip.play();
    }
    
    public void paint(Graphics g) {

    }

    public void stop() {
	controller.dispose();
    }

} // TICDisplay
