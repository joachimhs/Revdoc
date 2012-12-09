package org.haagensoftware.netty.webserver.plugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import no.haagensoftware.bookreview.auth.MozillaPersonaCredentials;
import no.haagensoftware.bookreview.repository.DocumentsCache;
import no.haagensoftware.bookreview.repository.DocumentsRepository;
import no.haagensoftware.bookreview.repository.User;
import no.haagensoftware.bookreview.repository.XmlDB;
import no.haagensoftware.bookreview.repository.query.Parameter;
import no.haagensoftware.netty.webserver.ServerInfo;
import no.haagensoftware.netty.webserver.handler.FileServerHandler;
import no.haagensoftware.netty.xml.Chapter;
import no.haagensoftware.netty.xml.ListXmlChapters;
import no.haagensoftware.netty.xml.ParseChapterXml;

import org.apache.log4j.Logger;
import org.haagensoftware.netty.webserver.spi.NettyWebserverRouterPlugin;
import org.jboss.netty.channel.ChannelHandler;
import org.xml.sax.SAXException;

/**
 * A simple router plugin to be able to serve the Haagen-Software.no website. 
 * @author joahaa
 *
 */
public class SimpleNettyRouterPlugin extends NettyWebserverRouterPlugin {
	private List<String> routes;
	private DocumentsRepository documentsRepository;
	private DocumentsCache documentsCache;
	private ServerInfo serverInfo;
	private static Logger logger = Logger.getLogger(SimpleNettyRouterPlugin.class.getName());
	private Hashtable<String, MozillaPersonaCredentials> authenticatedUsers = new Hashtable<String, MozillaPersonaCredentials>(); 
	
	private void connectToDB() throws SQLException {
		XmlDB xmldb = new XmlDB(
				System.getProperty("revdoc.dbName"),
				System.getProperty("revdoc.dbHost"),
				System.getProperty("revdoc.dbUser"),
				System.getProperty("revdoc.dbPass"),
				System.getProperty("revdoc.dbQueryFile")
				);
		
		xmldb.connectDB();
		documentsRepository = new DocumentsRepository(xmldb);
		documentsCache = new DocumentsCache();
	}
	
	public SimpleNettyRouterPlugin() throws SQLException {
		connectToDB();
		
		/*ListXmlChapters xmlChapters = new ListXmlChapters();
		ParseChapterXml parseXml = new ParseChapterXml();
		for (String xmlFile : xmlChapters.getXmlChapters(xmlDir)) {
			try {
				Chapter newChapter = parseXml.parse(xmlDir + File.separatorChar + xmlFile);
				chapters.add(newChapter);
				logger.info(newChapter);
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		
		routes = new ArrayList<String>();
		routes.add("equals:/chapters");
		routes.add("equals:/documents");
		routes.add("equals:/document");
		routes.add("startsWith:/uploadFile");
		routes.add("startsWith:/attachment");
		routes.add("equals:/auth/login");
		routes.add("startsWith:/chapter");
		routes.add("equals:/paragraphComment");
		routes.add("equals:/index.html");
		routes.add("equals:/");
		routes.add("startsWith:/cachedScript");
		//routes.add("endsWith:.json");
		//routes.add("endsWith:.jsons");
	}
	
	@Override
	public List<String> getRoutes() {
		return routes;
	}

	@Override
	public ChannelHandler getHandlerForRoute(String route) {
		//TODO: Expand with logic for handling specific routes.
		/*if (route.equalsIgnoreCase("startsWith:/cachedScript")) {
			return new CachedScriptHandler(route.substring(11));
		}
		return new CachedIndexHandler(serverInfo.getWebappPath(), 60);*/
		
		if (route.equalsIgnoreCase("equals:/documents")) {
			return new DocumentsHandler(serverInfo.getWebappPath(), documentsRepository, documentsCache, authenticatedUsers);
		} else if (route.equalsIgnoreCase("equals:/document")) {
			return new DocumentHandler(serverInfo.getWebappPath(), documentsRepository, documentsCache, authenticatedUsers);
		} else if (route.equalsIgnoreCase("equals:/chapters")) {
			return new ChaptersHandler(serverInfo.getWebappPath(), documentsRepository);
		} else if (route.equalsIgnoreCase("startsWith:/chapter")) {
			return new ChapterHandler(serverInfo.getWebappPath(), documentsRepository, documentsCache, authenticatedUsers);
		} else if (route.equalsIgnoreCase("equals:/auth/login")) {
			return new LoginHandler(serverInfo.getWebappPath(), authenticatedUsers);
		} else if (route.equalsIgnoreCase("equals:/paragraphComment")) {
			return new ParagraphCommentHandler(serverInfo.getWebappPath(), authenticatedUsers, documentsCache, documentsRepository);
		} else if (route.equalsIgnoreCase("startsWith:/uploadFile")) {
			return new FileUploadHandler(serverInfo.getWebappPath(), authenticatedUsers);
		} else if (route.equalsIgnoreCase("startsWith:/attachment")) {
			return new FileServerHandler(System.getProperty("revdoc.attachmentDir"));
		}
		
		return new FileServerHandler(serverInfo.getWebappPath());
	}
	
	@Override
	public void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
		
	}

}
