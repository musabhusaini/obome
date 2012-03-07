/**************************************************************************
 *
 * Copyright (c) 2011 UbiPOL Project, All Rights Reserved.
 *
 **************************************************************************/

package eu.ubipol.opinionmining;

/**************************************************************************
 * 
 * A point in a sentiment map, resulting from performing domain analysis for a specific domain. Each
 * point is a three element tuplet: feature, polarity, frequency.
 * 
 **************************************************************************/

public interface FeatureSentiment {

  /**************************************************************************
   * 
   * @return The feature this point in the sentiment map pertains to.
   * 
   **************************************************************************/

  Feature getFeature();

  /**************************************************************************
   * 
   * @return The polarity value of this point in the sentiment map.
   * 
   **************************************************************************/

  int getPolarity();

  /**************************************************************************
   * 
   * @return The value of the frequency of this point in the sentiment map.
   * 
   **************************************************************************/

  float getFrequency();

}

/**************************************************************************
 *
 * 
 *
 **************************************************************************/

