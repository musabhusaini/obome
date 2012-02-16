package controllers;

import java.util.List;

import javax.persistence.EntityManager;

import com.google.common.collect.Lists;

import models.ReviewCollectionItemViewModel;
import models.ReviewCollectionViewModel;

import edu.sabanciuniv.dataMining.experiment.models.Aspect;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCoverReview;
import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;

public class ReviewCollections extends Application {

	public static void list() {
		EntityManager em = OntologyLearnerProgram.em();
		List<SetCover> setCovers = em.createQuery("SELECT sc FROM SetCover sc", SetCover.class).getResultList();
		
		List<ReviewCollectionViewModel> viewModels = Lists.newArrayList();
		for (SetCover sc : setCovers) {
			encache(sc);
			ReviewCollectionViewModel viewModel = new ReviewCollectionViewModel(sc);
			viewModels.add(viewModel);
		}
		
		renderJSON(viewModels);
	}
	
	public static void single(String collection) {
		renderJSON(new ReviewCollectionViewModel(fetch(SetCover.class, collection)));
	}
	
	public static void items(String collection) {
		SetCover sc = fetch(SetCover.class, collection);
		
		List<String> uuids = Lists.newArrayList();
		for (SetCoverReview scReview : sc.getReviews()) {
			uuids.add(scReview.getIdentifier().toString());
		}
		
		renderJSON(uuids);
	}
	
	public static void singleItem(String collection, String item) {
		renderJSON(new ReviewCollectionItemViewModel(fetch(SetCoverReview.class, item)));
	}
}