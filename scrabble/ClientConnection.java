package scrabble;

import java.net.*;
import java.io.*;
import java.util.*;

/** 
 *  ClientConnection create a connection to server for each player
 *  Manage the I/O operation with player
 **/
class ClientConnection implements Runnable {
  private Socket sock;
  private BufferedReader in;
  private OutputStream out;
  private String host;
  private Server server;
  private static final String CRLF = "\r\n";
  private String name = null;    
  private String id;
  private boolean busy = false;
  
  /** 
   *  Constructor that store server parameter, socket and id
   **/
  public ClientConnection(Server srv, Socket s, int i) {
    try {
      server = srv;
      sock = s;
      in = new BufferedReader(new InputStreamReader(s.getInputStream()));
      out = s.getOutputStream();
      host = s.getInetAddress().getHostName();
      id = "" + i;
      write("id " + id + CRLF);
      new Thread(this).start();
    } catch (IOException e) {
      System.out.println("failed ClientConnection " + e);
    }
  }
  
  /** 
   * Override toString to show the connection data
   **/
  public String toString() {
    return id + " " + host + " " + name;
  }

  public String getHost() {
    return host;
  }
 
  public String getId() {
    return id;
  }
 
  public boolean isBusy() {
    return busy;
  }
 
  public void setBusy(boolean b) {
    busy = b;
  }
  
  /** 
   * Called when player quit or an exception has been throw
   **/
  public void close() {
    server.kill(this);
    try {
      sock.close();   
    } catch (IOException e) { }
  }
  
  /**
   * write string to stream
   * use getBytes to convert string to a byte array
   **/
  public void write(String s) {
    byte buf[];
    buf = s.getBytes();
    try {
      out.write(buf, 0, buf.length);
    } catch (IOException e) {
      close();
    }
  }
  
  private String readline() {
    try {
      return in.readLine();
    } catch (IOException e) {
      return null;
    }
  }

  static private final int NAME = 1;
  static private final int QUIT = 2;
  static private final int TO = 3;
  static private final int DELETE = 4;
 
  static private Hashtable keys = new Hashtable();
  static private String keystrings[] = {
    "", "name", "quit", "to", "delete"
  };
  static {
    for (int i = 0; i < keystrings.length; i++)
      keys.put(keystrings[i], new Integer(i));
  }
 
  private int lookup(String s) {
    Integer i = (Integer) keys.get(s);
    return i == null ? -1 : i.intValue();
  }
  
  /**
   * Manage all the message from player
   * */
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
      case NAME:
        name = st.nextToken() +
          (st.hasMoreTokens() ? " " + st.nextToken(CRLF) : "");
        System.out.println("[" + new Date() + "] " + this + "\r");
        server.set(id, this);
        break;
      case QUIT:
        close();
        return;
      case TO:
        String dest = st.nextToken();
        String body = st.nextToken(CRLF);
        server.sendto(dest, body);
        break;
      case DELETE:
        busy = true;
        server.delete(id);
        break;
      }
    }
    close();
  }
}