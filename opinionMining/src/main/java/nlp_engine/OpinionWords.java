package nlp_engine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import nlp_engine.Utils.WordType;

public class OpinionWords {
  private static Map<String, Float> wordScores;

  public static void FillScoreList(File file) {
    try {
      wordScores = new HashMap<String, Float>();
      Scanner reader = new Scanner(file);
      while (reader.hasNextLine()) {
        String[] line = reader.nextLine().split("\t");
        wordScores.put(line[0], Float.valueOf(line[1]));
      }
    } catch (Exception e) {
    }
  }

  private static void FillScoreList() {
    try {
      wordScores = new HashMap<String, Float>();
      Scanner reader = new Scanner(OpinionWords.class.getResourceAsStream("resources/sentiwordnet_stemmed.txt"));
      while (reader.hasNextLine()) {
        String[] line = reader.nextLine().split("\t");
        wordScores.put(line[0], Float.valueOf(line[1]));
      }
    } catch (Exception e) {
    }
  }

  private static Map<String, Float> GetScoreList() {
    try {
      if (wordScores == null) {
        FillScoreList();
      }
      return wordScores;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  protected static Float GetWordScore(String word, WordType wordType) {
    String searchString = word;
    if (wordType == WordType.ADJECTIVE)
      searchString += "_a";
    else if (wordType == WordType.ADVERB)
      searchString += "_r";
    else if (wordType == WordType.VERB)
      searchString += "_v";
    else if (wordType == WordType.NOUN)
      searchString += "_n";
    else
      return new Float(-2);
    if (GetScoreList().containsKey(searchString))
      return GetScoreList().get(searchString);
    else
      return new Float(-2);
  }
}
