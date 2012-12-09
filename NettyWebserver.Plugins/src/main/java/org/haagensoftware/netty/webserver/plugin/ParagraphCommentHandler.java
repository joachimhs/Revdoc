package org.haagensoftware.netty.webserver.plugin;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.google.gson.Gson;

import no.haagensoftware.bookreview.auth.MozillaPersonaCredentials;
import no.haagensoftware.bookreview.repository.DocumentsCache;
import no.haagensoftware.bookreview.repository.DocumentsRepository;
import no.haagensoftware.bookreview.repository.query.Parameter;
import no.haagensoftware.netty.webserver.handler.FileServerHandler;
import no.haagensoftware.netty.xml.Document;

public class ParagraphCommentHandler extends FileServerHandler{
	private DocumentsRepository documentRepository;
	private Hashtable<String, MozillaPersonaCredentials> authenticatedUsers;
	private Logger logger = Logger.getLogger(ParagraphCommentHandler.class.getName());
	private DocumentsCache documentsCache;
	
	public ParagraphCommentHandler(String path, Hashtable<String, MozillaPersonaCredentials> authenticatedUsers, DocumentsCache documentsCache, DocumentsRepository documentRepository) {
		super(path);
		this.authenticatedUsers = authenticatedUsers;
		this.documentsCache = documentsCache;
		this.documentRepository = documentRepository;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		
		String uuidToken = getCookieValue(e, "uuidToken");
		MozillaPersonaCredentials cred = authenticatedUsers.get(uuidToken);
		
		boolean authenticated = cred != null && cred.getStatus().equals("okay");
		
		if (!authenticated) {
			sendError(ctx, HttpResponseStatus.UNAUTHORIZED);
		} else if (isPost(e) || isPut(e)) {
			String messageContent = getHttpMessageContent(e);
			logger.info(messageContent);
			ParagraphComment comment = new Gson().fromJson(messageContent, ParagraphComment.class);
			
			MozillaPersonaCredentials commentCred = authenticatedUsers.get(comment.commentFrom);
			if (commentCred != null && commentCred.getStatus().equals("okay")) {
				Vector<Parameter> paramList = new Vector<Parameter>();
				paramList.add(new Parameter(1, "documentParagraphCommentId", comment.getId()));
				paramList.add(new Parameter(2, "documentId", comment.getDocumentId()));
				paramList.add(new Parameter(3, "userId", commentCred.getEmail()));
				paramList.add(new Parameter(4, "chapterId", comment.getChapterId()));
				paramList.add(new Parameter(5, "paragraphId", comment.getChapterParagraph()));
				paramList.add(new Parameter(6, "commentText", comment.getCommentContent()));
				paramList.add(new Parameter(7, "commentPosted", new Date()));
				boolean success = documentRepository.persistChapterComment(paramList);
				
				logger.info("Adding comment status: " + success + " to Document: " + comment.getDocumentId() + ", chapter: " + comment.getChapterId() + " and paragraph: " + comment.getChapterParagraph());
			}
		} else {
			sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
		}
	}
	
	class ParagraphComment {
		private String id;
		private String documentId;
		private String chapterId;
		private String chapterParagraph;
		private String commentFrom;
		private String commentDate;
		private String commentContent;
		
		public ParagraphComment() {
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getDocumentId() {
			return documentId;
		}

		public void setDocumentId(String documentId) {
			this.documentId = documentId;
		}
		
		public String getChapterId() {
			return chapterId;
		}
		
		public void setChapterId(String chapterId) {
			this.chapterId = chapterId;
		}
		
		public String getChapterParagraph() {
			return chapterParagraph;
		}
		
		public void setChapterParagraph(String chapterParagraph) {
			this.chapterParagraph = chapterParagraph;
		}
		
		public String getCommentFrom() {
			return commentFrom;
		}

		public void setCommentFrom(String commentFrom) {
			this.commentFrom = commentFrom;
		}

		public String getCommentDate() {
			return commentDate;
		}

		public void setCommentDate(String commentDate) {
			this.commentDate = commentDate;
		}

		public String getCommentContent() {
			return commentContent;
		}

		public void setCommentContent(String commentContent) {
			this.commentContent = commentContent;
		}
	}
	
}
