package edu.sabanciuniv.dataMining.experiment.models;

import java.text.DateFormat;
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
import javax.persistence.Transient;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions;
import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCoverItem;

@Entity
@Table(name="opinion_documents")
public class OpinionDocument extends IdentifiableObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String author;
	private String content;
	private Date date;
	private Corpus corpus;
	private List<SetCoverItem> setCoverItems;
	
	@Column
	public String getAuthor() {
		return author;
	}

	public String setAuthor(String author) {
		return this.author = author;
	}

	@Column(columnDefinition="LONGTEXT")
	public String getContent() {
		return content;
	}

	public String setContent(String content) {
		return this.content = content;
	}

	@Transient
	public TextDocument getTaggedContent() {
		return this.getTaggedContent(new TextDocumentOptions());
	}
	
	@Transient
	public TextDocument getTaggedContent(TextDocumentOptions options) {
		TextDocument doc = new TextDocument(options);
		doc.setIdentifier(this.getIdentifier());
		doc.setText(this.getContent());
		return doc;
	}

	@Temporal(TemporalType.DATE)
	public Date getDate() {
		return date;
	}

	public Date setDate(Date date) {
		return this.date = date;
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
	
	// Don't really care for this, but was running into problems without this cascading. 
	@OneToMany(mappedBy="opinionDocument", cascade=CascadeType.ALL)
	public List<SetCoverItem> getSetCoverItems() {
		return this.setCoverItems;
	}
	
	public void setSetCoverItems(List<SetCoverItem> setCoverItems) {
		this.setCoverItems = setCoverItems;
	}

	@Override
	public String toString() {
		return String.format("%s (on %s): %s", this.getAuthor(), DateFormat.getInstance().format(this.getDate()), this.getContent());
	}
}