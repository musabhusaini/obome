/**************************************************************************
 *
 * Copyright (c) 2011 UbiPOL Project, All Rights Reserved.
 *
 **************************************************************************/

package eu.ubipol.opinionmining;

/**************************************************************************
 * 
 * The polarity of a feature resulting the polarity analysis of a given comment.
 * 
 **************************************************************************/

public interface FeaturePolarity {

  /**************************************************************************
   * 
   * @return The feature this polarity result pertains to.
   * 
   **************************************************************************/

  Feature getFeature();

  /**************************************************************************
   * 
   * @return A non-negative integer representing the actual value of the comment polarity. Low
   *         values means "bad" and increases to "good".
   * 
   **************************************************************************/

  int getPolarity();

}

/**************************************************************************
 *
 * 
 *
 **************************************************************************/

