package scrabble;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;

/**
 *  Main Game Applet Class
 **/
public class Scrabble extends Applet implements ActionListener {
  // All parameters used by the game.
  private ServerConnection server;
  private String serverName;
  private Bag bag;		    // Container hold letters
  private Board board;
  private boolean single = false;
  private boolean myTurn;
  private boolean finishTurn = false;
  private Letter others[] = new Letter[7];
  private String name;
  private String others_name;
  private Panel upperPanel;
  private Label label;
  private TextField namefield;
  private Button button;
  private TextField textfield;
 
  // Parameters used by the GUI
  private List idList;
  private Button challenge;
  private Canvas canvas;
  
  /** 
   * Build BorderLayout, create splash canvas
   **/
  public void init() {
    setLayout(new BorderLayout());
    serverName = getCodeBase().getHost();
    //System.out.println(getCodeBase());
    if (serverName.equals(""))
      serverName = "localhost";
    canvas = new IntroCanvas();
  }
  
  /** 
   * Call the method to reload the program page
   **/
  public void start() {
    try {
      showStatus("Connecting to " + serverName);
      server = new ServerConnection(this, serverName);
      server.start();
      showStatus("Connected: " + serverName);

      if (name == null) {
        label = new Label("Enter your name here:");
        namefield = new TextField(20);
        namefield.addActionListener(this);
        upperPanel = new Panel();
        upperPanel.setBackground(new Color(255, 255, 200));
        upperPanel.add(label);
        upperPanel.add(namefield);
        add("North", upperPanel);
        add("Center", canvas);
      } else {
        if (textfield != null) {
          remove(textfield);
          remove(board);
          remove(button);
        }
        nameEntered(name);
      }
      validate();
    } catch (Exception e) {
      single = true;
      start_Game((int)(0x7fffffff * Math.random()));
    }
  }
  
  /** 
   * Call this method when the program quit
   **/
  public void stop() {
   if (!single)
     server.quit();
  }
 
  /** 
   * Call this method when new player join the game
   **/
  void add(String id, String hostname, String name) {
    delete(id); // in case it is already there.
    idList.add("(" + id + ")  " + name + "@" + hostname);
    showStatus("Choose a player from the list");
  }
  
  /**
   * Remove the user form the list of players
   **/
  void delete(String id) {
    for (int i = 0; i < idList.getItemCount(); i++) {
      String s = idList.getItem(i);
      s = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
      if (s.equals(id)) {
        idList.remove(i);
        break;
      }
    }
    if (idList.getItemCount() == 0 && bag == null)
      showStatus("Wait for other players to arrive.");
  }
  
  /**
   * Find name based on ID, it not found, return null
   **/
  private String getName(String id) {
    for (int i = 0; i < idList.getItemCount(); i++) {
      String s = idList.getItem(i);
      String id1 = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
      if (id1.equals(id)) {
        return s.substring(s.indexOf(" ") + 3, s.indexOf("@"));
      }
    }
    return null;
  }
  /** 
   * Called by ServerConnection when receive another player's challenge 
   **/
  void challenge(String id) {
    myTurn = false;
    int seed = (int)(0x7fffffff * Math.random());
    others_name = getName(id); 
    showStatus("challenged by " + others_name);
    server.accept(id, seed);
    server.delete();
    start_Game(seed);
//  System.out.println(server.lport);
//  new Thread(new Server(server.lport)).start();
  }
  
  /** 
   * Accept the challenge 
   **/
  void accept(String id, int seed) {
    myTurn = true;
    others_name = getName(id);
    server.delete();
    start_Game(seed);
  }
  
  /**
   * Called by server based on different input from the textfield
   **/
  void getChat(String id, String s) {
  	if(s.equals("ping")) {
  		showStatus(serverName);
//  		try {    
//  			String line = null;
//        Process pro = Runtime.getRuntime().exec("ping " + serverName +" -t");    
//        BufferedReader buf = new BufferedReader(new InputStreamReader(pro.getInputStream()));    
//        while((line = buf.readLine()) != null) {   
//          int position=0;   
//          if((position=line.indexOf("time"))>=0) {  
//         	 //System.out.println(line);    
//         	 String value=line.substring(position+5,line.lastIndexOf("ms"));   
//         	showStatus("Ping: " + value + "ms");                  
//          }   
//        }            
//   	 } catch(Exception ex) {    
//        System.out.println(ex.getMessage());    
//   	 	 }
  	}
  	else{
  		showStatus(others_name + ": " + s);
  	}
  }
  
 /** 
  * Called each time when opponent place a letter
  **/
  void move(String letter, int x, int y) {
    for (int i = 0; i < 7; i++) {
      if (others[i] != null && others[i].getSymbol().equals(letter)) {
        Letter already = board.getLetter(x, y);
        if (already != null) {
          board.moveLetter(already, 15, 15); 
        }
        board.moveLetter(others[i], x, y);
        board.commitLetter(others[i]);
        others[i] = bag.takeOut();
        if (others[i] == null)
          showStatus("No more letters");
        break;
      }
    }
    board.repaint();
  }
  
  /** 
   * Called when opponent finish his move
   **/
  void turn(int score, String words) {
    showStatus(others_name + " played: " + words + " worth " + score);
    button.setEnabled(true);
    board.othersTurn(score);
  }
  
  /**
   * Called when one player quit
   **/
  void quit(String id) {
    showStatus(others_name + " just quit.");
    remove(textfield);
    remove(board);
    remove(button);
    nameEntered(name);
  }
  
  /**
   * Called when player enter the name and hit enter
   **/
  private void nameEntered(String s) {
    if (s.equals(""))
      return;
    name = s;
    if (canvas != null)
      remove(canvas);
    if (idList != null)
      remove(idList);
    if (challenge != null)
      remove(challenge);
    idList = new List(10, false);
    add("Center", idList);
    challenge = new Button("Challenge");
    challenge.addActionListener(this);
    add("North", challenge);
    validate();
    server.setName(name);
    showStatus("Wait for other players to arrive.");
    if (upperPanel != null)
      remove(upperPanel);
  }
  
  /** 
   * Start game with called wepick and theypick
   **/
  private void wepick() {
    for (int i = 0; i < 7; i++) {
      Letter l = bag.takeOut();
      board.addLetter(l);
    }
  }
 
  private void theypick() {
    for (int i = 0; i < 7; i++) {
      Letter l = bag.takeOut();
      others[i] = l;
    }
  }
  
  /**
   * check single player or multiplier
   * create splash screen and game board, then start game
   **/
  private void start_Game(int seed) {
  	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Long time = System.currentTimeMillis();
    String date = format.format(time);
    System.out.println(new Date());
    if (single) {
      Frame popup = new Frame("Scrabble");
      popup.setSize(400, 300);
      popup.add("Center", canvas);
      popup.setResizable(false);
      popup.show();
      board = new Board();
      showStatus("no server found, playing solo");
      myTurn = true;
    } else {
    	System.out.println(new Date());
      remove(idList);
      remove(challenge);
      board = new Board(name, others_name);
      textfield = new TextField();
      textfield.addActionListener(this);
      add("North", textfield);
      showStatus("playing against " + others_name);
    }

    add("Center", board);
    button = new Button("done");
    button.addActionListener(this);
    add("South", button);
    validate();

    bag = new Bag(seed);
    if (myTurn) {
      wepick();
      if (!single)
        theypick();
    } else {
      button.setEnabled(false);
      theypick();
      wepick();
    }
    board.repaint();
  }
  
  /**
   * Called when challenge hit
   **/
  private void challenge_them() {
    String s = idList.getSelectedItem();
    if (s == null) {
      showStatus("Choose a player from the list then press Challenge");
    } else {
      remove(challenge);
      remove(idList);
      String destid = s.substring(s.indexOf('(')+1, s.indexOf(')'));
      showStatus("challenging: " + destid);
      server.challenge(destid);  
      validate();
    }
  }
  
  /**
   * Called when button hit 
   **/
  private void our_turn() {
    String word = board.findwords();
    if (word == null) {
      showStatus("Illegal letter positions");
    } else {
      if ("".equals(word)) {
        if (single)
          return;
        if (finishTurn) {
          button.setEnabled(false);
          server.turn("pass", 0);
          showStatus("You passed");
          finishTurn = false;
        } else {
          showStatus("Press button again to pass");
          finishTurn = true;
          return;
        }
      } else {
        finishTurn = false;
      }
      showStatus(word);
      board.commit(server);
      for (int i = 0; i < 7; i++) {
        if (board.getTray(i) == null) {
          Letter l = bag.takeOut();
          if (l == null)
            showStatus("No more letters");
          else
            board.addLetter(l);
        }
      }
      if (!single) {
        button.setEnabled(false);
        server.turn(word, board.getTurnScore());
      }
      board.repaint();
    }
  }
  
  /** 
   * get response from different components
   **/
  public void actionPerformed(ActionEvent ae) {
    Object source = ae.getSource();
    if(source == textfield) {
      server.chat(textfield.getText());
      textfield.setText("");
    }
    else if(source == challenge) {
      challenge_them();
    }
    else if(source == button) {
      our_turn();
    }
    else if(source == namefield) {
      TextComponent tc = (TextComponent)source;
      nameEntered(tc.getText());
    }
  }
}