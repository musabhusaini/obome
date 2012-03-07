package eu.ubipol.opinionmining.nlp_engine;

import eu.ubipol.opinionmining.owl_engine.OntologyHandler;
import eu.ubipol.opinionmining.stemmer.EnglishStemmer;

class TokenOpenNlp extends Token {

  private String original;
  private int tokenType;
  private float score;
  private int weight;
  private Long aspect;

  public TokenOpenNlp(String word, int type, OntologyHandler ont) throws Exception {
    original = word.trim();
    EnglishStemmer stemmer = new EnglishStemmer();
    stemmer.setCurrent(original.toLowerCase());
    stemmer.stem();
    String tokenRoot = stemmer.getCurrent().toLowerCase();
    tokenType = type;
    aspect = ont.GetFeatureOfAWord(tokenRoot);
    score = ont.GetWordScore(tokenRoot, tokenType);
    weight = 1;
  }

  @Override
  public void UpdateScore(float score1, int weight1, float score2, int weight2) {
    score = weight1 + weight2 != 0 ? ((score1 * weight1) + (score2 * weight2))
        / (weight1 + weight2) : 0;
    weight = weight1 + weight2;
  }

  @Override
  public boolean IsModified() {
    return true;
  }

  @Override
  public boolean IsUsed() {
    return false;
  }

  @Override
  public void SetWeight(int Weight) {
    weight = Weight;
  }

  @Override
  public void SetAsUsed() {
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

}
