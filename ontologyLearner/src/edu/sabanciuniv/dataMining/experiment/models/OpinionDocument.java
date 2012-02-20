package edu.sabanciuniv.dataMining.experiment.models;

import java.text.DateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions;
import edu.sabanciuniv.dataMining.data.text.TextDocument;

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
	private String corpusName;
	
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

	@Column(name="corpus_name")
	public String getCorpusName() {
		return this.corpusName;
	}
	
	public void setCorpusName(String corpusName) {
		this.corpusName = corpusName;
	}
	
	@Override
	public String toString() {
		return String.format("%s (on %s): %s", this.getAuthor(), DateFormat.getInstance().format(this.getDate()), this.getContent());
	}
}