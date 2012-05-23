package nlp_engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database_engine.DatabaseAdapter;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;

public class SentenceObject {
  private Sentence sentence;
  private int beginPosition;
  private int endPosition;
  private String text;

  public SentenceObject(SemanticGraph dependencies, int indexStart, DatabaseAdapter adp,
      int beginPosition, int endPosition, String text) {
    System.out.println(text);
    sentence = new Sentence(dependencies, indexStart, adp, beginPosition);
    this.beginPosition = beginPosition;
    this.endPosition = endPosition;
    this.text = text;
  }

  public Sentence GetSentence() {
    return sentence;
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

  private void AddChildren(List<Token> returnList, Token currentRoot) {
    for (Token t : currentRoot.GetChildrenList()) {
      returnList.add(t);
      AddChildren(returnList, t);
    }
  }

  public List<Token> GetTokenList() {
    List<Token> wholeToken = new ArrayList<Token>();
    wholeToken.add(sentence.GetRootToken());
    AddChildren(wholeToken, sentence.GetRootToken());
    return wholeToken;
  }

  public List<ModifierItem> GetModifierList() {
    List<ModifierItem> modifierList = new ArrayList<ModifierItem>();
    for (Token t : GetTokenList()) {
      if (t.GetModifierList().size() > 0) {
        for (Token mt : t.GetModifierList()) {
          modifierList.add(new ModifierItem(t, mt));
        }
      } else if (t.IsAKeyword()) {
        modifierList.add(new ModifierItem(t));
      }
    }
    return modifierList;
  }
}
