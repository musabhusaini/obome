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
  private int weight;
  private String parentEdgeType;
  private int tokenIndex;
  private int beginPosition;
  private int endPosition;
  private boolean invert;

  protected Token(String original, String typeString, Token parentToken, String parentEdge,
      int index, int beginPosition, int endPosition, DatabaseAdapter adp) {
    childTokens = new ArrayList<Token>();
    scoreList = new HashMap<Long, Float>();
    weightList = new HashMap<Long, Integer>();
    this.parentToken = parentToken;
    originalText = original.trim();
    parentEdgeType = parentEdge;
    stemmedTexts = Stemmer.GetStems(original.toLowerCase(), "h,p,s");
    stemmedTexts.add(original.toLowerCase());
    tokenIndex = index;
    this.beginPosition = beginPosition;
    this.endPosition = endPosition;
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

    aspect = new Long(-1);
    for (int i = 0; i < stemmedTexts.size() && aspect == -1; i++)
      aspect = adp.GetAspectId(stemmedTexts.get(i));
  }

  protected void AddChildToken(Token child) {
    childTokens.add(child);
  }

  private float GetProcessedScore() {
    return (invert ? score * -1 : score);
  }

  private void TransferScoreToParent() {
    if (parentEdgeType != null) {
      switch (parentEdgeType) {
      case "acomp":
        // parent a aktar
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      case "advmod":
        // parent a aktar
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      case "amod":
        // parent a aktar
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      case "dobj":
        // parent a aktar
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      case "neg":
        // parent ýn weight ini negatifleþtir
        parentToken.Negate();
        break;
      // case "npadvmod":
      // daha sonra karar verilecek
      // break;
      // case "nsubj":
      // parent ý kendine alacak
      // break;
      case "nsubjpass":
        // parent a aktar
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      case "partmod":
        // parent a aktar
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      case "rcmod":
        // parent a aktar
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      case "rel":
        // parent a aktar
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        else {
          parentToken.UpdateScore(GetProcessedScore(), weight);
        }
        break;
      // case "xcomp":
      // kendine al
      // break;
      default:
        if (IsAKeyword())
          parentToken.AddAspectScore(GetProcessedScore(), weight, aspect);
        break;
      }
      for (Entry<Long, Float> e : GetScoreMap().entrySet())
        parentToken.AddAspectScore(e.getValue(), weightList.get(e.getKey()), e.getKey());
    }
  }

  private void AddModifiers(List<ModifierItem> list, boolean included) {
    // acomp advmod amod dobj neg nsubjpass partmod rcmod rel
    boolean modifiedByAnother = false;
    for (Token t : childTokens) {
      if (IsAKeyword() || included) {
        if (t.GetParentEdgeType().equals("acomp") || t.GetParentEdgeType().equals("advmod")
            || t.GetParentEdgeType().equals("amod") || t.GetParentEdgeType().equals("dobj")
            || t.GetParentEdgeType().equals("neg") || t.GetParentEdgeType().equals("nsubjpass")
            || t.GetParentEdgeType().equals("partmod") || t.GetParentEdgeType().equals("rcmod")
            || t.GetParentEdgeType().equals("rel")) {
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
    this.score = ((this.score * this.weight) + (score * weight)) / (this.weight + weight);
    this.weight += weight;
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

  protected Long GetAspectId() {
    return aspect;
  }

  protected String GetASpectName() {
    return Aspects.GetAspectName(GetAspectId());
  }

  protected Float GetScore() {
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
}
