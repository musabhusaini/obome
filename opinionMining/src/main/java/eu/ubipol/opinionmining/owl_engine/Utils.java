package eu.ubipol.opinionmining.owl_engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import eu.ubipol.opinionmining.stemmer.EnglishStemmer;

public class Utils {
  // [start] Ontology Keywords
  public final static String ASPECT_KEYWORD = "Aspects";
  public final static String COMMENT_KEYWORD = "Comments";
  public final static String POLARITY_KEYWORD = "Polarities";
  public final static String ADJECTIVE_KEYWORD = "Adjectives";
  public final static String ADVERB_KEYWORD = "Adverbs";
  public final static String NOUN_KEYWORD = "Nouns";
  public final static String VERB_KEYWORD = "Verbs";
  public final static String COMMENTTEXT_KEYWORD = "CommentText";
  public final static String COMMENTDATE_KEYWORD = "Date";
  public final static String POLARITYVALUE_KEYWORD = "Value";
  public final static String COMMENT_PREFIX = "Comment_";
  public final static String RATING_PREFIX = "Rating_";
  public final static String WEIGTH_PREFIX = "Weight_";
  public final static String GROUP_PREFIX = "Group_";
  public final static String ASPECT_PREFIX = "Aspect_";
  public final static String ADJECTIVE_PREFIX = "Adj_";
  public final static String ADVERB_PREFIX = "Adv_";
  public final static String VERB_PREFIX = "Vb_";
  public final static String NOUN_PREFIX = "Nn_";
  public final static String KEYWORD_PREFIX = "Kyw_";
  public final static String DOMAIN_PREFIX = "Domain_";
  public final static String SCORECARD_KEYWORD = "Scorecards";
  public final static String SCORECARD_PREFIX = "Scorecard_";

  // [end]

  public static int ADJECTIVE_ID = 1;
  public static int ADVERB_ID = 2;
  public static int VERB_ID = 3;
  public static int NOUN_ID = 4;

  private static Map<String, Float> scores;

  public static Map<String, Float> GetScores() throws Exception {
    if (scores == null) {
      scores = new HashMap<String, Float>();
      EnglishStemmer stemmer = new EnglishStemmer();
      Scanner reader = new Scanner(Utils.class.getResourceAsStream("sentiwordnet_processed.txt"));
      while (reader.hasNextLine()) {
        String[] line = reader.nextLine().split("\t");
        stemmer.setCurrent(line[0].trim());
        stemmer.stem();
        if (!scores.containsKey(stemmer.getCurrent() + "_" + line[1]))
          scores.put(stemmer.getCurrent() + "_" + line[1], Float.parseFloat(line[2]));
      }
      reader.close();
    }
    return scores;
  }

  // [start] Ontology String Preparations
  protected static String GetAspectString(String aspect) {
    return ASPECT_PREFIX + aspect.trim().replace(' ', '_');
  }

  protected static String GetDomainString(String domainName) {
    return DOMAIN_PREFIX + domainName.trim();
  }

  protected static String GetKeywordString(String keyword) {
    return KEYWORD_PREFIX + keyword.trim().toLowerCase();
  }

  protected static String GetPolarityWordString(String word, String prefix) {
    return prefix + word.trim().toLowerCase();
  }

  protected static String GetGroupString(String groupId) {
    return Utils.GROUP_PREFIX + groupId;
  }

  protected static String GetCommentString(String commentId) {
    return Utils.COMMENT_PREFIX + commentId;
  }

  protected static String GetScorecardString(String commentId) {
    return Utils.SCORECARD_PREFIX + commentId;
  }

  protected static String GetCommentTextString(String text) {
    return text;
  }

  protected static String GetCommentDateString(String date) {
    return date;
  }

  protected static String GetRatingString(String aspect) {
    return Utils.RATING_PREFIX + aspect.trim().replace(' ', '_');
  }

  protected static String GetWeightString(String aspect) {
    return Utils.WEIGTH_PREFIX + aspect.trim().replace(' ', '_');
  }

  // [end]

  // [start] Ontology String Cleanings
  protected static String CleanAspectString(String str) {
    return str.substring(ASPECT_PREFIX.length());
  }

  protected static String CleanDomainString(String str) {
    return str.substring(DOMAIN_PREFIX.length());
  }

  protected static String CleanKeywordString(String str) {
    return str.substring(KEYWORD_PREFIX.length());
  }

  protected static String CleanPolarityWord(String str, String typePrefix) {
    return str.substring(typePrefix.length());
  }

  protected static String CleanGroupString(String str) {
    return str.substring(GROUP_PREFIX.length());
  }

  protected static String CleanCommentString(String str) {
    return str.substring(COMMENT_PREFIX.length());
  }

  protected static String CleanScorecardString(String str) {
    return str.substring(SCORECARD_PREFIX.length());
  }

  protected static String CleanRatingString(String str) {
    return str.substring(RATING_PREFIX.length());
  }
  // [end]
}
