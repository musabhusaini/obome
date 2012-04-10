package eu.ubipol.opinionmining.nlp_engine;

import java.util.ArrayList;
import java.util.List;

import eu.ubipol.opinionmining.owl_engine.OntologyHandler;
import eu.ubipol.opinionmining.owl_engine.Utils;
import eu.ubipol.opinionmining.stem_engine.Stemmer;

class TokenStanford extends Token {
  private String original;
  private String tokenRoot;
  private int tokenType;
  private float score;
  private int weight;
  private Long aspect;
  private boolean isModified;
  private boolean isUsed;
  private List<Token> preTokens;
  private int tokenIndex;

  public TokenStanford(String word, int type, OntologyHandler ont, int index) throws Exception {
    tokenIndex = index;
    preTokens = new ArrayList<Token>();
    original = word.trim();
    tokenType = type;
    isModified = false;
    isUsed = false;
    Stemmer stemmer = new Stemmer("stemrules.txt", "en_US.aff", "en_US.dic", "h,p,s");
    aspect = new Long(-1);
    List<String> stems = stemmer.GetStems(original.toLowerCase());
    stems.add(0, original.toLowerCase());
    if (stems.size() > 0)
      tokenRoot = stems.get(0);
    else
      tokenRoot = original.toLowerCase();
    for (int i = 0; i < stems.size() && aspect == -1; i++) {
      aspect = ont.GetFeatureOfAWord(stems.get(i));
      if (aspect != -1)
        tokenRoot = stems.get(i);
    }

    String typeString = "";
    if (tokenType == Utils.ADJECTIVE_ID)
      typeString = "_a";
    else if (tokenType == Utils.ADVERB_ID)
      typeString = "_r";
    else if (tokenType == Utils.NOUN_ID)
      typeString = "_n";
    else if (tokenType == Utils.VERB_ID)
      typeString = "_v";
    score = -2;
    if (tokenType == Utils.ADJECTIVE_ID || tokenType == Utils.ADVERB_ID) {
      for (int i = 0; i < stems.size() && score == -2; i++) {
        if (eu.ubipol.opinionmining.nlp_engine.Utils.GetScoreList().containsKey(
            stems.get(i) + typeString))
          score = eu.ubipol.opinionmining.nlp_engine.Utils.GetScoreList().get(
              stems.get(i) + typeString);
      }

      if (score == -2)
        weight = 0;
      else
        weight = 1;
    } else {
      for (int i = 0; i < stems.size() && score == -2; i++) {
        if (eu.ubipol.opinionmining.nlp_engine.Utils.GetScoreList().containsKey(
            stems.get(i) + typeString))
          score = eu.ubipol.opinionmining.nlp_engine.Utils.GetScoreList().get(
              stems.get(i) + typeString);
      }

      if (score == -2)
        weight = 0;
      else
        weight = 1;
    }
  }

  @Override
  public void UpdateScore(float score1, int weight1, float score2, int weight2) {
    score = weight1 + weight2 != 0 ? ((score1 * weight1) + (score2 * weight2))
        / (weight1 + weight2) : 0;
    weight = weight1 + weight2;
    isModified = true;
  }

  @Override
  public void addModifierToken(Token preToken) {
    preTokens.add(preToken);
  }

  @Override
  public List<Token> GetModifiers() {
    return preTokens;
  }

  @Override
  public boolean IsModified() {
    return isModified;
  }

  @Override
  public boolean IsUsed() {
    return isUsed;
  }

  @Override
  public void SetWeight(int Weight) {
    weight = Weight;
  }

  @Override
  public void SetAsUsed() {
    isUsed = true;
  }

  @Override
  public void SetScore(float Score) {
    score = Score;
  }

  @Override
  public Long GetAspect() {
    return aspect;
  }

  @Override
  public String GetOriginal() {
    return original;
  }

  @Override
  public float GetScore() {
    return score;
  }

  @Override
  public int GetWeight() {
    return weight;
  }

  @Override
  public int GetType() {
    return tokenType;
  }

  @Override
  public String toString() {
    return original;
  }

  @Override
  public String GetRoot() {
    return tokenRoot;
  }

  @Override
  public int GetTokenIndex() {
    return tokenIndex;
  }
}
