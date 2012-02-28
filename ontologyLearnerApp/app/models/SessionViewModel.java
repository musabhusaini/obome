package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@Entity(name="sessions")
public class SessionViewModel extends ViewModel {
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date created;
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date lastActivity;
	
	public SessionViewModel() {
		this.created = new Date(); 
	}
	
	public void keepAlive() {
		this.lastActivity = new Date();
		this.save();
	}
}
