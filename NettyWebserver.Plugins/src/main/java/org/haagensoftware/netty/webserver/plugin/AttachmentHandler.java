package org.haagensoftware.netty.webserver.plugin;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import no.haagensoftware.netty.webserver.handler.FileServerHandler;

public class AttachmentHandler extends FileServerHandler {

	public AttachmentHandler(String path) {
		super(path);
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		// TODO Auto-generated method stub
		super.messageReceived(ctx, e);
	}

	

	
}
