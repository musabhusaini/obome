package edu.sabanciuniv.dataMining.data.clustering.text;

import java.security.InvalidParameterException;

import com.google.common.collect.Iterables;

import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.data.factory.ObjectFactory;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticToken;

/**
 * A clusterer for {@link TextDocument} objects with some extra bell and whistles.
 * @author Mus'ab Husaini
 */
public class TextDocumentFeaturesClusterer {
	private ObjectFactory<TextDocument> factory;
	private double rejectTolerance;
	private double minDataCoverage;
	private FeaturesClusterWorld<LinguisticToken> clusterWorld;
	
	/**
	 * Creates a new instance of {@link TextDocumentFeaturesClusterer}.
	 * @param factory The {@link ObjectFactory} object that will generate the {@link TextDocument} instances to be clustered.
	 */
	public TextDocumentFeaturesClusterer(ObjectFactory<TextDocument> factory) {
		if (factory == null) {
			throw new InvalidParameterException("Must provide an object factory.");
		}
		
		this.factory = factory;
		this.clusterWorld = new FeaturesClusterWorld<LinguisticToken>();
	}

	/**
	 * Clusters all the documents in the factory.
	 * @return A {@link FeaturesClusterWorld} that was generated by this clusterer.
	 */
	public FeaturesClusterWorld<LinguisticToken> cluster() {
		return this.cluster(0);
	}
	
	/**
	 * Clusters all the documents in the factory.
	 * @param maxWorldSize Prune every time the world reaches this size.
	 * @return A {@link FeaturesClusterWorld} that was generated by this clusterer.
	 */
	public FeaturesClusterWorld<LinguisticToken> cluster(int maxWorldSize) {
		if (!this.factory.isPristine()) {
			this.factory.reset();
		}
		
		TextDocument doc;
		while ((doc = this.factory.create()) != null) {
			this.clusterWorld.add(doc.getSummary());
			
			if (maxWorldSize > 0 && Iterables.size(this.clusterWorld.getClusters()) > maxWorldSize) {
				this.clusterWorld.prune(1, this.minDataCoverage);
			}
		}
		
		this.factory.close();
		return this.clusterWorld;
	}

	/**
	 * Prunes this cluster world.
	 * @return A pruned cluster world.
	 */
	public FeaturesClusterWorld<LinguisticToken> prune() {
		if (this.clusterWorld.prune(0, this.minDataCoverage)) {
			return this.clusterWorld;
		} else {
			return null;
		}
	}
	
	/**
	 * Gets the underlying cluster world.
	 * @return The underlying cluster world.
	 */
	public FeaturesClusterWorld<LinguisticToken> getClusterWorld() {
		return this.clusterWorld;
	}

	/**
	 * Gets the factory object used for instance creation.
	 * @return The factory object.
	 */
	public ObjectFactory<TextDocument> getFactory() {
		return this.factory;
	}

	/**
	 * Gets the rejection tolerance of the clusters.
	 * @return The reject tolerance of the clusters.
	 */
	public double getRejectTolerance() {
		return this.rejectTolerance;
	}

	/**
	 * Sets the rejection tolerance of the clusters [0, 1].
	 * @param rejectTolerance The reject tolerance to set [0, 1].
	 */
	public void setRejectTolerance(double rejectTolerance) {
		this.rejectTolerance = rejectTolerance;
	}

	/**
	 * Gets the minimum data coverage for this cluster world.
	 * @return The minimum data coverage for this cluster world.
	 */
	public double getMinDataCoverage() {
		return this.minDataCoverage;
	}

	/**
	 * Sets the minimum data coverage for this cluster world [0, 1].
	 * @param minDataCoverage The minimum data coverage to set [0, 1].
	 */
	public void setMinDataCoverage(double minDataCoverage) {
		this.minDataCoverage = minDataCoverage;
	}
	
	@Override
	public String toString() {
		return this.clusterWorld.toString();
	}
}