package eu.ubipol.opinionmining.owl_engine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.ubipol.opinionmining.Domain;
import eu.ubipol.opinionmining.Feature;
import eu.ubipol.opinionmining.polarity_analyzer.DomainImpl;
import eu.ubipol.opinionmining.polarity_analyzer.FeatureImpl;

public class OntologyHandlerForDatabase extends OntologyHandler {
  private Connection conn;
  private Long domainId;
  private byte[] uuid;

  public OntologyHandlerForDatabase(String connectionString, Long domainId) throws Exception {
    Class.forName("org.postgresql.Driver");
    conn = DriverManager.getConnection(connectionString);
    this.domainId = domainId;
  }

  public OntologyHandlerForDatabase(String connectionString, byte[] uuid) throws Exception {
    // Class.forName("org.postgresql.Driver");
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    conn = DriverManager.getConnection(connectionString);
    this.uuid = uuid;
  }

  @Override
  public boolean HasOntology(Long domainId) throws Exception {
    PreparedStatement stmt = conn.prepareCall("select count(*) from DOMAINS where domainId = ?");
    stmt.setLong(1, domainId);
    ResultSet set = stmt.executeQuery();
    set.next();
    if (set.getInt(1) > 0)
      return true;
    else
      return false;
  }

  @Override
  public void CreateOntology(String domainName) throws Exception {
    PreparedStatement stmt = conn
        .prepareCall("insert into DOMAINS(domainId, domainName, createDate) values(?, ?, (SELECT LOCALTIMESTAMP))");
    stmt.setLong(1, domainId);
    stmt.setString(2, domainName);
    stmt.execute();
    stmt.close();
  }

  @Override
  public void CloseOntology() {
    try {
      if (conn != null)
        conn.close();
      conn = null;
    } catch (SQLException e) {
    }
  }

  @Override
  public String GetDomainName() throws SQLException {
    String result;
    PreparedStatement stmt = conn.prepareCall("Select domainName from DOMAINS where domainId = ?");
    stmt.setLong(1, domainId);
    ResultSet set = stmt.executeQuery();
    if (set.next())
      result = set.getString("domainName");
    else
      result = null;
    set.close();
    stmt.close();
    return result;
  }

  @Override
  public Map<Long, String> GetFeatures() throws SQLException {
    PreparedStatement stmt = conn
        .prepareStatement("Select featureId, featureName from FEATURES where domainId = ?");
    stmt.setLong(1, domainId);
    ResultSet set = stmt.executeQuery();
    Map<Long, String> features = new HashMap<Long, String>();
    while (set.next()) {
      features.put(set.getLong("featureId"), set.getString("featureName"));
    }
    set.close();
    stmt.close();
    return features;
  }

  @Override
  public Map<Long, String> GetKeywords(Long featureId) throws SQLException {
    PreparedStatement stmt = conn
        .prepareStatement("Select keywordId, keywordName from KEYWORDS where featureId = ?");
    stmt.setLong(1, featureId);
    ResultSet set = stmt.executeQuery();
    Map<Long, String> keywords = new HashMap<Long, String>();
    while (set.next()) {
      keywords.put(set.getLong("keywordId"), set.getString("keywordName"));
    }
    set.close();
    stmt.close();
    return keywords;
  }

  @Override
  public Long GetFeatureOfAWord(String word) throws SQLException {
    // PreparedStatement stmt = conn
    // .prepareStatement("Select featureId from KEYWORDS where featureId in (Select featureId from FEATURES where domainId = ?) and keywordName = ?");
    // stmt.setLong(1, domainId);
    // stmt.setString(2, word);
    // ResultSet set = stmt.executeQuery();
    // if (set.next()) {
    // Long result = set.getLong("featureId");
    // set.close();
    // stmt.close();
    // return result;
    // }
    // set.close();
    // stmt.close();
    // return new Long(-1);
    PreparedStatement stmt = conn
        .prepareStatement("SELECT CAST(CONV(SUBSTRING(MD5(aspects.uuid), 1, 15), 16, 10) AS SIGNED INTEGER) AS featureId FROM aspects WHERE aspects.uuid IN (SELECT keywords.aspect_uuid FROM keywords WHERE keywords.label=?) AND aspects.setcover_uuid=?;");
    stmt.setString(1, word);
    stmt.setBytes(2, uuid);
    ResultSet set = stmt.executeQuery();
    if (set.next()) {
      Long result = set.getLong("featureId");
      set.close();
      stmt.close();
      return result;
    }
    set.close();
    stmt.close();
    return new Long(-1);
  }

  @Override
  public float GetWordScore(String word, int typeId) throws SQLException {
    PreparedStatement stmt = conn
        .prepareStatement("select score from POLARITYWORDS where domainId = ? and wordType = ? and wordName = ?");
    stmt.setLong(1, domainId);
    stmt.setInt(2, typeId);
    stmt.setString(3, word);
    ResultSet set = stmt.executeQuery();
    float result = -2;
    if (set.next()) {
      result = set.getFloat("score");
    }
    set.close();
    stmt.close();
    return result;
  }

  @Override
  public List<ScoreResult> GetScoreCard(Long commentId) throws SQLException {
    PreparedStatement stmt = conn
        .prepareStatement("select featureName, SCORECARDS.featureId, score, weight from SCORECARDS inner join FEATURES on FEATURES.featureId = SCORECARDS.featureId where domainId = ? and commentId = ?");
    stmt.setLong(1, domainId);
    stmt.setLong(2, commentId);
    ResultSet set = stmt.executeQuery();
    List<ScoreResult> result = new ArrayList<ScoreResult>();
    while (set.next()) {
      result.add(new ScoreResult(set.getLong("featureId"), set.getString("featureName"), set
          .getInt("weight"), set.getFloat("score")));
    }
    set.close();
    stmt.close();
    return result;
  }

  @Override
  public Long AddFeature(String featureName) throws SQLException {
    PreparedStatement stmt = conn
        .prepareStatement("insert into FEATURES(domainId, featureName, createDate) values(?,?,(SELECT LOCALTIMESTAMP)) RETURNING featureId");
    stmt.setLong(1, domainId);
    stmt.setString(2, featureName);
    ResultSet set = stmt.executeQuery();
    if (set.next()) {
      Long newId = set.getLong(1);
      set.close();
      stmt.close();
      return newId;
    }
    set.close();
    stmt.close();
    return null;
  }

  @Override
  public Long AddKeyWord(Long featureId, String keywordName) throws SQLException {
    PreparedStatement stmt = conn
        .prepareStatement("insert into KEYWORDS(featureId, keywordName, createDate) values(?,?,(SELECT LOCALTIMESTAMP)) RETURNING keywordId");
    stmt.setLong(1, featureId);
    stmt.setString(2, keywordName);
    ResultSet set = stmt.executeQuery();
    if (set.next()) {
      Long newId = set.getLong(1);
      set.close();
      stmt.close();
      return newId;
    }
    set.close();
    stmt.close();
    return null;
  }

  @Override
  public Long AddPolarityWord(int wordType, float score, String word) throws SQLException {
    PreparedStatement stmt = conn
        .prepareStatement("insert into POLARITYWORDS(domainId, wordType, score, wordName, createDate) values(?,?,?,?,(SELECT LOCALTIMESTAMP)) RETURNING polarityWordId");
    stmt.setLong(1, domainId);
    stmt.setInt(2, wordType);
    stmt.setFloat(3, score);
    stmt.setString(4, word);
    ResultSet set = stmt.executeQuery();
    if (set.next()) {
      Long newId = set.getLong(1);
      set.close();
      stmt.close();
      return newId;
    }
    set.close();
    stmt.close();
    return null;
  }

  @Override
  public void AddScoreCardItem(Long commentId, Long featureId, float score, int weight)
      throws SQLException {
    PreparedStatement stmt = conn
        .prepareStatement("insert into SCORECARDS(commentId, featureId, score, weight, createDate) values(?,?,?,?,(SELECT LOCALTIMESTAMP))");
    stmt.setLong(1, commentId);
    stmt.setLong(2, featureId);
    stmt.setFloat(3, score);
    stmt.setInt(4, weight);
    stmt.execute();
    stmt.close();
  }

  @Override
  public void AddComment(Long commentId, Date date) throws Exception {
    PreparedStatement stmt = conn
        .prepareStatement("insert into COMMENTS(commentId, domainId, commentDate, createDate) values(?,?,?,(SELECT LOCALTIMESTAMP))");
    stmt.setLong(1, commentId);
    stmt.setLong(2, domainId);
    stmt.setDate(3, new java.sql.Date(date.getTime()));
    stmt.execute();
    stmt.close();
  }

  @Override
  public List<Feature> GetOrderedFeatures(Date startDate, Date endDate, int k) throws Exception {
    List<Feature> features = new ArrayList<Feature>();
    PreparedStatement stmt = conn
        .prepareStatement("Select FEATURES.featureId, featureName, count(*) as counts from SCORECARDS inner join FEATURES on FEATURES.featureId = SCORECARDS.featureId inner join COMMENTS on COMMENTS.commentId = SCORECARDS.commentId where FEATURES.domainId = ? and COMMENTS.commentDate >= ? and COMMENTS.commentDate <= ? group by FEATURES.featureId, featureName order by counts desc LIMIT "
            + Integer.toString(k));
    stmt.setLong(1, domainId);
    stmt.setDate(2, new java.sql.Date(startDate.getTime()));
    stmt.setDate(3, new java.sql.Date(endDate.getTime()));
    ResultSet set = stmt.executeQuery();
    Domain d = new DomainImpl(domainId, GetDomainName());
    while (set.next()) {
      features.add(new FeatureImpl(set.getLong("featureId"), set.getString("featureName"), d));
    }
    set.close();
    stmt.close();
    return features;
  }

  @Override
  public Map<Long, Integer> GetFeatureCountBetweenDates(Date startDate, Date endDate)
      throws Exception {
    Map<Long, Integer> result = new HashMap<Long, Integer>();
    PreparedStatement stmt = conn
        .prepareStatement("Select FEATURES.featureId, featureName, count(*) as counts from SCORECARDS inner join FEATURES on FEATURES.featureId = SCORECARDS.featureId inner join COMMENTS on COMMENTS.commentId = SCORECARDS.commentId where FEATURES.domainId = ? and COMMENTS.commentDate >= ? and COMMENTS.commentDate <= ? group by FEATURES.featureId, featureName order by counts desc");
    stmt.setLong(1, domainId);
    stmt.setDate(2, new java.sql.Date(startDate.getTime()));
    stmt.setDate(3, new java.sql.Date(endDate.getTime()));
    ResultSet set = stmt.executeQuery();
    while (set.next()) {
      result.put(set.getLong("featureId"), set.getInt("counts"));
    }
    set.close();
    stmt.close();
    return result;
  }

  @Override
  public String GetFeatureName(Long featureId) throws Exception {
    String result;
    PreparedStatement stmt = conn
        .prepareCall("Select featureName from FEATURES where featureId = ?");
    stmt.setLong(1, featureId);
    ResultSet set = stmt.executeQuery();
    if (set.next())
      result = set.getString("featureName");
    else
      result = null;
    set.close();
    stmt.close();
    return result;
  }

  @Override
  public Map<Long, Integer> GetSumOfFeatureWeightsBetweenDates(Date startDate, Date endDate)
      throws Exception {
    Map<Long, Integer> result = new HashMap<Long, Integer>();
    PreparedStatement stmt = conn
        .prepareStatement("Select FEATURES.featureId, sum(weight) as weightSum from SCORECARDS inner join FEATURES on FEATURES.featureId = SCORECARDS.featureId inner join COMMENTS on COMMENTS.commentId = SCORECARDS.commentId where FEATURES.domainId = ? and COMMENTS.commentDate >= ? and COMMENTS.commentDate <= ? group by FEATURES.featureId, featureName");
    stmt.setLong(1, domainId);
    stmt.setDate(2, new java.sql.Date(startDate.getTime()));
    stmt.setDate(3, new java.sql.Date(endDate.getTime()));
    ResultSet set = stmt.executeQuery();
    while (set.next()) {
      result.put(set.getLong("featureId"), set.getInt("weightSum"));
    }
    set.close();
    stmt.close();
    return result;
  }

  @Override
  public Map<Long, Float> GetSumOfFeatureScoresBetweenDates(Date startDate, Date endDate)
      throws Exception {
    Map<Long, Float> result = new HashMap<Long, Float>();
    PreparedStatement stmt = conn
        .prepareStatement("Select FEATURES.featureId, sum(score * weight) as scoreSum from SCORECARDS inner join FEATURES on FEATURES.featureId = SCORECARDS.featureId inner join COMMENTS on COMMENTS.commentId = SCORECARDS.commentId where FEATURES.domainId = ? and COMMENTS.commentDate >= ? and COMMENTS.commentDate <= ? group by FEATURES.featureId, featureName");
    stmt.setLong(1, domainId);
    stmt.setDate(2, new java.sql.Date(startDate.getTime()));
    stmt.setDate(3, new java.sql.Date(endDate.getTime()));
    ResultSet set = stmt.executeQuery();
    while (set.next()) {
      result.put(set.getLong("featureId"), set.getFloat("scoreSum"));
    }
    set.close();
    stmt.close();
    return result;
  }
}
