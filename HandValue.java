import java.util.Arrays;


/**
@author JJ Narang

A class that compares 2 hands based on a given score.
If a tie, then evaluates based on higher value in the category.
*/

public class HandValue implements Comparable<HandValue>{
  /**
  the category of the hand, from HIGH_CARD up to STRAIGHT_FLUSH
  */
  private final HandCategory category;

  /**
  the rank values used to break ties, listed from most to least important
  */
  private final int[] tiebreakers;

  /**
  @param category sets the category of the hand
  @param tiebreakers sets the rank values used to break ties
  creates a hand value with the specified category and tiebreakers
  */
  public HandValue(HandCategory category, int[] tiebreakers){
    this.category = category;
    this.tiebreakers = tiebreakers;
  }

  /**
  @return returns the category of the hand
  */
  public HandCategory getCategory(){
    return category;
  }

  /**
  @param other the hand value to compare against
  compares the category first, then each tiebreaker in order
  @return a positive number if this hand is better, a negative number
  if it is worse, or 0 if the two hands tie
  */
  @Override
  public int compareTo(HandValue other){
    int c = category.compareTo(other.category);
    if(c != 0){
      return c;
    }
    for(int i = 0; i < tiebreakers.length; i++){
      if(tiebreakers[i] != other.tiebreakers[i]){
        return Integer.compare(tiebreakers[i], other.tiebreakers[i]);
      }
    }
    return 0;
  }

  /**
  @return a readable format for the hand value, such as FLUSH [14, 11, 9, 6, 3]
  */
  @Override
  public String toString(){
    return category + " " + Arrays.toString(tiebreakers);
  }
}