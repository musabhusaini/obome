package nlp_engine;

import java.util.Properties;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Utils {
  private static StanfordCoreNLP pipeline;

  public static void LoadNlpEngine() {
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, parse");
    pipeline = new StanfordCoreNLP(props);
  }

  protected static StanfordCoreNLP GetNlpEngine() {
    if (pipeline == null) {
      LoadNlpEngine();
    }
    return pipeline;
  }

  protected enum WordType {
    ADJECTIVE, ADVERB, NOUN, VERB, OTHER
  }
}
