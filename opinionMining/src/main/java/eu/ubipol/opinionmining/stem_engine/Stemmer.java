package eu.ubipol.opinionmining.stem_engine;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.hunspell.HunspellDictionary;
import org.apache.lucene.analysis.hunspell.HunspellStemmer;
import org.apache.lucene.analysis.hunspell.HunspellStemmer.Stem;

public class Stemmer {
  HunspellStemmer hunspellStem = null;
  PaiceStemmer paiceStem = null;
  EnglishStemmer snowballStem = null;

  public Stemmer(String rulesFile, String affFile, String dicFile, String stemmerOptions)
      throws Exception {
	  this(rulesFile != null ? new FileInputStream(rulesFile) : (InputStream)null,
			  affFile != null ? new FileInputStream(affFile) : null,
			  dicFile != null ? new FileInputStream(dicFile) : null, stemmerOptions);
  }
  
  public Stemmer(InputStream rules, InputStream aff, InputStream dic, String stemmerOptions)
	      throws Exception {
    if (stemmerOptions.contains("h")) {
      HunspellDictionary dictionary = new HunspellDictionary(aff, dic);
      hunspellStem = new HunspellStemmer(dictionary);
    }
    if (stemmerOptions.contains("p"))
      paiceStem = new PaiceStemmer(rules, "/p");
    if (stemmerOptions.contains("s"))
      snowballStem = new EnglishStemmer();
  }

  public List<String> GetStems(String word) {
    List<String> result = new ArrayList<String>();
    if (hunspellStem != null)
      for (Stem s : hunspellStem.stem(word))
        if (!result.contains(s))
          result.add(s.getStemString());
    if (paiceStem != null) {
      String temp = paiceStem.stripAffixes(word);
      if (!result.contains(temp))
        result.add(temp);
    }
    if (snowballStem != null) {
      snowballStem.setCurrent(word);
      snowballStem.stem();
      if (!result.contains(snowballStem.getCurrent()))
        result.add(snowballStem.getCurrent());
    }
    return result;
  }
}
