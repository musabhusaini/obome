package jobs;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import models.SessionViewModel;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.experiment.models.Corpus;
import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
@Every(OrphanCorporaCleaner.FREQUENCY + "h")
public class OrphanCorporaCleaner extends Job {
	
	public static final int CORPUS_TIMEOUT=12;
	public static final int FREQUENCY=1;
	
	@Override
	public void doJob() {
		// Delete all orphan corpora.
		System.out.println("Starting to clean orphan corpora.");
		
		EntityManager em = OntologyLearnerProgram.em();
		em.getTransaction().begin();
		
		List<Corpus> corpora = em.createQuery("SELECT c FROM Corpus c WHERE c.ownerSessionId!=null", Corpus.class)
				.getResultList();
		
		for (Corpus corpus : corpora) {
			SessionViewModel session = SessionViewModel.findById(corpus.getOwnerSessionId());
			if (session == null || session.lastActivity.before(new DateTime().minusHours(CORPUS_TIMEOUT).toDate())) {
				em.remove(corpus);
			}
		}

		em.getTransaction().commit();
		em.close();
		
		System.out.println("Done cleaning orphan corpora.");
	}
}