package org.haagensoftware.netty.webserver.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import no.haagensoftware.bookreview.auth.MozillaPersonaCredentials;
import no.haagensoftware.bookreview.repository.DocumentsCache;
import no.haagensoftware.bookreview.repository.DocumentsRepository;
import no.haagensoftware.netty.webserver.handler.FileServerHandler;
import no.haagensoftware.netty.xml.Document;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DocumentsHandler extends FileServerHandler {
	private DocumentsRepository documentRepository;
	private DocumentsCache documentsCache;
	private Logger logger = Logger.getLogger(DocumentsHandler.class.getName());
	private Hashtable<String, MozillaPersonaCredentials> authenticatedUsers;

	public DocumentsHandler(String path, DocumentsRepository documentRepository, DocumentsCache documentsCache, Hashtable<String, MozillaPersonaCredentials> authenticatedUsers) {
		super(path);
		this.documentRepository = documentRepository;
		this.documentsCache = documentsCache;
		this.authenticatedUsers = authenticatedUsers;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		JsonObject responseObj = new JsonObject();
		
		String uuidToken = getCookieValue(e, "uuidToken");
		MozillaPersonaCredentials cred = authenticatedUsers.get(uuidToken);
		
		boolean authenticated = cred != null && cred.getStatus().equals("okay");
		
		if (!authenticated) {
			sendError(ctx, HttpResponseStatus.UNAUTHORIZED);
		} else if (isGet(e) && documentRepository != null) {
			List<Document> documents = documentRepository.getDocuments(cred.getEmail());
			
			JsonArray documentsArray = new JsonArray();
			
			String attachmentDir = (System.getProperty("revdoc.attachmentDir"));
			
			for (Document doc : documents) {
				doc.setChapters(documentRepository.getChapters(doc.getDocumentId()));
				
				File documentAttachmentDir = new File(attachmentDir + File.separatorChar + cred.getEmail() + File.separatorChar + doc.getDocumentId());
				if (documentAttachmentDir != null && documentAttachmentDir.exists() && documentAttachmentDir.isDirectory()) {
					List<String> attachments = new ArrayList<String>();
					for (File attachment : documentAttachmentDir.listFiles()) {
						if (attachment.getName().endsWith(".txt") ||
								attachment.getName().endsWith(".png") ||
								attachment.getName().endsWith(".gif") ||
								attachment.getName().endsWith(".jpg")) {
							attachments.add(attachment.getName());
						}
					}
					doc.setAttachments(attachments);
				}
				
				documentsArray.add(doc.toJSON());
				
				if (documentsCache != null) {
					documentsCache.addDocument(doc.getDocumentId(), doc);
				}
			}
			
			responseObj.add("documents", documentsArray);
		}

		writeContentsToBuffer(ctx, responseObj.toString(), "text/json");
	}
}
