package edu.sabanciuniv.dataMining.experiment.models.setcover.builder;

import javax.persistence.EntityManager;

import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;

public abstract class SetCoverBuilderBase implements SetCoverBuilder {

	protected EntityManager entityManager;
	
	public SetCoverBuilderBase(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	@Override
	public abstract void seeUniverseExample(TextDocument document);

	@Override
	public abstract SetCover build(String name);
}
