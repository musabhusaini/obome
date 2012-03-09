package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
import models.OpinionCollectionItemViewModel;
import models.OpinionCollectionViewModel;
import models.OpinionCorpusViewModel;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import play.Logger;
import play.libs.F.Promise;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

import demoPackage.demoPage;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.experiment.models.Corpus;
import edu.sabanciuniv.dataMining.experiment.models.OpinionDocument;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCoverItem;

public class OpinionCollections extends Application {

	private static long getCollectionSize(SetCover sc) {
		// We do it this way because getting this count from the property is costly.
		return em.createQuery("SELECT COUNT(scr) FROM SetCoverItem scr WHERE scr.setCover=:sc", Long.class)
			.setParameter("sc", sc)
			.getSingleResult();
	}
	
	private static long getCorpusSize(Corpus corpus) {
		// We do it this way because getting this count from the property is costly.
		return em.createQuery("SELECT COUNT(doc) FROM OpinionDocument doc WHERE doc.corpus=:corpus", Long.class)
				.setParameter("corpus", corpus)
				.getSingleResult();
	}
	
	private static OpinionDocument insertToCorpus(String content, Corpus corpus) {
		OpinionDocument opinionDoc = new OpinionDocument();
		opinionDoc.setContent(content);
		opinionDoc.setCorpus(corpus);
		em.persist(opinionDoc);
		
		return opinionDoc;
	}
	
	public static void list() {
		List<OpinionCollectionViewModel> viewModels = Lists.newArrayList();
		
		List<Corpus> corpora = em.createQuery("SELECT c FROM Corpus c WHERE c.ownerSessionId=null OR c.ownerSessionId=:sessionId", Corpus.class)
				.setParameter("sessionId", session.getId())
				.getResultList();
		
		if (corpora != null && corpora.size() > 0) {
			List<SetCover> setCovers = em.createQuery("SELECT sc FROM SetCover sc WHERE sc.corpus IN :corpora", SetCover.class)
					.setParameter("corpora", corpora)
					.getResultList();
			for (SetCover sc : setCovers) {
				encache(sc);
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
			Random rand = new Random();
			dbCorpus = new Corpus();
			dbCorpus.setName("Corpus " + rand.nextInt(1000));
			dbCorpus.setOwnerSessionId(session.getId());
			em.persist(dbCorpus);
		} else {
			dbCorpus = em.find(Corpus.class, IdentifiableObject.getUuidBytes(UUID.fromString(corpus)));
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
			throw new IllegalArgumentException("Error with the files.");
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
		Promise<SetCover> promise = new OpinionCollectionSynthesizer(c).now();
		SetCover setCover = await(promise);
		OpinionCollectionViewModel viewModel = new OpinionCollectionViewModel(setCover);
		viewModel.size = em.createQuery("SELECT COUNT(item) FROM SetCoverItem item WHERE item.setCover=:sc", Long.class)
				.setParameter("sc", setCover)
				.getSingleResult();
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
		viewModel.size = em.createQuery("SELECT COUNT(item) FROM SetCoverItem item WHERE item.setCover=:sc", Long.class)
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
		Corpus c = fetch(Corpus.class, corpus);
		if (c == null) {
			SetCover sc = fetch(SetCover.class, corpus);
			sc.setName(name);
			em.merge(sc);
		} else {
			c.setName(name);
			List<SetCover> setCovers = c.getSetCovers();
			if (setCovers != null && setCovers.size() == 1) {
				SetCover sc = setCovers.get(0);
				sc.setName(name);
				em.merge(sc);
			}
			em.merge(c);
		}
	}
	
	public static void opinionsBrowserPage(String collection) {
		SetCover sc = fetch(SetCover.class, collection);
		OpinionCollectionViewModel viewModel = new OpinionCollectionViewModel(sc);
		viewModel.size = getCollectionSize(sc);
		collection = new Gson().toJson(viewModel, OpinionCollectionViewModel.class);
		
		Corpus corpus = sc.getCorpus();
		List<String> documents = em.createQuery("SELECT HEX(doc.id) FROM OpinionDocument doc WHERE doc.corpus=:corpus", String.class)
				.setParameter("corpus", corpus)
				.getResultList();
		
		render(collection, documents);
	}
	
	public static void opinionMiner(String collection, String document) {
		String commentText = fetch(OpinionDocument.class, document).getContent();
		byte[] uuid = IdentifiableObject.getUuidBytes(IdentifiableObject.createUuid(collection));
		String url = em.getEntityManagerFactory().getProperties().get("javax.persistence.jdbc.url").toString();
		String user = em.getEntityManagerFactory().getProperties().get("javax.persistence.jdbc.user").toString();
		String pwd = em.getEntityManagerFactory().getProperties().get("javax.persistence.jdbc.password").toString();
		String connectionString = url + "?user=" + user + "&password=" + pwd;
		
		Map<Long, Float> aspectSummaryRaw;
		try {
			// Call opinion mining engine.
			aspectSummaryRaw = demoPage.GetScoreOfAComment(commentText, connectionString, uuid);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		
		Map<String, Float> aspectSummary = Maps.newHashMap();
		for (Long key : aspectSummaryRaw.keySet()) {
			String label = (String)em.createNativeQuery("SELECT label FROM Aspects WHERE CAST(CONV(SUBSTRING(MD5(uuid), 1, 15), 16, 10) AS SIGNED INTEGER)=:key")
					.setParameter("key", key)
					.getSingleResult();
			
			aspectSummary.put(label, aspectSummaryRaw.get(key));
		}
		
		renderJSON(aspectSummary);
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
		List<SetCoverItem> items = em.createQuery("SELECT scr FROM SetCoverItem scr WHERE scr.setCover=:sc AND scr.seen=true", SetCoverItem.class)
				.setParameter("sc", fetch(SetCover.class, collection))
				.getResultList();
		
		List<String> uuids = Lists.newArrayList();
		for (SetCoverItem scReview : items) {
			uuids.add(scReview.getIdentifier().toString());
		}
		renderJSON(uuids);
	}

	public static void unseenItems(String collection) {
		List<SetCoverItem> items = em.createQuery("SELECT scr FROM SetCoverItem scr WHERE scr.setCover=:sc AND scr.seen=false", SetCoverItem.class)
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
	    	scReview = em.createQuery("SELECT scr FROM SetCoverItem scr WHERE scr.setCover=:sc AND scr.seen=false " +
	    			"ORDER BY scr.utilityScore DESC", SetCoverItem.class)
	    			.setParameter("sc", fetch(SetCover.class, collection))
	    			.setMaxResults(1)
	    			.getSingleResult();
    	} catch (NoResultException e) {
    		scReview = em.createQuery("SELECT scr FROM SetCoverItem scr WHERE scr.setCover=:sc ORDER BY scr.utilityScore ASC", SetCoverItem.class)
    				.setParameter("sc", fetch(SetCover.class, collection))
    				.setMaxResults(1)
    				.getSingleResult();
    	}
    	
    	encache(scReview);
    	
    	renderJSON(new OpinionCollectionItemViewModel(scReview));
    }
    
    public static void seeItem(String collection, String item) {
    	SetCoverItem scReview = fetch(SetCoverItem.class, item);
    	scReview.setSeen(true);
    	
    	Logger.info(constructGenericLogMessage("seeing item " + item));
    	scReview = em.merge(scReview);
    	em.flush();
    	encache(scReview);
    	
    	renderJSON(new OpinionCollectionItemViewModel(scReview));
    }
}