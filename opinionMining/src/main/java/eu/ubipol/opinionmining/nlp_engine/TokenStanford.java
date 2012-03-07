package eu.ubipol.opinionmining.nlp_engine;

import eu.ubipol.opinionmining.owl_engine.OntologyHandler;
import eu.ubipol.opinionmining.owl_engine.Utils;
import eu.ubipol.opinionmining.stemmer.EnglishStemmer;

class TokenStanford extends Token {
  private String original;
  private int tokenType;
  private float score;
  private int weight;
  private Long aspect;
  private boolean isModified;
  private boolean isUsed;

  public TokenStanford(String word, int type, OntologyHandler ont) throws Exception {
    original = word.trim();
    tokenType = type;
    isModified = false;
    isUsed = false;
    // Stemmer stemmer = new Stemmer("C:\\Users\\akocyigit\\Desktop\\Ubipol\\stemrules.txt",
    // "C:\\Users\\akocyigit\\Desktop\\Ubipol\\en_US.aff",
    // "C:\\Users\\akocyigit\\Desktop\\Ubipol\\en_US.dic");
    // aspect = new Long(-1);
    // List<String> stems = stemmer.GetStems(original.toLowerCase());
    // for (int i = 0; i < stems.size() && aspect == -1; i++) {
    // aspect = ont.GetFeatureOfAWord(stems.get(i));
    // }
    //
    // score = -2;
    // if (tokenType == Utils.ADJECTIVE_ID || tokenType == Utils.ADVERB_ID) {
    // for (int i = 0; i < stems.size() && aspect == -1; i++) {
    // score = ont.GetWordScore(stems.get(i), tokenType);
    // }
    //
    // if (score == -2)
    // weight = 0;
    // else
    // weight = 1;
    // } else {
    // for (int i = 0; i < stems.size() && aspect == -1; i++) {
    // score = ont.GetWordScore(stems.get(i), tokenType);
    // }
    //
    // if (score == -2)
    // weight = 0;
    // else
    // weight = 1;
    // }
    EnglishStemmer stemmer = new EnglishStemmer();
    stemmer.setCurrent(original.toLowerCase());
    stemmer.stem();
    aspect = ont.GetFeatureOfAWord(stemmer.getCurrent());
    String typeString = "";
    if (tokenType == Utils.ADJECTIVE_ID)
      typeString = "a";
    else if (tokenType == Utils.VERB_ID)
      typeString = "v";
    else if (tokenType == Utils.ADVERB_ID)
      typeString = "r";
    else if (tokenType == Utils.NOUN_ID)
      typeString = "n";

    if (Utils.GetScores().containsKey(stemmer.getCurrent() + "_" + typeString)) {
      score = Utils.GetScores().get(stemmer.getCurrent() + "_" + typeString);
      weight = 1;
    } else {
      score = 0;
      weight = 0;
    }
  }

  @Override
  public void UpdateScore(float score1, int weight1, float score2, int weight2) {
    score = weight1 + weight2 != 0 ? ((score1 * weight1) + (score2 * weight2))
        / (weight1 + weight2) : 0;
    weight = weight1 + weight2;
    isModified = true;
  }

  public void UpdateScore2(float score1, int weight1, float score2, int weight2) {
    if (Math.abs(score1) < Math.abs(score2) && Math.abs(score) < Math.abs(score2))
      score = score2;
    else if (Math.abs(score) < Math.abs(score1))
      score = score1;
    weight = weight1 + weight2;
    isModified = true;
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
}
