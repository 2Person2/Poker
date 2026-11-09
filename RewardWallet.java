import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
Cosmetic inventory, independent equipment slots, and optional local progress saves.
Maps provide item lookup; a set prevents duplicate ownership; lists filter the shop.
@author Brandon Mai
*/
public final class RewardWallet {
    /**
    Independently customizable parts of the table.
    @author Brandon Mai
    */
    public enum Category {
        FACES("Card faces"), BACKS("Card backs"), FELT("Table felt"),
        RAILS("Table rails"), CHIPS("Chip sets"), BADGES("Player badges");
        public final String title;

        /**
        Stores the shop tab label.
        @author Brandon Mai
        */
        Category(String title) { this.title = title; }
    }

    /**
    Immutable catalog item with two colors and a drawing variation.
    @author Brandon Mai
    */
    public static final class Item {
        public final String name;
        public final Category category;
        public final int price;
        public final Color primary;
        public final Color secondary;
        public final int pattern;

        /**
        Creates a catalog entry. Names are unique stable save-file identifiers.
        @author Brandon Mai
        */
        private Item(String name, Category category, int price, int primary, int secondary, int pattern) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.primary = new Color(primary);
            this.secondary = new Color(secondary);
            this.pattern = pattern;
        }
    }

    private int coins;
    private final Map<String, Item> items = new LinkedHashMap<String, Item>();
    private final Set<String> owned = new HashSet<String>();
    private final Map<Category, String> equipment = new EnumMap<Category, String>(Category.class);
    private Path saveFile;
    private String saveStatus = "Practice profile: progress lasts for this session.";

    /**
    Builds 48 cosmetics: six free defaults and 42 purchasable options.
    Persistence is opt-in so model tests never touch the player's real save.
    @author Brandon Mai
    */
    public RewardWallet() {
        add("Classic", Category.FACES, 0, 0xF7F2E5, 0x303D4E, 0);
        add("Midnight", Category.FACES, 30, 0x22283E, 0xB5BBFF, 1);
        add("Sunset", Category.FACES, 60, 0xFFF0DD, 0xC65735, 2);
        add("Glacier", Category.FACES, 45, 0xE7F5FA, 0x256A8C, 0);
        add("Lavender", Category.FACES, 55, 0xF1E8FF, 0x8056AE, 1);
        add("Mint", Category.FACES, 65, 0xE9F7EB, 0x347C61, 2);
        add("Obsidian", Category.FACES, 100, 0x1C2028, 0xE6C583, 0);
        add("Rose Quartz", Category.FACES, 85, 0xFBE8EF, 0xA74D70, 1);
        add("Navy Weave", Category.BACKS, 0, 0x263D62, 0x9BB5D9, 0);
        add("Crimson Grid", Category.BACKS, 25, 0x792E42, 0xEFABB4, 0);
        add("Ocean Lines", Category.BACKS, 40, 0x1D566A, 0x85D4DF, 1);
        add("Golden Diamond", Category.BACKS, 80, 0x554322, 0xF1D08A, 2);
        add("Violet Stars", Category.BACKS, 70, 0x493766, 0xC6AFEE, 3);
        add("Forest Cross", Category.BACKS, 35, 0x284F46, 0xA2CFB4, 0);
        add("Coral Dots", Category.BACKS, 50, 0x8A493F, 0xFFD2B3, 3);
        add("Silver Chevron", Category.BACKS, 100, 0x414C5C, 0xD7E4F4, 2);
        add("Club Green", Category.FELT, 0, 0x16473E, 0x245E52, 0);
        add("Burgundy", Category.FELT, 40, 0x522D3B, 0x743F51, 0);
        add("Deep Ocean", Category.FELT, 50, 0x193E57, 0x285C74, 1);
        add("Royal Purple", Category.FELT, 60, 0x3D305A, 0x594775, 2);
        add("Slate", Category.FELT, 35, 0x354251, 0x495C70, 0);
        add("Emerald", Category.FELT, 80, 0x125644, 0x27816B, 1);
        add("Desert Sand", Category.FELT, 70, 0x706049, 0x9A8360, 2);
        add("Charcoal", Category.FELT, 100, 0x282B34, 0x414653, 1);
        add("Walnut", Category.RAILS, 0, 0x644D3D, 0xA78563, 0);
        add("Mahogany", Category.RAILS, 30, 0x633B36, 0xAC7260, 1);
        add("Brushed Steel", Category.RAILS, 60, 0x586675, 0xA7B8C6, 2);
        add("Rose Gold", Category.RAILS, 85, 0xAA7166, 0xF0B5A0, 1);
        add("Black Marble", Category.RAILS, 110, 0x303640, 0x9099A9, 2);
        add("Ivory", Category.RAILS, 90, 0xBAAF97, 0xF3E6CB, 0);
        add("Neon Violet", Category.RAILS, 120, 0x66478B, 0xD1A1FF, 2);
        add("Championship Gold", Category.RAILS, 150, 0x94723D, 0xF3D899, 1);
        add("Ruby Chips", Category.CHIPS, 0, 0xB6505F, 0xFFE1DF, 0);
        add("Sapphire Chips", Category.CHIPS, 25, 0x406EA3, 0xD4E9FF, 1);
        add("Emerald Chips", Category.CHIPS, 40, 0x38846C, 0xD3F5DF, 2);
        add("Violet Chips", Category.CHIPS, 55, 0x7A5CAB, 0xEEE0FF, 0);
        add("Onyx Chips", Category.CHIPS, 75, 0x38414C, 0xD5E1EA, 1);
        add("Pearl Chips", Category.CHIPS, 85, 0xB2B9C4, 0xFFFFFF, 2);
        add("Solar Chips", Category.CHIPS, 100, 0xB98A3C, 0xFFF1B9, 0);
        add("Coral Chips", Category.CHIPS, 45, 0xBE7966, 0xFFE5CE, 1);
        add("Rookie", Category.BADGES, 0, 0x54758A, 0xD6EBF7, 0);
        add("Ace Hunter", Category.BADGES, 25, 0x487B70, 0xD8FAE6, 1);
        add("Night Owl", Category.BADGES, 40, 0x665682, 0xE7DDFF, 2);
        add("Card Shark", Category.BADGES, 55, 0x376582, 0xCFEAFF, 3);
        add("Bluff Artist", Category.BADGES, 70, 0xA4676E, 0xFFE0E5, 1);
        add("High Roller", Category.BADGES, 95, 0x936E3D, 0xFFF0BA, 0);
        add("River Royal", Category.BADGES, 120, 0x78538B, 0xF3D6FF, 2);
        add("Grandmaster", Category.BADGES, 160, 0xA48343, 0xFFF4CE, 3);
    }

    /**
    Registers a cosmetic and equips the free default for its category.
    @author Brandon Mai
    */
    private void add(String name, Category category, int price, int first, int second, int pattern) {
        items.put(name, new Item(name, category, price, first, second, pattern));
        if (price == 0) { owned.add(name); equipment.put(category, name); }
    }

    /**
    Awards coins once per resolved hand, then saves if persistence is enabled.
    @author Brandon Mai
    */
    public void award(int result) {
        if (result < -1 || result > 1) throw new IllegalArgumentException("Invalid result");
        coins = Math.min(1000000, coins + (result > 0 ? 10 : result == 0 ? 3 : 0));
        save();
    }

    /**
    Purchases and equips an item in its own slot. Owned items equip for free.
    Unaffordable or unknown items leave the wallet unchanged.
    @author Brandon Mai
    */
    public boolean buyOrEquip(String name) {
        Item item = items.get(name);
        if (item == null) return false;
        if (!owned.contains(name)) {
            if (coins < item.price) return false;
            coins -= item.price;
            owned.add(name);
        }
        equipment.put(item.category, name);
        save();
        return true;
    }

    /**
    Returns cosmetic coins, separate from poker chips.
    @author Brandon Mai
    */
    public int coins() { return coins; }

    /**
    Preserves the original face-skin accessor for existing code.
    @author Brandon Mai
    */
    public String equipped() { return equipment.get(Category.FACES); }

    /**
    Returns the equipped item for one independent category.
    @author Brandon Mai
    */
    public Item equipped(Category category) { return items.get(equipment.get(category)); }

    /**
    Returns whether an item has already been unlocked.
    @author Brandon Mai
    */
    public boolean owns(String name) { return owned.contains(name); }

    /**
    Looks up a catalog entry by its unique name.
    @author Brandon Mai
    */
    public Item item(String name) { return items.get(name); }

    /**
    Returns the number of owned cosmetics, including six free defaults.
    @author Brandon Mai
    */
    public int ownedCount() { return owned.size(); }

    /**
    Builds the ordered list shown in one shop category.
    @author Brandon Mai
    */
    public List<Item> items(Category category) {
        ArrayList<Item> result = new ArrayList<Item>();
        for (Item item : items.values()) if (item.category == category) result.add(item);
        return Collections.unmodifiableList(result);
    }

    /**
    Returns all names and prices without exposing the mutable catalog.
    @author Brandon Mai
    */
    public Map<String, Integer> catalog() {
        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        for (Item item : items.values()) result.put(item.name, item.price);
        return Collections.unmodifiableMap(result);
    }

    /**
    Loads an optional local profile. A malformed file is preserved and saving is
    disabled for this session, so it is not silently overwritten with defaults.
    @author Brandon Mai
    */
    public void enablePersistence(Path file) {
        saveFile = file.toAbsolutePath();
        if (!Files.exists(saveFile)) { save(); return; }
        Properties data = new Properties();
        try (InputStream input = Files.newInputStream(saveFile)) {
            data.load(input);
            if (!"1".equals(data.getProperty("version"))) throw new IOException("Unknown save version");
            int loadedCoins = Integer.parseInt(data.getProperty("coins", "-1"));
            if (loadedCoins < 0 || loadedCoins > 1000000) throw new IOException("Invalid coins");
            Set<String> loadedOwned = new HashSet<String>();
            for (Item item : items.values()) {
                if (item.price == 0 || "true".equals(data.getProperty("owned." + item.name))) {
                    loadedOwned.add(item.name);
                }
            }
            Map<Category, String> loadedEquipment = new EnumMap<Category, String>(Category.class);
            for (Category category : Category.values()) {
                String name = data.getProperty("equipped." + category.name(), equipment.get(category));
                Item item = items.get(name);
                if (item == null || item.category != category || !loadedOwned.contains(name)) {
                    throw new IOException("Invalid equipment");
                }
                loadedEquipment.put(category, name);
            }
            coins = loadedCoins;
            owned.clear();
            owned.addAll(loadedOwned);
            equipment.putAll(loadedEquipment);
            saveStatus = "Progress saved on this computer.";
        } catch (IOException | IllegalArgumentException error) {
            saveFile = null;
            saveStatus = "Could not read save. Session-only progress; existing file kept.";
        }
    }

    /**
    Reports whether progress is persistent or a file operation failed.
    @author Brandon Mai
    */
    public String saveStatus() { return saveStatus; }

    /**
    Writes through a temporary sibling file, using atomic replacement if supported.
    Save failures keep the in-memory rewards usable and display a visible warning.
    @author Brandon Mai
    */
    private void save() {
        if (saveFile == null) return;
        Path temporary = null;
        try {
            Files.createDirectories(saveFile.getParent());
            Properties data = new Properties();
            data.setProperty("version", "1");
            data.setProperty("coins", "" + coins);
            for (String name : owned) data.setProperty("owned." + name, "true");
            for (Category category : Category.values()) {
                data.setProperty("equipped." + category.name(), equipment.get(category));
            }
            temporary = Files.createTempFile(saveFile.getParent(), "profile-", ".tmp");
            try (OutputStream output = Files.newOutputStream(temporary)) { data.store(output, "River Room cosmetics"); }
            try {
                Files.move(temporary, saveFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, saveFile, StandardCopyOption.REPLACE_EXISTING);
            }
            saveStatus = "Progress saved on this computer.";
        } catch (IOException error) {
            saveStatus = "Save failed. Progress is in memory; check folder permissions.";
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); } catch (IOException ignored) { /* Retry on next save. */ }
            }
        }
    }
}
