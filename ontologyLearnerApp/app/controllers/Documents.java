package controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions;
import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions.FeatureType;
import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.experiment.models.Review;
import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticToken;
import play.cache.Cache;
import play.mvc.*;
import play.mvc.results.Redirect;
import models.*;

public class Documents extends Application {
    
    public static void single(String document, String featureType) {
    	class DocFetch implements Function<String,DocumentViewModel> {
			
    		private FeatureType ft;
    		
    		public DocFetch(FeatureType ft) {
    			this.ft = ft;
    		}
    		
    		@Override
			public DocumentViewModel apply(String uuid) {
				Review review = fetch(Review.class, uuid);
		    	TextDocumentOptions options = new TextDocumentOptions();
		    	options.setFeatureType(this.ft);
		    	TextDocument textDoc = review.getTaggedContent(options);
		    	
		    	StringBuilder text = new StringBuilder(textDoc.getText());
		    	List<LinguisticToken> features = Lists.newArrayList(textDoc.getFeatures());
		    	Collections.sort(features, Collections.reverseOrder(new Comparator<LinguisticToken>() {
					@Override
					public int compare(LinguisticToken arg0, LinguisticToken arg1) {
						return arg0.getAbsoluteEndPosition() - arg1.getAbsoluteBeginPosition();
					}
		    	}));
		    	
		    	for (LinguisticToken feature : features) {
		    		String substring = text.substring(feature.getAbsoluteBeginPosition(), feature.getAbsoluteEndPosition());
		    		if (!substring.equals(feature.getText())) {
		    			System.err.println(feature.getText() + " != " + substring);
		    			continue;
		    		}
		    		
		    		text.insert(feature.getAbsoluteEndPosition(), "}");
		    		text.insert(feature.getAbsoluteBeginPosition(), "\\feature{");
		    	}
		    	
		    	return new DocumentViewModel(textDoc.getIdentifier().toString(), text.toString());
			}
    	}
    	
    	FeatureType ft = FeatureType.NONE;
    	if (featureType != null) {
	    	try {
	    		ft = FeatureType.valueOf(featureType.toUpperCase());
	    	} catch(Exception e) {
	    		System.out.println("No feature type called " + featureType);
	    	}
    	}
    	
    	renderJSON(fetch(DocumentViewModel.class, document, "-parsed-" + featureType, new DocFetch(ft)));
    }
}