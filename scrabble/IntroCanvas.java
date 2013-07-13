package scrabble;

import java.awt.*;
import java.awt.event.*;

/**
 *  Canvas class, override the paint method to show the game name 
 **/
class IntroCanvas extends Canvas {
  private Color pink = new Color(255, 200, 200);
  private Color blue = new Color(150, 200, 255);
  private Color yellow = new Color(250, 220, 100);
  private Font nameFont, titleFont, bookFont;
  private int width, height;
  private int edgeNumber = 16;
  private static final String title = "Scrabble";
  
  IntroCanvas() {
    setBackground(yellow);
    titleFont = new Font("Serif", Font.BOLD,60);
    nameFont = new Font("Serif", Font.BOLD, 20);
    bookFont = new Font("Serif", Font.PLAIN, 25);
    addMouseListener(new MyMouseAdapter());
  }
  
  /**
   * Draw string based on the parameters
   **/
  private void draw(Graphics g, String s, Color c, Font f, int y, int off) {
    g.setFont(f);
    FontMetrics fm = g.getFontMetrics();
    g.setColor(c);
    g.drawString(s, (width - fm.stringWidth(s)) / 2 + off, y + off);
  }

  public void paint(Graphics g) {
    Dimension draw = getSize();
    width = draw.width;
    height = draw.height;
    g.setColor(blue);
    g.fill3DRect(edgeNumber, edgeNumber, width - 2 * edgeNumber, height - 2 * edgeNumber, true);
    draw(g, title, Color.black, titleFont, height / 2, 1);
    draw(g, title, Color.white, titleFont, height / 2, -1);
    draw(g, title, pink, titleFont, height / 2, 0);
  }
  
  /**
   * Adapter use by the single player mode
   **/
  class MyMouseAdapter extends MouseAdapter {
    public void mousePressed(MouseEvent me) {
      ((Frame)getParent()).setVisible(false);
    }
  }
}