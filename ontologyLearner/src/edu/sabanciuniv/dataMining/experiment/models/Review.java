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
@Table(name="reviews")
public class Review extends IdentifiableObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String hotelId;
	private String author;
	private String content;
	private Date date;
	private short rating;

	@Column(name="hotel_id", columnDefinition="VARCHAR(10)", length=10)
	public String getHotelId() {
		return hotelId;
	}
	
	public String setHotelId(String hotelId) {
		return this.hotelId = hotelId;
	}

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

	@Column
	public short getRating() {
		return rating;
	}

	public short setRating(short rating) {
		return this.rating = rating;
	}
	
	@Override
	public String toString() {
		return String.format("%s (for %s on %s): %s",
				this.getAuthor(), this.getHotelId().trim(), DateFormat.getInstance().format(this.getDate()), this.getContent());
	}
}