package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import play.mvc.Http.Request;
import play.mvc.Scope.Session;

@Entity(name="sessions")
public class SessionViewModel extends ViewModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String remoteAddress;
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date created;
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date lastActivity;
	
	public SessionViewModel() {
		this(Session.current(), Request.current());
	}
	
	public SessionViewModel(Session session, Request request) {
		if (session != null) {
			this.setIdentifier(session.getId());
		}
		if (request != null) {
			this.remoteAddress = request.remoteAddress;
		}
		
		this.created = this.lastActivity = new Date();
	}
	
	public void keepAlive() {
		this.lastActivity = new Date();
		this.save();
	}
}
