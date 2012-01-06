package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Document extends Model {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6635638706289918437L;
	
	public String text;
    
    public Document(String text) {
    	this.text = text;
    }
}