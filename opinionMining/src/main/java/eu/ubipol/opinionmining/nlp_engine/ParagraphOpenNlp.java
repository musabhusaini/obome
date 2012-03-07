package eu.ubipol.opinionmining.nlp_engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.ubipol.opinionmining.owl_engine.OntologyHandler;

public class ParagraphOpenNlp extends Paragraph {
  private List<Sentence> Sentences;
  private Map<Long, Float> scoreList;
  private Map<Long, Integer> weightList;

  public ParagraphOpenNlp(String text, OntologyHandler ont) throws Exception {
    String[] sentences = Utils.GetSentenceDetector().sentDetect(text);
    Sentences = new ArrayList<Sentence>();
    for (String s : sentences)
      Sentences.add(new SentenceOpenNlp(s, ont));
  }

  @Override
  public List<Sentence> GetSentences() {
    return this.Sentences;
  }

  @Override
  public String toString() {
    String result = "";
    for (Sentence s : this.Sentences)
      result += " " + s.toString();
    return result.length() > 0 ? result.substring(1) : "";
  }

  private void AddASpectScore(Long aspect, float score, int weight) {
    if (this.scoreList.containsKey(aspect)) {
      scoreList.put(aspect, scoreList.get(aspect) + score);
      weightList.put(aspect, weightList.get(aspect) + weight);
    } else {
      scoreList.put(aspect, score);
      weightList.put(aspect, weight);
    }
  }

  @Override
  public Map<Long, Float> GetScoreInfo() {
    if (scoreList == null) {
      scoreList = new HashMap<Long, Float>();
      weightList = new HashMap<Long, Integer>();
      for (Sentence s : this.Sentences)
        for (Entry<Long, Float> e : s.GetScoreInfo().entrySet())
          AddASpectScore(e.getKey(), e.getValue(), s.GetWeightOfAnAspect(e.getKey()));
    }
    return scoreList;
  }

  @Override
  public Map<Long, Integer> GetWeightInfo() {
    if (weightList == null) {
      scoreList = new HashMap<Long, Float>();
      weightList = new HashMap<Long, Integer>();
      for (Sentence s : this.Sentences)
        for (Entry<Long, Float> e : s.GetScoreInfo().entrySet())
          AddASpectScore(e.getKey(), e.getValue(), s.GetWeightOfAnAspect(e.getKey()));
    }
    return weightList;
  }
}
