package org.haagensoftware.netty.webserver.plugin;

import java.util.List;

import no.haagensoftware.bookreview.repository.DocumentsRepository;
import no.haagensoftware.netty.webserver.handler.FileServerHandler;
import no.haagensoftware.netty.xml.Chapter;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ChaptersHandler extends FileServerHandler {
	private DocumentsRepository documentRepository;

	public ChaptersHandler(String path, DocumentsRepository documentRepository) {
		super(path);
		this.documentRepository = documentRepository;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		JsonObject responseObj = new JsonObject();
		
		/*if (isGet(e)) {
			for (Chapter chapter : chapters) {
				responseObj = chapter.toJSON();
			}
		}*/

		writeContentsToBuffer(ctx, responseObj.toString(), "text/json");
	}
}
