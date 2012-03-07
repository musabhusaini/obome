package eu.ubipol.opinionmining.nlp_engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.ubipol.opinionmining.owl_engine.OntologyHandler;

class ParagraphStanford extends Paragraph {
  private List<Sentence> sentences;
  private Map<Long, Float> scoreList;
  private Map<Long, Integer> weightList;

  public ParagraphStanford(String text, OntologyHandler ont) throws Exception {
    Annotation document = new Annotation(text);
    Utils.GetNlpEngine().annotate(document);
    List<CoreMap> iSentences = document.get(SentencesAnnotation.class);
    sentences = new ArrayList<Sentence>();
    for (CoreMap c : iSentences)
      sentences.add(Sentence.GetSentenceInstanceForStanford(c, ont));
  }

  @Override
  public List<Sentence> GetSentences() {
    return this.sentences;
  }

  @Override
  public String toString() {
    String result = "";
    for (Sentence s : this.sentences)
      result += s.toString();
    return result;
  }

  private void AddASpectScore(Long aspect, float score, int weight) {
    if (this.scoreList.containsKey(aspect)) {
      this.scoreList.put(aspect, (scoreList.get(aspect) * weightList.get(aspect) + score * weight)
          / (weight + weightList.get(aspect)));
      weightList.put(aspect, weightList.get(aspect) + weight);
    } else {
      this.scoreList.put(aspect, score);
      weightList.put(aspect, weight);
    }
  }

  private void AddASpectScore2(Long aspect, float score, int weight) {
    if (this.scoreList.containsKey(aspect) && Math.abs(scoreList.get(aspect)) < Math.abs(score)) {
      scoreList.put(aspect, score);
      weightList.put(aspect, weightList.get(aspect) + weight);
    } else {
      this.scoreList.put(aspect, score);
      weightList.put(aspect, weight);
    }
  }

  @Override
  public Map<Long, Float> GetScoreInfo() {
    if (scoreList == null) {
      scoreList = new HashMap<Long, Float>();
      weightList = new HashMap<Long, Integer>();
      for (Sentence s : this.sentences)
        for (Entry<Long, Float> e : s.GetScoreInfo().entrySet())
          this.AddASpectScore(e.getKey(), e.getValue(), s.GetWeightOfAnAspect(e.getKey()));
    }
    return this.scoreList;
  }

  @Override
  public Map<Long, Integer> GetWeightInfo() {
    if (weightList == null) {
      scoreList = new HashMap<Long, Float>();
      weightList = new HashMap<Long, Integer>();
      for (Sentence s : this.sentences)
        for (Entry<Long, Float> e : s.GetScoreInfo().entrySet())
          this.AddASpectScore(e.getKey(), e.getValue(), s.GetWeightOfAnAspect(e.getKey()));
    }
    return weightList;
  }
}
