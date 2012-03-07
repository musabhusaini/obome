package eu.ubipol.opinionmining.nlp_engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.ubipol.opinionmining.owl_engine.OntologyHandler;

public class SentenceOpenNlp extends Sentence {
  private List<PhraseOpenNlp> Phrases;
  private Map<Long, Float> scoreList;
  private Map<Long, Integer> weightList;

  public SentenceOpenNlp(String sentence, OntologyHandler ont) throws Exception {
    String[] tokens = Utils.GetTokenizer().tokenize(sentence);
    String[] tags = Utils.GetPosTagger().tag(tokens);
    String[] chunks = Utils.GetChunker().chunk(tokens, tags);

    Phrases = new ArrayList<PhraseOpenNlp>();
    for (int i = 0; i < tokens.length; i++) {
      int wordType = 5;
      if (tags[i].length() < 2)
        wordType = 5;
      else if (tags[i].substring(0, 2).equals("JJ"))
        wordType = eu.ubipol.opinionmining.owl_engine.Utils.ADJECTIVE_ID;
      else if (tags[i].substring(0, 2).equals("VB"))
        wordType = eu.ubipol.opinionmining.owl_engine.Utils.VERB_ID;
      else if (tags[i].substring(0, 2).equals("NN"))
        wordType = eu.ubipol.opinionmining.owl_engine.Utils.NOUN_ID;
      else if (tags[i].substring(0, 2).equals("RB"))
        wordType = eu.ubipol.opinionmining.owl_engine.Utils.ADVERB_ID;

      int type = 5;
      if (chunks[i].substring(2).equals("ADJP"))
        type = Utils.ADJECTIVE_PHRASE;
      else if (chunks[i].substring(2).equals("ADVP"))
        type = Utils.ADVERB_PHRASE;
      else if (chunks[i].substring(2).equals("NP"))
        type = Utils.NOUN_PHRASE;
      else if (chunks[i].substring(2).equals("VP"))
        type = Utils.VERB_PHRASE;

      if (chunks[i].substring(0, 1).equals("B")) {
        Phrases.add(new PhraseOpenNlp(type));
        Phrases.get(Phrases.size() - 1).AddWord(
            Token.GetTokenInstanceForOpenNlp(tokens[i], wordType, ont));
      } else if (chunks[i].substring(0, 1).equals("I"))
        Phrases.get(Phrases.size() - 1).AddWord(
            Token.GetTokenInstanceForOpenNlp(tokens[i], wordType, ont));
      else {
        Phrases.add(new PhraseOpenNlp(type));
        Phrases.get(Phrases.size() - 1).AddWord(
            Token.GetTokenInstanceForOpenNlp(tokens[i], wordType, ont));
      }
    }
  }

  @Override
  public List<Token> GetTokens() {
    List<Token> tokens = new ArrayList<Token>();
    for (PhraseOpenNlp p : Phrases)
      for (Token t : p.GetTokens())
        tokens.add(t);
    return tokens;
  }

  @Override
  protected int GetWeightOfAnAspect(Long aspect) {
    return weightList.get(aspect);
  }

  @Override
  protected double GetScoreOfAnAspect(Long aspect) {
    return scoreList.get(aspect);
  }

  private void AddAspectScore(Long aspect, float score, int weight) {
    if (scoreList.containsKey(aspect)) {
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
      for (PhraseOpenNlp p : Phrases) {
        if (p.GetType() == Utils.NOUN_PHRASE)
          for (Entry<Long, Float> e : p.GetScoreInfo().entrySet())
            AddAspectScore(e.getKey(), e.getValue(), p.GetWeightInfo().get(e.getKey()));
      }
    }
    return this.scoreList;
  }

  @Override
  public Map<Long, Integer> GetWeightInfo() {
    if (weightList == null) {
      scoreList = new HashMap<Long, Float>();
      weightList = new HashMap<Long, Integer>();
      for (PhraseOpenNlp p : Phrases) {
        if (p.GetType() == Utils.NOUN_PHRASE)
          for (Entry<Long, Float> e : p.GetScoreInfo().entrySet())
            AddAspectScore(e.getKey(), e.getValue(), p.GetWeightInfo().get(e.getKey()));
      }
    }
    return weightList;
  }

  @Override
  public String toString() {
    String result = "";
    for (PhraseOpenNlp p : Phrases)
      result += " " + p.toString();
    return result.length() > 0 ? result.substring(1) : "";
  }

}
