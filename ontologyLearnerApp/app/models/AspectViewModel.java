package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import com.google.common.collect.Lists;

import edu.sabanciuniv.dataMining.data.Identifiable;
import edu.sabanciuniv.dataMining.experiment.models.Aspect;
import edu.sabanciuniv.dataMining.experiment.models.Keyword;

import java.util.*;

@Entity
public class AspectViewModel extends ViewModel implements Comparable<AspectViewModel> {
	
    public String label;
    
    public AspectViewModel() {
    }
    
    public AspectViewModel(Aspect aspect) {
    	this.uuid = aspect.getIdentifier().toString();
    	this.label = aspect.getLabel();
    }

	@Override
	public int compareTo(AspectViewModel o) {
		return this.label.compareTo(o.label);
	}
}
