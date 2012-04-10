package eu.ubipol.opinionmining.nlp_engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.xml.internal.fastinfoset.stax.events.Util;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import eu.ubipol.opinionmining.owl_engine.OntologyHandler;

class SentenceStanford extends Sentence {
  private List<Token> tokens;
  private Map<Long, Float> scoreList;
  private Map<Long, Integer> weightList;
  private CoreMap processedSentence;

  public SentenceStanford(CoreMap sentence, OntologyHandler ont) throws Exception {
    tokens = new ArrayList<Token>();
    processedSentence = sentence;
    for (CoreLabel token : processedSentence.get(TokensAnnotation.class)) {
      tokens.add(new TokenStanford(token.get(TextAnnotation.class), Utils.GetWordType(token
          .get(PartOfSpeechAnnotation.class)), ont, token.beginPosition()));
    }
    SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
    String[] result = dependencies.toList().split("\n");
    String dependencyType;
    for (String s : result) {
      if (!Util.isEmptyString(s)) {
        dependencyType = s.substring(0, s.indexOf("("));
        String infoText = s.substring(s.indexOf("(") + 1);
        infoText = infoText.substring(0, infoText.length() - 1);

        String[] wordSet = infoText.substring(1).split(",", 2);
        wordSet[0] = infoText.substring(0, 1) + wordSet[0];
        wordSet[0] = wordSet[0].substring(wordSet[0].lastIndexOf("-") + 1);
        wordSet[1] = wordSet[1].substring(wordSet[1].lastIndexOf("-") + 1);
        Token word1 = tokens.get(Integer.parseInt(wordSet[0]) - 1);
        Token word2 = tokens.get(Integer.parseInt(wordSet[1]) - 1);

        if (dependencyType.equals("acomp")) // adjectival complement
        {
          word1.UpdateScore(word1.GetScore(), word1.GetWeight(), word2.GetScore(),
              word2.GetWeight());
          word1.addModifierToken(word2);
          word2.SetAsUsed();
        } else if (dependencyType.equals("advmod")) // adverbial modifier
        {
          word1.UpdateScore(word1.GetScore(), word1.GetWeight(), word2.GetScore(),
              word2.GetWeight());
          word1.addModifierToken(word2);
          word2.SetAsUsed();
        } else if (dependencyType.equals("amod")) // adjectival modifier
        {
          word1.UpdateScore(word1.GetScore(), word1.GetWeight(), word2.GetScore(),
              word2.GetWeight());
          word1.addModifierToken(word2);
          word2.SetAsUsed();
        } else if (dependencyType.equals("ccomp")) // clausal complement
        {
          // TODO clausal complemet
        } else if (dependencyType.equals("dobj")) // direct object
        {
          // TODO direct object
        } else if (dependencyType.equals("neg")) // negation modifier
        {
          word1.SetScore(word1.GetScore() * -1);
          word1.addModifierToken(word2);
        } else if (dependencyType.equals("npadvmod")) // noun phrase as adverbial modifier
        {
          word2.UpdateScore(word1.GetScore(), word1.GetWeight(), word2.GetScore(),
              word2.GetWeight());
          word2.addModifierToken(word1);
          word1.SetAsUsed();
        } else if (dependencyType.equals("nsubj")) // nominal subject
        {
          word2.UpdateScore(word1.GetScore(), word1.GetWeight(), word2.GetScore(),
              word2.GetWeight());
          word2.addModifierToken(word1);
          word1.SetAsUsed();
        } else if (dependencyType.equals("nsubjpass")) // passive nominal subject
        {
          word2.UpdateScore(word1.GetScore(), word1.GetWeight(), word2.GetScore(),
              word2.GetWeight());
          word2.addModifierToken(word1);
          word1.SetAsUsed();
        } else if (dependencyType.equals("partmod")) // participial modifier
        {
          word1.UpdateScore(word1.GetScore(), word1.GetWeight(), word2.GetScore(),
              word2.GetWeight());
          word1.addModifierToken(word2);
          word2.SetAsUsed();
        } else if (dependencyType.equals("punct")) // punctuation
        {
          if (word2.GetOriginal().equals("!")) {
            word1.SetWeight(word1.GetWeight() + 1);
            word1.addModifierToken(word2);
          }
        } else if (dependencyType.equals("rcmod")) // relative clause modifier
        {
          word1.UpdateScore(word1.GetScore(), word1.GetWeight(), word2.GetScore(),
              word2.GetWeight());
          word1.addModifierToken(word2);
          word2.SetAsUsed();
        } else if (dependencyType.equals("rel")) // relative
        {
          word2.UpdateScore(word1.GetScore(), word1.GetWeight(), word2.GetScore(),
              word2.GetWeight());
          word2.addModifierToken(word1);
          word1.SetAsUsed();
        } else if (dependencyType.equals("xcomp")) // open clausal complement
        {
          word2.UpdateScore(word1.GetScore(), word1.GetWeight(), word2.GetScore(),
              word2.GetWeight());
          word2.addModifierToken(word1);
          word1.SetAsUsed();
        }
      }
    }
  }

  @Override
  public List<Token> GetTokens() {
    return tokens;
  }

  private void AddAspectScore(Long aspect, float score, int weight) {
    if (this.scoreList.containsKey(aspect)) {
      this.scoreList.put(aspect, (scoreList.get(aspect) * weightList.get(aspect) + score * weight)
          / (weight + weightList.get(aspect)));
      weightList.put(aspect, weightList.get(aspect) + weight);
    } else {
      this.scoreList.put(aspect, score);
      weightList.put(aspect, weight);
    }
  }

  @Override
  protected int GetWeightOfAnAspect(Long aspect) {
    return weightList.get(aspect);
  }

  @Override
  protected double GetScoreOfAnAspect(Long aspect) {
    return scoreList.get(aspect);
  }

  @Override
  public Map<Long, Float> GetScoreInfo() {
    if (scoreList == null) {
      scoreList = new HashMap<Long, Float>();
      weightList = new HashMap<Long, Integer>();
      for (Token t : tokens) {
        if (t.GetAspect() >= 0 && !t.IsUsed() && t.IsModified())
          AddAspectScore(t.GetAspect(), t.GetScore(), t.GetWeight());
      }
    }
    return this.scoreList;
  }

  @Override
  public Map<Long, Integer> GetWeightInfo() {
    if (weightList == null) {
      scoreList = new HashMap<Long, Float>();
      weightList = new HashMap<Long, Integer>();
      for (Token t : tokens) {
        if (t.GetAspect() >= 0 && !t.IsUsed() && t.IsModified())
          AddAspectScore(t.GetAspect(), t.GetScore(), t.GetWeight());
      }
    }
    return weightList;
  }

  @Override
  public String toString() {
    String result = "";
    for (Token t : tokens)
      result += t.toString() + " ";
    return result;
  }
}
