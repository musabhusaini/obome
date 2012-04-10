package eu.ubipol.opinionmining.nlp_engine;

import java.util.List;
import java.util.Map;

import edu.stanford.nlp.util.CoreMap;
import eu.ubipol.opinionmining.owl_engine.OntologyHandler;

public abstract class Sentence {
  public static Sentence GetSentenceInstanceForStanford(CoreMap sentence, OntologyHandler ont)
      throws Exception {
    return new SentenceStanford(sentence, ont);
  }

  public abstract List<Token> GetTokens();

  protected abstract int GetWeightOfAnAspect(Long aspect);

  protected abstract double GetScoreOfAnAspect(Long aspect);

  public abstract Map<Long, Float> GetScoreInfo();

  public abstract Map<Long, Integer> GetWeightInfo();

  public abstract String toString();
}
