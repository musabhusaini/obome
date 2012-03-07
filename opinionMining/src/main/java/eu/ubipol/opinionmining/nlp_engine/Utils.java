package eu.ubipol.opinionmining.nlp_engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import com.sun.media.sound.InvalidFormatException;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Utils {
  public static enum TokenType {
    ADJECTIVE, ADVERB, NOUN, VERB, OTHER
  };

  public static int ADJECTIVE_PHRASE = 1;
  public static int ADVERB_PHRASE = 2;
  public static int NOUN_PHRASE = 3;
  public static int VERB_PHRASE = 4;

  private static StanfordCoreNLP pipeline;

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

  protected static SentenceDetectorME GetSentenceDetector() throws InvalidFormatException,
      FileNotFoundException, IOException {
    return new SentenceDetectorME(new SentenceModel(new FileInputStream("NlpModels/en-sent.bin")));
  }

  protected static ChunkerME GetChunker() throws InvalidFormatException, FileNotFoundException,
      IOException {
    return new ChunkerME(new ChunkerModel(new FileInputStream("NlpModels/en-chunker.bin")));
  }

  protected static TokenizerME GetTokenizer() throws InvalidFormatException, FileNotFoundException,
      IOException {
    return new TokenizerME(new TokenizerModel(new FileInputStream("NlpModels/en-token.bin")));
  }

  protected static POSTaggerME GetPosTagger() throws InvalidFormatException, FileNotFoundException,
      IOException {
    return new POSTaggerME(new POSModel(new FileInputStream("NlpModels/en-pos-maxent.bin")));
  }
}
