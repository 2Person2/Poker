import java.util.List;

/**
Ranks five to seven cards by checking every five-card combination.
Scores compare category first and then all required kickers.
@author Brandon Mai
*/
public final class HandEvaluator {
    private static final String[] NAMES = {"High card", "Pair", "Two pair",
        "Three of a kind", "Straight", "Flush", "Full house",
        "Four of a kind", "Straight flush"};
    private static final int CATEGORY_UNIT = 759375; // 15 to the fifth power

    /**
    Prevents creation of this utility class.
    @author Brandon Mai
    */
    private HandEvaluator() { }

    /**
    Returns the strongest five-card score in a legal set of five to seven cards.
    @author Brandon Mai
    */
    public static int best(List<Card> cards) {
        if (cards.size() < 5 || cards.size() > 7) throw new IllegalArgumentException("Need 5-7 cards");
        boolean[] seen = new boolean[52];
        for (Card card : cards) {
            if (seen[card.id()]) throw new IllegalArgumentException("Duplicate card");
            seen[card.id()] = true;
        }
        int best = -1;
        for (int a = 0; a < cards.size() - 4; a++)
            for (int b = a + 1; b < cards.size() - 3; b++)
                for (int c = b + 1; c < cards.size() - 2; c++)
                    for (int d = c + 1; d < cards.size() - 1; d++)
                        for (int e = d + 1; e < cards.size(); e++)
                            best = Math.max(best, five(new Card[] {cards.get(a), cards.get(b),
                                cards.get(c), cards.get(d), cards.get(e)}));
        return best;
    }

    /**
    Scores one five-card hand using rank counts and descending tie breakers.
    @author Brandon Mai
    */
    private static int five(Card[] cards) {
        int[] counts = new int[15];
        boolean flush = true;
        for (Card card : cards) {
            counts[card.rank]++;
            if (card.suit != cards[0].suit) flush = false;
        }
        int straight = 0;
        for (int high = 14; high >= 6; high--) {
            boolean found = true;
            for (int rank = high - 4; rank <= high; rank++) if (counts[rank] == 0) found = false;
            if (found) { straight = high; break; }
        }
        if (straight == 0 && counts[14] > 0 && counts[2] > 0 && counts[3] > 0
                && counts[4] > 0 && counts[5] > 0) straight = 5;
        int four = 0, three = 0;
        int[] pairs = new int[2];
        int pairCount = 0;
        int[] singles = new int[5];
        int singleCount = 0;
        int[] descending = new int[5];
        int index = 0;
        for (int rank = 14; rank >= 2; rank--) {
            for (int i = 0; i < counts[rank]; i++) descending[index++] = rank;
            if (counts[rank] == 4) four = rank;
            if (counts[rank] == 3) three = rank;
            if (counts[rank] == 2) pairs[pairCount++] = rank;
            if (counts[rank] == 1) singles[singleCount++] = rank;
        }
        if (flush && straight > 0) return pack(8, straight);
        if (four > 0) return pack(7, four, singles[0]);
        if (three > 0 && pairCount > 0) return pack(6, three, pairs[0]);
        if (flush) return pack(5, descending);
        if (straight > 0) return pack(4, straight);
        if (three > 0) return pack(3, three, singles[0], singles[1]);
        if (pairCount == 2) return pack(2, pairs[0], pairs[1], singles[0]);
        if (pairCount == 1) return pack(1, pairs[0], singles[0], singles[1], singles[2]);
        return pack(0, descending);
    }

    /**
    Encodes a category and up to five kickers as base-15 digits.
    @author Brandon Mai
    */
    private static int pack(int category, int... ranks) {
        int result = category;
        for (int i = 0; i < 5; i++) result = result * 15 + (i < ranks.length ? ranks[i] : 0);
        return result;
    }

    /**
    Returns the name of a hand category from its score.
    @author Brandon Mai
    */
    public static String name(int score) { return NAMES[score / CATEGORY_UNIT]; }
}
