package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import com.google.common.collect.Lists;

import edu.sabanciuniv.dataMining.data.Identifiable;
import edu.sabanciuniv.dataMining.experiment.models.Corpus;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;

import java.util.*;

@Entity
public class OpinionCollectionViewModel extends ViewModel {
    public String name;
    public String corpusName;
    public int offset;
    public int corpusSize;
    public long size;
    public double errorTolerance;
    public Date timestamp;
    
    public OpinionCollectionViewModel() {
    }
    
    public OpinionCollectionViewModel(SetCover sc) {
		this.uuid = sc.getIdentifier().toString();
		this.name = sc.getName();
		this.corpusName = sc.getCorpus().getName();
		this.offset = sc.getCoverOffset();
		this.corpusSize = sc.getCoverSize();
		this.errorTolerance = sc.getErrorTolerance();
		this.timestamp = sc.getTimestamp();
    }
}