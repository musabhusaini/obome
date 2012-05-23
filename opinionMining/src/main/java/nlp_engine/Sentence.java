package nlp_engine;

import java.util.Map;

import database_engine.DatabaseAdapter;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;

public class Sentence {
  private Token sentenceRoot;

  protected Sentence(SemanticGraph dependencies, int indexStart, DatabaseAdapter adp,
      int beginPosition) {
    //System.out.println(dependencies);
    IndexedWord rootWord = dependencies.getFirstRoot();
    sentenceRoot = new Token(rootWord.originalText(), rootWord.lemma(), rootWord.tag(), null, null,
        rootWord.index() + indexStart, rootWord.beginPosition(), rootWord.endPosition(), adp,
        beginPosition);
    AddChildTokens(sentenceRoot, rootWord, dependencies, indexStart, adp, beginPosition);
    sentenceRoot.TransferScores();
    if (sentenceRoot.IsAKeyword())
      sentenceRoot.AddAspectScore(sentenceRoot.GetScore(), sentenceRoot.GetWeight(),
          sentenceRoot.GetAspectId());
    indexStart += dependencies.size();
  }

  private void AddChildTokens(Token rootToken, IndexedWord currentRoot, SemanticGraph dependencies,
      int indexStart, DatabaseAdapter adp, int beginPosition) {
    for (IndexedWord child : dependencies.getChildren(currentRoot)) {
      Token childToken = new Token(child.originalText(), child.lemma(), child.tag(), rootToken,
          dependencies.getEdge(currentRoot, child).toString(), child.index() + indexStart,
          child.beginPosition(), child.endPosition(), adp, beginPosition);
      rootToken.AddChildToken(childToken);
      AddChildTokens(childToken, child, dependencies, indexStart, adp, beginPosition);
    }
  }

  protected Map<Long, Float> GetScoreMap() {
    return sentenceRoot.GetScoreMap();
  }

  protected Map<Long, Integer> GetWeightMap() {
    return sentenceRoot.GetWeightMap();
  }

  protected Token GetRootToken() {
    return sentenceRoot;
  }
}
