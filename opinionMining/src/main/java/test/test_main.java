package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import reader.AspectWordTextReader;
import reader.CommentTextReader;
import reader.OpinionWordReader;
import eu.ubipol.opinionmining.Feature;
import eu.ubipol.opinionmining.FeatureSentiment;
import eu.ubipol.opinionmining.OpinionMiningEngine;
import eu.ubipol.opinionmining.nlp_engine.Paragraph;
import eu.ubipol.opinionmining.owl_engine.OntologyHandler;
import eu.ubipol.opinionmining.polarity_analyzer.DomainImpl;
import eu.ubipol.opinionmining.polarity_analyzer.OpinionMiningEngineImpl;

public class test_main {

  @SuppressWarnings("unused")
  private static void AddCommentTable() throws ClassNotFoundException, SQLException {
    Class.forName("org.postgresql.Driver");
    Connection conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/postgres",
        "postgres", "1801");
    PreparedStatement stmt = conn.prepareCall("CREATE SEQUENCE comment_id_seq;");
    stmt.execute();
    stmt = conn
        .prepareStatement("CREATE TABLE OURCOMMENTS (commentId bigint,commentText text,rating1 int,rating2 int,rating3 int,rating4 int,rating5 int,rating6 int,rating7 int,rating8 int, commentDate timestamp, hotel text, CONSTRAINT commentKey UNIQUE(commentId));");
    stmt.execute();
    stmt = conn
        .prepareStatement("ALTER TABLE OURCOMMENTS ALTER COLUMN commentId SET DEFAULT NEXTVAL('comment_id_seq');");
    stmt.execute();
  }

  @SuppressWarnings("unused")
  private static void DeleteDatabase() throws ClassNotFoundException, SQLException {
    Class.forName("org.postgresql.Driver");
    Connection conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/postgres",
        "postgres", "1801");
    PreparedStatement stmt = conn.prepareCall("DROP TABLE DOMAINS");
    stmt.execute();
    stmt = conn.prepareCall("DROP TABLE FEATURES");
    stmt.execute();
    stmt = conn.prepareCall("DROP TABLE KEYWORDS");
    stmt.execute();
    stmt = conn.prepareCall("DROP TABLE POLARITYWORDS");
    stmt.execute();
    stmt = conn.prepareCall("DROP TABLE SCORECARDS");
    stmt.execute();
    stmt = conn.prepareCall("DROP TABLE COMMENTS");
    stmt.execute();
    // stmt = conn.prepareCall("DROP TABLE OURCOMMENTS");
    // stmt.execute();
    stmt = conn.prepareCall("DROP SEQUENCE domain_id_seq");
    stmt.execute();
    stmt = conn.prepareCall("DROP SEQUENCE feature_id_seq");
    stmt.execute();
    stmt = conn.prepareCall("DROP SEQUENCE keyword_id_seq");
    stmt.execute();
    stmt = conn.prepareCall("DROP SEQUENCE polarity_id_seq");
    stmt.execute();
    // stmt = conn.prepareCall("DROP SEQUENCE comment_id_seq");
    // stmt.execute();
  }

  @SuppressWarnings("unused")
  private static void createDatabase() throws ClassNotFoundException, SQLException {
    Class.forName("org.postgresql.Driver");
    Connection conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/postgres",
        "postgres", "1801");
    PreparedStatement stmt = conn.prepareCall("CREATE SEQUENCE domain_id_seq;");
    stmt.execute();
    stmt = conn.prepareCall("CREATE SEQUENCE feature_id_seq;");
    stmt.execute();
    stmt = conn.prepareCall("CREATE SEQUENCE keyword_id_seq;");
    stmt.execute();
    stmt = conn.prepareCall("CREATE SEQUENCE polarity_id_seq;");
    stmt.execute();

    stmt = conn
        .prepareStatement("CREATE TABLE DOMAINS (domainId bigint,domainName varchar(100),createDate timestamp, CONSTRAINT domainKey UNIQUE(domainId));");
    stmt.execute();
    stmt = conn
        .prepareStatement("ALTER TABLE DOMAINS ALTER COLUMN domainId SET DEFAULT NEXTVAL('domain_id_seq');");
    stmt.execute();

    stmt = conn
        .prepareStatement("CREATE TABLE FEATURES (featureId bigint, domainId bigint, featureName varchar(100),createDate timestamp, CONSTRAINT featureKey UNIQUE(featureId));");
    stmt.execute();
    stmt = conn
        .prepareStatement("ALTER TABLE FEATURES ALTER COLUMN featureId SET DEFAULT NEXTVAL('feature_id_seq');");
    stmt.execute();

    stmt = conn
        .prepareStatement("CREATE TABLE KEYWORDS (keywordId bigint, featureId bigint, keywordName varchar(100),createDate timestamp, CONSTRAINT keywordKey UNIQUE(keywordId));");
    stmt.execute();
    stmt = conn
        .prepareStatement("ALTER TABLE KEYWORDS ALTER COLUMN keywordId SET DEFAULT NEXTVAL('keyword_id_seq');");
    stmt.execute();

    stmt = conn
        .prepareStatement("CREATE TABLE POLARITYWORDS (polarityWordId bigint, wordType smallint, domainId bigint, score float8, wordName varchar(100),createDate timestamp, CONSTRAINT polarityKey UNIQUE(polarityWordId));");
    stmt.execute();
    stmt = conn
        .prepareStatement("ALTER TABLE POLARITYWORDS ALTER COLUMN polarityWordId SET DEFAULT NEXTVAL('polarity_id_seq');");
    stmt.execute();

    stmt = conn
        .prepareStatement("CREATE TABLE SCORECARDS (commentId bigint, featureId bigint, score float8, weight int,createDate timestamp);");
    stmt.execute();

    stmt = conn
        .prepareStatement("CREATE TABLE COMMENTS (commentId bigint, domainId bigint, commentDate timestamp, createDate timestamp);");
    stmt.execute();
  }

  private static void AddPolarityWords(OntologyHandler ont) throws Exception {
    OpinionWordReader opinionReader = new OpinionWordReader(
        "C:\\Users\\akocyigit\\Desktop\\Ubipol\\sentiwordnet_processed.txt");
    while (opinionReader.HasNextLine()) {
      String word = opinionReader.GetNext();
      ont.AddPolarityWord(opinionReader.GetCurrentType(), (float) opinionReader.GetCurrentScore(),
          word);
    }
    System.out.println("Polarity Words are done.");
  }

  private static void AddAspects(OntologyHandler ont) throws Exception {
    AspectWordTextReader aspectReader = new AspectWordTextReader(
        "C:\\Users\\akocyigit\\Desktop\\Ubipol\\FeatureWords.txt");

    String lastAspect = "";
    Long lastAspectId = new Long(0);
    while (aspectReader.HasNext()) {
      if (!aspectReader.GetCurrentAspect().equals(lastAspect)) {
        lastAspect = aspectReader.GetCurrentAspect();
        lastAspectId = ont.AddFeature(lastAspect);
      }
      ont.AddKeyWord(lastAspectId, aspectReader.GetNext());
    }
    System.out.println("Aspects and keywords are done.");
  }

  private static void AddComments(OntologyHandler ont, int commentCount, int overallScore)
      throws Exception {
    String path = "C:\\Users\\akocyigit\\Desktop\\Ubipol\\Texts";
    File folder = new File(path);
    File[] listOfFiles = folder.listFiles();
    int count = 0;
    for (int k = 0; k < listOfFiles.length && count < commentCount; k++) {
      CommentTextReader commentReader = new CommentTextReader(listOfFiles[k].getAbsolutePath());

      Class.forName("org.postgresql.Driver");
      Connection conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/postgres",
          "postgres", "1801");
      PreparedStatement stmt = conn
          .prepareCall("insert into OURCOMMENTS(commentText, rating1, rating2, rating3, rating4, rating5, rating6, rating7, rating8) values(?,?,?,?,?,?,?,?,?) RETURNING commentId");

      while (commentReader.HasNextLine() && count < commentCount) {
        String comment = commentReader.GetNext();
        Date commentDate = commentReader.GetCurrentDate();
        int[] commentRatings = commentReader.GetCurrentRatings();
        if (overallScore == -1 || commentRatings[0] == overallScore) {
          count++;
          stmt.setString(1, comment);
          for (int i = 0; i < commentRatings.length; i++)
            stmt.setInt(i + 2, commentRatings[i]);

          ResultSet set = stmt.executeQuery();
          set.next();
          Long commentId = set.getLong(1);
          Paragraph p = Paragraph.GetParagraphInstanceForStanford(comment, ont);
          for (Entry<Long, Float> e : p.GetScoreInfo().entrySet()) {
            ont.AddComment(commentId, new java.sql.Date(commentDate.getTime()));
            ont.AddScoreCardItem(commentId, e.getKey(), e.getValue(),
                p.GetWeightInfo().get(e.getKey()));
          }
          System.out.println("Comment no:" + commentId + " is added to ontology for hotel "
              + listOfFiles[k].getName().split("_")[1]);
        }
      }
      stmt.close();
      conn.close();
      commentReader = null;
    }
    System.out.println("Comments are done.");
  }

  public static void AddComments2(OntologyHandler ont, int commentCount, int overallScore)
      throws Exception {
    Class.forName("org.postgresql.Driver");
    Connection conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/postgres",
        "postgres", "1801");
    PreparedStatement stmt = conn
        .prepareCall("select * from OURCOMMENTS where rating1 = ? LIMIT 100");
    stmt.setInt(1, overallScore);
    ResultSet set = stmt.executeQuery();
    while (set.next()) {
      String comment = set.getString("commentText");
      Long commentId = set.getLong("commentId");
      Paragraph p = Paragraph.GetParagraphInstanceForStanford(comment, ont);
      for (Entry<Long, Float> e : p.GetScoreInfo().entrySet()) {
        ont.AddComment(commentId, set.getDate("commentDate"));
        ont.AddScoreCardItem(commentId, e.getKey(), e.getValue(), p.GetWeightInfo().get(e.getKey()));
      }
      System.out.println("Comment no:" + commentId + " is added to ontology");
    }
    stmt.close();
    conn.close();
  }

  @SuppressWarnings("unused")
  private static void FillOntology(String domainName, Long domainId, int rating) throws Exception {
    OntologyHandler ont = OntologyHandler
        .CreateOntologyHandler(
            "jdbc:postgresql://127.0.0.1:5432/postgres?user=postgres&password=1801", new Long(
                domainId));

    ont.CreateOntology(domainName);
    AddAspects(ont);
    AddPolarityWords(ont);
    AddComments2(ont, 100, 5);

    ont.CloseOntology();
    System.out.println("All works are finished.");
  }

  @SuppressWarnings("unused")
  private static void DrawGraph(OpinionMiningEngine eng) {
    for (Object o : eng.getSentimentMap(new DomainImpl(new Long(1)),
        new GregorianCalendar(1900, 1, 1).getTime(), new GregorianCalendar(2015, 1, 1).getTime())
        .toArray()) {
      FeatureSentiment s = (FeatureSentiment) o;
      System.out.println(s.getFeature().getText() + "\t" + s.getFrequency() + "\t"
          + s.getPolarity());
    }
  }

  @SuppressWarnings("unused")
  private static void GetTopFeatures(OpinionMiningEngine eng) {
    for (Feature o : eng.findTopFeatures(new DomainImpl(new Long(1)), new GregorianCalendar(1900,
        1, 1).getTime(), new GregorianCalendar(2011, 1, 1).getTime(), 3)) {

      System.out.println(o.getText());
    }
  }

  @SuppressWarnings("unused")
  private static void GetEmergantFeatures(OpinionMiningEngine eng) {
    for (Feature o : eng.findEmergentFeatures(new DomainImpl(new Long(1)), new GregorianCalendar(
        1900, 1, 1).getTime(), new GregorianCalendar(1900, 1, 2).getTime(), new GregorianCalendar(
        1900, 1, 1).getTime(), new GregorianCalendar(2011, 1, 1).getTime())) {

      System.out.println(o.getText());
    }
  }

  private static void GetStats(Long domainId, double threshold, boolean writeOutputs)
      throws Exception {
    Class.forName("org.postgresql.Driver");
    Connection conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/postgres",
        "postgres", "1801");
    int TN = 0;
    int TP = 0;
    int FN = 0;
    int FP = 0;
    int[] TPs = new int[] { 0, 0, 0, 0, 0, 0, 0 };
    int[] FPs = new int[] { 0, 0, 0, 0, 0, 0, 0 };
    int[] TNs = new int[] { 0, 0, 0, 0, 0, 0, 0 };
    int[] FNs = new int[] { 0, 0, 0, 0, 0, 0, 0 };

    String fileName = "Random";
    if (domainId.intValue() == 7)
      fileName = "5stars";
    else if (domainId.intValue() == 8)
      fileName = "1stars";

    FileWriter writer = null;
    if (writeOutputs)
      writer = new FileWriter(new File("C:\\Users\\akocyigit\\Desktop\\" + fileName + "_threshold"
          + threshold + ".txt"));

    PreparedStatement stmt = conn.prepareCall("select commentId from COMMENTS where domainId = ?");
    stmt.setLong(1, domainId);
    ResultSet set = stmt.executeQuery();
    List<Long> comments = new ArrayList<Long>();

    while (set.next()) {
      comments.add(set.getLong("commentId"));
    }
    List<List<Integer>> ratings = new ArrayList<List<Integer>>();
    List<Map<Long, Float>> scores = new ArrayList<Map<Long, Float>>();

    for (int i = 0; i < comments.size(); i++) {
      stmt = conn
          .prepareCall("select rating2, rating3, rating4, rating5, rating6, rating7, rating8 from OURCOMMENTS where commentId = ? and commentId in (select commentId from COMMENTS where domainId = ?)");
      stmt.setLong(1, comments.get(i));
      stmt.setLong(2, domainId);
      set = stmt.executeQuery();
      set.next();

      ratings.add(i, new ArrayList<Integer>());
      for (int k = 0; k < 7; k++)
        ratings.get(i).add(k, set.getInt(k + 1));

      stmt = conn
          .prepareCall("select featureId, score from SCORECARDS where commentId = ? and featureId in (select featureId from FEATURES where domainId = ?)");
      stmt.setLong(1, comments.get(i));
      stmt.setLong(2, domainId);

      set = stmt.executeQuery();
      scores.add(i, new HashMap<Long, Float>());
      while (set.next()) {
        scores.get(i).put(set.getLong(1), set.getFloat(2));
      }
    }
    for (int i = 0; i < comments.size(); i++) {
      for (Entry<Long, Float> e : scores.get(i).entrySet()) {
        int rate = ratings.get(i).get((e.getKey().intValue() - 1) % 7);
        if (rate > -1) {
          if (rate < 3 && e.getValue() < -1 * threshold) {
            TN++;
            TNs[(e.getKey().intValue() - 1) % 7]++;
          } else if (rate < 3 && e.getValue() > threshold) {
            FP++;
            FPs[(e.getKey().intValue() - 1) % 7]++;
          } else if (rate > 3 && e.getValue() > threshold) {
            TP++;
            TPs[(e.getKey().intValue() - 1) % 7]++;
          } else if (rate > 3 && e.getValue() < -1 * threshold) {
            FN++;
            FNs[(e.getKey().intValue() - 1) % 7]++;
          }
        }
      }
    }

    String[] aspectNames = new String[] { "Value", "Rooms", "Location", "Cleanliness",
        "Check in/front desk", "Service", "Business Service" };

    for (int i = 0; i < aspectNames.length; i++) {
      System.out.println("Stats for aspect : " + aspectNames[i]);
      System.out.println("True Negative : \t" + TNs[i]);
      System.out.println("True Positive : \t" + TPs[i]);
      System.out.println("False Negative : \t" + FNs[i]);
      System.out.println("False Positive : \t" + FPs[i]);

      System.out.println("Accuracy : \t" + ((double) TPs[i] + TNs[i])
          / (TNs[i] + TPs[i] + FNs[i] + FPs[i]));
      System.out.println("Recall : \t" + (double) TPs[i] / (TPs[i] + FPs[i]));
      System.out.println("Precision : \t" + (double) TNs[i] / (TNs[i] + FNs[i]));
      System.out.println("\n\n\n");

      if (writeOutputs) {
        writer.write("Stats for aspect : " + aspectNames[i] + "\n");
        writer.write("True Negative : \t" + TNs[i] + "\n");
        writer.write("True Positive : \t" + TPs[i] + "\n");
        writer.write("False Negative : \t" + FNs[i] + "\n");
        writer.write("False Positive : \t" + FPs[i] + "\n");

        writer.write("Accuracy : \t" + ((double) TPs[i] + TNs[i])
            / (TNs[i] + TPs[i] + FNs[i] + FPs[i]) + "\n");
        writer.write("Recall : \t" + (double) TPs[i] / (TPs[i] + FPs[i]) + "\n");
        writer.write("Precision : \t" + (double) TNs[i] / (TNs[i] + FNs[i]) + "\n");
        writer.write("\n\n\n");
      }
    }

    System.out.println("True Negative : \t" + TN);
    System.out.println("True Positive : \t" + TP);
    System.out.println("False Negative : \t" + FN);
    System.out.println("False Positive : \t" + FP);

    System.out.println("Accuracy : \t" + ((double) TP + TN) / (TN + TP + FN + FP));
    System.out.println("Recall : \t" + (double) TP / (TP + FP));
    System.out.println("Precision : \t" + (double) TN / (TN + FN));

    if (writeOutputs) {
      writer.write("True Negative : \t" + TN + "\n");
      writer.write("True Positive : \t" + TP + "\n");
      writer.write("False Negative : \t" + FN + "\n");
      writer.write("False Positive : \t" + FP + "\n");

      writer.write("Accuracy : \t" + ((double) TP + TN) / (TN + TP + FN + FP) + "\n");
      writer.write("Recall : \t" + (double) TP / (TP + FP) + "\n");
      writer.write("Precision : \t" + (double) TN / (TN + FN) + "\n");

      writer.close();
    }
  }

  public static String GetParsedText(String text, Map<String, Double> wordList,
      List<List<String>> words) throws Exception {
    String newText = new String(text.replaceAll("[^A-Za-z ]", ""));
    String tempText = "";
    String last;
    for (String s : newText.split(" ")) {
      last = "";
      if (s.length() >= 3 && s.toLowerCase().charAt(0) >= 'a' && s.toLowerCase().charAt(0) <= 'z') {
        List<String> tempList = words.get(s.toLowerCase().charAt(0) - 97);
        for (int i = 0; i < tempList.size(); i++) {
          String sentiWord = tempList.get(i);
          if (s.toLowerCase().startsWith(sentiWord) && last.length() < sentiWord.length()) {
            last = sentiWord;
          }
        }
      }
      if (wordList.containsKey(last))
        tempText += wordList.get(last) + " ";
      else
        tempText += s + " ";
    }
    return tempText;
  }

  public static Map<String, Double> GetSentiwordnetList(String sentiwordnetFilePath,
      List<List<String>> words) throws Exception {
    Scanner reader = new Scanner(new FileInputStream(sentiwordnetFilePath));
    Map<String, Double> wordList = new HashMap<String, Double>();
    int count = 0;
    while (reader.hasNextLine()) {
      String[] lineArray = reader.nextLine().split("\t");
      count++;
      // System.out.println(count);
      if (lineArray[0].length() >= 3 && lineArray[0].charAt(0) >= 'a'
          && lineArray[0].charAt(0) <= 'z'
          && !words.get(lineArray[0].charAt(0) - 97).contains(lineArray[0]))
        words.get(lineArray[0].charAt(0) - 97).add(lineArray[0]);
      if (lineArray[0].length() >= 3
          && (lineArray[1].equals("Adverb") || lineArray[1].equals("Adjective"))) {
        Double score = 0.0;
        if (Double.parseDouble(lineArray[2]) > Double.parseDouble(lineArray[4]))
          score = Double.parseDouble(lineArray[2]) * -1;
        else if (Double.parseDouble(lineArray[4]) > Double.parseDouble(lineArray[2]))
          score = Double.parseDouble(lineArray[4]);
        String curWord = lineArray[0].trim().toLowerCase();
        if (wordList.containsKey(curWord))
          wordList.put(curWord, (wordList.get(curWord) + score) / 2);
        else
          wordList.put(curWord, score);
      }
    }
    return wordList;
  }

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    OpinionMiningEngine eng = OpinionMiningEngineImpl
        .createOpinionMiningEngine("jdbc:postgresql://127.0.0.1:5432/postgres?user=postgres&password=1801");
    // DeleteDatabase();
    // createDatabase();
    // FillOntology("Hotel_5", new Long(1), 5);

    // Class.forName("org.postgresql.Driver");
    // Connection conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/postgres",
    // "postgres", "1801");
    // PreparedStatement stmt = conn
    // .prepareCall("Select commentText, score, featureName from SCORECARDS inner join OURCOMMENTS on OURCOMMENTS.commentId = SCORECARDS.commentId inner join FEATURES on FEATURES.featureId = SCORECARDS.featureId order by SCORECARDS.commentId");
    // ResultSet set = stmt.executeQuery();
    // while (set.next()) {
    // System.out.println(set.getString("commentText") + "\t" + set.getString("featureName") + "\t"
    // + set.getFloat("score"));
    // }
    // GetStats(new Long(1), 0, false);
    // GetStats(new Long(8), 0, true);
    // GetStats(new Long(9), 0, true);

    // GetStats(new Long(7), 0.05, true);
    // GetStats(new Long(8), 0.05, true);
    // GetStats(new Long(9), 0.05, true);

    // GetStats(new Long(7), 1, true);
    // GetStats(new Long(8), 1, true);
    // GetStats(new Long(9), 1, true);

    DrawGraph(eng);
    // GetEmergantFeatures(eng);
    // GetTopFeatures(eng);
  }
}
