package nlp_engine;

import java.util.List;
import java.util.Map;

import database_connector.DatabaseAdapter;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;

public class SentenceObject {
  private Sentence sentence;
  private int beginPosition;
  private int endPosition;
  private String text;

  public SentenceObject(SemanticGraph dependencies, int indexStart, DatabaseAdapter adp,
      int beginPosition, int endPosition, String text) {
    sentence = new Sentence(dependencies, indexStart, adp, beginPosition);
    this.beginPosition = beginPosition;
    this.endPosition = endPosition;
    this.text = text;
  }

  public Sentence GetSentence() {
    return sentence;
  }

  public List<ModifierItem> GetModifiers() {
    return sentence.GetModifierList();
  }

  public int GetBeginPosition() {
    return beginPosition;
  }

  public int GetEndPosition() {
    return endPosition;
  }

  public Map<Long, Float> GetScoreMap() {
    return sentence.GetScoreMap();
  }

  public String GetText() {
    return text;
  }
}
