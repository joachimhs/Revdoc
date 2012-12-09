package no.haagensoftware.netty.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class ListXmlChapters {
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		ListXmlChapters listXml = new ListXmlChapters();
		if (args.length == 1) {
			for (String name : listXml.getXmlChapters(args[0])) {
				System.out.println(name);
				ParseChapterXml xml = new ParseChapterXml();
				Chapter chapter = xml.parse(args[0] + File.separatorChar + name);
				System.out.println(chapter.toString());
				System.out.println("-------<<<>>>---------");
			}
		}
	}
	
	public List<String> getXmlChapters(String path) {
		List<String> chapters = new ArrayList<String>();
		
		File chaptersDir = new File(path);
		if (chaptersDir.exists() && chaptersDir.isDirectory()) {
			for (File chapterFile : chaptersDir.listFiles()) {
				if (chapterFile.getName().endsWith(".xml")) {
					chapters.add(chapterFile.getName());
				}
			}
		}
		
		return chapters;
	}
}
