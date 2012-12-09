package no.haagensoftware.netty.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ParseChapterXml {
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		ParseChapterXml xmlParser = new ParseChapterXml();
		
		if (args.length == 1) {
			xmlParser.parse(args[0]);
		}
	}
	
	public Chapter parse(String filename) throws ParserConfigurationException, SAXException, IOException {
		DOMParser parser = new DOMParser();
		parser.parse(filename);
		Chapter chapter = parseFromDomParser(parser);
	    
		return chapter;
	}
	
	public Chapter parseFromString(String content) throws ParserConfigurationException, SAXException, IOException {
		DOMParser parser = new DOMParser();
		InputSource is = new InputSource(new StringReader(content));
		parser.parse(is);
		Chapter chapter = parseFromDomParser(parser);
	    
		return chapter;
	}
	
	

	private Chapter parseFromDomParser(DOMParser parser) {
		Document doc = parser.getDocument();
		NodeList root = doc.getChildNodes();
		AtomicInteger figureIndex = new AtomicInteger(1);
		AtomicInteger exampleIndex = new AtomicInteger(1);
		
		Chapter chapter = new Chapter();
		Node chapterNode = getNode("chapter", root);
		if (chapterNode != null) {
			List<Node> sectionNodes = getNodes("section", chapterNode.getChildNodes());
			chapter.setTitle(getTitleFromParent(chapterNode));
			chapter.setId(getNodeAttr("id", chapterNode));
			
			try {
				chapter.setIndex(Integer.parseInt(getNodeAttr("number", chapterNode)));
			} catch (NumberFormatException nfe) {
				chapter.setIndex(0);
			}
			
			int sectionIndex = 1;
			for (Node node : sectionNodes) {
				String nodeId = getNodeAttr("id", node);
				if (nodeId.endsWith("_abs")) {
					chapter.getChapterAbstract().setId(nodeId);
					chapter.getChapterAbstract().getParagraphs().addAll(getParagraphsFromNode(node, sectionIndex, figureIndex, exampleIndex));
					Node itemizedListNode = getNode("itemizedlist", node.getChildNodes());
					List<Node> itemizedListItems = getNodes("listitem", itemizedListNode.getChildNodes());
					for (Node currItem : itemizedListItems) {
						chapter.getChapterAbstract().getItemlist().addAll(getParagraphsFromNode(currItem, sectionIndex, figureIndex, exampleIndex));
					}
				} else {
					ChapterSection chapterSection = new ChapterSection();
					chapterSection.setId(getNodeAttr("id", node));
					chapterSection.setSectionIndex(sectionIndex);
					String title = getTitleFromParent(node);
					if (title != null) {
						chapterSection.setTitle(getTitleFromParent(node));
					}
					
					chapterSection.setParagraphs(getParagraphsFromNode(node, sectionIndex, figureIndex, exampleIndex));
					chapter.getSections().add(chapterSection);
					
					List<Node> subSectionNodes = getNodes("section", node.getChildNodes());
					int subsectionIndex = 1;
					for (Node subNode : subSectionNodes) { 
						ChapterSection chapterSubSection = new ChapterSection();
						chapterSubSection.setId(getNodeAttr("id", subNode));
						chapterSubSection.setSectionIndex(subsectionIndex);
						chapterSubSection.setTitle(getTitleFromParent(subNode));
						chapterSubSection.setParagraphs(getParagraphsFromNode(subNode, sectionIndex, figureIndex, exampleIndex));
						chapterSection.getSubSections().add(chapterSubSection);
						subsectionIndex++;
					}
					
					sectionIndex++;
				}				
			}
		}
		return chapter;
	}

	private String getTitleFromParent(Node chapterNode) {
		String title = null;
		
		Node titleNode = getNode("title", chapterNode.getChildNodes());
		if (titleNode != null) {
			title = getNodeValue(titleNode);
		}
		return title;
	}
	
	private List<Paragraph> getParagraphsFromNode(Node parentNode, int chapterIndex, AtomicInteger figureIndex, AtomicInteger exampleIndex) {
		List<Node> nodes = getNodes("para", parentNode.getChildNodes());
		List<Paragraph> paragraphs = new ArrayList<Paragraph>();
		
		for (int index = 0; index < nodes.size(); index++) {
			Node currNode = nodes.get(index);
			Paragraph paragraph = new Paragraph();
			paragraph.setId(getNodeAttr("id", currNode));
			paragraph.setContent(getNodeValue(currNode));
			List<Node> figure = getNodes("figure", currNode.getChildNodes());
			if (figure.size() > 0) {

				paragraph.setFigure(new ChapterParagraphFigure(
						getNodeAttr("id", figure.get(0)),
						chapterIndex + "." + figureIndex.getAndIncrement() + " - " + getNodeAttr("title", figure.get(0)),
						getNodeAttr("link", figure.get(0))
				));
			}
			
			List<Node> example = getNodes("example", currNode.getChildNodes());
			if (example.size() > 0) {

				paragraph.setExample(new ChapterParagraphExample(
						getNodeAttr("id", example.get(0)),
						chapterIndex + "." + exampleIndex.getAndIncrement() + " - " + getNodeAttr("title", example.get(0)),
						getNodeAttr("link", example.get(0))
				));
			}			
			
			paragraphs.add(paragraph);
		}
		
		return paragraphs;
	}
	
	protected Node getNode(String tagName, NodeList nodes) {
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            return node;
	        }
	    }
	 
	    return null;
	}
	
	protected List<Node> getNodes(String tagName, NodeList nodes) {
		List<Node> allNodes = new ArrayList<Node>();
		
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            allNodes.add(node);
	        }
	    }
	 
	    return allNodes;
	}
	 
	protected String getNodeValue( Node node ) {
	    NodeList childNodes = node.getChildNodes();
	    for (int x = 0; x < childNodes.getLength(); x++ ) {
	        Node data = childNodes.item(x);
	        if ( data.getNodeType() == Node.TEXT_NODE )
	            return data.getNodeValue();
	    }
	    return "";
	}
	 
	protected String getNodeValue(String tagName, NodeList nodes ) {
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            NodeList childNodes = node.getChildNodes();
	            for (int y = 0; y < childNodes.getLength(); y++ ) {
	                Node data = childNodes.item(y);
	                if ( data.getNodeType() == Node.TEXT_NODE )
	                    return data.getNodeValue();
	            }
	        }
	    }
	    return "";
	}
	 
	protected String getNodeAttr(String attrName, Node node ) {
	    NamedNodeMap attrs = node.getAttributes();
	    for (int y = 0; y < attrs.getLength(); y++ ) {
	        Node attr = attrs.item(y);
	        if (attr.getNodeName().equalsIgnoreCase(attrName)) {
	            return attr.getNodeValue();
	        }
	    }
	    return "";
	}
	 
	protected String getNodeAttr(String tagName, String attrName, NodeList nodes ) {
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            NodeList childNodes = node.getChildNodes();
	            for (int y = 0; y < childNodes.getLength(); y++ ) {
	                Node data = childNodes.item(y);
	                if ( data.getNodeType() == Node.ATTRIBUTE_NODE ) {
	                    if ( data.getNodeName().equalsIgnoreCase(attrName) )
	                        return data.getNodeValue();
	                }
	            }
	        }
	    }
	 
	    return "";
	}
}
