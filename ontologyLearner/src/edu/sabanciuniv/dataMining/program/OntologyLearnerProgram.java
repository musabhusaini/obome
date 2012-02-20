package edu.sabanciuniv.dataMining.program;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.google.common.base.Joiner;

import edu.sabanciuniv.dataMining.data.factory.QueryBasedObjectFactory;
import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.experiment.models.OpinionDocument;
import edu.sabanciuniv.dataMining.experiment.models.factory.ReviewTaggedContentFactory;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.experiment.models.setcover.builder.GreedySetCoverBuilder;
import edu.sabanciuniv.dataMining.util.LargeTypedQuery;

/**
 * The main entry point for accessing ontology learner functionality.
 * @author Mus'ab Husaini
 */
public class OntologyLearnerProgram {

	private static EntityManagerFactory emFactory;
	
	public static EntityManager em() {
		if (emFactory == null) {
			emFactory = Persistence.createEntityManagerFactory("edu.sabanciuniv.dataMining.experiment");
		}
		
		return emFactory.createEntityManager();
	}
		
	/**
	 * Runs the ontology learner program.
	 * @param args Arguments to run the program with.
	 */
	public static void main(String[] args) {
		
		// Parse arguments.
		Map<String,String> argMap = new HashMap<>();
		String argString = Joiner.on(" ").join(args);
		argString = Pattern.compile("\\s*=\\s*").matcher(argString).replaceAll("=");
		StringTokenizer tokenizer = new StringTokenizer(argString);
		Pattern pattern = Pattern.compile("^([a-zA-Z_]+\\w*)=(\\w+)$");
		boolean error = false;
		
		while (tokenizer.hasMoreTokens()) {
			Matcher matcher = pattern.matcher(tokenizer.nextToken());
			if (!matcher.matches()) {
				error = true;
				break;
			}
			argMap.put(matcher.group(1), matcher.group(2));
		}
		
		if (error) {
			System.err.println("Please provide valid name/value pairs.");
			System.exit(0);
		}
		
		int offset = 0;
		int count = 5000;
		int increment = 1000;
		boolean discoverNewClusters = true;
		boolean extractFeatures = true;
		boolean evaluateQuality = true;
		
		try {
			if (argMap.containsKey("offset")) {
				offset = Integer.parseInt(argMap.get("offset"));
			}
			
			if (argMap.containsKey("count")) {
				count = Integer.parseInt(argMap.get("count"));
			}
	
			if (argMap.containsKey("increment")) {
				increment = Integer.parseInt(argMap.get("increment"));
			}

			if (argMap.containsKey("extractFeatures")) {
				discoverNewClusters = Boolean.parseBoolean(argMap.get("discoverNewClusters"));
			}
			
			if (argMap.containsKey("extractFeatures")) {
				extractFeatures = Boolean.parseBoolean(argMap.get("extractFeatures"));
			}
			
			if (argMap.containsKey("evaluateQuality")) {
				evaluateQuality = Boolean.parseBoolean(argMap.get("evaluateQuality"));
			}
		} catch (NumberFormatException ex) {
			System.err.println("Could not understand the numbers.");
			System.exit(0);
		}

		try {
			EntityManager em = em();
			em.getTransaction().begin();
			
			Scanner scanner = new Scanner(new FileInputStream("C:\\Users\\SUUSER\\Dropbox\\Projects\\Eclipse Projects\\" +
					"ontologyLearner\\LBH.txt"));
			while (scanner.hasNextLine()) {
				OpinionDocument document = new OpinionDocument();
				document.setContent(scanner.nextLine());
				document.setCorpusName("LBH Survey");
				em.persist(document);
				em.flush();
			}
			
			em.getTransaction().commit();
			
//			GreedySetCoverBuilder greedyBuilder = new GreedySetCoverBuilder(em);
////			ClustererSetCoverBuilder clustererBuilder = new ClustererSetCoverBuilder(em);
//			
//			LargeTypedQuery<OpinionDocument> query = new LargeTypedQuery<>(em.createQuery("SELECT p FROM Review p", OpinionDocument.class).setFirstResult(offset).setMaxResults(count),
//					increment);
//			ReviewTaggedContentFactory factory = new ReviewTaggedContentFactory(new QueryBasedObjectFactory<>(query));
//			TextDocument document;
//			while ((document = factory.create()) != null) {
//				greedyBuilder.seeUniverseExample(document);
////				clustererBuilder.seeUniverseExample(document);
//			}
//
//			SetCover setCover;
//			
//			setCover = greedyBuilder.buildRandom("garbage");
//			setCover.setCoverOffset(offset);
//			setCover.setCoverSize(count);
//			em.persist(setCover);
//			setCover = null;

//			setCover = greedyBuilder.build("Greedy-small");
//			setCover.setCoverOffset(offset);
//			setCover.setCoverSize(count);
//			setCover = null;
//			greedyBuilder = null;
//
//			int dc;
//			for (dc=100; dc>80; dc-=1) {
//				clustererBuilder.setMinDataCoverage(dc/100.0);
//				setCover = clustererBuilder.build("Clustering-small-" + dc);
//				setCover.setCoverOffset(offset);
//				setCover.setCoverSize(count);
//				setCover = null;
//			}
//
//			for (; dc>0; dc-=10) {
//				clustererBuilder.setMinDataCoverage(dc/100.0);
//				setCover = clustererBuilder.build("Clustering-small-" + dc);
//				setCover.setCoverOffset(offset);
//				setCover.setCoverSize(count);
//				setCover = null;
//			}
						
//			// Read data.
//			Iterable<UUID> clusterHeads;
//			if (discoverNewClusters) {
//				clusterHeads = program.discoverClusterHeads(offset, count, increment);
//			} else {
//				clusterHeads = program.retrieveExistingClusterHeads();
//			}
//			
//			if (evaluateQuality) {
//				SqlReviewFactory testFactory = new SqlLimitedReviewFactory(offset, count);
//				program.prepareReviewFactory(testFactory);
//				System.out.println("Cluster quality = " + program.evaluateClusteringQuality(testFactory) * 100 + "%");
//			}
//
//			if (extractFeatures) {
//				HashMap<LinguisticToken,Long> features = program.getFeatureMap(clusterHeads);
//				program.writeFeaturesToDB(features);
//			}
		} catch(Exception ex) {
			Logger.getLogger(OntologyLearnerProgram.class.getName()).log(Level.SEVERE, "Error talking to database.", ex);
		}
	}
}