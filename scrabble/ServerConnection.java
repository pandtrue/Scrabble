package scrabble;

import java.io.*;
import java.net.*;
import java.util.*;

/** 
 *  ServerConnection class manage the I/O operation with server and opponent
 **/
class ServerConnection implements Runnable {
  private static final int port = 8888;  
  private BufferedReader in;  
  private PrintWriter out;
  private String myID, OpponentID = null;  
  private Scrabble scrabble;
  private String serverName = null;
  Process pro = null;
  Integer lport = 0;
  
  /** 
   *  Constructor can create socket to connection to the port of the server
   *  if success, wrap input and outstream, otherwise, throw an exception
   **/
  public ServerConnection(Scrabble sc, String site) throws IOException {
    scrabble = sc;
    Socket server = new Socket(site, port);
    lport = server.getLocalPort();
    serverName = site;
    in = new BufferedReader(new InputStreamReader(server.getInputStream()));
    out = new PrintWriter(server.getOutputStream(), true);
  }
  
  private String readline() {
    try {
      return in.readLine();
    } catch (IOException e) {
      return null;
    }
  }
  
  /**
   * Notify server player's name
   **/
  void setName(String s) {
    out.println("name " + s);
  }
 
  /** 
   * Delete myself from the list
   **/
  void delete() {
    out.println("delete " + myID);
  }
  
  /**
   * bond to opponent's id
   **/
  void setTo(String to) {
    OpponentID = to;
  }

  void send(String s) {
    if (OpponentID != null)
      out.println("to " + OpponentID + " " + s);
  }

  /**
   * Send challenge message
   **/
  void challenge(String destid) {
    setTo(destid);
    send("challenge " + myID);
  }
 
  /**  
   * Accept message used for response challenge
   **/
  void accept(String destid, int seed) {
    setTo(destid);
    send("accept " + myID + " " + seed);
  }
 
  void chat(String s) {
    send("chat " + myID + " " + s);
  }
 
  /**
   * Send move message every time move a letter
   **/
  void move(String letter, int x, int y) {
    send("move " + letter + " " + x + " " + y);
  }
 
  /** 
   * Send turn message every turn finish
   **/
  void turn(String words, int score) {
    send("turn " + score + " " + words);
  }
 
  /** 
   * Send quit message when player quit the game 
   **/
  void quit() {
    send("quit " + myID);  // tell other player
    out.println("quit"); // unhook
  }
 
  private Thread t;
 
  void start() {
    t = new Thread(this);
    t.start();
  }

  // Parameters used for initialize hashtable, which used for map string and position of array
  private static final int ID = 1;
  private static final int ADD = 2;
  private static final int DELETE = 3;
  private static final int MOVE = 4;
  private static final int CHAT = 5;
  private static final int QUIT = 6;
  private static final int TURN = 7;
  private static final int ACCEPT = 8;
  private static final int CHALLENGE = 9;
  private static Hashtable keys = new Hashtable();
  private static String keystrings[] = {"", "id", "add", "delete", "move", "chat",
    "quit", "turn", "accept", "challenge"};
  static {
    for (int i = 0; i < keystrings.length; i++)
      keys.put(keystrings[i], new Integer(i));
  }
 
  private int lookup(String s) {
    Integer i = (Integer) keys.get(s);
    return i == null ? -1 : i.intValue();
  }
  
  /** 
   *  Main loop used for connect to the server and do blocking-call to get readline()
   *  Use StringTokenizer to separate string and do response based on it
   **/
  public void run() {
    String s;
    StringTokenizer st;
    while ((s = readline()) != null) {
      st = new StringTokenizer(s);
      String keyword = st.nextToken();
      switch (lookup(keyword)) {
      default:
        System.out.println("bogus keyword: " + keyword + "\r");
        break;
      case ID:
        myID = st.nextToken();
        break;
      case ADD: {
          String myID = st.nextToken();
          String hostname = st.nextToken();
          String name = st.nextToken("\r\n");
          scrabble.add(myID, hostname, name);
        }
        break;
      case DELETE:
        scrabble.delete(st.nextToken());
        break;
      case MOVE: {
          String ch = st.nextToken();
          int x = Integer.parseInt(st.nextToken());
          int y = Integer.parseInt(st.nextToken());
          scrabble.move(ch, x, y);
        }
        break;
      case CHAT: {
          String from = st.nextToken();
          String sentence = st.nextToken("\r\n");
          
          if(sentence.equals("ping")) {
          	try {    
	      			String line = null;
	            pro = Runtime.getRuntime().exec("ping " + serverName +" -t");    
	            BufferedReader buf = new BufferedReader(new InputStreamReader(pro.getInputStream()));    
	            while((line = buf.readLine()) != null) {   
	              int position=0;   
	              if((position=line.indexOf("time"))>=0) {  
	             	 //System.out.println(line);    
	             	 String value=line.substring(position+5,line.lastIndexOf("ms"));   
	             	scrabble.getChat(from, "Ping: " + value + "ms");                  
	              }   
	            }            
	       	 } catch(Exception ex) {    
	            System.out.println(ex.getMessage());    
	       	 	 }
          }
          else{
          	if(pro!=null){
          		pro.destroy();
          		pro=null;
          	}
          	scrabble.getChat(from, sentence);
          }
        }
        break;
      case QUIT: {
          String from = st.nextToken();
          scrabble.quit(from);
        }
        break;
      case TURN: {
          int score = Integer.parseInt(st.nextToken());
          scrabble.turn(score, st.nextToken("\r\n"));
        }
        break;
      case ACCEPT: {
          String from = st.nextToken();
          int seed = Integer.parseInt(st.nextToken());
          scrabble.accept(from, seed);
        }
        break;
      case CHALLENGE: {
          String from = st.nextToken();
          scrabble.challenge(from);
        }
        break;
      }
    }
  }
}