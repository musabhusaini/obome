package nlp_engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nlp_engine.Utils.WordType;
import stem_engine.Stemmer;
import database_connector.DatabaseAdapter;

public class Token {
  private Token parentToken;
  private List<Token> childTokens;
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
  private int relativeBeginPosition;
  private int relativeEndPosition;

  protected Token(String original, String typeString, Token parentToken, String parentEdge,
      int index, int beginPosition, int endPosition, DatabaseAdapter adp, int sentenceBeginPosition) {
    childTokens = new ArrayList<Token>();
    scoreList = new HashMap<Long, Float>();
    weightList = new HashMap<Long, Integer>();
    this.parentToken = parentToken;
    originalText = original.trim();
    parentEdgeType = parentEdge;
    stemmedTexts = Stemmer.GetStems(original.toLowerCase(), "h,p,s");
    stemmedTexts.add(0, original.toLowerCase());
    tokenIndex = index;
    this.beginPosition = beginPosition;
    this.endPosition = endPosition;
    relativeBeginPosition = beginPosition - sentenceBeginPosition;
    relativeEndPosition = endPosition - sentenceBeginPosition;
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
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      case "advmod":
        // very good advmod(good, very)
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.UpdateScore(GetProcessedScore());
        }
        break;
      case "amod":
        // Sam eats red meat amod(meat, red)
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      case "appos":
        // Bill (John's cousin) appos(Bill, cousin)
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
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
        UpdateScore(parentToken.GetProcessedScore(), parentToken.GetWeight());
        if (IsAKeyword())
          AddAspectScore(GetProcessedScore(), GetWeight(), GetAspectId());
        break;
      case "csubjpass":
        // That she lied was suspected by everyone csubjpass(suspected, lied)
        UpdateScore(parentToken.GetProcessedScore(), parentToken.GetWeight());
        if (IsAKeyword())
          AddAspectScore(GetProcessedScore(), GetWeight(), GetAspectId());
        break;
      case "dobj":
        // They win the lottery dobj(win, lottery)
        UpdateScore(parentToken.GetProcessedScore(), parentToken.GetWeight());
        if (IsAKeyword())
          AddAspectScore(GetProcessedScore(), GetWeight(), GetAspectId());
        break;
      case "neg":
        // parent ýn weight ini negatifleþtir
        parentToken.Negate();
        break;
      case "npadvmod":
        // The silence is itself significant npadvmod(significant, itself)
        UpdateScore(parentToken.GetProcessedScore(), parentToken.GetWeight());
        if (IsAKeyword())
          AddAspectScore(GetProcessedScore(), GetWeight(), GetAspectId());
        break;
      case "nsubj":
        // The baby is cute nsubj(cute, baby)
        UpdateScore(parentToken.GetProcessedScore(), parentToken.GetWeight());
        if (IsAKeyword())
          AddAspectScore(GetProcessedScore(), GetWeight(), GetAspectId());
        break;
      case "nsubjpass":
        // Dole was defeated by Clinton nsubjpass(defeated, Doll)
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      case "partmod":
        // Bill tried to shoot demonstrating his incompetence partmod(shoot, demonstrating)
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      case "rcmod":
        // I saw the man you love rcmod(man, love)
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      case "rel":
        // I saw the man whose wife you love rel(love, wife)
        UpdateScore(parentToken.GetProcessedScore(), parentToken.GetWeight());
        if (IsAKeyword())
          AddAspectScore(GetProcessedScore(), GetWeight(), GetAspectId());
        break;
      case "xcomp":
        // He says that you like to swim xcomp(like, swim)
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
          || (wordType == WordType.VERB && pureScore != 0))
        parentToken.AddAspectScore(pureScore, 1, -1);
      for (Entry<Long, Float> e : GetScoreMap().entrySet())
        parentToken.AddAspectScore(GetProcessedScore(e.getValue()), weightList.get(e.getKey()),
            e.getKey());
    }
  }

  private void AddModifiers(List<ModifierItem> list, boolean included) {
    // acomp advmod amod dobj neg nsubjpass partmod rcmod rel appos csubj csubjpass npadvmod xcomp
    // nsubj
    boolean modifiedByAnother = false;
    for (Token t : childTokens) {
      if (IsAKeyword() || included) {
        if (t.GetParentEdgeType().equals("acomp") || t.GetParentEdgeType().equals("advmod")
            || t.GetParentEdgeType().equals("amod") || t.GetParentEdgeType().equals("appos")
            || t.GetParentEdgeType().equals("csubj") || t.GetParentEdgeType().equals("csubjpass")
            || t.GetParentEdgeType().equals("dobj") || t.GetParentEdgeType().equals("neg")
            || t.GetParentEdgeType().equals("npadvmod") || t.GetParentEdgeType().equals("nsubj")
            || t.GetParentEdgeType().equals("nsubjpass") || t.GetParentEdgeType().equals("partmod")
            || t.GetParentEdgeType().equals("rcmod") || t.GetParentEdgeType().equals("rel")
            || t.GetParentEdgeType().equals("xcomp")) {
          modifiedByAnother = true;
          list.add(new ModifierItem(t, this));
          t.AddModifiers(list, true);
        }
      } else
        t.AddModifiers(list, false);
    }
    if (!modifiedByAnother && IsAKeyword())
      list.add(new ModifierItem(this));
  }

  protected List<ModifierItem> GetModifierList() {
    List<ModifierItem> indexList = new ArrayList<ModifierItem>();
    AddModifiers(indexList, false);
    return indexList;
  }

  protected void UpdateScore(float score, int weight) {
    if (this.score == 0 && this.weight == 1) {
      this.score = score;
      this.weight = weight + 1;
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

  protected Map<Long, Float> GetScoreMap() {
    if (invert) {
      for (Entry<Long, Float> e : scoreList.entrySet())
        e.setValue(e.getValue() * -1);
    }
    return scoreList;
  }

  protected Map<Long, Integer> GetWeightMap() {
    return weightList;
  }

  protected void TransferScores() {
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

  protected String GetASpectName() {
    return Aspects.GetAspectName(GetAspectId());
  }

  public Float GetScore() {
    return score;
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
    return relativeBeginPosition;
  }

  public int GetRelativeEndPosition() {
    return relativeEndPosition;
  }
}