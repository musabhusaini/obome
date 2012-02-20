package edu.sabanciuniv.dataMining.experiment.models.setcover.builder;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.sabanciuniv.dataMining.data.text.TextDocumentSummary;
import edu.sabanciuniv.dataMining.experiment.models.OpinionDocument;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCoverItem;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticToken;

public class GreedySetCoverBuilder extends RandomSetCoverBuilder {

	public GreedySetCoverBuilder(EntityManager entityManager) {
		super(entityManager);
	}
	
	public SetCover buildRandom() {
		return super.build();
	}
	
	public SetCover buildRandom(String name) {
		return super.build(name);
	}
	
	@Override
	public SetCover build() {
		return this.build("Greedy");
	}
	
	@Override
	public SetCover build(String name) {
		SetCover setCover = new SetCover();
		setCover.setName(name);

//		Map<LinguisticToken,Double> coveredNouns = new HashMap<>();
//		while (coveredNouns.keySet().size() != this.remainingNouns.size()) {
//			double bestCost = 100;
//			int bestIndex = 0;
//			
//			for (int index=0; index<this.examples.size(); index++) {
//				double cost = this.examples.get(index).getFeatures().size() /
//						(double)Sets.difference(this.examples.get(index).getFeatures(), coveredNouns.keySet()).size();
//				
//				if (cost < bestCost) {
//					bestCost = cost;
//					bestIndex = index;
//				}
//			}
//			
//			Set<LinguisticToken> newTokens = Sets.difference(this.examples.get(bestIndex).getFeatures(), coveredNouns.keySet());
//			for (LinguisticToken token : newTokens) {
//				coveredNouns.put(token, bestCost);
//			}
//			
//			TextDocumentSummary docSummary = this.examples.get(bestIndex);
//			this.examples.remove(bestIndex);
//			SetCoverReview scReview = new SetCoverReview(setCover);
//			scReview.setReviewUuid(docSummary.getIdentifier());
//			scReview.setMemberCount(1);
//			scReview.setSeen(false);
//			setCover.getReviews().add(scReview);
//		}

		Set<LinguisticToken> remainingNouns = Sets.newHashSet(this.remainingNouns);
		List<TextDocumentSummary> examples = Lists.newArrayList(this.examples);

		while (remainingNouns.size() > 0) {
			double bestIntersect = 0;
			int bestIndex = 0;
			
			for (int index=0; index < examples.size(); index++) {
				int intersect = Sets.intersection(examples.get(index).getFeatures(), remainingNouns).size();
				if (intersect > bestIntersect) {
					bestIntersect = intersect;
					bestIndex = index;
				}
			}
			
			TextDocumentSummary docSummary = examples.get(bestIndex);
			examples.remove(bestIndex);
			SetCoverItem scReview = new SetCoverItem(setCover);
			scReview.setReview(this.entityManager.find(OpinionDocument.class, docSummary.getId()));
			scReview.setUtilityScore(1);
			scReview.setSeen(false);
			setCover.getReviews().add(scReview);
			
			remainingNouns = Sets.newHashSet(Sets.difference(remainingNouns, docSummary.getFeatures()));
		}
		
		return setCover;
	}
}