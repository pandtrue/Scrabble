package scrabble;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 *  Server class for the game
 **/
public class Server implements Runnable {
  private int port;
  private Hashtable IDMap = new Hashtable(); // Store the connection with all the player
  private int id = 0;
  
  public Server(Integer port) {
  	this.port = port;
  }
  
  /**
   * Add new player
   **/
  synchronized void addConnection(Socket s) {
    ClientConnection con = new ClientConnection(this, s, id);
    id++;
  }
  
  /** 
   *  Called to response player and trace all ID in incon to prevent 
   *  players have same name, then call setBusy to indicate the connection can start the game
   *  To the not busy connection, send message to notify this new connection
   **/
  synchronized void set(String the_id, ClientConnection con) {
    IDMap.remove(the_id) ; 
    con.setBusy(false);
    Enumeration e = IDMap.keys();
    while (e.hasMoreElements()) {
      String id = (String)e.nextElement();
      ClientConnection other = (ClientConnection) IDMap.get(id);
      if (!other.isBusy())
        con.write("add " + other + "\r\n");
    }
    IDMap.put(the_id, con);
    broadcast(the_id, "add " + con);
  }
  
  /**
   * Send message to destination connection
   **/
  synchronized void sendto(String dest, String body) {
    ClientConnection con = (ClientConnection)IDMap.get(dest);
    if (con != null) {
      con.write(body + "\r\n");
    }
  }
  
  /**
   * Send message to all the connection exception exclude
   **/
  synchronized void broadcast(String exclude, String body) {
    Enumeration e = IDMap.keys();
    while (e.hasMoreElements()) {
      String id = (String)e.nextElement();
      if (!exclude.equals(id)) {
        ClientConnection con = (ClientConnection) IDMap.get(id);
        con.write(body + "\r\n");
      }
    }
  }
  
  /** 
   *  Delete player from the list of available to challenge
   **/
  synchronized void delete(String the_id) {
     broadcast(the_id, "delete " + the_id);
  }
  
  /** 
   *  Called when player quit the game or just close the program
   **/
  synchronized void kill(ClientConnection c) {
    if (IDMap.remove(c.getId()) == c) {
      delete(c.getId());
    }
  }
  
  /** 
   *  Infinite loop for server to listen for the new player to join
   **/
  public void run() {
    try {
      ServerSocket acceptSocket = new ServerSocket(port);
      System.out.println("Server listening on port " + port);
      while (true) {
        Socket s = acceptSocket.accept();
        addConnection(s);
      }
    } catch (IOException e) {
      System.out.println("accept loop IOException: " + e);
    }
  }
  
  /**
   * Create a thread to run the server instance
   **/
  public static void main(String args[]) {
    new Thread(new Server(8888)).start();
    try {
      Thread.currentThread().join();
    } catch (InterruptedException e) { }
  }
}