/**
Checks the five player moves.
@author Brandon Mai
*/
public class ActionTester {
  /**
  Prints each move and checks its type and amount.
  @author Brandon Mai
  */
  public static void main(String[] args) {
    Action[] moves = {
      Action.fold(), Action.check(), Action.call(), Action.raise(50), Action.allIn()
    };
    ActionType[] types = {
      ActionType.FOLD, ActionType.CHECK, ActionType.CALL, ActionType.RAISE, ActionType.ALL_IN
    };
    int[] amounts = {0, 0, 0, 50, 0};
    String[] labels = {"FOLD", "CHECK", "CALL", "RAISE to 50", "ALL_IN"};

    for (int i = 0; i < moves.length; i++) {
      Action move = moves[i];
      if (move.getType() != types[i] || move.getAmount() != amounts[i]
          || !move.toString().equals(labels[i])) {
        System.out.println("Test failed for " + labels[i]);
        return;
      }
      System.out.println(move);
    }
  }
}
