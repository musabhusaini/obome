package eu.ubipol.opinionmining.nlp_engine;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import reader.OpinionWordReader;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.ubipol.opinionmining.stem_engine.Stemmer;

public class Utils {
  public static enum TokenType {
    ADJECTIVE, ADVERB, NOUN, VERB, OTHER
  };

  public static int ADJECTIVE_PHRASE = 1;
  public static int ADVERB_PHRASE = 2;
  public static int NOUN_PHRASE = 3;
  public static int VERB_PHRASE = 4;

  private static StanfordCoreNLP pipeline;

  private static Map<String, Float> wordScores;

  public static Map<String, Float> GetScoreList() throws Exception {
    if (wordScores == null) {
      wordScores = new HashMap<String, Float>();
      OpinionWordReader opinionReader = new OpinionWordReader(Utils.class.getResourceAsStream("resources/sentiwordnet_processed.txt"));
      Stemmer stemmer = new Stemmer((InputStream)null, null, null, "s");
      while (opinionReader.HasNextLine()) {
        String word = stemmer.GetStems(opinionReader.GetNext()).get(0);
        if (opinionReader.GetCurrentType() == eu.ubipol.opinionmining.owl_engine.Utils.ADJECTIVE_ID)
          word = word + "_a";
        else if (opinionReader.GetCurrentType() == eu.ubipol.opinionmining.owl_engine.Utils.ADVERB_ID)
          word = word + "_r";
        else if (opinionReader.GetCurrentType() == eu.ubipol.opinionmining.owl_engine.Utils.NOUN_ID)
          word = word + "_n";
        else if (opinionReader.GetCurrentType() == eu.ubipol.opinionmining.owl_engine.Utils.VERB_ID)
          word = word + "_v";
        if (!wordScores.containsKey(word)) {
          wordScores.put(word, Float.parseFloat(Double.toString(opinionReader.GetCurrentScore())));
        }
      }
    }
    return wordScores;
  }

  public static int GetWordType(String typeString) {
    if (typeString.length() < 2)
      return 5;
    else {
      String typeDef = typeString.substring(0, 2);
      if (typeDef.equals("JJ"))
        return eu.ubipol.opinionmining.owl_engine.Utils.ADJECTIVE_ID;
      else if (typeDef.equals("RB"))
        return eu.ubipol.opinionmining.owl_engine.Utils.ADVERB_ID;
      else if (typeDef.equals("VB"))
        return eu.ubipol.opinionmining.owl_engine.Utils.VERB_ID;
      else if (typeDef.equals("NN"))
        return eu.ubipol.opinionmining.owl_engine.Utils.NOUN_ID;
      else
        return 5;
    }
  }

  protected static StanfordCoreNLP GetNlpEngine() {
    if (pipeline == null) {
      Properties props = new Properties();
      props.put("annotators", "tokenize, ssplit, pos, parse");
      pipeline = new StanfordCoreNLP(props);
    }
    return pipeline;
  }
}
