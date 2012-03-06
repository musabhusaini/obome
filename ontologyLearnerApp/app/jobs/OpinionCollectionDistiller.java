package jobs;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCoverItem;
import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;
import play.db.jpa.JPA;
import play.jobs.Job;

public class OpinionCollectionDistiller extends Job<SetCover> {
	
	private static Map<String, OpinionCollectionDistiller> registry = Maps.newHashMap();
	
	public static OpinionCollectionDistiller get(String uuid) {
		if (registry.containsKey(uuid)) {
			return registry.get(uuid);
		}
		return null;
	}
	
	private SetCover setCover;
	private double threshold;
	private double progress;
	
	public OpinionCollectionDistiller(SetCover setCover) {
		this(setCover, 0);
	}
	
	public OpinionCollectionDistiller(SetCover setCover, double threshold) {
		this.setCover = setCover;
		this.threshold = threshold;
		
		registry.put(setCover.getIdentifier().toString(), this);
	}

	public double getProgress() {
		return this.progress;
	}
	
	@Override
	public SetCover doJobWithResult() {
		EntityManager em = OntologyLearnerProgram.em();
		em.getTransaction().begin();
		
		this.setCover = em.find(SetCover.class, this.setCover.getId());
		this.setCover.setErrorTolerance(this.threshold);
		this.setCover = em.merge(this.setCover);
		
		em.flush();
		
		List<SetCoverItem> items = em.createQuery("SELECT item FROM SetCoverItem item WHERE item.setCover=:sc", SetCoverItem.class)
				.setParameter("sc", this.setCover)
				.getResultList();
		
		Collections.sort(items, Collections.reverseOrder());
		
		long totalUtility = 0L;
		for (SetCoverItem item : items) {
			totalUtility += item.getUtilityScore();
		}
		
		long cumulativeUtlity = 0L;
		int index=0;
		
		for (index=0; index<items.size(); index++) {
			double coverage = cumulativeUtlity / (double)totalUtility;
			System.out.println("Cumut: " + cumulativeUtlity + ", threshold: " + threshold * 100);
			
			if (coverage >= 1 - threshold) {
				break;
			}

			this.progress = (index + 1) / (double)items.size();
			cumulativeUtlity += items.get(index).getUtilityScore();
		}
		
		System.out.println("Done at index " + index);
		
		for (; index<items.size(); index++) {
			SetCoverItem item = items.get(index);
			
			System.out.println("Excluding utility: " + item.getUtilityScore());
			
			em.remove(item);
			
			em.flush();
			
			this.progress = (index + 1) / (double)items.size();
		}
		
		em.getTransaction().commit();
		em.close();
		
		registry.remove(this.setCover.getIdentifier().toString());
		
		return this.setCover;
	}
}