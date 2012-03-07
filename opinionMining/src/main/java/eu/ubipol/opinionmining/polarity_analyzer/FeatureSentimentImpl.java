package eu.ubipol.opinionmining.polarity_analyzer;

import eu.ubipol.opinionmining.Feature;
import eu.ubipol.opinionmining.FeatureSentiment;

public class FeatureSentimentImpl implements FeatureSentiment {

  private Feature feature;
  private int polarity;
  private float frequency;

  public FeatureSentimentImpl(Feature sentimentFeature, int polarityValue, float sentimentFrequency) {
    feature = sentimentFeature;
    polarity = polarityValue;
    frequency = sentimentFrequency;
  }

  public Feature getFeature() {
    return feature;
  }

  public int getPolarity() {
    return polarity;
  }

  public float getFrequency() {
    return frequency;
  }

}
