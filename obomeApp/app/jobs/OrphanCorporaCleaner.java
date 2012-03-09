package jobs;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import models.SessionViewModel;

import org.joda.time.DateTime;

import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import edu.sabanciuniv.dataMining.experiment.models.Corpus;
import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;

@OnApplicationStart
@Every(OrphanCorporaCleaner.FREQUENCY + "h")
public class OrphanCorporaCleaner extends Job<Object> {
	
	public static final int CORPUS_TIMEOUT=30;
	public static final int FREQUENCY=1;
	
	@Override
	public void doJob() {
		// Delete all orphan corpora.
		Logger.info("Began cleaning orphan corpora");
		
		EntityManager em = OntologyLearnerProgram.em();
		em.getTransaction().begin();
		
		List<Corpus> corpora = em.createQuery("SELECT c FROM Corpus c WHERE c.ownerSessionId!=null", Corpus.class)
				.getResultList();
		
		Date staleDate = new DateTime().minusSeconds(CORPUS_TIMEOUT).toDate();
		
		for (Corpus corpus : corpora) {
			Logger.debug("Let's see if " + corpus.getName() + " needs to be removed");
			SessionViewModel session = SessionViewModel.findById(corpus.getOwnerSessionId());
			if (session == null || session.lastActivity.before(staleDate)) {
				Logger.debug("Yep, let's remove it!");
				em.remove(corpus);
				Logger.debug("Yay!");
			}
		}

		em.getTransaction().commit();
		em.close();
		
		// Delete all orphan sessions as well.
		List<SessionViewModel> sessions = SessionViewModel.find("SELECT s FROM models.SessionViewModel s WHERE s.lastActivity < ?", staleDate)
				.fetch();
		for (SessionViewModel session : sessions) {
			session.delete();
		}
		
		Logger.info("Finished cleaning orphan corpora");
	}
}