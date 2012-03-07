package eu.ubipol.opinionmining.stem_engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.hunspell.HunspellDictionary;
import org.apache.lucene.analysis.hunspell.HunspellStemmer;
import org.apache.lucene.analysis.hunspell.HunspellStemmer.Stem;

public class Stemmer {
  HunspellStemmer hunspellStem;
  PaiceStemmer paiceStem;

  public Stemmer(String rulesFile, String affFile, String dicFile) throws Exception {
    InputStream aff = new FileInputStream(new File(affFile));
    InputStream dic = new FileInputStream(new File(dicFile));
    HunspellDictionary dictionary = new HunspellDictionary(aff, dic);
    hunspellStem = new HunspellStemmer(dictionary);

    paiceStem = new PaiceStemmer(rulesFile, "/p");
  }

  public List<String> GetStems(String word) {
    List<String> result = new ArrayList<String>();
    for (Stem s : hunspellStem.stem(word))
      result.add(s.getStemString());
    result.add(paiceStem.stripAffixes(word));
    return result;
  }
}
