package edu.sabanciuniv.dataMining.experiment.models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;

@Entity
@Table(name="corpora")
public class Corpus extends IdentifiableObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String ownerSessionId;
	private List<OpinionDocument> opinionDocuments;
	private List<SetCover> setCovers;
	
	@Column(nullable=false)
	public String getName() {
		return this.name;
	}
	
	public Corpus setName(String name) {
		this.name = name;
		return this;
	}
	
	@Column(name="owner_session_id")
	public String getOwnerSessionId() {
		return this.ownerSessionId;
	}
	
	public Corpus setOwnerSessionId(String ownerSession) {
		this.ownerSessionId = ownerSession;
		return this;
	}
	
	@OneToMany(mappedBy="corpus", cascade=CascadeType.ALL)
	public List<OpinionDocument> getOpinionDocuments() {
		return this.opinionDocuments;
	}
	
	public Corpus setOpinionDocuments(List<OpinionDocument> opinionDocument) {
		this.opinionDocuments = opinionDocument;
		return this;
	}
	
	@OneToMany(mappedBy="corpus", cascade=CascadeType.ALL)
	public List<SetCover> getSetCovers() {
		return this.setCovers;
	}
	
	public Corpus setSetCovers(List<SetCover> setCovers) {
		this.setCovers = setCovers;
		return this;
	}
}