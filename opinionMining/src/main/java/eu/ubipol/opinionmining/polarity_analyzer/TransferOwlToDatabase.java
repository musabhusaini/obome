package eu.ubipol.opinionmining.polarity_analyzer;

import java.util.List;

import eu.ubipol.opinionmining.nlp_engine.Utils.TokenType;
import eu.ubipol.opinionmining.owl_engine.OntologyHandler;
import eu.ubipol.opinionmining.owl_engine.OntologyHandlerForOwlFile;
import eu.ubipol.opinionmining.owl_engine.Utils;

public class TransferOwlToDatabase {
  public static void TransfreOwlFileToDatabase(String owlFile, String connectionString,
      Long domainId, String domainName) {
    try {
      OntologyHandlerForOwlFile owlHandler = new OntologyHandlerForOwlFile(domainName, owlFile);
      OntologyHandler ontDatabase = OntologyHandler.CreateOntologyHandler(connectionString,
          domainId);
      List<String> adjectives = owlHandler.GetAdjectives();
      List<String> adverbs = owlHandler.GetAdverbs();
      List<String> verbs = owlHandler.GetVerbs();
      List<String> nouns = owlHandler.GetNouns();
      List<String> aspects = owlHandler.GetAspects();
      ontDatabase.CreateOntology(domainName);
      for (int i = 0; i < adjectives.size(); i++)
        ontDatabase.AddPolarityWord(Utils.ADJECTIVE_ID,
            (float) owlHandler.GetPolarityValue(adjectives.get(i), TokenType.ADJECTIVE),
            adjectives.get(i));

      for (int i = 0; i < adverbs.size(); i++)
        ontDatabase.AddPolarityWord(Utils.ADVERB_ID,
            (float) owlHandler.GetPolarityValue(adverbs.get(i), TokenType.ADVERB), adverbs.get(i));

      for (int i = 0; i < verbs.size(); i++)
        ontDatabase.AddPolarityWord(Utils.VERB_ID,
            (float) owlHandler.GetPolarityValue(verbs.get(i), TokenType.VERB), verbs.get(i));

      for (int i = 0; i < nouns.size(); i++)
        ontDatabase.AddPolarityWord(Utils.NOUN_ID,
            (float) owlHandler.GetPolarityValue(nouns.get(i), TokenType.NOUN), nouns.get(i));

      for (int i = 0; i < aspects.size(); i++) {
        List<String> keywords = owlHandler.GetKeywords(aspects.get(i));
        Long aspectId = ontDatabase.AddFeature(aspects.get(i));

        for (int k = 0; k < keywords.size(); k++)
          if (keywords.get(i).length() <= 30)
            ontDatabase.AddKeyWord(aspectId, keywords.get(i));
      }
      ontDatabase.CloseOntology();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
