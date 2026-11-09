import acm.graphics.GCanvas;
import acm.graphics.GLabel;
import acm.graphics.GLine;
import acm.graphics.GOval;
import acm.graphics.GRect;
import acm.graphics.GRoundRect;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
ACM-drawn home, poker table, and cosmetics shop with canvas-native controls.
The layout scales using the canvas width and height. No extra UI dependencies.
@author Brandon Mai
*/
public final class PokerPrototype extends MouseAdapter
        implements ActionListener, KeyListener, ComponentListener {
    private static final Color BG = new Color(0x101823);
    private static final Color PANEL = new Color(0x1A2533);
    private static final Color BORDER = new Color(0x344354);
    private static final Color TEXT = new Color(0xF2EBDD);
    private static final Color MUTED = new Color(0xA6B4C2);
    private static final Color GOLD = new Color(0xE6C18A);
    private final RiverGame game = new RiverGame(new Random(), new Random());
    private final GCanvas canvas = new GCanvas();
    private final ArrayList<Hit> buttons = new ArrayList<Hit>();
    private PokerAi.Difficulty difficulty = PokerAi.Difficulty.EASY;
    private String screen = "Home";
    private RewardWallet.Category category = RewardWallet.Category.FACES;
    private String selected = "Classic";
    private String notice = "Select an item to preview it. Buying also equips it.";
    private int page;
    private String hover = "";
    private String focus = "";
    private double scale = 1, offsetX, offsetY;

    /**
    One canvas control, recorded in logical coordinates for mouse and keyboard use.
    @author Brandon Mai
    */
    private static final class Hit {
        private final Rectangle2D bounds;
        private final String action;
        private final boolean enabled;

        /**
        Stores a control's click area and action.
        @author Brandon Mai
        */
        private Hit(double x, double y, double w, double h, String action, boolean enabled) {
            bounds = new Rectangle2D.Double(x, y, w, h);
            this.action = action;
            this.enabled = enabled;
        }
    }

    /**
    Opens the desktop app and loads this computer's cosmetic progress.
    @author Brandon Mai
    */
    public PokerPrototype() { this(true); }

    /**
    Supports isolated offscreen tests without reading or writing real player saves.
    @author Brandon Mai
    */
    PokerPrototype(boolean openWindow) {
        canvas.setSize(1120, 760);
        canvas.setPreferredSize(new Dimension(1120, 760));
        canvas.setBackground(BG);
        canvas.setFocusable(true);
        canvas.setFocusTraversalKeysEnabled(false);
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addKeyListener(this);
        canvas.addComponentListener(this);
        if (openWindow) game.wallet().enablePersistence(Paths.get("saves", "profile.properties"));
        redraw();
        if (!openWindow) return;
        JFrame frame = new JFrame("River Room | Brandon Mai");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(canvas, BorderLayout.CENTER);
        frame.pack();
        frame.setMinimumSize(new Dimension(900, 650));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        canvas.requestFocusInWindow();
    }

    /**
    Routes navigation, selection, purchases, and legal poker actions.
    @author Brandon Mai
    */
    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("Home") || action.equals("Table") || action.equals("Shop")) {
            screen = action;
            focus = "";
        } else if (action.startsWith("category:")) {
            category = RewardWallet.Category.valueOf(action.substring(9));
            selected = game.wallet().equipped(category).name;
            page = game.wallet().items(category).indexOf(game.wallet().item(selected)) / 6;
        } else if (action.startsWith("select:")) {
            selected = action.substring(7);
            notice = "Preview only. Equip or buy to apply this item to your table.";
        } else if (action.startsWith("difficulty:")) {
            if (game.complete()) difficulty = PokerAi.Difficulty.valueOf(action.substring(11));
        } else if (action.equals("Next")) page = 1;
        else if (action.equals("Previous")) page = 0;
        else if (action.equals("Purchase")) {
            RewardWallet.Item item = game.wallet().item(selected);
            boolean alreadyOwned = game.wallet().owns(selected);
            notice = game.wallet().buyOrEquip(selected)
                ? selected + (alreadyOwned ? " equipped. No coins charged." : " purchased and equipped.")
                : "You need " + (item.price - game.wallet().coins()) + " more coins for " + selected + ".";
        } else if (action.equals("Deal")) {
            if (game.complete()) game.deal();
        } else if (action.equals("Check") || action.equals("Bet 20") || action.equals("Fold")) {
            if (!game.complete()) game.act(action, difficulty);
        }
        hover = "";
        redraw();
    }

    /**
    Scales the design to the current canvas and draws the active screen.
    @author Brandon Mai
    */
    private void redraw() {
        scale = Math.min(canvas.getWidth() / 1120.0, canvas.getHeight() / 760.0);
        if (scale <= 0) scale = 1;
        offsetX = (canvas.getWidth() - 1120 * scale) / 2;
        offsetY = (canvas.getHeight() - 760 * scale) / 2;
        canvas.setAutoRepaintFlag(false);
        canvas.removeAll();
        buttons.clear();
        rect(0, 0, 1120, 760, BG, BG, false);
        header();
        if (screen.equals("Home")) home();
        else if (screen.equals("Shop")) shop();
        else table();
        line(30, 712, 1090, 712, BORDER);
        label(game.wallet().saveStatus(), 32, 737, 12, MUTED);
        label("TAB to focus  /  ENTER to select  /  ESC for home", 765, 737, 11, MUTED);
        canvas.setAutoRepaintFlag(true);
        canvas.repaint();
    }

    /**
    Draws consistent canvas-based navigation and the cosmetic coin balance.
    @author Brandon Mai
    */
    private void header() {
        label("RIVER ROOM", 32, 45, 22, GOLD);
        label("POKER  /  MAKE IT YOUR TABLE", 33, 64, 9, MUTED);
        button("Home", "Home", 420, 22, 95, 40, true, screen.equals("Home"));
        button("Table", "Table", 527, 22, 95, 40, true, screen.equals("Table"));
        button("Shop", "Shop", 634, 22, 95, 40, true, screen.equals("Shop"));
        oval(929, 30, 24, 24, GOLD, GOLD);
        centered("C", 941, 47, 13, BG);
        label("" + game.wallet().coins() + " coins", 965, 48, 16, TEXT);
        line(30, 84, 1090, 84, BORDER);
    }

    /**
    Draws the start screen and a preview of the player's equipped collection.
    @author Brandon Mai
    */
    private void home() {
        label("A SEAT AT YOUR OWN TABLE", 44, 154, 12, GOLD);
        label("Play the river.", 42, 215, 43, TEXT);
        label("Build your style.", 42, 267, 43, TEXT);
        label("A quick hand of Hold'em. A collection that grows with you.", 45, 311, 15, MUTED);
        button(game.complete() ? "Take a seat" : "Resume hand", "Table", 44, 351, 208, 54, true, true);
        button("Visit the shop", "Shop", 268, 351, 208, 54, true, false);
        label("YOUR COLLECTION", 46, 464, 12, GOLD);
        label(game.wallet().ownedCount() + " / 48 cosmetics unlocked", 46, 495, 22, TEXT);
        label("6 categories. Mix card faces, backs, felt, rails, chips and badges.", 46, 524, 13, MUTED);
        label("Wins +10 coins. Ties +3. Every upgrade is cosmetic.", 46, 550, 13, MUTED);
        preview(590, 144, 490, 380, null);
        label("YOUR CURRENT LOOK", 600, 559, 12, GOLD);
        label(game.wallet().equipped(RewardWallet.Category.FELT).name + " felt / "
            + game.wallet().equipped(RewardWallet.Category.BACKS).name, 600, 586, 15, TEXT);
        rect(32, 616, 1056, 65, PANEL, BORDER, true);
        label("FIRST DELIVERABLE", 50, 642, 11, GOLD);
        label("One river decision versus AI. Full betting rounds and friend play are still planned.", 50, 665, 14, MUTED);
    }

    /**
    Draws category tabs, a paged catalog, ownership states, and a live item preview.
    @author Brandon Mai
    */
    private void shop() {
        label("THE COLLECTION", 32, 130, 27, TEXT);
        label("48 ways to make it yours. Preview freely, then buy or equip.", 32, 156, 14, MUTED);
        int index = 0;
        for (RewardWallet.Category tab : RewardWallet.Category.values()) {
            button(tab.title, "category:" + tab.name(), 32 + index * 178, 178, 166, 38, true, tab == category);
            index++;
        }
        List<RewardWallet.Item> catalog = game.wallet().items(category);
        for (int i = page * 6; i < Math.min(catalog.size(), page * 6 + 6); i++) {
            RewardWallet.Item item = catalog.get(i);
            int slot = i - page * 6;
            double x = 32 + (slot % 3) * 238;
            double y = 238 + (slot / 3) * 177;
            boolean chosen = item.name.equals(selected);
            String action = "select:" + item.name;
            rect(x, y, 222, 161, chosen ? new Color(0x293442) : PANEL,
                chosen || action.equals(hover) || action.equals(focus) ? GOLD : BORDER, true);
            buttons.add(new Hit(x, y, 222, 161, action, true));
            swatch(item, x + 63, y + 13, 96, 78);
            fit(item.name, x + 14, y + 119, 16, 195, TEXT);
            boolean equipped = game.wallet().equipped(category).name.equals(item.name);
            label(equipped ? "EQUIPPED" : game.wallet().owns(item.name) ? "OWNED" : item.price + " coins",
                x + 14, y + 144, 12, equipped ? GOLD : MUTED);
        }
        button("< Previous", "Previous", 32, 610, 136, 38, page > 0, false);
        label("Page " + (page + 1) + " / 2", 194, 635, 13, MUTED);
        button("Next >", "Next", 584, 610, 146, 38, page == 0, false);
        RewardWallet.Item item = game.wallet().item(selected);
        rect(762, 238, 326, 410, PANEL, BORDER, true);
        label("LIVE PREVIEW", 782, 265, 11, GOLD);
        preview(777, 281, 296, 188, item);
        fit(item.name, 783, 503, 22, 285, TEXT);
        label(category.title + " / " + (game.wallet().owns(item.name) ? "In your collection" : item.price + " coins"),
            784, 532, 13, MUTED);
        boolean owned = game.wallet().owns(item.name);
        boolean equipped = game.wallet().equipped(category).name.equals(item.name);
        boolean affordable = owned || game.wallet().coins() >= item.price;
        String buyText = equipped ? "Equipped" : owned ? "Equip item" : "Buy & equip - " + item.price;
        button(buyText, "Purchase", 782, 553, 286, 46, affordable && !equipped, true);
        label(!affordable ? "Earn " + (item.price - game.wallet().coins()) + " more coins to unlock."
            : "Only this category changes when equipped.", 783, 624, 12, MUTED);
        fit(notice, 32, 687, 14, 1056, GOLD);
    }

    /**
    Draws the poker table with independent cosmetics and all controls on the canvas.
    @author Brandon Mai
    */
    private void table() {
        label("RIVER TABLE", 32, 125, 22, TEXT);
        int index = 0;
        for (PokerAi.Difficulty level : PokerAi.Difficulty.values()) {
            button(level.name(), "difficulty:" + level.name(), 490 + index * 115, 99, 105, 34,
                game.complete(), level == difficulty);
            index++;
        }
        label("Difficulty locks during a hand", 855, 121, 11, MUTED);
        RewardWallet.Item rail = game.wallet().equipped(RewardWallet.Category.RAILS);
        RewardWallet.Item felt = game.wallet().equipped(RewardWallet.Category.FELT);
        oval(58, 154, 1004, 377, rail.primary, rail.secondary);
        oval(75, 169, 970, 347, felt.primary, felt.secondary);
        if (felt.pattern > 0) oval(112, 190, 896, 306, null, felt.secondary);
        if (rail.pattern > 0) oval(64, 160, 992, 365, null, rail.secondary);
        if (rail.pattern == 2) oval(69, 165, 982, 355, null, rail.secondary);
        label("OPPONENT", 159, 203, 11, TEXT);
        drawHand(game.shownCards(1), 116, 220, 2, 64, 90);
        centered("COMMUNITY CARDS", 651, 209, 11, TEXT);
        drawHand(game.board(), 432, 228, 5, 78, 108);
        chipStack(520, 359, 42, game.wallet().equipped(RewardWallet.Category.CHIPS));
        label("POT  " + game.pot(), 584, 390, 22, TEXT);
        label("simulated poker chips", 584, 410, 11, TEXT);
        RewardWallet.Item badge = game.wallet().equipped(RewardWallet.Category.BADGES);
        badge(122, 361, 48, badge);
        label(badge.name, 182, 391, 15, TEXT);
        drawHand(game.shownCards(0), 121, 419, 2, 65, 90);
        label("YOUR CARDS", 296, 475, 12, TEXT);
        label("AI sees its own cards and the public board only.", 554, 472, 12, TEXT);
        fit(game.message(), 36, 558, 15, 1048, TEXT);
        button("Deal next hand", "Deal", 32, 581, 234, 49, game.complete(), true);
        button("Check", "Check", 285, 581, 198, 49, !game.complete(), false);
        button("Bet 20", "Bet 20", 502, 581, 198, 49, !game.complete(), true);
        button("Fold", "Fold", 719, 581, 164, 49, !game.complete(), false);
        button("Shop", "Shop", 902, 581, 186, 49, true, false);
        label("RECENT HAND", 34, 661, 10, GOLD);
        List<String> history = game.history();
        fit(history.isEmpty() ? "Deal a hand to begin. Wins +10 coins; ties +3."
            : history.get(history.size() - 1), 34, 685, 13, 1052, MUTED);
    }

    /**
    Resolves an item for a preview without mutating purchased equipment.
    @author Brandon Mai
    */
    private RewardWallet.Item look(RewardWallet.Category slot, RewardWallet.Item candidate) {
        return candidate != null && candidate.category == slot ? candidate : game.wallet().equipped(slot);
    }

    /**
    Draws a miniature table showing the selected item combined with equipped items.
    @author Brandon Mai
    */
    private void preview(double x, double y, double w, double h, RewardWallet.Item candidate) {
        RewardWallet.Item rail = look(RewardWallet.Category.RAILS, candidate);
        RewardWallet.Item felt = look(RewardWallet.Category.FELT, candidate);
        oval(x, y, w, h, rail.primary, rail.secondary);
        oval(x + 9, y + 9, w - 18, h - 18, felt.primary, felt.secondary);
        if (rail.pattern > 0) oval(x + 4, y + 4, w - 8, h - 8, null, rail.secondary);
        if (rail.pattern == 2) oval(x + 7, y + 7, w - 14, h - 14, null, rail.secondary);
        if (felt.pattern > 0) oval(x + 18, y + 18, w - 36, h - 36, null, felt.secondary);
        if (felt.pattern == 2) line(x + w * .22, y + h * .5, x + w * .78, y + h * .5, felt.secondary);
        double cw = w * .18, ch = h * .42;
        card(new Card(14, 3), x + w * .18, y + h * .22, cw, ch,
            look(RewardWallet.Category.FACES, candidate), look(RewardWallet.Category.BACKS, candidate));
        card(new Card(13, 2), x + w * .39, y + h * .22, cw, ch,
            look(RewardWallet.Category.FACES, candidate), look(RewardWallet.Category.BACKS, candidate));
        card(null, x + w * .60, y + h * .22, cw, ch,
            look(RewardWallet.Category.FACES, candidate), look(RewardWallet.Category.BACKS, candidate));
        chipStack(x + w * .56, y + h * .73, w * .08, look(RewardWallet.Category.CHIPS, candidate));
        badge(x + w * .35, y + h * .74, w * .085, look(RewardWallet.Category.BADGES, candidate));
    }

    /**
    Draws a catalog item's own colors and motif, even before it is purchased.
    @author Brandon Mai
    */
    private void swatch(RewardWallet.Item item, double x, double y, double w, double h) {
        if (item.category == RewardWallet.Category.FACES || item.category == RewardWallet.Category.BACKS) {
            card(item.category == RewardWallet.Category.FACES ? new Card(14, 3) : null,
                x + 21, y, w - 42, h, item, item);
        } else if (item.category == RewardWallet.Category.CHIPS) chipStack(x + 20, y + 18, 54, item);
        else if (item.category == RewardWallet.Category.BADGES) badge(x + 17, y + 7, 62, item);
        else {
            oval(x, y + 10, w, h - 20, item.primary, item.secondary);
            if (item.category == RewardWallet.Category.RAILS) {
                oval(x + 8, y + 18, w - 16, h - 36, BG, item.secondary);
            }
            if (item.pattern > 0) oval(x + 4, y + 14, w - 8, h - 28, null, item.secondary);
            if (item.pattern == 2) line(x + 22, y + h / 2, x + w - 22, y + h / 2, item.secondary);
        }
    }

    /**
    Draws the cards returned by the game or anonymous backs, never hidden values.
    @author Brandon Mai
    */
    private void drawHand(List<Card> hand, double x, double y, int slots, double w, double h) {
        for (int i = 0; i < slots; i++) {
            card(i < hand.size() ? hand.get(i) : null, x + i * (w + 12), y, w, h,
                game.wallet().equipped(RewardWallet.Category.FACES), game.wallet().equipped(RewardWallet.Category.BACKS));
        }
    }

    /**
    Draws a face style or one of four back motifs using ACM primitives only.
    @author Brandon Mai
    */
    private void card(Card value, double x, double y, double w, double h,
            RewardWallet.Item face, RewardWallet.Item back) {
        RewardWallet.Item style = value == null ? back : face;
        rect(x + 2, y + 3, w, h, new Color(0x0C1820), new Color(0x0C1820), true);
        rect(x, y, w, h, style.primary, style.secondary, true);
        if (value != null) {
            Color ink = face.primary.getRed() + face.primary.getGreen() + face.primary.getBlue() < 250
                ? new Color(0xF0E3C5) : new Color(0x253447);
            if (value.suit == 1 || value.suit == 2) {
                ink = face.primary.getRed() + face.primary.getGreen() + face.primary.getBlue() < 250
                    ? new Color(0xFFADB6) : new Color(0xAA3F54);
            }
            label(value.toString(), x + w * .12, y + h * .28, w * .23, ink);
            centered("CDHS".substring(value.suit, value.suit + 1), x + w * .52, y + h * .69, w * .38, style.secondary);
            if (face.pattern == 1) line(x + 8, y + h - 14, x + w - 8, y + h - 14, style.secondary);
            if (face.pattern == 2) oval(x + w / 2 - 3, y + h - 13, 6, 6, style.secondary, style.secondary);
        } else {
            rect(x + 6, y + 6, w - 12, h - 12, null, back.secondary, true);
            if (back.pattern == 0) {
                for (int i = 1; i < 4; i++) {
                    line(x + 10, y + h * i / 4, x + w - 10, y + h * i / 4, back.secondary);
                    line(x + w * i / 4, y + 10, x + w * i / 4, y + h - 10, back.secondary);
                }
            } else if (back.pattern == 1) {
                for (int i = 1; i < 5; i++) line(x + 10, y + h * i / 6, x + w - 10, y + h * (i + 1) / 6, back.secondary);
            } else if (back.pattern == 2) {
                line(x + w / 2, y + 15, x + w - 12, y + h / 2, back.secondary);
                line(x + w - 12, y + h / 2, x + w / 2, y + h - 15, back.secondary);
                line(x + w / 2, y + h - 15, x + 12, y + h / 2, back.secondary);
                line(x + 12, y + h / 2, x + w / 2, y + 15, back.secondary);
            } else {
                for (int i = 1; i < 4; i++) for (int j = 1; j < 5; j++) {
                    oval(x + w * i / 4 - 2, y + h * j / 5 - 2, 4, 4, back.secondary, back.secondary);
                }
            }
        }
    }

    /**
    Draws chip colors, stack edges, and a distinct center motif.
    @author Brandon Mai
    */
    private void chipStack(double x, double y, double size, RewardWallet.Item item) {
        for (int i = 2; i >= 0; i--) oval(x, y + i * 5, size, size * .65, item.primary, item.secondary);
        oval(x + 7, y + 5, size - 14, size * .65 - 10, null, item.secondary);
        centered(item.pattern == 0 ? "C" : item.pattern == 1 ? "+" : "=",
            x + size / 2, y + size * .45, size * .24, item.secondary);
    }

    /**
    Draws the player's chosen badge with a visible emblem variation.
    @author Brandon Mai
    */
    private void badge(double x, double y, double size, RewardWallet.Item item) {
        oval(x, y, size, size, item.primary, item.secondary);
        oval(x + 5, y + 5, size - 10, size - 10, null, item.secondary);
        String[] marks = {"R", "A", "K", "S"};
        centered(marks[item.pattern], x + size / 2, y + size * .67, size * .43, item.secondary);
    }

    /**
    Adds an accessible keyboard-targetable canvas button and its hit rectangle.
    @author Brandon Mai
    */
    private void button(String text, String action, double x, double y, double w, double h,
            boolean enabled, boolean accent) {
        boolean active = action.equals(hover) || action.equals(focus);
        Color fill = !enabled ? new Color(0x23303B) : accent ? GOLD : active ? new Color(0x354354) : PANEL;
        Color ink = !enabled ? new Color(0x83909C) : accent ? BG : TEXT;
        rect(x, y, w, h, fill, active && enabled ? TEXT : accent && enabled ? GOLD : BORDER, true);
        centered(text, x + w / 2, y + h / 2 + 5, 13, ink);
        buttons.add(new Hit(x, y, w, h, action, enabled));
    }

    /**
    Draws a rectangle using the current responsive layout transform.
    @author Brandon Mai
    */
    private void rect(double x, double y, double w, double h, Color fill, Color border, boolean rounded) {
        GRect shape = rounded ? new GRoundRect(offsetX + x * scale, offsetY + y * scale, w * scale, h * scale, 12 * scale)
            : new GRect(offsetX + x * scale, offsetY + y * scale, w * scale, h * scale);
        shape.setColor(border);
        shape.setFilled(fill != null);
        if (fill != null) shape.setFillColor(fill);
        canvas.add(shape);
    }

    /**
    Draws an oval, optionally as an outline.
    @author Brandon Mai
    */
    private void oval(double x, double y, double w, double h, Color fill, Color border) {
        GOval shape = new GOval(offsetX + x * scale, offsetY + y * scale, w * scale, h * scale);
        shape.setColor(border);
        shape.setFilled(fill != null);
        if (fill != null) shape.setFillColor(fill);
        canvas.add(shape);
    }

    /**
    Draws a scaled decorative line.
    @author Brandon Mai
    */
    private void line(double x1, double y1, double x2, double y2, Color color) {
        GLine line = new GLine(offsetX + x1 * scale, offsetY + y1 * scale, offsetX + x2 * scale, offsetY + y2 * scale);
        line.setColor(color);
        canvas.add(line);
    }

    /**
    Creates a scaled label and returns it for alignment or width measurement.
    @author Brandon Mai
    */
    private GLabel label(String text, double x, double y, double size, Color color) {
        GLabel label = new GLabel(text, offsetX + x * scale, offsetY + y * scale);
        label.setFont(new Font("SansSerif", Font.PLAIN, Math.max(8, (int) Math.round(size * scale))));
        label.setColor(color);
        canvas.add(label);
        return label;
    }

    /**
    Centers text using the actual rendered label width.
    @author Brandon Mai
    */
    private void centered(String text, double center, double y, double size, Color color) {
        GLabel label = label(text, center, y, size, color);
        label.move(-label.getWidth() / 2, 0);
    }

    /**
    Shrinks a dynamic label to fit its allotted width without truncating the result.
    @author Brandon Mai
    */
    private void fit(String text, double x, double y, double size, double width, Color color) {
        GLabel label = label(text, x, y, size, color);
        int fontSize = label.getFont().getSize();
        while (label.getWidth() > width * scale && fontSize > 8) {
            label.setFont(new Font("SansSerif", Font.PLAIN, --fontSize));
        }
    }

    /**
    Finds an enabled control after converting screen pixels to layout coordinates.
    @author Brandon Mai
    */
    private Hit hit(double x, double y) {
        for (Hit button : buttons) {
            if (button.enabled && button.bounds.contains((x - offsetX) / scale, (y - offsetY) / scale)) return button;
        }
        return null;
    }

    /**
    Activates only enabled left-click controls, including their text area.
    @author Brandon Mai
    */
    public void mouseClicked(MouseEvent event) {
        if (event.getButton() != MouseEvent.BUTTON1) return;
        canvas.requestFocusInWindow();
        Hit button = hit(event.getX(), event.getY());
        if (button != null) actionPerformed(new ActionEvent(this, 0, button.action));
    }

    /**
    Highlights the hovered control and displays the hand cursor.
    @author Brandon Mai
    */
    public void mouseMoved(MouseEvent event) {
        Hit button = hit(event.getX(), event.getY());
        String next = button == null ? "" : button.action;
        canvas.setCursor(Cursor.getPredefinedCursor(button == null ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
        if (!next.equals(hover)) { hover = next; redraw(); }
    }

    /**
    Clears hover when the pointer leaves the canvas.
    @author Brandon Mai
    */
    public void mouseExited(MouseEvent event) { hover = ""; redraw(); }

    /**
    Supports Tab and Shift-Tab focus, Enter/Space activation, and Escape navigation.
    @author Brandon Mai
    */
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            actionPerformed(new ActionEvent(this, 0, "Home"));
        } else if (event.getKeyCode() == KeyEvent.VK_TAB) {
            ArrayList<String> actions = new ArrayList<String>();
            for (Hit button : buttons) if (button.enabled && !actions.contains(button.action)) actions.add(button.action);
            int current = actions.indexOf(focus);
            if (!actions.isEmpty()) {
                int next = current < 0 ? (event.isShiftDown() ? actions.size() - 1 : 0)
                    : Math.floorMod(current + (event.isShiftDown() ? -1 : 1), actions.size());
                focus = actions.get(next);
                redraw();
            }
            event.consume();
        } else if (event.getKeyCode() == KeyEvent.VK_ENTER || event.getKeyCode() == KeyEvent.VK_SPACE) {
            for (Hit button : new ArrayList<Hit>(buttons)) {
                if (button.enabled && button.action.equals(focus)) {
                    actionPerformed(new ActionEvent(this, 0, focus));
                    break;
                }
            }
        }
    }

    /**
    Key releases require no game action.
    @author Brandon Mai
    */
    public void keyReleased(KeyEvent event) { }

    /**
    Typed characters require no game action.
    @author Brandon Mai
    */
    public void keyTyped(KeyEvent event) { }

    /**
    Recomputes layout and click regions whenever the canvas size changes.
    @author Brandon Mai
    */
    public void componentResized(ComponentEvent event) { redraw(); }

    /**
    Refreshes the canvas when it becomes visible.
    @author Brandon Mai
    */
    public void componentShown(ComponentEvent event) { redraw(); }

    /**
    Moving the canvas does not change its internal layout.
    @author Brandon Mai
    */
    public void componentMoved(ComponentEvent event) { }

    /**
    Hiding the canvas preserves the active hand and screen.
    @author Brandon Mai
    */
    public void componentHidden(ComponentEvent event) { }

    /**
    Starts the desktop app on Swing's event thread.
    @author Brandon Mai
    */
    public static void main(String[] args) { SwingUtilities.invokeLater(PokerPrototype::new); }
}
