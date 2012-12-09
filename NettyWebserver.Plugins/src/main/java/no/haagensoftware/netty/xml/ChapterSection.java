package no.haagensoftware.netty.xml;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ChapterSection {
	private String id;
	private String title;
	private String type;
	private String content;
	private Integer sectionIndex;
	private List<Paragraph> paragraphs;
	private List<ChapterSection> subSections;
	
	public ChapterSection() {
		paragraphs = new ArrayList<Paragraph>();
		subSections = new ArrayList<ChapterSection>();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public List<Paragraph> getParagraphs() {
		return paragraphs;
	}
	public void setParagraphs(List<Paragraph> paragraphs) {
		this.paragraphs = paragraphs;
	}
	public List<ChapterSection> getSubSections() {
		return subSections;
	}
	public void setSubSections(List<ChapterSection> subSections) {
		this.subSections = subSections;
	}
	public Integer getSectionIndex() {
		return sectionIndex;
	}
	public void setSectionIndex(Integer sectionIndex) {
		this.sectionIndex = sectionIndex;
	}
	
	public JsonObject toJSON() {
		JsonObject sectionObj = new JsonObject();
		sectionObj.addProperty("id", this.getId());
		sectionObj.addProperty("type", this.getType());
		sectionObj.addProperty("title", this.getTitle());
		sectionObj.addProperty("sectionIndex", this.getSectionIndex());
		
		JsonArray pars = new JsonArray();
		for (Paragraph par : getParagraphs()) {
			JsonObject paragraph = new JsonObject();
			paragraph.addProperty("id", par.getId());
			pars.add(paragraph);
		}
		sectionObj.add("paragraphs", pars);
		return sectionObj;
	}
	
}
