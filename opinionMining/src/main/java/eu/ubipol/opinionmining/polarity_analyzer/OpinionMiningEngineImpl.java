package eu.ubipol.opinionmining.polarity_analyzer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import eu.ubipol.opinionmining.Comment;
import eu.ubipol.opinionmining.Domain;
import eu.ubipol.opinionmining.Feature;
import eu.ubipol.opinionmining.FeaturePolarity;
import eu.ubipol.opinionmining.FeatureSentiment;
import eu.ubipol.opinionmining.OpinionMiningEngine;
import eu.ubipol.opinionmining.nlp_engine.Paragraph;
import eu.ubipol.opinionmining.owl_engine.OntologyHandler;
import eu.ubipol.opinionmining.owl_engine.ScoreResult;

public class OpinionMiningEngineImpl implements OpinionMiningEngine {

  private String connectionString;

  private OpinionMiningEngineImpl(String connectionString) {
    this.connectionString = connectionString;
  }

  public static OpinionMiningEngine createOpinionMiningEngine(String connectionString) {
    return new OpinionMiningEngineImpl(connectionString);
  }

  public Set<Domain> getDomains() {
    try {
      Class.forName("org.postgresql.Driver");
      Connection conn = DriverManager.getConnection(connectionString);
      Set<Domain> domains = new HashSet<Domain>();
      Statement stmt = conn.createStatement();
      ResultSet set = stmt.executeQuery("select domainId, domainName from DOMAINS");
      while (set.next()) {
        domains.add(new DomainImpl(set.getLong("domainId"), set.getString("domainName")));
      }
      set.close();
      stmt.close();
      conn.close();
      return domains;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public Set<Feature> getFeaturesOfDomain(Domain domain) {
    try {
      OntologyHandler temp = OntologyHandler
          .CreateOntologyHandler(connectionString, domain.getId());
      Map<Long, String> featureMap = temp.GetFeatures();
      Set<Feature> features = new HashSet<Feature>();
      for (Entry<Long, String> e : featureMap.entrySet())
        features.add(new FeatureImpl(e.getKey(), e.getValue(), domain));
      temp.CloseOntology();
      temp = null;
      featureMap = null;

      return features;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public void registerComment(Comment comment) {
    try {
      OntologyHandler temp = OntologyHandler.CreateOntologyHandler(connectionString, comment
          .getDomain().getId());
      if (!temp.HasOntology(comment.getDomain().getId()))
        temp.CreateOntology(comment.getDomain().getText());
      Paragraph p = Paragraph.GetParagraphInstanceForStanford(comment.getText(), temp);
      Map<Long, Float> scores = p.GetScoreInfo();
      for (Entry<Long, Float> e : scores.entrySet()) {
        temp.AddComment(comment.getId(), new java.sql.Date(comment.getDate().getTime()));
        temp.AddScoreCardItem(comment.getId(), e.getKey(), e.getValue(),
            p.GetWeightInfo().get(e.getKey()));
      }
      temp.CloseOntology();
      p = null;
      scores = null;
      temp = null;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private int ConvertDoubleScoresToInt(float score) {
    int result = 0;
    if (score <= -0.6)
      result = 1;
    else if (score <= -0.3)
      result = 2;
    else if (score < 0)
      result = 3;
    else if (score == 0)
      result = 4;
    else if (score < 0.3)
      result = 5;
    else if (score < 0.6)
      result = 6;
    else if (score <= 1.0)
      result = 7;
    return result;
  }

  public Set<FeaturePolarity> getCommentPolarity(Comment comment) {
    try {
      OntologyHandler temp = OntologyHandler.CreateOntologyHandler(connectionString, comment
          .getDomain().getId());

      Set<FeaturePolarity> polarities = new HashSet<FeaturePolarity>();

      List<ScoreResult> result = temp.GetScoreCard(comment.getId());
      for (ScoreResult r : result)
        polarities.add(new FeaturePolarityImpl(new FeatureImpl(r.GetFeatureId(),
            r.GetFeatureName(), comment.getDomain()), ConvertDoubleScoresToInt(r.GetScore())));

      temp.CloseOntology();
      temp = null;
      result = null;
      return polarities;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public List<Feature> findTopFeatures(Domain domain, Date startDate, Date endDate, int featureCount) {
    try {
      OntologyHandler temp = OntologyHandler
          .CreateOntologyHandler(connectionString, domain.getId());
      List<Feature> result = temp.GetOrderedFeatures(startDate, endDate, featureCount);
      temp.CloseOntology();
      temp = null;
      return result;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public List<Feature> findEmergentFeatures(Domain domain, Date analysisStartDate,
      Date analysisEndDate, Date startDate, Date endDate) {
    try {
      OntologyHandler temp = OntologyHandler
          .CreateOntologyHandler(connectionString, domain.getId());
      Map<Long, Integer> analysisResult = temp.GetFeatureCountBetweenDates(analysisStartDate,
          analysisEndDate);
      Map<Long, Integer> focusedResult = temp.GetFeatureCountBetweenDates(startDate, endDate);
      double analysisCount = 0.0;
      double focusedCount = 0.0;
      for (Entry<Long, Integer> e : focusedResult.entrySet())
        focusedCount += (double) e.getValue();

      for (Entry<Long, Integer> e : analysisResult.entrySet())
        analysisCount += (double) e.getValue();

      List<Feature> result = new ArrayList<Feature>();
      for (Entry<Long, Integer> e : focusedResult.entrySet()) {
        if (!analysisResult.containsKey(e.getKey()) && e.getValue() / focusedCount >= 0.05)
          result.add(new FeatureImpl(e.getKey(), temp.GetFeatureName(e.getKey()), domain));
        else if (analysisResult.containsKey(e.getKey())
            && ((double) (analysisResult.get(e.getKey())) / analysisCount)
                - ((double) (e.getValue()) / focusedCount) >= 0.05)
          result.add(new FeatureImpl(e.getKey(), temp.GetFeatureName(e.getKey()), domain));
      }
      temp.CloseOntology();
      temp = null;
      focusedResult = null;
      analysisResult = null;
      return result;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public Set<FeatureSentiment> getSentimentMap(Domain domain, Date startDate, Date endDate) {
    try {
      OntologyHandler temp = OntologyHandler
          .CreateOntologyHandler(connectionString, domain.getId());
      Map<Long, Integer> weights = temp.GetSumOfFeatureWeightsBetweenDates(startDate, endDate);
      Map<Long, Float> scores = temp.GetSumOfFeatureScoresBetweenDates(startDate, endDate);
      Map<Long, Integer> counts = temp.GetFeatureCountBetweenDates(startDate, endDate);
      Set<FeatureSentiment> result = new HashSet<FeatureSentiment>();
      float count = new Float(0.0);
      for (Entry<Long, Integer> e : counts.entrySet())
        count += (double) e.getValue();

      for (Entry<Long, Integer> e : weights.entrySet()) {
        result.add(new FeatureSentimentImpl(new FeatureImpl(e.getKey(), temp.GetFeatureName(e
            .getKey()), domain), ConvertDoubleScoresToInt(scores.get(e.getKey()) / e.getValue()),
            new Float(counts.get(e.getKey()) / count)));
      }
      weights = null;
      scores = null;
      counts = null;
      temp.CloseOntology();
      temp = null;
      return result;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
