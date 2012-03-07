package eu.ubipol.opinionmining.polarity_analyzer;

import eu.ubipol.opinionmining.Feature;
import eu.ubipol.opinionmining.FeaturePolarity;

public class FeaturePolarityImpl implements FeaturePolarity {

  Feature feature;
  int polarity;

  public FeaturePolarityImpl(Feature polarityFeature, int polarityValue) {
    feature = polarityFeature;
    polarity = polarityValue;
  }

  public Feature getFeature() {
    return feature;
  }

  public int getPolarity() {
    return polarity;
  }

}
