import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
Owns the private game state for one human-versus-AI river scenario.
The 20-chip opening pot is simulated; player bankrolls are not implemented.
@author Brandon Mai
*/
public final class RiverGame {
    private final Random deckRandom;
    private final PokerAi ai;
    private final RewardWallet wallet = new RewardWallet();
    private final ArrayList<String> history = new ArrayList<String>();
    private final Card[][] holeCards = new Card[2][2];
    private final ArrayList<Card> board = new ArrayList<Card>();
    private boolean complete = true;
    private boolean reveal;
    private int pot;
    private int handNumber;
    private String message = "Deal a river scenario to begin.";

    /**
    Keeps deck randomness separate from AI sampling randomness.
    @author Brandon Mai
    */
    public RiverGame(Random deckRandom, Random aiRandom) {
        this.deckRandom = deckRandom;
        ai = new PokerAi(aiRandom);
    }

    /**
    Starts a new scenario only after the prior scenario is resolved.
    @author Brandon Mai
    */
    public void deal() {
        if (!complete) throw new IllegalStateException("Finish the current hand first");
        Deck deck = new Deck(deckRandom);
        for (int player = 0; player < 2; player++) {
            for (int card = 0; card < 2; card++) holeCards[player][card] = deck.deal();
        }
        board.clear();
        for (int i = 0; i < 5; i++) board.add(deck.deal());
        pot = 20;
        handNumber++;
        complete = false;
        reveal = false;
        message = "Your turn: check, bet 20, or fold. Opening pot: 20 simulated chips.";
    }

    /**
    Resolves a single legal action and awards cosmetics exactly once.
    @author Brandon Mai
    */
    public void act(String action, PokerAi.Difficulty difficulty) {
        if (complete) throw new IllegalStateException("Deal a hand first");
        if (!action.equals("Check") && !action.equals("Bet 20") && !action.equals("Fold")) {
            throw new IllegalArgumentException("Unknown action");
        }
        int result;
        if (action.equals("Fold")) {
            result = -1;
            message = "You folded. AI wins the 20-chip pot. Opponent cards stay hidden.";
        } else {
            int cost = action.equals("Bet 20") ? 20 : 0;
            pot += cost;
            AiView view = new AiView(Arrays.asList(holeCards[1]), board, pot, cost);
            if (!ai.calls(view, difficulty)) {
                result = 1;
                message = "AI folded. You win the " + pot + "-chip pot. +10 cosmetic coins.";
            } else {
                pot += cost;
                reveal = true;
                int human = score(0), computer = score(1);
                result = Integer.compare(human, computer);
                message = (cost == 0 ? "Both check. " : "AI calls. ")
                    + (result > 0 ? "You win! +10 coins. " : result == 0 ? "Split pot. +3 coins. " : "AI wins. ")
                    + "You: " + HandEvaluator.name(human) + "; AI: " + HandEvaluator.name(computer) + ".";
            }
        }
        complete = true;
        wallet.award(result);
        history.add("Hand " + handNumber + " [" + difficulty + "] " + action + ": " + message);
        if (history.size() > 5) history.remove(0);
    }

    /**
    Combines one player's private cards with the shared board for showdown.
    @author Brandon Mai
    */
    private int score(int player) {
        ArrayList<Card> cards = new ArrayList<Card>(board);
        cards.addAll(Arrays.asList(holeCards[player]));
        return HandEvaluator.best(cards);
    }

    /**
    Returns human cards, or AI cards only after showdown.
    @author Brandon Mai
    */
    public List<Card> shownCards(int player) {
        if (player < 0 || player > 1) throw new IllegalArgumentException("Invalid player");
        if (handNumber == 0 || (player == 1 && !reveal)) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<Card>(Arrays.asList(holeCards[player])));
    }

    /**
    Returns a protected copy of the public board.
    @author Brandon Mai
    */
    public List<Card> board() { return Collections.unmodifiableList(new ArrayList<Card>(board)); }

    /**
    Returns whether another hand can be dealt.
    @author Brandon Mai
    */
    public boolean complete() { return complete; }

    /**
    Returns the current public pot.
    @author Brandon Mai
    */
    public int pot() { return pot; }

    /**
    Returns the latest action description.
    @author Brandon Mai
    */
    public String message() { return message; }

    /**
    Returns this session's cosmetic wallet.
    @author Brandon Mai
    */
    public RewardWallet wallet() { return wallet; }

    /**
    Returns a read-only recent history.
    @author Brandon Mai
    */
    public List<String> history() { return Collections.unmodifiableList(history); }
}
