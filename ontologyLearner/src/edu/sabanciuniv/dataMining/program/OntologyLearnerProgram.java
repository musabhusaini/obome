package edu.sabanciuniv.dataMining.program;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Iterables;

import net.didion.jwnl.JWNLException;

import edu.sabanciuniv.dataMining.data.factory.text.SqlReviewFactory;
import edu.sabanciuniv.dataMining.data.factory.text.SqlTopNReviewFactory;
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
	
	private static Iterable<String> getClusterHeads(int n) throws SQLException {
		SqlReviewFactory factory = new SqlTopNReviewFactory(n);
		TextDocumentFeaturesClusterer clusterer = new TextDocumentFeaturesClusterer(factory);
		clusterer.setRejectTolerance(0.0);
		clusterer.setMinDataCoverage(0.99);
		FeaturesClusterWorld<LinguisticToken> clusterWorld = clusterer.cluster(1000);
		factory.close();
		
		System.out.print("Pruning... ");
		clusterer.setMinDataCoverage(0.95);
		clusterWorld = clusterer.prune();
		System.out.println("down to " + Iterables.size(clusterWorld.getClusters()) + " clusters now.");

		List<String> clusterHeads = new ArrayList<String>();
		for (FeaturesCluster<LinguisticToken> cluster : clusterWorld.getClusters()) {
			clusterHeads.add(((TextDocumentSummary)cluster.getHead()).getIdentifier().toString());
		}
		
		return clusterHeads;
	}
	
	private static HashMap<String,Long> getFeatureMap(Iterable<String> clusterHeads) {
		// Get features map.
		HashMap<String,Long> features = new HashMap<String,Long>();
		SqlReviewFactory factory = new SqlUuidListReviewFactory(clusterHeads);
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
	
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws JWNLException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
		// Read data.
		HashMap<String,Long> features = OntologyLearnerProgram.getFeatureMap(OntologyLearnerProgram.getClusterHeads(100));
		
		// Sort features by frequency;
		List<Entry<String,Long>> sortedFeatures = new ArrayList<Entry<String,Long>>(features.entrySet());
		Collections.sort(sortedFeatures, Collections.reverseOrder(new EntryValueComparator<String,Long>()));		
		System.out.println(sortedFeatures);
		
		// Delete all existing records so we don't have duplicates.
		Connection sqlConn = DriverManager.getConnection("jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=TripAdvisor;integratedSecurity=true");
		PreparedStatement sqlStmt;
		sqlStmt = sqlConn.prepareStatement("DELETE FROM review_features");
		sqlStmt.executeUpdate();
		
		// Write all words to database.
		System.out.println("Writing to database...");
		sqlStmt = sqlConn.prepareStatement("INSERT INTO review_features([feature], [count]) VALUES(?, ?)");
		for (Entry<String,Long> entry : sortedFeatures) {
			String word = entry.getKey();
//			if (word.length() < 3 || word.matches("^[a-z]+$")) {
//				continue;
//			}
//			
			try {
				sqlStmt.setString(1, word);
				sqlStmt.setLong(2, entry.getValue());
				sqlStmt.executeUpdate();
			} catch(SQLException ex) {
				System.out.println("Problem writing word: \"" + word + "\" to database");
			}
		}
		sqlConn.close();
		System.out.println("Finished.");
	}
}