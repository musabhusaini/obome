package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Document extends Model {
	public String text;
	public String uuid;
    
    public Document(String uuid, String text) {
    	this.uuid = uuid;
    	this.text = text;
    }
}