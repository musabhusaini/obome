package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import edu.sabanciuniv.dataMining.experiment.models.Keyword;

import java.util.*;

@Entity
public class KeywordViewModel extends ViewModel {
    
	public String label;
	
	public KeywordViewModel() {
	}
	
	public KeywordViewModel(Keyword keyword) {
		this.uuid = keyword.getIdentifier().toString();
		this.label = keyword.getLabel();
	}
}
