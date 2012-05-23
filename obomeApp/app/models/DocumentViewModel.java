package models;

import java.util.UUID;

import javax.persistence.Entity;

import com.google.gson.Gson;

import edu.sabanciuniv.dataMining.experiment.models.OpinionDocument;

@Entity
public class DocumentViewModel extends ViewModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DisplayTextModel text;
    
	public DocumentViewModel() {
		this(UUID.randomUUID(), "");
	}
	
    public DocumentViewModel(UUID uuid, DisplayTextModel text) {
    	super(uuid);
    	this.text = text;
    }
    
    public DocumentViewModel(UUID uuid, String text) {
    	this(uuid, new Gson().fromJson(text, DisplayTextModel.class));
    }
    
    public DocumentViewModel(String uuid, String text) {
    	this(UUID.fromString(uuid), text);
    }
    
    public DocumentViewModel(OpinionDocument document) {
    	this(document.getIdentifier(), document.getContent());
    }
}