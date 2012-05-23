package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.persistence.NoResultException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import jobs.OpinionCollectionDistiller;
import jobs.OpinionCollectionDistillerAnalyzer;
import jobs.OpinionCollectionSynthesizer;
import models.DisplayTextModel;
import models.DisplayTextModel.DisplayTextType;
import models.DocumentViewModel;
import models.OpinionCollectionItemViewModel;
import models.OpinionCollectionViewModel;
import models.OpinionCorpusViewModel;
import models.OpinionMinerResultViewModel;
import models.ViewModel;
import nlp_engine.ModifierItem;
import nlp_engine.SentenceObject;
import nlp_engine.Token;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import play.Logger;
import play.cache.Cache;
import play.libs.F.Promise;
import play.mvc.results.NotFound;
import web_package.CommentResult;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.experiment.models.Corpus;
import edu.sabanciuniv.dataMining.experiment.models.OpinionDocument;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCoverItem;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticSentence;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticText;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticToken;

public class OpinionCollections extends Application {

	private static class TokenBeginEquals implements Predicate<Token> {
		private Token token;
		
		public TokenBeginEquals(Token token) {
			this.token = token;
		}

		@Override
		public boolean apply(Token other) {
			return other.GetRelativeBeginPosition() == this.token.GetRelativeBeginPosition();
		}
	}
	
	public static class TokenBeginComparer implements Comparator<Token> {
		@Override
		public int compare(Token token1, Token token2) {
			return token1.GetRelativeBeginPosition() - token2.GetRelativeBeginPosition();
		}
	}
	
	private static long getCollectionSize(SetCover sc) {
		// We do it this way because getting this count from the property is costly.
		return em().createQuery("SELECT COUNT(scr) FROM SetCoverItem scr WHERE scr.setCover=:sc", Long.class)
			.setParameter("sc", sc)
			.getSingleResult();
	}
	
	private static long getCorpusSize(Corpus corpus) {
		// We do it this way because getting this count from the property is costly.
		return em().createQuery("SELECT COUNT(doc) FROM OpinionDocument doc WHERE doc.corpus=:corpus", Long.class)
				.setParameter("corpus", corpus)
				.getSingleResult();
	}
	
	private static OpinionDocument insertToCorpus(String content, Corpus corpus) {
		OpinionDocument opinionDoc = new OpinionDocument();
		opinionDoc.setContent(content);
		opinionDoc.setCorpus(corpus);
		em().persist(opinionDoc);
		
		return opinionDoc;
	}
	
	public static void list() {
		List<ViewModel> viewModels = Lists.newArrayList();
		
		List<Corpus> corpora = em().createQuery("SELECT c FROM Corpus c WHERE c.ownerSessionId=null OR " +
				"c.ownerSessionId=:sessionId", Corpus.class)
				.setParameter("sessionId", session.getId())
				.getResultList();
		
		if (corpora != null) {
			for (Corpus corpus : corpora) {
				OpinionCorpusViewModel viewModel = new OpinionCorpusViewModel(corpus);
				viewModel.size = getCorpusSize(corpus);
				viewModels.add(viewModel);
			}
		}
		
		List<SetCover> setCovers = em().createQuery("SELECT sc FROM SetCover sc WHERE sc.ownerSessionId=null OR " +
				"sc.ownerSessionId=:sessionId", SetCover.class)
				.setParameter("sessionId", session.getId())
				.getResultList();
		
		if (setCovers != null) {
			for (SetCover sc : setCovers) {
				OpinionCollectionViewModel viewModel = new OpinionCollectionViewModel(sc);
				viewModel.size = getCollectionSize(sc);
				viewModels.add(viewModel);
			}
		}
		
		renderJSON(viewModels);
	}

	public static void upload(String corpus, File file) {
		Corpus dbCorpus;
		
		// If it's a new one, we create it, otherwise, we get from DB.
		if ("new".equals(corpus)) {
			dbCorpus = new Corpus();
			dbCorpus.setName("Corpus " + new Random().nextInt(1000));
			dbCorpus.setOwnerSessionId(session.getId());
			em().persist(dbCorpus);
		} else {
			dbCorpus = em().find(Corpus.class, IdentifiableObject.getUuidBytes(UUID.fromString(corpus)));
			if (dbCorpus == null || !dbCorpus.getOwnerSessionId().equals(session.getId())) {
				throw new IllegalArgumentException("Invalid corpus.");
			}
		}
				
		try {
			// Put all documents in the corpus.
			Map<String,InputStream> streamMap = Maps.newHashMap();
			if (file.getName().endsWith(".zip")) {
				ZipFile zip = new ZipFile(file);
				
				for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
				    ZipEntry ze = e.nextElement();
				    
				    String name = ze.getName();
				    if (streamMap.containsKey(name)) {
				    	name = new Random().nextInt(1000) + name;
				    }
				    
				    streamMap.put(name, zip.getInputStream(ze));
				}
			} else {
				streamMap.put(file.getName(), new FileInputStream(file));
			}

			for (String filename : streamMap.keySet()) {
				Logger.info(constructGenericLogMessage("saving file " + filename));
				
				InputStream stream = streamMap.get(filename);
				
				if (filename.endsWith(".xml")) {
					DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
					Document doc = docBuilder.parse(stream);
					doc.getDocumentElement().normalize();
					
					NodeList nodes = doc.getDocumentElement().getChildNodes();
					for (int index=0; index<nodes.getLength(); index++) {
						Node node = nodes.item(index);
						Node child = node.getFirstChild();
						String content = (child != null ? child : node).getTextContent().trim();
						
						if (!StringUtils.isEmpty(content)) {
							content = StringEscapeUtils.unescapeXml(content);
							insertToCorpus(content, dbCorpus);
						}
					}
				} else if (filename.endsWith(".txt") || filename.endsWith(".csv")) {
					Scanner scanner = new Scanner(stream);
					while (scanner.hasNext()) {
						String content = scanner.nextLine();
						insertToCorpus(content, dbCorpus);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Error with the files.", e);
		}
		
		OpinionCorpusViewModel viewModel = new OpinionCorpusViewModel(dbCorpus);
		viewModel.size = getCorpusSize(dbCorpus);
		renderJSON(viewModel);
	}
	
	public static void synthesizerPage(String corpus) {
		Corpus c = fetch(Corpus.class, corpus);
		OpinionCorpusViewModel viewModel = new OpinionCorpusViewModel(c);
		viewModel.size = getCorpusSize(c);
		corpus = new Gson().toJson(viewModel, OpinionCorpusViewModel.class);
		render(corpus);
	}
	
	public static void synthesize(String corpus) {
		Corpus c = fetch(Corpus.class, corpus);
		Promise<SetCover> promise = new OpinionCollectionSynthesizer(c, session).now();
		SetCover setCover = await(promise);
		OpinionCollectionViewModel viewModel = new OpinionCollectionViewModel(setCover);
		viewModel.size = getCollectionSize(setCover);
		renderJSON(viewModel);
	}
	
	public static void synthesizerProgress(String corpus) {
		OpinionCollectionSynthesizer process = OpinionCollectionSynthesizer.get(corpus);
		double progress = process == null ? 1.0 : process.getProgress();
		renderJSON(progress);
	}
	
	public static void distillerStats(String collection) {
		SetCover sc = fetch(SetCover.class, collection);
		Promise<Map<Double, Double>> promise = new OpinionCollectionDistillerAnalyzer(sc).now();
		Map<Double, Double> map = await(promise);
		List<Double[]> points = Lists.newArrayList();
		for (Entry<Double, Double> entry : map.entrySet()) {
			points.add(new Double[]{ entry.getKey(), entry.getValue() });
		}
		renderJSON(points);
	}
	
	public static void distill(String collection, double threshold) {
		threshold /= 100;
		
		SetCover sc = fetch(SetCover.class, collection);
		Promise<SetCover> promise = new OpinionCollectionDistiller(sc, threshold).now();
		sc = await(promise);
		OpinionCollectionViewModel viewModel = new OpinionCollectionViewModel(sc);
		viewModel.size = em().createQuery("SELECT COUNT(item) FROM SetCoverItem item WHERE item.setCover=:sc", Long.class)
				.setParameter("sc", sc)
				.getSingleResult();
		renderJSON(viewModel);
	}
	
	public static void distillerProgress(String collection) {
		OpinionCollectionDistillerAnalyzer process = OpinionCollectionDistillerAnalyzer.get(collection);
		double progress = process == null ? 1.0 : process.getProgress();
		renderJSON(progress);
	}
	
	public static void rename(String corpus, String name) {
		try {
			Corpus c = fetch(Corpus.class, corpus);
			
			if (!session.getId().equals(c.getOwnerSessionId())) {
				renderJSON(false);
			}
			
			c.setName(name);
			List<SetCover> setCovers = c.getSetCovers();
			if (setCovers != null && setCovers.size() == 1) {
				SetCover sc = setCovers.get(0);
				sc.setName(name);
				em().merge(sc);
			}
			em().merge(c);
		} catch(NotFound e) {
			SetCover sc = fetch(SetCover.class, corpus);
			
			if (sc == null || !session.getId().equals(sc.getOwnerSessionId())) {
				renderJSON(false);
			}
			
			sc.setName(name);
			em().merge(sc);			
		}
		
		renderJSON(true);
	}
	
	public static void opinionsBrowserPage(String collection) {
		SetCover sc = fetch(SetCover.class, collection);
		
		// Clear cache for this collection.
		Cache.delete(collection);
		
		OpinionCollectionViewModel viewModel = new OpinionCollectionViewModel(sc);
		viewModel.size = getCollectionSize(sc);
		collection = new Gson().toJson(viewModel, OpinionCollectionViewModel.class);
		
		Corpus corpus = sc.getCorpus();
		List<String> documents = em().createQuery("SELECT HEX(doc.id) FROM OpinionDocument doc WHERE doc.corpus=:corpus " +
				"ORDER BY doc.content ASC", String.class)
				.setParameter("corpus", corpus)
				.getResultList();
		
		render(collection, documents);
	}
	
	public static void opinionMiner(String collection, String document) {
		Map<String, OpinionMinerResultViewModel> results = (Map<String, OpinionMinerResultViewModel>) Cache.get(collection);
		
		if (results == null) {
			results = Maps.newHashMap();
			Cache.set(collection, results, "1h");
		}
		
		OpinionMinerResultViewModel result = results.get(document);
		if (result == null) {
			result = new OpinionMinerResultViewModel();
			OpinionDocument opinDoc = fetch(OpinionDocument.class, document);
			
			// Set up connection string.
			byte[] uuid = IdentifiableObject.getUuidBytes(IdentifiableObject.createUuid(collection));
			String url = em().getEntityManagerFactory().getProperties().get("javax.persistence.jdbc.url").toString();
			String user = em().getEntityManagerFactory().getProperties().get("javax.persistence.jdbc.user").toString();
			String pwd = em().getEntityManagerFactory().getProperties().get("javax.persistence.jdbc.password").toString();
			String connectionString = url + "?user=" + user + "&password=" + pwd;
			
			// Get opinion mining result from the engine.
			CommentResult commentResult;
			try {
				// Call the opinion mining engine.
				commentResult = new CommentResult(opinDoc.getContent(), uuid, connectionString);	
			} catch (Throwable e) {
				Logger.error("Could not process review: %s", opinDoc.getContent());
				throw new IllegalStateException(e);
			}
			
			// Make sense of the score map.
			Map<Long, Float> rawScorecard = commentResult.GetScoreMap();
			Map<Long, String> aspectLongIdMap = Maps.newHashMap();
			result.scorecard = Maps.newHashMap();
			for (Long key : rawScorecard.keySet()) {
				String label;
				float score;
				
				if (key >= 0) {
					label = (String)em().createNativeQuery("SELECT label " +
							"FROM Aspects WHERE CAST(CONV(SUBSTRING(MD5(uuid), 1, 15), 16, 10) AS SIGNED INTEGER)=:key")
						.setParameter("key", key)
						.getSingleResult();
				} else {
					label = "Overall";
				}
				
				aspectLongIdMap.put(key, label);
				
				score = rawScorecard.get(key);
				if (Float.isNaN(score)) {
					score = 0;
				}
				
				result.scorecard.put(label, score);
			}
			
			DisplayTextModel rootDisplayText = new DisplayTextModel();
			rootDisplayText.types.add(DisplayTextType.ROOT);
			
			int lastSentenceEnd = 0;
			
			List<SentenceObject> sentences = commentResult.GetSentences();
			for (SentenceObject sentence : sentences) {
				// Add preceding text (separator, etc).
				DisplayTextModel sentenceDisplayText = new DisplayTextModel();
				sentenceDisplayText.content = opinDoc.getContent().substring(lastSentenceEnd, sentence.GetBeginPosition());
				sentenceDisplayText.types.add(DisplayTextType.SEPARATOR);
				if (StringUtils.isNotEmpty(sentenceDisplayText.content)) {
					rootDisplayText.children.add(sentenceDisplayText);
				}
				
				String sentenceText = sentence.GetText();
				sentenceDisplayText = new DisplayTextModel();
				sentenceDisplayText.types.add(DisplayTextType.SENTENCE);
				
				// Get all sentence tokens.
				List<Token> tokens = sentence.GetTokenList();
				Collections.sort(tokens, new TokenBeginComparer());
				
				// Get modifier tokens.
				List<ModifierItem> modifierItems = sentence.GetModifierList();
				List<Token> modifierTokens = Lists.newArrayList();
				for (ModifierItem item : modifierItems) {
					Token token = Iterables.find(modifierTokens, new TokenBeginEquals(item.GetModifiedToken()), null);
					if (token == null) {
						modifierTokens.add(item.GetModifiedToken());
					}
					
					if (item.HasModifier()) {
						token = Iterables.find(modifierTokens, new TokenBeginEquals(item.GetModifierToken()), null);
						if (token == null) {
							modifierTokens.add(item.GetModifierToken());
						}
					}
				}
								
				int lastTokenIndex = 0;
				DisplayTextModel displayText;
				
				for (Token token : tokens) {
					// Add preceding text, if any (separators, etc).
					displayText = new DisplayTextModel();
					displayText.content = sentenceText.substring(lastTokenIndex, token.GetRelativeBeginPosition());
					displayText.types.add(DisplayTextType.SEPARATOR);
					if (StringUtils.isNotEmpty(displayText.content)) {
						sentenceDisplayText.children.add(displayText);
					}
					
					// Process this token.
					displayText = new DisplayTextModel();
					displayText.content = sentenceText.substring(token.GetRelativeBeginPosition(), token.GetRelativeEndPosition());
					
					// Determine token type.
					if (token.IsAKeyword()) {
						displayText.types.add(DisplayTextType.KEYWORD);
						displayText.otherInfo.put("aspect", aspectLongIdMap.get(token.GetAspectId()));
					} else {
						Token tmp = Iterables.find(modifierTokens, new TokenBeginEquals(token), null);
						if (tmp != null) {
							displayText.types.add(DisplayTextType.MODIFIER);
						}
						
						Float score = token.GetScore();
						if (score != 0) {
							displayText.types.add(DisplayTextType.POLAR);
							displayText.otherInfo.put("polarity", token.GetScore());
						} else {
							displayText.types.add(DisplayTextType.STANDARD);
						}
					}
					
					sentenceDisplayText.children.add(displayText);
					
					lastTokenIndex = token.GetRelativeEndPosition();
				}
				
				displayText = new DisplayTextModel();
				displayText.content = sentenceText.substring(lastTokenIndex);
				displayText.types.add(DisplayTextType.SEPARATOR);
				
				if (StringUtils.isNotEmpty(displayText.content)) {
					sentenceDisplayText.children.add(displayText);
				}
				
				if (sentence.GetScoreMap().size() <= 1) {
					sentenceDisplayText.types.add(DisplayTextType.IRRELEVANT);
				}
				
				if (sentence.GetScoreMap().containsKey(-1L)) {
					displayText = new DisplayTextModel();
					displayText.types.add(DisplayTextType.SENTENCE_POLARITY);
					displayText.otherInfo.put("polarity", sentence.GetScoreMap().get(-1L));
					sentenceDisplayText.children.add(displayText);
				}
				
				rootDisplayText.children.add(sentenceDisplayText);
				lastSentenceEnd = sentence.GetEndPosition();
			}
			
			result.document = new DocumentViewModel(opinDoc.getIdentifier(), rootDisplayText);
			
			results.put(document, result);
		}
		
		renderJSON(result);
	}
	
	public static void single(String collection) {
		SetCover sc = fetch(SetCover.class, collection);
		OpinionCollectionViewModel viewModel = new OpinionCollectionViewModel(sc);
		viewModel.size = getCollectionSize(sc);
		
		renderJSON(viewModel);
	}

	public static void aspectsBrowserPage(String collection, boolean bypassCache, String featureType) {
		SetCover sc = fetch(SetCover.class, collection);
		OpinionCollectionViewModel viewModel = new OpinionCollectionViewModel(sc);
		viewModel.size = getCollectionSize(sc);
		collection = new Gson().toJson(viewModel, OpinionCollectionViewModel.class);
		
		render(collection, bypassCache, featureType);
	}
	
	public static void items(String collection) {
		SetCover sc = fetch(SetCover.class, collection);
		
		List<String> uuids = Lists.newArrayList();
		for (SetCoverItem scReview : sc.getItems()) {
			uuids.add(scReview.getIdentifier().toString());
		}
		renderJSON(uuids);
	}
	
	public static void seenItems(String collection) {
		List<SetCoverItem> items = em().createQuery("SELECT scr FROM SetCoverItem scr WHERE scr.setCover=:sc AND scr.seen=true", SetCoverItem.class)
				.setParameter("sc", fetch(SetCover.class, collection))
				.getResultList();
		
		List<String> uuids = Lists.newArrayList();
		for (SetCoverItem scReview : items) {
			uuids.add(scReview.getIdentifier().toString());
		}
		renderJSON(uuids);
	}

	public static void unseenItems(String collection) {
		List<SetCoverItem> items = em().createQuery("SELECT scr FROM SetCoverItem scr WHERE scr.setCover=:sc AND scr.seen=false", SetCoverItem.class)
				.setParameter("sc", fetch(SetCover.class, collection))
				.getResultList();
		
		List<String> uuids = Lists.newArrayList();
		for (SetCoverItem scReview : items) {
			uuids.add(scReview.getIdentifier().toString());
		}
		renderJSON(uuids);
	}
	
	public static void singleItem(String collection, String item) {
		renderJSON(new OpinionCollectionItemViewModel(fetch(SetCoverItem.class, item)));
	}
		
    public static void nextBestItem(String collection) {
    	SetCoverItem scReview;
    	
    	try {
	    	scReview = em().createQuery("SELECT scr FROM SetCoverItem scr WHERE scr.setCover=:sc AND scr.seen=false " +
	    			"ORDER BY scr.utilityScore DESC", SetCoverItem.class)
	    			.setParameter("sc", fetch(SetCover.class, collection))
	    			.setMaxResults(1)
	    			.getSingleResult();
    	} catch (NoResultException e) {
    		scReview = em().createQuery("SELECT scr FROM SetCoverItem scr WHERE scr.setCover=:sc ORDER BY scr.utilityScore ASC", SetCoverItem.class)
    				.setParameter("sc", fetch(SetCover.class, collection))
    				.setMaxResults(1)
    				.getSingleResult();
    	}
    	
    	encache(scReview);
    	
    	// TODO: make changes here to really send the next best one.
    	
    	renderJSON(new OpinionCollectionItemViewModel(scReview));
    }
    
    public static void seeItem(String collection, String item) {
    	SetCoverItem scReview = fetch(SetCoverItem.class, item);
    	scReview.setSeen(true);
    	
    	Logger.info(constructGenericLogMessage("seeing item " + item));
    	scReview = em().merge(scReview);
    	em().flush();
    	encache(scReview);
    	
    	redirect("Documents.seeDocument", item);
    }
}