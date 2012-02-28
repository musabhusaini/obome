package models;

import edu.sabanciuniv.dataMining.experiment.models.Corpus;

public class OpinionCorpusViewModel extends ViewModel {
	public String name;
	public long size;
	
	public OpinionCorpusViewModel() {
	}
	
	public OpinionCorpusViewModel(Corpus corpus) {
    	this.uuid = corpus.getIdentifier().toString();
    	this.name = corpus.getName();
    }
}
