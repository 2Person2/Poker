# River Room

Java poker prototype by Brandon Mai. ACM draws the home screen, poker table,
shop, and all on-screen controls. Standard Java supplies the window, input events,
and local save files. No additional framework, images, or downloads are needed
beyond an installed JDK 17 or 21 and the included ACM library.

## Run

Open Terminal or Command Prompt in this repository folder.

- Mac/Linux: `bash run.sh`
- Windows Command Prompt: `run.bat`
- Windows PowerShell: `./run.bat`

Both `java -version` and `javac -version` should work. The scripts compile the
Java files into `out/` and launch the desktop game. They support the repository's
root-level Java files as well as the earlier `src/` folder layout. In an IDE,
add `lib/acm.jar` as a library and run `PokerPrototype.main`.

## Screens and controls

- **Home:** start or resume a hand, visit the shop, and see your current table look.
- **Table:** choose a difficulty before dealing, then Check, Bet 20, or Fold.
  Difficulty is locked for the current hand. All buttons are inside the canvas.
- **Shop:** choose a category, select a product to preview it, and buy/equip it.
  There are two pages per category. Owned items can be equipped free of charge.
  An unaffordable purchase is disabled and shows how many coins you still need.
- **Keyboard:** Tab / Shift-Tab moves focus, Enter or Space activates, Escape goes home.
- **Resizing:** drawing and click regions scale together using canvas width/height.

Moving between screens preserves an unfinished hand. Previewing an item does not
spend coins or change your equipment. Buying applies it only to its own category.

## 48 cosmetics across six independent categories

| Category | Choices |
| --- | --- |
| Card faces | Classic, Midnight, Sunset, Glacier, Lavender, Mint, Obsidian, Rose Quartz |
| Card backs | Navy Weave, Crimson Grid, Ocean Lines, Golden Diamond, Violet Stars, Forest Cross, Coral Dots, Silver Chevron |
| Table felt | Club Green, Burgundy, Deep Ocean, Royal Purple, Slate, Emerald, Desert Sand, Charcoal |
| Table rails | Walnut, Mahogany, Brushed Steel, Rose Gold, Black Marble, Ivory, Neon Violet, Championship Gold |
| Chip sets | Ruby Chips, Sapphire Chips, Emerald Chips, Violet Chips, Onyx Chips, Pearl Chips, Solar Chips, Coral Chips |
| Player badges | Rookie, Ace Hunter, Night Owl, Card Shark, Bluff Artist, High Roller, River Royal, Grandmaster |

Six defaults are free; the other 42 items cost 25-160 coins. Win a hand for 10
coins, tie for 3, or lose for 0. Coins are independent of the simulated poker pot.
Styles change colors and patterns, not the cards dealt or the AI's information.
The card/table appearance is local; your badge marks your seat.

## Saved progress

Coins, owned items, and all six equipment slots are saved automatically to
`saves/profile.properties` inside the project folder. No account or network is
used. The status line tells you if saving fails. A malformed profile is kept
unchanged and the game uses session-only progress until the file is repaired or
moved aside. Close the game before moving or editing the file. Do not run two
copies against the same save file; concurrent profile merging is not supported.

An unfinished hand, recent hand history, and difficulty are not saved. This is a
local prototype save, not a tamper-resistant economy. `saves/` is ignored by Git.

## Data structures

`RewardWallet` uses a LinkedHashMap for catalog lookup/order, a HashSet for unique
ownership, an EnumMap for equipped categories, and filtered Lists for shop pages.
`PokerPrototype` keeps canvas control hit areas in an ArrayList. Existing deck,
multidimensional player-card arrays, evaluator, and limited-information AI remain.
All declared source methods retain `@author Brandon Mai` in Javadoc without
extra decorative leading asterisks.

## Current scope

This is still the first-deliverable river prototype. Each scenario starts with a
20-chip pot, two private cards per player, and all five community cards. Checking
leads to showdown; betting 20 lets the AI call or fold. There are no player stacks,
raises, earlier betting rounds, friend mode, or full matches yet.

Easy calls randomly; Medium samples hypothetical unseen hands; Hard checks all
990 unseen pairs. The AI receives only its own cards, the public board, pot, and
call cost. Player cards remain hidden until showdown.

## Validation of the shop update

The updated source compiles against the original ACM JAR on OpenJDK 21. Original
123 logic checks pass. An additional isolated test harness passes 2,918 checks
covering all 48 purchases, independent slots, free re-equips, save/reload,
malformed/unwritable saves, previews without spending, canvas click routing,
navigation preserving hands, keyboard focus, and text bounds at multiple sizes.
Home, shop, and table were rendered and visually inspected offscreen. Native
Windows/macOS interaction and the Windows batch launcher still need a local check.
The temporary validation harness is not required to run the app.

## Third-party dependency

`lib/acm.jar` is the unmodified ACM Java Task Force library, not code authored by
Brandon Mai. Its license is in `lib/ACM-License.pdf`.

- [ACM graphics documentation](https://cs.stanford.edu/people/eroberts/jtf/javadoc/student/acm/graphics/package-summary.html)
- [Official ACM library download](https://cs.stanford.edu/people/eroberts/jtf/)
- [Component width documentation](https://docs.oracle.com/javase/1.5.0/docs/api/java/awt/Component.html#getWidth())
