package controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import play.Logger;

import models.DisplayFeatureModel;
import models.DocumentViewModel;
import models.OpinionCollectionItemViewModel;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions;
import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions.FeatureType;
import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.experiment.models.BacklogToken;
import edu.sabanciuniv.dataMining.experiment.models.Keyword;
import edu.sabanciuniv.dataMining.experiment.models.OpinionDocument;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCoverItem;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticToken;

public class Documents extends Application {
    
    public static void single(String collection, String document) {
    	class DocFetch implements Function<String,DocumentViewModel> {
			
    		private FeatureType ft;
    		private SetCover sc;
    		
    		public DocFetch(SetCover sc, FeatureType ft) {
    			this.sc = sc;
    			this.ft = ft;
    		}
    		
    		@Override
			public DocumentViewModel apply(String uuid) {
				OpinionDocument opinionDocument = fetch(OpinionDocument.class, uuid);
		    	TextDocumentOptions options = new TextDocumentOptions();
		    	options.setFeatureType(this.ft);
		    	TextDocument textDoc = opinionDocument.getTaggedContent(options);
		    	
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
		    			Logger.error("While parsing features %s != %s", feature.getText(), substring);
		    			continue;
		    		}
		    		
		    		DisplayFeatureModel featureModel = new DisplayFeatureModel();
		    		featureModel.content = feature.getText();
		    		featureModel.type = "feature";
		    		
		    		JsonObject featureJson = featureModel.toJson();
		    		featureJson.addProperty("lemma", feature.getLemma());
		    		
		    		BacklogToken backlogToken = Iterables.getFirst(em().createQuery("SELECT bt FROM BacklogToken bt " +
		    				"WHERE bt.setCover=:sc AND bt.label=:label", BacklogToken.class)
		    			.setParameter("sc", this.sc)
		    			.setParameter("label", feature.getLemma())
		    			.setMaxResults(1)
		    			.getResultList(), null);
		    		
		    		Keyword keyword = null;
		    		
		    		if (backlogToken != null) {
		    			featureJson.addProperty("isSeen", true);
		    			keyword = backlogToken.getKeyword();
		    		} else {
		    			keyword = Iterables.getFirst(em().createQuery("SELECT kw FROM Keyword kw " +
								"WHERE kw.aspect.setCover=:sc AND kw.label=:label", Keyword.class)
							.setParameter("sc", sc)
							.setParameter("label", feature.getLemma())
							.setMaxResults(1)
							.getResultList(), null);
		    		}
		    		
		    		if (keyword != null) {
		    			featureJson.addProperty("aspect", keyword.getAspect().getLabel());
		    		}
		    		
		    		text.replace(feature.getAbsoluteBeginPosition(), feature.getAbsoluteEndPosition(), String.format("\\{%s}\\",
		    				featureJson.toString()));
		    	}
		    	
		    	return new DocumentViewModel(textDoc.getIdentifier().toString(), text.toString());
			}
    	}
    	
    	FeatureType ft = FeatureType.NOUNS;
//    	if (featureType != null) {
//	    	try {
//	    		ft = FeatureType.valueOf(featureType.toUpperCase());
//	    	} catch(Exception e) {
//	    		Logger.error(constructGenericLogMessage("no feature type called \"" + featureType + "\""));
//	    	}
//    	}
    	
    	renderJSON(fetch(DocumentViewModel.class, document, "-parsed-" + ft,
    			new DocFetch(fetch(SetCover.class, collection), ft)));
    }
    
    public static void seeDocument(String item) {
    	SetCoverItem scItem = fetch(SetCoverItem.class, item);
    	OpinionDocument document = scItem.getOpinionDocument();
    	SetCover setCover = scItem.getSetCover();
    	
    	TextDocumentOptions options = new TextDocumentOptions();
    	options.setFeatureType(FeatureType.NOUNS);
    	TextDocument textDoc = document.getTaggedContent(options);

    	for (LinguisticToken token : textDoc.getFeatures()) {
    		BacklogToken backlogToken = Iterables.getFirst(em().createQuery("SELECT bt FROM BacklogToken bt " +
    				"WHERE bt.setCover=:sc AND bt.label=:label", BacklogToken.class)
    			.setParameter("sc", setCover)
    			.setParameter("label", token.getLemma())
    			.setMaxResults(1)
    			.getResultList(), null);

    		Keyword keyword = null;
    		
    		if (backlogToken == null || backlogToken.getKeyword() == null) {
				keyword = Iterables.getFirst(em().createQuery("SELECT kw FROM Keyword kw " +
						"WHERE kw.aspect.setCover=:sc AND kw.label=:label", Keyword.class)
					.setParameter("sc", setCover)
					.setParameter("label", token.getLemma())
					.setMaxResults(1)
					.getResultList(), null);
    		}
    		
    		if (backlogToken == null) {
    			backlogToken = new BacklogToken()
    				.setLabel(token.getLemma())
    				.setSetCover(setCover)
    				.setKeyword(keyword);
    			
    			em().persist(backlogToken);
    		} else if (keyword != null) {
    			backlogToken.setKeyword(keyword);
    			em().merge(backlogToken);
    		}
    	}
    	
    	renderJSON(new OpinionCollectionItemViewModel(scItem));
    }
}