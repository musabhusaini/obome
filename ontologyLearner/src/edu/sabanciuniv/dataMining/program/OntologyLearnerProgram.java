package edu.sabanciuniv.dataMining.program;

import java.util.HashMap;
import java.util.Map;
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
import edu.sabanciuniv.dataMining.experiment.models.Review;
import edu.sabanciuniv.dataMining.experiment.models.factory.ReviewTaggedContentFactory;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.experiment.models.setcover.builder.GreedySetCoverBuilder;
import edu.sabanciuniv.dataMining.util.LargeTypedQuery;

/**
 * The main entry point for accessing ontology learner functionality.
 * @author Mus'ab Husaini
 */
public class OntologyLearnerProgram {
	
//	private HashMap<LinguisticToken,Long> getFeatureMap(SqlReviewFactory factory) {
//		HashMap<LinguisticToken,Long> features = new HashMap<>();
//		TextDocument doc;
//		
//		while ((doc = factory.create()) != null) {
//			for (LinguisticToken token : doc.getFeatures()) {
//				String feature = token.getLemma();
//
//				if (feature.length() < 3 || !feature.matches("^[\\sa-z]+$")) {
//					continue;
//				}
//				
//				if (!features.containsKey(token)) {
//					features.put(token, new Long(0));
//				}
//				features.put(token, new Long(features.get(token) + 1));
//			}
//		}
//		
//		return features;
//	}
//	
//	private HashMap<LinguisticToken,Long> getFeatureMap(Iterable<UUID> clusterHeads) {
//		SqlReviewFactory factory = new SqlUuidListReviewFactory(clusterHeads);
//		this.prepareReviewFactory(factory);
//		factory.getOptions().setFeatureType(TextDocumentOptions.FeatureType.SMART_NOUNS);
//		
//		HashMap<LinguisticToken,Long> features = this.getFeatureMap(factory);
//		factory.close();
//		return features;
//	}
//	
//	private void writeFeaturesToDB(HashMap<LinguisticToken,Long> features) throws SQLException {
//		// Sort features by frequency;
//		List<Entry<LinguisticToken,Long>> sortedFeatures = new ArrayList<>(features.entrySet());
//		Collections.sort(sortedFeatures, Collections.reverseOrder(new EntryValueComparator<LinguisticToken,Long>()));		
//		System.out.println(sortedFeatures);
//		
//		// Delete all existing records so we don't have duplicates.
//		PreparedStatement sqlStmt;
//		sqlStmt = this.sqlConnection.prepareStatement("TRUNCATE TABLE " + FEATURES_TABLE);
//		sqlStmt.executeUpdate();
//		
//		// Write all words to database.
//		System.out.println("Writing to database...");
//		sqlStmt = this.sqlConnection.prepareStatement("INSERT INTO " + FEATURES_TABLE + "(feature, count) VALUES(?, ?)");
//		for (Entry<LinguisticToken,Long> entry : sortedFeatures) {
//			String word = entry.getKey().getLemma();
//			try {
//				sqlStmt.setString(1, word);
//				sqlStmt.setLong(2, entry.getValue());
//				sqlStmt.executeUpdate();
//			} catch(SQLException ex) {
//				System.err.println("Problem writing word: \"" + word + "\" to database");
//				throw ex;
//			}
//		}
//		
//		System.out.println("Finished.");
//	}
//	
//	private double evaluateClusteringQuality(SqlReviewFactory testFactory) throws SQLException {
//		HashMap<LinguisticToken,Long> testNounsMap = this.getFeatureMap(testFactory);
//		
//		Set<LinguisticToken> clusterNouns = new HashSet<LinguisticToken>();
//		//Map<TextDocumentSummary,Integer> clusters = getExistingPseudoClusters();
//		SetCover setCover = null; // SetCover.createFromSql("Clustering-99", this.sqlConnection);
////		setCover.populateReviewsFromSql(this.sqlConnection);
//		//for (TextDocumentSummary docSummary : clusters.keySet()) {
//		for (SetCoverReview scReview : setCover.getReviews()) {
//			//clusterNouns = Sets.newHashSet(Sets.union(clusterNouns, docSummary.getFeatures()));
//			//clusterNouns = Sets.newHashSet(Sets.union(clusterNouns, scReview.getReview(this.sqlConnection).getTaggedContent().getFeatures()));
//		}
//		
//		Set<LinguisticToken> commonNouns = Sets.newHashSet(Sets.intersection(clusterNouns, testNounsMap.keySet()));
//		
//		double totalMass = 0.0;
//		for (long count : testNounsMap.values()) {
//			totalMass += count;
//		}
//		
//		double clusterMass = 0.0;
//		for (LinguisticToken token : commonNouns) {
//			clusterMass += testNounsMap.get(token);
//		}
//		
//		return clusterMass/totalMass;
//	}
		
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
			EntityManagerFactory emFactory = Persistence.createEntityManagerFactory("edu.sabanciuniv.dataMining.experiment");
			EntityManager em = emFactory.createEntityManager();
			em.getTransaction().begin();

			GreedySetCoverBuilder greedyBuilder = new GreedySetCoverBuilder(em);
//			ClustererSetCoverBuilder clustererBuilder = new ClustererSetCoverBuilder(em);
			
			LargeTypedQuery<Review> query = new LargeTypedQuery<>(em.createQuery("SELECT p FROM Review p", Review.class).setFirstResult(offset).setMaxResults(count),
					increment);
			ReviewTaggedContentFactory factory = new ReviewTaggedContentFactory(new QueryBasedObjectFactory<>(query));
			TextDocument document;
			while ((document = factory.create()) != null) {
				greedyBuilder.seeUniverseExample(document);
//				clustererBuilder.seeUniverseExample(document);
			}

			SetCover setCover;
			
			setCover = greedyBuilder.buildRandom("garbage");
			setCover.setCoverOffset(offset);
			setCover.setCoverSize(count);
			em.persist(setCover);
			setCover = null;

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
			
			em.getTransaction().commit();
			em.close();
			emFactory.close();
		} catch(Exception ex) {
			Logger.getLogger(OntologyLearnerProgram.class.getName()).log(Level.SEVERE, "Error talking to database.", ex);
		}
	}
}