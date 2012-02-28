package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.persistence.EntityManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import play.data.Upload;

import com.google.common.collect.Lists;

import models.OpinionCollectionItemViewModel;
import models.OpinionCollectionViewModel;
import models.OpinionCorpusViewModel;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.experiment.models.Aspect;
import edu.sabanciuniv.dataMining.experiment.models.Corpus;
import edu.sabanciuniv.dataMining.experiment.models.OpinionDocument;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCoverItem;
import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;

public class OpinionCollections extends Application {

	private static long getCollectionSize(SetCover sc) {
		// We do it this way because getting this count from the property is costly.
		return em.createQuery("SELECT COUNT(scr) FROM SetCoverItem scr WHERE scr.setCover=:sc", Long.class)
			.setParameter("sc", sc)
			.getSingleResult();
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
			dbCorpus = new Corpus();
			dbCorpus.setName(dbCorpus.getIdentifier().toString());
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
			List<InputStream> streams = Lists.newArrayList();
			if (file.getName().endsWith(".zip")) {
				ZipFile zip = new ZipFile(file);
				
				for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
				    ZipEntry ze = e.nextElement();
				    String name = ze.getName();
				    if (name.endsWith(".xml")) {
				    	streams.add(zip.getInputStream(ze));
				    }
				}
			} else if (file.getName().endsWith(".xml")) {
				streams.add(new FileInputStream(file));
			}

			for (InputStream stream : streams) {
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
						OpinionDocument opinionDoc = new OpinionDocument();
						opinionDoc.setContent(content);
						opinionDoc.setCorpus(dbCorpus);
						em.persist(opinionDoc);
					
						System.out.println("Saved document with text: " + content);
					}
				}
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Error with the files.");
		}
		
		OpinionCorpusViewModel viewModel = new OpinionCorpusViewModel(dbCorpus);
		viewModel.size = em.createQuery("SELECT COUNT(d) FROM OpinionDocument d WHERE d.corpus=:corpus", Long.class)
				.setParameter("corpus", dbCorpus)
				.getSingleResult();
		renderJSON(viewModel);
	}
	
	public static void single(String collection) {
		SetCover sc = fetch(SetCover.class, collection);
		OpinionCollectionViewModel viewModel = new OpinionCollectionViewModel(sc);
		viewModel.size = getCollectionSize(sc);
		
		renderJSON(viewModel);
	}

	public static void browserPage(String collection, boolean bypassCache, String featureType) {
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
    	SetCoverItem scReview = em.createQuery("SELECT scr FROM SetCoverItem scr WHERE scr.setCover=:sc AND scr.seen=false " +
    			"ORDER BY scr.utilityScore DESC", SetCoverItem.class)
    			.setParameter("sc", fetch(SetCover.class, collection))
    			.setMaxResults(1)
    			.getSingleResult();
    	encache(scReview);
    	
    	renderJSON(new OpinionCollectionItemViewModel(scReview));
    }
    
    public static void seeItem(String collection, String item) {
    	SetCoverItem scReview = fetch(SetCoverItem.class, item);
    	scReview.setSeen(true);
    	
    	System.out.println("Seeing item " + item);
    	scReview = em.merge(scReview);
    	em.flush();
    	encache(scReview);
    	
    	renderJSON(new OpinionCollectionItemViewModel(scReview));
    }
}