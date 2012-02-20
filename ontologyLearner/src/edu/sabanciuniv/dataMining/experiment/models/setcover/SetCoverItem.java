package edu.sabanciuniv.dataMining.experiment.models.setcover;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.experiment.models.OpinionDocument;

@Entity
@Table(name="setcover_items")
public class SetCoverItem extends IdentifiableObject implements Comparable<SetCoverItem> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SetCover setCover;
	private int utilityScore;
	private boolean seen;
	private OpinionDocument opinionDocument;
	
	public SetCoverItem() {
		this(new SetCover());
	}
	
	public SetCoverItem(SetCover setCover) {
		this(setCover, new OpinionDocument());
	}
	
	public SetCoverItem(SetCover setCover, OpinionDocument review) {
		this.setSetCover(setCover);
		this.setOpinionDocument(review);
	}
	
	@ManyToOne
	@JoinColumn(name="setcover_uuid", nullable=false)
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
	@JoinColumn(name="opinion_document_uuid", nullable=false)
	@Basic(fetch=FetchType.LAZY)
	public OpinionDocument getOpinionDocument() {
		return this.opinionDocument;
	}
	
	public void setOpinionDocument(OpinionDocument review) {
		if (setCover == null) {
			throw new IllegalArgumentException("Must provide a set cover.");
		}
		this.opinionDocument = review;
	}

	@Override
	public int compareTo(SetCoverItem other) {
		return this.getUtilityScore() - other.getUtilityScore();
	}
}