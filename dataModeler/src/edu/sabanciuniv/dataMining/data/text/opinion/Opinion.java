package edu.sabanciuniv.dataMining.data.text.opinion;

import java.sql.Date;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;

public class Opinion extends IdentifiableObject {
	private String author;
	private String content;
	private Date date;
	private String dataRole;
	
	public String getAuthor() {
		return author;
	}
	
	public String getContent() {
		return content;
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getDataRole() {
		return dataRole;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public void setDataRole(String dataRole) {
		this.dataRole = dataRole;
	}
	
	@Override
	public String toString() {
		return "uuid: " + this.getIdentifier() +
				", author: " + this.getAuthor() +
				", date: " + this.getDate() +
				", content: " + this.getContent();
	}
}
