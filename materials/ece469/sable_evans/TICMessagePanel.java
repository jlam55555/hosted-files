
/**
 * TICMessagePanel.java
 *
 * This class is a small message area for us to use for debugging 
 * messages and so on.  It is just a text area that appends what is
 * sent to it.  
 * 
 * Created: Sat Apr 24 23:11:16 1999
 *
 * @author David Evans
 * @version
 */

import java.awt.*;
import java.lang.*;

public class TICMessagePanel extends Panel {
    TextArea messages;
    public static final int bigInt = Integer.MAX_VALUE;
    String newline;

    public TICMessagePanel() {
	setBackground(Color.white);
	messages = new TextArea("TIC Initializing...\n", 3, 45);
	messages.setEditable(false);
	add(messages);
	newline = System.getProperty("line.separator");
    }

    public void print(String s) {
	messages.append(s + newline);
	messages.setCaretPosition(bigInt);  // A hack to scroll down
    }
    
} // TICMessagePanel
