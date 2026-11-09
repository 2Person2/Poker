import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Collections;
import java.util.Set;

/**
Session-only cosmetic rewards, kept separate from the simulated poker pot.
@author Brandon Mai
*/
public final class RewardWallet {
    private int coins;
    private String equipped = "Classic";
    private final Set<String> owned = new HashSet<String>();
    private final Map<String, Integer> prices = new LinkedHashMap<String, Integer>();

    /**
    Creates a free starter skin and two earnable skins.
    @author Brandon Mai
    */
    public RewardWallet() {
        owned.add("Classic");
        prices.put("Classic", 0);
        prices.put("Midnight", 30);
        prices.put("Sunset", 60);
    }

    /**
    Awards 10 coins for a win, 3 for a tie, and zero for a loss.
    The controller calls this once per resolved hand.
    @author Brandon Mai
    */
    public void award(int result) {
        if (result < -1 || result > 1) throw new IllegalArgumentException("Invalid result");
        coins += result > 0 ? 10 : result == 0 ? 3 : 0;
    }

    /**
    Buys and equips a skin, or equips an owned skin without charging again.
    @author Brandon Mai
    */
    public boolean buyOrEquip(String skin) {
        Integer price = prices.get(skin);
        if (price == null) return false;
        if (!owned.contains(skin)) {
            if (coins < price) return false;
            coins -= price;
            owned.add(skin);
        }
        equipped = skin;
        return true;
    }

    /**
    Returns current cosmetic coins.
    @author Brandon Mai
    */
    public int coins() { return coins; }

    /**
    Returns the current skin.
    @author Brandon Mai
    */
    public String equipped() { return equipped; }

    /**
    Returns a read-only shop catalog in display order.
    @author Brandon Mai
    */
    public Map<String, Integer> catalog() { return Collections.unmodifiableMap(prices); }
}
