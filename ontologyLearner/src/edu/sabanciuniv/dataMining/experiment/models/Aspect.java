package edu.sabanciuniv.dataMining.experiment.models;

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

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;

@Entity
@Table(name="aspects")
public class Aspect extends IdentifiableObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String label;
	private SetCover setCover;
	private List<Keyword> keywords;

	public Aspect() {
		this(new SetCover(), "");
	}
	
	public Aspect(SetCover setCover) {
		this(setCover, "");
	}
	
	public Aspect(SetCover setCover, String label) {
		this.setSetCover(setCover);
		this.setLabel(label);
	}

	@Column(nullable=false)
	public String getLabel() {
		return label;
	}

	public String setLabel(String name) {
		return this.label = name;
	}

	@ManyToOne
	@JoinColumn(name="setcover_uuid", nullable=false)
	@Basic(fetch=FetchType.LAZY)
	public SetCover getSetCover() {
		return setCover;
	}

	public SetCover setSetCover(SetCover setCover) {
		return this.setCover = setCover;
	}

	@OneToMany(mappedBy="aspect", cascade=CascadeType.ALL)
	public List<Keyword> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<Keyword> keywords) {
		this.keywords = keywords;
	}
}