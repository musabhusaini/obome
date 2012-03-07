package models;

import javax.persistence.Entity;

@Entity
public class DocumentViewModel extends ViewModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String text;
    
    public DocumentViewModel(String uuid, String text) {
    	this.uuid = uuid;
    	this.text = text;
    }
}