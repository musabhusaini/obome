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

	public static void list() {
		EntityManager em = OntologyLearnerProgram.em();
		List<SetCover> setCovers = em.createQuery("SELECT sc FROM SetCover sc", SetCover.class).getResultList();
		
		List<OpinionCollectionViewModel> viewModels = Lists.newArrayList();
		for (SetCover sc : setCovers) {
			encache(sc);
			OpinionCollectionViewModel viewModel = new OpinionCollectionViewModel(sc);
			viewModels.add(viewModel);
		}
		
		renderJSON(viewModels);
	}
	
	public static void single(String collection) {
		renderJSON(new OpinionCollectionViewModel(fetch(SetCover.class, collection)));
	}
	
	public static void items(String collection) {
		SetCover sc = fetch(SetCover.class, collection);
		
		List<String> uuids = Lists.newArrayList();
		for (SetCoverItem scReview : sc.getReviews()) {
			uuids.add(scReview.getIdentifier().toString());
		}
		renderJSON(uuids);
	}
	
	public static void seenItems(String collection) {
		EntityManager em = OntologyLearnerProgram.em();
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
		EntityManager em = OntologyLearnerProgram.em();
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
    	EntityManager em = OntologyLearnerProgram.em();
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
    	EntityManager em = OntologyLearnerProgram.em();
    	scReview = em.merge(scReview);
    	em.flush();
    	encache(scReview);
    	
    	renderJSON(new OpinionCollectionItemViewModel(scReview));
    }
}