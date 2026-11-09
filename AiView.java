import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
A read-only snapshot containing only information the AI is allowed to know.
It has no reference to the opponent's cards, the game, or the real deck.
@author Brandon Mai
*/
public final class AiView {
    public final List<Card> ownCards;
    public final List<Card> board;
    public final int pot;
    public final int callCost;

    /**
    Copies visible cards, checking that this is a valid river decision.
    @author Brandon Mai
    */
    public AiView(List<Card> ownCards, List<Card> board, int pot, int callCost) {
        if (ownCards.size() != 2 || board.size() != 5 || pot <= 0 || callCost < 0) {
            throw new IllegalArgumentException("Invalid river view");
        }
        ArrayList<Card> combined = new ArrayList<Card>(ownCards);
        combined.addAll(board);
        HandEvaluator.best(combined);
        this.ownCards = Collections.unmodifiableList(new ArrayList<Card>(ownCards));
        this.board = Collections.unmodifiableList(new ArrayList<Card>(board));
        this.pot = pot;
        this.callCost = callCost;
    }
}
