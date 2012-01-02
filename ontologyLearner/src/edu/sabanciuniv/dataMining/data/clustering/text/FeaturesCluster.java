package edu.sabanciuniv.dataMining.data.clustering.text;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.sabanciuniv.dataMining.data.text.HasFeatures;
import edu.sabanciuniv.dataMining.data.text.IdentifiableWithFeatures;
import edu.sabanciuniv.dataMining.data.Identifiable;
import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.util.MPredicates;

import edu.stanford.nlp.ling.HasWord;

/**
 * A cluster of features.
 * @author Mus'ab Husaini
 * @param <T> Type of features; must extend {@link HasWord}. 
 */
public class FeaturesCluster<T extends HasWord> extends IdentifiableObject implements HasFeatures<T>, Comparable<FeaturesCluster<T>>, Cloneable {

	private static class SplitTuple<K extends HasWord> {
		IdentifiableWithFeatures<K> accept;
		IdentifiableWithFeatures<K> reject;
	}
	
	private IdentifiableWithFeatures<T> head;
	private List<Identifiable> members;
	private int dummyMembers;

	private FeaturesCluster() {
	}
	
	private boolean isSubsetOf(HasFeatures<T> features) {
		return Sets.difference(this.head.getFeatures(), features.getFeatures()).size() == 0;
	}

	/**
	 * Creates a new instance of {@link FeaturesCluster}
	 * @param features
	 */
	public FeaturesCluster(IdentifiableWithFeatures<T> features) {
		this();
		
		if (features == null) {
			throw new InvalidParameterException("Must supply a list of features.");
		}
		
		this.head = features;
		this.members = new ArrayList<Identifiable>();
		this.members.add(features);
		this.setIdentifier(this.head.getIdentifier());
	}
	
	/**
	 * Gets the head of the cluster.
	 * @return An object representing the head of this cluster.
	 */
	public IdentifiableWithFeatures<T> getHead() {
		return this.head;
	}
	
	/**
	 * Gets all the members of this cluster.
	 * @return All the members of this cluster.
	 */
	public Iterable<Identifiable> getMembers() {
		return Iterables.unmodifiableIterable(this.members);
	}

	private SplitTuple<T> getSplit(IdentifiableWithFeatures<T> features) {
		SplitTuple<T> tuple = new SplitTuple<T>();
		tuple.reject = features.cloneOut(Sets.difference(features.getFeatures(), this.head.getFeatures()));
		tuple.accept = features.cloneOut(Sets.intersection(features.getFeatures(), this.head.getFeatures()));
		return tuple;
	}
	
	/**
	 * Gets a {@link IdentifiableWithFeatures} object that contains all the features from the provided object that are not in this cluster.
	 * @param features Features to search for.
	 * @return An object that contains all the features from the provided object that are not in this cluster.
	 */
	public IdentifiableWithFeatures<T> getReject(IdentifiableWithFeatures<T> features) {
		return this.getSplit(features).reject;
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
	 * Add the given feature set to this cluster with some tolerance.
	 * @param features The feature set to add.
	 * @param rejectTolerance The fraction of features that can be allowed in even when rejected [0, 1].
	 * @return A feature set which contains all the features from the argument not in this cluster.
	 */
	public FeaturesCluster<T> add(IdentifiableWithFeatures<T> features, double rejectTolerance) {
		// Get all the rejected features.
		SplitTuple<T> splitTuple = this.getSplit(features);
		
		// If not everything was rejected, then this set can be added here.
		if (splitTuple.reject.getFeatures().size() < features.getFeatures().size()) {
			this.members.add(splitTuple.accept);
		}

		if (this.isSubsetOf(features)) {
			// If the new set is a power set, then it becomes the cluster head.
			this.head = features;
		} else if (splitTuple.reject.getFeatures().size()/(double)features.getFeatures().size() > rejectTolerance) {
			// Return a new cluster for all rejected ones.
			return new FeaturesCluster<T>(splitTuple.reject);
		}
		
		// This would be the case where we have found a perfect match (nothing to reject). 
		return null;
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
	 * Add the given cluster to this cluster with some tolerance.
	 * @param other The cluster to add.
	 * @param rejectTolerance The fraction of features that can be allowed in even when rejected [0, 1].
	 * @return A feature set which contains all the features from the other cluster that are not in this cluster.
	 */	
	public FeaturesCluster<T> add(FeaturesCluster<T> other, double rejectTolerance) {
		// Try to add the head of the other and get the rejected cluster.
		FeaturesCluster<T> reject = this.add(other.head, rejectTolerance);
		double rejectRatio = (reject == null ? 0 : reject.getFeatures().size()) /
				(double)(other.getFeatures().size() == 0 ? 1 : other.getFeatures().size()); 
		Random rand = new Random();
		
		if (reject == null || reject.head.getFeatures().size() == 0) {
			// If nothing was rejected, then this is a perfect fit.
			// Add all remaining members.
			this.members.addAll(Lists.newArrayList(Iterables.filter(other.members, Predicates.not(MPredicates.identifierEquals(other.head)))));
			return null;
		} else if (reject.head.getFeatures().size() != other.head.getFeatures().size()) {
			// If only some were rejected, then we can partially add this cluster in.
			// Try to add all members to this cluster (ignore the head since it's already there).
			for(Identifiable member : Iterables.filter(other.members, Predicates.not(MPredicates.identifierEquals(other.head)))) {
				// If this member also has features, then we add figure out which part of this member should go where.
				IdentifiableWithFeatures<T> featuresMembers = null;
				if (member instanceof IdentifiableWithFeatures<?>) {
					try {
						// For some reason this is how it works.
						@SuppressWarnings("unchecked")
						IdentifiableWithFeatures<T> temp = (IdentifiableWithFeatures<T>)member;
						featuresMembers = temp;
						
						FeaturesCluster<T> tempCluster = this.add(featuresMembers, rejectTolerance);
						// If some features were rejected, add member to the reject cluster.
						if (tempCluster != null && tempCluster.head.getFeatures().size() != 0) {
							reject.add(tempCluster.head);
						}
					} catch(ClassCastException ex) {
						featuresMembers = null;
					}
				}
				
				if (featuresMembers == null) {
					// Use a probabilistic model to determine where to put this one since exact membership information is lost.
					// The reject ratio determines the probability of this member being in the reject cluster or this one.
					if (rand.nextDouble() < rejectRatio) {
						reject.members.add(member);
					} else {
						this.members.add(member);
					}
				}
			}
			
			// Deal with dummy members (if there are any).
			for (int i=0; i<other.dummyMembers; i++) {
				// Use a probabilistic model to determine where to put this one since exact membership information is lost.
				// The reject ratio determines the probability of this member being in the reject cluster or this one.
				if (rand.nextDouble() < rejectRatio) {
					reject.addDummyMembers(1);
				} else {
					this.addDummyMembers(1);
				}
			}
		} else {
			// Everything was rejected, so no change needed.
			reject = other;
		}
		
		return reject;
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

		this.dummyMembers += count;		
		return this;
	}
	
	/**
	 * Strips the members down to just identification information.
	 * This can potentially help with reduction in memory usage.
	 */
	public void stripMemberFeatures() {
		this.members = Lists.newArrayList(Iterables.transform(this.members, new Function<Identifiable, Identifiable>() {
			@Override
			public Identifiable apply(Identifiable member) {
				return new IdentifiableObject(member.getIdentifier());
			}
		}));
	}
	
	/**
	 * Gets the number of total members in this cluster.
	 * @return Number of members.
	 */
	public int getMemberCount() {
		return this.members.size() + this.dummyMembers;
	}
	
	/**
	 * Gets the mass of data in this cluster.
	 * @return Mass of data in this cluster.
	 */
	public double getDataMass() {
		return this.getMemberCount() * (double)this.head.getFeatures().size();
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
		this.members.clear();
		this.dummyMembers = 0;
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
		return this.getMemberCount() + " x " + this.head.toString();
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
		clonedCluster.head = this.head;
		clonedCluster.members = Lists.newArrayList(this.members);
		clonedCluster.dummyMembers = this.dummyMembers;
		
		return clonedCluster;
	}
}