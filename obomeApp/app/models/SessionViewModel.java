package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name="sessions")
public class SessionViewModel extends ViewModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
