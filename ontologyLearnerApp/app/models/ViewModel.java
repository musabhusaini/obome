package models;

import java.util.UUID;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import edu.sabanciuniv.dataMining.data.Identifiable;

import play.db.jpa.GenericModel;

@MappedSuperclass
public abstract class ViewModel extends GenericModel implements Identifiable {
	@Id
	public String uuid;
	
	public ViewModel() {
		this.uuid = UUID.randomUUID().toString();
	}
	
	public String getId() {
		return this.uuid;
	}
	
    @Override
    public Object _key() {
        return getId();
    }
    
    @Override
	public UUID getIdentifier() {
		return UUID.fromString(this.uuid);
	}
}