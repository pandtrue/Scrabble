package scrabble;

import java.awt.*;

/** 
 * Letter class for the game
 **/
class Letter {
  static int width, height; 
  private static Font font, smfont;
  private static int OffsetY, OffsetYS;
  private static int oldHeight = -1;
  static final int NORMAL = 0;
  static final int DIMMER = 1;
  static final int BRIGHTTER = 2;
  private static Color colors[][] = {mix(250, 220, 100), mix(200, 150, 80), mix(255, 230, 150)};
 
  /** 
   * Convert the color to brighter or dimmer
   **/
  private static Color mix(int r, int g, int b)[] {
    Color arr[] = new Color[3];
 
    arr[NORMAL] = new Color(r, g, b);
    arr[DIMMER] = gain(arr[0], .71);
    arr[BRIGHTTER] = gain(arr[0], 1.31);
    return arr;
  }
  
  /**
   * Check validate of the color after convert
   **/
  private static int clamp(double d) {
    return (d < 0) ? 0 : ((d > 255) ? 255 : (int) d);
  }
  
  /**
   * Increase or decrease the brightness of the color
   **/
  private static Color gain(Color c, double f) {
    return new Color(
      clamp(c.getRed() * f),
      clamp(c.getGreen() * f),
      clamp(c.getBlue() * f));
  }
  private boolean valid = false;
 
  // parameters to store the position of the letter
  private Point tile = null;
  int x, y;              
  private int symbolOffset;         
  private int symbolWidth;         
  private int pointOffset;        
  private int pointWidth;        
  private int gap = 1;    
  private String symbol;
  private int points;

  Letter(char s, int p) {
    symbol = "" + s;
    points = p;       // point of each letter
  }

  String getSymbol() {
    return symbol;
  }

  int getPoints() {
    return points;
  }

  /**
   * Called to place letter
   **/
  void move(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /** 
   *  if t is null, then this letter is available, otherwise, it has been used
   **/
  void remember(Point t) {
    if (t == null) {
      tile = t;
    } else {
      tile = new Point(t.x, t.y);
    }
  }

  Point recall() {
    return tile;
  }
  
  /**
   * Resize the letter
   **/
  static void resize(int symbolWidth, int h0) {
    width = symbolWidth;
    height = h0;
  }
  
  /** 
   * Check whether parameters are in range
   **/
  boolean hit(int xp, int yp) {
    return (xp >= x && xp < x + width && yp >= y && yp < y + height);
  }
  
  private int font_ascent;
  /** 
   * Change font, size and position
   **/
  void validate(Graphics g) {
    FontMetrics fm;
    if (height != oldHeight) {
      font = new Font("SansSerif", Font.BOLD, (int)(height * .6));
      g.setFont(font);
      fm = g.getFontMetrics();
      font_ascent = fm.getAscent();
 
      OffsetY = (height - font_ascent) * 4 / 10 + font_ascent;
 
      smfont = new Font("SansSerif", Font.BOLD, (int)(height * .3));
      g.setFont(smfont);
      fm = g.getFontMetrics();
      OffsetYS = OffsetY + fm.getAscent() / 2;
      oldHeight = height;
    }
    if (!valid) {
      valid = true;
      g.setFont(font);
      fm = g.getFontMetrics();
      symbolWidth = fm.stringWidth(symbol);
      g.setFont(smfont);
      fm = g.getFontMetrics();
      pointWidth = fm.stringWidth("" + points);
      int slop = width - (symbolWidth + gap + pointWidth);
      symbolOffset = slop / 2;
      if (symbolOffset < 1)
        symbolOffset = 1;
      pointOffset = symbolOffset + symbolWidth + gap;
      if (points > 9)
        pointOffset--;
    }
  }
  
  /**
   * Called by board to set brightness
   **/
  void paint(Graphics g, int i) {
    Color c[] = colors[i];
    validate(g);
    g.setColor(c[NORMAL]);
    g.fillRect(x, y, width, height);
    g.setColor(c[BRIGHTTER]);
    g.fillRect(x, y, width - 1, 1);
    g.fillRect(x, y + 1, 1, height - 2);
    g.setColor(Color.black);
    g.fillRect(x, y + height - 1, width, 1);
    g.fillRect(x + width - 1, y, 1, height - 1);
    g.setColor(c[DIMMER]);
    g.fillRect(x + 1, y + height - 2, width - 2, 1);
    g.fillRect(x + width - 2, y + 1, 1, height - 3);
    g.setColor(Color.black);
    if (points > 0) {
      g.setFont(font);
      g.drawString(symbol, x + symbolOffset, y + OffsetY);
      g.setFont(smfont);
      g.drawString("" + points, x + pointOffset, y + OffsetYS);
    }
  }
}