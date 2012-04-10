package eu.ubipol.opinionmining.nlp_engine;

import java.util.List;
import java.util.Map;

import eu.ubipol.opinionmining.owl_engine.OntologyHandler;

public abstract class Paragraph {
  public static Paragraph GetParagraphInstanceForStanford(String text, OntologyHandler ont)
      throws Exception {
    return new ParagraphStanford(text, ont);
  }

  public abstract List<Sentence> GetSentences();

  public abstract String toString();

  public abstract Map<Long, Float> GetScoreInfo();

  public abstract Map<Long, Integer> GetWeightInfo();
}
