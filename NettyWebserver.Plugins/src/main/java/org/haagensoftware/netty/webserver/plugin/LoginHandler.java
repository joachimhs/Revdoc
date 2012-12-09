package org.haagensoftware.netty.webserver.plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import no.haagensoftware.bookreview.auth.MozillaPersonaCredentials;
import no.haagensoftware.netty.webserver.handler.FileServerHandler;

public class LoginHandler extends FileServerHandler {
	private Logger logger = Logger.getLogger(LoginHandler.class.getName());
	private Hashtable<String, MozillaPersonaCredentials> authenticatedUsers;
	
	public LoginHandler(String path, Hashtable<String, MozillaPersonaCredentials> authenticatedUsers) {
		super(path);
		this.authenticatedUsers = authenticatedUsers;
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		
		if (isPost(e)) {
			
			String messageContent = getHttpMessageContent(e);
			
			JsonObject assertionJson = new JsonObject();
			if (messageContent.startsWith("assertion=")) {
				messageContent = messageContent.substring(10, messageContent.length());
			}
			assertionJson.addProperty("assertion", messageContent);
			assertionJson.addProperty("audience", "http://localhost:8081");
			
			int statusCode = -1;
	        DefaultHttpClient httpclient = new DefaultHttpClient();
	        
	        
			HttpPost httpPost = new HttpPost("https://verifier.login.persona.org/verify");
			System.out.println(assertionJson.toString());
			StringEntity requestEntity = new StringEntity(assertionJson.toString(), "UTF-8");
	        //requestEntity.setContentType("application/x-www-form-urlencoded");
	        requestEntity.setContentType("application/json");

	        httpPost.setEntity(requestEntity);
	        HttpResponse response = httpclient.execute(httpPost);
	        statusCode = response.getStatusLine().getStatusCode();

	        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	        String responseContent = "";
	        String line = "";
	        while ((line = rd.readLine()) != null) {
	          responseContent = line + "\n";
	        }
	        
	        MozillaPersonaCredentials credentials = new Gson().fromJson(responseContent, MozillaPersonaCredentials.class);
	        if (credentials != null && credentials.getStatus().equalsIgnoreCase("okay")) {
	        	String uuid = UUID.randomUUID().toString();
	        	authenticatedUsers.put(uuid, credentials);
	        	responseContent = "{ \"uuidToken\": \"" + uuid + "\"}";
	        } else {
	        	responseContent = "{ \"authFailed\": true }";
	        }
	        
	        logger.info(responseContent);
	        
	        writeContentsToBuffer(ctx, responseContent, "text/json");
		}
	}	
}
