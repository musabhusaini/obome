package web_package;

import java.util.List;
import java.util.Map;

import nlp_engine.ModifierItem;
import nlp_engine.Paragraph;
import nlp_engine.SentenceObject;
import database_connector.DatabaseAdapter;

public class CommentResult {
	private Paragraph paragraph;

	public CommentResult(String comment, byte[] uuid, String connectionString)
			throws Exception {
		this.paragraph = new Paragraph(comment, new DatabaseAdapter(
				connectionString, uuid));
	}

	public Map<Long, Float> GetScoreMap() {
		return this.paragraph.GetScoreMap();
	}

	public List<ModifierItem> GetModifierList() {
		return this.paragraph.GetModifierList();
	}
	
	public List<SentenceObject> GetSentences() {
		return this.paragraph.GetSentenceMap();
	}
}
