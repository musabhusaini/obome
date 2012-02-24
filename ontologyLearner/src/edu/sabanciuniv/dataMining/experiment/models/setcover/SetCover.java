package edu.sabanciuniv.dataMining.experiment.models.setcover;

import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.experiment.models.Aspect;
import edu.sabanciuniv.dataMining.experiment.models.Corpus;

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
	private double errorTolerance;
	private List<SetCoverItem> items;
	private Date timestamp;
	private List<Aspect> aspects;
	private Corpus corpus;
	
	@Column(nullable=false)
	public String getName() {
		return this.name;
	}
	
	public String setName(String name) {
		return this.name = name;
	}

	@Column(name="cover_offset")
	public int getCoverOffset() {
		return coverOffset;
	}

	public int setCoverOffset(int coverageOffset) {
		return this.coverOffset = coverageOffset;
	}

	@Column(name="cover_size")
	public int getCoverSize() {
		return coverSize;
	}

	public int setCoverSize(int coverageSize) {
		return this.coverSize = coverageSize;
	}

	@Column(name="error_tolerance")
	public double getErrorTolerance() {
		return errorTolerance;
	}

	public void setErrorTolerance(double errorTolerance) {
		this.errorTolerance = errorTolerance;
	}

	@Version
	@Temporal(TemporalType.TIMESTAMP)
	public Date getTimestamp() {
		return this.timestamp;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@OneToMany(mappedBy="setCover", cascade=CascadeType.ALL)
	public List<SetCoverItem> getItems() {
		return this.items;
	}
	
	public List<SetCoverItem> setItems(List<SetCoverItem> items) {
		return this.items = items;
	}
	
	@OneToMany(mappedBy="setCover", cascade=CascadeType.ALL)
	public List<Aspect> getAspects() {
		return this.aspects;
	}
	
	public List<Aspect> setAspects(List<Aspect> aspects) {
		return this.aspects = aspects;
	}
	
	@ManyToOne
	@JoinColumn(name="corpus_uuid")
	@Basic(fetch=FetchType.LAZY)
	public Corpus getCorpus() {
		return corpus;
	}

	public void setCorpus(Corpus corpus) {
		this.corpus = corpus;
	}
}