/**
Stores a player's move.
@author Brandon Mai
*/
public final class Action {
  private final ActionType type;
  private final int amount;

  /**
  Makes an action. Only raises keep the amount.
  @author Brandon Mai
  */
  public Action(ActionType type, int amount) {
    this.type = type;
    if (type == ActionType.RAISE) {
      this.amount = amount;
    } else {
      this.amount = 0;
    }
  }

  /**
  Makes a fold.
  @author Brandon Mai
  */
  public static Action fold() {
    return new Action(ActionType.FOLD, 0);
  }

  /**
  Makes a check.
  @author Brandon Mai
  */
  public static Action check() {
    return new Action(ActionType.CHECK, 0);
  }

  /**
  Makes a call.
  @author Brandon Mai
  */
  public static Action call() {
    return new Action(ActionType.CALL, 0);
  }

  /**
  Makes an all-in.
  @author Brandon Mai
  */
  public static Action allIn() {
    return new Action(ActionType.ALL_IN, 0);
  }

  /**
  Makes a raise.
  @author Brandon Mai
  */
  public static Action raise(int amount) {
    return new Action(ActionType.RAISE, amount);
  }

  /**
  Gets the move type.
  @author Brandon Mai
  */
  public ActionType getType() {
    return type;
  }

  /**
  Gets the amount.
  @author Brandon Mai
  */
  public int getAmount() {
    return amount;
  }

  /**
  Shows the move as text.
  @author Brandon Mai
  */
  @Override
  public String toString() {
    if (type == ActionType.RAISE) {
      return "RAISE to " + amount;
    }
    return type.name();
  }
}
