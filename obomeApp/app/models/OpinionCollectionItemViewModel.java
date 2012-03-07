package models;

import javax.persistence.Entity;

import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCoverItem;

@Entity
public class OpinionCollectionItemViewModel extends ViewModel {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String review;
    public int utilityScore;
    public boolean seen;
    
    public OpinionCollectionItemViewModel() {
    }
    
    public OpinionCollectionItemViewModel(SetCoverItem scReview) {
    	this.uuid = scReview.getIdentifier().toString();
    	this.review = scReview.getOpinionDocument().getIdentifier().toString();
    	this.utilityScore = scReview.getUtilityScore();
    	this.seen = scReview.isSeen();
    }
}
