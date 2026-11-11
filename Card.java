/**
@author Aarnav Kumta
card class is used to create card objects that have a rank and a suit
*/
class Card{
   /**
   the rank is either A, 2, 3, 4, 5, 6, 7, 8, 9, 10, J, Q, or K
   */
   private final Rank rank;
   
   /**
   the suit is either clubs, spades, hearts, or diamonds
   */
   private final Suit suit;

   /**
   @param rank sets the rank of the card
   @param suit sets the suit of the card
   creates a card object with the specified rank and suit
   */
   public Card(Rank rank, Suit suit){
      this.rank = rank;
      this.suit = suit;
   }

   /**
   @return returns the rank of the card
   */
   public Rank getRank(){
      return rank;
   }

   /**
   @return returns the suit of the card
   */
   public Suit getSuit(){
      return suit;
   }

   /**
   @return a readable format for the card
   */
   @Override
   public String toString(){
      return rank.getLabel() + suit.getSymbol();
   }
}
