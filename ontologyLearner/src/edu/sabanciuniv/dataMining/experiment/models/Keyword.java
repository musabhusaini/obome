package edu.sabanciuniv.dataMining.experiment.models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;

@Entity
@Table(name="keywords")
public class Keyword extends IdentifiableObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Aspect aspect;
	private String label;
	
	public Keyword() {
		this(new Aspect());
	}
	
	public Keyword(Aspect aspect) {
		this(aspect, "");
	}
	
	public Keyword(Aspect aspect, String label) {
		this.setAspect(aspect);
		this.setLabel(label);
	}

	@ManyToOne
	@JoinColumn(name="aspect_uuid")
	@Basic(fetch=FetchType.LAZY)
	public Aspect getAspect() {
		return aspect;
	}
	public void setAspect(Aspect aspect) {
		this.aspect = aspect;
	}

	@Column(nullable=false)
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}