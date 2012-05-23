package nlp_engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nlp_engine.Utils.WordType;
import stem_engine.Stemmer;
import database_engine.DatabaseAdapter;

@SuppressWarnings("rawtypes")
public class Token implements Comparable {
  private static List<String> nonPriorTypes = new ArrayList<String>(Arrays.asList(new String[] {
      "csubj", "csubjpass", "dobj", "npadvmod", "nsubj", "rel", "xcomp" }));
  private static List<String> priorTypes = new ArrayList<String>(Arrays.asList(new String[] {
      "acomp", "advmod", "amod", "appos", "neg", "nsubjpass", "partmod", "rcmod" }));
  private Token parentToken;
  private List<Token> childTokens;
  private List<Token> modifiedByList;
  private String originalText;
  private List<String> stemmedTexts;
  private WordType wordType;
  private Map<Long, Float> scoreList;
  private Map<Long, Integer> weightList;
  private long aspect;
  private float score;
  private float pureScore;
  private int weight;
  private String parentEdgeType;
  private int tokenIndex;
  private int beginPosition;
  private int endPosition;
  private boolean invert;
  private int sentenceBeginPosition;

  protected Token(String original, String lemma, String typeString, Token parentToken,
      String parentEdge, int index, int beginPosition, int endPosition, DatabaseAdapter adp,
      int sentenceBeginPosition) {
    modifiedByList = new ArrayList<Token>();
    childTokens = new ArrayList<Token>();
    scoreList = new HashMap<Long, Float>();
    weightList = new HashMap<Long, Integer>();
    this.parentToken = parentToken;
    originalText = original.trim();
    parentEdgeType = parentEdge;
    stemmedTexts = Stemmer.GetStems(lemma.toLowerCase(), "h,p,s");
    stemmedTexts.add(0, lemma.toLowerCase());
    tokenIndex = index;
    this.beginPosition = beginPosition;
    this.endPosition = endPosition;
    this.sentenceBeginPosition = sentenceBeginPosition;
    invert = false;

    if (typeString.length() < 2)
      wordType = WordType.OTHER;
    else {
      String typeDef = typeString.substring(0, 2);
      if (typeDef.equals("JJ"))
        wordType = WordType.ADJECTIVE;
      else if (typeDef.equals("RB"))
        wordType = WordType.ADVERB;
      else if (typeDef.equals("VB"))
        wordType = WordType.VERB;
      else if (typeDef.equals("NN"))
        wordType = WordType.NOUN;
      else
        wordType = WordType.OTHER;
    }

    score = new Float(-2);
    for (int i = 0; i < stemmedTexts.size() && score == -2; i++)
      score = OpinionWords.GetWordScore(stemmedTexts.get(i), wordType);

    if (score == -2) {
      weight = 1;
      score = 0;
    } else
      weight = 1;
    pureScore = score;

    aspect = new Long(-1);
    for (int i = 0; i < stemmedTexts.size() && aspect == -1; i++)
      aspect = adp.GetAspectId(stemmedTexts.get(i));
    // aspect = Aspects.GetAspectOfKeyword(stemmedTexts.get(i));
  }

  protected void AddChildToken(Token child) {
    childTokens.add(child);
  }

  private float GetProcessedScore(float score) {
    return (invert ? score * -1 : score);
  }

  private float GetProcessedScore() {
    return (invert ? score * -1 : score);
  }

  private void TransferScoreToParent() {
    if (parentEdgeType != null) {
      switch (parentEdgeType) {
      case "acomp":
        // She looks very beautiful acomp(looks, beautiful)
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.AddModifier(this);
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      case "advmod":
        // very good advmod(good, very)
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.AddModifier(this);
          parentToken.UpdateScore(GetProcessedScore());
        }
        break;
      case "amod":
        // Sam eats red meat amod(meat, red)
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.AddModifier(this);
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      case "appos":
        // Bill (John's cousin) appos(Bill, cousin)
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.AddModifier(this);
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      // case "aux":
      // He should leave aux(leave, should)
      // if (IsAKeyword())
      // parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
      // else {
      // parentToken.UpdateScore(GetProcessedScore(), weight);
      // }
      // break;
      case "csubj":
        // What she said is true csubj(true, said)
        AddModifier(parentToken);
        UpdateScore(parentToken.GetProcessedScore(), parentToken.GetWeight());
        if (IsAKeyword())
          AddAspectScore(GetProcessedScore(), GetWeight(), GetAspectId());
        break;
      case "csubjpass":
        // That she lied was suspected by everyone csubjpass(suspected, lied)
        AddModifier(parentToken);
        UpdateScore(parentToken.GetProcessedScore(), parentToken.GetWeight());
        if (IsAKeyword())
          AddAspectScore(GetProcessedScore(), GetWeight(), GetAspectId());
        break;
      case "dobj":
        // They win the lottery dobj(win, lottery)
        AddModifier(parentToken);
        UpdateScore(parentToken.GetProcessedScore(), parentToken.GetWeight());
        if (IsAKeyword())
          AddAspectScore(GetProcessedScore(), GetWeight(), GetAspectId());
        break;
      case "neg":
        // parent ýn weight ini negatifleþtir
        parentToken.AddModifier(this);
        parentToken.Negate();
        break;
      case "npadvmod":
        // The silence is itself significant npadvmod(significant, itself)
        AddModifier(parentToken);
        UpdateScore(parentToken.GetProcessedScore(), parentToken.GetWeight());
        if (IsAKeyword())
          AddAspectScore(GetProcessedScore(), GetWeight(), GetAspectId());
        break;
      case "nsubj":
        // The baby is cute nsubj(cute, baby)
        AddModifier(parentToken);
        UpdateScore(parentToken.GetProcessedScore(), parentToken.GetWeight());
        if (IsAKeyword())
          AddAspectScore(GetProcessedScore(), GetWeight(), GetAspectId());
        break;
      case "nsubjpass":
        // Dole was defeated by Clinton nsubjpass(defeated, Doll)
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.AddModifier(this);
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      case "partmod":
        // Bill tried to shoot demonstrating his incompetence partmod(shoot, demonstrating)
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.AddModifier(this);
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      case "rcmod":
        // I saw the man you love rcmod(man, love)
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.AddModifier(this);
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      case "rel":
        // I saw the man whose wife you love rel(love, wife)
        AddModifier(parentToken);
        UpdateScore(parentToken.GetProcessedScore(), parentToken.GetWeight());
        if (IsAKeyword())
          AddAspectScore(GetProcessedScore(), GetWeight(), GetAspectId());
        break;
      case "xcomp":
        // He says that you like to swim xcomp(like, swim)
        AddModifier(parentToken);
        UpdateScore(parentToken.GetProcessedScore(), parentToken.GetWeight());
        if (IsAKeyword())
          AddAspectScore(GetProcessedScore(), GetWeight(), GetAspectId());
        break;
      default:
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(score), weight, aspect);
        break;
      }
      if (wordType == WordType.ADJECTIVE || wordType == WordType.ADVERB
          || (wordType == WordType.NOUN && pureScore != 0)
          || (wordType == WordType.VERB && pureScore != 0)) { // add overall score
        parentToken.AddAspectScore(pureScore, 1, -1);
      }
      for (Entry<Long, Float> e : GetScoreMap().entrySet())
        parentToken.AddAspectScore(GetProcessedScore(e.getValue()), weightList.get(e.getKey()),
            e.getKey());
    } else {
      if (wordType == WordType.ADJECTIVE || wordType == WordType.ADVERB
          || (wordType == WordType.NOUN && pureScore != 0)
          || (wordType == WordType.VERB && pureScore != 0)) { // add overall score
        AddAspectScore(pureScore, 1, -1);
      }
      if (IsAKeyword())
        AddAspectScore(GetProcessedScore(), GetWeight(), GetAspectId());
    }
  }

  private void AddModifier(Token modifier) {
    modifiedByList.add(modifier);
  }

  protected List<Token> GetModifierList() {
    return modifiedByList;
  }

  protected void UpdateScore(float score, int weight) {
    if (this.score == 0 && this.weight == 1) {
      this.score = score;
      this.weight = weight + 1;
    } else if (score == 0 && weight == 1) {
      // do nothing
    } else {
      this.score = ((this.score * this.weight) + (score * weight)) / (this.weight + weight);
      this.weight += weight;
    }
  }

  protected void UpdateScore(float score) {
    if (this.score < 0)
      this.score = (Math.abs(this.score) + (1 - Math.abs(this.score)) / (1 / score)) * -1;
    else if (this.score > 0) {
      this.score = Math.abs(this.score) + (1 - Math.abs(this.score)) / (1 / score);
    } else
      this.score = score;
    weight++;
  }

  protected void AddAspectScore(float score, int weight, long aspectId) {
    if (!scoreList.containsKey(aspectId)) {
      scoreList.put(aspectId, score);
      weightList.put(aspectId, weight);
    } else {
      scoreList.put(aspectId, (scoreList.get(aspectId) * weightList.get(aspectId) + score * weight)
          / (weightList.get(aspectId) + weight));
      weightList.put(aspectId, weightList.get(aspectId) + weight);
    }
  }

  protected void Negate() {
    invert = !invert;
  }

  protected int GetIndex() {
    return tokenIndex;
  }

  public String GetOriginal() {
    return originalText;
  }

  protected List<Token> GetChildrenList() {
    return childTokens;
  }

  protected Map<Long, Float> GetScoreMap() {
    if (invert) {
      Map<Long, Float> tempScoreMap = new HashMap<Long, Float>();
      for (Entry<Long, Float> e : scoreList.entrySet()) {
        tempScoreMap.put(e.getKey(), e.getValue() * -1);
      }
      return tempScoreMap;
    } else
      return scoreList;
  }

  protected Map<Long, Integer> GetWeightMap() {
    return weightList;
  }

  @SuppressWarnings("unchecked")
  protected void TransferScores() {
    Collections.sort(childTokens);
    if (childTokens.size() != 0) {
      for (Token t : childTokens)
        t.TransferScores();
    }
    TransferScoreToParent();
  }

  public boolean IsAKeyword() {
    return (aspect >= 0);
  }

  public Long GetAspectId() {
    return aspect;
  }

  public Float GetScore() {
    return score;
  }

  public Float GetPureScore() {
    return pureScore;
  }

  protected int GetWeight() {
    return weight;
  }

  protected String GetParentEdgeType() {
    return parentEdgeType;
  }

  public int GetBeginPosition() {
    return beginPosition;
  }

  public int GetEndPosition() {
    return endPosition;
  }

  public int GetRelativeBeginPosition() {
    return beginPosition - sentenceBeginPosition;
  }

  public int GetRelativeEndPosition() {
    return endPosition - sentenceBeginPosition;
  }

  @Override
  public int compareTo(Object o) {
    Token t = (Token) o;
    if (priorTypes.contains(t.GetParentEdgeType()) && priorTypes.contains(this.GetParentEdgeType()))
      return 0;
    else if (nonPriorTypes.contains(t.GetParentEdgeType())
        && nonPriorTypes.contains(this.GetParentEdgeType()))
      return 0;
    else if (!priorTypes.contains(t.GetParentEdgeType())
        && !priorTypes.contains(this.GetParentEdgeType())
        && !nonPriorTypes.contains(t.GetParentEdgeType())
        && !nonPriorTypes.contains(this.GetParentEdgeType()))
      return 0;
    else if (priorTypes.contains(t.GetParentEdgeType()))
      return 1;
    else if (priorTypes.contains(this.GetParentEdgeType()))
      return -1;
    else if (nonPriorTypes.contains(t.GetParentEdgeType()))
      return 1;
    else if (nonPriorTypes.contains(this.GetParentEdgeType()))
      return -1;
    return 0;
  }
}