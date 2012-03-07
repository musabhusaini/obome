/**************************************************************************
 *
 * Copyright (c) 2011 UbiPOL Project, All Rights Reserved.
 *
 **************************************************************************/

package eu.ubipol.opinionmining;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**************************************************************************
 * 
 * Defines the operations to be provided by an opinion mining engine.
 * 
 **************************************************************************/

public interface OpinionMiningEngine {

  /**************************************************************************
   * 
   * Retrieves the domains configured in this engine.
   * 
   * @return A set containing all the domains currently configured in this engine.
   * 
   **************************************************************************/

  Set<Domain> getDomains();

  /**************************************************************************
   * 
   * Retrieves the features of a specific domain.
   * 
   * @param domain
   *          The domain of the features to retrieve.
   * 
   * @return A set containing all the features currently configured in the given domain.
   * 
   **************************************************************************/

  Set<Feature> getFeaturesOfDomain(Domain domain);

  /**************************************************************************
   * 
   * Registers a comment into the opinion mining engine.
   * 
   * @param comment
   *          The comment to be registered.
   **************************************************************************/

  void registerComment(Comment comment);

  /**************************************************************************
   * 
   * Retrieves the results of polarity analysis for a specific comment.
   * 
   * @param Comment
   *          The comment to get its ratings <code>{@link
   * #registerComment(Comment)}</code>.
   * 
   * @return A set containing the polarities of the relevant features.
   **************************************************************************/

  Set<FeaturePolarity> getCommentPolarity(Comment comment);

  /**************************************************************************
   * 
   * Retrieves the ordered list of top-K features of given domain in a specific time interval.
   * 
   * @param domain
   *          The domain to be scanned.
   * 
   * @param startDate
   *          The start date of the time interval to scan.
   * 
   * @param endDate
   *          The end date of the time interval to scan.
   * 
   * @param featureCount
   *          The maximum number of features to return.
   * 
   * @return A list containing the top-K features. It will ordered with decreasing rank.
   * 
   **************************************************************************/

  List<Feature> findTopFeatures(Domain domain, Date startDate, Date endDate, int featureCount);

  /**************************************************************************
   * 
   * Retrieves the features that are the top emerging features of a given domain in a specific time
   * interval.
   * 
   * @param domain
   *          The domain to be scanned.
   * 
   * @param analysisStartDate
   *          The start date of the time interval to use in the analysis.
   * 
   * @param analysisEndDate
   *          The end date of the time interval to use in the analysis.
   * 
   * @param startDate
   *          The start date of the time interval where emerging features will be looked for.
   * 
   * @param endDate
   *          The end date of the time interval where emerging features will be looked for.
   * 
   **************************************************************************/

  List<Feature> findEmergentFeatures(Domain domain, Date analysisStartDate, Date analysisEndDate,
      Date startDate, Date endDate);

  /**************************************************************************
   * 
   * Retrieves the data points of the sentiment map of a given domain in a specific time interval.
   * 
   * @param domain
   *          The domain to be scanned.
   * 
   * @param startDate
   *          The start date of the time interval to scan.
   * 
   * @param endDate
   *          The end date of the time interval to scan.
   * 
   **************************************************************************/

  Set<FeatureSentiment> getSentimentMap(Domain domain, Date startDate, Date endDate);

}

/**************************************************************************
 *
 * 
 *
 **************************************************************************/

