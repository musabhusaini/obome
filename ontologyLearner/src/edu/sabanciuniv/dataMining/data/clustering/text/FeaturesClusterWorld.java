package edu.sabanciuniv.dataMining.data.clustering.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Iterables;

import edu.stanford.nlp.ling.HasWord;

import edu.sabanciuniv.dataMining.data.text.IdentifiableWithFeatures;

/**
 * A collection (world) of features clusters.
 * @author Mus'ab Husaini
 * @param <T> Type of features; must extend {@link HasWord}.
 */
public class FeaturesClusterWorld<T extends HasWord> {
	private List<FeaturesCluster<T>> clusters;
	private double rejectTolerance;

	/**
	 * Creates an empty instance of {@link FeaturesClusterWorld}
	 */
	public FeaturesClusterWorld() {
		this(0);
	}

	/**
	 * Creates an instance of {@link FeaturesClusterWorld} with a given amount of tolerance to rejection.
	 * @param rejectTolerance Rejection tolerance used for adding feature sets to clusters.
	 */
	public FeaturesClusterWorld(double rejectTolerance) {
		this.clusters = new ArrayList<FeaturesCluster<T>>();
		this.rejectTolerance = rejectTolerance;
	}

	/**
	 * Gets all the clusters in this world.
	 * @return All the clusters in this world.
	 */
	public Iterable<FeaturesCluster<T>> getClusters() {
		return Iterables.unmodifiableIterable(this.clusters);
	}

	/**
	 * Gets the rejection tolerance used for adding feature sets to clusters.
	 * @return Rejection tolerance.
	 */
	public double getRejectTolerance() {
		return this.rejectTolerance;
	}

	/**
	 * Sets the rejection tolerance used for adding feature sets to clusters.
	 * @param rejectTolerance
	 */
	public void setRejectTolerance(double rejectTolerance) {
		this.rejectTolerance = rejectTolerance;
	}

	/**
	 * Adds a feature set to this cluster world.
	 * @param features Feature set to add.
	 * @return A flag indicating whether the operation was successful or not.
	 */
	public boolean add(IdentifiableWithFeatures<T> features) {
		if (features == null || features.getFeatures().size() == 0) {
			return false;
		}
		
		// If this is the first cluster, not much work is needed.
		if (this.clusters.size() == 0) {
			this.clusters.add(new FeaturesCluster<T>(features));
			return true;
		}
		
		// Go through all the clusters and try to fit this in.
		FeaturesCluster<T> newCluster = new FeaturesCluster<T>(features);
		for (FeaturesCluster<T> cluster : this.clusters) {
			newCluster = cluster.add(newCluster.getHead(), this.rejectTolerance);
			if (newCluster == null) {
				break;
			}
		}
		
		// If there were still unclustered features.
		if (newCluster != null) {
			this.clusters.add(newCluster);
		}
		
		return true;
	}

	private boolean reduceData(double minDataCoverage) {
		// Make sure the coverage value is within range.
		if (minDataCoverage >= 1.0 || minDataCoverage <= 0) {
			return false;
		}
		
		// Calculate the total size of data.
		double totalDataSize = 0;
		for (FeaturesCluster<T> cluster : this.clusters) {
			totalDataSize += cluster.getDataMass(); 
		}
		
		// Keep including clusters until minimum coverage is reached.
		Collections.sort(this.clusters, Collections.reverseOrder());
		List<FeaturesCluster<T>> newClusters = new ArrayList<FeaturesCluster<T>>();
		double coverageSize = 0;
		for (FeaturesCluster<T> cluster : this.clusters) {
			 if (coverageSize >= minDataCoverage * totalDataSize) {
				 break;
			 }
			 
			 cluster.stripMemberFeatures();
			 newClusters.add(cluster);
			 coverageSize += cluster.getDataMass();
		}
		
		this.clusters = newClusters;
		return true;
	}

	/**
	 * Post-prune the clusters in this world.
	 * @return A flag indicating whether this operation was successful or not.
	 */
	public boolean prune() {
		return this.prune(0);
	}
	
	/**
	 * Post-prune the clusters in this world.
	 * @param times Number of iterations to prune (use -1 for full-prune).
	 * @return A flag indicating whether this operation was successful or not.
	 */
	public boolean prune(int times) {
		return this.prune(times, 1.0);
	}

	/**
	 * Post-prune the clusters in this world.
	 * @param times Number of iterations to prune (use -1 for full-prune). 
	 * @param minDataCoverage Minimum data coverage [0, 1] desired. The algorithm will attempt to discard all data beyond this threshold.
	 * @return A flag indicating whether this operation was successful or not.
	 */
	public boolean prune(int times, double minDataCoverage) {
		int originalSize = this.clusters.size();
		
		// Make a copy of clusters and sort.
		List<FeaturesCluster<T>> newClusters = new ArrayList<FeaturesCluster<T>>();
		for (FeaturesCluster<T> cluster : this.clusters) {
			newClusters.add(cluster.clone());
		}
		Collections.sort(newClusters, Collections.reverseOrder());
		
		// Start from the smallest cluster and go to the highest.
		for (int small=newClusters.size()-1; small>=0; small--) {
			FeaturesCluster<T> smallCluster = newClusters.get(small);
			
			for (int big=0; big<newClusters.size() && small!=big; big++) {
				FeaturesCluster<T> bigCluster = newClusters.get(big);
				smallCluster = bigCluster.add(smallCluster, this.rejectTolerance);
				
				if (smallCluster == null || smallCluster.getFeatures().size() == 0) {
					newClusters.remove(small);
					break;
				}
			}
		}
		
		// Update clusters.
		this.clusters = newClusters;
		
		// If cluster size remains the same, we must've reached the end.
		if (originalSize == newClusters.size()) {
			this.reduceData(minDataCoverage);
			
			// Returning false means we stopped before the actual number of times (in the case of a full-prune, it's true).
			return times <= 0;
		}
		
		// If this was the last time, we can end here, otherwise, if this is a full-prune, then make another round.
		if (times == 1) {
			this.reduceData(minDataCoverage);
			
			return true;
		} else if (times < 1) {
			return this.prune(times, minDataCoverage);
		}
		
		// Prune next round.
		return this.prune(times-1, minDataCoverage);
	}
	
	/**
	 * Clears this cluster world.
	 */
	public void clear() {
		for(FeaturesCluster<T> cluster : this.clusters) {
			cluster.clear();
		}
		this.clusters.clear();
	}
	
	@Override
	public String toString() {
		return this.clusters.size() + " clusters";
	}
}