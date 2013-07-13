package scrabble;

import java.util.Random;

/**
 * Bag class use to hold letters, here is a simple serialization
 **/
class Bag {
  private Random ran;
  private int counts[] = {2, 9, 3, 2, 4, 12, 2, 3, 1, 9, 1, 2, 4, 2, 8, 6, 2, 1, 5, 4, 6, 4, 1, 2, 1, 2, 1};
  private int points[] = {0, 1, 3, 3, 2, 1, 4, 2, 3, 1, 8, 5, 1, 3, 2, 1, 3, 10, 1, 2, 1, 1, 4, 4, 8, 4, 10};
  private Letter letters[] = new Letter[100];
  private int n = 0;
  
  /** 
   *  Create random instance based on the parameter passed in,
   *  then traversal counts to create new letter instance in correct number
   **/
  Bag(int seed) {
    ran = new Random(seed);
    for (int i = 0; i < counts.length; i++) {
      for (int j = 0; j < counts[i]; j++) {
        Letter l = new Letter(i == 0 ? '*' : (char)('A' + i - 1), points[i]);
        putBack(l);
      }
    }
  }
  
  /** 
   *  Create a random number between 0 and n-1 then pick letter based on this number
   **/
  synchronized Letter takeOut() {
    if (n == 0)
      return null;
    int i = (int)(ran.nextDouble() * n);
    Letter l = letters[i];
    if (i != n - 1)
      System.arraycopy(letters, i + 1, letters, i, n - i - 1);
    n--;
    return l;
  }
  
  /** 
   * Called to put letter into the bag
   **/
  synchronized void putBack(Letter l) {
    letters[n++] = l;
  }
}