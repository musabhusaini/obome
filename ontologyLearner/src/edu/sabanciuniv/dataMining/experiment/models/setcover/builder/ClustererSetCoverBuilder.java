package edu.sabanciuniv.dataMining.experiment.models.setcover.builder;

import javax.persistence.EntityManager;

import edu.sabanciuniv.dataMining.data.clustering.text.FeaturesCluster;
import edu.sabanciuniv.dataMining.data.clustering.text.FeaturesClusterWorld;
import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.experiment.models.Review;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCoverReview;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticToken;

public class ClustererSetCoverBuilder extends SetCoverBuilderBase {

	private FeaturesClusterWorld<LinguisticToken> clusterWorld;
	private double minDataCoverage;
	private boolean isClosed;
	
	public ClustererSetCoverBuilder(EntityManager entityManager) {
		super(entityManager);
		
		this.clusterWorld = new FeaturesClusterWorld<>();
		this.minDataCoverage = 1.0;
		this.isClosed = false;
	}
	
	public double getMinDataCoverage() {
		return minDataCoverage;
	}

	public double setMinDataCoverage(double minDataCoverage) {
		return this.minDataCoverage = minDataCoverage;
	}

	public boolean isClosed() {
		return this.isClosed;
	}
	
	public boolean close() {
		if (!this.isClosed) {
			this.clusterWorld.prune();
			return this.isClosed = true;
		}
		
		return false;
	}
	
	@Override
	public void seeUniverseExample(TextDocument document) {
		if (this.isClosed) {
			throw new IllegalStateException("Cannot operate on a closed universe.");
		}
		
		this.clusterWorld.add(document.getSummary());
	}

	public SetCover build() {
		return this.build("Clustering");
	}
	
	@Override
	public SetCover build(String name) {
		if (!this.isClosed) {
			this.close();
		}
		
		FeaturesClusterWorld<LinguisticToken> clusterWorld = this.clusterWorld.clone();
		if (this.minDataCoverage < 1.0) {
			clusterWorld.prune(0, this.minDataCoverage);
		}
		
		SetCover setCover = new SetCover();
		setCover.setName(name);
		
		Iterable<FeaturesCluster<LinguisticToken>> clusters = clusterWorld.getClusters();
		for (FeaturesCluster<LinguisticToken> cluster : clusters) {
			SetCoverReview scReview = new SetCoverReview(setCover);
			scReview.setReview(this.entityManager.find(Review.class, cluster.getHead().getId()));
			scReview.setUtilityScore(cluster.getMemberCount());
			scReview.setSeen(false);
			setCover.getReviews().add(scReview);
		}
		
		return setCover;
	}
}
