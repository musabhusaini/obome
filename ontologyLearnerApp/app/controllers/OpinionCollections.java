package controllers;

import java.util.List;

import javax.persistence.EntityManager;

import com.google.common.collect.Lists;

import models.OpinionCollectionItemViewModel;
import models.OpinionCollectionViewModel;

import edu.sabanciuniv.dataMining.experiment.models.Aspect;
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
		List<SetCover> setCovers = em.createQuery("SELECT sc FROM SetCover sc", SetCover.class).getResultList();
		
		List<OpinionCollectionViewModel> viewModels = Lists.newArrayList();
		for (SetCover sc : setCovers) {
			encache(sc);
			OpinionCollectionViewModel viewModel = new OpinionCollectionViewModel(sc);
			viewModel.size = getCollectionSize(sc);
			viewModels.add(viewModel);
		}
		
		renderJSON(viewModels);
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