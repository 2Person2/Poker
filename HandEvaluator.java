import java.util.ArrayList;

/**
@author Claude

A class that finds the best 5-card poker hand from a group of 5 to 7 cards
all methods are static, so no HandEvaluator object is ever created
*/
public class HandEvaluator{
  /**
  @param cards the cards to choose from, usually 2 hole cards plus the board
  tries every 5-card combination and keeps the strongest one
  @return returns the hand value of the best 5-card hand
  */
  public static HandValue evaluate(ArrayList<Card> cards){
    if(cards.size() < 5){
      throw new IllegalArgumentException("Need at least 5 cards, got " + cards.size());
    }

    HandValue best = null;
    int n = cards.size();

    for(int a = 0; a < n; a++){
      for(int b = a + 1; b < n; b++){
        for(int c = b + 1; c < n; c++){
          for(int d = c + 1; d < n; d++){
            for(int e = d + 1; e < n; e++){
              ArrayList<Card> five = new ArrayList<>();
              five.add(cards.get(a));
              five.add(cards.get(b));
              five.add(cards.get(c));
              five.add(cards.get(d));
              five.add(cards.get(e));

              HandValue value = scoreFive(five);
              if(best == null || value.compareTo(best) > 0){
                best = value;
              }
            }
          }
        }
      }
    }
    return best;
  }

  /**
  @param five exactly 5 cards
  figures out the category of the 5 cards and builds their tiebreakers
  @return returns the hand value of the 5 cards
  */
  private static HandValue scoreFive(ArrayList<Card> five){
    int[] counts = new int[15];
    for(Card card : five){
      counts[card.getRank().getValue()]++;
    }

    boolean flush = isFlush(five);
    int straightHigh = getStraightHigh(counts);
    int[] ordered = orderRanks(counts);
    int maxCount = counts[ordered[0]];
    int distinct = ordered.length;

    if(flush && straightHigh > 0){
      return new HandValue(HandCategory.STRAIGHT_FLUSH, new int[]{straightHigh});
    }
    if(maxCount == 4){
      return new HandValue(HandCategory.FOUR_OF_A_KIND, ordered);
    }
    if(maxCount == 3 && distinct == 2){
      return new HandValue(HandCategory.FULL_HOUSE, ordered);
    }
    if(flush){
      return new HandValue(HandCategory.FLUSH, ordered);
    }
    if(straightHigh > 0){
      return new HandValue(HandCategory.STRAIGHT, new int[]{straightHigh});
    }
    if(maxCount == 3){
      return new HandValue(HandCategory.THREE_OF_A_KIND, ordered);
    }
    if(maxCount == 2 && distinct == 3){
      return new HandValue(HandCategory.TWO_PAIR, ordered);
    }
    if(maxCount == 2){
      return new HandValue(HandCategory.ONE_PAIR, ordered);
    }
    return new HandValue(HandCategory.HIGH_CARD, ordered);
  }

  /**
  @param five exactly 5 cards
  checks whether every card has the same suit as the first card
  @return returns true if all 5 cards share one suit
  */
  private static boolean isFlush(ArrayList<Card> five){
    Suit suit = five.get(0).getSuit();
    for(Card card : five){
      if(card.getSuit() != suit){
        return false;
      }
    }
    return true;
  }

  /**
  @param counts how many cards of each rank value, indexed 2 through 14
  looks for 5 ranks in a row, counting A-2-3-4-5 as a straight with a high card of 5
  @return returns the high card of the straight, or 0 if there is no straight
  */
  private static int getStraightHigh(int[] counts){
    for(int high = 14; high >= 6; high--){
      if(counts[high] == 1 && counts[high - 1] == 1 && counts[high - 2] == 1
          && counts[high - 3] == 1 && counts[high - 4] == 1){
        return high;
      }
    }
    if(counts[14] == 1 && counts[2] == 1 && counts[3] == 1
        && counts[4] == 1 && counts[5] == 1){
      return 5;
    }
    return 0;
  }

  /**
  @param counts how many cards of each rank value, indexed 2 through 14
  lists each rank once, sorted by how many times it appears, then by rank, highest first
  for example, K K 7 7 9 becomes [13, 7, 9]
  @return returns the ordered ranks, which are the tiebreakers for most categories
  */
  private static int[] orderRanks(int[] counts){
    int distinct = 0;
    for(int rank = 2; rank <= 14; rank++){
      if(counts[rank] > 0){
        distinct++;
      }
    }

    int[] ordered = new int[distinct];
    int index = 0;
    for(int count = 4; count >= 1; count--){
      for(int rank = 14; rank >= 2; rank--){
        if(counts[rank] == count){
          ordered[index] = rank;
          index++;
        }
      }
    }
    return ordered;
  }
}