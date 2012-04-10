package eu.ubipol.opinionmining.nlp_engine;

import java.util.List;

import eu.ubipol.opinionmining.owl_engine.OntologyHandler;

public abstract class Token {
  public static Token GetTokenInstanceForStanford(String word, int type, OntologyHandler ont,
      int tokenIndex) throws Exception {
    return new TokenStanford(word, type, ont, tokenIndex);
  }

  public abstract void UpdateScore(float score1, int weight1, float score2, int weight2);

  public abstract boolean IsModified();

  public abstract void addModifierToken(Token preToken);

  public abstract boolean IsUsed();

  public abstract void SetWeight(int Weight);

  public abstract void SetAsUsed();

  public abstract void SetScore(float Score);

  public abstract Long GetAspect();

  public abstract String GetOriginal();

  public abstract String GetRoot();

  public abstract float GetScore();

  public abstract int GetWeight();

  public abstract int GetType();

  public abstract String toString();

  public abstract List<Token> GetModifiers();

  public abstract int GetTokenIndex();
}
