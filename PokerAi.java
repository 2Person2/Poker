import java.util.ArrayList;
import java.util.Random;

/**
A non-cheating opponent with random, sampled, and exhaustive river strategies.
The evaluator assumes every possible opponent hand is equally likely.
@author Brandon Mai
*/
public final class PokerAi {
    /**
    Available strategies. Hard is more accurate under a uniform-hand assumption.
    @author Brandon Mai
    */
    public enum Difficulty { EASY, MEDIUM, HARD }

    private final Random random;

    /**
    Uses its own random generator, separate from the real shuffled deck.
    @author Brandon Mai
    */
    public PokerAi(Random random) { this.random = random; }

    /**
    Chooses call or fold without receiving the human's cards.
    A free check always reaches showdown in this prototype.
    @author Brandon Mai
    */
    public boolean calls(AiView view, Difficulty difficulty) {
        if (view.callCost == 0) return true;
        if (difficulty == Difficulty.EASY) return random.nextBoolean();
        double equity = equity(view, difficulty);
        double potOdds = (double) view.callCost / (view.pot + view.callCost);
        return equity >= potOdds;
    }

    /**
    Estimates win share, including half credit for ties, from unseen cards.
    Medium samples 120 hands; Hard examines all 990 possible pairs on the river.
    @author Brandon Mai
    */
    public double equity(AiView view, Difficulty difficulty) {
        boolean[] visible = new boolean[52];
        for (Card card : view.ownCards) visible[card.id()] = true;
        for (Card card : view.board) visible[card.id()] = true;
        ArrayList<Card> unseen = new ArrayList<Card>();
        for (int suit = 0; suit < 4; suit++) {
            for (int rank = 2; rank <= 14; rank++) {
                Card card = new Card(rank, suit);
                if (!visible[card.id()]) unseen.add(card);
            }
        }
        ArrayList<Card> own = new ArrayList<Card>(view.board);
        own.addAll(view.ownCards);
        int score = HandEvaluator.best(own);
        double wins = 0;
        int trials = 0;
        if (difficulty == Difficulty.HARD) {
            for (int a = 0; a < unseen.size() - 1; a++) {
                for (int b = a + 1; b < unseen.size(); b++) {
                    wins += share(score, view, unseen.get(a), unseen.get(b));
                    trials++;
                }
            }
        } else {
            for (int i = 0; i < 120; i++) {
                int a = random.nextInt(unseen.size());
                int b = random.nextInt(unseen.size() - 1);
                if (b >= a) b++;
                wins += share(score, view, unseen.get(a), unseen.get(b));
                trials++;
            }
        }
        return wins / trials;
    }

    /**
    Compares one hypothetical opponent hand against the AI's hand.
    @author Brandon Mai
    */
    private double share(int ownScore, AiView view, Card first, Card second) {
        ArrayList<Card> opponent = new ArrayList<Card>(view.board);
        opponent.add(first);
        opponent.add(second);
        int score = HandEvaluator.best(opponent);
        return ownScore > score ? 1.0 : ownScore == score ? 0.5 : 0.0;
    }
}
