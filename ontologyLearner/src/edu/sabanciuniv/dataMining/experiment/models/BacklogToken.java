package edu.sabanciuniv.dataMining.experiment.models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;

@Entity
@Table(name="backlog_tokens")
public class BacklogToken extends IdentifiableObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String label;
	private Keyword keyword;
	private SetCover setCover;
	
	@Column(nullable=false)
	public String getLabel() {
		return label;
	}

	public BacklogToken setLabel(String label) {
		this.label = label;
		return this;
	}
	
	@ManyToOne
	@JoinColumn(name="keyword_uuid", nullable=true)
	@Basic(fetch=FetchType.LAZY)
	public Keyword getKeyword() {
		return this.keyword;
	}
	
	public BacklogToken setKeyword(Keyword keyword) {
		this.keyword = keyword;
		return this;
	}
	
	@ManyToOne
	@JoinColumn(name="setcover_uuid", nullable=false)
	@Basic(fetch=FetchType.LAZY)
	public SetCover getSetCover() {
		return setCover;
	}

	public BacklogToken setSetCover(SetCover setCover) {
		this.setCover = setCover;
		return this;
	}
}