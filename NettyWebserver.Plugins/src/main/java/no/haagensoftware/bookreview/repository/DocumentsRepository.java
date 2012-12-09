package no.haagensoftware.bookreview.repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import no.haagensoftware.bookreview.repository.query.Parameter;
import no.haagensoftware.netty.xml.Chapter;
import no.haagensoftware.netty.xml.Document;

public class DocumentsRepository {
	private XmlDB xmldb;
	
	public DocumentsRepository(XmlDB xmldb) {
		this.xmldb = xmldb;
	}
	
	public List<Document> getDocuments(String userId) {
		Vector<Parameter> paramList = new Vector<Parameter>();
		paramList.add(new Parameter(1, "userId", userId));
		
		List<Document> documents = new ArrayList<Document>();
		try {
			documents.addAll(xmldb.executeQuery("getDocuments", paramList, Document.class));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return documents;
	}
	
	public boolean createDocument(String documentId, String documentOwnerUserId, String documentTitle) {
		Vector<Parameter> paramList = new Vector<Parameter>();
		paramList.add(new Parameter(1, "documentId", documentId));
		paramList.add(new Parameter(2, "documentTitle", documentTitle));
		paramList.add(new Parameter(3, "documentOwnerUserId", documentOwnerUserId));
		paramList.add(new Parameter(4, "documentLastEditedBy", documentOwnerUserId));
		
		boolean success = xmldb.executeUpdate("createDocument", paramList);
		
		return success;
	}
	
	public boolean updateDocument(String documentId, String documentOwnerUserId, String documentTitle, String documentEditedBy) {
		Vector<Parameter> paramList = new Vector<Parameter>();
		paramList.add(new Parameter(1, "documentTitle", documentTitle));
		paramList.add(new Parameter(2, "documentLastEditedBy", documentEditedBy));
		paramList.add(new Parameter(3, "documentId", documentId));
		paramList.add(new Parameter(4, "documentOwnerUserId", documentOwnerUserId));
		
		boolean success = xmldb.executeUpdate("updateDocument", paramList);
		
		return success;
	}
	
	public boolean createChapter(String chapterId, String documentId, String chapterTitle, Integer chapterIndex) {
		Vector<Parameter> paramList = new Vector<Parameter>();
		paramList.add(new Parameter(1, "chapterId", chapterId));
		paramList.add(new Parameter(2, "documentId", documentId));
		paramList.add(new Parameter(3, "chapterTitle", chapterTitle));
		paramList.add(new Parameter(4, "chapterIndex", chapterIndex));
		
		boolean success = xmldb.executeUpdate("createChapter", paramList);
		
		return success;
	}
	
	public boolean updateChapter(String chapterId, String documentId, String chapterTitle, Integer chapterIndex, String chapterText) {
		Vector<Parameter> paramList = new Vector<Parameter>();
		paramList.add(new Parameter(1, "chapterTitle", chapterTitle));
		paramList.add(new Parameter(2, "chapterText", chapterText));
		paramList.add(new Parameter(3, "chapterIndex", chapterIndex));
		paramList.add(new Parameter(4, "chapterId", chapterId));
		paramList.add(new Parameter(5, "documentId", documentId));
		
		boolean success = xmldb.executeUpdate("updateChapter", paramList);
		
		return success;
	}
	
	public List<DocumentChapterComment> getDocumentChapterComments(String documentId, String chapterId) {
		Vector<Parameter> paramList = new Vector<Parameter>();
		paramList.add(new Parameter(1, "documentId", documentId));
		paramList.add(new Parameter(2, "chapterId", chapterId));
		
		List<DocumentChapterComment> documentComments = new ArrayList<DocumentChapterComment>();
		try {
			documentComments.addAll(xmldb.executeQuery("getDocumentComments", paramList, DocumentChapterComment.class));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return documentComments;
	}
	
	public List<Chapter> getChapters(String documentId) {
		Vector<Parameter> paramList = new Vector<Parameter>();
		paramList.add(new Parameter(1, "documentId", documentId));
		
		List<Chapter> chapters = new ArrayList<Chapter>();
		try {
			chapters.addAll(xmldb.executeQuery("getChapters", paramList, Chapter.class));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return chapters;
	}
	
	public boolean persistChapterComment(Vector<Parameter> paramList) {
		boolean success = false;
		
		xmldb.executeUpdate("persistChapterComment", paramList);
		
		return success;
	}
}
