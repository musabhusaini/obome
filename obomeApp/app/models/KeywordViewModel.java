package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import edu.sabanciuniv.dataMining.experiment.models.Keyword;

import java.util.*;

@Entity
public class KeywordViewModel extends ViewModel implements Comparable<KeywordViewModel> {
    
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
