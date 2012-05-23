package models;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import play.db.jpa.GenericModel;

public class DisplayTextModel extends GenericModel {
	
	public enum DisplayTextType {
		ROOT,
		SENTENCE,
		STANDARD,
		SEPARATOR,
		MODIFIER,
		KEYWORD,
		POLAR,
		SENTENCE_POLARITY,
		IRRELEVANT,
		SEEN,
		UNSEEN
	}
	
	public String content;
	public List<DisplayTextType> types;
	public Map<String,Object> otherInfo;
	public List<DisplayTextModel> children;
	
	public DisplayTextModel() {
		this.types = Lists.newArrayList();
		this.otherInfo = Maps.newHashMap();
		this.children = Lists.newArrayList();
	}
	
	public JsonObject toJson() {
		return new Gson().toJsonTree(this, this.getClass()).getAsJsonObject();
	}
}