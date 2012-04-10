package web_package;

public class ModifierItem {
  private int modifiedIndex;
  private int modifierIndex;

  public ModifierItem(int modifiedIndex, int modifierIndex) {
    this.modifiedIndex = modifiedIndex;
    this.modifierIndex = modifierIndex;
  }

  public int GetModifiedIndex() {
    return modifiedIndex;
  }

  public int GetModifierIndex() {
    return modifierIndex;
  }
}
