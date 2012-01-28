package controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions.FeatureType;
import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticToken;
import play.cache.Cache;
import play.mvc.*;
import play.mvc.results.Redirect;
import models.*;

public class Documents extends Controller {

	private static String makeCacheId(String uuid, FeatureType ft) {
		return (uuid + "-" + ft.toString());
	}
	
    public static void list() {
    	Function<UUID,String> uuidToString = new Function<UUID,String>() {
			@Override
			public String apply(UUID arg0) {
				return arg0.toString();
			}
    		
    	};
    	
    	OntologyLearnerProgram program = new OntologyLearnerProgram();
    	ClusterHeads clusterHeads = new ClusterHeads(Lists.newArrayList(Iterables.transform(program.retrieveSeenClusterHeads(), uuidToString)),
    			Lists.newArrayList(Iterables.transform(program.retrieveUnseenClusterHeads(), uuidToString)));
    	program.close();
    	renderJSON(clusterHeads);
    }

    public static void nextBest(String featureType, boolean bypassCache) {
    	OntologyLearnerProgram program = new OntologyLearnerProgram();
    	UUID uuid = program.getNextBestUnseenDocumentIdentifier();
    	program.close();
    	
    	redirect("Documents.single", uuid.toString(), featureType, bypassCache);
    }
    
    public static void single(String uuid, String featureType, boolean bypassCache) {
    	FeatureType ft;
    	try {
    		ft = FeatureType.valueOf(featureType);
    	} catch(Exception e) {
    		System.out.println("No feature type called " + featureType);
    		ft = FeatureType.NONE;
    	}

    	Document doc = (!bypassCache ? Cache.get(makeCacheId(uuid, ft), Document.class) : null);

    	if (doc == null) {
	    	OntologyLearnerProgram program = new OntologyLearnerProgram();
	    	TextDocument textDoc = program.retrieveTextDocument(UUID.fromString(uuid), ft);
	    	program.close();
	    	
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
	    	
	    	doc = new Document(textDoc.getIdentifier().toString(), text.toString());
	    	Cache.set(makeCacheId(textDoc.getIdentifier().toString(), ft), doc, "1h");
    	} else {
    		System.out.println("Found it! Loading from cache...");
    	}
    	
    	renderJSON(doc);
    }
    
    public static void see(String uuid) {
    	OntologyLearnerProgram program = new OntologyLearnerProgram();
    	boolean success = program.seeCluster(UUID.fromString(uuid));
    	program.close();
    	
    	if (!success) {
    		throw new IllegalStateException("Could not update cluster status.");
    	}
    	
    	renderText("Success");
    }
}