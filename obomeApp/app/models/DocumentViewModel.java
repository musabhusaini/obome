package models;

import java.util.UUID;

import javax.persistence.Entity;

import edu.sabanciuniv.dataMining.experiment.models.OpinionDocument;

@Entity
public class DocumentViewModel extends ViewModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String text;
    
	public DocumentViewModel() {
		this(UUID.randomUUID(), "");
	}
	
    public DocumentViewModel(UUID uuid, String text) {
    	super(uuid);
    	this.text = text;
    }
    
    public DocumentViewModel(String uuid, String text) {
    	this(UUID.fromString(uuid), text);
    }
    
    public DocumentViewModel(OpinionDocument document) {
    	this(document.getIdentifier(), document.getContent());
    }
}