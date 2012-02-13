package edu.sabanciuniv.dataMining.experiment.models.setcover.builder;

import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;

public interface SetCoverBuilder {
	public void seeUniverseExample(TextDocument document);
	
	public SetCover build(String name);
}
