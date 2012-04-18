package nlp_engine;

public class ModifierItem {
  private Token modifierToken;
  private Token modifiedToken;

  public ModifierItem(Token modifierToken, Token modifiedToken) {
    this.modifierToken = modifierToken;
    this.modifiedToken = modifiedToken;
  }

  public ModifierItem(Token token) {
    this.modifiedToken = token;
  }

  public int GetModifierIndex() {
    return modifierToken != null ? modifierToken.GetIndex() : -1;
  }

  public int GetModifiedIndex() {
    return modifiedToken != null ? modifiedToken.GetIndex() : -1;
  }

  public String GetModifierString() {
    return modifierToken != null ? modifierToken.GetOriginal() : null;
  }

  public String GetModifiedString() {
    return modifiedToken != null ? modifiedToken.GetOriginal() : null;
  }

  public boolean IsModifiedTokenAKeyword() {
    return modifiedToken.IsAKeyword();
  }

  public boolean HasModifier() {
    return !(modifierToken == null);
  }

  public String GetModifiedKeywordAspectName() {
    return IsModifiedTokenAKeyword() ? Aspects.GetAspectName(modifiedToken.GetAspectId()) : null;
  }

  public int GetModifierBeginPosition() {
    return modifierToken.GetBeginPosition();
  }

  public int GetModifierEndPosition() {
    return modifierToken.GetEndPosition();
  }

  public int GetModifiedBeginPosition() {
    return modifiedToken.GetBeginPosition();
  }

  public int GetModifiedEndPosition() {
    return modifiedToken.GetEndPosition();
  }
  
  public Token GetModifierToken() {
	  return this.modifierToken;
  }
  
  public Token GetModifiedToken() {
	  return this.modifiedToken;
  }
}
