package no.haagensoftware.bookreview.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class DocumentChapterComment implements DSJDBPopulatedObject {
	private String documentParagraphId;
	private String documentId;
	private String userId;
	private String chapterId;
	private String paragraphId;
	private String commentText;
	private Date commentPosted;
	
	@Override
	public void populate(ResultSet resultset) throws SQLException {
		this.setDocumentParagraphId(resultset.getString("DocumentParagraphCommentId"));
		this.setDocumentId(resultset.getString("DocumentId"));
		this.setUserId(resultset.getString("UserId"));
		this.setChapterId(resultset.getString("ChapterId"));
		this.setParagraphId(resultset.getString("ParagraphId"));
		this.setCommentText(resultset.getString("CommentText"));
		this.setCommentPosted(resultset.getDate("CommentPosted"));
	}
	
	public DocumentChapterComment() {
		// TODO Auto-generated constructor stub
	}

	public String getDocumentParagraphId() {
		return documentParagraphId;
	}
	
	public void setDocumentParagraphId(String documentParagraphId) {
		this.documentParagraphId = documentParagraphId;
	}
	
	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getChapterId() {
		return chapterId;
	}
	
	public void setChapterId(String chapterId) {
		this.chapterId = chapterId;
	}

	public String getParagraphId() {
		return paragraphId;
	}

	public void setParagraphId(String paragraphId) {
		this.paragraphId = paragraphId;
	}

	public String getCommentText() {
		return commentText;
	}

	public void setCommentText(String commentText) {
		this.commentText = commentText;
	}

	public Date getCommentPosted() {
		return commentPosted;
	}

	public void setCommentPosted(Date commentPosted) {
		this.commentPosted = commentPosted;
	}
	
	
}
