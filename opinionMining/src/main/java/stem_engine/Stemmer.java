package stem_engine;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.hunspell.HunspellDictionary;
import org.apache.lucene.analysis.hunspell.HunspellStemmer;
import org.apache.lucene.analysis.hunspell.HunspellStemmer.Stem;

public class Stemmer {
  private static HunspellStemmer hunspellStem = null;
  private static PaiceStemmer paiceStem = null;
  private static EnglishStemmer snowballStem = null;

  public static List<String> GetStems(String word, String stemmerOptions) {
    if (stemmerOptions.contains("h") && hunspellStem == null) {
      try {
        InputStream aff = Stemmer.class.getResourceAsStream("resources/en_US.aff");
        InputStream dic = Stemmer.class.getResourceAsStream("resources/en_US.dic");
        HunspellDictionary dictionary = new HunspellDictionary(aff, dic);
        hunspellStem = new HunspellStemmer(dictionary);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (stemmerOptions.contains("p") && paiceStem == null)
      paiceStem = new PaiceStemmer(Stemmer.class.getResourceAsStream("resources/stemrules.txt"), "/p");
    if (stemmerOptions.contains("s") && snowballStem == null)
      snowballStem = new EnglishStemmer();

    List<String> result = new ArrayList<String>();
    String temp;
    if (stemmerOptions.contains("h"))
      for (Stem s : hunspellStem.stem(word))
        if (!result.contains(s))
          result.add(s.getStemString());
    if (stemmerOptions.contains("p")) {
      temp = paiceStem.stripAffixes(word);
      if (!result.contains(temp))
        result.add(temp);
    }
    if (stemmerOptions.contains("s")) {
      snowballStem.setCurrent(word);
      snowballStem.stem();
      temp = snowballStem.getCurrent();
      if (!result.contains(temp))
        result.add(temp);
    }
    temp = null;
    return result;
  }
}
