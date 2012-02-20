package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import edu.sabanciuniv.dataMining.data.Identifiable;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCoverItem;

import java.util.*;

@Entity
public class OpinionCollectionItemViewModel extends ViewModel {
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
