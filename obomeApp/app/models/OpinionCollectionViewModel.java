package models;

import java.util.Date;

import javax.persistence.Entity;

import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;

@Entity
public class OpinionCollectionViewModel extends ViewModel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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
		this.errorTolerance = sc.getErrorTolerance() * 100;
		this.timestamp = sc.getTimestamp();
    }
}