package controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions.FeatureType;
import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticToken;
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

    public static void single(String uuid) {
    	OntologyLearnerProgram program = new OntologyLearnerProgram();
    	TextDocument doc = program.retrieveTextDocument(UUID.fromString(uuid), FeatureType.SMART_NOUNS);
    	program.close();
    	
    	StringBuilder text = new StringBuilder(doc.getText());
    	List<LinguisticToken> features = Lists.newArrayList(doc.getFeatures());
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
    		
    		text.insert(feature.getAbsoluteEndPosition(), "</a>");
    		text.insert(feature.getAbsoluteBeginPosition(), "<a id='feature_" + feature.getText() + "' class='feature' href='#'>");
    	}
    	
    	renderJSON(new Document(text.toString()));
    }
}