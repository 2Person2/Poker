import java.util.ArrayList;
import java.util.Collections;

/**
@author Aarnav Kumta
deck object which holds all 52 cards
*/
public class Deck{
  /**
  arraylist holding all cards in the deck
  */
   private final ArrayList<Card> cards;
  
   /**
   puts 52 unique cards in the arraylist
   */
   public Deck(){
      cards = new ArrayList<>();
      
      for(Suit suit : Suit.values()){
         for(Rank rank : Rank.values()){
            cards.add(new Card(rank, suit));
         }
      }
   }
  
   /**
   randomizes the order of the arraylist holding all the cards in the deck
   */
   public void shuffle(){
      Collections.shuffle(cards);
   }
   /**
   removes a card from the end of the arraylist and returns that card
   @return a card object
   */
   public Card deal(){
      if(cards.size() == 0){
        System.out.println("Error, no cards left in the deck"); // for debugging purposes, won't actually happen in a game of poker
      }
      return cards.remove(cards.size() - 1);
   }

   /**
   restores the entire deck to its 52 cards
   */
   public void reset(){
     cards.clear();
     for(Suit suit : Suit.values()){
         for(Rank rank : Rank.values()){
            cards.add(new Card(rank, suit));
         }
      }
   }
  
   /**
   prints out each card in the deck in a readable format
   */
   public void printDeck(){
      for(Card c : cards){
         System.out.println(c);
      }
   }
}
