package nlp_engine;

import java.util.List;
import java.util.Map;

import database_connector.DatabaseAdapter;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;

public class Sentence {
  private Token sentenceRoot;

  protected Sentence(SemanticGraph dependencies, int indexStart, DatabaseAdapter adp) {
    IndexedWord rootWord = dependencies.getFirstRoot();
    sentenceRoot = new Token(rootWord.lemma(), rootWord.tag(), null, null, rootWord.index()
        + indexStart, rootWord.beginPosition(), rootWord.endPosition(), adp);
    AddChildTokens(sentenceRoot, rootWord, dependencies, indexStart, adp);
    sentenceRoot.TransferScores();
    if (sentenceRoot.IsAKeyword())
      sentenceRoot.AddAspectScore(sentenceRoot.GetScore(), sentenceRoot.GetWeight(),
          sentenceRoot.GetAspectId());
    indexStart += dependencies.size();
  }

  private void AddChildTokens(Token rootToken, IndexedWord currentRoot, SemanticGraph dependencies,
      int indexStart, DatabaseAdapter adp) {
    for (IndexedWord child : dependencies.getChildren(currentRoot)) {
      Token childToken = new Token(child.lemma(), child.tag(), rootToken, dependencies.getEdge(
          currentRoot, child).toString(), child.index() + indexStart, child.beginPosition(),
          child.endPosition(), adp);
      rootToken.AddChildToken(childToken);
      AddChildTokens(childToken, child, dependencies, indexStart, adp);
    }
  }

  protected Map<Long, Float> GetScoreMap() {
    return sentenceRoot.GetScoreMap();
  }

  protected Map<Long, Integer> GetWeightMap() {
    return sentenceRoot.GetWeightMap();
  }

  protected List<ModifierItem> GetModifierList() {
    return sentenceRoot.GetModifierList();
  }
}
