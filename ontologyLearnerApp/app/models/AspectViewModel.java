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
public class AspectViewModel extends ViewModel {
	
    public String label;
    
    public AspectViewModel() {
    }
    
    public AspectViewModel(Aspect aspect) {
    	this.uuid = aspect.getIdentifier().toString();
    	this.label = aspect.getLabel();
    }
}
