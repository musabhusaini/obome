package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import com.google.common.collect.Lists;

import edu.sabanciuniv.dataMining.data.Identifiable;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCoverReview;

import java.util.*;

@Entity
public class ReviewCollectionViewModel extends ViewModel {
    public String name;
    public int offset;
    public int corpusSize;
    public int size;
    public Date timestamp;
    
    public ReviewCollectionViewModel() {
    }
    
    public ReviewCollectionViewModel(SetCover sc) {
		this.uuid = sc.getIdentifier().toString();
		this.name = sc.getName();
		this.offset = sc.getCoverOffset();
		this.corpusSize = sc.getCoverSize();
		this.timestamp = sc.getTimestamp();
		this.size = sc.getReviews().size();
    }
}