package edu.sabanciuniv.dataMining.experiment.models.setcover.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.data.text.TextDocumentSummary;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCoverReview;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticToken;

public class RandomSetCoverBuilder implements SetCoverBuilder {

	protected List<TextDocumentSummary> examples;
	protected Set<LinguisticToken> remainingNouns;

	public RandomSetCoverBuilder() {
		this.examples = new ArrayList<>();
		this.remainingNouns = new HashSet<>();
	}
	
	@Override
	public void seeUniverseExample(TextDocument document) {
		if (document != null && document.getIdentifier() != null) {
			this.examples.add(document.getSummary());
			this.remainingNouns = Sets.newHashSet(Sets.union(this.remainingNouns, document.getFeatures()));
		}
	}

	public SetCover build() {
		return this.build("Random");
	}
	
	@Override
	public SetCover build(String name) {
		Random rand = new Random();
		SetCover setCover = new SetCover();
		setCover.setName(name);

		Set<LinguisticToken> remainingNouns = Sets.newHashSet(this.remainingNouns);
		List<TextDocumentSummary> examples = Lists.newArrayList(this.examples);
		
		while (remainingNouns.size() > 0) {
			int index = rand.nextInt(examples.size());
			TextDocumentSummary docSummary = examples.get(index);			
			examples.remove(index);
			SetCoverReview scReview = new SetCoverReview(setCover);
			scReview.setReviewUuid(docSummary.getIdentifier());
			scReview.setMemberCount(1);
			scReview.setSeen(false);
			setCover.getReviews().add(scReview);
			
			remainingNouns = Sets.newHashSet(Sets.difference(remainingNouns, docSummary.getFeatures()));
		}
		
		return setCover;
	}
}