/**************************************************************************
 *
 * Copyright (c) 2011 UbiPOL Project, All Rights Reserved.
 *
 **************************************************************************/

package eu.ubipol.opinionmining;

import eu.ubipol.opinionmining.Domain;





/**************************************************************************
 *
 * A feature of a domain. A feature can be though of as a word or
 * short sequence of words with semantics relevant to the domain.
 *
 **************************************************************************/

public interface Feature {





/**************************************************************************
 *
 * Retrieves this feature unique ID. All the features in one opinion
 * mining engine must have unique IDs.
 *
 * @return This feature unique ID.
 *
 **************************************************************************/

    Long getId();





/**************************************************************************
 *
 * @return The human readable text of this feature.
 *
 **************************************************************************/

    String getText();





/**************************************************************************
 *
 * @return The domain this feature is belongs to.
 *
 **************************************************************************/

    Domain getDomain();


}





/**************************************************************************
 *
 * 
 *
 **************************************************************************/

