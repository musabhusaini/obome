package models;

import javax.persistence.Entity;

import edu.sabanciuniv.dataMining.experiment.models.Keyword;

@Entity
public class KeywordViewModel extends ViewModel implements Comparable<KeywordViewModel> {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String label;
	
	public KeywordViewModel() {
	}
	
	public KeywordViewModel(Keyword keyword) {
		this.uuid = keyword.getIdentifier().toString();
		this.label = keyword.getLabel();
	}

	@Override
	public int compareTo(KeywordViewModel o) {
		return this.label.compareTo(o.label);
	}
}
