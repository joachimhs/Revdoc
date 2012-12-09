package no.haagensoftware.bookreview.repository;

import java.util.Hashtable;

import no.haagensoftware.netty.xml.Document;

public class DocumentsCache {
	Hashtable<String, Document> docCache = new Hashtable<>();
	
	public DocumentsCache() {
		// TODO Auto-generated constructor stub
	}
	
	public Document getDocument(String key) {
		Document doc = docCache.get(key);
		return doc;
	}
	
	public void addDocument(String key, Document doc) {
		docCache.put(key, doc);
	}
}
