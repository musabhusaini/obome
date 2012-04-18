package nlp_engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import database_connector.DatabaseAdapter;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class Paragraph {
  private List<Sentence> sentences;

  public Paragraph(String text, DatabaseAdapter adp) {
    Annotation document = new Annotation(text);
    Utils.GetNlpEngine().annotate(document);
    List<CoreMap> iSentences = document.get(SentencesAnnotation.class);
    sentences = new ArrayList<Sentence>();
    int count = 0;
    SemanticGraph dependencies;
    for (CoreMap c : iSentences) {
      dependencies = c.get(BasicDependenciesAnnotation.class);
      if (dependencies.getRoots().size() > 0) {
        sentences.add(new Sentence(dependencies, count, adp));
        count += dependencies.size();
      }
    }
  }

  public Map<Long, Float> GetScoreMap() {
    Map<Long, Float> tempScoreMap = new HashMap<Long, Float>();
    Map<Long, Integer> tempWeightMap = new HashMap<Long, Integer>();

    for (Sentence s : sentences)
      for (Entry<Long, Float> e : s.GetScoreMap().entrySet()) {
        if (!tempScoreMap.containsKey(e.getKey())) {
          tempScoreMap.put(e.getKey(), new Float(0));
          tempWeightMap.put(e.getKey(), 0);
        }
        tempScoreMap.put(e.getKey(),
            (tempScoreMap.get(e.getKey()) * tempWeightMap.get(e.getKey()) + e.getValue()
                * s.GetWeightMap().get(e.getKey()))
                / (s.GetWeightMap().get(e.getKey()) + tempWeightMap.get(e.getKey())));
        tempWeightMap.put(e.getKey(),
            tempWeightMap.get(e.getKey()) + s.GetWeightMap().get(e.getKey()));
      }

    return tempScoreMap;
  }

  public List<ModifierItem> GetModifierList() {
    List<ModifierItem> indexList = new ArrayList<ModifierItem>();
    for (Sentence s : sentences)
      indexList.addAll(s.GetModifierList());
    return indexList;
  }
}
