package models;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import play.db.jpa.GenericModel;

public class DisplayFeatureModel extends GenericModel {
	public String content;
	public String type;
	
	public JsonObject toJson() {
		return new Gson().toJsonTree(this, this.getClass()).getAsJsonObject();
	}
}