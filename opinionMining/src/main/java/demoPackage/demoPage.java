package demoPackage;

import java.util.Map;

import eu.ubipol.opinionmining.nlp_engine.Paragraph;
import eu.ubipol.opinionmining.owl_engine.OntologyHandler;
import eu.ubipol.opinionmining.owl_engine.OntologyHandlerForDatabase;

public class demoPage {
  public static Map<Long, Float> GetScoreOfAComment(String commentText, String connectionString,
      byte[] uuid) throws Exception {
    OntologyHandler ont = new OntologyHandlerForDatabase(connectionString, uuid);
    Paragraph p = Paragraph.GetParagraphInstanceForStanford(commentText, ont);
    return p.GetScoreInfo();
  }
}
