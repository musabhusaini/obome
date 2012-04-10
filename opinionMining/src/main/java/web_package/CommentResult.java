package web_package;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.ubipol.opinionmining.nlp_engine.Paragraph;
import eu.ubipol.opinionmining.nlp_engine.Sentence;
import eu.ubipol.opinionmining.nlp_engine.Token;
import eu.ubipol.opinionmining.owl_engine.OntologyHandlerForDatabase;

public class CommentResult {
  private Map<Long, Float> scoreMap;
  private List<ModifierItem> modifierList;

  public CommentResult(String comment, byte[] uuid, String connectionString) throws Exception {
    this.scoreMap = new HashMap<Long, Float>();
    this.modifierList = new ArrayList<ModifierItem>();

    Paragraph p = Paragraph.GetParagraphInstanceForStanford(comment,
        new OntologyHandlerForDatabase(connectionString, uuid));

    scoreMap = p.GetScoreInfo();

    for (Sentence s : p.GetSentences()) {
      for (Token t : s.GetTokens()) {
        if (!t.IsUsed() && t.GetAspect() >= 0) {
          fillModifiers(t);
        }
      }
    }
  }

  private void fillModifiers(Token t) {
    for (Token temp : t.GetModifiers()) {
      modifierList.add(new ModifierItem(t.GetTokenIndex(), temp.GetTokenIndex()));
      fillModifiers(temp);
    }
  }

  public Map<Long, Float> GetScoreCard() {
    return scoreMap;
  }

  public List<ModifierItem> GetModifierList() {
    return modifierList;
  }
}
