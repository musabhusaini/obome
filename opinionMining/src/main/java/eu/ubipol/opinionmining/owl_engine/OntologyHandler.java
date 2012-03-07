package eu.ubipol.opinionmining.owl_engine;

import java.util.Date;
import java.util.List;
import java.util.Map;

import eu.ubipol.opinionmining.Feature;

public abstract class OntologyHandler {
  public static OntologyHandler CreateOntologyHandler(String ontologyId, String ontologyName,
      String path) {
    // TODO:Ontology Handler for Owl File
    return null;
  };

  public static OntologyHandler CreateOntologyHandler(String connectionString, Long domainId)
      throws Exception {
    return new OntologyHandlerForDatabase(connectionString, domainId);
  }

  public abstract void CloseOntology();

  public abstract String GetDomainName() throws Exception;

  public abstract boolean HasOntology(Long domainId) throws Exception;

  public abstract void CreateOntology(String domainName) throws Exception;

  public abstract Map<Long, String> GetFeatures() throws Exception;

  public abstract Map<Long, String> GetKeywords(Long featureId) throws Exception;

  public abstract Long GetFeatureOfAWord(String word) throws Exception;

  public abstract float GetWordScore(String word, int typeId) throws Exception;

  public abstract List<ScoreResult> GetScoreCard(Long commentId) throws Exception;

  public abstract String GetFeatureName(Long featureId) throws Exception;

  public abstract void AddComment(Long commentId, Date date) throws Exception;

  public abstract Long AddFeature(String featureName) throws Exception;

  public abstract Long AddKeyWord(Long featureId, String keywordName) throws Exception;

  public abstract Long AddPolarityWord(int wordType, float score, String word) throws Exception;

  public abstract void AddScoreCardItem(Long commentId, Long featureId, float score, int weight)
      throws Exception;

  public abstract List<Feature> GetOrderedFeatures(Date startDate, Date endDate, int k)
      throws Exception;

  public abstract Map<Long, Integer> GetFeatureCountBetweenDates(Date startDate, Date endDate)
      throws Exception;

  public abstract Map<Long, Integer> GetSumOfFeatureWeightsBetweenDates(Date startDate, Date endDate)
      throws Exception;

  public abstract Map<Long, Float> GetSumOfFeatureScoresBetweenDates(Date startDate, Date endDate)
      throws Exception;
}
