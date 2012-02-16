package models;

import java.util.UUID;

import javax.persistence.Entity;

import edu.sabanciuniv.dataMining.data.Identifiable;

import play.db.jpa.Model;

@Entity
public class DocumentViewModel extends ViewModel {
	public String text;
    
    public DocumentViewModel(String uuid, String text) {
    	this.uuid = uuid;
    	this.text = text;
    }
}