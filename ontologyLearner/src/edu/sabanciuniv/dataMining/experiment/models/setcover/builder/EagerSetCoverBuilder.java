package edu.sabanciuniv.dataMining.experiment.models.setcover.builder;

import java.util.List;

import javax.persistence.EntityManager;

import com.google.common.collect.Lists;

import edu.sabanciuniv.dataMining.data.clustering.text.FeaturesCluster;
import edu.sabanciuniv.dataMining.data.clustering.text.FeaturesClusterWorld;
import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.experiment.models.OpinionDocument;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCoverItem;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticToken;

public class EagerSetCoverBuilder extends SetCoverBuilderBase {

	private FeaturesClusterWorld<LinguisticToken> clusterWorld;
	private double minDataCoverage;
	private boolean isClosed;
	
	public EagerSetCoverBuilder(EntityManager entityManager) {
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
		return this.build("Eager");
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
		
		List<SetCoverItem> items = Lists.newArrayList();
		SetCover setCover = new SetCover();
		setCover.setName(name);
		
		Iterable<FeaturesCluster<LinguisticToken>> clusters = clusterWorld.getClusters();
		for (FeaturesCluster<LinguisticToken> cluster : clusters) {
			SetCoverItem scReview = new SetCoverItem(setCover);
			scReview.setOpinionDocument(this.entityManager.find(OpinionDocument.class, cluster.getHead().getId()));
			scReview.setUtilityScore(cluster.getMemberCount());
			scReview.setSeen(false);
			scReview.setSetCover(setCover);
			items.add(scReview);
		}
		
		setCover.setItems(items);
		return setCover;
	}
}