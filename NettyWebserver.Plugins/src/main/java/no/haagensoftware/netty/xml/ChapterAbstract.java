package no.haagensoftware.netty.xml;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ChapterAbstract {
	private String id;
	private List<Paragraph> itemlist;
	private List<Paragraph> paragraphs;
	
	public ChapterAbstract() {
		paragraphs = new ArrayList<Paragraph>();
		itemlist = new ArrayList<Paragraph>();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<Paragraph> getItemlist() {
		return itemlist;
	}
	public void setItemlist(List<Paragraph> itemlist) {
		this.itemlist = itemlist;
	}
	public List<Paragraph> getParagraphs() {
		return paragraphs;
	}
	public void setParagraphs(List<Paragraph> paragraphs) {
		this.paragraphs = paragraphs;
	}
	
	public JsonObject toJSON() {
		JsonObject chAbs = new JsonObject();
		chAbs.addProperty("id", this.getId());
		
		JsonArray pars = new JsonArray();
		for (Paragraph par : this.getParagraphs()) {
			pars.add(new JsonPrimitive(par.getId()));
		}
		chAbs.add("paragraphs", pars);
		
		JsonArray items = new JsonArray();
		for (Paragraph par : this.getItemlist()) {
			items.add(new JsonPrimitive(par.getId()));
		}
		chAbs.add("itemList", items);
		return chAbs;
	}
	
	
}
