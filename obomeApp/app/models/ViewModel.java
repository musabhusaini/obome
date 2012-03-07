package models;

import java.util.UUID;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import play.db.jpa.GenericModel;
import edu.sabanciuniv.dataMining.data.Identifiable;

@MappedSuperclass
public abstract class ViewModel extends GenericModel implements Identifiable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	public String uuid;
	
	public ViewModel() {
		this.setIdentifier(UUID.randomUUID());
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
        
    public void setIdentifier(UUID uuid) {
    	this.setIdentifier(uuid.toString());
    }
    
    public void setIdentifier(String uuid) {
    	this.uuid = UUID.fromString(uuid).toString();
    }
}