package web_package;

import java.util.List;
import java.util.Map;

import nlp_engine.ModifierItem;
import nlp_engine.Paragraph;
import database_connector.DatabaseAdapter;

public class CommentResult {
  private Map<Long, Float> scoreMap;
  private List<ModifierItem> modifierList;

  public CommentResult(String comment, byte[] uuid, String connectionString) throws Exception {
    Paragraph p = new Paragraph(comment, new DatabaseAdapter(connectionString, uuid));
    scoreMap = p.GetScoreMap();
    modifierList = p.GetModifierList();
  }

  public Map<Long, Float> GetScoreMap() {
    return scoreMap;
  }

  public List<ModifierItem> GetModifierList() {
    return modifierList;
  }
}
