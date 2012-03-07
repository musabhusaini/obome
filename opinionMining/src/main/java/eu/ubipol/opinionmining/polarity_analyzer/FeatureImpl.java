package eu.ubipol.opinionmining.polarity_analyzer;

import eu.ubipol.opinionmining.Domain;
import eu.ubipol.opinionmining.Feature;

public class FeatureImpl implements Feature {
  private Long id;
  private String name;
  private Domain domain;

  public FeatureImpl(Long featureId, String featureName, Domain featureDomain) {
    id = featureId;
    name = featureName;
    domain = featureDomain;
  }

  public FeatureImpl(String featureName) {
    name = featureName;
  }

  public Long getId() {
    return id;
  }

  public String getText() {
    return name;
  }

  public Domain getDomain() {
    return domain;
  }
}
