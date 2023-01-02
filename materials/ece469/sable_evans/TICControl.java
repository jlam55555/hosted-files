
/**
 * TICControl.java
 *
 *  TICControl is a class that implements a separate frame that
 *  controls the administrative functions of the TIC program.  It
 *  is going to be used for debugging purposes initially, and later 
 *  evolve into a parameter setting object.  
 *
 *  The initial main use will be to set up boards to test our 
 *  legalMoves functions on and so on.
 *
 * Created: Mon Apr 26 22:31:16 1999
 *
 * @author David Evans
 * @version
 */

import java.awt.*;
import java.awt.event.*;
import TICMessagePanel;
import TICGame;
import TICHumLis;

public class TICControl extends Frame {
    // Constants
    public static final int WIDTH = 300;
    public static final int HEIGHT = 400;
    
    TICMessagePanel message;
    TICGame controlee; 
    CardLayout myLayout;

    Panel tabs;   //  A panel that controls which card to show
    Panel cards;  //  A panel of different cards, controlled by above
    Button turnButton;  /* We have to remember this button so we can 
			   change it from "Now Black" to "Now Red" to
			   reflect the change in turns as we click it.
			*/
    Button debugMode;  /*  A button that says "Debug Mode" or 
			   "Normal Mode" depending on if we are 
			   debugging or not.  Note that while 
			   debugging HUMAN PLAYERS CAN NOT PLAY!
		       */
    Button pause;  /*  A button to pause / resume the game */
    Choice redPlayerChoice;  /*  We have to keep refs to the options
			      *  choices around so we can get the 
			      *  selected values out of them.
			      */
    Choice blackPlayerChoice;

    /**
     *  Constructor - give it a TICMessagePanel to spit messages to,
     *  and a String title that is the title of this frame.
     *  
     */

    public TICControl(TICGame toControl, TICMessagePanel mp, 
		      String title) {
	super(title);
	message = mp;
	controlee = toControl;

	setResizable(true);
	setSize(TICControl.WIDTH, TICControl.HEIGHT);

	//  Make a panel for buttons to flip through the cards
	tabs = new Panel(new FlowLayout());
	Button temp = new Button("Debug");
	temp.addActionListener( new TICCntlCardLis() );
	tabs.add(temp);
	temp = new Button("Game Options");
	temp.addActionListener( new TICCntlCardLis() );
	tabs.add(temp);

	//  Make the card panel
	cards = new Panel();
	//  Make cards a card layout
	myLayout = new CardLayout();
	cards.setLayout( myLayout );
	
	//  Add a new Debug mode panel card to cards
	cards.add( new TICCntlDebugCard(), "Debug");
	
	//  Add a new Game Options panel to card cards
	cards.add( new TICCntlOptsCard(), "Game Options");

	//  Show the game options mode by default
	myLayout.show(cards, "Game Options");

	//  Set the frame layout to a border layout
	setLayout(new BorderLayout());
	//  Add in the buttons panel
	add(tabs, "North");
	//  Add in the card panel
	add(cards, "Center");

	//  Add a window listener to this punk mother so we can
	//  close it
	addWindowListener( new TICCntlDie() );

	//  Make this mother visible
	setVisible(true);

	message.print("TICControl initialized");
    }

    /**
     * createNewPlayer(int color)
     * Returns a player for the color of player color,
     * created by looking at the option for the type of player
     * in the game options card.  
     */
    public TICPlayer createNewPlayer(int color) {
	TICPlayer tempPlayer;

	tempPlayer = null;  // Stop your whining compiler!
	//  Show the game options dialog if it is not
	//  Actually, it must be, they pressed play game...
	myLayout.show(cards, "Game Options");	
	String selected;
	if (color == TICConstants.REDPLAYER) {
	    //  see what is selected
	    selected = redPlayerChoice.getSelectedItem();
	} else if (color == TICConstants.BLACKPLAYER) {
	    selected = blackPlayerChoice.getSelectedItem();
	} else {
	    selected = "";
	    message.print("Player color "+color+" unknown.");
	}
	if (selected.equals("Computer")) {
	    tempPlayer = new TICComputerPlayer();
	    if (color == TICConstants.REDPLAYER) {
		((TICComputerPlayer)tempPlayer).setOptionsTitle("Red Computer Player Options");
	    } else {
		((TICComputerPlayer) tempPlayer).setOptionsTitle("Black Computer Player Options");
	    }
	} else if (selected.equals("Human")) {
	    //  A human player needs to first create the human player, then the listener
	    tempPlayer = new TICHumanPlayer();
	    //  Now we have to create the TICHumLis for this player
	    TICHumLis humanListener = new TICHumLis( (TICHumanPlayer) tempPlayer, controlee);
	    //  Now we have to register the listener on each square of the board
	    controlee.registerHumanListener( humanListener );
	} else {
	    message.print(selected+" players not implemented yet.");
	}
	return tempPlayer;
    }

    /**
     *  Inner class TICCntlOptsCard
     *  This panel has options for the game. 
     */
    class TICCntlOptsCard extends Panel {
	public TICCntlOptsCard() {
	    //  Make a panel that contains stuff for the game options
	    //  Human players, computer players.  
	    //  Other stuff later.  
	    setLayout(new GridLayout(5,2));
	    //  Add a Label for Red player
	    add(new Label("Red player:"));
	    //  Make a choice for the red player
	    redPlayerChoice = new Choice();
	    redPlayerChoice.add("Human");
	    redPlayerChoice.add("Computer");
	    add( redPlayerChoice );
	    //  Add a Label for the Black player
	    add(new Label("Black player:"));
	    //  Make a choice for the black player
	    blackPlayerChoice = new Choice();
	    blackPlayerChoice.add("Human");
	    blackPlayerChoice.add("Computer");
	    add( blackPlayerChoice );
	    
	    //  Make a button to play the game
	    Button playGame = new Button("Play Game");
	    TICCntlPMLis myListener = new TICCntlPMLis();
	    playGame.addActionListener( myListener );
	    add( playGame );

	    //  Make a button to reset the game
	    Button resetGame = new Button("Reset");
	    resetGame.addActionListener( myListener );
	    add( resetGame );

	    //  Make a thing for turn history
	    Button goToHistory = new Button("Undo Move");
	    goToHistory.addActionListener( myListener );
	    add( goToHistory );

	    //  Make a continue button for undoing moves
	    Button continueGame = new Button("Continue");
	    continueGame.addActionListener( myListener );
	    add( continueGame );

	    //  Make a button for getting hints
	    Button getHint = new Button("Get Hint");
	    getHint.addActionListener( myListener );
	    add( getHint );

	    //  Make a button for offering a draw
	    Button offerDraw = new Button("Offer Draw");
	    offerDraw.addActionListener( myListener );
	    add( offerDraw );

	}
    }

    /**
     *  Inner class TICCntlDebugCard
     *  This class implements all GUI elements for debugging stuff
     */
    class TICCntlDebugCard extends Panel {
	
	public TICCntlDebugCard() {
	    // Make a panel that just contains the stuff that we use to
	    // control clicking: what to insert.  Make it a radio group.
	    // Make a single button that prints the board moves
	    setLayout(new GridLayout(10,2));

	    add(new Label("Click for:"));

	    CheckboxGroup clickGroup = new CheckboxGroup();

	    Checkbox c = new Checkbox("Red piece", clickGroup, false);
	    c.addMouseListener(new TICCntlClk());
	    add(c);

	    c = new Checkbox("Red King", clickGroup, false);
	    c.addMouseListener(new TICCntlClk());
	    add(c);

	    c = new Checkbox("Black piece", clickGroup, false);
	    c.addMouseListener(new TICCntlClk());
	    add(c);

	    c = new Checkbox("Black King", clickGroup, false);
	    c.addMouseListener(new TICCntlClk());
	    add(c);

	    c = new Checkbox("Selected", clickGroup, false);
	    c.addMouseListener(new TICCntlClk());
	    add(c);

	    c = new Checkbox("Highlighted", clickGroup, false);
	    c.addMouseListener(new TICCntlClk());
	    add(c);

	    c = new Checkbox("Moveable piece", clickGroup, false);
	    c.addMouseListener(new TICCntlClk());
	    add(c);

	    c = new Checkbox("Possible Destination", clickGroup, false);
	    c.addMouseListener(new TICCntlClk());
	    add(c);

	    c = new Checkbox("Clear", clickGroup, true);
	    c.addMouseListener(new TICCntlClk());
	    add(c);

	    //  Add the button for printing board moves
	    Button printMoves = new Button("legal moves");	
	    printMoves.addActionListener(new TICCntlPMLis());
	    add(printMoves);	

	    //  Add the button for changing the turn
	    turnButton = new Button("Now Red");
	    turnButton.addActionListener(new TICCntlPMLis());
	    add(turnButton);

	    //  Make a button for choosing a move
	    Button chooseMove = new Button("Choose Move");
	    chooseMove.addActionListener(new TICCntlPMLis());
	    add(chooseMove);

	    //  Make a button for debug/normal mode
	    debugMode = new Button("Normal Mode");
	    debugMode.addActionListener( new TICCntlPMLis());
	    add(debugMode);

	    //  Make a button for pause / resume
	    pause = new Button("Pause");
	    pause.addActionListener(new TICCntlPMLis());
	    add(pause);

	    setVisible(true);
	}
    }

    /**
     *  Inner class TICCntlPMLis
     */
    class TICCntlPMLis implements java.awt.event.ActionListener {
	public void actionPerformed(ActionEvent e) {
	    String command = e.getActionCommand();
	    if (command.equals("legal moves")) {
		TICState theState = controlee.getState();
		theState.printState();
		theState.createMoves();
		theState.printMoves();
	    } else if (command.equals("Now Red")) {
		//  It is now Red's turn, we want it to be Black's
		turnButton.setLabel("Now Black");
		controlee.setTurn(TICConstants.BLACKPLAYER);
	    } else if (command.equals("Now Black")) {
		//  It is now Black's turn, change it to Red's
		turnButton.setLabel("Now Red");
		controlee.setTurn(TICConstants.REDPLAYER);
	    } else if (command.equals("Choose Move")) {
		controlee.chooseMove();
	    } else if (command.equals("Play Game")) {
		// Funky new inner class syntax:
		// EnclosingClassName.EnclosingClassField
		controlee.playGame( TICControl.this );
	    } else if (command.equals("Reset")) {
		controlee.resetGame( TICControl.this );
	    } else if (command.equals("Debug Mode")) {
		debugMode.setLabel("Normal Mode");
		// Got to Normal Mode
		controlee.normalMode();		
	    } else if (command.equals("Normal Mode")) {
		debugMode.setLabel("Debug Mode");
		// Go to Debug Mode
		controlee.debugMode();
	    } else if (command.equals("Pause")) {
		pause.setLabel("Resume");
		//  Pause the game
		controlee.pauseGame();
		message.print("Game is paused");
	    } else if (command.equals("Resume")) {
		pause.setLabel("Pause");
		//  Resume the game
		controlee.resumeGame();
		message.print("Game resumed");
	    } else if (command.equals("Undo Move")) {
		if ( controlee.gameHistoryLen() <= 1) {
		    //  We only have one state: the initial state.
		    //  Just reset the game
		    controlee.resetGame( TICControl.this );
		} else {
		    if (controlee.getFirstUndo() == true) {
			controlee.goToTurn( controlee.gameHistoryLen() - 2);
			controlee.setFirstUndo(false);
		    } else {
			controlee.goToTurn( controlee.gameHistoryLen() - 1);
		    }
		}
	    } else if (command.equals("Continue")) {
		controlee.continueGame();
	    } else if (command.equals("Get Hint")) {
		TICComputerPlayer hintGiver = new TICComputerPlayer( false );
		hintGiver.setOptionsTitle("Hint Giver Options");
		TICMove hint = hintGiver.getHint( controlee.getState() );
		message.print("The hint is: "+ hint);
		//  Kill the hintGiver
		hintGiver.cleanUp();
		hintGiver = null;
		//  Try to flash the hint squares
		TICBoardPanel theBoard = controlee.getBoard();
		for (int i=0; i<hint.size(); i++) {
		    Point hintSq = hint.getElementAt(i);
		    theBoard.board[hintSq.x][hintSq.y].setBackgroundState(TICConstants.POSSIBLE_DESTINATION_ON);
		    controlee.setBoardDirtyBit(true);
		}
	    } else if (command.equals("Offer Draw")) {
		//  Check to make sure that we have one human and one
		//  computer player
		if ((controlee.getBlackPlayer() instanceof TICHumanPlayer &&
		     controlee.getRedPlayer() instanceof TICComputerPlayer) ||
		    (controlee.getBlackPlayer() instanceof TICComputerPlayer &&
		     controlee.getRedPlayer() instanceof TICHumanPlayer)) {
		    //  We have one human and one computer
		    //  Check to see that it is the human's turn
		    if (controlee.whoseTurn() instanceof TICHumanPlayer) {
			//  It is the human's turn
			TICComputerPlayer computerPlayer;
			int color;
			if (controlee.getBlackPlayer() instanceof TICHumanPlayer) {
			    computerPlayer = (TICComputerPlayer) controlee.getRedPlayer();
			    color = TICConstants.REDPLAYER;
			} else {
			    computerPlayer = (TICComputerPlayer) controlee.getBlackPlayer();
			    color = TICConstants.BLACKPLAYER;
			}
			// Check to see if the computer will accept a draw
			message.print("Thinking...  Please wait...");
			if (computerPlayer.considerDraw( controlee.getState(), color)) {
			    //  The computer will accept a draw
			    message.print("Draw offer accepted.");
			    controlee.playClip(1);
			} else {
			    //  The computer does not accept the draw
			    message.print("Draw request DENIED!");
			    controlee.playClip(5);
			}
		    } else {
			message.print("Wait for your turn to offer a draw");
		    }
		} else {
		    if (controlee.getBlackPlayer() instanceof TICComputerPlayer) {			
			message.print("We are the borg.  Draw offers are irrelevant.");
		    } else {
			message.print("You are humans.  Talk it out.");
		    }
		}
	    }
	}
    }

    /**
     *  Inner class TICCntlCardLis
     *  This class listens to buttons in the TICControl main panel
     *  that flip between different cards in the card layout.  
     */
    class TICCntlCardLis implements java.awt.event.ActionListener {
	public void actionPerformed(ActionEvent e) {
	    String buttonLabel = e.getActionCommand();
	    if (buttonLabel.equals("Debug")) {
		myLayout.show(cards, "Debug");
	    } else if (buttonLabel.equals("Game Options")) {
		myLayout.show(cards, "Game Options");
	    }
	}
    }

    /**
     *  Inner class TICCntlDie
     *  A class to listen for the close window event and do it.
     */
    class TICCntlDie implements java.awt.event.WindowListener {
	public TICCntlDie() {

	}
	
	public void windowClosing(WindowEvent e) {
	    //  Kill this window
	    TICControl.this.dispose();
	}
	
	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
    }
 
    /** 
     *  Inner class TICCntlClk
     *  This inner class listens for a click on one of the buttons,
     *  and tells the TICBoardPanel to change the function of the 
     *  clicking accordingly.
     */

    class TICCntlClk extends java.awt.event.MouseAdapter {
	public void mouseClicked(MouseEvent e) {
	    //  Get a ref to the button that was clicked
	    Checkbox button = (Checkbox) e.getComponent();
	    //  Get the label of this button - try to find out
	    //  what it is that this button wants to do to the board
	    String label = button.getLabel();
	    //  Would use a switch -- if theirs wasn't the brain
	    //  dead C style switch.  Default to IF THEN ELSE IF THEN...
	    int action = TICConstants.NORMAL;
	    if (label.equals("Red piece")) {
		action = TICConstants.RED;
	    } else if (label.equals("Red King")) {
		action = TICConstants.REDKING;
	    } else if (label.equals("Black piece")) {
		action = TICConstants.BLACK;
	    } else if (label.equals("Black King")) {
		action = TICConstants.BLACKKING;
	    } else if (label.equals("Selected")) {
		action = TICConstants.SELECTED;
	    } else if (label.equals("Highlighted")) {
		action = TICConstants.HIGHLIGHTED;
	    } else if (label.equals("Moveable piece")) {
		action = TICConstants.PIECE_CAN_MOVE_ON;
	    } else if (label.equals("Possible Destination")) {
		action = TICConstants.POSSIBLE_DESTINATION_ON;
	    } else if (label.equals("Clear")) {
		action = TICConstants.NORMAL;
	    }	    
	    //  Now send that action to the board
	    controlee.getBoard().setClickFunction(action);
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}       
    }
    
} // TICControl
