package models;

import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;

@Entity
public class OpinionMinerResultViewModel extends ViewModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DocumentViewModel document;
	
	@ElementCollection
	public Map<String, Float> scorecard;
}
