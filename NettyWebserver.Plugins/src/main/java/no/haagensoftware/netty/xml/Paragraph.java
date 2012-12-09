package no.haagensoftware.netty.xml;

import java.util.List;

public class Paragraph {
	private String id;
	private String content;
	private ChapterParagraphFigure figure;
	private ChapterParagraphExample example;
	private List<String> bulletList;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		if (content != null) {
			content = content.trim();
		}
		this.content = content;
	}
	
	public ChapterParagraphFigure getFigure() {
		return figure;
	}
	
	public void setFigure(ChapterParagraphFigure figure) {
		this.figure = figure;
	}
	
	public ChapterParagraphExample getExample() {
		return example;
	}
	
	public void setExample(ChapterParagraphExample example) {
		this.example = example;
	}
	
	public List<String> getBulletList() {
		return bulletList;
	}
	
	public void setBulletList(List<String> bulletList) {
		this.bulletList = bulletList;
	}
	
	
}
