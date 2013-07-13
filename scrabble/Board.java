package scrabble;

import java.awt.*;
import java.awt.event.*;

/**
 *  Board encapsulate most of the game logic and the GUI of the gaming board
 **/
class Board extends Canvas {
  private Letter board[][] = new Letter[15][15];
  private Letter tray[] = new Letter[7];
  private Point oldPosition = new Point(0,0);
  private Point newPosition = new Point(0,0);
  private String myName = null;
  private String opponentName = null;
  private int myScore = 0;
  private int opponentScore = 0;
  private int lastTurnScore = 0;
  
  /** 
   * Create player myName
   **/
  Board(String our_name, String other_name) {
    myName = our_name;
    opponentName = other_name;
    addMouseListener(new MyMouseAdapter());
    addMouseMotionListener(new MyMouseMotionAdapter());
  }

  /**
   * In single player mode, myName is empty
   **/
  Board() {
    addMouseListener(new MyMouseAdapter());
    addMouseMotionListener(new MyMouseMotionAdapter());
  }
  
  /**
   * Called when one turn finish, and add score by repaint method
   **/
  void othersTurn(int score) {
    opponentScore += score;
    paintScore();
    repaint();
  }
 
  /**
   * Return score of last turn
   **/
  int getTurnScore() {
    paintScore();
    return lastTurnScore;
  }

  Letter getTray(int i) {
    return tray[i];
  }
  
  /** 
   * Put letters to player's tray
   **/
  synchronized boolean addLetter(Letter l) {
    for (int i = 0; i < 7; i++) {
      if (tray[i] == null) {
        tray[i] = l;
        moveLetter(l, i, 15);
        return true;
      }
    }
    return false;
  }
  
  /**
   * check validation of the latter on the board
   **/
  private boolean existingLetterAt(int x, int y) {
    Letter l = null;
    return (x >= 0 && x <= 14 && y >= 0 && y <= 14
      && (l = board[y][x]) != null && l.recall() == null);
  }
  
  /** 
   * Check status for each turn and calculate the score
   **/
  synchronized String findwords() {
    String res = "";
    lastTurnScore = 0;

    int ntiles = 0;
    Letter atplay[] = new Letter[7];
    for (int i = 0; i < 7; i++) {
      if (tray[i] != null && tray[i].recall() != null) {
        atplay[ntiles++] = tray[i];
      }
    }
    if (ntiles == 0)
      return res;

    boolean horizontal = true; 
    boolean vertical = false;
    if (ntiles > 1) {
      int x = atplay[0].x;
      int y = atplay[0].y;
      horizontal = atplay[1].y == y;
      vertical = atplay[1].x == x;
      if (!horizontal && !vertical)
        return null;
      for (int i = 2; i < ntiles; i++) {
        if (horizontal && atplay[i].y != y
          || vertical && atplay[i].x != x)
          return null;
      }
    }
    
    boolean attached = false;
    for (int i = 0; i < ntiles; i++) {
      Point p = atplay[i].recall();
      int x = p.x;
      int y = p.y;
      if ((x == 7 && y == 7 && ntiles > 1) ||
          existingLetterAt(x-1, y) || existingLetterAt(x+1, y) ||
          existingLetterAt(x, y-1) || existingLetterAt(x, y+1)) {
        attached = true;
        break;
      }
    }
    if (!attached) {
      return null;
    }
   
    for (int i = -1; i < ntiles; i++) {
      Point p = atplay[i==-1?0:i].recall(); 
      int x = p.x;
      int y = p.y;
 
      int xinc, yinc;
      if (horizontal) {
        xinc = 1;
        yinc = 0;
      } else {
        xinc = 0;
        yinc = 1;
      }
      int mult = 1;
 
      String word = "";
      int word_score = 0;
      
      while (x >= xinc && y >= yinc &&
             board[y-yinc][x-xinc] != null) {
        x -= xinc;
        y -= yinc;
      }
 
      int n = 0;
      int letters_seen = 0; 
      Letter l;
      while (x < 15 && y < 15 && (l = board[y][x]) != null) {
        word += l.getSymbol();
        int lscore = l.getPoints();
        if (l.recall() != null) {  
          Color t = tiles[y < 8 ? y : 14 - y][x < 8 ? x : 14 - x];
          if (t == word3)
            mult *= 3;
          else if (t == word2)
            mult *= 2;
          else if (t == letter3)
            lscore *= 3;
          else if (t == letter2)
            lscore *= 2;
          if (i == -1) {
            letters_seen++;
          }
        }
        word_score += lscore;
        n++;
        x += xinc;
        y += yinc;
      }
      word_score *= mult;
      if (i == -1) {    
         if (letters_seen != ntiles) {
           return null;
         }
 
         if (ntiles == 7) {
           lastTurnScore += 50;
         }
         horizontal = !horizontal;
       }
      if (n < 2)  
         continue;
 
      lastTurnScore += word_score;
      res += word + " ";
    }
    myScore += lastTurnScore;
    return res;
  }
  
  /**
   * Submit letter on the board
   **/
  synchronized void commit(ServerConnection s) {
    for (int i = 0 ; i < 7 ; i++) {
      Point p;
      if (tray[i] != null && (p = tray[i].recall()) != null) {
        if (s != null)
          s.move(tray[i].getSymbol(), p.x, p.y);
        commitLetter(tray[i]);  
        tray[i] = null;
      }
    }
  }
 
  void commitLetter(Letter l) {
    if (l != null && l.recall() != null) {
      l.paint(secondGraphc, Letter.DIMMER);
      l.remember(null);   
    }
  }
  
  // Private parameter for different position on board
  private Letter pick;  // the letter being dragged
  private int offsetX, offsetY;   
  private int letterWidth, letterHeight;   
  private int topMargin, leftMargin;   
  private int gapWidth;       
  private int areaWidth, areaHeight;   
  private Dimension secondScreensize;
  private Graphics secondGraphc;
  private Graphics secondGraphc2;
  private Image secondScreen;
  private Image secondScreen2;
  
  public void update(Graphics g) {
    paint(g);
  }

  /** 
   *  Call checksize to see whether player is dragging letter and do corresponding step
   **/
  public synchronized void paint(Graphics g) {
  	Dimension d = checksize();
    Graphics gc = secondGraphc2;
    if (pick != null) {
      gc = gc.create();
      gc.clipRect(xx, yy, ww, hh);
      g.clipRect(xx, yy, ww, hh);
    }
    gc.drawImage(secondScreen, 0, 0, null);

    for (int i = 0 ; i < 7 ; i++) {
      Letter l = tray[i];
      if (l != null && l != pick)
        l.paint(gc, Letter.NORMAL);
    }
    if (pick != null)
      pick.paint(gc, Letter.BRIGHTTER);

    g.drawImage(secondScreen2, 0, 0, null);
  }
  
  /** 
   * Return the letter at x, y position
   **/
  Letter LetterHit(int x, int y) {
    for (int i = 0; i < 7; i++) {
      if (tray[i] != null && tray[i].hit(x, y)) {
        return tray[i];
      }
    }
    return null;
  }
  
  /**
   * Delete the letter on board but not submitted
   **/
  private void unplay(Letter let) {
    Point p = let.recall();
    if (p != null) {
      board[p.y][p.x] = null;
      let.remember(null);
    }
  }
  
  /**
   * Calculate the position of the letter going to be move to board
   **/
  private void moveToTray(Letter l, int i) {
    int x = leftMargin + (letterWidth + gapWidth) * i;
    int y = topMargin + areaHeight - 2 * gapWidth;
    l.move(x, y);
  }
  
  /**
   * Called when put letter to tray or letter has dragged from board
   **/
  private void dropOnTray(Letter l, int x) {
    unplay(l); 
    int oldx = 0;
    for (int i = 0 ; i < 7 ; i++) {
      if (tray[i] == l) {
        oldx = i;
        break;
      }
    }
 
    if (tray[x] == null) {
      for (int i = 6 ; i >= 0 ; i--) {
        if (tray[i] != null) {
          x = i;
          break;
        }
      }
    }
    if (tray[x].recall() != null) {
      tray[oldx] = tray[x];
    } else {
      if (oldx < x) {   
        for (int i = oldx ; i < x ; i++) {
          tray[i] = tray[i+1];
          if (tray[i].recall() == null)
            moveToTray(tray[i], i);
        }
      } else {        
       for (int i = oldx ; i > x ; i--) {
         tray[i] = tray[i-1];
         if (tray[i].recall() == null)
           moveToTray(tray[i], i);
        }
      }
    }
    tray[x] = l;
    moveToTray(l, x);
  }
  
  Letter getLetter(int x, int y) {
    return board[y][x];
  }
  
  /**
   * Move letter to board from tray or vice versa
   **/
  void moveLetter(Letter l, int x, int y) {
    if (y > 14 || x > 14 || y < 0 || x < 0) {
      if (x > 6)
        x = 6;
      if (x < 0)
        x = 0;
      dropOnTray(l, x);
    } else {
      if (board[y][x] != null) {
        x = oldPosition.x;
        y = oldPosition.y;
      } else {
        newPosition.x = x;
        newPosition.y = y;
        unplay(l);
        board[y][x] = l;
        l.remember(newPosition);
        x = leftMargin + (letterWidth + gapWidth) * x;
        y = topMargin + (letterHeight + gapWidth) * y;
      }
      l.move(x, y);
    }
  }
  private Color background = new Color(175, 185, 175);
  private Color word3 = new Color(255, 50, 100);
  private Color word2 = new Color(255, 200, 200);
  private Color letter3 = new Color(75, 75, 255);
  private Color letter2 = new Color(150, 200, 255);
  
  // tiles on board
  private Color tiles[][] = {
		{word3,			 background, background, letter2, 	 background, background, background, word3},
    {background, word2, 		 background, background, background, letter3, 	 background, background},
    {background, background, word2, 		 background, background, background, letter2, 	 background},
    {letter2, 	 background, background, word2, 		 background, background, background, letter2},
    {background, background, background, background, word2, 		 background, background, background},
    {background, letter3, 	 background, background, background, letter3, 	 background, background},
    {background, background, letter2, 	 background, background, background, letter2, 	 background},
    {word3, 		 background, background, letter2, 	 background, background, background, word2}
  };

  /**
   *  Check the size and do the first initialize 
   **/
  private Dimension checksize() {
    Dimension d = getSize();
    int w = d.width;
    int h = d.height;
    if (w < 1 || h < 1)
      return d;
    if ((secondScreen == null) ||
      (w != secondScreensize.width) ||
      (h != secondScreensize.height)) {
      System.out.println("updating board: " + w + " x " + h + "\r");
      secondScreen = createImage(w, h);
      secondScreensize = d;
      secondGraphc = secondScreen.getGraphics();
      secondScreen2 = createImage(w, h);
      secondGraphc2 = secondScreen2.getGraphics();
      secondGraphc.setColor(Color.white);
      secondGraphc.fillRect(0,0,w,h);
      
      gapWidth = 1 + w / 400;
      int gaps = gapWidth * 20;
      letterWidth = (w - gaps) / 15;
      letterHeight = (h - gaps - gapWidth * 2) / 16;    
      areaWidth = letterWidth * 15 + gaps;
      areaHeight = letterHeight * 15 + gaps;
      leftMargin = (w - areaWidth) / 2 + gapWidth;
      topMargin = (h - areaHeight - (gapWidth * 2 + letterHeight)) / 2 + gapWidth;

      secondGraphc.setColor(Color.black);
      secondGraphc.fillRect(leftMargin,topMargin,areaWidth-2*gapWidth,areaHeight-2*gapWidth);
      leftMargin += gapWidth;
      topMargin += gapWidth;
      secondGraphc.setColor(Color.white);
      secondGraphc.fillRect(leftMargin,topMargin,areaWidth-4*gapWidth,areaHeight-4*gapWidth);
      leftMargin += gapWidth;
      topMargin += gapWidth;
      int sfh = (letterHeight > 30) ? letterHeight / 4 : letterHeight / 2;
      Font font = new Font("SansSerif", Font.PLAIN, sfh);
      secondGraphc.setFont(font);
      for (int j = 0, y = topMargin; j < 15; j++, y += letterHeight + gapWidth) {
        for (int i = 0, x = leftMargin; i < 15; i++, x += letterWidth + gapWidth) {
          Color c = tiles[j < 8 ? j : 14 - j][i < 8 ? i : 14 - i];
          secondGraphc.setColor(c);
          secondGraphc.fillRect(x, y, letterWidth, letterHeight);
          secondGraphc.setColor(Color.black);
          if (letterHeight > 30) {
            String td = (c == word2 || c == letter2) ? "DOUBLE" :
                        (c == word3 || c == letter3) ? "TRIPLE" : null;
            String wl = (c == letter2 || c == letter3) ? "LETTER" :
                        (c == word2 || c == word3) ? "WORD" : null;
            if (td != null) {
              center(secondGraphc, td, x, y + 2 + sfh, letterWidth);
              center(secondGraphc, wl, x, y + 2 * (2 + sfh), letterWidth);
              center(secondGraphc, "SCORE", x, y + 3 * (2 + sfh), letterWidth);
            }
          } else {
            String td = (c == word2 || c == letter2) ? "2" :
                        (c == word3 || c == letter3) ? "3" : null;
            String wl = (c == letter2 || c == letter3) ? "L" :
                        (c == word2 || c == word3) ? "W" : null;
            if (td != null) {
              center(secondGraphc, td + wl, x,
                y + (letterHeight - sfh) * 4 / 10 + sfh, letterWidth);
            }
          }
        }
      }
      Color c = new Color(255, 255, 200);
      secondGraphc.setColor(c);
      secondGraphc.fillRect(leftMargin, topMargin + areaHeight - 3 * gapWidth, 7 * (letterWidth + gapWidth), letterHeight + 2 * gapWidth);

      Letter.resize(letterWidth, letterHeight);

     
      for (int i = 0; i < 7; i++) {
        if (tray[i] != null) {
          moveToTray(tray[i], i);
        }
      }
      paintScore();
    }
    return d;
  }
  
  /**
   * Call checksize to show string at center
   **/
  private void center(Graphics g, String s, int x, int y, int w) {
    x += (w - g.getFontMetrics().stringWidth(s)) / 2;
    g.drawString(s, x, y);
  }
  
  /** 
   * Show score
   **/
  private void paintScore() {
    int x = leftMargin + (letterWidth + gapWidth) * 7 + leftMargin;
    int y = topMargin + areaHeight - 3 * gapWidth;
    int h = letterHeight + 2 * gapWidth;
    Font font = new Font("TimesRoman", Font.PLAIN, h/2);
    secondGraphc.setFont(font);
    FontMetrics fm = secondGraphc.getFontMetrics();

    secondGraphc.setColor(Color.white);
    secondGraphc.fillRect(x, y, areaWidth, h);
    secondGraphc.setColor(Color.black);
    if (opponentName == null) {
      int yy = (h - fm.getHeight()) / 2 + fm.getAscent();
      secondGraphc.drawString("Score: " + myScore, x, y + yy);
    } else {
      h/=2;
      int yy = (h - fm.getHeight()) / 2 + fm.getAscent();
      secondGraphc.drawString(myName + ": " + myScore, x, y + yy);
      secondGraphc.drawString(opponentName + ": " + opponentScore, x, y + h + yy);
    }
  }
  
  private int xx, yy, ww, hh;

  /**
   * Check whether mouse on a letter
   **/
  private void selectLetter(int x, int y) {
    pick = LetterHit(x, y);
    if(pick != null) {
      offsetX = pick.x - x;
      offsetY = pick.y - y;
      oldPosition.x = pick.x;
      oldPosition.y = pick.y;
    }
    repaint();
  }
  
  /** 
   * Calculate the tile that letter going to put 
   **/
  private void dropLetter(int x, int y) {
    if(pick != null) {
      
      x += offsetX + letterWidth / 2;
      y += offsetY + letterHeight / 2;
      
      x = (x - leftMargin) / (letterWidth + gapWidth);
      y = (y - topMargin) / (letterHeight + gapWidth);
      
      moveLetter(pick, x, y);
  
      pick = null;
      repaint();
    }
  }
  
  /**
   * Calculate the difference between new position and current position
   **/
  private void dragLetter(int x, int y) {
    if (pick != null) {
      int ox = pick.x;
      int oy = pick.y;
      pick.move(x + offsetX, y + offsetY);
      xx = Math.min(ox, pick.x);
      yy = Math.min(oy, pick.y);
      ww = pick.width + Math.abs(ox - pick.x);
      hh = pick.height + Math.abs(oy - pick.y);
      paint(getGraphics());
    }
  }
  
  class MyMouseAdapter extends MouseAdapter {
    public void mousePressed(MouseEvent me) {
      selectLetter(me.getX(), me.getY());
    }
    public void mouseReleased(MouseEvent me) {
      dropLetter(me.getX(), me.getY());
    }
  }
  
  class MyMouseMotionAdapter extends MouseMotionAdapter {
    public synchronized void mouseDragged(MouseEvent me) {
      dragLetter(me.getX(), me.getY());
    }
  }
}