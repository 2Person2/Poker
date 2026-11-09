import acm.graphics.GCanvas;
import acm.graphics.GLabel;
import acm.graphics.GRect;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
ACM graphics risk prototype. Swing supplies the window and standard buttons;
all table artwork and cards are drawn with ACM GCanvas, GRect, and GLabel.
@author Brandon Mai
*/
public final class PokerPrototype implements ActionListener {
    private final RiverGame game = new RiverGame(new Random(), new Random());
    private final GCanvas canvas = new GCanvas();
    private final JComboBox<PokerAi.Difficulty> difficulty =
        new JComboBox<PokerAi.Difficulty>(PokerAi.Difficulty.values());
    private final JButton deal = new JButton("Deal");
    private final JButton check = new JButton("Check");
    private final JButton bet = new JButton("Bet 20");
    private final JButton fold = new JButton("Fold");
    private final JComboBox<String> skins = new JComboBox<String>(new String[] {"Classic", "Midnight", "Sunset"});
    private String shopMessage = "Skins: Classic free | Midnight 30 | Sunset 60. Win +10, tie +3 coins.";

    /**
    Creates the window and event controls on the Swing event thread.
    @author Brandon Mai
    */
    public PokerPrototype() { this(true); }

    /**
    Builds the same graphics with an optional window for offscreen testing.
    @author Brandon Mai
    */
    PokerPrototype(boolean openWindow) {
        JPanel controls = new JPanel();
        canvas.setPreferredSize(new Dimension(1040, 650));
        canvas.setSize(1040, 650);
        canvas.setBackground(new Color(18, 57, 48));
        controls.add(difficulty);
        for (JButton button : new JButton[] {deal, check, bet, fold}) {
            button.addActionListener(this);
            controls.add(button);
        }
        controls.add(skins);
        JButton shop = new JButton("Buy / Equip");
        shop.addActionListener(this);
        controls.add(shop);
        redraw();
        if (!openWindow) return;
        JFrame frame = new JFrame("River Room | First Deliverable | Brandon Mai");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(canvas, BorderLayout.CENTER);
        frame.add(controls, BorderLayout.SOUTH);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        redraw();
        frame.setVisible(true);
    }

    /**
    Routes button clicks to game logic and redraws the table.
    @author Brandon Mai
    */
    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("Deal")) game.deal();
        else if (action.equals("Buy / Equip")) {
            String skin = (String) skins.getSelectedItem();
            shopMessage = game.wallet().buyOrEquip(skin) ? "Equipped " + skin + "."
                : "Not enough coins. Midnight costs 30; Sunset costs 60.";
        } else game.act(action, (PokerAi.Difficulty) difficulty.getSelectedItem());
        redraw();
    }

    /**
    Draws the current visible state without requesting hidden AI cards.
    @author Brandon Mai
    */
    private void redraw() {
        canvas.removeAll();
        label("RIVER ROOM", 30, 40, 26, new Color(239, 211, 149));
        label("Texas Hold'em river prototype | No bankroll or earlier betting rounds yet", 30, 66, 14, Color.WHITE);
        label("Coins: " + game.wallet().coins() + "   Skin: " + game.wallet().equipped(), 680, 38, 16, Color.WHITE);
        label("AI cards", 40, 104, 16, Color.WHITE);
        cards(game.shownCards(1), 40, 118, 2);
        label("Public board | Pot: " + game.pot(), 300, 104, 16, Color.WHITE);
        cards(game.board(), 300, 118, 5);
        label("Your cards", 40, 265, 16, Color.WHITE);
        cards(game.shownCards(0), 40, 279, 2);
        label("AI knows its own 2 cards and the 5 board cards.", 300, 298, 17, Color.WHITE);
        label("Easy: random calls | Medium: 120 samples | Hard: 990 possible hands", 300, 329, 15, Color.WHITE);
        label("The selected difficulty applies when you take your action.", 300, 358, 14, Color.WHITE);
        label(game.message(), 30, 422, 15, Color.WHITE);
        label(shopMessage, 30, 452, 14, new Color(239, 211, 149));
        label("RECENT HANDS (this session)", 30, 486, 14, Color.WHITE);
        int y = 512;
        for (String entry : game.history()) { label(entry, 30, y, 12, Color.WHITE); y += 23; }
        deal.setEnabled(game.complete());
        check.setEnabled(!game.complete());
        bet.setEnabled(!game.complete());
        fold.setEnabled(!game.complete());
    }

    /**
    Draws face-up cards or anonymous backs without storing hidden values in labels.
    @author Brandon Mai
    */
    private void cards(List<Card> cards, int x, int y, int slots) {
        String skin = game.wallet().equipped();
        Color accent = skin.equals("Midnight") ? new Color(110, 117, 225)
            : skin.equals("Sunset") ? new Color(224, 117, 65) : new Color(60, 123, 173);
        for (int i = 0; i < slots; i++) {
            boolean faceUp = i < cards.size();
            GRect rectangle = new GRect(x + i * 102, y, 88, 112);
            rectangle.setFilled(true);
            rectangle.setFillColor(faceUp ? new Color(249, 246, 234) : accent);
            rectangle.setColor(accent);
            canvas.add(rectangle);
            if (faceUp) {
                Card card = cards.get(i);
                label(card.toString(), x + i * 102 + 10, y + 40, 24,
                    card.suit == 1 || card.suit == 2 ? new Color(180, 45, 50) : Color.BLACK);
                label(skin, x + i * 102 + 7, y + 97, 11, accent);
            } else label("?", x + i * 102 + 34, y + 65, 30, Color.WHITE);
        }
    }

    /**
    Adds a formatted ACM text object to the canvas.
    @author Brandon Mai
    */
    private void label(String text, int x, int y, int size, Color color) {
        GLabel label = new GLabel(text, x, y);
        label.setFont("SansSerif-plain-" + size);
        label.setColor(color);
        canvas.add(label);
    }

    /**
    Launches the desktop prototype.
    @author Brandon Mai
    */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(PokerPrototype::new);
    }
}
