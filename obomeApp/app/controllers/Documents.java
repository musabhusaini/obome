package controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import play.Logger;

import models.DisplayTextModel;
import models.DisplayTextModel.DisplayTextType;
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
		    	Collections.sort(features, new Comparator<LinguisticToken>() {
					@Override
					public int compare(LinguisticToken arg0, LinguisticToken arg1) {
						return arg0.getAbsoluteEndPosition() - arg1.getAbsoluteBeginPosition();
					}
		    	});
		    	
		    	DisplayTextModel rootDisplayFeature = new DisplayTextModel();
		    	rootDisplayFeature.types.add(DisplayTextType.ROOT);
		    	
		    	DisplayTextModel displayFeature;
		    	int lastIndex = 0;
		    	
		    	for (LinguisticToken feature : features) {
		    		displayFeature = new DisplayTextModel();
		    		displayFeature.content = text.substring(lastIndex, feature.getAbsoluteBeginPosition());
		    		displayFeature.types.add(DisplayTextType.IRRELEVANT);
		    		rootDisplayFeature.children.add(displayFeature);
		    		
		    		String substring = text.substring(feature.getAbsoluteBeginPosition(), feature.getAbsoluteEndPosition());
		    		if (!substring.equals(feature.getText())) {
		    			Logger.error("While parsing features %s != %s", feature.getText(), substring);
		    			continue;
		    		}
		    		
		    		displayFeature = new DisplayTextModel();
		    		displayFeature.content = feature.getText();
		    		displayFeature.otherInfo.put("lemma", feature.getLemma());
		    		
		    		BacklogToken backlogToken = Iterables.getFirst(em().createQuery("SELECT bt FROM BacklogToken bt " +
		    				"WHERE bt.setCover=:sc AND bt.label=:label", BacklogToken.class)
		    			.setParameter("sc", this.sc)
		    			.setParameter("label", feature.getLemma())
		    			.setMaxResults(1)
		    			.getResultList(), null);
		    		
		    		Keyword keyword = null;
		    		
		    		if (backlogToken != null) {
		    			displayFeature.types.add(DisplayTextType.SEEN);
		    			keyword = backlogToken.getKeyword();
		    		} else {
		    			displayFeature.types.add(DisplayTextType.UNSEEN);
		    			
		    			keyword = Iterables.getFirst(em().createQuery("SELECT kw FROM Keyword kw " +
								"WHERE kw.aspect.setCover=:sc AND kw.label=:label", Keyword.class)
							.setParameter("sc", sc)
							.setParameter("label", feature.getLemma())
							.setMaxResults(1)
							.getResultList(), null);
		    		}
		    		
		    		if (keyword != null) {
		    			displayFeature.otherInfo.put("aspect", keyword.getAspect().getLabel());
		    		}
		    		
		    		if (keyword != null || backlogToken == null) {
		    			displayFeature.types.add(DisplayTextType.KEYWORD);
		    		} else {
		    			displayFeature.types.add(DisplayTextType.STANDARD);
		    		}
		    		
		    		rootDisplayFeature.children.add(displayFeature);
		    		
		    		lastIndex = feature.getAbsoluteEndPosition();
		    	}
		    	
		    	displayFeature = new DisplayTextModel();
		    	displayFeature.content = text.substring(lastIndex);
		    	displayFeature.types.add(DisplayTextType.SEPARATOR);
		    	if (StringUtils.isNotBlank(displayFeature.content)) {
		    		rootDisplayFeature.children.add(displayFeature);
		    	}
		    	
		    	return new DocumentViewModel(textDoc.getIdentifier(), rootDisplayFeature);
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