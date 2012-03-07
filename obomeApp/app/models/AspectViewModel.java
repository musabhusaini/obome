package models;

import javax.persistence.Entity;

import edu.sabanciuniv.dataMining.experiment.models.Aspect;

@Entity
public class AspectViewModel extends ViewModel implements Comparable<AspectViewModel> {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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
