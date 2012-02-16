package edu.sabanciuniv.dataMining.experiment.models.setcover;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.google.common.collect.Lists;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.experiment.models.Aspect;

@Entity
@Table(name="setcovers")
public class SetCover extends IdentifiableObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;
	private int coverOffset;
	private int coverSize;
	private List<SetCoverReview> reviews;
	private Date timestamp;
	private List<Aspect> aspects;
	
	@Column(nullable=false)
	public String getName() {
		return this.name;
	}
	
	public String setName(String name) {
		return this.name = name;
	}

	@Column
	public int getCoverOffset() {
		return coverOffset;
	}

	public int setCoverOffset(int coverageOffset) {
		return this.coverOffset = coverageOffset;
	}

	@Column
	public int getCoverSize() {
		return coverSize;
	}

	public int setCoverSize(int coverageSize) {
		return this.coverSize = coverageSize;
	}

	@Version
	@Temporal(TemporalType.TIMESTAMP)
	public Date getTimestamp() {
		return this.timestamp;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@OneToMany(mappedBy="setCover")
	public List<SetCoverReview> getReviews() {
		if (this.reviews == null) {
			this.setReviews(null);
		}
		
		return this.reviews;
	}
	
	public List<SetCoverReview> setReviews(Iterable<SetCoverReview> reviews) {
		if (reviews == null) {
			reviews = Collections.emptyList();
		}
		return this.reviews = Lists.newArrayList(reviews);
	}
	
	@OneToMany(mappedBy="setCover")
	public List<Aspect> getAspects() {
		if (aspects == null) {
			this.setAspects(null);
		}
		
		return this.aspects;
	}
	
	public void setAspects(List<Aspect> aspects) {
		if (aspects == null) {
			aspects = Collections.emptyList();
		}
		this.aspects = Lists.newArrayList(aspects);
	}
}