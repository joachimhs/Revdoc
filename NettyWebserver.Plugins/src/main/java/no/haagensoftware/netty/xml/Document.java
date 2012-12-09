package no.haagensoftware.netty.xml;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import no.haagensoftware.bookreview.repository.DSJDBPopulatedObject;

public class Document implements DSJDBPopulatedObject {
	private String documentId;
	private String documentText;
	private String documentOwnerUserId;
	private String documentTitle;
	private List<Chapter> chapters;
	private List<String> attachments;
	
	public Document() {
		this.chapters = new ArrayList<Chapter>();
		this.attachments = new ArrayList<String>();
	}
	
	@Override
	public void populate(ResultSet resultset) throws SQLException {
		this.setDocumentId(resultset.getString("DocumentId"));
		this.setDocumentText(resultset.getString("DocumentText"));
		this.setDocumentOwnerUserId(resultset.getString("DocumentOwnerUserId"));
		this.setDocumentTitle(resultset.getString("DocumentTitle"));
	}
	
	public String getDocumentId() {
		return documentId;
	}
	
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}
	
	public String getDocumentOwnerUserId() {
		return documentOwnerUserId;
	}
	
	public void setDocumentOwnerUserId(String documentOwnerUserId) {
		this.documentOwnerUserId = documentOwnerUserId;
	}
	
	public String getDocumentText() {
		return documentText;
	}
	
	public void setDocumentText(String documentText) {
		this.documentText = documentText;
	}
	
	public List<Chapter> getChapters() {
		return chapters;
	}
	
	public void setChapters(List<Chapter> chapters) {
		this.chapters = chapters;
	}
	
	public String getDocumentTitle() {
		return documentTitle;
	}
	
	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}
	
	public Chapter getChapter(String chapterId) {
		Chapter foundChapter = null;
		
		for (Chapter chapter : chapters) {
			if (chapter.getId().equals(chapterId)) {
				foundChapter = chapter;
				break;
			}
		}
		
		return foundChapter;
	}
	
	public List<String> getAttachments() {
		return attachments;
	}
	
	public void setAttachments(List<String> attachments) {
		this.attachments = attachments;
	}
	
	public JsonObject toJSON() {
		JsonObject docJson = new JsonObject();
		docJson.addProperty("id", this.getDocumentId());
		docJson.addProperty("documentOwnerUserId", this.getDocumentOwnerUserId());
		docJson.addProperty("title", this.getDocumentTitle());
		
		JsonArray chaptersJson = new JsonArray();
		for (Chapter chapter : chapters) {
			chaptersJson.add(new JsonPrimitive(chapter.getId()));
		}
		
		JsonArray attachmentsJson = new JsonArray();
		for (String attachment : getAttachments()) {
			attachmentsJson.add(new JsonPrimitive(attachment));
		}
		
		docJson.add("attachments", attachmentsJson);
		docJson.add("chapters", chaptersJson);
		
		return docJson;
	}
}
