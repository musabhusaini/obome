package edu.sabanciuniv.dataMining.experiment.models.setcover;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.experiment.models.Review;

@Entity
@Table(name="setcover_reviews")
public class SetCoverReview extends IdentifiableObject implements Comparable<SetCoverReview> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SetCover setCover;
	private int utilityScore;
	private boolean seen;
	private Review review;
	
	public SetCoverReview() {
		this(new SetCover());
	}
	
	public SetCoverReview(SetCover setCover) {
		this(setCover, new Review());
	}
	
	public SetCoverReview(SetCover setCover, Review review) {
		this.setSetCover(setCover);
		this.setReview(review);
	}
	
	@ManyToOne
	@JoinColumn(name="setcover_uuid")
	@Basic(fetch=FetchType.LAZY)
	public SetCover getSetCover() {
		return setCover;
	}

	public SetCover setSetCover(SetCover setCover) {
		if (setCover == null) {
			throw new IllegalArgumentException("Must provide a set cover.");
		}
		return this.setCover = setCover;
	}

	@Column(name="utility_score")
	public int getUtilityScore() {
		return this.utilityScore;
	}
	
	public int setUtilityScore(int utilityScore) {
		return this.utilityScore = utilityScore;
	}
	
	@Column
	public boolean isSeen() {
		return seen;
	}
	
	public boolean setSeen(boolean seen) {
		return this.seen = seen;
	}
	
	@ManyToOne
	@JoinColumn(name="review_uuid")
	@Basic(fetch=FetchType.LAZY)
	public Review getReview() {
		return this.review;
	}
	
	public void setReview(Review review) {
		if (setCover == null) {
			throw new IllegalArgumentException("Must provide a set cover.");
		}
		this.review = review;
	}

	@Override
	public int compareTo(SetCoverReview other) {
		return this.getUtilityScore() - other.getUtilityScore();
	}
}