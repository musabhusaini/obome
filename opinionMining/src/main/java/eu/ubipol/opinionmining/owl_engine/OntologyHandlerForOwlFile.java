package eu.ubipol.opinionmining.owl_engine;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.sun.xml.internal.fastinfoset.stax.events.Util;

import eu.ubipol.opinionmining.nlp_engine.Utils.TokenType;

public class OntologyHandlerForOwlFile {
  private OwlFile owlFile;

  // [start] Ontology Creation Members
  public OntologyHandlerForOwlFile(String ontologyName, String path)
      throws OWLOntologyCreationException, OWLOntologyStorageException {
    owlFile = OwlFile.PrepareOntologyFile(ontologyName, path);
  }

  public void SaveOntology() throws OWLOntologyStorageException {
    owlFile.SaveOntology();
  }

  public List<String> GetSubClasses(String parent) {
    List<String> classes = new ArrayList<String>();
    classes = owlFile.GetNamesOfSubClasses(parent);
    return classes;
  }

  public List<String> GetParentClasses(String className) {
    List<String> classes = new ArrayList<String>();
    classes = owlFile.GetNamesOfParentClasses(className);
    return classes;
  }

  public void AddAspect(String aspect) throws OWLOntologyStorageException {
    owlFile.AddSubClass(Utils.ASPECT_KEYWORD, Utils.GetAspectString(aspect));
  }

  public void AddKeywordToAspect(String aspect, String keyword) throws OWLOntologyStorageException {
    owlFile.AddInstanceToClass(Utils.GetKeywordString(keyword), Utils.GetAspectString(aspect));
  }

  public void AddAdjective(String word, double polarityValue) throws OWLOntologyStorageException {
    owlFile.AddInstanceToClass(Utils.GetPolarityWordString(word, Utils.ADJECTIVE_PREFIX),
        Utils.ADJECTIVE_KEYWORD);
    owlFile.AddValueToInstance(Utils.GetPolarityWordString(word, Utils.ADJECTIVE_PREFIX),
        polarityValue, Utils.POLARITYVALUE_KEYWORD);
  }

  public void AddAdverb(String word, double polarityValue) throws OWLOntologyStorageException {
    owlFile.AddInstanceToClass(Utils.GetPolarityWordString(word, Utils.ADVERB_PREFIX),
        Utils.ADVERB_KEYWORD);
    owlFile.AddValueToInstance(Utils.GetPolarityWordString(word, Utils.ADVERB_PREFIX),
        polarityValue, Utils.POLARITYVALUE_KEYWORD);
  }

  public void AddVerb(String word, double polarityValue) throws OWLOntologyStorageException {
    owlFile.AddInstanceToClass(Utils.GetPolarityWordString(word, Utils.VERB_PREFIX),
        Utils.VERB_KEYWORD);
    owlFile.AddValueToInstance(Utils.GetPolarityWordString(word, Utils.VERB_PREFIX), polarityValue,
        Utils.POLARITYVALUE_KEYWORD);
  }

  public void AddNoun(String word, double polarityValue) throws OWLOntologyStorageException {
    owlFile.AddInstanceToClass(Utils.GetPolarityWordString(word, Utils.NOUN_PREFIX),
        Utils.NOUN_KEYWORD);
    owlFile.AddValueToInstance(Utils.GetPolarityWordString(word, Utils.NOUN_PREFIX), polarityValue,
        Utils.POLARITYVALUE_KEYWORD);
  }

  public void AddComment(String commentId, String text) throws OWLOntologyStorageException {
    owlFile.AddInstanceToClass(Utils.GetCommentString(commentId), Utils.COMMENT_KEYWORD);
    owlFile.AddValueToInstance(Utils.GetCommentString(commentId), Utils.GetCommentTextString(text),
        Utils.COMMENTTEXT_KEYWORD);
  }

  public void AddScorecard(String commentId) throws OWLOntologyStorageException {
    owlFile.AddInstanceToClass(Utils.GetScorecardString(commentId), Utils.SCORECARD_KEYWORD);
  }

  public void AddRating(String commentId, String aspect, double score)
      throws OWLOntologyStorageException {
    owlFile.AddValueToInstance(Utils.GetScorecardString(commentId), score,
        Utils.GetRatingString(aspect));
  }

  public void AddWeight(String commentId, String aspect, int weight)
      throws OWLOntologyStorageException {
    owlFile.AddValueToInstance(Utils.GetScorecardString(commentId), weight,
        Utils.GetWeightString(aspect));
  }

  public void AddDate(String commentId, Date time) throws OWLOntologyStorageException {
    DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
    owlFile.AddValueToInstance(Utils.GetCommentString(commentId), format.format(time),
        Utils.COMMENTDATE_KEYWORD);
  }

  // [end]

  // [start] Ontology Reading Members
  public String GetDomainName() {
    return (GetParentClasses("Polarities").size() > 0 ? Utils.CleanDomainString(GetParentClasses(
        "Polarities").get(0)) : "");
  }

  public List<String> GetAspects() {
    List<String> result = owlFile.GetNamesOfSubClasses(Utils.ASPECT_KEYWORD);
    for (int i = 0; i < result.size(); i++)
      result.set(i, Utils.CleanAspectString(result.get(i)));
    return result;
  }

  public List<String> GetKeywords(String aspect) {
    List<String> result = owlFile.GetNamesOfInstances(Utils.GetAspectString(aspect));
    for (int i = 0; i < result.size(); i++)
      result.set(i, Utils.CleanKeywordString(result.get(i)));
    return result;
  }

  public String GetAspectofKeyword(String word) {
    List<String> aspects = GetAspects();
    for (String s : aspects)
      if (GetKeywords(s).contains(word.trim().toLowerCase()))
        return s;
    return null;
  }

  public double GetPolarityValue(String word, TokenType type) {
    String className = "", wordString = "";
    if (TokenType.ADJECTIVE == type) {
      className = Utils.ADJECTIVE_KEYWORD;
      wordString = Utils.ADJECTIVE_PREFIX;
    } else if (TokenType.ADVERB == type) {
      className = Utils.ADVERB_KEYWORD;
      wordString = Utils.ADVERB_PREFIX;
    } else if (TokenType.VERB == type) {
      className = Utils.VERB_KEYWORD;
      wordString = Utils.VERB_PREFIX;
    } else if (TokenType.NOUN == type) {
      className = Utils.NOUN_KEYWORD;
      wordString = Utils.NOUN_PREFIX;
    } else
      return 0.0;

    wordString += word.trim().toLowerCase();
    String result = owlFile.GetValueOfADataProperty(className, wordString,
        Utils.POLARITYVALUE_KEYWORD);
    // for non found words, you may return -2.0 to send it as unfound
    return (Util.isEmptyString(result) ? 0.0 : Double.parseDouble(result));
  }

  public List<String> GetAdjectives() {
    List<String> result = owlFile.GetNamesOfInstances(Utils.ADJECTIVE_KEYWORD);
    for (int i = 0; i < result.size(); i++)
      result.set(i, Utils.CleanPolarityWord(result.get(i), Utils.ADJECTIVE_PREFIX));
    return result;
  }

  public boolean HasComment(String commentId) {
    return GetComments().contains(commentId);
  }

  public List<String> GetAdverbs() {
    List<String> result = owlFile.GetNamesOfInstances(Utils.ADVERB_KEYWORD);
    for (int i = 0; i < result.size(); i++)
      result.set(i, Utils.CleanPolarityWord(result.get(i), Utils.ADVERB_PREFIX));
    return result;
  }

  public List<String> GetVerbs() {
    List<String> result = owlFile.GetNamesOfInstances(Utils.VERB_KEYWORD);
    for (int i = 0; i < result.size(); i++)
      result.set(i, Utils.CleanPolarityWord(result.get(i), Utils.VERB_PREFIX));
    return result;
  }

  public List<String> GetNouns() {
    List<String> result = owlFile.GetNamesOfInstances(Utils.NOUN_KEYWORD);
    for (int i = 0; i < result.size(); i++)
      result.set(i, Utils.CleanPolarityWord(result.get(i), Utils.NOUN_PREFIX));
    return result;
  }

  public List<String> GetComments() {
    List<String> result = owlFile.GetNamesOfInstances(Utils.COMMENT_KEYWORD);
    for (int i = 0; i < result.size(); i++)
      result.set(i, Utils.CleanCommentString(result.get(i)));
    return result;
  }

  public String GetCommentText(String commentId) {
    String result = owlFile.GetValueOfADataProperty(Utils.COMMENT_KEYWORD,
        Utils.GetCommentString(commentId), Utils.COMMENTTEXT_KEYWORD);
    return (Util.isEmptyString(result) ? "" : result);
  }

  public Date GetCommentDate(String commentId) throws ParseException {
    String result = owlFile.GetValueOfADataProperty(Utils.COMMENT_KEYWORD,
        Utils.GetCommentString(commentId), Utils.COMMENTDATE_KEYWORD);
    if (Util.isEmptyString(result))
      return null;
    else {
      DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
      Date parsedDate = format.parse(result);
      return parsedDate;
    }
  }

  public List<String> GetCommentAspects(String commentId) {
    List<String> result = owlFile.GetNamesOfValues(Utils.SCORECARD_KEYWORD,
        Utils.GetScorecardString(commentId));
    result.remove(Utils.COMMENTTEXT_KEYWORD);
    for (int i = 0; i < result.size(); i++)
      result.set(i, Utils.CleanRatingString(result.get(i)));
    return result;
  }

  public double GetCommentScoreForAnAspect(String commentId, String aspect) {
    String result = owlFile.GetValueOfADataProperty(Utils.SCORECARD_KEYWORD,
        Utils.GetScorecardString(commentId), Utils.GetRatingString(aspect));
    return (Util.isEmptyString(result) ? 0.0 : Double.parseDouble(result));
  }

  public int GetCommentWeightForAnAspect(String commentId, String aspect) {
    String result = owlFile.GetValueOfADataProperty(Utils.SCORECARD_KEYWORD,
        Utils.GetScorecardString(commentId), Utils.GetWeightString(aspect));
    return (Util.isEmptyString(result) ? 1 : Integer.parseInt(result));
  }

  // [end]

  // [start] Ontology Stats Members
  public List<String> GetCommentsBetweenDates(Date startDate, Date endDate) throws ParseException {
    List<String> result = new ArrayList<String>();
    List<String> commentList = GetComments();
    for (String comment : commentList) {
      Date d = GetCommentDate(comment);
      if ((d.after(startDate) && d.before(endDate)) || d.equals(startDate) || d.equals(endDate))
        result.add(comment);
    }
    return result;
  }

  private Map<String, Integer> GetWeightsBetweenDates(Date startDate, Date endDate)
      throws ParseException {
    Map<String, Integer> weights = new HashMap<String, Integer>();
    List<String> comments = GetCommentsBetweenDates(startDate, endDate);
    for (String comment : comments) {
      List<String> aspects = GetCommentAspects(comment);
      for (String aspect : aspects) {
        int weight = GetCommentWeightForAnAspect(comment, aspect);
        if (weights.containsKey(aspect))
          weights.put(aspect, weights.get(aspect) + weight);
        else
          weights.put(aspect, weight);
      }
    }
    return weights;
  }

  public List<String> GetTopKFeatures(Date startDate, Date endDate, int k) throws ParseException {
    Map<String, Integer> weights = GetWeightsBetweenDates(startDate, endDate);
    List<String> result = new ArrayList<String>();
    for (int i = 0; i < k; i++) {
      Entry<String, Integer> maxEntry = null;
      for (Entry<String, Integer> e : weights.entrySet()) {
        if (maxEntry == null || e.getValue() > maxEntry.getValue()) {
          maxEntry = e;
        }
      }
      result.add(maxEntry.getKey());
      weights.remove(maxEntry.getKey());
    }
    return result;
  }

  public List<String> GetEmergingFeatures(Date analysisStart, Date analysisEnd, Date emergingStart,
      Date emergingEnd) throws ParseException {
    Map<String, Integer> analysisWeights = GetWeightsBetweenDates(analysisStart, analysisEnd);
    Map<String, Integer> emergingWeights = GetWeightsBetweenDates(emergingStart, emergingEnd);

    double analysisCount = 0;
    for (Entry<String, Integer> e : analysisWeights.entrySet())
      analysisCount += (double) e.getValue();

    double emergingCount = 0;
    for (Entry<String, Integer> e : emergingWeights.entrySet())
      emergingCount += (double) e.getValue();

    Map<String, Double> frequencies = new HashMap<String, Double>();
    for (Entry<String, Integer> e : emergingWeights.entrySet()) {
      if (!analysisWeights.containsKey(e.getKey()))
        frequencies.put(e.getKey(), (double) e.getValue());
      else {
        double tempFreq = (e.getValue() / emergingCount)
            / (analysisWeights.get(e.getKey()) / analysisCount);
        if (tempFreq > 1.0)
          frequencies.put(e.getKey(), tempFreq);
      }

    }

    List<String> result = new ArrayList<String>();
    while (frequencies.size() > 0) {
      Entry<String, Double> temp = null;
      for (Entry<String, Double> e : frequencies.entrySet()) {
        if (temp == null || e.getValue() > temp.getValue())
          temp = e;
      }
      result.add(temp.getKey());
      frequencies.remove(temp.getKey());
    }
    return result;
  }
  // [end]
}
