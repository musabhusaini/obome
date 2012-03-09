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
import play.jobs.Job;

public class OpinionCollectionDistillerAnalyzer extends Job<Map<Double, Double>> {
	
	private static Map<String, OpinionCollectionDistillerAnalyzer> registry = Maps.newHashMap();
	
	public static OpinionCollectionDistillerAnalyzer get(String uuid) {
		if (registry.containsKey(uuid)) {
			return registry.get(uuid);
		}
		return null;
	}
	
	private SetCover setCover;
	private double progress;
	
	public OpinionCollectionDistillerAnalyzer(SetCover setCover) {
		this.setCover = setCover;
		
		registry.put(setCover.getIdentifier().toString(), this);
	}

	public double getProgress() {
		return this.progress;
	}
	
	@Override
	public Map<Double, Double> doJobWithResult() {
		Map<Double, Double> map = Maps.newHashMap();
		
		EntityManager em = OntologyLearnerProgram.em();
		em.getTransaction().begin();
		
		this.setCover = em.find(SetCover.class, this.setCover.getId());
		
		long corpusSize = em.createQuery("SELECT COUNT(d) FROM OpinionDocument d WHERE d.corpus=:corpus", Long.class)
				.setParameter("corpus", this.setCover.getCorpus())
				.getSingleResult();
		
		List<SetCoverItem> items = Lists.newArrayList(this.setCover.getItems());
		Collections.sort(items, Collections.reverseOrder());
		
		long totalUtility = 0L;
		for (SetCoverItem item : items) {
			totalUtility += item.getUtilityScore();
		}
		
		long cumulativeUtlity = 0L;
		double step = 0.025;
		
		int index=0;
		for (double threshold = 1.0; threshold>0 && index<items.size(); threshold-=step) {
			for (; index<items.size(); index++) {
				double reduction = (1 - ((index + 1)/(double)corpusSize));
				double coverage = cumulativeUtlity / (double)totalUtility;
				if (coverage >= 1 - threshold) {
					map.put(threshold * 100, reduction * 100);
					break;
				}
				
				cumulativeUtlity += items.get(index).getUtilityScore();
			}
		}
		
		if (!map.containsKey(0.0 * 100)) {
			map.put(0.0 * 100, (1 - items.size()/(double)corpusSize) * 100);
		}
		
		em.getTransaction().commit();
		em.close();
		
		registry.remove(this.setCover.getIdentifier().toString());
		return map;
	}
}