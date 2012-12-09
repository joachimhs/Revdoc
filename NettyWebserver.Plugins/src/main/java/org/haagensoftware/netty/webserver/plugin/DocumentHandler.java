package org.haagensoftware.netty.webserver.plugin;

import java.util.Hashtable;

import no.haagensoftware.bookreview.auth.MozillaPersonaCredentials;
import no.haagensoftware.bookreview.repository.DocumentsCache;
import no.haagensoftware.bookreview.repository.DocumentsRepository;
import no.haagensoftware.netty.webserver.handler.FileServerHandler;
import no.haagensoftware.netty.xml.Document;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DocumentHandler extends FileServerHandler {
	private DocumentsRepository documentRepository;
	private DocumentsCache documentsCache;
	private Logger logger = Logger.getLogger(DocumentHandler.class.getName());
	private Hashtable<String, MozillaPersonaCredentials> authenticatedUsers;

	public DocumentHandler(String path, DocumentsRepository documentRepository, DocumentsCache documentsCache, Hashtable<String, MozillaPersonaCredentials> authenticatedUsers) {
		super(path);
		this.documentRepository = documentRepository;
		this.documentsCache = documentsCache;
		this.authenticatedUsers = authenticatedUsers;
	}
	
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		
		String response = "";
		
		String uuidToken = getCookieValue(e, "uuidToken");
		MozillaPersonaCredentials cred = authenticatedUsers.get(uuidToken);
		
		boolean authenticated = cred != null && cred.getStatus().equals("okay");
		
		if (!authenticated) {
			sendError(ctx, HttpResponseStatus.UNAUTHORIZED);
		} else if (isPut(e) || isPost(e)) {
			String messageContent = getHttpMessageContent(e);
			DocumentJsonObject doc = new Gson().fromJson(messageContent, DocumentJsonObject.class);
			
			//MozillaPersonaCredentials docCred = authenticatedUsers.get(doc.getDocumentOwnerUserId());
			//if (docCred != null && docCred.getStatus().equals("okay")) {
				if (documentsCache.getDocument(doc.getId()) == null) {
					//create
					documentRepository.createDocument(doc.getId(), cred.getEmail(), doc.getTitle());
					doc.setDocumentOwnerUserId(cred.getEmail());
					Document newDoc = new Document();
					newDoc.setDocumentId(doc.getId());
					newDoc.setDocumentOwnerUserId(cred.getEmail());
					documentsCache.addDocument(doc.getId(), newDoc);
				} else {
					//update
					documentRepository.updateDocument(doc.getId(), doc.getDocumentOwnerUserId(), doc.getTitle(), cred.getEmail());
				}
				
				Document oldDoc = documentsCache.getDocument(doc.getId());
				oldDoc.setDocumentTitle(doc.getTitle());
				
				response = oldDoc.toJSON().toString();
			//} else {
			//	logger.info("Unable to authenticate!: " + docCred);
			//}
		}
		
		writeContentsToBuffer(ctx, response, "text/json");
	}
	
	class DocumentJsonObject {
		private String id;
		private String title;
		private String documentOwnerUserId;
		
		public DocumentJsonObject() {
			// TODO Auto-generated constructor stub
		}
		
		public String getDocumentOwnerUserId() {
			return documentOwnerUserId;
		}
		
		public void setDocumentOwnerUserId(String documentOwnerUserId) {
			this.documentOwnerUserId = documentOwnerUserId;
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
	}
}
