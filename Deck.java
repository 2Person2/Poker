/**
@author Aarnav Kumta
deck object which holds all 52 cards
*/
class Deck{
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
      return cards.remove(cards.size() - 1);
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
