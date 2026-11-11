/**
@author JJ Narang

An enum that classifies a card with a suit.
*/
public enum Suit {
  CLUBS("\u2663", false),
  DIAMONDS("\u2666", true),
  HEARTS("\u2665", true),
  SPADES("\u2660", false);
 
  private final String symbol;
  private final boolean red;
 
  Suit(String symbol, boolean red) {
    this.symbol = symbol;
    this.red = red;
  }
 
  public String getSymbol() {
    return symbol;
  }
 
  public boolean isRed() {
    return red;
  }
}