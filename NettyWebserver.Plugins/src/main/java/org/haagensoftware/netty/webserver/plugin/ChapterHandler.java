package org.haagensoftware.netty.webserver.plugin;

import java.net.URLDecoder;
import java.util.Hashtable;

import no.haagensoftware.bookreview.auth.MozillaPersonaCredentials;
import no.haagensoftware.bookreview.repository.DocumentsCache;
import no.haagensoftware.bookreview.repository.DocumentsRepository;
import no.haagensoftware.netty.webserver.handler.FileServerHandler;
import no.haagensoftware.netty.xml.Chapter;
import no.haagensoftware.netty.xml.Document;
import no.haagensoftware.netty.xml.ParseChapterXml;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.google.gson.Gson;

public class ChapterHandler extends FileServerHandler {
	private DocumentsRepository documentRepository;
	private DocumentsCache documentsCache;
	private Hashtable<String, MozillaPersonaCredentials> authenticatedUsers;
	
	public ChapterHandler(String path, DocumentsRepository documentRepository, DocumentsCache documentsCache, Hashtable<String, MozillaPersonaCredentials> authenticatedUsers) {
		super(path);
		this.documentRepository = documentRepository;
		this.documentsCache = documentsCache;
		this.authenticatedUsers = authenticatedUsers;
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		
		String jsonResponse = "";
				
		String uuidToken = getCookieValue(e, "uuidToken");
		MozillaPersonaCredentials cred = authenticatedUsers.get(uuidToken);
		
		boolean authenticated = cred != null && cred.getStatus().equals("okay");
		
		if (!authenticated) {
			sendError(ctx, HttpResponseStatus.UNAUTHORIZED);
		} else if (isGet(e)) {
			HttpRequest request = (HttpRequest)e.getMessage();
	        String uri = request.getUri();
			String queryString = URLDecoder.decode(uri.substring(uri.lastIndexOf('?')+1, uri.length()), "UTF-8");
			ChapterIdentifier ci = new Gson().fromJson(queryString, ChapterIdentifier.class);
			
			Chapter chapter = null;
			if (ci.getDocumentId() != null && ci.getChapterId() != null) {
				Document doc = documentsCache.getDocument(ci.getDocumentId());
				if (doc != null) {
					for (Chapter ch : doc.getChapters()) {
						if (ch.getId().equals(ci.getChapterId())) {
							chapter = ch;
							chapter.setDocumentChapterComments(documentRepository.getDocumentChapterComments(ci.getDocumentId(), ci.getChapterId()));
							break;
						}
					}
				}
				
				if (chapter != null) {
					jsonResponse = chapter.toJSON(doc.getDocumentOwnerUserId(), doc.getDocumentId()).toString();
				}
			}
		} else if (isPost(e) || isPut(e)) {
			String messageContent = getHttpMessageContent(e);
			
			ChapterJson chapterJson = new Gson().fromJson(messageContent, ChapterJson.class);
			Document doc = documentsCache.getDocument(chapterJson.getDocument());
			if (doc != null) {
				Chapter foundChapter = doc.getChapter(chapterJson.getId());
				if (foundChapter != null) {
					documentRepository.updateChapter(chapterJson.getId(), chapterJson.getDocument(), chapterJson.getTitle(), chapterJson.getIndex(), chapterJson.getText());
					ParseChapterXml parser = new ParseChapterXml();
					Chapter updatedChapter = parser.parseFromString(chapterJson.getText());
					foundChapter.update(updatedChapter);
				} else {
					documentRepository.createChapter(chapterJson.getId(), chapterJson.getDocument(), chapterJson.getTitle(), chapterJson.getIndex());
					foundChapter = new Chapter();
					foundChapter.setId(chapterJson.getId());
					foundChapter.setTitle(chapterJson.getTitle());
					foundChapter.setIndex(chapterJson.getIndex());
					doc.getChapters().add(foundChapter);
				}
				jsonResponse = foundChapter.toSimpleJSON().toString();
			}
		}
		
		writeContentsToBuffer(ctx, jsonResponse, "text/json");
	}

	class ChapterIdentifier {
		String chapterId;
		String documentId;
		
		public String getChapterId() {
			return chapterId;
		}
		
		public void setChapterId(String chapterId) {
			this.chapterId = chapterId;
		}
		
		public String getDocumentId() {
			return documentId;
		}
		
		public void setDocumentId(String documentId) {
			this.documentId = documentId;
		}
	}
	
	class ChapterJson {
		private String id;
		private String title;
		private String document;
		private Integer index;
		private String text;
		
		public ChapterJson() {
			
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

		public String getDocument() {
			return document;
		}

		public void setDocument(String document) {
			this.document = document;
		}

		public Integer getIndex() {
			return index;
		}

		public void setIndex(Integer index) {
			this.index = index;
		}
		
		public String getText() {
			return text;
		}
		
		public void setText(String text) {
			this.text = text;
		}
	}
}
