package eu.ubipol.opinionmining.nlp_engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhraseOpenNlp {
  private List<Token> tokens;
  private int phraseType;
  private Map<Long, Float> scoreList;
  private Map<Long, Integer> weightList;

  public PhraseOpenNlp(int type) {
    tokens = new ArrayList<Token>();
    phraseType = type;
  }

  public void AddWord(Token token) {
    tokens.add(token);
  }

  private void AddAspectScore(Long aspect, float score, int weight) {
    if (this.scoreList.containsKey(aspect)) {
      scoreList.put(aspect, scoreList.get(aspect) + score);
      weightList.put(aspect, weightList.get(aspect) + weight);
    } else {
      scoreList.put(aspect, score);
      weightList.put(aspect, weight);
    }
  }

  public Map<Long, Float> GetScoreInfo() {
    if (scoreList == null) {
      scoreList = new HashMap<Long, Float>();
      double sum = 0.0;
      int count = 0;
      for (Token w : tokens) {
        if (w.GetType() == eu.ubipol.opinionmining.owl_engine.Utils.ADJECTIVE_ID
            || w.GetType() == eu.ubipol.opinionmining.owl_engine.Utils.ADVERB_ID) {
          float curScore = w.GetScore();
          if (curScore > -2.0) {
            sum += curScore;
            count++;
          }
        } else if (w.GetType() == eu.ubipol.opinionmining.owl_engine.Utils.NOUN_ID) {
          Long aspect = w.GetAspect();
          if (aspect != null) {
            AddAspectScore(aspect, count > 0 ? (float) (sum / count) : new Float(0.0), count);
          }
          sum = 0.0;
          count = 0;
        }
      }
    }
    return scoreList;
  }

  public Map<Long, Integer> GetWeightInfo() {
    if (weightList == null) {
      scoreList = new HashMap<Long, Float>();
      double sum = 0.0;
      int count = 0;
      for (Token w : tokens) {
        if (w.GetType() == eu.ubipol.opinionmining.owl_engine.Utils.ADJECTIVE_ID
            || w.GetType() == eu.ubipol.opinionmining.owl_engine.Utils.ADVERB_ID) {
          float curScore = w.GetScore();
          if (curScore > -2.0) {
            sum += curScore;
            count++;
          }
        } else if (w.GetType() == eu.ubipol.opinionmining.owl_engine.Utils.NOUN_ID) {
          Long aspect = w.GetAspect();
          if (aspect != null) {
            AddAspectScore(aspect, count > 0 ? (float) (sum / count) : new Float(0.0), count);
          }
          sum = 0.0;
          count = 0;
        }
      }
    }
    return weightList;
  }

  public int GetType() {
    return phraseType;
  }

  public List<Token> GetTokens() {
    return tokens;
  }

  public String toString() {
    String result = "";
    for (Token t : tokens)
      result += " " + t.toString();
    return result.length() > 0 ? result.substring(1) : "";
  }
}
