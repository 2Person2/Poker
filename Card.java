/**
An immutable playing card. Suits are clubs, diamonds, hearts, spades (0-3).
@author Brandon Mai
*/
public final class Card {
    public final int rank;
    public final int suit;

    /**
    Creates a card with rank 2-14, where 14 is an ace.
    @author Brandon Mai
    */
    public Card(int rank, int suit) {
        if (rank < 2 || rank > 14 || suit < 0 || suit > 3) {
            throw new IllegalArgumentException("Invalid card");
        }
        this.rank = rank;
        this.suit = suit;
    }

    /**
    Returns the unique index used by the deck and AI.
    @author Brandon Mai
    */
    public int id() { return suit * 13 + rank - 2; }

    /**
    Returns a short label using standard keyboard characters.
    @author Brandon Mai
    */
    public String toString() {
        String value = rank <= 10 ? "" + rank : "JQKA".substring(rank - 11, rank - 10);
        return value + "CDHS".charAt(suit);
    }
}
