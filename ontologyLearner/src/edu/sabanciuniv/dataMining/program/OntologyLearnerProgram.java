package edu.sabanciuniv.dataMining.program;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import net.didion.jwnl.JWNLException;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.data.factory.text.SqlLimitedReviewFactory;
import edu.sabanciuniv.dataMining.data.factory.text.SqlReviewFactory;
import edu.sabanciuniv.dataMining.data.factory.text.SqlUuidListReviewFactory;
import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions;
import edu.sabanciuniv.dataMining.data.text.TextDocumentSummary;
import edu.sabanciuniv.dataMining.data.clustering.text.FeaturesCluster;
import edu.sabanciuniv.dataMining.data.clustering.text.FeaturesClusterWorld;
import edu.sabanciuniv.dataMining.data.clustering.text.TextDocumentFeaturesClusterer;
import edu.sabanciuniv.dataMining.util.EntryValueComparator;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticToken;

public class OntologyLearnerProgram {
	
	private static final String SQL_URL = "jdbc:mysql://localhost/trip_advisor";
	private static final String SQL_USERNAME = "java_user";
	private static final String SQL_PASSWORD = "java_user_pwd";
	private static final String REVIEWS_TABLE = "reviews";
	private static final String FEATURES_TABLE = "features";
	private static final String REVIEW_CLUSTERS_TABLE = "review_clusters";
	
	private Connection sqlConnection;
		
	private SqlReviewFactory prepareReviewFactory(SqlReviewFactory factory) {
		factory.setSqlUrl(SQL_URL);
		factory.setSqlUserName(SQL_USERNAME);
		factory.setSqlPassword(SQL_PASSWORD);
		factory.setTableName(REVIEWS_TABLE);
		factory.initialize();
		return factory;
	}

	private Map<UUID,Integer> retrieveExistingClusterStubs() throws SQLException {
		Map<UUID,Integer> clusters = new HashMap<>();
		
		PreparedStatement selectClustersStmt =
				this.sqlConnection.prepareStatement("SELECT cluster_head_uuid AS uuid, member_count AS count FROM " + REVIEW_CLUSTERS_TABLE);
		ResultSet clustersRs = selectClustersStmt.executeQuery();
		
		while (clustersRs.next()) {
			clusters.put(IdentifiableObject.createUuid(clustersRs.getBytes("uuid")), clustersRs.getInt("count"));
		}
		
		return clusters;
	}
	
	private Map<TextDocumentSummary,Integer> getExistingPseudoClusters() throws SQLException {
		return this.retrievePseudoClusters(this.retrieveExistingClusterStubs());
	}
	
	private Iterable<FeaturesCluster<LinguisticToken>> recreateClusters(Map<TextDocumentSummary,Integer> existingClusterMap)
			throws SQLException {
		List<FeaturesCluster<LinguisticToken>> clusters = new ArrayList<>();
		
		for (TextDocumentSummary doc : existingClusterMap.keySet()) {
			clusters.add(this.recreateCluster(doc, existingClusterMap.get(doc)));
		}
		
		return clusters;
	}
	
	private FeaturesCluster<LinguisticToken> recreateCluster(TextDocumentSummary doc, int count) {
		FeaturesCluster<LinguisticToken> cluster = new FeaturesCluster<>(doc);
		cluster.addDummyMembers(count-1);
		return cluster;
	}
	
	private Map<TextDocumentSummary,Integer> retrievePseudoClusters(Map<UUID,Integer> existingClusterMap)
			throws SQLException {

		Map<TextDocumentSummary,Integer> clusters = new HashMap<>();
		
		SqlReviewFactory factory = new SqlUuidListReviewFactory(existingClusterMap.keySet());
		this.prepareReviewFactory(factory);

		TextDocument doc;
		while ((doc = factory.create()) != null) {
			clusters.put(doc.getSummary(), existingClusterMap.get(doc.getIdentifier()));
		}
		
		factory.close();
		return clusters;
	}
	
	private Map<TextDocumentSummary,Integer> discoverPsuedoClusters(int offset, int count, Map<TextDocumentSummary,Integer> existingPseudoCluster)
			throws SQLException {
		SqlReviewFactory factory = new SqlLimitedReviewFactory(offset, count);
		this.prepareReviewFactory(factory);
		TextDocumentFeaturesClusterer clusterer = new TextDocumentFeaturesClusterer(factory);
		clusterer.setRejectTolerance(0.0);
		clusterer.setMinDataCoverage(0.95);
		clusterer.setInitialClusters(this.recreateClusters(existingPseudoCluster));
		FeaturesClusterWorld<LinguisticToken> clusterWorld = clusterer.cluster();
		factory.close();
		
		if (factory.getCount() == 0) {
			return existingPseudoCluster;
		}
		
		System.out.print("Pruning... ");
		clusterWorld = clusterer.prune();
		System.out.println("down to " + Iterables.size(clusterWorld.getClusters()) + " clusters now.");

		existingPseudoCluster = new HashMap<>();
		for (FeaturesCluster<LinguisticToken> cluster : clusterWorld.getClusters()) {
			existingPseudoCluster.put((TextDocumentSummary)cluster.getHead(), cluster.getMemberCount());
		}
		
		return existingPseudoCluster;
	}

	private Iterable<UUID> discoverClusterHeads(int offset, int count, int increment) throws SQLException {
		Map<TextDocumentSummary,Integer> currentClusters = this.getExistingPseudoClusters();
		Map<TextDocumentSummary,Integer> newClusters = null;
		
		increment = Math.min(count, increment);
		while ((newClusters = this.discoverPsuedoClusters(offset, increment, currentClusters)) != currentClusters) {
			offset += increment;
			count -= increment;
			increment = Math.min(count, increment);
			currentClusters = newClusters;
			
			if (count <= 0) {
				break;
			}
		}

		PreparedStatement insertStmt = this.sqlConnection.prepareStatement("INSERT INTO " + REVIEW_CLUSTERS_TABLE + "(cluster_head_uuid, member_count) " +
				"VALUES(?,?)");
		PreparedStatement truncateStmt = this.sqlConnection.prepareStatement("TRUNCATE TABLE review_clusters");
		truncateStmt.executeUpdate();
		
		List<UUID> clusters = new ArrayList<>();
		for (TextDocumentSummary doc : currentClusters.keySet()) {
			insertStmt.setBytes(1, IdentifiableObject.getUuidBytes(doc.getIdentifier()));
			insertStmt.setInt(2, currentClusters.get(doc));
			insertStmt.executeUpdate();
			
			clusters.add(doc.getIdentifier());
		}
		
		return clusters;
	}
		
	private HashMap<String,Long> getFeatureMap(Iterable<UUID> clusterHeads) {
		// Get features map.
		HashMap<String,Long> features = new HashMap<String,Long>();
		SqlReviewFactory factory = new SqlUuidListReviewFactory(clusterHeads);
		this.prepareReviewFactory(factory);
		factory.getOptions().setFeatureType(TextDocumentOptions.FeatureType.SMART_NOUNS);
		TextDocument doc;
		while ((doc = factory.create()) != null) {
			for (LinguisticToken token : doc.getFeatures()) {
				String feature = token.getLemma();

				if (feature.length() < 3 || !feature.matches("^[\\sa-z]+$")) {
					continue;
				}
				
				if (!features.containsKey(feature)) {
					features.put(feature, new Long(0));
				}
				features.put(feature, new Long(features.get(feature) + 1));
			}
		}
		
		factory.close();
		return features;
	}
	
	private void close() {
		try {
			this.sqlConnection.close();
		} catch (SQLException e) {
			Logger.getLogger(OntologyLearnerProgram.class.getName()).log(Level.WARNING, "Connection already closed or could not be closed.", e);
		}
	}

	private void writeFeaturesToDB(HashMap<String,Long> features) throws SQLException {
		// Sort features by frequency;
		List<Entry<String,Long>> sortedFeatures = new ArrayList<Entry<String,Long>>(features.entrySet());
		Collections.sort(sortedFeatures, Collections.reverseOrder(new EntryValueComparator<String,Long>()));		
		System.out.println(sortedFeatures);
		
		// Delete all existing records so we don't have duplicates.
		PreparedStatement sqlStmt;
		sqlStmt = this.sqlConnection.prepareStatement("TRUNCATE TABLE " + FEATURES_TABLE);
		sqlStmt.executeUpdate();
		
		// Write all words to database.
		System.out.println("Writing to database...");
		sqlStmt = this.sqlConnection.prepareStatement("INSERT INTO " + FEATURES_TABLE + "(feature, count) VALUES(?, ?)");
		for (Entry<String,Long> entry : sortedFeatures) {
			String word = entry.getKey();
			try {
				sqlStmt.setString(1, word);
				sqlStmt.setLong(2, entry.getValue());
				sqlStmt.executeUpdate();
			} catch(SQLException ex) {
				System.err.println("Problem writing word: \"" + word + "\" to database");
				throw ex;
			}
		}
		
		System.out.println("Finished.");
	}
	
	public OntologyLearnerProgram() {
		try {
			this.sqlConnection = DriverManager.getConnection(SQL_URL, SQL_USERNAME, SQL_PASSWORD);
		} catch (SQLException e) {
			Logger.getLogger(OntologyLearnerProgram.class.getName()).log(Level.SEVERE, "Could not connect to database.", e);
		}
	}
	
	public Iterable<UUID> getExistingClusterHeads() {
		try {
			Map<UUID,Integer> clusterStubMap = this.retrieveExistingClusterStubs();
			return clusterStubMap.keySet();
		} catch (SQLException e) {
			Logger.getLogger(OntologyLearnerProgram.class.getName()).log(Level.SEVERE, "Failed to retrieve cluster heads.", e);
		}
		
		return null;
	}
	
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws JWNLException 
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
		boolean extractFeatures = true;
		
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
				extractFeatures = Boolean.getBoolean(argMap.get("extractFeatures"));
			}
		} catch (NumberFormatException ex) {
			System.err.println("Could not understand the numbers.");
			System.exit(0);
		}

		try {
			OntologyLearnerProgram program = new OntologyLearnerProgram();
			
			// Read data.
			Iterable<UUID> clusterHeads = program.discoverClusterHeads(offset, count, increment);
			
			if (extractFeatures) {
				HashMap<String,Long> features = program.getFeatureMap(clusterHeads);
				program.writeFeaturesToDB(features);
			}
			
			program.close();
		} catch(SQLException ex) {
			Logger.getLogger(OntologyLearnerProgram.class.getName()).log(Level.SEVERE, "Error talking to database.", ex);
		}
	}
}