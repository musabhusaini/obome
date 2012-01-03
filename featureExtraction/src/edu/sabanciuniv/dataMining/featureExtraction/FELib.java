/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.sabanciuniv.dataMining.featureExtraction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.sabanciuniv.dataMining.data.clustering.text.FeaturesCluster;
import edu.sabanciuniv.dataMining.data.clustering.text.FeaturesClusterWorld;
import edu.sabanciuniv.dataMining.data.clustering.text.TextDocumentFeaturesClusterer;
import edu.sabanciuniv.dataMining.data.factory.text.SqlLimitedReviewFactory;
import edu.sabanciuniv.dataMining.data.factory.text.SqlReviewFactory;
import edu.sabanciuniv.dataMining.data.factory.text.SqlUuidListReviewFactory;
import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.data.text.TextDocumentSummary;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticToken;

/**
 * 
 * @author bgulcu
 */
public class FELib {
	public static final String DB_NAME = "trip_advisor";
	public static final String REVIEWS_TABLE_NAME = "reviews";
	public static final String FEATURES_TABLE_NAME = "aspects";

	private String sqlUrl;
	private String sqlUserName;
	private String sqlPassword;
	private Connection sqlConnection;
	private Statement selectStatement;
	private Statement insertStatement;
	private SqlReviewFactory clusterHeadFactory;
	
	public FELib(String jdriver, String url, String userName, String password)
		throws ClassNotFoundException, SQLException {
		
		Class.forName(jdriver);
		this.sqlUrl = url;
		this.sqlUserName = userName;
		this.sqlPassword = password;
		this.sqlConnection = DriverManager.getConnection(url, userName, password);
		this.insertStatement = this.sqlConnection.createStatement();
		this.selectStatement = this.sqlConnection.createStatement();
	}
	
	public void initialize() {
		SqlLimitedReviewFactory factory = new SqlLimitedReviewFactory(0, 1000);
		factory.setSqlUrl(this.sqlUrl);
		factory.setSqlUserName(this.sqlUserName);
		factory.setSqlPassword(this.sqlPassword);
		factory.initialize();

		TextDocumentFeaturesClusterer clusterer = new TextDocumentFeaturesClusterer(factory);
		clusterer.setRejectTolerance(0.0);
		clusterer.setMinDataCoverage(0.99);
		FeaturesClusterWorld<LinguisticToken> clusterWorld = clusterer.cluster(1000);
		factory.close();

		clusterer.setMinDataCoverage(0.95);
		clusterWorld = clusterer.prune();
		
		List<UUID> clusterHeads = new ArrayList<UUID>();
		for (FeaturesCluster<LinguisticToken> cluster : clusterWorld.getClusters()) {
			clusterHeads.add(((TextDocumentSummary)cluster.getHead()).getIdentifier());
		}
		
		this.clusterHeadFactory = new SqlUuidListReviewFactory(clusterHeads);
		this.clusterHeadFactory.setSqlUrl(this.sqlUrl);
		this.clusterHeadFactory.setSqlUserName(this.sqlUserName);
		this.clusterHeadFactory.setSqlPassword(this.sqlPassword);
		this.clusterHeadFactory.initialize();
	}

	public int get_sentiment_score(String review_content) {
		return 1;
	}

	// TODO keyword ve aspect geçmiyorsa çıkma ihtimalini artır
	// TODO bir önceki review'da kullanılan keyword'lerin olduğu review'ları
	// sona at
	// TODO next'e basıldıktan sonra is_evaluated = 1 olsun.
	public String get_next_sample() { //(Statement stmt, Statement insert_stmt)
		if (this.clusterHeadFactory == null) {
			this.initialize();
		}

		TextDocument doc = this.clusterHeadFactory.create();
		if (doc != null) {
			return doc.getText();
		}
		
		return "";
	}

//	public void mark_evaluation_flag(Statement insert_stmt, int review_id)
//			throws SQLException {
//		insert_stmt
//				.execute("update tripadvisor.review_1000 set is_evaluated = 1 where review_id = "
//						+ review_id);
//	}

//	public void add_aspect(Statement stmt, String aspect) {
//
//	}
//
//	public void add_keywords(Statement stmt, int aspect_id,
//			String[] keyword_list) {
//
//	}

	public void get_input() {
		System.out.println("#########");
		System.out.println("# n: next");
		System.out.println("# q: quit");
		System.out.println("# a: aspect list");
		System.out.println("# f: aspect:feature list");
		System.out.println("# aspect;keyword1 keyword2 keyword3");
		System.out.println("#########");
		// return raw_input("> ");
	}

	public void get_plain_input() {
		// return raw_input("> ");
	}

	// TODO: fix user message. Should output
	// "xxx aspect has been added"/"xxx aspect has been updated" and similar
	// with keywords
	public void parse_input(String user_input)
			throws SQLException {
		int separator = user_input.indexOf(";");
		String aspect = user_input.substring(0, separator);
		String keyword_lst[] = user_input.substring(separator + 1,
				user_input.length()).split(" ");
		for (int i = 0; i < keyword_lst.length; i++) {
			this.insertStatement
					.executeUpdate("insert into " + FELib.DB_NAME + "." + FELib.FEATURES_TABLE_NAME + "(aspect, keyword) values ('"
							+ aspect + "', '" + keyword_lst[i] + "')");
		}
		System.out.println("Inserted aspect and the keywords");
	}

	public String[] get_aspects() throws SQLException {
		String aspect_lst[] = new String[100];
		String query = "select distinct aspect \n";
		query += "from " + FELib.DB_NAME + "." + FELib.FEATURES_TABLE_NAME + "\n";
		query += "order by 1";
		ResultSet result_set = this.selectStatement.executeQuery(query);
		int i = 0;
		while (result_set.next()) {
			aspect_lst[i] = result_set.getString(1);
			i++;
		}
		return aspect_lst;
	}

	public String[] get_keywords(String aspect)
			throws SQLException {
		String keyword_lst[] = new String[100];
		String query = "select distinct keyword \n";
		query += "from " + FELib.DB_NAME + "." + FELib.FEATURES_TABLE_NAME + "\n";
		query += "where aspect = '" + aspect + "' \n";
		query += "order by 1";
		int i = 0;
		ResultSet result_set = this.selectStatement.executeQuery(query);
		while (result_set.next()) {
			keyword_lst[i] = result_set.getString(1);
			i++;
		}
		return keyword_lst;
	}

	public String[][] get_features() throws SQLException {
		String feature_lst[][] = new String[100][2];
		String query = "select aspect, keyword \n";
		query += "from " + FELib.DB_NAME + "." + FELib.FEATURES_TABLE_NAME + "\n";
		query += "order by 1, 2";
		int i = 0;
		ResultSet result_set = this.selectStatement.executeQuery(query);
		while (result_set.next()) {
			feature_lst[i][0] = result_set.getString(1);
			feature_lst[i][1] = result_set.getString(2);
			i++;
		}
		return feature_lst;
	}
}