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
import models.*;

public class Documents extends Controller {

    public static void list() {
    	OntologyLearnerProgram program = new OntologyLearnerProgram();
    	Iterable<String> identifierList = Lists.newArrayList(Iterables.transform(program.retrieveExistingClusterHeads(), new Function<UUID,String>() {
			@Override
			public String apply(UUID arg0) {
				return arg0.toString();
			}
    		
    	}));
    	program.close();
    	renderJSON(identifierList);
    }

    public static void single(String uuid, boolean bypassCache) {
    	Document doc = (!bypassCache ? Cache.get(uuid, Document.class) : null);
    	
    	if (doc == null) {
	    	OntologyLearnerProgram program = new OntologyLearnerProgram();
	    	TextDocument textDoc = program.retrieveTextDocument(UUID.fromString(uuid), FeatureType.SMART_NOUNS);
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
	    	
	    	doc = new Document(text.toString());
	    	Cache.set(textDoc.getIdentifier().toString(), doc, "1h");
    	}
    	
    	renderJSON(doc);
    }
}