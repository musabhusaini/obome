package models;

import javax.persistence.Entity;

import edu.sabanciuniv.dataMining.experiment.models.Corpus;

@Entity
public class OpinionCorpusViewModel extends ViewModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String name;
	public long size;
	public double progress;
	
	public OpinionCorpusViewModel() {
	}
	
	public OpinionCorpusViewModel(Corpus corpus) {
    	this.uuid = corpus.getIdentifier().toString();
    	this.name = corpus.getName();
    }
}
