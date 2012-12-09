package org.haagensoftware.netty.webserver.plugin;

import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import no.haagensoftware.bookreview.auth.MozillaPersonaCredentials;
import no.haagensoftware.netty.webserver.handler.FileServerHandler;

public class FileUploadHandler extends FileServerHandler {
	private Logger logger = Logger.getLogger(FileUploadHandler.class.getName());
	private Hashtable<String, MozillaPersonaCredentials> authenticatedUsers;
	
	public FileUploadHandler(String path, Hashtable<String, MozillaPersonaCredentials> authenticatedUsers) {
		super(path);
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
		} else {
			HttpRequest request = (HttpRequest) e.getMessage();
	        String uri = request.getUri();
	        
	        QueryStringDecoder qsd = new QueryStringDecoder(uri);
			List<String> filenames = qsd.getParameters().get("qqfile");
			List<String> documentIds = qsd.getParameters().get("documentId");
			
			if (filenames != null && filenames.size() > 0 && documentIds != null && documentIds.size() > 0) {
				ChannelBuffer content = request.getContent();
				ByteBuffer bf = content.toByteBuffer();
				
				String toDir = System.getProperty("revdoc.attachmentDir");
				String toFile = toDir + "/" + cred.getEmail() + "/" + documentIds.get(0) + "/" + filenames.get(0);
				FileOutputStream fos = new FileOutputStream(toFile);
				FileChannel out = fos.getChannel();
				out.write(bf);
				out.close();
				fos.close();
				
				response = "{\"success\":true}";
			} else {
				response = "{\"success\": false, \"error\": \"Either documentId or qqfile is missing form Query String!\"}";
			}
			
			writeContentsToBuffer(ctx, response, "text/plain");
		}
		
	}

}
