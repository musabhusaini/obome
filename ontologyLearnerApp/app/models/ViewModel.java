package models;

import java.util.UUID;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import edu.sabanciuniv.dataMining.data.Identifiable;
import edu.sabanciuniv.dataMining.data.IdentifiableObject;

import play.db.jpa.GenericModel;

@MappedSuperclass
public abstract class ViewModel extends GenericModel implements Identifiable {
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