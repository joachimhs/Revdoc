package no.haagensoftware.netty.xml;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import no.haagensoftware.bookreview.repository.DSJDBPopulatedObject;
import no.haagensoftware.bookreview.repository.DocumentChapterComment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Chapter implements DSJDBPopulatedObject {
	private String id;
	private String title;
	private int index;
	private ChapterAbstract chapterAbstract;
	private List<ChapterSection> sections;
	private List<DocumentChapterComment> documentChapterComments;
	
	public Chapter() {
		chapterAbstract = new ChapterAbstract();
		sections = new ArrayList<ChapterSection>();
		documentChapterComments = new ArrayList<DocumentChapterComment>();
	}
	
	@Override
	public void populate(ResultSet resultset) throws SQLException {
		this.setId(resultset.getString("ChapterId"));
		this.setTitle(resultset.getString("ChapterTitle"));
		this.setIndex(resultset.getInt("ChapterIndex"));
		String content = resultset.getString("ChapterText");
		if (content != null && content.length() > 3) {
			ParseChapterXml xmlParser = new ParseChapterXml();
			try {
				Chapter ch = xmlParser.parseFromString(content);
				this.setChapterAbstract(ch.getChapterAbstract());
				this.setSections(ch.getSections());
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void update(Chapter newChapter) {
		if (newChapter != null && this.id.equals(newChapter.getId())) {
			setTitle(newChapter.getTitle());
			setSections(newChapter.getSections());
		}
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
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public ChapterAbstract getChapterAbstract() {
		return chapterAbstract;
	}
	public void setChapterAbstract(ChapterAbstract chapterAbstract) {
		this.chapterAbstract = chapterAbstract;
	}
	public List<ChapterSection> getSections() {
		return sections;
	}
	public void setSections(List<ChapterSection> sections) {
		this.sections = sections;
	}
	public List<DocumentChapterComment> getDocumentChapterComments() {
		return documentChapterComments;
	}
	public void setDocumentChapterComments(
			List<DocumentChapterComment> documentChapterComments) {
		this.documentChapterComments = documentChapterComments;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getTitle()).append("\n");
		
		for (Paragraph listItemParagraph : getChapterAbstract().getItemlist()) {
			sb.append(" - " + listItemParagraph.getContent()).append("\n");
		}
		
		for (Paragraph paragraph : this.getChapterAbstract().getParagraphs()) {
			sb.append(paragraph.getId() + " - " + paragraph.getContent()).append("\n");
		}
		
		for (ChapterSection section : this.getSections()) {
			sb.append(section.getTitle());
			
			for (Paragraph paragraph : section.getParagraphs()) {
				sb.append(paragraph.getId() + " - " + paragraph.getContent()).append("\n");
				if (paragraph.getFigure() != null) {
					sb.append("Figure: ").append(paragraph.getFigure().getId()).append(" - ").append(paragraph.getFigure().getTitle()).append(" - ").append(paragraph.getFigure().getLink()).append("\n");
				}
				if (paragraph.getExample() != null) {
					sb.append("Example: ").append(paragraph.getExample().getId()).append(" - ").append(paragraph.getExample().getTitle()).append(" - ").append(paragraph.getExample().getLink()).append("\n");
				}
			}
		}
		
		return sb.toString();
	}
	
	public JsonObject toSimpleJSON() {
		JsonObject chapter = new JsonObject();
		chapter.addProperty("title", this.getTitle());
		chapter.addProperty("index", this.getIndex());
		chapter.addProperty("id", this.getId());
		
		JsonArray sections = new JsonArray();
		for (ChapterSection section : getSections()) {
			sections.add(new JsonPrimitive(section.getId()));
		}
		chapter.add("sections", sections);
		
		return chapter;
	}
	
	public JsonObject toJSON(String username, String documentId) {
		JsonArray chapterAbstractsJson = new JsonArray();
		JsonArray chapterSectionsJson = new JsonArray();
		JsonArray chapterParagraphsJson = new JsonArray();
		JsonArray chapterCommentsJson = new JsonArray();
		JsonArray chapterFiguresJson = new JsonArray();
		JsonArray chapterExamplesJson = new JsonArray();
		
		JsonObject mainObject = new JsonObject();
		
		JsonObject chapter = new JsonObject();
		chapter.addProperty("title", this.getTitle());
		chapter.addProperty("index", this.getIndex());
		chapter.addProperty("id", this.getId());
		
		if (getChapterAbstract() != null) {
			chapter.addProperty("abstract", getChapterAbstract().getId());
			JsonObject chapterAbstractJson = getChapterAbstract().toJSON();
			chapterAbstractJson.add("chapter", new JsonPrimitive(this.getId()));
			chapterAbstractsJson.add(chapterAbstractJson);
			
			int paragraphIndex = 0;
			for (Paragraph par : getChapterAbstract().getItemlist()) {
				JsonObject paragraph = createChapterParagraph(par, chapterFiguresJson, chapterExamplesJson, username, documentId);
				paragraph.addProperty("paragraphIndex", ++paragraphIndex);
				chapterParagraphsJson.add(paragraph);
			}
			
			for (Paragraph par : getChapterAbstract().getParagraphs()) {
				JsonObject paragraph = createChapterParagraph(par, chapterFiguresJson, chapterExamplesJson, username, documentId);
				chapterParagraphsJson.add(paragraph);
			}
		}
		
		JsonArray sections = new JsonArray();
		for (ChapterSection section : getSections()) {
			chapterSectionsJson.add(createSectionJson(chapterParagraphsJson, chapterFiguresJson, chapterExamplesJson, section, username, documentId));
			sections.add(new JsonPrimitive(section.getId()));
			
			for (ChapterSection subSection : section.getSubSections()) {
				chapterSectionsJson.add(createSectionJson(chapterParagraphsJson, chapterFiguresJson, chapterExamplesJson, subSection, username, documentId));
			}
		}
		chapter.add("sections", sections);
		
		for (DocumentChapterComment dcc : this.getDocumentChapterComments()) {
			JsonObject dccJson = new JsonObject();
			dccJson.addProperty("id", dcc.getDocumentParagraphId());
			dccJson.addProperty("documentId", dcc.getDocumentId());
			dccJson.addProperty("chapterId", dcc.getChapterId());
			dccJson.addProperty("chapterParagraph", dcc.getParagraphId());
			dccJson.addProperty("commentFrom", dcc.getUserId());
			dccJson.addProperty("commentDate", dcc.getCommentPosted().toString());
			dccJson.addProperty("commentContent", dcc.getCommentText());
			chapterCommentsJson.add(dccJson);
			
		}
		JsonArray chaptersJson = new JsonArray();
		chaptersJson.add(chapter);
		mainObject.add("chapters", chaptersJson);
		mainObject.add("sections", chapterSectionsJson);
		mainObject.add("paragraphs", chapterParagraphsJson);
		mainObject.add("abstracts", chapterAbstractsJson);
		mainObject.add("comments", chapterCommentsJson);
		mainObject.add("figures", chapterFiguresJson);
		mainObject.add("examples", chapterExamplesJson);
		return mainObject;
	}

	private JsonObject createSectionJson(
			JsonArray chapterParagraphsJson,
			JsonArray chapterFiguresJson,
			JsonArray chapterExamplesJson,
			ChapterSection section,
			String username,
			String documentId) {
		JsonObject sectionObj = new JsonObject();
		sectionObj.addProperty("id", section.getId());
		sectionObj.addProperty("type", section.getType());
		sectionObj.addProperty("title", section.getTitle());
		sectionObj.addProperty("sectionIndex", section.getSectionIndex());
		sectionObj.addProperty("chapter", this.getId());
		
		JsonArray pars = new JsonArray();
		int paragraphIndex = 0;
		for (Paragraph par : section.getParagraphs()) {
			JsonObject paragraph = createChapterParagraph(par, chapterFiguresJson, chapterExamplesJson, username, documentId);
			paragraph.addProperty("chapterSection", section.getId());
			paragraph.addProperty("paragraphIndex", ++paragraphIndex);
			pars.add(new JsonPrimitive(par.getId()));
			chapterParagraphsJson.add(paragraph);
		}
		sectionObj.add("paragraphs", pars);
		
		JsonArray subSectionsJson = new JsonArray();
		for (ChapterSection subSection : section.getSubSections()) {
			subSectionsJson.add(new JsonPrimitive(subSection.getId()));
		}
		sectionObj.add("subSections", subSectionsJson);
		
		return sectionObj;
	}

	private JsonObject createChapterParagraph(Paragraph par, JsonArray chapterFiguresJson, JsonArray chapterExamplesJson, String username, String documentId) {
		JsonObject paragraph = new JsonObject();
		paragraph.addProperty("id", par.getId());
		paragraph.addProperty("content", par.getContent());
		if (par.getFigure() != null) {
			paragraph.add("figure", new JsonPrimitive(par.getFigure().getId()));
			JsonObject figureJson = new JsonObject();
			figureJson.addProperty("id", par.getFigure().getId());
			figureJson.addProperty("title", par.getFigure().getTitle());
			figureJson.addProperty("link", "attachment/" + username + "/" + documentId + "/" + par.getFigure().getLink());
			figureJson.addProperty("chapterParagraph", par.getId());
			chapterFiguresJson.add(figureJson);
		}
		if (par.getExample() != null) {
			paragraph.add("example", new JsonPrimitive(par.getExample().getId()));
			JsonObject exampleJson = new JsonObject();
			exampleJson.addProperty("id", par.getExample().getId());
			exampleJson.addProperty("title", par.getExample().getTitle());
			exampleJson.addProperty("link", "attachment/" + username + "/" + documentId + "/" + par.getExample().getLink());
			exampleJson.addProperty("chapterParagraph", par.getId());
			chapterExamplesJson.add(exampleJson);
		}
		JsonArray commentsJson = new JsonArray();
		for (DocumentChapterComment dcc : this.getDocumentChapterComments()) {
			if (dcc.getParagraphId().equals(par.getId())) {
				commentsJson.add(new JsonPrimitive(dcc.getDocumentParagraphId()));
			}
		}
		paragraph.add("comments", commentsJson);
		return paragraph;
	}
}
