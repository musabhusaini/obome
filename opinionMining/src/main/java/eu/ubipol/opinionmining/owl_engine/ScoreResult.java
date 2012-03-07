package eu.ubipol.opinionmining.owl_engine;

public class ScoreResult {
  private Long featureId;
  private int weight;
  private float score;
  private String featureName;

  public ScoreResult(Long featureId, String featureName, int weight, float score) {
    this.featureId = featureId;
    this.weight = weight;
    this.score = score;
    this.featureName = featureName;
  }

  public Long GetFeatureId() {
    return featureId;
  }

  public int GetWeight() {
    return weight;
  }

  public float GetScore() {
    return score;
  }

  public String GetFeatureName() {
    return featureName;
  }
}
