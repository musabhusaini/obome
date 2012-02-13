package edu.sabanciuniv.dataMining.data.clustering.text;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.data.text.HasFeatures;
import edu.sabanciuniv.dataMining.data.text.IdentifiableWithFeatures;
import edu.stanford.nlp.ling.HasWord;

/**
 * A cluster of features.
 * @author Mus'ab Husaini
 * @param <T> Type of features; must extend {@link HasWord}. 
 */
public class FeaturesCluster<T extends HasWord> extends IdentifiableObject implements HasFeatures<T>, Comparable<FeaturesCluster<T>>, Cloneable {

	static class ClusterHead<T extends HasWord> extends IdentifiableWithFeatures<T> implements Cloneable {
		protected Map<T,Integer> headMap;
		protected int dummyMembers;

		private Map<T,Integer> makeMap(Iterable<T> features) {
			Map<T,Integer> newHeadMap = Maps.newHashMap();
			for (T feature : features) {
				newHeadMap.put(feature, 1);
			}		
			return newHeadMap;
		}
		
		private ClusterHead() {
		}
		
		public ClusterHead(IdentifiableWithFeatures<T> features) {
			this(features.getIdentifier(), features.getFeatures());
		}
		
		public ClusterHead(UUID uuid, Iterable<T> features) {
			super(uuid, features);
		}
		
		public ClusterHead(UUID uuid, Map<T,Integer> headMap) {
			super(uuid);
			this.setMap(headMap);
		}
		
		public Map<T,Integer> getMap() {
			return Collections.unmodifiableMap(this.headMap);
		}
		
		public Map<T,Integer> setMap(Map<T,Integer> headMap) {
			return this.headMap = Maps.newHashMap(headMap);
		}
		
		public Map<T,Integer> subsede(IdentifiableWithFeatures<T> features) {
			return this.subsede(new ClusterHead<T>(features));
		}
		
		public Map<T,Integer> subsede(ClusterHead<T> features) {
			this.setIdentifier(features.getIdentifier());
			
			Map<T,Integer> newHeadMap = Maps.newHashMap(features.headMap);
			for (T feature : this.headMap.keySet()) {
				if (newHeadMap.containsKey(feature)) {
					newHeadMap.put(feature, newHeadMap.get(feature) + this.headMap.get(feature));
				}
			}
			return this.headMap = newHeadMap;
		}
		
		public Map<T,Integer> add(ClusterHead<T> other) {
			Set<T> intersect = Sets.intersection(this.headMap.keySet(), other.headMap.keySet());
			for (T feature : intersect) {
				this.headMap.put(feature, this.headMap.get(feature) + other.headMap.get(feature));
			}
			return this.headMap;			
		}
		
		public Map<T,Integer> add(Iterable<T> features) {
			return this.add(new ClusterHead<T>(UUID.randomUUID(), features));
		}
		
		public int getDummyMembers() {
			return this.dummyMembers;
		}
		
		public int setDummyMembers(int dummyMembers) {
			return this.dummyMembers = dummyMembers;
		}
		
		public int addDummyMembers(int dummyMembers) {
			return this.dummyMembers += dummyMembers;
		}
		
		public int getMemberCount() {
			int total = 0;
			for (int count : this.headMap.values()) {
				total += count;
			}
			return total + this.getDummyMembers();
		}
		
		public void clearMembers() {
			for (Map.Entry<T,Integer> kvp : this.headMap.entrySet()) {
				kvp.setValue(0);
			}
			this.dummyMembers = 0;
		}

		@Override
		public Set<T> getFeatures() {
			return this.headMap.keySet();
		}
		
		@Override
		public Set<T> setFeatures(Iterable<T> features) {
			this.headMap = makeMap(features);
			return this.getFeatures();
		}

		@Override
		public ClusterHead<T> cloneOut(Iterable<T> otherFeatures) {
			Map<T,Integer> newHeadMap = Maps.newHashMap(Maps.filterKeys(this.headMap, Predicates.in(Lists.newArrayList(otherFeatures))));
			return new ClusterHead<T>(this.getIdentifier(), newHeadMap);
		}
		
		@Override
		public ClusterHead<T> clone() {
			ClusterHead<T> clusterHead = new ClusterHead<>();
			clusterHead.setIdentifier(this.getIdentifier());
			clusterHead.setDummyMembers(this.dummyMembers);
			clusterHead.setMap(this.headMap);
			return clusterHead;
		}
		
		@Override
		public int compareTo(IdentifiableWithFeatures<T> other) {
			if (other instanceof FeaturesCluster.ClusterHead) {
				ClusterHead<T> otherHead = (ClusterHead<T>)other;
				return super.compareTo(other) * (this.getMemberCount() - otherHead.getMemberCount());
			}
			
			return super.compareTo(other);
		}
	}
	
	static class SplitTuple<T extends HasWord> {
		ClusterHead<T> accept;
		ClusterHead<T> reject;
	}
	
	private ClusterHead<T> head;

	private FeaturesCluster() {
	}
	
	private boolean isSubsetOf(HasFeatures<T> features) {
		return Sets.difference(this.head.getFeatures(), features.getFeatures()).size() == 0 &&
				this.head.getFeatures().size() != features.getFeatures().size();
	}

	private FeaturesCluster(ClusterHead<T> head) {
		this();
		
		this.setIdentifier(head.getIdentifier());
		this.head = head;
	}
	
	/**
	 * Creates a new instance of {@link FeaturesCluster}
	 * @param features
	 */
	public FeaturesCluster(IdentifiableWithFeatures<T> features) {
		this(new ClusterHead<T>(features));
	}
	
	/**
	 * Gets the head of the cluster.
	 * @return An object representing the head of this cluster.
	 */
	public IdentifiableWithFeatures<T> getHead() {
		return this.head;
	}
	
	private SplitTuple<T> getSplit(ClusterHead<T> features) {
		SplitTuple<T> tuple = new SplitTuple<>();
		tuple.reject = features.cloneOut(Sets.difference(features.getFeatures(), this.head.getFeatures()));
		tuple.accept = features.cloneOut(Sets.intersection(features.getFeatures(), this.head.getFeatures()));
		return tuple;
	}
	
	private ClusterHead<T> getReject(ClusterHead<T> features) {
		return this.getSplit(features).reject;
	}
	
	/**
	 * Gets a {@link IdentifiableWithFeatures} object that contains all the features from the provided object that are not in this cluster.
	 * @param features Features to search for.
	 * @return An object that contains all the features from the provided object that are not in this cluster.
	 */
	public IdentifiableWithFeatures<T> getReject(IdentifiableWithFeatures<T> features) {
		return this.getReject(new ClusterHead<T>(features));
	}

	/**
	 * Add the given feature set to this cluster.
	 * @param features The feature set to add.
	 * @return A feature set which contains all the features from the argument not in this cluster.
	 */
	public FeaturesCluster<T> add(IdentifiableWithFeatures<T> features) {
		return this.add(features, 0);
	}

	/**
	 * Add the given cluster to this cluster with some tolerance.
	 * @param other The cluster to add.
	 * @param rejectTolerance The fraction of features that can be allowed in even when rejected [0, 1].
	 * @return A feature set which contains all the features from the other cluster that are not in this cluster.
	 */
	public FeaturesCluster<T> add(FeaturesCluster<T> features, double rejectTolerance) {
		// Get all the rejected features.
		SplitTuple<T> splitTuple = this.getSplit(features.head);
		
		if (this.isSubsetOf(features)) {
			// If the new set is a super set, then it supersedes the current cluster head.
			this.head.subsede(features.head);
		} else {
			if (splitTuple.reject.getFeatures().size() < features.getFeatures().size()) {
				// If not everything was rejected, then this set can be added here.
				this.head.add(splitTuple.accept.getFeatures());
			}
			
			if (splitTuple.reject.getFeatures().size()/(double)features.getFeatures().size() > rejectTolerance) {
				// Return a new cluster for all rejected ones.
				return new FeaturesCluster<T>(splitTuple.reject);
			}
		}
				
		// This would be the case where we have found a perfect match (nothing to reject). 
		return null;
	}
	
	/**
	 * Add the given feature set to this cluster with some tolerance.
	 * @param features The feature set to add.
	 * @param rejectTolerance The fraction of features that can be allowed in even when rejected [0, 1].
	 * @return A feature set which contains all the features from the argument not in this cluster.
	 */
	public FeaturesCluster<T> add(IdentifiableWithFeatures<T> features, double rejectTolerance) {
		return this.add(new FeaturesCluster<T>(features), rejectTolerance);
	}

	/**
	 * Add the given cluster to this cluster.
	 * @param other The cluster to add.
	 * @return A feature set which contains all the features from the other cluster that are not in this cluster.
	 */
	public FeaturesCluster<T> add(FeaturesCluster<T> other) {
		return this.add(other, 0);
	}
	
	/**
	 * Adds a number of dummy members to this cluster.
	 * @param count Number of dummy members to add.
	 * @return The current cluster.
	 */
	public FeaturesCluster<T> addDummyMembers(int count) {
		if (count < 0) {
			throw new IllegalArgumentException("Count must be positive.");
		}

		this.head.addDummyMembers(count);
		return this;
	}
	
	/**
	 * Gets the number of total members in this cluster.
	 * @return Number of members.
	 */
	public int getMemberCount() {
		return this.head.getMemberCount();
	}
	
	/**
	 * Gets the mass of data in this cluster.
	 * @return Mass of data in this cluster.
	 */
	public double getDataMass() {
		return this.getMemberCount();
	}
	
	/**
	 * Gets the weight of data in this cluster.
	 * @return Weight of data in this cluster.
	 */
	public double getSignificance() {
		return this.getMemberCount() / (double)this.head.getFeatures().size();
	}
	
	/**
	 * Clears the membership of this cluster.
	 */
	public void clear() {
		this.head.clearMembers();
	}
	
	@Override
	public Set<T> getFeatures() {
		return this.head.getFeatures();
	}

	@Override
	public FeaturesCluster<T> cloneOut(Iterable<T> otherFeatures) {
		return new FeaturesCluster<T>((IdentifiableWithFeatures<T>)this.head.cloneOut(otherFeatures));
	}
	
	@Override
	public String toString() {
		return this.getSignificance() + " x " + this.head.toString();
	}

	@Override
	public int compareTo(FeaturesCluster<T> other) {
		double relativeWeight = this.getSignificance() - other.getSignificance();
		double rlogWeight = Math.ceil(Math.log10(Math.abs(relativeWeight)));
		if (rlogWeight < 0) {
			rlogWeight = Math.abs(rlogWeight) + 2;
		} else if (rlogWeight < 2) {
			rlogWeight = 2-rlogWeight;
		} else {
			rlogWeight = 0;
		}
		
		relativeWeight *= Math.pow(10, rlogWeight);
		return (int)Math.round(relativeWeight);
	}
	
	@Override
	public FeaturesCluster<T> clone() {
		FeaturesCluster<T> clonedCluster = new FeaturesCluster<T>();
		clonedCluster.head = this.head.clone();
		
		return clonedCluster;
	}
}