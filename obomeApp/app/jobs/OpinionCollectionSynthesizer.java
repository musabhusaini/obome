package jobs;

import java.util.Map;

import javax.persistence.EntityManager;

import com.google.common.collect.Maps;

import edu.sabanciuniv.dataMining.data.factory.QueryBasedObjectFactory;
import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.experiment.models.Corpus;
import edu.sabanciuniv.dataMining.experiment.models.OpinionDocument;
import edu.sabanciuniv.dataMining.experiment.models.factory.OpinionDocumentTaggedContentFactory;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.experiment.models.setcover.builder.EagerSetCoverBuilder;
import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;
import edu.sabanciuniv.dataMining.util.LargeTypedQuery;
import models.OpinionCollectionViewModel;
import models.OpinionCorpusViewModel;
import play.db.jpa.JPA;
import play.jobs.Job;

public class OpinionCollectionSynthesizer extends Job<SetCover> {
	
	private static Map<String, OpinionCollectionSynthesizer> registry = Maps.newHashMap();
	
	public static OpinionCollectionSynthesizer get(String uuid) {
		if (registry.containsKey(uuid)) {
			return registry.get(uuid);
		}
		return null;
	}
	
	private Corpus corpus;
	private double progress;
	
	public OpinionCollectionSynthesizer(Corpus corpus) {
		this.corpus = corpus;
		
		registry.put(corpus.getIdentifier().toString(), this);
	}

	public double getProgress() {
		return this.progress;
	}
	
	@Override
	public SetCover doJobWithResult() {
		EntityManager em = OntologyLearnerProgram.em();
		em.getTransaction().begin();

		long corpusSize = em.createQuery("SELECT COUNT(doc) FROM OpinionDocument doc WHERE doc.corpus=:corpus", Long.class)
				.setParameter("corpus", this.corpus)
				.getSingleResult();
		
		EagerSetCoverBuilder eagerBuilder = new EagerSetCoverBuilder(em);
		LargeTypedQuery<OpinionDocument> query = new LargeTypedQuery<>(em.createQuery("SELECT doc FROM OpinionDocument doc WHERE doc.corpus=:corpus",
				OpinionDocument.class).setParameter("corpus", this.corpus),
				1000);
		OpinionDocumentTaggedContentFactory factory = new OpinionDocumentTaggedContentFactory(new QueryBasedObjectFactory<>(query));
		TextDocument document;
		while ((document = factory.create()) != null) {
			eagerBuilder.seeUniverseExample(document);
			
			// Save progress (we decrease 5% to account for the time it takes to commit).
			this.progress = factory.getCount() / (corpusSize * 1.05);
		}
		
		SetCover setCover = eagerBuilder.build();
		
		this.corpus = em.find(Corpus.class, this.corpus.getId());
		
		setCover.setCoverOffset(0);
		setCover.setCorpus(this.corpus);
		setCover.setCoverSize((int)corpusSize);
		setCover.setName(this.corpus.getName());
		setCover.setErrorTolerance(0.0);
		em.persist(setCover);
		em.getTransaction().commit();
		
		registry.remove(this.corpus.getIdentifier().toString());
		return setCover;
	}
}
