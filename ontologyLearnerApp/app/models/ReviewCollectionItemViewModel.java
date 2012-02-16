package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import edu.sabanciuniv.dataMining.data.Identifiable;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCoverReview;

import java.util.*;

@Entity
public class ReviewCollectionItemViewModel extends ViewModel {
    public String reviewUuid;
    public int utilityScore;
    public boolean seen;
    
    public ReviewCollectionItemViewModel() {
    }
    
    public ReviewCollectionItemViewModel(SetCoverReview scReview) {
    	this.uuid = scReview.getIdentifier().toString();
    	this.reviewUuid = scReview.getReview().getIdentifier().toString();
    	this.utilityScore = scReview.getUtilityScore();
    	this.seen = scReview.isSeen();
    }
}
