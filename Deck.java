import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
A shuffled deck that removes each dealt card from an ArrayList.
@author Brandon Mai
*/
public final class Deck {
    private final ArrayList<Card> cards = new ArrayList<Card>();

    /**
    Builds and shuffles all 52 cards using the supplied random generator.
    @author Brandon Mai
    */
    public Deck(Random random) {
        for (int suit = 0; suit < 4; suit++) {
            for (int rank = 2; rank <= 14; rank++) cards.add(new Card(rank, suit));
        }
        Collections.shuffle(cards, random);
    }

    /**
    Removes the last card in constant time.
    @author Brandon Mai
    */
    public Card deal() {
        if (cards.isEmpty()) throw new IllegalStateException("Deck is empty");
        return cards.remove(cards.size() - 1);
    }
}
