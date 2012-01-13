package edu.sabanciuniv.dataMining.program;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.data.clustering.text.FeaturesCluster;
import edu.sabanciuniv.dataMining.data.clustering.text.FeaturesClusterWorld;
import edu.sabanciuniv.dataMining.data.clustering.text.TextDocumentFeaturesClusterer;
import edu.sabanciuniv.dataMining.data.factory.text.SqlLimitedReviewFactory;
import edu.sabanciuniv.dataMining.data.factory.text.SqlReviewFactory;
import edu.sabanciuniv.dataMining.data.factory.text.SqlUuidListReviewFactory;
import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions;
import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions.FeatureType;
import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.data.text.TextDocumentSummary;
import edu.sabanciuniv.dataMining.util.EntryValueComparator;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticToken;

/**
 * The main entry point for accessing ontology learner functionality.
 * @author Mus'ab Husaini
 */
public class OntologyLearnerProgram {
	
	private static final String SQL_URL = "jdbc:mysql://localhost/trip_advisor";
	private static final String SQL_USERNAME = "java_user";
	private static final String SQL_PASSWORD = "java_user_pwd";
	private static final String REVIEWS_TABLE = "reviews";
	private static final String FEATURES_TABLE = "features";
	private static final String REVIEW_CLUSTERS_TABLE = "review_clusters";
	private static final String ASPECTS_TABLE = "aspects";
	
	private Connection sqlConnection;
	private Logger logger;
		
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
		clusterer.setMinDataCoverage(0.99);
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

	private HashMap<LinguisticToken,Long> getFeatureMap(SqlReviewFactory factory) {
		HashMap<LinguisticToken,Long> features = new HashMap<>();
		TextDocument doc;
		
		while ((doc = factory.create()) != null) {
			for (LinguisticToken token : doc.getFeatures()) {
				String feature = token.getLemma();

				if (feature.length() < 3 || !feature.matches("^[\\sa-z]+$")) {
					continue;
				}
				
				if (!features.containsKey(token)) {
					features.put(token, new Long(0));
				}
				features.put(token, new Long(features.get(token) + 1));
			}
		}
		
		return features;
	}
	
	private HashMap<LinguisticToken,Long> getFeatureMap(Iterable<UUID> clusterHeads) {
		SqlReviewFactory factory = new SqlUuidListReviewFactory(clusterHeads);
		this.prepareReviewFactory(factory);
		factory.getOptions().setFeatureType(TextDocumentOptions.FeatureType.SMART_NOUNS);
		
		HashMap<LinguisticToken,Long> features = this.getFeatureMap(factory);
		factory.close();
		return features;
	}
	
	private void writeFeaturesToDB(HashMap<LinguisticToken,Long> features) throws SQLException {
		// Sort features by frequency;
		List<Entry<LinguisticToken,Long>> sortedFeatures = new ArrayList<>(features.entrySet());
		Collections.sort(sortedFeatures, Collections.reverseOrder(new EntryValueComparator<LinguisticToken,Long>()));		
		System.out.println(sortedFeatures);
		
		// Delete all existing records so we don't have duplicates.
		PreparedStatement sqlStmt;
		sqlStmt = this.sqlConnection.prepareStatement("TRUNCATE TABLE " + FEATURES_TABLE);
		sqlStmt.executeUpdate();
		
		// Write all words to database.
		System.out.println("Writing to database...");
		sqlStmt = this.sqlConnection.prepareStatement("INSERT INTO " + FEATURES_TABLE + "(feature, count) VALUES(?, ?)");
		for (Entry<LinguisticToken,Long> entry : sortedFeatures) {
			String word = entry.getKey().getLemma();
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
	
	private double evaluateClusteringQuality(SqlReviewFactory testFactory) throws SQLException {
		HashMap<LinguisticToken,Long> testNounsMap = this.getFeatureMap(testFactory);
		
		Set<LinguisticToken> clusterNouns = new HashSet<LinguisticToken>();
		Map<TextDocumentSummary,Integer> clusters = getExistingPseudoClusters();
		for (TextDocumentSummary docSummary : clusters.keySet()) {
			clusterNouns = Sets.newHashSet(Sets.union(clusterNouns, docSummary.getFeatures()));
		}
		
		Set<LinguisticToken> commonNouns = Sets.newHashSet(Sets.intersection(clusterNouns, testNounsMap.keySet()));
		
		double totalMass = 0.0;
		for (long count : testNounsMap.values()) {
			totalMass += count;
		}
		
		double clusterMass = 0.0;
		for (LinguisticToken token : commonNouns) {
			clusterMass += testNounsMap.get(token);
		}
		
		return clusterMass/totalMass;
	}
	
	/**
	 * Creates a new instance of {@link OntologyLearnerProgram}.
	 */
	public OntologyLearnerProgram() {
		this.logger = Logger.getLogger(OntologyLearnerProgram.class.getName());
		
		try {
			this.sqlConnection = DriverManager.getConnection(SQL_URL, SQL_USERNAME, SQL_PASSWORD);
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Could not connect to database.", e);
		}
	}
	
	/**
	 * Retrieve all the UUIDs for existing cluster from the database.
	 * @return Cluster head UUIDs.
	 */
	public Iterable<UUID> retrieveExistingClusterHeads() {
		try {
			Map<UUID,Integer> clusterStubMap = this.retrieveExistingClusterStubs();
			return clusterStubMap.keySet();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to retrieve cluster heads.", e);
		}
		
		return null;
	}
	
	/**
	 * Retrieves a {@link TextDocument} instance for the document for the given UUID in the database. 
	 * @param uuid The UUID of the document.
	 * @param featureType The type of features to create.
	 * @return The {@link TextDocument} instance.
	 */
	public TextDocument retrieveTextDocument(UUID uuid, FeatureType featureType) {
		Iterable<UUID> uuids = ImmutableList.of(uuid);
		SqlReviewFactory factory = new SqlUuidListReviewFactory(uuids);
		factory.getOptions().setFeatureType(featureType);
		this.prepareReviewFactory(factory);
		TextDocument doc = factory.create();
		factory.close();
		return doc;
	}

	/**
	 * Retrieves all existing aspects from the database.
	 * @return All the aspects.
	 */
	public Iterable<String> retrieveExistingAspects() {
		List<String> aspects = new ArrayList<>();
		
		try {
			PreparedStatement sqlStmt = this.sqlConnection.prepareStatement("SELECT DISTINCT aspect FROM " + ASPECTS_TABLE);
			ResultSet rs = sqlStmt.executeQuery();
			while (rs.next()) {
				aspects.add(rs.getString("aspect"));
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to retrieve aspects.", e);
		}
		
		return aspects;
	}

	/**
	 * Retrieves all the keywords for a given aspect from the database.
	 * @param aspect The aspect to look for.
	 * @return A list of keywords.
	 */
	public Iterable<String> retrieveKeywords(String aspect) {
		if (StringUtils.isEmpty(aspect)) {
			throw new IllegalArgumentException("Must provide an aspect to look for.");
		}
		
		List<String> keywords = new ArrayList<>();
		try {
			PreparedStatement sqlStmt = this.sqlConnection.prepareStatement("SELECT DISTINCT keyword FROM " + ASPECTS_TABLE +
					" WHERE aspect=? AND keyword IS NOT NULL");
			sqlStmt.setString(1, aspect);
			ResultSet rs = sqlStmt.executeQuery();
			while (rs.next()) {
				keywords.add(rs.getString("keyword"));
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to retrieve aspects.", e);
		}
		
		return keywords;
	}
	
	/**
	 * Retrieves all the aspects from the database that the given keyword appears in.
	 * @param keyword The keyword to look for.
	 * @return A list of aspects.
	 */
	public Iterable<String> retrieveAspects(String keyword) {
		if (StringUtils.isEmpty(keyword)) {
			throw new IllegalArgumentException("Must provide a keyword to look for.");
		}

		List<String> aspects = new ArrayList<>();
		try {
			PreparedStatement sqlStmt = this.sqlConnection.prepareStatement("SELECT DISTINCT aspect FROM " + ASPECTS_TABLE +
					" WHERE keyword=?");
			sqlStmt.setString(1, keyword);
			ResultSet rs = sqlStmt.executeQuery();
			while (rs.next()) {
				aspects.add(rs.getString("aspect"));
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to retrieve aspects.", e);
		}
		
		return aspects;		
	}
	
	/**
	 * Adds an aspect to the database.
	 * @param aspect The aspect to add.
	 * @return A flag indicating whether the aspect was added or not.
	 */
	public boolean addAspect(String aspect) {
		if (StringUtils.isEmpty(aspect)) {
			throw new IllegalArgumentException("Must provide an aspect to add.");
		}

		try {
			PreparedStatement sqlStmt = this.sqlConnection.prepareStatement("SELECT uuid FROM " + ASPECTS_TABLE +
					" WHERE aspect=?");
			sqlStmt.setString(1, aspect);
			ResultSet rs = sqlStmt.executeQuery();
			if (rs.next()) {
				return false;
			}
			
			sqlStmt = this.sqlConnection.prepareStatement("INSERT INTO " + ASPECTS_TABLE + "(aspect) VALUES(?)");
			sqlStmt.setString(1, aspect);
			sqlStmt.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to add aspect.", e);
			return false;
		}
		
		return true;		
	}
	
	/**
	 * Adds a keyword to the database.
	 * @param aspect The aspect this keyword belongs to.
	 * @param keyword The keyword to add.
	 * @return A flag indicating whether the keyword was added or not.
	 */
	public boolean addKeyword(String aspect, String keyword) {
		if (StringUtils.isEmpty(aspect)) {
			throw new IllegalArgumentException("Must provide an aspect to add to.");
		}
		
		if (StringUtils.isEmpty(keyword)) {
			throw new IllegalArgumentException("Must provide a keyword to add.");
		}

		try {
			PreparedStatement sqlStmt = this.sqlConnection.prepareStatement("SELECT uuid FROM " + ASPECTS_TABLE +
					" WHERE aspect=? AND keyword=?");
			sqlStmt.setString(1, aspect);
			sqlStmt.setString(2, keyword);
			ResultSet rs = sqlStmt.executeQuery();
			if (rs.next()) {
				return false;
			}
			
			sqlStmt = this.sqlConnection.prepareStatement("INSERT INTO " + ASPECTS_TABLE + "(aspect, keyword) VALUES(?,?)");
			sqlStmt.setString(1, aspect);
			sqlStmt.setString(2, keyword);
			sqlStmt.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to add keyword.", e);
			return false;
		}
		
		return true;
	}

	/**
	 * Deletes an aspect from the database.
	 * @param aspect The aspect to delete.
	 * @return A flag indicating whether the aspect was deleted or not.
	 */
	public boolean deleteAspect(String aspect) {
		if (StringUtils.isEmpty(aspect)) {
			throw new IllegalArgumentException("Must provide an aspect to delete.");
		}

		try {
			PreparedStatement sqlStmt = this.sqlConnection.prepareStatement("SELECT uuid FROM " + ASPECTS_TABLE +
					" WHERE aspect=?");
			sqlStmt.setString(1, aspect);
			ResultSet rs = sqlStmt.executeQuery();
			if (!rs.next()) {
				return false;
			}
			
			sqlStmt = this.sqlConnection.prepareStatement("DELETE FROM " + ASPECTS_TABLE + " WHERE aspect=?");
			sqlStmt.setString(1, aspect);
			sqlStmt.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to delete the aspect.", e);
			return false;
		}
		
		return true;		
	}
	
	/**
	 * Deletes a keyword from the database.
	 * @param aspect The aspect this keyword belongs to.
	 * @param keyword The keyword to delete.
	 * @return A flag indicating whether the keyword was deleted or not.
	 */
	public boolean deleteKeyword(String aspect, String keyword) {
		if (StringUtils.isEmpty(aspect)) {
			throw new IllegalArgumentException("Must provide an aspect to add to.");
		}
		
		if (StringUtils.isEmpty(keyword)) {
			throw new IllegalArgumentException("Must provide a keyword to add.");
		}

		try {
			PreparedStatement sqlStmt = this.sqlConnection.prepareStatement("SELECT uuid FROM " + ASPECTS_TABLE +
					" WHERE aspect=? AND keyword=?");
			sqlStmt.setString(1, aspect);
			sqlStmt.setString(2, keyword);
			ResultSet rs = sqlStmt.executeQuery();
			if (!rs.next()) {
				return false;
			}
			
			sqlStmt = this.sqlConnection.prepareStatement("DELETE FROM " + ASPECTS_TABLE + " WHERE aspect=? AND keyword=?");
			sqlStmt.setString(1, aspect);
			sqlStmt.setString(2, keyword);
			sqlStmt.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to delete keyword.", e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Updates an existing aspect value in the database.
	 * @param aspect The aspect to update.
	 * @param newAspect The new value of the aspect.
	 * @return A flag indicating whether the aspect was updated or not.
	 */
	public boolean updateAspect(String aspect, String newAspect) {
		if (StringUtils.isEmpty(aspect) || StringUtils.isEmpty(newAspect)) {
			throw new IllegalArgumentException("Must provide valid aspect values.");
		}

		try {
			// Make sure the old aspect exists.
			PreparedStatement sqlStmt = this.sqlConnection.prepareStatement("SELECT uuid FROM " + ASPECTS_TABLE +
					" WHERE aspect=?");
			sqlStmt.setString(1, aspect);
			ResultSet rs = sqlStmt.executeQuery();
			if (!rs.next()) {
				return false;
			}

			// Make sure the new aspect does not exist.
			sqlStmt = this.sqlConnection.prepareStatement("SELECT uuid FROM " + ASPECTS_TABLE +
					" WHERE aspect=?");
			sqlStmt.setString(1, newAspect);
			rs = sqlStmt.executeQuery();
			if (rs.next()) {
				return false;
			}
			
			sqlStmt = this.sqlConnection.prepareStatement("UPDATE " + ASPECTS_TABLE + " SET aspect=? WHERE aspect=?");
			sqlStmt.setString(1, newAspect);
			sqlStmt.setString(2, aspect);
			sqlStmt.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to update aspect.", e);
			return false;
		}
		
		return true;
	}

	/**
	 * Updates an existing keyword value in the database.
	 * @param aspect The aspect the keyword belongs to.
	 * @param keyword The keyword to update.
	 * @param newAspect The new value of the keyword.
	 * @return A flag indicating whether the keyword was updated or not.
	 */
	public boolean updateKeyword(String aspect, String keyword, String newKeyword) {
		if (StringUtils.isEmpty(aspect)) {
			throw new IllegalArgumentException("Must provide an aspect to add to.");
		}
		
		if (StringUtils.isEmpty(keyword)) {
			throw new IllegalArgumentException("Must provide a keyword to add.");
		}

		try {
			// Make sure the old keyword exists.
			PreparedStatement sqlStmt = this.sqlConnection.prepareStatement("SELECT uuid FROM " + ASPECTS_TABLE +
					" WHERE aspect=? AND keyword=?");
			sqlStmt.setString(1, aspect);
			sqlStmt.setString(2, keyword);
			ResultSet rs = sqlStmt.executeQuery();
			if (!rs.next()) {
				return false;
			}
			
			// Make sure the new keyword does not exist.
			sqlStmt = this.sqlConnection.prepareStatement("SELECT uuid FROM " + ASPECTS_TABLE +
					" WHERE aspect=? AND keyword=?");
			sqlStmt.setString(1, aspect);
			sqlStmt.setString(2, newKeyword);
			rs = sqlStmt.executeQuery();
			if (rs.next()) {
				return false;
			}
			
			sqlStmt = this.sqlConnection.prepareStatement("UPDATE " + ASPECTS_TABLE + " SET keyword=? WHERE aspect=? AND keyword=?");
			sqlStmt.setString(1, newKeyword);
			sqlStmt.setString(2, aspect);
			sqlStmt.setString(3, keyword);
			sqlStmt.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to update keyword.", e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Closes all connections and releases resources used by this instance.
	 */
	public void close() {
		try {
			this.sqlConnection.close();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Connection already closed or could not be closed.", e);
		}
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
			OntologyLearnerProgram program = new OntologyLearnerProgram();
			
			// Read data.
			Iterable<UUID> clusterHeads;
			if (discoverNewClusters) {
				clusterHeads = program.discoverClusterHeads(offset, count, increment);
			} else {
				clusterHeads = program.retrieveExistingClusterHeads();
			}
			
			if (extractFeatures) {
				HashMap<LinguisticToken,Long> features = program.getFeatureMap(clusterHeads);
				program.writeFeaturesToDB(features);
			}
			
			if (evaluateQuality) {
				SqlReviewFactory testFactory = new SqlLimitedReviewFactory(offset, count);
				program.prepareReviewFactory(testFactory);
				System.out.println("Cluster quality = " + program.evaluateClusteringQuality(testFactory) * 100 + "%");
			}
			
			program.close();
		} catch(SQLException ex) {
			Logger.getLogger(OntologyLearnerProgram.class.getName()).log(Level.SEVERE, "Error talking to database.", ex);
		}
	}
}